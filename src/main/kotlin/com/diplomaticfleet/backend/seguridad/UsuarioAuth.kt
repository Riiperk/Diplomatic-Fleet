package com.diplomaticfleet.backend.seguridad

import com.diplomaticfleet.backend.exception.ConflictoException
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private const val ADMIN_ROLE = "ADMINISTRADOR"

/** Resuelve cuentas activas y roles persistidos; BCrypt compara hashes sin recuperar contraseñas originales. */
@Service
class UsuarioAuth(private val jdbc: JdbcTemplate, private val encoder: PasswordEncoder) : UserDetailsService {
    /** Carga las autoridades del usuario autenticado para el filtro HTTP Basic. */
    override fun loadUserByUsername(username: String): org.springframework.security.core.userdetails.UserDetails {
        val correo = username.trim().lowercase()
        val accounts = jdbc.query("SELECT id, correo, password_hash, activo FROM usuarios WHERE LOWER(correo)=?", { rs, _ ->
            Account(rs.getLong("id"), rs.getString("correo"), rs.getString("password_hash"), rs.getBoolean("activo"))
        }, correo)
        val account = accounts.firstOrNull() ?: throw UsernameNotFoundException("Cuenta inexistente")
        if (!account.activo) throw UsernameNotFoundException("Cuenta inactiva")
        val roles = jdbc.query("""SELECT r.nombre FROM roles r JOIN usuarios_roles ur ON ur.rol_id=r.id
            WHERE ur.usuario_id=? AND r.activo=true""", { rs, _ -> SimpleGrantedAuthority("ROLE_" + rs.getString(1)) }, account.id)
        return User(account.correo, account.hash, roles)
    }

    /** Proyecta el perfil visible, omitiendo el hash de autenticación. */
    fun perfil(correo: String): Perfil {
        return jdbc.query("""SELECT u.id,u.nombre,u.apellido,u.correo,u.cliente_institucional_id,
          (SELECT r.nombre FROM usuarios_roles ur JOIN roles r ON r.id=ur.rol_id
          WHERE ur.usuario_id=u.id AND r.activo=true ORDER BY CASE r.nombre WHEN 'ADMINISTRADOR' THEN 0 WHEN 'CLIENTE' THEN 1 ELSE 2 END LIMIT 1) AS rol
          FROM usuarios u WHERE LOWER(u.correo)=LOWER(?)""", { rs, _ ->
            Perfil(rs.getLong("id"), rs.getString("nombre"), rs.getString("apellido"),
                rs.getString("correo"), rs.getLong("cliente_institucional_id").let { if (rs.wasNull()) null else it },
                rs.getString("rol") ?: "SIN_ROL")
        }, correo).firstOrNull() ?: throw UsernameNotFoundException("Cuenta inexistente")
    }

    /** Crea institución y usuario CLIENTE en una sola transacción; valida ubicación y unicidad. */
    @Transactional
    fun registrar(req: RegistroRequest): Perfil {
        if (req.password.toByteArray(Charsets.UTF_8).size !in 8..72) throw IllegalArgumentException("La contraseña requiere al menos 8 caracteres")
        if (req.tipoEntidad !in setOf("EMBAJADA", "CONSULADO", "ORGANISMO_INTERNACIONAL", "OTRO"))
            throw IllegalArgumentException("Tipo de institución inválido")
        val email = req.correo.trim().lowercase()
        if (jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo)=?", Long::class.java, email)!! > 0)
            throw ConflictoException("El correo ya está registrado")
        if (jdbc.queryForObject("SELECT COUNT(*) FROM clientes_institucionales WHERE LOWER(nombre_entidad)=LOWER(?)", Long::class.java,req.nombreEntidad.trim())!! > 0)
            throw ConflictoException("La institución ya existe; contacte a administración para asociar otra cuenta")
        // La direccion y la ciudad se registran con la institucion, no solo con la cuenta.
        // Las instituciones antiguas con estos datos vacios siguen siendo validas.
        if (req.ciudad.trim().length < 2 || req.direccion.trim().length < 5)
            throw IllegalArgumentException("Ingrese ciudad y direccion institucional validas")
        val clienteId = jdbc.queryForObject("""INSERT INTO clientes_institucionales
          (nombre_entidad,tipo_entidad,nombre_contacto,correo_contacto,telefono_contacto,
           numero_identificacion_fiscal,ciudad,direccion)
          VALUES(?,?,?,?,?,?,?,?) RETURNING id""", Long::class.java,
            req.nombreEntidad.trim(), req.tipoEntidad,
            "${req.nombre.trim()} ${req.apellido.trim()}", email, req.telefono?.trim(),
            req.numeroIdentificacionFiscal?.trim()?.ifBlank { null },
            req.ciudad.trim(), req.direccion.trim())!!
        val id = jdbc.queryForObject("""INSERT INTO usuarios
          (nombre,apellido,correo,password_hash,cliente_institucional_id)
          VALUES(?,?,?,?,?) RETURNING id""",Long::class.java,req.nombre.trim(),req.apellido.trim(),email,
            encoder.encode(req.password),clienteId)!!
        jdbc.update("""INSERT INTO usuarios_roles(usuario_id,rol_id)
          SELECT ?,id FROM roles WHERE nombre='CLIENTE'""",id)
        return Perfil(id,req.nombre.trim(),req.apellido.trim(),email,clienteId,"CLIENTE")
    }

    /** Proyección interna de autenticación, nunca utilizada como respuesta REST. */
    private data class Account(val id: Long,val correo: String,val hash: String,val activo: Boolean)
}

/** Contrato de sesión consumido por Vue: identificador, contacto, institución y rol. */
data class Perfil(val id: Long,val nombre: String,val apellido: String,val correo: String,
    val clienteInstitucionalId: Long?,val rol: String)

/** Campos obligatorios del registro público; no permite elegir un rol privilegiado. */
data class RegistroRequest(
    @field:NotBlank @field:Size(max=70) val nombre: String,
    @field:NotBlank @field:Size(max=70) val apellido: String,
    @field:NotBlank @field:Email val correo: String,
    @field:Size(min=8,max=72) val password: String,
    @field:NotBlank @field:Size(max=150) val nombreEntidad: String,
    @field:NotBlank val tipoEntidad: String,
    @field:NotBlank @field:Size(min=2,max=100) val ciudad: String,
    @field:NotBlank @field:Size(min=5,max=255) val direccion: String,
    @field:Size(max=40) val numeroIdentificacionFiscal: String? = null,
    @field:Size(max=30) val telefono: String? = null,
)

/** API de identidad: consulta de sesión y alta pública de clientes. */
@RestController
@RequestMapping("/api/auth")
class AuthController(private val auth: UsuarioAuth,
                     @Value("\${DF_PUBLIC_REGISTRATION:true}") private val registroPublico: Boolean) {
    /** Devuelve la identidad confirmada por Spring Security. */
    @GetMapping("/me")
    fun me(authentication: org.springframework.security.core.Authentication): Perfil = auth.perfil(authentication.name)
    /** Registra exclusivamente cuentas institucionales mediante Jakarta Validation. */
    @PostMapping("/registro")
    fun registro(@Valid @RequestBody req: RegistroRequest): Perfil {
        if (!registroPublico) throw org.springframework.security.access.AccessDeniedException("Registro público cerrado; contacte a administración")
        return auth.registrar(req)
    }
}

/** Inicializa el administrador solo si las variables locales se configuraron y la cuenta no existe. */
@org.springframework.context.annotation.Configuration
class AdminBootstrapping {
    /** Ejecuta el alta inicial al arrancar; no sustituye ni restablece cuentas existentes. */
    @Bean
    fun initialAdmin(jdbc: JdbcTemplate, encoder: PasswordEncoder,
                     @Value("\${ADMIN_EMAIL:}") email: String,
                     @Value("\${ADMIN_PASSWORD:}") password: String): ApplicationRunner = ApplicationRunner {
        // Credentials deliberately supplied by environment; never commit a default password.
        if (email.isNotBlank() && password.length >= 8) {
            val normalized = email.trim().lowercase()
            val exists = jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo)=?", Long::class.java,normalized)!! > 0
            if (!exists) {
                val id = jdbc.queryForObject("""INSERT INTO usuarios(nombre,apellido,correo,password_hash)
                  VALUES('Administrador','Fleet',?,?) RETURNING id""",Long::class.java,normalized,encoder.encode(password))!!
                jdbc.update("""INSERT INTO usuarios_roles(usuario_id,rol_id)
                  SELECT ?,id FROM roles WHERE nombre='ADMINISTRADOR'""",id)
            }
        }
    }
}
