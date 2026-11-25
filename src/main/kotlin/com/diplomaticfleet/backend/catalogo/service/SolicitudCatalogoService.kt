package com.diplomaticfleet.backend.catalogo.service

import com.diplomaticfleet.backend.catalogo.domain.EstadoSolicitudCatalogo
import com.diplomaticfleet.backend.catalogo.domain.SolicitudCatalogo
import com.diplomaticfleet.backend.catalogo.dto.*
import com.diplomaticfleet.backend.catalogo.repository.SolicitudCatalogoRepository
import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import com.diplomaticfleet.backend.flota.domain.EstadoVehiculo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime

/** Separa el contacto comercial de la reserva formal y de la administración. */
@Service
class SolicitudCatalogoService(
    private val repository: SolicitudCatalogoRepository,
    private val catalogo: CatalogoVehiculoService,
) {
    /** Valida fechas y vehículo visible antes de persistir el contacto. */
    @Transactional
    fun registrar(request: SolicitudCatalogoRequest): SolicitudCreadaResponse {
        val inicio = requireNotNull(request.fechaInicio)
        val fin = requireNotNull(request.fechaFin)
        if (inicio.isBefore(LocalDate.now()) || fin.isBefore(inicio)) {
            throw ConflictoException("Las fechas solicitadas deben ser futuras y estar en orden.")
        }
        val vehiculo = catalogo.buscarVisible(requireNotNull(request.vehiculoId))
        if (vehiculo.estado != EstadoVehiculo.DISPONIBLE) {
            throw ConflictoException("El vehículo no acepta solicitudes en este momento.")
        }
        val solicitud = repository.save(
            SolicitudCatalogo(
                vehiculo = vehiculo,
                nombreContacto = request.nombreContacto.trim(),
                correoContacto = request.correoContacto.trim().lowercase(),
                entidad = request.entidad.trim(),
                telefono = request.telefono?.trim()?.ifBlank { null },
                fechaInicio = inicio,
                fechaFin = fin,
                mensaje = request.mensaje?.trim()?.ifBlank { null },
            ),
        )
        return SolicitudCreadaResponse(id = requireNotNull(solicitud.id), estado = solicitud.estado)
    }

    /** Recupera contactos comerciales en orden de creación descendente. */
    @Transactional(readOnly = true)
    fun listar(): List<SolicitudCatalogoAdminResponse> =
        repository.findAllByOrderByCreadoEnDesc().map(::convertir)

    /** Cambia el estado del interés comercial y registra la fecha de actualización. */
    @Transactional
    fun actualizarEstado(id: Long, nuevoEstado: EstadoSolicitudCatalogo): SolicitudCatalogoAdminResponse {
        val solicitud = repository.findById(id).orElseThrow {
            RecursoNoEncontradoException("No existe una solicitud comercial con ID $id.")
        }
        solicitud.estado = nuevoEstado
        solicitud.actualizadoEn = OffsetDateTime.now()
        return convertir(repository.save(solicitud))
    }

    /** Proyecta la entidad a los campos utilizados por la gestión comercial. */
    private fun convertir(solicitud: SolicitudCatalogo): SolicitudCatalogoAdminResponse =
        SolicitudCatalogoAdminResponse(
            id = requireNotNull(solicitud.id),
            vehiculoId = requireNotNull(solicitud.vehiculo.id),
            vehiculo = "${solicitud.vehiculo.marca} ${solicitud.vehiculo.modelo}",
            nombreContacto = solicitud.nombreContacto,
            correoContacto = solicitud.correoContacto,
            entidad = solicitud.entidad,
            telefono = solicitud.telefono,
            fechaInicio = solicitud.fechaInicio,
            fechaFin = solicitud.fechaFin,
            mensaje = solicitud.mensaje,
            estado = solicitud.estado,
            creadoEn = solicitud.creadoEn,
        )
}
