package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.TipoCombustible
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Datos recibidos para registrar o actualizar un vehículo de la flota.
 *
 * El estado operativo no se recibe mediante este DTO, porque sus cambios
 * dependen de las reservas, los servicios y los mantenimientos asociados.
 *
 * @property categoriaId Identificador de la categoría asignada.
 * @property placa Placa única utilizada para identificar el vehículo.
 * @property marca Fabricante del vehículo.
 * @property modelo Referencia o modelo comercial.
 * @property anio Año de fabricación.
 * @property color Color registrado.
 * @property combustible Tipo de combustible utilizado.
 * @property capacidadPasajeros Cantidad máxima de pasajeros.
 * @property capacidadEquipaje Cantidad estimada de piezas de equipaje.
 * @property activo Indica si el vehículo permanece habilitado en la flota.
 */
data class VehiculoRequest(

    @field:NotNull(
        message = "La categoría del vehículo es obligatoria.",
    )
    val categoriaId: Long?,

    @field:NotBlank(
        message = "La placa es obligatoria.",
    )
    @field:Size(
        max = 12,
        message = "La placa no puede superar los 12 caracteres.",
    )
    val placa: String,

    @field:NotBlank(
        message = "La marca es obligatoria.",
    )
    @field:Size(
        max = 60,
        message = "La marca no puede superar los 60 caracteres.",
    )
    val marca: String,

    @field:NotBlank(
        message = "El modelo es obligatorio.",
    )
    @field:Size(
        max = 60,
        message = "El modelo no puede superar los 60 caracteres.",
    )
    val modelo: String,

    @field:NotNull(
        message = "El año de fabricación es obligatorio.",
    )
    @field:Min(
        value = 1950,
        message = "El año de fabricación no puede ser inferior a 1950.",
    )
    @field:Max(
        value = 2100,
        message = "El año de fabricación no puede ser superior a 2100.",
    )
    val anio: Int?,

    @field:NotBlank(
        message = "El color es obligatorio.",
    )
    @field:Size(
        max = 40,
        message = "El color no puede superar los 40 caracteres.",
    )
    val color: String,

    @field:NotNull(
        message = "El tipo de combustible es obligatorio.",
    )
    val combustible: TipoCombustible?,

    @field:NotNull(
        message = "La capacidad de pasajeros es obligatoria.",
    )
    @field:Min(
        value = 1,
        message = "La capacidad de pasajeros debe ser como mínimo 1.",
    )
    @field:Max(
        value = 100,
        message = "La capacidad de pasajeros no puede superar 100.",
    )
    val capacidadPasajeros: Int?,

    @field:Min(
        value = 0,
        message = "La capacidad de equipaje no puede ser negativa.",
    )
    val capacidadEquipaje: Int = 0,

    val activo: Boolean = true,
)