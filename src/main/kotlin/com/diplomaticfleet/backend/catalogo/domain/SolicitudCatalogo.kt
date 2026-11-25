package com.diplomaticfleet.backend.catalogo.domain

import com.diplomaticfleet.backend.flota.domain.Vehiculo
import jakarta.persistence.*
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Contacto comercial preliminar. NO constituye una reserva ni un cobro.
 * El personal debe validar identidad institucional, cupo y documentación.
 */
@Entity
@Table(name = "solicitudes_catalogo")
class SolicitudCatalogo(
    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(name = "vehiculo_id", nullable = false)
    val vehiculo: Vehiculo,

    @field:Column(name = "nombre_contacto", nullable = false, length = 120)
    val nombreContacto: String,

    @field:Column(name = "correo_contacto", nullable = false, length = 150)
    val correoContacto: String,

    @field:Column(name = "entidad", nullable = false, length = 150)
    val entidad: String,

    @field:Column(name = "telefono", length = 30)
    val telefono: String? = null,

    @field:Column(name = "fecha_inicio", nullable = false)
    val fechaInicio: LocalDate,

    @field:Column(name = "fecha_fin", nullable = false)
    val fechaFin: LocalDate,

    @field:Column(name = "mensaje", length = 600)
    val mensaje: String? = null,

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "estado", nullable = false, length = 20)
    var estado: EstadoSolicitudCatalogo = EstadoSolicitudCatalogo.PENDIENTE,

    @field:Column(name = "creado_en", nullable = false, updatable = false)
    val creadoEn: OffsetDateTime = OffsetDateTime.now(),

    @field:Column(name = "actualizado_en", nullable = false)
    var actualizadoEn: OffsetDateTime = OffsetDateTime.now(),

    @field:Id @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)

/** Estados del seguimiento comercial, separados de las solicitudes formales de V3. */
enum class EstadoSolicitudCatalogo {
    PENDIENTE, CONTACTADA, CERRADA, CANCELADA
}
