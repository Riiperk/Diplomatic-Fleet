package com.diplomaticfleet.backend.reservas

import com.diplomaticfleet.backend.exception.ConflictoException
import java.math.BigDecimal

/** Valida montos COP declarados para operaciones externas; no contacta bancos. */
object ReglasPago {
    private val maximo = BigDecimal("999999999999.99")
    fun montoValido(monto: BigDecimal?): BigDecimal {
        require(monto != null && monto.signum() > 0 && monto <= maximo && monto.stripTrailingZeros().scale() <= 2) {
            "Indique un importe positivo en COP, con máximo dos decimales"
        }
        return monto
    }
    fun montoCompleto(monto: BigDecimal?, esperado: BigDecimal): BigDecimal {
        val valor = montoValido(monto)
        if (valor.compareTo(esperado) != 0) throw ConflictoException("El importe debe coincidir con el total de la reserva; no se admiten pagos parciales")
        return valor
    }
    fun referencia(valor: String?): String {
        val texto = valor?.trim().orEmpty()
        require(texto.length in 4..120) { "Indique una referencia externa de 4 a 120 caracteres" }
        return texto
    }
}
