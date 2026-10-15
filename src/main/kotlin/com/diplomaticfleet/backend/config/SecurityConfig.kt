package com.diplomaticfleet.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

/**
 * Configuración de seguridad HTTP de Diplomatic Fleet.
 *
 * Mantiene autenticados los endpoints de la API mediante HTTP Basic
 * y excluye los endpoints de la API de CSRF porque reciben datos JSON y no
 * utiliza formularios tradicionales basados en sesión.
*/
@Configuration
class SecurityConfig {

/**
 * Configura las reglas de acceso y protección de la aplicación.
 *
 * @param http Configuración de seguridad proporcionada por Spring.
 * @return Cadena de filtros utilizada para proteger las solicitudes.
*/
@Bean
fun securityFilterChain(
http: HttpSecurity,
): SecurityFilterChain {
http
.csrf { csrf ->
// La API REST recibe JSON y será consumida por Vue.js.
csrf.ignoringRequestMatchers("/api/**")
}
.authorizeHttpRequests { solicitudes ->
solicitudes
.requestMatchers("/api/**").authenticated()
.anyRequest().permitAll()
}
.httpBasic(Customizer.withDefaults())
.formLogin(Customizer.withDefaults())

return http.build()
}
}