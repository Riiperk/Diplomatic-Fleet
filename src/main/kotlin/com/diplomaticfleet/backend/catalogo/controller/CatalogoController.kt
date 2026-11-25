package com.diplomaticfleet.backend.catalogo.controller

import com.diplomaticfleet.backend.catalogo.dto.CatalogoVehiculoResponse
import com.diplomaticfleet.backend.catalogo.service.CatalogoVehiculoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** API pública solo de lectura; permite navegar sin credenciales de administración. */
@RestController
@RequestMapping("/api/catalogo/vehiculos")
class CatalogoController(private val service: CatalogoVehiculoService) {
    /** Lista fichas comerciales públicas de la ruta histórica. */
    @GetMapping
    fun listar(): List<CatalogoVehiculoResponse> = service.listar()

    /** Recupera la ficha visible del vehículo identificado. */
    @GetMapping("/{id}")
    fun detalle(@PathVariable id: Long): CatalogoVehiculoResponse = service.detalle(id)
}
