package com.diplomaticfleet.backend.reservas

import com.diplomaticfleet.backend.exception.ConflictoException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ReglasPagoTest {
    @Test fun aceptaMontoExacto() {
        assertEquals(0,ReglasPago.montoCompleto(BigDecimal("520000.00"),BigDecimal("520000")).compareTo(BigDecimal("520000")))
    }
    @Test fun rechazaPagoParcial() {
        assertThrows(ConflictoException::class.java) { ReglasPago.montoCompleto(BigDecimal("100"),BigDecimal("520000")) }
    }
    @Test fun rechazaMontoAusenteNegativoYPrecisionIncorrecta() {
        listOf(null,BigDecimal("0"),BigDecimal("-1"),BigDecimal("1.001")).forEach { monto ->
            assertThrows(IllegalArgumentException::class.java) { ReglasPago.montoValido(monto) }
        }
    }
    @Test fun exigeReferencia() {
        assertEquals("REC-123",ReglasPago.referencia(" REC-123 "))
        assertThrows(IllegalArgumentException::class.java) { ReglasPago.referencia(" ") }
    }
}
