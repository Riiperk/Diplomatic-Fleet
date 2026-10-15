package com.diplomaticfleet.backend.flota.service

import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import com.diplomaticfleet.backend.flota.domain.EstadoVehiculo
import com.diplomaticfleet.backend.flota.domain.Vehiculo
import com.diplomaticfleet.backend.flota.dto.VehiculoRequest
import com.diplomaticfleet.backend.flota.dto.VehiculoResponse
import com.diplomaticfleet.backend.flota.repository.CategoriaVehiculoRepository
import com.diplomaticfleet.backend.flota.repository.VehiculoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Servicio encargado de gestionar los vehículos de la flota.
 *
 * Aplica las reglas de negocio relacionadas con el registro,
 * la consulta y la actualización de vehículos, manteniendo
 * separadas las entidades JPA de los datos expuestos por la API.
 *
 * @property vehiculoRepository Repositorio de vehículos.
 * @property categoriaVehiculoRepository Repositorio de categorías.
 */
@Service
class VehiculoService(
    private val vehiculoRepository: VehiculoRepository,
    private val categoriaVehiculoRepository: CategoriaVehiculoRepository,
) {

    /**
     * Registra un vehículo nuevo en la flota.
     *
     * La placa se normaliza antes de comprobar duplicados y persistirla.
     * Todo vehículo nuevo comienza en estado [EstadoVehiculo.DISPONIBLE].
     *
     * @param request Datos validados del vehículo.
     * @return Información del vehículo registrado.
     * @throws ConflictoException Si la placa ya está registrada.
     * @throws RecursoNoEncontradoException Si la categoría no existe.
     */
    @Transactional
    fun registrar(
        request: VehiculoRequest,
    ): VehiculoResponse {
        val placaNormalizada = normalizarPlaca(request.placa)

        if (vehiculoRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            throw ConflictoException(
                "Ya existe un vehículo registrado con la placa $placaNormalizada.",
            )
        }

        val categoriaId = requireNotNull(request.categoriaId) {
            "La categoría del vehículo es obligatoria."
        }

        val categoria = categoriaVehiculoRepository.findById(categoriaId)
            .orElseThrow {
                RecursoNoEncontradoException(
                    "No existe una categoría de vehículo con el identificador $categoriaId.",
                )
            }

        if (!categoria.activo) {
            throw ConflictoException(
                "La categoría seleccionada no está activa.",
            )
        }

        val vehiculo = Vehiculo(
            categoria = categoria,
            placa = placaNormalizada,
            marca = request.marca.trim(),
            modelo = request.modelo.trim(),
            anio = requireNotNull(request.anio) {
                "El año de fabricación es obligatorio."
            },
            color = request.color.trim(),
            combustible = requireNotNull(request.combustible) {
                "El tipo de combustible es obligatorio."
            },
            capacidadPasajeros = requireNotNull(request.capacidadPasajeros) {
                "La capacidad de pasajeros es obligatoria."
            },
            capacidadEquipaje = request.capacidadEquipaje,
            estado = EstadoVehiculo.DISPONIBLE,
            activo = request.activo,
        )

        return convertirAResponse(
            vehiculoRepository.save(vehiculo),
        )
    }

    /**
     * Obtiene todos los vehículos registrados.
     *
     * @return Vehículos convertidos a objetos de respuesta.
     */
    @Transactional(readOnly = true)
    fun listar(): List<VehiculoResponse> =
        vehiculoRepository.findAll()
            .map(::convertirAResponse)

    /**
     * Consulta un vehículo mediante su identificador.
     *
     * @param id Identificador del vehículo.
     * @return Información del vehículo encontrado.
     * @throws RecursoNoEncontradoException Si el vehículo no existe.
     **/
    @Transactional(readOnly = true)
    fun obtenerPorId(
        id: Long,
    ): VehiculoResponse =
        convertirAResponse(
            buscarEntidadPorId(id),
        )

    /**
     * Actualiza los datos generales de un vehículo.
     *
     * El estado operativo no se modifica mediante esta operación,
     * porque depende de las reservas, servicios y mantenimientos.
     *
     * @param id Identificador del vehículo que se actualizará.
     * @param request Nuevos datos validados.
     * @return Información actualizada del vehículo.
     * @throws RecursoNoEncontradoException Si el vehículo o la categoría no existen.
     * @throws ConflictoException Si la placa pertenece a otro vehículo.
     */
    @Transactional
    fun actualizar(
        id: Long,
        request: VehiculoRequest,
    ): VehiculoResponse {
        val vehiculo = buscarEntidadPorId(id)
        val placaNormalizada = normalizarPlaca(request.placa)

        val vehiculoConMismaPlaca =
            vehiculoRepository.findByPlacaIgnoreCase(placaNormalizada)

        if (
            vehiculoConMismaPlaca != null &&
            vehiculoConMismaPlaca.id != vehiculo.id
        ) {
            throw ConflictoException(
                "Ya existe otro vehículo registrado con la placa $placaNormalizada.",
            )
        }

        val categoriaId = requireNotNull(request.categoriaId) {
            "La categoría del vehículo es obligatoria."
        }

        val categoria = categoriaVehiculoRepository.findById(categoriaId)
            .orElseThrow {
                RecursoNoEncontradoException(
                    "No existe una categoría de vehículo con el identificador $categoriaId.",
                )
            }

        if (!categoria.activo) {
            throw ConflictoException(
                "La categoría seleccionada no está activa.",
            )
        }

        vehiculo.apply {
            this.categoria = categoria
            placa = placaNormalizada
            marca = request.marca.trim()
            modelo = request.modelo.trim()
            anio = requireNotNull(request.anio) {
                "El año de fabricación es obligatorio."
            }
            color = request.color.trim()
            combustible = requireNotNull(request.combustible) {
                "El tipo de combustible es obligatorio."
            }
            capacidadPasajeros = requireNotNull(request.capacidadPasajeros) {
                "La capacidad de pasajeros es obligatoria."
            }
            capacidadEquipaje = request.capacidadEquipaje
            activo = request.activo
            actualizadoEn = OffsetDateTime.now()
        }

        return convertirAResponse(
            vehiculoRepository.save(vehiculo),
        )
    }

    /**
     * Busca internamente una entidad de vehículo.
     *
     * @param id Identificador solicitado.
     * @return Entidad encontrada.
     * @throws RecursoNoEncontradoException Si no existe el vehículo.
     */
    private fun buscarEntidadPorId(
        id: Long,
    ): Vehiculo =
        vehiculoRepository.findById(id)
            .orElseThrow {
                RecursoNoEncontradoException(
                    "No existe un vehículo con el identificador $id.",
                )
            }

    /**
     * Normaliza una placa eliminando espacios externos
     * y convirtiéndola a mayúsculas.
     *
     * @param placa Placa recibida.
     * @return Placa normalizada.
     */
    private fun normalizarPlaca(
        placa: String,
    ): String =
        placa.trim().uppercase()

    /**
     * Convierte una entidad JPA en un objeto de respuesta.
     *
     * @param vehiculo Entidad que se convertirá.
     * @return Datos seguros para exponer mediante la API.
     */
    private fun convertirAResponse(
        vehiculo: Vehiculo,
    ): VehiculoResponse =
        VehiculoResponse(
            id = requireNotNull(vehiculo.id) {
                "El vehículo persistido no tiene identificador."
            },
            categoriaId = requireNotNull(vehiculo.categoria.id) {
                "La categoría asociada no tiene identificador."
            },
            codigoCategoria = vehiculo.categoria.codigo,
            nombreCategoria = vehiculo.categoria.nombre,
            placa = vehiculo.placa,
            marca = vehiculo.marca,
            modelo = vehiculo.modelo,
            anio = vehiculo.anio,
            color = vehiculo.color,
            combustible = vehiculo.combustible,
            capacidadPasajeros = vehiculo.capacidadPasajeros,
            capacidadEquipaje = vehiculo.capacidadEquipaje,
            estado = vehiculo.estado,
            activo = vehiculo.activo,
            creadoEn = vehiculo.creadoEn,
            actualizadoEn = vehiculo.actualizadoEn,
        )
}