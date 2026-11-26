package com.diplomaticfleet.backend.operaciones

import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Formularios de los módulos operativos del panel administrativo.
 *
 * Todos los identificadores son claves existentes de PostgreSQL; los valores
 * de enumeración se comprueban antes de enviarlos a las restricciones CHECK.
 */
data class DocumentoNuevo(
    @field:NotNull val vehiculoId: Long,
    @field:NotBlank val tipoDocumento: String,
    @field:NotNull val fechaVencimiento: LocalDate,
    val numeroDocumento: String? = null,
    val fechaEmision: LocalDate? = null,
    val observaciones: String? = null,
)

/** Parámetros de programación preventiva o correctiva de una unidad. */
data class MantenimientoNuevo(
    @field:NotNull val vehiculoId: Long,
    @field:NotBlank val tipo: String,
    @field:NotBlank @field:Size(max = 500) val descripcion: String,
    @field:NotNull val fechaProgramada: LocalDate,
    val proveedor: String? = null,
    val costo: BigDecimal? = null,
)

/** Datos de identificación y vigencia de un conductor autorizado por su institución. */
data class ConductorNuevo(
    @field:NotNull val clienteInstitucionalId: Long,
    @field:NotBlank val nombreCompleto: String,
    @field:NotBlank val tipoDocumento: String,
    @field:NotBlank val numeroDocumento: String,
    @field:NotBlank val numeroLicencia: String,
    @field:NotNull val fechaVencimientoLicencia: LocalDate,
    val categoriaLicencia: String? = null,
    val telefono: String? = null,
    @field:Email val correo: String? = null,
)

/** Datos de una versión económica asociada a una solicitud. */
data class CotizacionNueva(
    @field:NotNull val solicitudId: Long,
    @field:NotNull val validaHasta: LocalDate,
    val observaciones: String? = null,
)

/** Datos que se pueden revisar antes de publicar una propuesta; la tarifa no se altera. */
data class CotizacionEdicion(
    @field:NotNull val validaHasta: LocalDate,
    @field:Size(max=1000) val observaciones: String? = null,
)

/** Transición solicitada; cada módulo valida sus estados permitidos. */
data class EstadoOperacion(@field:NotBlank val estado: String)
/** Suspensión o habilitación lógica sin eliminación de registros. */
data class Activacion(@field:NotNull val activo: Boolean)

/**
 * Implementa módulos previamente demostrativos utilizando las tablas V1-V3.
 *
 * El prefijo `/api/admin` obliga a poseer el rol ADMINISTRADOR según
 * SecurityConfig; no se basa en restricciones visuales del navegador.
 */
@RestController
@RequestMapping("/api/admin")
class OperacionAdminController(private val jdbc: JdbcTemplate) {

    /** Consulta los vehículos para completar selectores en los formularios. */
    private fun existeVehiculo(id: Long) = existe("SELECT COUNT(*) FROM vehiculos WHERE id=?", id)

    /** Ejecuta una consulta de existencia sin montar SQL desde el usuario. */
    private fun existe(sql: String, vararg args: Any): Boolean =
        (jdbc.queryForObject(sql, Long::class.java, *args) ?: 0L) > 0

    /** Lee los documentos y calcula su vigencia efectiva según la fecha actual. */
    @GetMapping("/documentos")
    fun documentos(): List<Map<String, Any?>> = jdbc.queryForList("""
        SELECT d.id,d.vehiculo_id,d.tipo_documento,d.numero_documento,
               d.fecha_emision::text AS fecha_emision,d.fecha_vencimiento::text AS fecha_vencimiento,d.observaciones,v.placa,
               CASE WHEN d.fecha_vencimiento < CURRENT_DATE THEN 'VENCIDO'
                    WHEN d.fecha_vencimiento <= CURRENT_DATE + 30 THEN 'PROXIMO_A_VENCER'
                    ELSE 'VIGENTE' END AS estado
        FROM documentos_vehiculo d JOIN vehiculos v ON v.id=d.vehiculo_id
        ORDER BY d.fecha_vencimiento ASC, d.id DESC
    """.trimIndent())

    /** Registra un documento; no permite fechas incoherentes ni tipos arbitrarios. */
    @PostMapping("/documentos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun crearDocumento(@Valid @RequestBody req: DocumentoNuevo): Map<String, Any?> {
        if (!existeVehiculo(req.vehiculoId)) throw RecursoNoEncontradoException("Vehículo inexistente")
        val tipos = setOf("SOAT", "REVISION_TECNOMECANICA", "TARJETA_PROPIEDAD", "POLIZA", "OTRO")
        if (req.tipoDocumento !in tipos) throw IllegalArgumentException("Tipo documental no reconocido")
        if (req.fechaEmision != null && req.fechaVencimiento.isBefore(req.fechaEmision))
            throw IllegalArgumentException("El vencimiento no puede preceder a la expedición")
        val estado = when {
            req.fechaVencimiento.isBefore(LocalDate.now()) -> "VENCIDO"
            !req.fechaVencimiento.isAfter(LocalDate.now().plusDays(30)) -> "PROXIMO_A_VENCER"
            else -> "VIGENTE"
        }
        val id = jdbc.queryForObject("""INSERT INTO documentos_vehiculo
          (vehiculo_id,tipo_documento,numero_documento,fecha_emision,fecha_vencimiento,estado,observaciones)
          VALUES (?,?,?,?,?,?,?) RETURNING id""", Long::class.java,
          req.vehiculoId,req.tipoDocumento,req.numeroDocumento?.trim(),req.fechaEmision,
          req.fechaVencimiento,estado,req.observaciones?.trim())!!
        return documentos().first { (it["id"] as Number).toLong() == id }
    }

    /** Edita una fecha de vencimiento y vuelve a calcular la vigencia del documento. */
    @PutMapping("/documentos/{id}")
    @Transactional
    fun editarDocumento(@PathVariable id: Long, @Valid @RequestBody req: DocumentoNuevo): Map<String, Any?> {
        if (!existe("SELECT COUNT(*) FROM documentos_vehiculo WHERE id=?", id))
            throw RecursoNoEncontradoException("Documento inexistente")
        if (!existeVehiculo(req.vehiculoId)) throw RecursoNoEncontradoException("Vehículo inexistente")
        if (req.tipoDocumento !in setOf("SOAT","REVISION_TECNOMECANICA","TARJETA_PROPIEDAD","POLIZA","OTRO"))
            throw IllegalArgumentException("Tipo documental inválido")
        if (req.fechaEmision != null && req.fechaVencimiento.isBefore(req.fechaEmision))
            throw IllegalArgumentException("Fecha de vencimiento anterior a la emisión")
        val estado = if (req.fechaVencimiento.isBefore(LocalDate.now())) "VENCIDO"
                     else if (req.fechaVencimiento <= LocalDate.now().plusDays(30)) "PROXIMO_A_VENCER" else "VIGENTE"
        jdbc.update("""UPDATE documentos_vehiculo SET vehiculo_id=?,tipo_documento=?,numero_documento=?,
            fecha_emision=?,fecha_vencimiento=?,estado=?,observaciones=?,actualizado_en=NOW() WHERE id=?""",
            req.vehiculoId,req.tipoDocumento,req.numeroDocumento?.trim(),req.fechaEmision,
            req.fechaVencimiento,estado,req.observaciones?.trim(),id)
        return documentos().first { (it["id"] as Number).toLong() == id }
    }

    /** Recupera programaciones reales y el vehículo asociado. */
    @GetMapping("/mantenimientos")
    fun mantenimientos(): List<Map<String, Any?>> = jdbc.queryForList("""
        SELECT m.id,m.vehiculo_id,v.placa,m.tipo,m.estado,m.descripcion,m.fecha_programada::text AS fecha_programada,
          m.fecha_inicio::text AS fecha_inicio,m.fecha_fin::text AS fecha_fin,m.costo,m.proveedor,m.observaciones
        FROM mantenimientos m JOIN vehiculos v ON v.id=m.vehiculo_id
        ORDER BY m.fecha_programada DESC,m.id DESC
    """.trimIndent())

    /** Programa un mantenimiento sin cambiar el estado del vehículo anticipadamente. */
    @PostMapping("/mantenimientos")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun crearMantenimiento(authentication: Authentication, @Valid @RequestBody req: MantenimientoNuevo): Map<String, Any?> {
        // Serializar programación con las nuevas solicitudes del vehículo.
        val activo=jdbc.query("SELECT activo FROM vehiculos WHERE id=? FOR UPDATE",{rs,_->rs.getBoolean(1)},req.vehiculoId).firstOrNull()
            ?: throw RecursoNoEncontradoException("Vehículo inexistente")
        if(!activo) throw ConflictoException("Vehículo inactivo")
        if(req.fechaProgramada.isBefore(LocalDate.now())) throw IllegalArgumentException("La programación no puede estar en el pasado")
        if(existe("""SELECT COUNT(*) FROM solicitudes WHERE vehiculo_id=? AND estado IN ('RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION','CONFIRMADA','ASIGNADA','EN_SERVICIO') AND (estado='EN_SERVICIO' OR ? BETWEEN CAST(fecha_inicio AT TIME ZONE 'America/Bogota' AS DATE) AND CAST(fecha_fin AT TIME ZONE 'America/Bogota' AS DATE))""",req.vehiculoId,req.fechaProgramada))
            throw ConflictoException("Existe una reserva incompatible con el mantenimiento")
        if (req.tipo !in setOf("PREVENTIVO","CORRECTIVO")) throw IllegalArgumentException("Tipo inválido")
        if (req.costo != null && req.costo < BigDecimal.ZERO) throw IllegalArgumentException("Costo inválido")
        val actor = jdbc.queryForObject("SELECT id FROM usuarios WHERE lower(correo)=lower(?)",Long::class.java,authentication.name)
        val proveedor = req.proveedor?.trim()?.ifBlank { null }
        if (proveedor != null && existe("SELECT COUNT(*) FROM taller WHERE nom_taller=? AND act_taller=false",proveedor)) throw ConflictoException("Taller inactivo")
        val id = jdbc.queryForObject("""INSERT INTO mantenimientos
           (vehiculo_id,tipo,descripcion,fecha_programada,proveedor,costo,usuario_responsable_id)
           VALUES(?,?,?,?,?,?,?) RETURNING id""",Long::class.java,
           req.vehiculoId,req.tipo,req.descripcion.trim(),req.fechaProgramada,proveedor,req.costo,actor)!!
        return mantenimientos().first { (it["id"] as Number).toLong() == id }
    }

    /**
     * Aplica las transiciones permitidas y bloquea vehículos mientras se reparan.
     * Una unidad no se libera si otro mantenimiento continúa EN_PROCESO.
     */
    @PatchMapping("/mantenimientos/{id}/estado")
    @Transactional
    fun estadoMantenimiento(@PathVariable id: Long, @RequestBody req: EstadoOperacion): Map<String, Any?> {
        val actual = jdbc.query("SELECT estado,vehiculo_id FROM mantenimientos WHERE id=? FOR UPDATE",
            { rs, _ -> Pair(rs.getString(1),rs.getLong(2)) },id).firstOrNull()
            ?: throw RecursoNoEncontradoException("Mantenimiento inexistente")
        val permitido = mapOf("PROGRAMADO" to setOf("EN_PROCESO","CANCELADO"),
            "EN_PROCESO" to setOf("FINALIZADO","CANCELADO"))
        if (req.estado !in (permitido[actual.first] ?: emptySet()))
            throw ConflictoException("Transición de mantenimiento no permitida")
        // El mismo bloqueo protege inicio y cierre de mantenimientos concurrentes.
        val activo=jdbc.query("SELECT activo FROM vehiculos WHERE id=? FOR UPDATE",{rs,_->rs.getBoolean(1)},actual.second).firstOrNull() ?: false
        if (req.estado == "EN_PROCESO") {
            if(!activo) throw ConflictoException("Vehículo inactivo")
            val vehicleState = jdbc.queryForObject("SELECT estado FROM vehiculos WHERE id=? FOR UPDATE",
                String::class.java,actual.second)
            if (vehicleState !in setOf("DISPONIBLE","EN_MANTENIMIENTO"))
                throw ConflictoException("El vehículo está ocupado o inactivo")
            val enUso = existe("""SELECT COUNT(*) FROM solicitudes WHERE vehiculo_id=?
              AND (estado='EN_SERVICIO' OR (estado IN ('CONFIRMADA','ASIGNADA') AND fecha_inicio <= NOW() AND fecha_fin > NOW()))""",actual.second)
            if (enUso) throw ConflictoException("No se puede intervenir un vehículo en servicio")
            jdbc.update("UPDATE vehiculos SET estado='EN_MANTENIMIENTO',actualizado_en=NOW() WHERE id=?",actual.second)
        }
        jdbc.update("""UPDATE mantenimientos SET estado=?,
            fecha_inicio=CASE WHEN ?='EN_PROCESO' THEN NOW() ELSE fecha_inicio END,
            fecha_fin=CASE WHEN ?='FINALIZADO' THEN NOW() ELSE fecha_fin END,
            actualizado_en=NOW() WHERE id=?""",req.estado,req.estado,req.estado,id)
        if (req.estado in setOf("FINALIZADO","CANCELADO") &&
            !existe("SELECT COUNT(*) FROM mantenimientos WHERE vehiculo_id=? AND estado='EN_PROCESO'",actual.second)) {
            jdbc.update("""UPDATE vehiculos SET estado='DISPONIBLE',actualizado_en=NOW()
                 WHERE id=? AND estado='EN_MANTENIMIENTO'""",actual.second)
        }
        return mantenimientos().first { (it["id"] as Number).toLong() == id }
    }

    /** Lista conductores autorizados y su acreditación de licencia vigente. */
    @GetMapping("/conductores")
    fun conductores(): List<Map<String, Any?>> = jdbc.queryForList("""
        SELECT c.id,c.cliente_institucional_id,ci.nombre_entidad,c.nombre_completo,c.tipo_documento,
            c.numero_documento,c.numero_licencia,c.categoria_licencia,c.fecha_vencimiento_licencia::text AS fecha_vencimiento_licencia,
            c.telefono,c.correo,c.activo,
            (c.activo AND c.fecha_vencimiento_licencia >= CURRENT_DATE) AS licencia_vigente
        FROM conductores_autorizados c JOIN clientes_institucionales ci
        ON ci.id=c.cliente_institucional_id ORDER BY c.id DESC
    """.trimIndent())

    /** Registra un conductor vinculado a una institución existente. */
    @PostMapping("/conductores")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun crearConductor(@Valid @RequestBody req: ConductorNuevo): Map<String, Any?> {
        if (!existe("SELECT COUNT(*) FROM clientes_institucionales WHERE id=? AND activo=true",req.clienteInstitucionalId))
            throw RecursoNoEncontradoException("Institución inexistente o inactiva")
        if (req.tipoDocumento !in setOf("CEDULA_CIUDADANIA","CEDULA_EXTRANJERIA","PASAPORTE","OTRO"))
            throw IllegalArgumentException("Tipo de documento inválido")
        if (req.fechaVencimientoLicencia.isBefore(LocalDate.now()))
            throw IllegalArgumentException("No se puede registrar una licencia vencida")
        val id = jdbc.queryForObject("""INSERT INTO conductores_autorizados
          (cliente_institucional_id,nombre_completo,tipo_documento,numero_documento,numero_licencia,
           categoria_licencia,fecha_vencimiento_licencia,telefono,correo)
           VALUES (?,?,?,?,?,?,?,?,?) RETURNING id""",Long::class.java,
           req.clienteInstitucionalId,req.nombreCompleto.trim(),req.tipoDocumento,
           req.numeroDocumento.trim(),req.numeroLicencia.trim(),req.categoriaLicencia?.trim()?.uppercase()?.ifBlank { null },
           req.fechaVencimientoLicencia,req.telefono?.trim(),req.correo?.trim())!!
        return conductores().first { (it["id"] as Number).toLong() == id }
    }

    /** Permite inactivar una autorización sin borrar el historial del conductor. */
    @PatchMapping("/conductores/{id}/activo")
    @Transactional
    fun activarConductor(@PathVariable id: Long, @RequestBody req: Activacion): Map<String, Any?> {
        if (req.activo && !existe("""SELECT COUNT(*) FROM conductores_autorizados
            WHERE id=? AND fecha_vencimiento_licencia >= CURRENT_DATE""",id))
            throw ConflictoException("No puede activarse una licencia vencida")
        val changed = jdbc.update("UPDATE conductores_autorizados SET activo=?,actualizado_en=NOW() WHERE id=?",req.activo,id)
        if (changed == 0) throw RecursoNoEncontradoException("Conductor inexistente")
        return conductores().first { (it["id"] as Number).toLong() == id }
    }

    /** Lista cotizaciones versionadas y los códigos de reserva asociados. */
    @GetMapping("/cotizaciones")
    fun cotizaciones(): List<Map<String, Any?>> = jdbc.queryForList("""
        SELECT c.id,c.solicitud_id,s.codigo,c.numero_version,c.estado,c.valida_hasta::text AS valida_hasta,
          c.subtotal,c.impuestos,c.descuentos,c.valor_total,c.observaciones,c.fecha_emision::text AS fecha_emision
        FROM cotizaciones c JOIN solicitudes s ON s.id=c.solicitud_id
        ORDER BY c.id DESC
    """.trimIndent())

    /** Crea una propuesta académica con tarifa estimada y sin impuestos asumidos. */
    @PostMapping("/cotizaciones")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun cotizar(authentication: Authentication, @Valid @RequestBody req: CotizacionNueva): Map<String, Any?> {
        if (req.validaHasta.isBefore(LocalDate.now())) throw IllegalArgumentException("La vigencia no puede estar vencida")
        val cuenta = jdbc.queryForObject("SELECT id FROM usuarios WHERE LOWER(correo)=LOWER(?)",
            Long::class.java,authentication.name) ?: throw RecursoNoEncontradoException("Usuario inexistente")
        val importe = jdbc.query("SELECT valor_estimado,estado FROM solicitudes WHERE id=? FOR UPDATE",
            { rs, _ -> Pair(rs.getBigDecimal(1),rs.getString(2)) },req.solicitudId).firstOrNull()
            ?: throw RecursoNoEncontradoException("Solicitud inexistente")
        if (importe.second !in setOf("RECIBIDA","EN_REVISION","COTIZADA","PENDIENTE_APROBACION"))
            throw ConflictoException("Solo se cotizan solicitudes pendientes de confirmacion")
        if(existe("SELECT COUNT(*) FROM pagos_reserva WHERE solicitud_id=? AND estado NOT IN ('PENDIENTE','RECHAZADO')",req.solicitudId))
            throw ConflictoException("Resuelva primero el pago existente antes de generar otra propuesta")
        jdbc.update("UPDATE cotizaciones SET estado='VENCIDA',actualizado_en=NOW() WHERE solicitud_id=? AND estado IN ('BORRADOR','ENVIADA') AND valida_hasta<CURRENT_DATE",req.solicitudId)
        if(existe("SELECT COUNT(*) FROM cotizaciones WHERE solicitud_id=? AND estado IN ('BORRADOR','ENVIADA','ACEPTADA')",req.solicitudId))
            throw ConflictoException("Ya existe una propuesta activa para esta solicitud")
        val version = (jdbc.queryForObject("""SELECT COALESCE(MAX(numero_version),0)+1
            FROM cotizaciones WHERE solicitud_id=?""",Int::class.java,req.solicitudId) ?: 1)
        val id = jdbc.queryForObject("""INSERT INTO cotizaciones
            (solicitud_id,numero_version,valida_hasta,subtotal,valor_total,creado_por_usuario_id,observaciones)
            VALUES (?,?,?,?,?,?,?) RETURNING id""",Long::class.java,req.solicitudId,version,
            req.validaHasta,importe.first,importe.first,cuenta,req.observaciones?.trim())!!
        jdbc.update("""INSERT INTO detalles_cotizacion(cotizacion_id,concepto,cantidad,valor_unitario)
            VALUES (?,'Alquiler del periodo solicitado',1,?)""",id,importe.first)
        jdbc.update("UPDATE solicitudes SET estado='COTIZADA',actualizado_en=NOW() WHERE id=?",req.solicitudId)
        return cotizaciones().first { (it["id"] as Number).toLong() == id }
    }

    /** Ajusta un borrador antes de enviarlo, manteniendo intacto el importe calculado. */
    @PatchMapping("/cotizaciones/{id}/borrador")
    @Transactional
    fun editarCotizacion(@PathVariable id: Long,@Valid @RequestBody req: CotizacionEdicion): Map<String, Any?> {
        if (req.validaHasta.isBefore(LocalDate.now())) throw IllegalArgumentException("La vigencia no puede estar vencida")
        val solicitud = jdbc.query("SELECT s.estado FROM solicitudes s JOIN cotizaciones c ON c.solicitud_id=s.id WHERE c.id=? FOR UPDATE OF s", { rs,_ -> rs.getString(1) },id).firstOrNull()
            ?: throw RecursoNoEncontradoException("Cotización inexistente")
        if(solicitud !in setOf("RECIBIDA","EN_REVISION","COTIZADA","PENDIENTE_APROBACION"))
            throw ConflictoException("La solicitud ya no admite propuestas")
        val cambio=jdbc.update("""UPDATE cotizaciones SET valida_hasta=?,observaciones=?,actualizado_en=NOW()
            WHERE id=? AND estado='BORRADOR'""",req.validaHasta,req.observaciones?.trim(),id)
        if(cambio!=1) throw ConflictoException("Solo se puede editar un borrador")
        return cotizaciones().first { (it["id"] as Number).toLong() == id }
    }

    /** Registra la decisión comercial sin realizar cobros. */
    @PatchMapping("/cotizaciones/{id}/estado")
    @Transactional
    fun estadoCotizacion(@PathVariable id: Long,@RequestBody req: EstadoOperacion): Map<String, Any?> {
        val solicitud=jdbc.query("SELECT s.id,s.estado FROM solicitudes s JOIN cotizaciones c ON c.solicitud_id=s.id WHERE c.id=? FOR UPDATE OF s",{rs,_->Pair(rs.getLong(1),rs.getString(2))},id).firstOrNull()
            ?: throw RecursoNoEncontradoException("Cotización inexistente")
        if(req.estado=="ENVIADA" && solicitud.second !in setOf("RECIBIDA","EN_REVISION","COTIZADA","PENDIENTE_APROBACION"))
            throw ConflictoException("La solicitud no permite enviar propuestas")
        val r = jdbc.query("SELECT estado,valida_hasta FROM cotizaciones WHERE id=? FOR UPDATE",
            { rs, _ -> Pair(rs.getString(1),rs.getDate(2).toLocalDate()) },id).firstOrNull()
            ?: throw RecursoNoEncontradoException("Cotización inexistente")
        val siguientes = mapOf("BORRADOR" to setOf("ENVIADA","ANULADA"),
            "ENVIADA" to setOf("ANULADA"))
        if (req.estado !in (siguientes[r.first] ?: emptySet()))
            throw ConflictoException("Transición de cotización no permitida")
        if (req.estado == "ENVIADA" && r.second.isBefore(LocalDate.now()))
            throw ConflictoException("La cotización está vencida")
        jdbc.update("UPDATE cotizaciones SET estado=?,actualizado_en=NOW() WHERE id=?",req.estado,id)
        if(req.estado=="ENVIADA") jdbc.update("UPDATE solicitudes SET estado='PENDIENTE_APROBACION',actualizado_en=NOW() WHERE id=?",solicitud.first)
        if(req.estado=="ANULADA" && solicitud.second in setOf("COTIZADA","PENDIENTE_APROBACION"))
            jdbc.update("UPDATE solicitudes SET estado='EN_REVISION',actualizado_en=NOW() WHERE id=?",solicitud.first)
        return cotizaciones().first { (it["id"] as Number).toLong() == id }
    }

    /** Consulta los perfiles y permisos sin revelar hashes ni contraseñas. */
    @GetMapping("/usuarios")
    fun usuarios(): List<Map<String, Any?>> = jdbc.queryForList("""
        SELECT u.id,u.nombre,u.apellido,u.correo,u.activo,u.creado_en,
               COALESCE(string_agg(r.nombre,', ' ORDER BY r.nombre),'SIN_ROL') AS roles
        FROM usuarios u LEFT JOIN usuarios_roles ur ON ur.usuario_id=u.id
        LEFT JOIN roles r ON r.id=ur.rol_id
        GROUP BY u.id ORDER BY u.id DESC
    """.trimIndent())

    /** Inactiva cuentas, excepto el propio usuario que administra la sesión. */
    @PatchMapping("/usuarios/{id}/activo")
    @Transactional
    fun activarUsuario(authentication: Authentication,@PathVariable id: Long,@RequestBody req: Activacion): Map<String, Any?> {
        val propio = jdbc.queryForObject("SELECT id FROM usuarios WHERE LOWER(correo)=LOWER(?)",
            Long::class.java,authentication.name) ?: throw RecursoNoEncontradoException("Cuenta administrativa inexistente")
        if (propio == id && !req.activo) throw ConflictoException("No se permite desactivar la cuenta de la sesion actual")
        if (!req.activo) {
            // La fila del rol serializa desactivaciones administrativas concurrentes.
            jdbc.query("SELECT id FROM roles WHERE nombre='ADMINISTRADOR' FOR UPDATE",{rs,_ -> rs.getLong(1)})
            val esAdmin = existe("SELECT COUNT(*) FROM usuarios_roles ur JOIN roles r ON r.id=ur.rol_id WHERE ur.usuario_id=? AND r.nombre='ADMINISTRADOR' AND r.activo=true",id)
            if (esAdmin && !existe("SELECT COUNT(*) FROM usuarios u JOIN usuarios_roles ur ON ur.usuario_id=u.id JOIN roles r ON r.id=ur.rol_id WHERE u.activo=true AND r.activo=true AND r.nombre='ADMINISTRADOR' AND u.id<>?",id))
                throw ConflictoException("Debe conservarse al menos un administrador activo")
            // Una cuenta autenticada que fue suspendida durante otra petición no administra usuarios.
            if (!existe("SELECT COUNT(*) FROM usuarios WHERE id=? AND activo=true",propio))
                throw ConflictoException("La cuenta administrativa ya no se encuentra activa")
        }
        if (jdbc.update("UPDATE usuarios SET activo=?,actualizado_en=NOW() WHERE id=?",req.activo,id)==0)
            throw RecursoNoEncontradoException("Cuenta inexistente")
        return usuarios().first { (it["id"] as Number).toLong() == id }
    }

    /** Calcula métricas operativas directamente en PostgreSQL, sin datos ficticios. */
    @GetMapping("/reportes")
    fun reportes(): Map<String, Any?> = mapOf(
        "vehiculos" to conteo("SELECT COUNT(*) FROM vehiculos WHERE activo=true"),
        "vehiculosTotales" to conteo("SELECT COUNT(*) FROM vehiculos"),
        "clientes" to conteo("SELECT COUNT(*) FROM clientes_institucionales WHERE activo=true"),
        "clientesTotales" to conteo("SELECT COUNT(*) FROM clientes_institucionales"),
        "reservas" to conteo("SELECT COUNT(*) FROM solicitudes"),
        "pendientes" to conteo("SELECT COUNT(*) FROM solicitudes WHERE estado IN ('RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION')"),
        "mantenimientos" to conteo("SELECT COUNT(*) FROM mantenimientos WHERE estado='EN_PROCESO'"),
        "mantenimientosProgramados" to conteo("SELECT COUNT(*) FROM mantenimientos WHERE estado='PROGRAMADO'"),
        "mantenimientosTotales" to conteo("SELECT COUNT(*) FROM mantenimientos"),
        "reservasFinalizadas" to conteo("SELECT COUNT(*) FROM solicitudes WHERE estado='FINALIZADA'"),
        "reservasCanceladas" to conteo("SELECT COUNT(*) FROM solicitudes WHERE estado='CANCELADA'"),
        "pagosVerificados" to conteo("SELECT COUNT(*) FROM pagos_reserva WHERE estado='VERIFICADO'"),
        "documentosVencidos" to conteo("SELECT COUNT(*) FROM documentos_vehiculo WHERE fecha_vencimiento < CURRENT_DATE"),
        "cotizaciones" to conteo("SELECT COUNT(*) FROM cotizaciones"),
        "valorEstimadoConfirmado" to (jdbc.queryForObject("""SELECT COALESCE(SUM(valor_estimado),0)
            FROM solicitudes WHERE estado IN ('CONFIRMADA','ASIGNADA','EN_SERVICIO','FINALIZADA')""",BigDecimal::class.java) ?: BigDecimal.ZERO),
    )

    /** Función compartida para conteos de consultas fijas y parametrizadas. */
    private fun conteo(sql: String): Long = jdbc.queryForObject(sql,Long::class.java) ?: 0L
}
