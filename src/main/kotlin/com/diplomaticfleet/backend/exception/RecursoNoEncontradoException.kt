package com.diplomaticfleet.backend.exception

/**
 * Excepción utilizada cuando un recurso solicitado no existe.
 *
 * La capa de servicios lanza esta excepción para evitar devolver
 * valores nulos y permitir que la API responda posteriormente
 * con un estado HTTP 404.
 *
 * @param message Descripción del recurso que no pudo encontrarse.
 */
class RecursoNoEncontradoException(
    message: String,
) : RuntimeException(message)