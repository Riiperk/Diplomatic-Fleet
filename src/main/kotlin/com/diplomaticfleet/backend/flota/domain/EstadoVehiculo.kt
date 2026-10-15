package com.diplomaticfleet.backend.flota.domain

/**
 * Estados operativos permitidos para los vehículos.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_vehiculos_estado` definida en la migración V2.
 */
enum class EstadoVehiculo {
    DISPONIBLE,
    RESERVADO,
    EN_SERVICIO,
    EN_MANTENIMIENTO,
    INACTIVO,
}
