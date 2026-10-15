package com.diplomaticfleet.backend.flota.domain

/**
 * Códigos permitidos para clasificar la flota.
 *
 * Los valores coinciden con la restricción definida en PostgreSQL
 * y con los catálogos insertados mediante la migración V4.
 */
enum class CodigoCategoriaVehiculo {
    ECONOMICO,
    EJECUTIVO,
    PREMIUM,
}