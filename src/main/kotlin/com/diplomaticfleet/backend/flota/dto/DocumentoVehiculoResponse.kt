package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.EstadoDocumentoVehiculo
import com.diplomaticfleet.backend.flota.domain.TipoDocumentoVehiculo
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Datos expuestos por la API para representar un documento vehicular.
 *
 * Este DTO evita devolver directamente la entidad JPA e incluye
 * información básica del vehículo para facilitar la identificación
 * del documento desde el frontend.
 *
 * @property id Identificador del documento.
 * @property vehiculoId Identificador del vehículo asociado.
 * @property placaVehiculo Placa utilizada para identificar el vehículo.
 * @property tipoDocumento Tipo de documento registrado.
 * @property numeroDocumento Número o referencia del documento.
 * @property fechaEmision Fecha de expedición, cuando esté disponible.
 * @property fechaVencimiento Fecha hasta la que permanece vigente.
 * @property estado Estado de vigencia del documento.
 * @property observaciones Información adicional sobre el documento.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property actualizadoEn Fecha y hora de la última actualización.
 */
data class DocumentoVehiculoResponse(
    val id: Long,
    val vehiculoId: Long,
    val placaVehiculo: String,
    val tipoDocumento: TipoDocumentoVehiculo,
    val numeroDocumento: String?,
    val fechaEmision: LocalDate?,
    val fechaVencimiento: LocalDate,
    val estado: EstadoDocumentoVehiculo,
    val observaciones: String?,
    val creadoEn: OffsetDateTime,
    val actualizadoEn: OffsetDateTime,
)