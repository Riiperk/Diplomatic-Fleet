package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.CodigoCategoriaVehiculo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Datos recibidos para registrar o actualizar una categoría de vehículo.
 *
 * El identificador y la fecha de creación no se reciben desde el cliente,
 * porque son generados y administrados por el sistema.
 *
 * @property codigo Código estable de la categoría.
 * @property nombre Nombre visible de la categoría.
 * @property descripcion Información adicional sobre la categoría.
 * @property activo Indica si la categoría puede asignarse a nuevos vehículos.
 */
data class CategoriaVehiculoRequest(

    @field:NotNull(
        message = "El código de la categoría es obligatorio.",
    )
    val codigo: CodigoCategoriaVehiculo?,

    @field:NotBlank(
        message = "El nombre de la categoría es obligatorio.",
    )
    @field:Size(
        max = 70,
        message = "El nombre no puede superar los 70 caracteres.",
    )
    val nombre: String,

    @field:Size(
        max = 255,
        message = "La descripción no puede superar los 255 caracteres.",
    )
    val descripcion: String? = null,

    val activo: Boolean = true,
)