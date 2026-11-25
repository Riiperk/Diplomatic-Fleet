package com.diplomaticfleet.backend.catalogo.dto

import com.diplomaticfleet.backend.catalogo.domain.EstadoSolicitudCatalogo
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.OffsetDateTime

/** Datos públicos validados antes de registrar una solicitud de contacto. */
data class SolicitudCatalogoRequest(
    @field:NotNull val vehiculoId: Long?,
    @field:NotBlank @field:Size(max = 120) val nombreContacto: String,
    @field:NotBlank @field:Email @field:Size(max = 150) val correoContacto: String,
    @field:NotBlank @field:Size(max = 150) val entidad: String,
    @field:Size(max = 30) val telefono: String? = null,
    @field:NotNull @field:FutureOrPresent val fechaInicio: LocalDate?,
    @field:NotNull val fechaFin: LocalDate?,
    @field:Size(max = 600) val mensaje: String? = null,
    @field:AssertTrue(message = "Debes autorizar el contacto para enviar la solicitud.")
    val aceptaContacto: Boolean = false,
)

/** Respuesta pública mínima; no devuelve datos privados del contacto. */
data class SolicitudCreadaResponse(
    val id: Long,
    val estado: EstadoSolicitudCatalogo,
    val mensaje: String = "Solicitud recibida. La disponibilidad debe confirmarla un asesor.",
)

/** Vista administrativa autenticada, protegida del endpoint público. */
data class SolicitudCatalogoAdminResponse(
    val id: Long,
    val vehiculoId: Long,
    val vehiculo: String,
    val nombreContacto: String,
    val correoContacto: String,
    val entidad: String,
    val telefono: String?,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate,
    val mensaje: String?,
    val estado: EstadoSolicitudCatalogo,
    val creadoEn: OffsetDateTime,
)

data class CambiarEstadoSolicitudRequest(@field:NotNull val estado: EstadoSolicitudCatalogo?)
