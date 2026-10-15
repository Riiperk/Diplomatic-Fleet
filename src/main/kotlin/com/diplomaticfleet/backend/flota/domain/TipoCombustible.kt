package com.diplomaticfleet.backend.flota.domain

/**
 * Tipos de combustible admitidos para los vehículos.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_vehiculos_combustible` definida en la migración V2.
 */
enum class TipoCombustible {
    GASOLINA,
    DIESEL,
    HIBRIDO,
    ELECTRICO,
}