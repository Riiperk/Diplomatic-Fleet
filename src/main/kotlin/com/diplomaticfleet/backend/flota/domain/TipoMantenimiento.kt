package com.diplomaticfleet.backend.flota.domain

/**
 * Tipos de mantenimiento admitidos para los vehículos.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_mantenimientos_tipo` definida en la migración V2.
 */
enum class TipoMantenimiento {
    PREVENTIVO,
    CORRECTIVO,
}