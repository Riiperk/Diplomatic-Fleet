package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.TipoDocumentoVehiculo
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

/**
 * Datos recibidos para registrar o actualizar un documento vehicular.
 *
 * El estado de vigencia no se recibe desde el cliente, porque debe
 * calcularse según la fecha de vencimiento y las reglas del sistema.
 *
 * @property vehiculoId Identificador del vehículo asociado.
 * @property tipoDocumento Tipo de documento registrado.
 * @property numeroDocumento Número o referencia del documento.
 * @property fechaEmision Fecha de expedición, cuando esté disponible.
 * @property fechaVencimiento Fecha hasta la que permanece vigente.
 * @property observaciones Información adicional sobre el documento.
 */
data class DocumentoVehiculoRequest(

    @field:NotNull(
        message = "El identificador del vehículo es obligatorio.",
    )
    val vehiculoId: Long?,

    @field:NotNull(
        message = "El tipo de documento es obligatorio.",
    )
    val tipoDocumento: TipoDocumentoVehiculo?,

    @field:Size(
        max = 80,
        message = "El número del documento no puede superar los 80 caracteres.",
    )
    val numeroDocumento: String? = null,

    val fechaEmision: LocalDate? = null,

    @field:NotNull(
        message = "La fecha de vencimiento es obligatoria.",
    )
    val fechaVencimiento: LocalDate?,

    @field:Size(
        max = 500,
        message = "Las observaciones no pueden superar los 500 caracteres.",
    )
    val observaciones: String? = null,
)