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
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Mantenimiento preventivo o correctivo asociado a un vehículo.
 *
 * La entidad corresponde a la tabla `mantenimientos`, creada
 * mediante la migración V2.
 *
 * Cada mantenimiento pertenece obligatoriamente a un vehículo.
 * Las fechas de inicio y finalización son opcionales mientras
 * el mantenimiento permanezca programado.
 *
 * @property vehiculo Vehículo al que pertenece el mantenimiento.
 * @property tipo Tipo de mantenimiento realizado.
 * @property estado Estado actual del proceso.
 * @property descripcion Trabajo que debe realizarse o que fue realizado.
 * @property fechaProgramada Fecha prevista para realizar el mantenimiento.
 * @property fechaInicio Momento en que comenzó efectivamente el trabajo.
 * @property fechaFin Momento en que finalizó el mantenimiento.
 * @property costo Coste registrado, cuando esté disponible.
 * @property proveedor Persona o empresa responsable del mantenimiento.
 * @property observaciones Información adicional del proceso.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property actualizadoEn Fecha y hora de la última actualización.
 * @property id Identificador generado por PostgreSQL.
 */
@Entity
@Table(name = "mantenimientos")
class Mantenimiento(

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
        name = "tipo",
        nullable = false,
        length = 20,
    )
    var tipo: TipoMantenimiento,

    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "estado",
        nullable = false,
        length = 20,
    )
    var estado: EstadoMantenimiento = EstadoMantenimiento.PROGRAMADO,

    @field:Column(
        name = "descripcion",
        nullable = false,
        length = 500,
    )
    var descripcion: String,

    @field:Column(
        name = "fecha_programada",
        nullable = false,
    )
    var fechaProgramada: LocalDate,

    @field:Column(name = "fecha_inicio")
    var fechaInicio: OffsetDateTime? = null,

    @field:Column(name = "fecha_fin")
    var fechaFin: OffsetDateTime? = null,

    @field:Column(
        name = "costo",
        precision = 12,
        scale = 2,
    )
    var costo: BigDecimal? = null,

    @field:Column(
        name = "proveedor",
        length = 150,
    )
    var proveedor: String? = null,

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