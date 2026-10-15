package com.diplomaticfleet.backend.clientes.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

/**
 * Entidad institucional que solicita servicios de transporte.
 *
 * Corresponde a la tabla `clientes_institucionales`, creada mediante
 * la migración V1. Puede representar embajadas, consulados,
 * organismos internacionales u otras instituciones.
 *
 * @property nombreEntidad Nombre oficial de la institución.
 * @property tipoEntidad Clasificación institucional.
 * @property numeroIdentificacionFiscal Identificación fiscal o institucional.
 * @property nombreContacto Persona responsable de la comunicación.
 * @property correoContacto Correo electrónico del contacto principal.
 * @property telefonoContacto Teléfono del contacto, cuando esté disponible.
 * @property direccion Dirección física de la institución.
 * @property ciudad Ciudad donde está ubicada la institución.
 * @property activo Indica si puede solicitar nuevos servicios.
 * @property creadoEn Fecha y hora de creación.
 * @property actualizadoEn Fecha y hora de la última actualización.
 * @property id Identificador generado por PostgreSQL.
 */
@Entity
@Table(name = "clientes_institucionales")
class ClienteInstitucional(

    @field:Column(
        name = "nombre_entidad",
        nullable = false,
        length = 150,
    )
    var nombreEntidad: String,

    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "tipo_entidad",
        nullable = false,
        length = 40,
    )
    var tipoEntidad: TipoEntidad,

    @field:Column(
        name = "numero_identificacion_fiscal",
        length = 40,
    )
    var numeroIdentificacionFiscal: String? = null,

    @field:Column(
        name = "nombre_contacto",
        nullable = false,
        length = 140,
    )
    var nombreContacto: String,

    @field:Column(
        name = "correo_contacto",
        nullable = false,
        length = 150,
    )
    var correoContacto: String,

    @field:Column(
        name = "telefono_contacto",
        length = 30,
    )
    var telefonoContacto: String? = null,

    @field:Column(
        name = "direccion",
        length = 255,
    )
    var direccion: String? = null,

    @field:Column(
        name = "ciudad",
        length = 100,
    )
    var ciudad: String? = null,

    @field:Column(
        name = "activo",
        nullable = false,
    )
    var activo: Boolean = true,

    @field:Column(
        name = "creado_en",
        nullable = false,
        updatable = false,
    )
    var creadoEn: OffsetDateTime = OffsetDateTime.now(),

    @field:Column(
        name = "actualizado_en",
        nullable = false,
    )
    var actualizadoEn: OffsetDateTime = OffsetDateTime.now(),

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null,
)