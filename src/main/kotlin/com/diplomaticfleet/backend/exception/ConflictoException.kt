package com.diplomaticfleet.backend.exception

/**
 * Excepción utilizada cuando una operación incumple una regla
 * de unicidad o entra en conflicto con el estado actual del sistema.
 *
 * Permite representar situaciones como una placa duplicada o una
 * transición de estado no permitida mediante una respuesta HTTP 409.
 *
 * @param message Descripción del conflicto detectado.
 */
class ConflictoException(
    message: String,
) : RuntimeException(message)