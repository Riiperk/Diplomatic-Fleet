package com.diplomaticfleet.backend.reservas

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.math.BigDecimal
import java.time.OffsetDateTime

/** Casos unitarios deterministas: no necesitan cuentas, API ni PostgreSQL. */
class ReglasAlquilerTest {
    private val ahora = OffsetDateTime.parse("2026-11-25T09:00:00-05:00")
    private val inicio = ahora.plusDays(1)
    private val tarifa = BigDecimal("290000.00")

    /** Un periodo exacto y una fracción se facturan por días iniciados. */
    @Test fun importePorDias() {
        assertEquals(BigDecimal("290000.00"), ReglasAlquiler.valorPeriodo(tarifa,inicio,inicio.plusHours(1)))
        assertEquals(BigDecimal("580000.00"), ReglasAlquiler.valorPeriodo(tarifa,inicio,inicio.plusDays(2)))
        assertEquals(BigDecimal("870000.00"), ReglasAlquiler.valorPeriodo(tarifa,inicio,inicio.plusDays(2).plusSeconds(1)))
    }

    /** El máximo de 90 días admite el límite exacto y no una fracción adicional. */
    @Test fun limitePeriodo() {
        ReglasAlquiler.validarPeriodo(inicio,inicio.plusDays(90),ahora)
        assertFailsWith<IllegalArgumentException> { ReglasAlquiler.validarPeriodo(inicio,inicio.plusDays(90).plusNanos(1),ahora) }
    }

    /** Rechaza periodos pasados, vacíos y tarifas negativas. */
    @Test fun entradasInvalidas() {
        assertFailsWith<IllegalArgumentException> { ReglasAlquiler.validarPeriodo(ahora,ahora.plusDays(1),ahora) }
        assertFailsWith<IllegalArgumentException> { ReglasAlquiler.validarPeriodo(inicio,inicio,ahora) }
        assertFailsWith<IllegalArgumentException> { ReglasAlquiler.valorPeriodo(BigDecimal("-1"),inicio,inicio.plusDays(1)) }
    }
}
