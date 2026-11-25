package com.diplomaticfleet.backend.catalogo.repository

import com.diplomaticfleet.backend.catalogo.domain.SolicitudCatalogo
import org.springframework.data.jpa.repository.JpaRepository

/** Mantiene el historial para control comercial y presentación académica. */
interface SolicitudCatalogoRepository : JpaRepository<SolicitudCatalogo, Long> {
    fun findAllByOrderByCreadoEnDesc(): List<SolicitudCatalogo>
}
