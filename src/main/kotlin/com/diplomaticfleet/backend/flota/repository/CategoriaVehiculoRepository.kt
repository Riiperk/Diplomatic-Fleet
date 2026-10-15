package com.diplomaticfleet.backend.flota.repository

import com.diplomaticfleet.backend.flota.domain.CategoriaVehiculo
import com.diplomaticfleet.backend.flota.domain.CodigoCategoriaVehiculo
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Repositorio encargado del acceso a las categorías de vehículos.
 *
 * Spring Data JPA proporciona automáticamente las operaciones básicas
 * de creación, consulta, actualización y eliminación para
 * [CategoriaVehiculo].
 */
interface CategoriaVehiculoRepository :
    JpaRepository<CategoriaVehiculo, Long> {

    /**
     * Busca una categoría mediante su código estable.
     *
     * @param codigo Código utilizado por el dominio y PostgreSQL.
     * @return La categoría encontrada o `null` si no existe.
     */
    fun findByCodigo(
        codigo: CodigoCategoriaVehiculo,
    ): CategoriaVehiculo?

    /**
     * Obtiene las categorías según su estado de activación.
     *
     * @param activo Estado por el que se filtrarán los registros.
     * @return Categorías que cumplen el estado solicitado.
     */
    fun findAllByActivoOrderByNombreAsc(
        activo: Boolean,
    ): List<CategoriaVehiculo>

    /**
     * Comprueba si existe una categoría con el código indicado.
     *
     * @param codigo Código de categoría que se desea comprobar.
     * @return `true` cuando el código ya está registrado.
     */
    fun existsByCodigo(
        codigo: CodigoCategoriaVehiculo,
    ): Boolean
}