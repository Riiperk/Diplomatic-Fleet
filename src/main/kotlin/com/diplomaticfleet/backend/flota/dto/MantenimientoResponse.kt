package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.EstadoMantenimiento
import com.diplomaticfleet.backend.flota.domain.TipoMantenimiento
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Datos expuestos por la API para representar un mantenimiento vehicular.
 *
 * Este DTO evita devolver directamente la entidad JPA y proporciona
 * únicamente la información necesaria para consultar el mantenimiento
 * y el vehículo asociado.
 *
 * @property id Identificador del mantenimiento.
 * @property vehiculoId Identificador del vehículo asociado.
 * @property placaVehiculo Placa que permite identificar el vehículo.
 * @property tipo Tipo de mantenimiento preventivo o correctivo.
 * @property estado Estado actual del mantenimiento.
 * @property descripcion Descripción del trabajo programado o realizado.
 * @property fechaProgramada Fecha prevista para realizar el mantenimiento.
 * @property fechaInicio Momento en que comenzó efectivamente el trabajo.
 * @property fechaFin Momento en que finalizó el mantenimiento.
 * @property costo Coste registrado, cuando esté disponible.
 * @property proveedor Persona o empresa responsable del trabajo.
 * @property observaciones Información adicional del proceso.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property actualizadoEn Fecha y hora de la última actualización.
 */
data class MantenimientoResponse(
    val id: Long,
    val vehiculoId: Long,
    val placaVehiculo: String,
    val tipo: TipoMantenimiento,
    val estado: EstadoMantenimiento,
    val descripcion: String,
    val fechaProgramada: LocalDate,
    val fechaInicio: OffsetDateTime?,
    val fechaFin: OffsetDateTime?,
    val costo: BigDecimal?,
    val proveedor: String?,
    val observaciones: String?,
    val creadoEn: OffsetDateTime,
    val actualizadoEn: OffsetDateTime,
)