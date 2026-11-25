package com.diplomaticfleet.backend.reservas

import java.math.BigDecimal
import java.time.Duration
import java.time.OffsetDateTime

/** Reglas puras del periodo contractual. No consulta ni modifica la base de datos. */
object ReglasAlquiler {
    /**
     * Valida un intervalo futuro de hasta 90 días exactos.
     * @param inicio instante previsto de recogida.
     * @param fin instante previsto de devolución.
     * @param ahora instante de referencia, inyectable en pruebas deterministas.
     * @throws IllegalArgumentException si el periodo no es admisible.
     */
    fun validarPeriodo(inicio: OffsetDateTime, fin: OffsetDateTime, ahora: OffsetDateTime = OffsetDateTime.now()) {
        require(inicio.isAfter(ahora)) { "La recogida debe ser futura" }
        require(fin.isAfter(inicio)) { "La devolucion debe ser posterior" }
        require(Duration.between(inicio, fin) <= Duration.ofDays(90)) { "Maximo 90 dias por solicitud" }
    }

    /**
     * Calcula el importe por bloques de 24 horas iniciados, sin truncar segundos.
     * @param tarifa valor diario no negativo, recuperado de PostgreSQL.
     * @param inicio instante de inicio.
     * @param fin instante de fin.
     * @return valor estimado, sin impuestos, depósitos ni cobro bancario.
     */
    fun valorPeriodo(tarifa: BigDecimal, inicio: OffsetDateTime, fin: OffsetDateTime): BigDecimal {
        require(tarifa.signum() >= 0) { "Tarifa invalida" }
        require(fin.isAfter(inicio)) { "La devolucion debe ser posterior" }
        val duracion = Duration.between(inicio, fin)
        val dias = duracion.toDays() + if (duracion == Duration.ofDays(duracion.toDays())) 0L else 1L
        return tarifa.multiply(BigDecimal.valueOf(dias.coerceAtLeast(1)))
    }
}
