package com.diplomaticfleet.backend.flota.repository

import com.diplomaticfleet.backend.flota.domain.EstadoVehiculo
import com.diplomaticfleet.backend.flota.domain.TipoCombustible
import com.diplomaticfleet.backend.flota.domain.Vehiculo
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio encargado del acceso a los vehículos de la flota.
 *
 * Además de las operaciones básicas proporcionadas por
 * [JpaRepository], permite consultar vehículos por placa,
 * estado operativo, categoría y tipo de combustible.
 */
interface VehiculoRepository : JpaRepository<Vehiculo, Long> {

    /**
     * Busca un vehículo por su placa sin distinguir mayúsculas.
     *
     * @param placa Placa que se desea consultar.
     * @return El vehículo encontrado o `null` si no existe.
     */
    fun findByPlacaIgnoreCase(
        placa: String,
    ): Vehiculo?

    /**
     * Comprueba si una placa ya se encuentra registrada.
     *
     * @param placa Placa que se desea comprobar.
     * @return `true` si la placa ya pertenece a un vehículo.
     */
    fun existsByPlacaIgnoreCase(
        placa: String,
    ): Boolean

    /**
     * Obtiene los vehículos activos que tengan un estado específico.
     *
     * @param estado Estado operativo solicitado.
     * @param activo Indica si se buscan registros activos o inactivos.
     * @return Vehículos ordenados por marca y modelo.
     */
    fun findAllByEstadoAndActivoOrderByMarcaAscModeloAsc(
        estado: EstadoVehiculo,
        activo: Boolean,
    ): List<Vehiculo>

    /**
     * Obtiene los vehículos pertenecientes a una categoría.
     *
     * `CategoriaId` permite que Spring Data consulte la propiedad
     * `id` de la relación `categoria`.
     *
     * @param categoriaId Identificador de la categoría.
     * @param activo Indica si se buscan vehículos activos o inactivos.
     * @return Vehículos ordenados por marca y modelo.
     */
    fun findAllByCategoriaIdAndActivoOrderByMarcaAscModeloAsc(
        categoriaId: Long,
        activo: Boolean,
    ): List<Vehiculo>

    /**
     * Obtiene los vehículos según el tipo de combustible.
     *
     * @param combustible Tipo de combustible utilizado.
     * @param activo Indica si se buscan registros activos o inactivos.
     * @return Vehículos ordenados por marca y modelo.
     */
    fun findAllByCombustibleAndActivoOrderByMarcaAscModeloAsc(
        combustible: TipoCombustible,
        activo: Boolean,
    ): List<Vehiculo>
}