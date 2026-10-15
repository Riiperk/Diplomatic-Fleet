package com.diplomaticfleet.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.OffsetDateTime

/**
 * Estructura utilizada para comunicar errores controlados al cliente.
 *
 * @property fecha Momento en que se produjo el error.
 * @property estado Código HTTP asociado.
 * @property error Nombre del tipo de error HTTP.
 * @property mensaje Descripción comprensible del problema.
 * @property validaciones Errores de validación organizados por campo.
 */
data class ErrorResponse(
    val fecha: OffsetDateTime = OffsetDateTime.now(),
    val estado: Int,
    val error: String,
    val mensaje: String,
    val validaciones: Map<String, String>? = null,
)

/**
 * Controlador global de excepciones de la API.
 *
 * Convierte las excepciones de negocio y los errores de validación
 * en respuestas HTTP uniformes para facilitar su tratamiento desde Vue.js.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Responde con HTTP 404 cuando no existe el recurso solicitado.
     *
     * @param exception Excepción generada por la capa de servicios.
     * @return Respuesta estructurada con información del error.
     */
    @ExceptionHandler(RecursoNoEncontradoException::class)
    fun manejarRecursoNoEncontrado(
        exception: RecursoNoEncontradoException,
    ): ResponseEntity<ErrorResponse> =
        construirRespuesta(
            estado = HttpStatus.NOT_FOUND,
            mensaje = exception.message ?: "El recurso solicitado no existe.",
        )

    /**
     * Responde con HTTP 409 cuando una operación produce un conflicto.
     *
     * @param exception Excepción generada por una regla de negocio.
     * @return Respuesta estructurada con información del conflicto.
     */
    @ExceptionHandler(ConflictoException::class)
    fun manejarConflicto(
        exception: ConflictoException,
    ): ResponseEntity<ErrorResponse> =
        construirRespuesta(
            estado = HttpStatus.CONFLICT,
            mensaje = exception.message ?: "La operación genera un conflicto.",
        )

    /**
     * Responde con HTTP 400 cuando el cuerpo recibido incumple
     * las restricciones declaradas mediante Jakarta Validation.
     *
     * @param exception Resultado de las validaciones fallidas.
     * @return Respuesta con los mensajes organizados por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun manejarValidacion(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ErrorResponse> {
        val validaciones = exception.bindingResult.fieldErrors
            .associate { error ->
                error.field to (error.defaultMessage ?: "Valor no válido.")
            }

        val cuerpo = ErrorResponse(
            estado = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            mensaje = "Los datos enviados no superaron la validación.",
            validaciones = validaciones,
        )

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(cuerpo)
    }

    /**
     * Construye una respuesta uniforme para errores de negocio.
     *
     * @param estado Estado HTTP que se devolverá.
     * @param mensaje Explicación del error.
     * @return Respuesta HTTP estructurada.
     */
    private fun construirRespuesta(
        estado: HttpStatus,
        mensaje: String,
    ): ResponseEntity<ErrorResponse> {
        val cuerpo = ErrorResponse(
            estado = estado.value(),
            error = estado.reasonPhrase,
            mensaje = mensaje,
        )

        return ResponseEntity
            .status(estado)
            .body(cuerpo)
    }
}
