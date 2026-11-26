package com.diplomaticfleet.backend.operaciones

import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import com.diplomaticfleet.backend.seguridad.UsuarioAuth
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.OffsetDateTime

/** Datos de una cuenta nueva; el servidor valida el rol y nunca devuelve la contraseña. */
data class CuentaNueva(
    @field:NotBlank @field:Size(max=70) val nombre: String,
    @field:NotBlank @field:Size(max=70) val apellido: String,
    @field:NotBlank @field:Email @field:Size(max=150) val correo: String,
    @field:Size(min=8,max=72) val password: String,
    @field:NotBlank val rol: String,
    val clienteInstitucionalId: Long? = null,
    @field:Size(max=60) val numeroLicencia: String? = null,
    @field:Size(max=20) val categoriaLicencia: String? = null,
    val vencimientoLicencia: LocalDate? = null,
)

/** Nueva contraseña administrativa; no se devuelve ni se almacena en texto claro. */
data class CambioCredencial(@field:Size(min=12,max=72) val password: String)

/** Clave del conductor profesional seleccionado en la bandeja de reservas. */
data class AsignarProfesional(val conductorId: Long)

/** Identifica una decisión comercial realizada por el cliente autenticado. */
data class DecisionCotizacion(@field:NotBlank val estado: String)

/**
 * Operaciones de V10 sobre cuentas, conductores profesionales y cotizaciones.
 * Las rutas están segregadas por rol en SecurityConfig; los filtros de propiedad
 * se aplican también en SQL. Todas las escrituras se ejecutan en transacciones.
 */
@RestController
class AccesoYAsignacionesController(
    private val jdbc: JdbcTemplate,
    private val auth: UsuarioAuth,
    private val encoder: PasswordEncoder,
) {
    /**
     * Crea una cuenta de demostración o de operación sin cambiar cuentas existentes.
     * Los clientes se vinculan a una institución activa; los conductores requieren licencia vigente.
     * @param req formulario de alta validado.
     * @return identificador y rol, sin datos de autenticación sensibles.
     */
    @PostMapping("/api/admin/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun crearCuenta(@Valid @RequestBody req: CuentaNueva): Map<String, Any?> {
        val rol=req.rol.trim().uppercase()
        if(rol !in setOf("ADMINISTRADOR","CLIENTE","OPERACIONES","CONDUCTOR")) throw IllegalArgumentException("Rol no habilitado")
        if(req.password.toByteArray(Charsets.UTF_8).size !in 8..72) throw IllegalArgumentException("Contraseña de 8 a 72 bytes UTF-8")
        val correo=req.correo.trim().lowercase()
        if((jdbc.queryForObject("SELECT COUNT(*) FROM usuarios WHERE LOWER(correo)=?",Long::class.java,correo) ?: 0L)>0)
            throw ConflictoException("El correo ya está registrado")
        if(rol=="CLIENTE") {
            if(req.clienteInstitucionalId==null || (jdbc.queryForObject("SELECT COUNT(*) FROM clientes_institucionales WHERE id=? AND activo=true",Long::class.java,req.clienteInstitucionalId) ?: 0L)==0L)
                throw ConflictoException("Seleccione una institución activa")
        } else if(req.clienteInstitucionalId!=null) throw IllegalArgumentException("Solo CLIENTE admite vínculo institucional")
        if(rol=="CONDUCTOR" && (req.numeroLicencia.isNullOrBlank() || req.categoriaLicencia.isNullOrBlank() || req.vencimientoLicencia==null || req.vencimientoLicencia.isBefore(LocalDate.now())))
            throw IllegalArgumentException("Complete una licencia profesional vigente")
        val rolId=jdbc.query("SELECT id FROM roles WHERE nombre=? AND activo=true",{rs,_->rs.getLong(1)},rol).firstOrNull()
            ?: throw ConflictoException("Rol inactivo")
        val id=jdbc.queryForObject("""INSERT INTO usuarios(nombre,apellido,correo,password_hash,cliente_institucional_id)
            VALUES(?,?,?,?,?) RETURNING id""",Long::class.java,req.nombre.trim(),req.apellido.trim(),correo,encoder.encode(req.password),req.clienteInstitucionalId)!!
        jdbc.update("INSERT INTO usuarios_roles(usuario_id,rol_id) VALUES(?,?)",id,rolId)
        if(rol=="CONDUCTOR") jdbc.update("""INSERT INTO conductores_profesionales(usuario_id,numero_licencia,categoria_licencia,fecha_vencimiento_licencia)
            VALUES(?,?,?,?)""",id,req.numeroLicencia!!.trim(),req.categoriaLicencia!!.trim().uppercase(),req.vencimientoLicencia)
        return mapOf("id" to id,"correo" to correo,"rol" to rol)
    }

    /** Restablece la credencial de una cuenta y revoca todas sus sesiones activas. */
    @PostMapping("/api/admin/usuarios/{id}/credencial")
    @Transactional
    fun cambiarCredencial(@PathVariable id:Long,@Valid @RequestBody req:CambioCredencial):Map<String,Boolean> {
        require(req.password.toByteArray(Charsets.UTF_8).size in 12..72) { "La contraseña debe tener de 12 a 72 bytes UTF-8" }
        if(jdbc.update("UPDATE usuarios SET password_hash=?,actualizado_en=now() WHERE id=?",encoder.encode(req.password),id)!=1)
            throw RecursoNoEncontradoException("Cuenta inexistente")
        jdbc.update("DELETE FROM sesiones_web WHERE usuario_id=?",id)
        return mapOf("ok" to true)
    }

    /** Consulta personal profesional y vigencia declarada para selección administrativa. */
    @GetMapping("/api/admin/conductores-profesionales")
    fun profesionales(): List<Map<String,Any?>> = jdbc.queryForList("""SELECT cp.id,cp.usuario_id,
        CONCAT_WS(' ',u.nombre,u.apellido) AS nombre,cp.numero_licencia,cp.categoria_licencia,
        cp.fecha_vencimiento_licencia::text AS vencimiento,
        (cp.activo AND u.activo AND cp.fecha_vencimiento_licencia>=CURRENT_DATE) AS habilitado
        FROM conductores_profesionales cp JOIN usuarios u ON u.id=cp.usuario_id ORDER BY u.nombre,u.apellido""")

    /** Datos mínimos de una reserva para validar modalidad y fechas sin exponer otra institución. */
    private data class Periodo(val estado:String,val modalidad:String,val inicio:OffsetDateTime,val fin:OffsetDateTime)

    /**
     * Asigna o reasigna un profesional a una reserva confirmada. Bloquea la fila del
     * conductor durante la consulta de solapamientos para impedir asignaciones simultáneas.
     */
    @PatchMapping("/api/admin/solicitudes/{id}/conductor")
    @Transactional
    fun asignar(@PathVariable id:Long,@RequestBody req:AsignarProfesional):Map<String,Any?> {
        val s=jdbc.query("SELECT estado,modalidad,fecha_inicio,fecha_fin FROM solicitudes WHERE id=? FOR UPDATE",{rs,_->
            Periodo(rs.getString(1),rs.getString(2),rs.getObject(3,OffsetDateTime::class.java),rs.getObject(4,OffsetDateTime::class.java))},id).firstOrNull()
            ?: throw RecursoNoEncontradoException("Reserva inexistente")
        if(s.modalidad!="CON_CONDUCTOR" || s.estado !in setOf("CONFIRMADA","ASIGNADA") || !s.fin.isAfter(OffsetDateTime.now()))
            throw ConflictoException("La reserva no admite asignación profesional")
        val habilitado=jdbc.query("""SELECT cp.activo AND u.activo AND cp.fecha_vencimiento_licencia>=?
            FROM conductores_profesionales cp JOIN usuarios u ON u.id=cp.usuario_id WHERE cp.id=? FOR UPDATE OF cp""",{rs,_->rs.getBoolean(1)},s.fin.atZoneSameInstant(java.time.ZoneId.of("America/Bogota")).toLocalDate(),req.conductorId).firstOrNull() ?: false
        if(!habilitado) throw ConflictoException("Conductor inactivo o licencia insuficiente para el periodo")
        val solapamientos=jdbc.queryForObject("""SELECT COUNT(*) FROM solicitudes WHERE conductor_profesional_id=? AND id<>?
            AND estado IN ('CONFIRMADA','ASIGNADA','EN_SERVICIO') AND (estado='EN_SERVICIO' OR (fecha_inicio<? AND fecha_fin>?))""",Long::class.java,req.conductorId,id,s.fin,s.inicio) ?: 0L
        if(solapamientos>0) throw ConflictoException("Conductor no disponible en ese intervalo")
        jdbc.update("UPDATE solicitudes SET conductor_profesional_id=?,estado='ASIGNADA',actualizado_en=NOW() WHERE id=?",req.conductorId,id)
        return mapOf("id" to id,"estado" to "ASIGNADA","conductorProfesionalId" to req.conductorId)
    }

    /** Cada profesional consulta solo sus servicios; el usuario no proporciona un ID ajeno. */
    @GetMapping("/api/conductor/asignaciones")
    fun asignaciones(authentication:Authentication):List<Map<String,Any?>> = jdbc.queryForList("""SELECT s.id,s.codigo,s.estado,
        CONCAT_WS(' ',v.marca,v.modelo) AS vehiculo,to_char(s.fecha_inicio AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS"Z"') AS inicio,to_char(s.fecha_fin AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS"Z"') AS fin,
        s.lugar_recogida AS recogida,s.lugar_devolucion AS devolucion
        FROM solicitudes s JOIN conductores_profesionales cp ON cp.id=s.conductor_profesional_id
        JOIN usuarios u ON u.id=cp.usuario_id JOIN vehiculos v ON v.id=s.vehiculo_id
        WHERE LOWER(u.correo)=LOWER(?) AND cp.activo=true ORDER BY s.fecha_inicio DESC""",authentication.name)

    /** Devuelve propuestas propias, nunca borradores internos ni cotizaciones de otras instituciones. */
    @GetMapping("/api/cliente/cotizaciones")
    fun propuestas(authentication:Authentication):List<Map<String,Any?>> {
        val cliente=auth.perfil(authentication.name).clienteInstitucionalId ?: throw AccessDeniedException("Sin institución")
        return jdbc.queryForList("""SELECT c.id,c.solicitud_id,s.codigo,c.numero_version,c.estado,c.valida_hasta::text AS valida_hasta,
            c.subtotal,c.impuestos,c.descuentos,c.valor_total,c.observaciones,s.estado AS estado_reserva,
            (c.estado='ENVIADA' AND c.valida_hasta>=CURRENT_DATE AND s.estado IN ('RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION') AND c.numero_version=(SELECT MAX(v.numero_version) FROM cotizaciones v WHERE v.solicitud_id=s.id)) AS decidible
            FROM cotizaciones c JOIN solicitudes s ON s.id=c.solicitud_id
            WHERE s.cliente_institucional_id=? AND c.estado<>'BORRADOR' ORDER BY c.id DESC""",cliente)
    }

    /**
     * El contacto institucional acepta o rechaza una propuesta enviada y vigente.
     * Se conserva la cuenta responsable y la fecha de decisión para trazabilidad.
     */
    @PatchMapping("/api/cliente/cotizaciones/{id}/estado")
    @Transactional
    fun decidir(@PathVariable id:Long,authentication:Authentication,@Valid @RequestBody req:DecisionCotizacion):Map<String,Any?> {
        val perfil=auth.perfil(authentication.name)
        val cliente=perfil.clienteInstitucionalId ?: throw AccessDeniedException("Sin institución")
        if(req.estado !in setOf("ACEPTADA","RECHAZADA")) throw IllegalArgumentException("Decisión inválida")
        val solicitud=jdbc.query("""SELECT s.id,s.estado FROM solicitudes s JOIN cotizaciones c ON c.solicitud_id=s.id
            WHERE c.id=? AND s.cliente_institucional_id=? FOR UPDATE OF s""",{rs,_->Pair(rs.getLong(1),rs.getString(2))},id,cliente).firstOrNull()
            ?: throw RecursoNoEncontradoException("Cotización no encontrada")
        if(solicitud.second !in setOf("RECIBIDA","EN_REVISION","COTIZADA","PENDIENTE_APROBACION")) throw ConflictoException("La solicitud ya no admite decisiones")
        val cambio=jdbc.update("""UPDATE cotizaciones SET estado=?,decidido_por_usuario_id=?,fecha_decision=NOW(),actualizado_en=NOW()
            WHERE id=? AND estado='ENVIADA' AND valida_hasta>=CURRENT_DATE AND numero_version=(SELECT MAX(numero_version) FROM cotizaciones WHERE solicitud_id=?)""",req.estado,perfil.id,id,solicitud.first)
        if(cambio==0) throw ConflictoException("La propuesta no está vigente o ya fue resuelta")
        if(req.estado=="RECHAZADA") jdbc.update("UPDATE solicitudes SET estado='EN_REVISION',actualizado_en=NOW() WHERE id=?",solicitud.first)
        return propuestas(authentication).first { (it["id"] as Number).toLong()==id }
    }
    /** Consulta los 500 cambios técnicos más recientes sin devolver información personal o credenciales. */
    @GetMapping("/api/admin/auditoria")
    fun auditoria():List<Map<String,Any?>> = jdbc.queryForList("""SELECT id,tabla,registro_id,operacion,
        estado_anterior,estado_nuevo,fecha::text AS fecha,usuario_bd FROM auditoria_operativa ORDER BY id DESC LIMIT 500""")

}
