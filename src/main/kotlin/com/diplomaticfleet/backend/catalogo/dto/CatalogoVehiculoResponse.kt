package com.diplomaticfleet.backend.catalogo.dto

import com.diplomaticfleet.backend.flota.domain.CodigoCategoriaVehiculo
import com.diplomaticfleet.backend.flota.domain.EstadoVehiculo
import com.diplomaticfleet.backend.flota.domain.TipoCombustible
import java.math.BigDecimal

/**
 * Información comercial pública: por seguridad, excluye placas, auditoría y datos internos.
 * Una tarifa nula significa que el servicio necesita cotización individual.
 */
data class CatalogoVehiculoResponse(
    val id: Long,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val categoria: CodigoCategoriaVehiculo,
    val nombreCategoria: String,
    val color: String,
    val combustible: TipoCombustible,
    val pasajeros: Int,
    val equipaje: Int,
    val estado: EstadoVehiculo,
    val tarifaDiaria: BigDecimal?,
    val imagenUrl: String?,
    val descripcion: String?,
    val blindado: Boolean,
)
