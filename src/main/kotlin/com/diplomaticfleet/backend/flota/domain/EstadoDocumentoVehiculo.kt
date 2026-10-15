package com.diplomaticfleet.backend.flota.domain

/**
 * Estados de vigencia admitidos para los documentos vehiculares.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_documentos_vehiculo_estado` definida en la migración V2.
 */
enum class EstadoDocumentoVehiculo {
    VIGENTE,
    PROXIMO_A_VENCER,
    VENCIDO,
}