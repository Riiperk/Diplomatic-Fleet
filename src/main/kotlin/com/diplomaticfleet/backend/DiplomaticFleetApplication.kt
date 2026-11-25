package com.diplomaticfleet.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** Aplicación Spring Boot; descubre controladores, repositorios, configuración y servicios del paquete. */
@SpringBootApplication
class DiplomaticFleetApplication

/** Punto de entrada: inicia el contexto, Flyway y el servidor HTTP con los argumentos de Gradle. */
fun main(args: Array<String>) {
    runApplication<DiplomaticFleetApplication>(*args)
}
