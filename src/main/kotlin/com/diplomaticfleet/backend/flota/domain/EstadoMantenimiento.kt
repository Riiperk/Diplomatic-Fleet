package com.diplomaticfleet.backend.flota.domain

/**
 * Estados admitidos durante el ciclo de vida de un mantenimiento.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_mantenimientos_estado` definida en la migración V2.
 */
enum class EstadoMantenimiento {
    PROGRAMADO,
    EN_PROCESO,
    FINALIZADO,
    CANCELADO,
}