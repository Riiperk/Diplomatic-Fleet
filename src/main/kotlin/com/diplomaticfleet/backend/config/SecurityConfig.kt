package com.diplomaticfleet.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.AuthenticationEntryPoint
import jakarta.servlet.http.HttpServletResponse
import com.diplomaticfleet.backend.seguridad.SesionesWeb
import com.diplomaticfleet.backend.seguridad.FiltroSesion
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.cors.CorsConfigurationSource

/** Reglas de acceso HTTP Basic para el laboratorio local; las rutas administrativas se autorizan en servidor. */
@Configuration
class SecurityConfig(
    @Value("\${DF_BASIC_LOCAL:true}") private val basicLocal: Boolean,
    @Value("\${DF_FRONTEND_ORIGIN:}") private val origen: String,
) {
    /** Utiliza BCrypt con sal aleatoria para almacenar credenciales irreversibles. */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Devuelve un error REST 401 sin el reto WWW-Authenticate de HTTP Basic.
     * El navegador no debe mostrar su cuadro de credenciales: Vue gestiona el
     * formulario. Esto no autentica al usuario ni concede acceso a rutas privadas.
     */
    private val accesoNoAutorizado = AuthenticationEntryPoint { _, response, _ ->
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.setHeader("Cache-Control", "no-store")
        response.writer.write("""{"mensaje":"Credenciales incorrectas o sesion no autorizada."}""")
    }

    /** Permite únicamente el origen frontend configurado; el proxy Vite no requiere CORS. */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        val origenesProduccion = origen.split(',')
            .map { it.trim() }
            .filter {
                it.isNotEmpty() &&
                        it.startsWith("https://") &&
                        !it.contains('*')
            }

        val origenesLocales = if (basicLocal) {
            listOf(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
            )
        } else {
            emptyList()
        }

        config.allowedOrigins =
            (origenesProduccion + origenesLocales).distinct()
        config.allowedMethods = listOf("GET","POST","PUT","PATCH","OPTIONS")
        config.allowedHeaders = listOf("Authorization","Content-Type","Accept","X-Requested-With")
        config.allowCredentials = false
        config.maxAge = 600L
        return UrlBasedCorsConfigurationSource().also { it.registerCorsConfiguration("/api/**",config) }
    }

    /** Aplica reglas de menor privilegio por ruta y rol; no utiliza cookies de sesión. */
    @Bean
    fun securityFilterChain(http: HttpSecurity, sesiones: SesionesWeb): SecurityFilterChain {
        http
            // Auth via Authorization header (no browser cookies), stateless API.
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .addFilterBefore(FiltroSesion(sesiones,basicLocal),BasicAuthenticationFilter::class.java)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { rules ->
                rules
                    .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                    .requestMatchers("/api/vehiculos", "/api/vehiculos/**", "/api/admin/documentos", "/api/admin/documentos/**", "/api/admin/mantenimientos", "/api/admin/mantenimientos/**")
                    .hasAnyRole("ADMINISTRADOR", "OPERACIONES")
                    .requestMatchers("/api/conductor/**").hasRole("CONDUCTOR")
                    .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                    .requestMatchers("/api/solicitudes/**", "/api/solicitudes").hasAnyRole("CLIENTE", "ADMINISTRADOR")
                     .requestMatchers("/api/gestion/**").hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/clientes", "/api/clientes/**", "/api/admin", "/api/admin/**")
                    .hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            // El mismo punto de entrada cubre credenciales incorrectas y ausencia de sesión.
            .exceptionHandling { it.authenticationEntryPoint(accesoNoAutorizado) }
            .httpBasic { it.authenticationEntryPoint(accesoNoAutorizado) }
            .formLogin { it.disable() }
        return http.build()
    }
}
