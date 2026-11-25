package com.diplomaticfleet.backend.catalogo.controller

import com.diplomaticfleet.backend.catalogo.dto.*
import com.diplomaticfleet.backend.catalogo.service.SolicitudCatalogoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** Endpoint público: solo recepción, nunca listado de datos personales. */
@RestController
@RequestMapping("/api/catalogo/solicitudes")
class SolicitudCatalogoController(private val service: SolicitudCatalogoService) {
    /** Recibe un interés comercial preliminar, sin reservar inventario. */
    @PostMapping
    fun solicitar(@Valid @RequestBody request: SolicitudCatalogoRequest): ResponseEntity<SolicitudCreadaResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request))
}

/** Endpoints internos: consultar contactos y registrar seguimiento comercial. */
@RestController
@RequestMapping("/api/gestion/solicitudes-catalogo")
class SolicitudCatalogoAdminController(private val service: SolicitudCatalogoService) {
    /** Lista los contactos comerciales únicamente para administración. */
    @GetMapping
    fun listar(): List<SolicitudCatalogoAdminResponse> = service.listar()

    /** Actualiza el seguimiento comercial, sin confirmar una reserva formal. */
    @PatchMapping("/{id}/estado")
    fun cambiarEstado(
        @PathVariable id: Long,
        @Valid @RequestBody request: CambiarEstadoSolicitudRequest,
    ): SolicitudCatalogoAdminResponse =
        service.actualizarEstado(id, requireNotNull(request.estado))
}
