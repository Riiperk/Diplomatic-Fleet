package com.diplomaticfleet.backend.clientes.domain

/**
 * Clasificaciones admitidas para los clientes institucionales.
 *
 * Los valores coinciden exactamente con la restricción
 * `chk_clientes_tipo_entidad` definida en la migración V1.
 */
enum class TipoEntidad {
    EMBAJADA,
    CONSULADO,
    ORGANISMO_INTERNACIONAL,
    OTRO,
}