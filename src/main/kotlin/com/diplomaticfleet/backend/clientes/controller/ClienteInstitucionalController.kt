package com.diplomaticfleet.backend.clientes.controller

import com.diplomaticfleet.backend.clientes.dto.ClienteInstitucionalRequest
import com.diplomaticfleet.backend.clientes.dto.ClienteInstitucionalResponse
import com.diplomaticfleet.backend.clientes.service.ClienteInstitucionalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST para clientes institucionales.
 *
 * Expone operaciones de registro, consulta y actualización mediante
 * la ruta base `/api/clientes`.
 *
 * @property service Servicio que aplica las reglas de negocio.
 */
@RestController
@RequestMapping("/api/clientes")
class ClienteInstitucionalController(
    private val service: ClienteInstitucionalService,
) {

    /**
     * Registra un cliente institucional.
     *
     * @param request Datos validados del cliente.
     * @return Cliente creado con estado HTTP 201.
     */
    @PostMapping
    fun registrar(
        @Valid
        @RequestBody
        request: ClienteInstitucionalRequest,
    ): ResponseEntity<ClienteInstitucionalResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(service.registrar(request))

    /**
     * Obtiene todos los clientes institucionales.
     *
     * @return Lista de instituciones registradas.
     */
    @GetMapping
    fun listar():
            ResponseEntity<List<ClienteInstitucionalResponse>> =
        ResponseEntity.ok(
            service.listar(),
        )

    /**
     * Consulta un cliente mediante su identificador.
     *
     * @param id Identificador del cliente.
     * @return Institución encontrada.
     */
    @GetMapping("/{id}")
    fun obtenerPorId(
        @PathVariable
        id: Long,
    ): ResponseEntity<ClienteInstitucionalResponse> =
        ResponseEntity.ok(
            service.obtenerPorId(id),
        )

    /**
     * Actualiza los datos de un cliente institucional.
     *
     * @param id Identificador del cliente.
     * @param request Nuevos datos validados.
     * @return Institución actualizada.
     */
    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable
        id: Long,
        @Valid
        @RequestBody
        request: ClienteInstitucionalRequest,
    ): ResponseEntity<ClienteInstitucionalResponse> =
        ResponseEntity.ok(
            service.actualizar(
                id = id,
                request = request,
            ),
        )
}