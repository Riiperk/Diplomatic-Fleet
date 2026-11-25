package com.diplomaticfleet.backend.seguridad

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Token opaco de 256 bits. El servidor conserva únicamente su hash y una caducidad de ocho horas. */
@Service
class SesionesWeb(private val jdbc: JdbcTemplate, private val auth: UsuarioAuth) {
    private val random = SecureRandom()
    /** El digest permite buscar el token sin almacenar la credencial original. */
    fun digest(token: String): String = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 0xff) }

    /** Crea una sesión después de que Spring Security haya validado la identidad. */
    @Transactional
    fun crear(correo: String): SesionResponse {
        val perfil = auth.perfil(correo)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also { random.nextBytes(it) })
        jdbc.update("DELETE FROM sesiones_web WHERE expira_en <= now()")
        jdbc.update("INSERT INTO sesiones_web(token_hash,usuario_id,expira_en) VALUES(?,?,now()+interval '8 hours')",digest(token),perfil.id)
        return SesionResponse(token,perfil)
    }

    /** Verifica vigencia y reconsulta los permisos actuales; una cuenta desactivada pierde acceso. */
    fun autenticar(token: String): Authentication? {
        if (!token.matches(Regex("[A-Za-z0-9_-]{43}"))) return null
        val correo = jdbc.query("""SELECT u.correo FROM sesiones_web s JOIN usuarios u ON u.id=s.usuario_id
            WHERE s.token_hash=? AND s.expira_en>now() AND u.activo=true""", { rs,_ -> rs.getString(1) },digest(token)).firstOrNull() ?: return null
        val cuenta = try { auth.loadUserByUsername(correo) } catch (_: org.springframework.security.core.userdetails.UsernameNotFoundException) { return null }
        return UsernamePasswordAuthenticationToken.authenticated(cuenta.username,null,cuenta.authorities)
    }

    /** Revoca exclusivamente el token presentado por el usuario. */
    fun revocar(token: String) { jdbc.update("DELETE FROM sesiones_web WHERE token_hash=?",digest(token)) }
}
data class SesionResponse(val token: String, val perfil: Perfil)

/** Filtro de credenciales explícitas. No utiliza cookies ni vuelve a enviar la contraseña. */
class FiltroSesion(private val sesiones: SesionesWeb, private val basicLocal: Boolean) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader("Authorization") ?: ""
        val login = request.requestURI == request.contextPath + "/api/auth/sesion" && request.method == "POST"
        if (header.startsWith("Basic ",ignoreCase=true) && !basicLocal && !login) {
            response.status=401; response.contentType="application/json"; response.setHeader("Cache-Control","no-store")
            response.writer.write("""{"mensaje":"Utilice una sesión web vigente."}"""); return
        }
        if (header.startsWith("Bearer ",ignoreCase=true)) {
            val identity = sesiones.autenticar(header.substring(7))
            if (identity == null) {
                response.status=401; response.contentType="application/json"; response.setHeader("Cache-Control","no-store")
                response.writer.write("""{"mensaje":"Sesión vencida o no válida."}"""); return
            }
            SecurityContextHolder.getContext().authentication=identity
        }
        chain.doFilter(request,response)
    }
}

/** Inicio y cierre explícitos de sesión; la contraseña se verifica solo mediante Spring Security. */
@RestController
@RequestMapping("/api/auth")
class SesionController(private val sesiones: SesionesWeb) {
    @PostMapping("/sesion")
    fun entrar(authentication: Authentication): SesionResponse = sesiones.crear(authentication.name)
    @PostMapping("/salir")
    fun salir(request: HttpServletRequest): Map<String,Boolean> {
        val header=request.getHeader("Authorization") ?: ""
        if (header.startsWith("Bearer ",ignoreCase=true)) sesiones.revocar(header.substring(7))
        return mapOf("ok" to true)
    }
}
