package com.diplomaticfleet.backend.catalogo.service

import com.diplomaticfleet.backend.catalogo.dto.CatalogoVehiculoResponse
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import com.diplomaticfleet.backend.flota.domain.Vehiculo
import com.diplomaticfleet.backend.flota.repository.VehiculoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Proyecta la flota de PostgreSQL como un catálogo comercial público.
 * No hay arreglos simulados ni se filtran placas hacia la página pública.
 */
@Service
class CatalogoVehiculoService(private val repository: VehiculoRepository) {
    /** Obtiene vehículos activos y los transforma en fichas sin datos internos. */
    @Transactional(readOnly = true)
    fun listar(): List<CatalogoVehiculoResponse> =
        repository.findAllByActivoTrueOrderByMarcaAscModeloAsc().map(::convertir)

    /** Recupera una ficha tras verificar su publicación. */
    @Transactional(readOnly = true)
    fun detalle(id: Long): CatalogoVehiculoResponse =
        convertir(buscarVisible(id))

    /** Reutilizado al registrar intereses para evitar vehículos eliminados de la oferta. */
    @Transactional(readOnly = true)
    fun buscarVisible(id: Long): Vehiculo {
        val vehiculo = repository.findById(id).orElseThrow {
            RecursoNoEncontradoException("No existe el vehículo solicitado.")
        }
        if (!vehiculo.activo) {
            throw RecursoNoEncontradoException("El vehículo no está publicado en el catálogo.")
        }
        return vehiculo
    }

    /** Transforma la entidad JPA a un DTO comercial sin exponer la placa. */
    private fun convertir(vehiculo: Vehiculo): CatalogoVehiculoResponse =
        CatalogoVehiculoResponse(
            id = requireNotNull(vehiculo.id),
            marca = vehiculo.marca,
            modelo = vehiculo.modelo,
            anio = vehiculo.anio,
            categoria = vehiculo.categoria.codigo,
            nombreCategoria = vehiculo.categoria.nombre,
            color = vehiculo.color,
            combustible = vehiculo.combustible,
            pasajeros = vehiculo.capacidadPasajeros,
            equipaje = vehiculo.capacidadEquipaje,
            estado = vehiculo.estado,
            tarifaDiaria = vehiculo.tarifaDiaria,
            imagenUrl = vehiculo.imagenUrl,
            descripcion = vehiculo.descripcion,
            blindado = vehiculo.blindado,
        )
}
