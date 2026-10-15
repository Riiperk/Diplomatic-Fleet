package com.diplomaticfleet.backend.clientes.dto

import com.diplomaticfleet.backend.clientes.domain.TipoEntidad
import java.time.OffsetDateTime

/**
 * Datos expuestos por la API para representar un cliente institucional.
 *
 * Este DTO evita devolver directamente la entidad JPA y contiene
 * la información necesaria para la gestión administrativa.
 *
 * @property id Identificador del cliente.
 * @property nombreEntidad Nombre oficial de la institución.
 * @property tipoEntidad Clasificación institucional.
 * @property numeroIdentificacionFiscal Identificación fiscal o institucional.
 * @property nombreContacto Persona responsable de la comunicación.
 * @property correoContacto Correo del contacto principal.
 * @property telefonoContacto Teléfono de contacto.
 * @property direccion Dirección física.
 * @property ciudad Ciudad donde se encuentra la institución.
 * @property activo Indica si puede solicitar nuevos servicios.
 * @property creadoEn Fecha y hora de creación.
 * @property actualizadoEn Fecha y hora de la última actualización.
 */
data class ClienteInstitucionalResponse(
    val id: Long,
    val nombreEntidad: String,
    val tipoEntidad: TipoEntidad,
    val numeroIdentificacionFiscal: String?,
    val nombreContacto: String,
    val correoContacto: String,
    val telefonoContacto: String?,
    val direccion: String?,
    val ciudad: String?,
    val activo: Boolean,
    val creadoEn: OffsetDateTime,
    val actualizadoEn: OffsetDateTime,
)