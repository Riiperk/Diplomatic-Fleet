package com.diplomaticfleet.backend.flota.domain

/**
 * Tipos de documentos admitidos para los vehículos.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_documentos_vehiculo_tipo` definida en la migración V2.
 */
enum class TipoDocumentoVehiculo {
    SOAT,
    REVISION_TECNOMECANICA,
    TARJETA_PROPIEDAD,
    POLIZA,
    OTRO,
}