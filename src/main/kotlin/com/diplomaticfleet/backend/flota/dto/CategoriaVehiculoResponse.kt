package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.CodigoCategoriaVehiculo
import java.time.OffsetDateTime

/**
 * Datos expuestos por la API para representar una categoría de vehículo.
 *
 * Este DTO permite consultar la clasificación de servicio utilizada
 * por los vehículos sin exponer directamente la entidad JPA.
 *
 * @property id Identificador de la categoría.
 * @property codigo Código estable utilizado por el dominio.
 * @property nombre Nombre visible de la categoría.
 * @property descripcion Información adicional sobre la categoría.
 * @property activo Indica si puede asignarse a nuevos vehículos.
 * @property creadoEn Fecha y hora de creación del registro.
 */
data class CategoriaVehiculoResponse(
    val id: Long,
    val codigo: CodigoCategoriaVehiculo,
    val nombre: String,
    val descripcion: String?,
    val activo: Boolean,
    val creadoEn: OffsetDateTime,
)