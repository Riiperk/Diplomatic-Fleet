package com.diplomaticfleet.backend.reservas

import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import com.diplomaticfleet.backend.seguridad.UsuarioAuth
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/** Ficha publica: los datos internos como placa o propietario no son expuestos. */
data class CochePublico(
    val id: Long, val marca: String, val modelo: String, val anio: Int,
    val categoria: String, val pasajeros: Int, val equipaje: Int,
    val tarifaDiaria: BigDecimal, val imagenUrl: String?, val nivelBlindaje: String,
)

/** Una solicitud elige metodo de pago EXTERNO. No se reciben tarjetas ni se debita dinero. */
data class ReservaRequest(
    val vehiculoId: Long,
    val inicio: OffsetDateTime,
    val fin: OffsetDateTime,
    @field:NotBlank @field:jakarta.validation.constraints.Size(max=255) val recogida: String,
    @field:NotBlank @field:jakarta.validation.constraints.Size(max=255) val devolucion: String,
    @field:Min(1) val pasajeros: Int,
    @field:jakarta.validation.constraints.Size(max=1000) val observaciones: String? = null,
    @field:NotBlank val metodoPago: String = "TRANSFERENCIA",
    val modalidad: String = "SIN_CONDUCTOR",
    val requiereBlindaje: Boolean = false,
)

/** Respuesta conjunta del alquiler, pago y fechas reales para ambas vistas. */
data class ReservaResponse(
    val id: Long, val codigo: String, val vehiculoId: Long?, val vehiculo: String?,
    val cliente: String, val inicio: OffsetDateTime, val fin: OffsetDateTime,
    val recogida: String, val devolucion: String, val pasajeros: Int,
    val estado: String, val valorEstimado: BigDecimal,
    val metodoPago: String?, val estadoPago: String?, val referenciaPago: String?,
    val fechaInicioReal: OffsetDateTime?, val fechaFinReal: OffsetDateTime?,
    val motivoCierreAnticipado: String?,
    val modalidad: String, val conductorProfesionalId: Long?, val conductor: String?,
    val motivoInicioAnticipado: String?,
    val editable: Boolean,
    val requiereBlindaje: Boolean,
)

/** Los cambios administrativos solo aceptan estados predefinidos. */
data class EstadoRequest(@field:NotBlank val estado: String, @field:jakarta.validation.constraints.Size(max=500) val motivoCierre: String? = null, @field:jakarta.validation.constraints.Size(max=500) val motivoInicio: String? = null)

/** Nuevo periodo solicitado por un cliente antes de cotizar o reportar el pago. */
data class ReprogramarRequest(val inicio: OffsetDateTime, val fin: OffsetDateTime)

/** Medio de pago externo para regularizar una reserva antigua. */
data class MedioPagoRequest(@field:NotBlank val metodo: String)

/** Motivo obligatorio para dejar constancia de la cancelacion. */
data class CancelarRequest(@field:NotBlank @field:jakarta.validation.constraints.Size(max=500) val motivo: String)

/** El cliente declara la referencia de la transferencia (no demuestra cobro). */
data class ReportarPagoRequest(@field:NotBlank val referencia: String)

/** Solo administracion confirma recepcion de dinero o marca un reembolso manual. */
data class EstadoPagoRequest(@field:NotBlank val estado: String, @field:jakarta.validation.constraints.Size(max=500) val observacion: String? = null)

/**
 * API de solicitudes de alquiler de Diplomatic Fleet.
 *
 * - Disponibilidad y cotizacion en PostgreSQL, transaccional al crear.
 * - Separacion de reserva y pago (no se realiza cobro online).
 * - Cancelacion antes de iniciar el servicio, con trazabilidad.
 * - Inicio y devolucion reales, incluso si se entrega anticipadamente.
 */
@RestController
class ReservaController(private val jdbc: JdbcTemplate, private val auth: UsuarioAuth) {
    private val ocupados = "('RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION','CONFIRMADA','ASIGNADA','EN_SERVICIO')"
    private val metodos = setOf("TRANSFERENCIA", "EFECTIVO", "TARJETA_PRESENCIAL")
    /** Zona comercial: los mantenimientos se programan por fecha colombiana. */
    private val zonaOperacion = java.time.ZoneId.of("America/Bogota")

    /** Consulta flota libre para el intervalo; excluye mantenimiento e intervalos solapados. */
    @GetMapping("/api/public/vehiculos")
    fun catalogo(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) inicio: OffsetDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) fin: OffsetDateTime?,
    ): List<CochePublico> {
        if ((inicio == null) != (fin == null)) throw IllegalArgumentException("Indique ambas fechas")
        if (inicio != null && fin != null && !fin.isAfter(inicio)) throw IllegalArgumentException("Periodo invalido")
        val where = if (inicio != null) """AND NOT EXISTS (SELECT 1 FROM solicitudes s WHERE
          s.vehiculo_id=v.id AND s.estado IN $ocupados AND (s.estado='EN_SERVICIO' OR (s.fecha_inicio < ? AND s.fecha_fin > ?)))
          AND NOT EXISTS (SELECT 1 FROM mantenimientos m WHERE m.vehiculo_id=v.id
          AND m.estado='PROGRAMADO' AND m.fecha_programada BETWEEN ? AND ?)""" else ""
        val sql = """SELECT v.id,v.marca,v.modelo,v.anio,c.nombre AS categoria,
           v.capacidad_pasajeros,v.capacidad_equipaje,v.tarifa_diaria,v.imagen_url,v.nivel_blindaje
           FROM vehiculos v JOIN categorias_vehiculo c ON c.id=v.categoria_id
           WHERE v.activo=true AND v.estado='DISPONIBLE' AND v.tarifa_diaria IS NOT NULL
           AND NOT EXISTS (SELECT 1 FROM mantenimientos m
             WHERE m.vehiculo_id=v.id AND m.estado='EN_PROCESO')
           AND NOT EXISTS (SELECT 1 FROM solicitudes s WHERE s.vehiculo_id=v.id AND s.estado='EN_SERVICIO')
           $where ORDER BY v.tarifa_diaria ASC"""
        val mapper = org.springframework.jdbc.core.RowMapper<CochePublico> { r, _ ->
            CochePublico(r.getLong("id"), r.getString("marca"), r.getString("modelo"), r.getInt("anio"),
                r.getString("categoria"), r.getInt("capacidad_pasajeros"), r.getInt("capacidad_equipaje"),
                r.getBigDecimal("tarifa_diaria"), r.getString("imagen_url"), r.getString("nivel_blindaje"))
        }
        return if (inicio != null && fin != null)
            jdbc.query(sql, mapper, fin, inicio, inicio.atZoneSameInstant(zonaOperacion).toLocalDate(), fin.atZoneSameInstant(zonaOperacion).toLocalDate())
        else jdbc.query(sql, mapper)
    }

    /** Devuelve todos los detalles visibles, incluido el estado manual del pago. */
    private val reservaSelect = """SELECT s.id,s.codigo,s.vehiculo_id,
         CONCAT(v.marca,' ',v.modelo) AS vehiculo,ci.nombre_entidad AS cliente,
         s.fecha_inicio,s.fecha_fin,s.lugar_recogida,s.lugar_devolucion,
         s.cantidad_pasajeros,s.estado,s.valor_estimado,
         p.metodo AS metodo_pago,p.estado AS estado_pago,p.referencia AS referencia_pago,
         s.fecha_inicio_real,s.fecha_fin_real,s.motivo_cierre_anticipado,s.modalidad,s.conductor_profesional_id,s.motivo_inicio_anticipado,s.requiere_blindaje,
         CONCAT_WS(' ',cu.nombre,cu.apellido) AS conductor,
         (s.estado IN ('RECIBIDA','EN_REVISION') AND s.fecha_inicio>NOW() AND s.vehiculo_id IS NOT NULL
          AND COALESCE(p.estado='PENDIENTE',false) AND NOT EXISTS
            (SELECT 1 FROM cotizaciones c WHERE c.solicitud_id=s.id)) AS editable
         FROM solicitudes s JOIN clientes_institucionales ci ON ci.id=s.cliente_institucional_id
         LEFT JOIN vehiculos v ON v.id=s.vehiculo_id
         LEFT JOIN pagos_reserva p ON p.solicitud_id=s.id
         LEFT JOIN conductores_profesionales cp ON cp.id=s.conductor_profesional_id
         LEFT JOIN usuarios cu ON cu.id=cp.usuario_id"""

    private val reservaMapper = org.springframework.jdbc.core.RowMapper<ReservaResponse> { r, _ ->
        val vehiculoId = r.getLong("vehiculo_id").let { if(r.wasNull()) null else it }
        ReservaResponse(r.getLong("id"), r.getString("codigo"), vehiculoId,
            r.getString("vehiculo"), r.getString("cliente"),
            r.getObject("fecha_inicio", OffsetDateTime::class.java), r.getObject("fecha_fin", OffsetDateTime::class.java),
            r.getString("lugar_recogida"), r.getString("lugar_devolucion"), r.getInt("cantidad_pasajeros"),
            r.getString("estado"), r.getBigDecimal("valor_estimado"),
            r.getString("metodo_pago"), r.getString("estado_pago"), r.getString("referencia_pago"),
            r.getObject("fecha_inicio_real", OffsetDateTime::class.java),
            r.getObject("fecha_fin_real", OffsetDateTime::class.java), r.getString("motivo_cierre_anticipado"),
            r.getString("modalidad"),r.getLong("conductor_profesional_id").let { if(r.wasNull()) null else it },r.getString("conductor").takeIf { it.isNotBlank() },r.getString("motivo_inicio_anticipado"),r.getBoolean("editable"),r.getBoolean("requiere_blindaje"))
    }

    /** Un cliente solo puede leer solicitudes asociadas a su propia institucion. */
    @GetMapping("/api/solicitudes/mias")
    fun mias(authentication: Authentication): List<ReservaResponse> {
        val cliente = auth.perfil(authentication.name).clienteInstitucionalId ?: return emptyList()
        return jdbc.query("$reservaSelect WHERE s.cliente_institucional_id=? ORDER BY s.creado_en DESC", reservaMapper, cliente)
    }

    /** Administracion consulta todas las solicitudes. */
    @GetMapping("/api/admin/solicitudes")
    fun todas(): List<ReservaResponse> = jdbc.query("$reservaSelect ORDER BY s.creado_en DESC", reservaMapper)

    /** Bloquea el vehiculo en la transaccion y comprueba que no exista solapamiento. */
    @PostMapping("/api/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun crear(authentication: Authentication, @Valid @RequestBody req: ReservaRequest): ReservaResponse {
        val perfil = auth.perfil(authentication.name)
        val cliente = perfil.clienteInstitucionalId ?: throw ConflictoException("Debe ingresar con cuenta institucional")
        val metodo = req.metodoPago.trim().uppercase()
        if (metodo !in metodos) throw IllegalArgumentException("Metodo de pago no reconocido")
        if (req.modalidad !in setOf("SIN_CONDUCTOR","CON_CONDUCTOR")) throw IllegalArgumentException("Modalidad no reconocida")
        ReglasAlquiler.validarPeriodo(req.inicio, req.fin)
        if (req.recogida.trim().length < 4 || req.devolucion.trim().length < 4)
            throw IllegalArgumentException("Complete recogida y devolucion")
        if ((jdbc.queryForObject("SELECT COUNT(*) FROM ubicacion WHERE nom_ubicacion IN (?,?) AND act_ubicacion=false",Long::class.java,req.recogida.trim(),req.devolucion.trim()) ?: 0L)>0L)
            throw ConflictoException("La ubicación seleccionada está inactiva")
        val vehiculos = jdbc.query("""SELECT v.id,v.tarifa_diaria,v.capacidad_pasajeros,v.estado,v.activo,
           ci.activo AS cliente_activo FROM vehiculos v CROSS JOIN clientes_institucionales ci
           WHERE v.id=? AND ci.id=? AND v.tarifa_diaria IS NOT NULL FOR UPDATE OF v""", { r, _ ->
            VehicleInfo(r.getBigDecimal("tarifa_diaria"), r.getInt("capacidad_pasajeros"),
                r.getString("estado"), r.getBoolean("activo"), r.getBoolean("cliente_activo"))
        }, req.vehiculoId, cliente)
        val vehiculo = vehiculos.firstOrNull() ?: throw RecursoNoEncontradoException("Vehiculo o cliente no disponible")
        if (!vehiculo.activo || !vehiculo.clienteActivo || vehiculo.estado != "DISPONIBLE")
            throw ConflictoException("Vehiculo o institucion inactivos")
        // La unidad ya está bloqueada por esta transacción; el nivel se consulta en el servidor.
        if (req.requiereBlindaje && jdbc.queryForObject("SELECT nivel_blindaje FROM vehiculos WHERE id=?",String::class.java,req.vehiculoId) in setOf(null,"NO_DECLARADO","SIN_BLINDAJE"))
            throw ConflictoException("El vehículo no tiene blindaje declarado")
        if (req.pasajeros !in 1..vehiculo.pasajeros) throw IllegalArgumentException("Se excede la capacidad")
        val conflictos = jdbc.queryForObject("""SELECT COUNT(*) FROM solicitudes WHERE vehiculo_id=?
          AND estado IN $ocupados AND (estado='EN_SERVICIO' OR (fecha_inicio < ? AND fecha_fin > ?))""", Long::class.java,
            req.vehiculoId, req.fin, req.inicio) ?: 0L
        if (conflictos > 0) throw ConflictoException("El vehiculo ya tiene reserva para ese intervalo")
        if (vehiculo.tarifa < BigDecimal.ZERO) throw ConflictoException("Tarifa invalida")
        val mantenimiento = jdbc.queryForObject("""SELECT COUNT(*) FROM mantenimientos WHERE vehiculo_id=?
           AND (estado='EN_PROCESO' OR (estado='PROGRAMADO' AND fecha_programada BETWEEN ? AND ?))""",
            Long::class.java, req.vehiculoId, req.inicio.atZoneSameInstant(zonaOperacion).toLocalDate(), req.fin.atZoneSameInstant(zonaOperacion).toLocalDate()) ?: 0L
        if (mantenimiento > 0) throw ConflictoException("Mantenimiento incompatible con las fechas")
        val importe = ReglasAlquiler.valorPeriodo(vehiculo.tarifa, req.inicio, req.fin)
        val codigo = "DF-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val id = jdbc.queryForObject("""INSERT INTO solicitudes
          (codigo,cliente_institucional_id,usuario_creador_id,vehiculo_id,modalidad,fecha_inicio,
          fecha_fin,lugar_recogida,lugar_devolucion,cantidad_pasajeros,necesidades_especiales,estado,valor_estimado,requiere_blindaje)
          VALUES(?,?,?,?,?,?,?,?,?,?,?,'RECIBIDA',?,?) RETURNING id""", Long::class.java,
            codigo, cliente, perfil.id, req.vehiculoId, req.modalidad, req.inicio, req.fin, req.recogida.trim(),
            req.devolucion.trim(), req.pasajeros, req.observaciones?.trim(), importe,req.requiereBlindaje)!!
        // El estado PENDIENTE no indica un cobro: solo registra la preferencia del cliente.
        jdbc.update("INSERT INTO pagos_reserva(solicitud_id,metodo) VALUES(?,?)", id, metodo)
        return buscar(id)
    }

    /**
     * Reprograma una solicitud propia aún pendiente, sin cotización ni pago reportado.
     * Bloquea la solicitud y la unidad para comprobar de nuevo el calendario y el
     * valor estimado. No modifica contratos confirmados, pagos ni cotizaciones.
     * @param id identificador de la solicitud institucional.
     * @param authentication identidad confirmada por Spring Security.
     * @param req nuevas fechas de recogida y devolución.
     * @return proyección de la solicitud actualizada.
     */
    @PatchMapping("/api/solicitudes/{id}/periodo")
    @Transactional
    fun reprogramar(@PathVariable id: Long, authentication: Authentication, @RequestBody req: ReprogramarRequest): ReservaResponse {
        val perfil = auth.perfil(authentication.name)
        if (perfil.rol != "CLIENTE" || perfil.clienteInstitucionalId == null) throw AccessDeniedException("Solo el cliente institucional puede reprogramar")
        ReglasAlquiler.validarPeriodo(req.inicio, req.fin)
        val actual = jdbc.query("""SELECT s.vehiculo_id,s.cliente_institucional_id,s.estado,s.fecha_inicio,p.estado,ci.activo
            FROM solicitudes s JOIN clientes_institucionales ci ON ci.id=s.cliente_institucional_id
            LEFT JOIN pagos_reserva p ON p.solicitud_id=s.id WHERE s.id=? FOR UPDATE OF s""", { rs,_ ->
            ReprogramacionInfo(rs.getLong(1).let { if(rs.wasNull()) null else it },rs.getLong(2),rs.getString(3),rs.getObject(4,OffsetDateTime::class.java),rs.getString(5),rs.getBoolean(6))
        },id).firstOrNull() ?: throw RecursoNoEncontradoException("Solicitud inexistente")
        if (actual.cliente != perfil.clienteInstitucionalId) throw AccessDeniedException("Solicitud ajena")
        if (!actual.clienteActivo || actual.estado !in setOf("RECIBIDA","EN_REVISION") || !actual.inicio.isAfter(OffsetDateTime.now()) || actual.pago != "PENDIENTE")
            throw ConflictoException("Solo se modifican solicitudes pendientes, antes de la recogida y sin pago reportado")
        if ((jdbc.queryForObject("SELECT COUNT(*) FROM cotizaciones WHERE solicitud_id=?",Long::class.java,id) ?: 0L)>0)
            throw ConflictoException("La solicitud ya tiene una cotizacion; contacte a administracion")
        val vehiculoId = actual.vehiculo ?: throw ConflictoException("Solicitud sin vehiculo")
        val tarifa = jdbc.query("SELECT tarifa_diaria FROM vehiculos WHERE id=? AND activo=true AND estado='DISPONIBLE' AND tarifa_diaria IS NOT NULL FOR UPDATE",{ rs,_ -> rs.getBigDecimal(1) },vehiculoId).firstOrNull()
            ?: throw ConflictoException("Vehiculo no disponible")
        val conflictos = jdbc.queryForObject("""SELECT COUNT(*) FROM solicitudes WHERE vehiculo_id=? AND id<>?
            AND estado IN $ocupados AND (estado='EN_SERVICIO' OR (fecha_inicio<? AND fecha_fin>?))""",Long::class.java,vehiculoId,id,req.fin,req.inicio) ?: 0L
        if (conflictos>0) throw ConflictoException("El vehiculo ya tiene reserva para ese intervalo")
        val mantenimiento = jdbc.queryForObject("""SELECT COUNT(*) FROM mantenimientos WHERE vehiculo_id=?
            AND (estado='EN_PROCESO' OR (estado='PROGRAMADO' AND fecha_programada BETWEEN ? AND ?))""",Long::class.java,vehiculoId,req.inicio.atZoneSameInstant(zonaOperacion).toLocalDate(),req.fin.atZoneSameInstant(zonaOperacion).toLocalDate()) ?: 0L
        if (mantenimiento>0) throw ConflictoException("Mantenimiento incompatible con las fechas")
        jdbc.update("UPDATE solicitudes SET fecha_inicio=?,fecha_fin=?,valor_estimado=?,actualizado_en=NOW() WHERE id=?",req.inicio,req.fin,ReglasAlquiler.valorPeriodo(tarifa,req.inicio,req.fin),id)
        return buscar(id)
    }

    /** Proyección de una solicitud bloqueada durante la reprogramación. */
    private data class ReprogramacionInfo(val vehiculo:Long?,val cliente:Long,val estado:String,val inicio:OffsetDateTime,val pago:String?,val clienteActivo:Boolean)

    /**
     * Un cliente cancela antes de la fecha de recogida; administracion puede cancelar
     * una reserva confirmada que no haya pasado a EN_SERVICIO, incluso si vencio la fecha.
     */
    @PostMapping("/api/solicitudes/{id}/cancelar")
    @Transactional
    fun cancelar(@PathVariable id: Long, authentication: Authentication, @Valid @RequestBody req: CancelarRequest): ReservaResponse {
        if (req.motivo.trim().length < 4) throw IllegalArgumentException("Indique motivo (minimo 4 caracteres)")
        val perfil = auth.perfil(authentication.name)
        val datos = jdbc.query("""SELECT cliente_institucional_id,estado,fecha_inicio FROM solicitudes
          WHERE id=? FOR UPDATE""", { r, _ ->
            Triple(r.getLong(1), r.getString(2), r.getObject(3, OffsetDateTime::class.java))
        }, id).firstOrNull() ?: throw RecursoNoEncontradoException("Solicitud inexistente")
        val admin = perfil.rol == ADMIN_ROLE
        if (!admin && datos.first != perfil.clienteInstitucionalId) throw AccessDeniedException("Sin acceso")
        if (datos.second !in setOf("RECIBIDA", "EN_REVISION", "COTIZADA", "PENDIENTE_APROBACION", "CONFIRMADA", "ASIGNADA"))
            throw ConflictoException("No se puede cancelar una reserva iniciada o ya cerrada")
        if (!admin && !datos.third.isAfter(OffsetDateTime.now()))
            throw ConflictoException("La cancelacion del cliente debe hacerse antes de la recogida; contacte a administracion")
        jdbc.update("UPDATE solicitudes SET estado='CANCELADA',motivo_cancelacion=?,actualizado_en=NOW() WHERE id=?",
            req.motivo.trim(), id)
        jdbc.update("UPDATE cotizaciones SET estado='ANULADA',actualizado_en=NOW() WHERE solicitud_id=? AND estado IN ('BORRADOR','ENVIADA')",id)
        // Un pago verificado requiere gestion humana de devolucion; no se reembolsa automaticamente.
        jdbc.update("""UPDATE pagos_reserva SET estado='REEMBOLSO_PENDIENTE',actualizado_en=NOW()
            WHERE solicitud_id=? AND estado='VERIFICADO'""", id)
        return buscar(id)
    }

    /** Inicializa el medio de pago de reservas anteriores a V9, sin registrar un ingreso. */
    @PostMapping("/api/admin/solicitudes/{id}/pago/inicializar")
    @Transactional
    fun inicializarPago(@PathVariable id: Long, @Valid @RequestBody req: MedioPagoRequest): ReservaResponse {
        val metodo=req.metodo.trim().uppercase()
        if(metodo !in metodos) throw IllegalArgumentException("Metodo de pago no reconocido")
        val estado=jdbc.query("SELECT estado FROM solicitudes WHERE id=? FOR UPDATE",{rs,_ -> rs.getString(1)},id).firstOrNull()
            ?: throw RecursoNoEncontradoException("Reserva inexistente")
        if(estado !in setOf("RECIBIDA","EN_REVISION","COTIZADA","PENDIENTE_APROBACION","CONFIRMADA","ASIGNADA"))
            throw ConflictoException("Reserva no elegible para inicializar pago")
        if(jdbc.update("INSERT INTO pagos_reserva(solicitud_id,metodo) VALUES(?,?) ON CONFLICT(solicitud_id) DO NOTHING",id,metodo)==0)
            throw ConflictoException("Ya existe un pago registrado")
        return buscar(id)
    }

    /** El cliente informa referencia de transferencia; administracion debe validarla. */
    @PostMapping("/api/solicitudes/{id}/pago/reportar")
    @Transactional
    fun reportarPago(@PathVariable id: Long, authentication: Authentication,
                     @Valid @RequestBody req: ReportarPagoRequest): ReservaResponse {
        val perfil = auth.perfil(authentication.name)
        val filas = jdbc.query("""SELECT s.cliente_institucional_id,s.estado,p.metodo,p.estado
            FROM solicitudes s JOIN pagos_reserva p ON p.solicitud_id=s.id
            WHERE s.id=? FOR UPDATE OF s,p""", { r, _ ->
            PagoInfo(r.getLong(1), r.getString(2), r.getString(3), r.getString(4))
        }, id)
        val pago = filas.firstOrNull() ?: throw RecursoNoEncontradoException("Pago no encontrado")
        if (perfil.clienteInstitucionalId != pago.cliente) throw AccessDeniedException("Solicitud ajena")
        if (pago.metodo != "TRANSFERENCIA") throw ConflictoException("Solo las transferencias admiten referencia")
        if (pago.reserva in setOf("RECHAZADA", "CANCELADA", "FINALIZADA", "ARCHIVADA"))
            throw ConflictoException("Esta solicitud esta cerrada")
        if (pago.estado in setOf("VERIFICADO", "REEMBOLSO_PENDIENTE", "REEMBOLSADO"))
            throw ConflictoException("El pago ya fue verificado o esta en reembolso")
        val referencia = req.referencia.trim()
        if (referencia.length !in 4..120) throw IllegalArgumentException("Referencia de 4 a 120 caracteres")
        jdbc.update("""UPDATE pagos_reserva SET referencia=?,estado='REPORTADO',actualizado_en=NOW()
            WHERE solicitud_id=?""", referencia, id)
        return buscar(id)
    }

    /** Verifica ingreso externo (o reembolso manual); nunca procesa dinero online. */
    @PatchMapping("/api/admin/solicitudes/{id}/pago")
    @Transactional
    fun revisarPago(@PathVariable id: Long, authentication: Authentication,
                    @Valid @RequestBody req: EstadoPagoRequest): ReservaResponse {
        val perfil = auth.perfil(authentication.name)
        if (perfil.rol != ADMIN_ROLE) throw AccessDeniedException("Solo administracion")
        val pago = jdbc.query("""SELECT p.metodo,p.estado,s.estado FROM pagos_reserva p
            JOIN solicitudes s ON s.id=p.solicitud_id WHERE s.id=? FOR UPDATE OF s,p""", { r, _ ->
            Triple(r.getString(1), r.getString(2), r.getString(3))
        }, id).firstOrNull() ?: throw RecursoNoEncontradoException("Pago no encontrado")
        val nuevo = req.estado.trim().uppercase()
        val permitidos = when (nuevo) {
            "VERIFICADO" -> pago.second in setOf("PENDIENTE", "REPORTADO", "RECHAZADO") &&
                (pago.first != "TRANSFERENCIA" || pago.second == "REPORTADO") &&
                pago.third !in setOf("CANCELADA", "RECHAZADA", "ARCHIVADA")
            "RECHAZADO" -> pago.second == "REPORTADO"
            "REEMBOLSADO" -> pago.second == "REEMBOLSO_PENDIENTE"
            else -> false
        }
        if (!permitidos) throw ConflictoException("Transicion de pago no permitida")
        if (nuevo == "REEMBOLSADO" && (req.observacion?.trim()?.length ?: 0) < 4)
            throw IllegalArgumentException("Describa la devolucion manual del dinero")
        jdbc.update("""UPDATE pagos_reserva SET estado=?,observacion=?,revisado_por=?,actualizado_en=NOW()
            WHERE solicitud_id=?""", nuevo, req.observacion?.trim(), perfil.id, id)
        return buscar(id)
    }

    /**
     * Controla el ciclo: RECIBIDA -> CONFIRMADA -> EN_SERVICIO -> FINALIZADA.
     * La fecha contractual no impide una devolucion anticipada real, que exige motivo.
     */
    @PatchMapping("/api/admin/solicitudes/{id}/estado")
    @Transactional
    fun cambiarEstado(@PathVariable id: Long, authentication: Authentication, @Valid @RequestBody req: EstadoRequest): ReservaResponse {
        val destino = req.estado.trim().uppercase()
        val info = jdbc.query("""SELECT s.estado,s.fecha_fin,p.metodo,p.estado,s.fecha_inicio,s.vehiculo_id,s.modalidad,s.conductor_profesional_id
            FROM solicitudes s LEFT JOIN pagos_reserva p ON p.solicitud_id=s.id
            WHERE s.id=? FOR UPDATE OF s""", { r, _ ->
            EstadoInfo(r.getString(1), r.getObject(2, OffsetDateTime::class.java),
                r.getString(3), r.getString(4),r.getObject(5,OffsetDateTime::class.java),r.getLong(6),r.getString(7),r.getLong(8).let { if(r.wasNull()) null else it })
        }, id).firstOrNull() ?: throw RecursoNoEncontradoException("Reserva inexistente")
        val transiciones = mapOf(
            "RECIBIDA" to setOf("EN_REVISION", "CONFIRMADA", "RECHAZADA"),
            "EN_REVISION" to setOf("CONFIRMADA", "RECHAZADA"),
            "CONFIRMADA" to setOf("EN_SERVICIO"),
            "ASIGNADA" to setOf("EN_SERVICIO"),
            "COTIZADA" to setOf("CONFIRMADA","RECHAZADA"),
            "PENDIENTE_APROBACION" to setOf("CONFIRMADA","RECHAZADA"),
            "EN_SERVICIO" to setOf("FINALIZADA"),
        )
        if (destino !in (transiciones[info.estado] ?: emptySet()))
            throw ConflictoException("Transicion no permitida: ${info.estado} a $destino")
        if (destino == "CONFIRMADA") {
            // Si hay una propuesta activa, la última versión debe haber sido aceptada.
            val propuesta = jdbc.query("SELECT estado FROM cotizaciones WHERE solicitud_id=? ORDER BY numero_version DESC LIMIT 1", { rs,_ -> rs.getString(1) },id).firstOrNull()
            if (propuesta != null && propuesta != "ACEPTADA")
                throw ConflictoException("La cotizacion debe aceptarse antes de confirmar")
        }
        if (destino == "EN_SERVICIO") {
            if (info.estadoPago != "VERIFICADO") throw ConflictoException("Registre y verifique el pago antes de entregar el vehiculo")
            val ahora = OffsetDateTime.now()
            if (!info.fin.isAfter(ahora)) throw ConflictoException("El periodo ya termino; gestione una nueva solicitud")
            if (info.inicio.isAfter(ahora) && (req.motivoInicio?.trim()?.length ?: 0) < 4)
                throw IllegalArgumentException("Indique el motivo de entrega anticipada (minimo 4 caracteres)")
            // Bloqueo de la unidad: evita entregar un vehículo ocupado o en mantenimiento.
            val disponible = jdbc.query("SELECT activo AND estado='DISPONIBLE' FROM vehiculos WHERE id=? FOR UPDATE",{rs,_ -> rs.getBoolean(1)},info.vehiculoId).firstOrNull() ?: false
            if (!disponible) throw ConflictoException("Vehiculo no disponible para entrega")
            val conflictos = jdbc.queryForObject("""SELECT COUNT(*) FROM solicitudes WHERE vehiculo_id=? AND id<>?
              AND estado IN $ocupados AND (estado='EN_SERVICIO' OR (fecha_inicio < ? AND fecha_fin > ?))""",Long::class.java,info.vehiculoId,id,info.fin,ahora) ?: 0L
            if (conflictos>0) throw ConflictoException("Existe otra reserva incompatible con la entrega")
            val mantenimiento = jdbc.queryForObject("""SELECT COUNT(*) FROM mantenimientos WHERE vehiculo_id=? AND
              (estado='EN_PROCESO' OR (estado='PROGRAMADO' AND fecha_programada BETWEEN ? AND ?))""",Long::class.java,info.vehiculoId,ahora.atZoneSameInstant(zonaOperacion).toLocalDate(),info.fin.atZoneSameInstant(zonaOperacion).toLocalDate()) ?: 0L
            if (mantenimiento>0) throw ConflictoException("Mantenimiento incompatible con la entrega")
            if (info.modalidad=="CON_CONDUCTOR") {
                val conductor = info.conductorId ?: throw ConflictoException("Asigne un conductor profesional antes de iniciar")
                val habilitado = jdbc.query("""SELECT cp.activo AND u.activo AND cp.fecha_vencimiento_licencia>=? FROM conductores_profesionales cp JOIN usuarios u ON u.id=cp.usuario_id WHERE cp.id=? FOR UPDATE OF cp""",{rs,_ -> rs.getBoolean(1)},info.fin.atZoneSameInstant(zonaOperacion).toLocalDate(),conductor).firstOrNull() ?: false
                if (!habilitado) throw ConflictoException("Conductor no habilitado")
                val ocupacion=jdbc.queryForObject("""SELECT COUNT(*) FROM solicitudes WHERE conductor_profesional_id=? AND id<>? AND estado IN $ocupados AND (estado='EN_SERVICIO' OR (fecha_inicio<? AND fecha_fin>?))""",Long::class.java,conductor,id,info.fin,ahora) ?: 0L
                if(ocupacion>0) throw ConflictoException("Conductor ocupado")
            }
        }
        if (destino == "FINALIZADA" && info.fin.isAfter(OffsetDateTime.now()) &&
            (req.motivoCierre?.trim()?.length ?: 0) < 4)
            throw IllegalArgumentException("Para una devolucion anticipada registre el motivo (minimo 4 caracteres)")
        when (destino) {
            "EN_SERVICIO" -> jdbc.update("""UPDATE solicitudes SET estado=?,fecha_inicio_real=NOW(),
                motivo_inicio_anticipado=?,iniciado_por=?,actualizado_en=NOW() WHERE id=?""", destino, req.motivoInicio?.trim(),auth.perfil(authentication.name).id,id)
            "FINALIZADA" -> jdbc.update("""UPDATE solicitudes SET estado=?,fecha_fin_real=NOW(),
                motivo_cierre_anticipado=?,actualizado_en=NOW() WHERE id=?""",
                destino, req.motivoCierre?.trim(), id)
            else -> jdbc.update("UPDATE solicitudes SET estado=?,actualizado_en=NOW() WHERE id=?", destino, id)
        }
        if (destino == "RECHAZADA") jdbc.update("UPDATE cotizaciones SET estado='ANULADA',actualizado_en=NOW() WHERE solicitud_id=? AND estado IN ('BORRADOR','ENVIADA')",id)
        if (destino == "RECHAZADA") jdbc.update("""UPDATE pagos_reserva
            SET estado='REEMBOLSO_PENDIENTE',actualizado_en=NOW()
            WHERE solicitud_id=? AND estado='VERIFICADO'""", id)
        return buscar(id)
    }

    /** Resuelve el identificador tras cualquier mutacion. */
    private fun buscar(id: Long): ReservaResponse = jdbc.query("$reservaSelect WHERE s.id=?", reservaMapper, id)
        .firstOrNull() ?: throw RecursoNoEncontradoException("Reserva inexistente")

    /** Datos bloqueados de inventario y cliente utilizados al reservar. */
    private data class VehicleInfo(val tarifa: BigDecimal, val pasajeros: Int,
                                   val estado: String, val activo: Boolean, val clienteActivo: Boolean)
    /** Datos de propiedad y estado utilizados al reportar un pago. */
    private data class PagoInfo(val cliente: Long, val reserva: String, val metodo: String, val estado: String)
    /** Proyección del periodo, pago y modalidad necesarios para una transición segura. */
    private data class EstadoInfo(val estado: String, val fin: OffsetDateTime,
                                  val metodoPago: String?, val estadoPago: String?, val inicio: OffsetDateTime, val vehiculoId: Long, val modalidad: String, val conductorId: Long?)
    private companion object { const val ADMIN_ROLE = "ADMINISTRADOR" }
}
