package com.diplomaticfleet.backend.clientes.service

import com.diplomaticfleet.backend.clientes.domain.ClienteInstitucional
import com.diplomaticfleet.backend.clientes.dto.ClienteInstitucionalRequest
import com.diplomaticfleet.backend.clientes.dto.ClienteInstitucionalResponse
import com.diplomaticfleet.backend.clientes.repository.ClienteInstitucionalRepository
import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Servicio encargado de gestionar clientes institucionales.
 *
 * Centraliza el registro, la consulta y la actualización de
 * embajadas, consulados y organismos que solicitan servicios.
 *
 * @property repository Repositorio de clientes institucionales.
 */
@Service
class ClienteInstitucionalService(
    private val repository: ClienteInstitucionalRepository,
) {

    /**
     * Registra un nuevo cliente institucional.
     *
     * @param request Datos validados de la institución.
     * @return Información del cliente registrado.
     * @throws ConflictoException Si el nombre, correo o número fiscal ya existen.
     */
    @Transactional
    fun registrar(
        request: ClienteInstitucionalRequest,
    ): ClienteInstitucionalResponse {
        val nombreEntidad = request.nombreEntidad.trim()
        val correoContacto = request.correoContacto.trim().lowercase()
        val identificacionFiscal =
            normalizarTextoOpcional(
                request.numeroIdentificacionFiscal,
            )?.uppercase()

        validarDatosUnicos(
            nombreEntidad = nombreEntidad,
            correoContacto = correoContacto,
            identificacionFiscal = identificacionFiscal,
        )

        val cliente = ClienteInstitucional(
            nombreEntidad = nombreEntidad,
            tipoEntidad = requireNotNull(request.tipoEntidad) {
                "El tipo de entidad es obligatorio."
            },
            numeroIdentificacionFiscal = identificacionFiscal,
            nombreContacto = request.nombreContacto.trim(),
            correoContacto = correoContacto,
            telefonoContacto =
                normalizarTextoOpcional(request.telefonoContacto),
            direccion =
                normalizarTextoOpcional(request.direccion),
            ciudad =
                normalizarTextoOpcional(request.ciudad),
            activo = request.activo,
        )

        return convertirAResponse(
            repository.save(cliente),
        )
    }

    /**
     * Obtiene todos los clientes institucionales.
     *
     * @return Clientes ordenados alfabéticamente.
     */
    @Transactional(readOnly = true)
    fun listar(): List<ClienteInstitucionalResponse> =
        repository.findAll()
            .sortedBy { cliente ->
                cliente.nombreEntidad.lowercase()
            }
            .map(::convertirAResponse)

    /**
     * Consulta un cliente mediante su identificador.
     *
     * @param id Identificador solicitado.
     * @return Cliente institucional encontrado.
     * @throws RecursoNoEncontradoException Si el cliente no existe.
     */
    @Transactional(readOnly = true)
    fun obtenerPorId(
        id: Long,
    ): ClienteInstitucionalResponse =
        convertirAResponse(
            buscarEntidadPorId(id),
        )

    /**
     * Actualiza un cliente institucional.
     *
     * @param id Identificador del cliente.
     * @param request Nuevos datos validados.
     * @return Información actualizada.
     * @throws RecursoNoEncontradoException Si el cliente no existe.
     * @throws ConflictoException Si otro cliente utiliza un dato único.
     */
    @Transactional
    fun actualizar(
        id: Long,
        request: ClienteInstitucionalRequest,
    ): ClienteInstitucionalResponse {
        val cliente = buscarEntidadPorId(id)
        val nombreEntidad = request.nombreEntidad.trim()
        val correoContacto = request.correoContacto.trim().lowercase()
        val identificacionFiscal =
            normalizarTextoOpcional(
                request.numeroIdentificacionFiscal,
            )?.uppercase()

        validarDatosUnicos(
            nombreEntidad = nombreEntidad,
            correoContacto = correoContacto,
            identificacionFiscal = identificacionFiscal,
            clienteActualId = id,
        )

        cliente.apply {
            this.nombreEntidad = nombreEntidad
            tipoEntidad = requireNotNull(request.tipoEntidad) {
                "El tipo de entidad es obligatorio."
            }
            numeroIdentificacionFiscal = identificacionFiscal
            nombreContacto = request.nombreContacto.trim()
            this.correoContacto = correoContacto
            telefonoContacto =
                normalizarTextoOpcional(request.telefonoContacto)
            direccion =
                normalizarTextoOpcional(request.direccion)
            ciudad =
                normalizarTextoOpcional(request.ciudad)
            activo = request.activo
            actualizadoEn = OffsetDateTime.now()
        }

        return convertirAResponse(
            repository.save(cliente),
        )
    }

    /**
     * Comprueba que los valores restringidos por índices únicos
     * no correspondan a otra institución.
     */
    private fun validarDatosUnicos(
        nombreEntidad: String,
        correoContacto: String,
        identificacionFiscal: String?,
        clienteActualId: Long? = null,
    ) {
        val clientePorNombre =
            repository.findByNombreEntidadIgnoreCase(nombreEntidad)

        if (
            clientePorNombre != null &&
            clientePorNombre.id != clienteActualId
        ) {
            throw ConflictoException(
                "Ya existe una institución registrada con el nombre $nombreEntidad.",
            )
        }

        val clientePorCorreo =
            repository.findByCorreoContactoIgnoreCase(correoContacto)

        if (
            clientePorCorreo != null &&
            clientePorCorreo.id != clienteActualId
        ) {
            throw ConflictoException(
                "El correo de contacto ya pertenece a otra institución.",
            )
        }

        if (identificacionFiscal != null) {
            val clientePorIdentificacion =
                repository.findByNumeroIdentificacionFiscal(
                    identificacionFiscal,
                )

            if (
                clientePorIdentificacion != null &&
                clientePorIdentificacion.id != clienteActualId
            ) {
                throw ConflictoException(
                    "La identificación fiscal ya está registrada.",
                )
            }
        }
    }

    /**
     * Busca internamente una entidad institucional.
     */
    private fun buscarEntidadPorId(
        id: Long,
    ): ClienteInstitucional =
        repository.findById(id)
            .orElseThrow {
                RecursoNoEncontradoException(
                    "No existe un cliente institucional con el identificador $id.",
                )
            }

    /**
     * Elimina espacios externos y convierte cadenas vacías en `null`.
     */
    private fun normalizarTextoOpcional(
        value: String?,
    ): String? =
        value
            ?.trim()
            ?.takeIf(String::isNotEmpty)

    /**
     * Convierte la entidad JPA en un DTO seguro para la API.
     */
    private fun convertirAResponse(
        cliente: ClienteInstitucional,
    ): ClienteInstitucionalResponse =
        ClienteInstitucionalResponse(
            id = requireNotNull(cliente.id) {
                "El cliente persistido no tiene identificador."
            },
            nombreEntidad = cliente.nombreEntidad,
            tipoEntidad = cliente.tipoEntidad,
            numeroIdentificacionFiscal =
                cliente.numeroIdentificacionFiscal,
            nombreContacto = cliente.nombreContacto,
            correoContacto = cliente.correoContacto,
            telefonoContacto = cliente.telefonoContacto,
            direccion = cliente.direccion,
            ciudad = cliente.ciudad,
            activo = cliente.activo,
            creadoEn = cliente.creadoEn,
            actualizadoEn = cliente.actualizadoEn,
        )
}