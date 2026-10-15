package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.TipoMantenimiento
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Datos recibidos para programar o actualizar un mantenimiento vehicular.
 *
 * El estado y las fechas reales de inicio y finalización no se reciben
 * mediante este DTO, porque dependen de las transiciones controladas
 * por la lógica de negocio.
 *
 * @property vehiculoId Identificador del vehículo asociado.
 * @property tipo Tipo de mantenimiento preventivo o correctivo.
 * @property descripcion Descripción del trabajo previsto.
 * @property fechaProgramada Fecha establecida para realizar el mantenimiento.
 * @property costo Coste estimado o definitivo, cuando esté disponible.
 * @property proveedor Persona o empresa responsable del trabajo.
 * @property observaciones Información adicional del mantenimiento.
 */
data class MantenimientoRequest(

    @field:NotNull(
        message = "El identificador del vehículo es obligatorio.",
    )
    val vehiculoId: Long?,

    @field:NotNull(
        message = "El tipo de mantenimiento es obligatorio.",
    )
    val tipo: TipoMantenimiento?,

    @field:NotBlank(
        message = "La descripción es obligatoria.",
    )
    @field:Size(
        max = 500,
        message = "La descripción no puede superar los 500 caracteres.",
    )
    val descripcion: String,

    @field:NotNull(
        message = "La fecha programada es obligatoria.",
    )
    val fechaProgramada: LocalDate?,

    @field:PositiveOrZero(
        message = "El costo debe ser mayor o igual que cero.",
    )
    val costo: BigDecimal? = null,

    @field:Size(
        max = 150,
        message = "El proveedor no puede superar los 150 caracteres.",
    )
    val proveedor: String? = null,

    @field:Size(
        max = 500,
        message = "Las observaciones no pueden superar los 500 caracteres.",
    )
    val observaciones: String? = null,
)