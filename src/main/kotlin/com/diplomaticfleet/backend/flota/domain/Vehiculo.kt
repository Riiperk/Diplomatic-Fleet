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
import java.time.OffsetDateTime

/**
 * Vehículo perteneciente a la flota de Diplomatic Fleet.
 *
 * La entidad corresponde a la tabla `vehiculos`, creada mediante
 * la migración V2.
 *
 * Cada vehículo pertenece obligatoriamente a una categoría.
 * Su disponibilidad se determina mediante [estado], no mediante
 * un indicador booleano independiente.
 *
 * @property categoria Categoría de servicio asignada al vehículo.
 * @property placa Identificador único registrado para el vehículo.
 * @property marca Fabricante del vehículo.
 * @property modelo Modelo comercial del vehículo.
 * @property anio Año asociado al modelo del vehículo.
 * @property color Color registrado.
 * @property combustible Tipo de energía utilizada.
 * @property capacidadPasajeros Cantidad máxima de pasajeros.
 * @property capacidadEquipaje Cantidad estimada de piezas de equipaje.
 * @property estado Estado operativo actual.
 * @property activo Indica si el vehículo continúa registrado para operación.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property actualizadoEn Fecha y hora de la última actualización.
 * @property id Identificador generado por PostgreSQL.
 */
@Entity
@Table(name = "vehiculos")
class Vehiculo(

    @field:ManyToOne(
        fetch = FetchType.LAZY,
        optional = false,
    )
    @field:JoinColumn(
        name = "categoria_id",
        nullable = false,
    )
    var categoria: CategoriaVehiculo,

    @field:Column(
        name = "placa",
        nullable = false,
        length = 12,
    )
    var placa: String,

    @field:Column(
        name = "marca",
        nullable = false,
        length = 60,
    )
    var marca: String,

    @field:Column(
        name = "modelo",
        nullable = false,
        length = 60,
    )
    var modelo: String,

    @field:Column(
        name = "anio",
        nullable = false,
    )
    var anio: Int,

    @field:Column(
        name = "color",
        nullable = false,
        length = 40,
    )
    var color: String,

    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "combustible",
        nullable = false,
        length = 30,
    )
    var combustible: TipoCombustible,

    @field:Column(
        name = "capacidad_pasajeros",
        nullable = false,
    )
    var capacidadPasajeros: Int,

    @field:Column(
        name = "capacidad_equipaje",
        nullable = false,
    )
    var capacidadEquipaje: Int = 0,

    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "estado",
        nullable = false,
        length = 30,
    )
    var estado: EstadoVehiculo = EstadoVehiculo.DISPONIBLE,

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