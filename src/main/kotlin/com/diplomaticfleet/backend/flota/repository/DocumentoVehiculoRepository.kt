package com.diplomaticfleet.backend.flota.repository

import com.diplomaticfleet.backend.flota.domain.DocumentoVehiculo
import com.diplomaticfleet.backend.flota.domain.EstadoDocumentoVehiculo
import com.diplomaticfleet.backend.flota.domain.TipoDocumentoVehiculo
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

/**
 * Repositorio encargado del acceso a los documentos vehiculares.
 *
 * Permite consultar documentos por vehículo, tipo, estado de vigencia
 * y fecha de vencimiento.
 */
interface DocumentoVehiculoRepository :
    JpaRepository<DocumentoVehiculo, Long> {

    /**
     * Obtiene todos los documentos asociados a un vehículo.
     *
     * @param vehiculoId Identificador del vehículo.
     * @return Documentos ordenados por fecha de vencimiento.
     */
    fun findAllByVehiculoIdOrderByFechaVencimientoAsc(
        vehiculoId: Long,
    ): List<DocumentoVehiculo>

    /**
     * Obtiene los documentos de un tipo concreto para un vehículo.
     *
     * @param vehiculoId Identificador del vehículo.
     * @param tipoDocumento Tipo documental solicitado.
     * @return Documentos coincidentes ordenados por vencimiento.
     */
    fun findAllByVehiculoIdAndTipoDocumentoOrderByFechaVencimientoDesc(
        vehiculoId: Long,
        tipoDocumento: TipoDocumentoVehiculo,
    ): List<DocumentoVehiculo>

    /**
     * Obtiene los documentos que se encuentran en un estado determinado.
     *
     * @param estado Estado de vigencia solicitado.
     * @return Documentos ordenados por la fecha de vencimiento más próxima.
     */
    fun findAllByEstadoOrderByFechaVencimientoAsc(
        estado: EstadoDocumentoVehiculo,
    ): List<DocumentoVehiculo>

    /**
     * Busca documentos cuya fecha de vencimiento esté dentro
     * del periodo indicado.
     *
     * @param fechaInicial Primera fecha incluida en la consulta.
     * @param fechaFinal Última fecha incluida en la consulta.
     * @return Documentos que vencen dentro del periodo.
     */
    fun findAllByFechaVencimientoBetweenOrderByFechaVencimientoAsc(
        fechaInicial: LocalDate,
        fechaFinal: LocalDate,
    ): List<DocumentoVehiculo>

    /**
     * Comprueba si un vehículo tiene registrado un tipo de documento.
     *
     * @param vehiculoId Identificador del vehículo.
     * @param tipoDocumento Tipo que se desea comprobar.
     * @return `true` cuando existe al menos un documento coincidente.
     */
    fun existsByVehiculoIdAndTipoDocumento(
        vehiculoId: Long,
        tipoDocumento: TipoDocumentoVehiculo,
    ): Boolean
}