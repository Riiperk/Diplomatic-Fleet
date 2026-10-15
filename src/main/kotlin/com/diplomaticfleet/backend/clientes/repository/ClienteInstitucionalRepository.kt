package com.diplomaticfleet.backend.clientes.repository

import com.diplomaticfleet.backend.clientes.domain.ClienteInstitucional
import com.diplomaticfleet.backend.clientes.domain.TipoEntidad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repositorio de acceso a los clientes institucionales.
 *
 * Proporciona operaciones CRUD y consultas derivadas para comprobar
 * datos únicos y filtrar instituciones por estado o clasificación.
 */
@Repository
interface ClienteInstitucionalRepository :
    JpaRepository<ClienteInstitucional, Long> {

    /**
     * Busca una institución mediante su nombre, ignorando mayúsculas.
     *
     * @param nombreEntidad Nombre institucional normalizado.
     * @return Cliente encontrado o `null`.
     */
    fun findByNombreEntidadIgnoreCase(
        nombreEntidad: String,
    ): ClienteInstitucional?

    /**
     * Comprueba si existe una institución con el nombre indicado.
     *
     * @param nombreEntidad Nombre institucional.
     * @return `true` cuando el nombre ya está registrado.
     */
    fun existsByNombreEntidadIgnoreCase(
        nombreEntidad: String,
    ): Boolean

    /**
     * Busca una institución mediante el correo de contacto.
     *
     * @param correoContacto Correo normalizado.
     * @return Cliente encontrado o `null`.
     */
    fun findByCorreoContactoIgnoreCase(
        correoContacto: String,
    ): ClienteInstitucional?

    /**
     * Comprueba si el correo ya pertenece a una institución.
     *
     * @param correoContacto Correo electrónico de contacto.
     * @return `true` cuando el correo ya está registrado.
     */
    fun existsByCorreoContactoIgnoreCase(
        correoContacto: String,
    ): Boolean

    /**
     * Busca una institución mediante su identificación fiscal.
     *
     * @param numeroIdentificacionFiscal Identificación fiscal.
     * @return Cliente encontrado o `null`.
     */
    fun findByNumeroIdentificacionFiscal(
        numeroIdentificacionFiscal: String,
    ): ClienteInstitucional?

    /**
     * Comprueba si existe una identificación fiscal registrada.
     *
     * @param numeroIdentificacionFiscal Identificación fiscal.
     * @return `true` cuando la identificación ya existe.
     */
    fun existsByNumeroIdentificacionFiscal(
        numeroIdentificacionFiscal: String,
    ): Boolean

    /**
     * Obtiene los clientes según su estado de activación.
     *
     * @param activo Estado requerido.
     * @return Instituciones que coinciden con el estado.
     */
    fun findAllByActivoOrderByNombreEntidadAsc(
        activo: Boolean,
    ): List<ClienteInstitucional>

    /**
     * Obtiene instituciones según su clasificación.
     *
     * @param tipoEntidad Tipo institucional solicitado.
     * @return Clientes de la clasificación indicada.
     */
    fun findAllByTipoEntidadOrderByNombreEntidadAsc(
        tipoEntidad: TipoEntidad,
    ): List<ClienteInstitucional>
}