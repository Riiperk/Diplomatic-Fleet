package com.diplomaticfleet.backend.flota.domain

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
 * Categoría utilizada para clasificar los vehículos según
 * el nivel de servicio ofrecido.
 *
 * La entidad corresponde a la tabla `categorias_vehiculo`,
 * creada mediante la migración V2.
 *
 * @property codigo Código estable utilizado por el backend.
 * @property nombre Nombre visible para las personas usuarias.
 * @property descripcion Explicación opcional de la categoría.
 * @property activo Indica si puede asignarse a nuevos vehículos.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property id Identificador generado por PostgreSQL.
 */
@Entity
@Table(name = "categorias_vehiculo")
class CategoriaVehiculo(
    @field:Enumerated(EnumType.STRING)
    @field:Column(
        name = "codigo",
        nullable = false,
        unique = true,
        length = 30,
    )
    var codigo: CodigoCategoriaVehiculo,

    @field:Column(
        name = "nombre",
        nullable = false,
        length = 70,
    )
    var nombre: String,

    @field:Column(
        name = "descripcion",
        length = 255,
    )
    var descripcion: String? = null,

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

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    @field:Column(name = "id")
    var id: Long? = null,
)