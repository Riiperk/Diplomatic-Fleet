package com.diplomaticfleet.backend.flota.dto

import com.diplomaticfleet.backend.flota.domain.CodigoCategoriaVehiculo
import com.diplomaticfleet.backend.flota.domain.EstadoVehiculo
import com.diplomaticfleet.backend.flota.domain.TipoCombustible
import java.time.OffsetDateTime

/**
 * Datos expuestos por la API para representar un vehículo de la flota.
 *
 * Este DTO evita devolver directamente la entidad JPA e incluye
 * información básica de la categoría para facilitar su visualización
 * en el frontend.
 *
 * @property id Identificador del vehículo.
 * @property categoriaId Identificador de la categoría asignada.
 * @property codigoCategoria Código estable de la categoría.
 * @property nombreCategoria Nombre visible de la categoría.
 * @property placa Placa normalizada del vehículo.
 * @property marca Fabricante del vehículo.
 * @property modelo Referencia o modelo comercial.
 * @property anio Año de fabricación.
 * @property color Color registrado.
 * @property combustible Tipo de combustible utilizado.
 * @property capacidadPasajeros Cantidad máxima de pasajeros.
 * @property capacidadEquipaje Cantidad estimada de piezas de equipaje.
 * @property estado Estado operativo actual.
 * @property activo Indica si el vehículo permanece habilitado.
 * @property creadoEn Fecha y hora de creación del registro.
 * @property actualizadoEn Fecha y hora de la última actualización.
 */
data class VehiculoResponse(
    val id: Long,
    val categoriaId: Long,
    val codigoCategoria: CodigoCategoriaVehiculo,
    val nombreCategoria: String,
    val placa: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val color: String,
    val combustible: TipoCombustible,
    val capacidadPasajeros: Int,
    val capacidadEquipaje: Int,
    val estado: EstadoVehiculo,
    val activo: Boolean,
    val creadoEn: OffsetDateTime,
    val actualizadoEn: OffsetDateTime,
)