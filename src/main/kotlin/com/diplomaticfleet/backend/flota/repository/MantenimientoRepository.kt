package com.diplomaticfleet.backend.flota.repository

import com.diplomaticfleet.backend.flota.domain.EstadoMantenimiento
import com.diplomaticfleet.backend.flota.domain.Mantenimiento
import com.diplomaticfleet.backend.flota.domain.TipoMantenimiento
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

/**
 * Repositorio encargado del acceso a los mantenimientos vehiculares.
 *
 * Permite consultar mantenimientos por vehículo, tipo,
 * estado y fecha programada.
 */
interface MantenimientoRepository :
    JpaRepository<Mantenimiento, Long> {

    /**
     * Obtiene todos los mantenimientos asociados a un vehículo.
     *
     * @param vehiculoId Identificador del vehículo.
     * @return Mantenimientos ordenados por fecha programada descendente.
     */
    fun findAllByVehiculoIdOrderByFechaProgramadaDesc(
        vehiculoId: Long,
    ): List<Mantenimiento>

    /**
     * Obtiene los mantenimientos de un tipo concreto.
     *
     * @param tipo Tipo de mantenimiento solicitado.
     * @return Mantenimientos ordenados por fecha programada descendente.
     */
    fun findAllByTipoOrderByFechaProgramadaDesc(
        tipo: TipoMantenimiento,
    ): List<Mantenimiento>

    /**
     * Obtiene los mantenimientos que se encuentran en un estado determinado.
     *
     * @param estado Estado del mantenimiento.
     * @return Mantenimientos ordenados por fecha programada ascendente.
     */
    fun findAllByEstadoOrderByFechaProgramadaAsc(
        estado: EstadoMantenimiento,
    ): List<Mantenimiento>

    /**
     * Busca mantenimientos programados dentro de un periodo.
     *
     * @param fechaInicial Primera fecha incluida en la consulta.
     * @param fechaFinal Última fecha incluida en la consulta.
     * @return Mantenimientos programados dentro del periodo indicado.
     */
    fun findAllByFechaProgramadaBetweenOrderByFechaProgramadaAsc(
        fechaInicial: LocalDate,
        fechaFinal: LocalDate,
    ): List<Mantenimiento>

    /**
     * Comprueba si un vehículo tiene mantenimientos asociados.
     *
     * @param vehiculoId Identificador del vehículo.
     * @return `true` cuando existe al menos un mantenimiento registrado.
     */
    fun existsByVehiculoId(
        vehiculoId: Long,
    ): Boolean
}