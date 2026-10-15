package com.diplomaticfleet.backend.flota.controller

import com.diplomaticfleet.backend.flota.dto.VehiculoRequest
import com.diplomaticfleet.backend.flota.dto.VehiculoResponse
import com.diplomaticfleet.backend.flota.service.VehiculoService
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
 * Controlador REST encargado de gestionar los vehículos de la flota.
 *
 * Expone operaciones para registrar, listar, consultar y actualizar
 * vehículos mediante la ruta base `/api/vehiculos`.
 *
 * @property vehiculoService Servicio que aplica las reglas de negocio.
 */
@RestController
@RequestMapping("/api/vehiculos")
class VehiculoController(
    private val vehiculoService: VehiculoService,
) {

    /**
     * Registra un vehículo nuevo en la flota.
     *
     * @param request Datos validados del vehículo.
     * @return Vehículo registrado con estado HTTP 201.
     */
    @PostMapping
    fun registrar(
        @Valid
        @RequestBody
        request: VehiculoRequest,
    ): ResponseEntity<VehiculoResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(vehiculoService.registrar(request))

    /**
     * Obtiene todos los vehículos registrados.
     *
     * @return Vehículos disponibles para consulta.
     */
    @GetMapping
    fun listar(): ResponseEntity<List<VehiculoResponse>> =
        ResponseEntity.ok(
            vehiculoService.listar(),
        )

    /**
     * Consulta un vehículo mediante su identificador.
     *
     * @param id Identificador del vehículo.
     * @return Información del vehículo encontrado.
     */
    @GetMapping("/{id}")
    fun obtenerPorId(
        @PathVariable
        id: Long,
    ): ResponseEntity<VehiculoResponse> =
        ResponseEntity.ok(
            vehiculoService.obtenerPorId(id),
        )

    /**
     * Actualiza los datos generales de un vehículo.
     *
     * Esta operación no modifica directamente su estado operativo.
     *
     * @param id Identificador del vehículo.
     * @param request Nuevos datos validados.
     * @return Información actualizada del vehículo.
     */
    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable
        id: Long,
        @Valid
        @RequestBody
        request: VehiculoRequest,
    ): ResponseEntity<VehiculoResponse> =
        ResponseEntity.ok(
            vehiculoService.actualizar(
                id = id,
                request = request,
            ),
        )
}