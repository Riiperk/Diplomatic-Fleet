package com.diplomaticfleet.backend.clientes.dto

import com.diplomaticfleet.backend.clientes.domain.TipoEntidad
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * Datos recibidos para registrar o actualizar un cliente institucional.
 *
 * El identificador y las fechas de auditoría no se reciben desde el
 * frontend porque son administrados por PostgreSQL y el backend.
 *
 * @property nombreEntidad Nombre oficial de la institución.
 * @property tipoEntidad Clasificación institucional.
 * @property numeroIdentificacionFiscal Identificación fiscal o institucional.
 * @property nombreContacto Persona responsable de la comunicación.
 * @property correoContacto Correo del contacto principal.
 * @property telefonoContacto Teléfono de contacto.
 * @property direccion Dirección física.
 * @property ciudad Ciudad donde se encuentra la institución.
 * @property activo Indica si puede solicitar nuevos servicios.
 */
data class ClienteInstitucionalRequest(

    @field:NotBlank(
        message = "El nombre de la entidad es obligatorio.",
    )
    @field:Size(
        max = 150,
        message = "El nombre de la entidad no puede superar los 150 caracteres.",
    )
    val nombreEntidad: String,

    @field:NotNull(
        message = "El tipo de entidad es obligatorio.",
    )
    val tipoEntidad: TipoEntidad?,

    @field:Size(
        max = 40,
        message = "La identificación fiscal no puede superar los 40 caracteres.",
    )
    val numeroIdentificacionFiscal: String? = null,

    @field:NotBlank(
        message = "El nombre del contacto es obligatorio.",
    )
    @field:Size(
        max = 140,
        message = "El nombre del contacto no puede superar los 140 caracteres.",
    )
    val nombreContacto: String,

    @field:NotBlank(
        message = "El correo del contacto es obligatorio.",
    )
    @field:Email(
        message = "El correo del contacto no tiene un formato válido.",
    )
    @field:Size(
        max = 150,
        message = "El correo del contacto no puede superar los 150 caracteres.",
    )
    val correoContacto: String,

    @field:Size(
        max = 30,
        message = "El teléfono no puede superar los 30 caracteres.",
    )
    val telefonoContacto: String? = null,

    @field:Size(
        max = 255,
        message = "La dirección no puede superar los 255 caracteres.",
    )
    val direccion: String? = null,

    @field:Size(
        max = 100,
        message = "La ciudad no puede superar los 100 caracteres.",
    )
    val ciudad: String? = null,

    val activo: Boolean = true,
)