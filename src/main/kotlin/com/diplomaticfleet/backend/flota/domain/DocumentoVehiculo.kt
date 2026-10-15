package com.diplomaticfleet.backend.flota.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Documento legal u operativo asociado a un vehículo.
 *
 * La entidad corresponde a la tabla `documentos_vehiculo`,
 * creada mediante la migración V2.
 *
 * Cada documento pertenece obligatoriamente a un vehículo.
 * La relación utiliza carga diferida para evitar recuperar el vehículo
 * cuando solamente se necesita consultar la información documental.
 *
 * @property vehiculo Vehículo al que pertenece el documento.
 * @property tipoDocumento Tipo de documento registrado.
 * @property numeroDocumento Número o referencia opcional del documento.
 * @property fechaEmision Fecha en la que se emitió el documento.
 * @property fechaVencimiento Fecha utilizada para controlar su vigencia.
 * @property estado Estado actual de vigencia.
 * @property observaciones Información adicional del documento.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property actualizadoEn Fecha y hora de la última actualización.
 * @property id Identificador generado por PostgreSQL.
 */
@Entity
@Table(name = "documentos_vehiculo")
class DocumentoVehiculo(

    @field:ManyToOne(
        fetch = FetchType.LAZY,
        optional = false,
    )
    @field:JoinColumn(
        name = "vehiculo_id",
        nullable = false,
    )
    var vehiculo: Vehiculo,

    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "tipo_documento",
        nullable = false,
        length = 40,
    )
    var tipoDocumento: TipoDocumentoVehiculo,

    @field:Column(
        name = "numero_documento",
        length = 80,
    )
    var numeroDocumento: String? = null,

    @field:Column(name = "fecha_emision")
    var fechaEmision: LocalDate? = null,

    @field:Column(
        name = "fecha_vencimiento",
        nullable = false,
    )
    var fechaVencimiento: LocalDate,

    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "estado",
        nullable = false,
        length = 20,
    )
    var estado: EstadoDocumentoVehiculo = EstadoDocumentoVehiculo.VIGENTE,

    @field:Column(
        name = "observaciones",
        length = 500,
    )
    var observaciones: String? = null,

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