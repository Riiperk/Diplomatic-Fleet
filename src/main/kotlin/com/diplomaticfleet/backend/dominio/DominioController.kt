package com.diplomaticfleet.backend.dominio

import com.diplomaticfleet.backend.exception.ConflictoException
import com.diplomaticfleet.backend.exception.RecursoNoEncontradoException
import com.diplomaticfleet.backend.seguridad.UsuarioAuth
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Gestión de datos normalizados del diagrama docente. Solo ADMINISTRADOR accede
 * a este recurso; no se publican documentos personales ni credenciales en el catálogo.
 * Los comandos aceptados son explícitos, con SQL fijo y parámetros enlazados.
 */
@RestController
@RequestMapping("/api/admin/dominio")
class DominioController(private val jdbc: JdbcTemplate, private val auth: UsuarioAuth) {
    /** Conjunto fijo de catálogos consultables; no proviene de entradas HTTP. */
    private val catalogos = listOf("cat_tipo_documento","cat_genero","cat_tipo_doc_persona","cat_tipo_cliente","cat_tipo_mision","cat_rango_diplomatico","cat_estado_acreditacion","cat_categoria_licencia","cat_tipo_vehiculo","cat_nivel_blindaje","cat_estado_admin","cat_estado_operativo","cat_tipo_mantenimiento","cat_estado_mant","cat_tipo_baja","cat_tipo_servicio","cat_estado_reserva","cat_perfil")
    private val hoy: LocalDate get() = LocalDate.now(ZoneId.of("America/Bogota"))
    /** Datos de administración y opciones reales de las claves foráneas. */
    @GetMapping
    fun consultar(): Map<String,Any> = mapOf(
        "catalogos" to catalogos.associateWith { jdbc.queryForList("SELECT codigo FROM $it WHERE activo=true ORDER BY codigo") },
        "paises" to jdbc.queryForList("SELECT * FROM pais ORDER BY nombre"),
        "ciudades" to jdbc.queryForList("SELECT * FROM ciudad ORDER BY nombre"),
        "personas" to jdbc.queryForList("""SELECT id_persona,pri_nom_per,seg_nom_per,pri_ape_per,seg_ape_per,fech_nac_per::text AS fech_nac_per,fk_id_tip_doc,documento_per,fk_id_gen,fk_id_ciu,act_per FROM persona ORDER BY id_persona"""),
        "direcciones" to jdbc.queryForList("SELECT * FROM persona_direccion ORDER BY id_direccion"),
        "contactos" to jdbc.queryForList("SELECT * FROM persona_contacto ORDER BY id_contacto"),
        "documentos" to jdbc.queryForList("SELECT id_doc_persona,fk_id_persona,fk_id_tip_doc_per,num_doc,fech_expedicion::text AS fech_expedicion,fech_vencimiento::text AS fech_vencimiento,archivo_url,act_doc FROM persona_documento ORDER BY id_doc_persona DESC"),
        "clientes" to jdbc.queryForList("""SELECT d.id_cliente,d.fk_id_persona,d.fk_id_tip_cli,d.fk_id_tip_mision,d.nom_mision,d.fk_id_pais_acred,d.fk_id_rango,d.num_cedula_diplo,d.fech_acreditacion::text AS fech_acreditacion,d.fech_venc_acred::text AS fech_venc_acred,d.fk_id_est_acred,d.act_cliente,
          CASE WHEN d.fk_id_est_acred='VIGENTE' AND d.fech_venc_acred<(now() AT TIME ZONE 'America/Bogota')::date THEN 'VENCIDA' ELSE d.fk_id_est_acred END AS estado_efectivo FROM cliente_diplomatico d ORDER BY d.nom_mision"""),
        "talleres" to jdbc.queryForList("SELECT * FROM taller ORDER BY nom_taller"),
        "ubicaciones" to jdbc.queryForList("SELECT * FROM ubicacion ORDER BY nom_ubicacion"),
        "proveedores" to jdbc.queryForList("SELECT * FROM proveedor ORDER BY nombre"),
        "vehiculos" to jdbc.queryForList("SELECT id,placa,marca,modelo,nivel_blindaje,estado_admin,estado,tipo_vehiculo,proveedor_id FROM vehiculos ORDER BY placa"),
        "conductores" to jdbc.queryForList("SELECT c.*,concat_ws(' ',p.pri_nom_per,p.pri_ape_per) AS nombre FROM conductor c JOIN persona p ON p.id_persona=c.fk_id_persona ORDER BY c.id_conductor"),
        "bajas" to jdbc.queryForList("SELECT b.*,v.placa FROM baja_vehiculo b JOIN vehiculos v ON v.id=b.fk_id_vehiculo ORDER BY b.id_baja DESC")
    )

    /** Las fechas y la longitud se verifican antes de la escritura; NULL significa sin dato. */
    private fun Map<String,String?>.texto(k:String,max:Int=255): String? = get(k)?.trim()?.takeIf { it.isNotEmpty() }?.also { require(it.length<=max) { "Longitud excedida: $k" } }
    private fun Map<String,String?>.obligatorio(k:String,max:Int=255): String = texto(k,max) ?: throw IllegalArgumentException("Complete $k")
    private fun Map<String,String?>.id(k:String): Long? = texto(k,20)?.let { it.toLongOrNull()?.takeIf { n -> n>0 } ?: throw IllegalArgumentException("Identificador inválido: $k") }
    private fun Map<String,String?>.idObligatorio(k:String): Long = id(k) ?: throw IllegalArgumentException("Seleccione $k")
    private fun Map<String,String?>.fecha(k:String): LocalDate? = texto(k,10)?.let { try { LocalDate.parse(it) } catch (_: java.time.format.DateTimeParseException) { throw IllegalArgumentException("Fecha inválida: $k") } }
    private fun Map<String,String?>.booleano(k:String,def:Boolean=true):Boolean = when(texto(k,5)){null->def;"true"->true;"false"->false;else->throw IllegalArgumentException("Valor inválido: $k")}
    private fun comprobar(n:Int) { if(n!=1) throw RecursoNoEncontradoException("Registro no encontrado") }

    /** Crea o actualiza un registro mediante una operación de dominio explícita. */
    @PostMapping("/{operacion}")
    @Transactional
    fun guardar(@PathVariable operacion:String, @RequestBody d:Map<String,String?>, authentication:Authentication): Map<String,Boolean> {
        when(operacion) {
            "pais" -> {
                val codigo=d.obligatorio("id_pais",10).uppercase();require(codigo.matches(Regex("[A-Z]{2,3}"))) { "Use un código de país de dos o tres letras" }
                jdbc.update("INSERT INTO pais(id_pais,nombre) VALUES(?,?) ON CONFLICT(id_pais) DO UPDATE SET nombre=EXCLUDED.nombre",codigo,d.obligatorio("nombre",100))
            }
            "ciudad" -> {
                val id=d.id("id_ciudad"); val pais=d.obligatorio("fk_id_pais",10); val nombre=d.obligatorio("nombre",100)
                if(id==null)jdbc.update("INSERT INTO ciudad(fk_id_pais,nombre) VALUES(?,?)",pais,nombre)
                else comprobar(jdbc.update("UPDATE ciudad SET fk_id_pais=?,nombre=? WHERE id_ciudad=?",pais,nombre,id))
            }
            "persona" -> {
                val id=d.obligatorio("id_persona",80); val nac=d.fecha("fech_nac_per");require(nac==null || !nac.isAfter(hoy)) { "Fecha de nacimiento futura" }
                // Los nombres principales y la actividad pertenecen a la cuenta o contacto original.
                comprobar(jdbc.update("""UPDATE persona SET seg_nom_per=?,seg_ape_per=?,fech_nac_per=?,fk_id_tip_doc=?,documento_per=?,fk_id_gen=?,fk_id_ciu=? WHERE id_persona=?""",d.texto("seg_nom_per",100),d.texto("seg_ape_per",100),nac,d.texto("fk_id_tip_doc",50),d.texto("documento_per",80),d.texto("fk_id_gen",50),d.id("fk_id_ciu"),id))
            }
            "direccion" -> {
                val p=d.obligatorio("fk_id_persona",80); val dir=d.obligatorio("dir_fisica",255);val id=d.texto("id_direccion",100) ?: "D-"+UUID.randomUUID()
                jdbc.update("""INSERT INTO persona_direccion(id_direccion,fk_id_persona,dir_fisica,fk_id_ciu,es_principal,act_direccion) VALUES(?,?,?,?,?,?)
                   ON CONFLICT(id_direccion) DO UPDATE SET dir_fisica=EXCLUDED.dir_fisica,fk_id_ciu=EXCLUDED.fk_id_ciu,es_principal=EXCLUDED.es_principal,act_direccion=EXCLUDED.act_direccion""",id,p,dir,d.id("fk_id_ciu"),d.booleano("es_principal",false),d.booleano("act_direccion"))
            }
            "contacto" -> {
                val tipo=d.obligatorio("tipo_contacto",30);require(tipo in setOf("CORREO","TELEFONO","OTRO")) { "Tipo de contacto inválido" }
                val id=d.texto("id_contacto",140) ?: "K-"+UUID.randomUUID();val valor=d.obligatorio("valor_contacto",255)
                if(tipo=="CORREO")require(valor.contains('@')) { "Correo inválido" }
                jdbc.update("""INSERT INTO persona_contacto(id_contacto,fk_id_persona,tipo_contacto,valor_contacto,es_principal,act_contacto) VALUES(?,?,?,?,?,?)
                   ON CONFLICT(id_contacto) DO UPDATE SET tipo_contacto=EXCLUDED.tipo_contacto,valor_contacto=EXCLUDED.valor_contacto,es_principal=EXCLUDED.es_principal,act_contacto=EXCLUDED.act_contacto""",id,d.obligatorio("fk_id_persona",80),tipo,valor,d.booleano("es_principal",false),d.booleano("act_contacto"))
            }
            "documento" -> {
                val exped=d.fecha("fech_expedicion");val vence=d.fecha("fech_vencimiento");require(exped==null || vence==null || !vence.isBefore(exped)) { "Vigencia documental inválida" }
                val url=d.texto("archivo_url",800);require(url==null || url.startsWith("https://")) { "Solo URL HTTPS; no cargue archivos personales reales en la demo" }
                val id=d.id("id_doc_persona");val persona=d.obligatorio("fk_id_persona",80);val tipo=d.obligatorio("fk_id_tip_doc_per",50);val numero=d.obligatorio("num_doc",80);val activo=d.booleano("act_doc")
                if(id==null)jdbc.update("INSERT INTO persona_documento(fk_id_persona,fk_id_tip_doc_per,num_doc,fech_expedicion,fech_vencimiento,archivo_url,act_doc) VALUES(?,?,?,?,?,?,?)",persona,tipo,numero,exped,vence,url,activo)
                else comprobar(jdbc.update("UPDATE persona_documento SET fk_id_tip_doc_per=?,num_doc=?,fech_expedicion=?,fech_vencimiento=?,archivo_url=?,act_doc=? WHERE id_doc_persona=? AND fk_id_persona=?",tipo,numero,exped,vence,url,activo,id,persona))
            }
            "acreditacion" -> {
                val id=d.idObligatorio("id_cliente");val estado=d.obligatorio("fk_id_est_acred",50);val fecha=d.fecha("fech_acreditacion");val vence=d.fecha("fech_venc_acred");val pais=d.texto("fk_id_pais_acred",10);val rango=d.texto("fk_id_rango",50);val numero=d.texto("num_cedula_diplo",80)
                require(fecha==null || vence==null || !vence.isBefore(fecha)) { "Periodo de acreditación inválido" }
                if(estado=="VIGENTE") require(fecha!=null && !fecha.isAfter(hoy) && vence!=null && !vence.isBefore(hoy) && pais!=null && rango!=null && rango!="NO_DECLARADO" && numero!=null) { "Complete los datos y la vigencia de la acreditación" }
                comprobar(jdbc.update("""UPDATE cliente_diplomatico SET fk_id_tip_cli=?,fk_id_pais_acred=?,fk_id_rango=?,num_cedula_diplo=?,fech_acreditacion=?,fech_venc_acred=?,fk_id_est_acred=? WHERE id_cliente=?""",d.obligatorio("fk_id_tip_cli",50),pais,rango,numero,fecha,vence,estado,id))
            }
            "taller" -> {
                val id=d.id("id_taller"); val nombre=d.obligatorio("nom_taller",150);val nit=d.texto("nit_taller",40);val dir=d.texto("dir_taller",255);val ciudad=d.id("fk_id_ciu");val tel=d.texto("tel_taller",30);val activo=d.booleano("act_taller")
                if(id==null)jdbc.update("INSERT INTO taller(nom_taller,nit_taller,dir_taller,fk_id_ciu,tel_taller,act_taller) VALUES(?,?,?,?,?,?)",nombre,nit,dir,ciudad,tel,activo)
                else {
                    // El nombre histórico es una referencia textual usada por el sistema previo; no se cambia.
                    comprobar(jdbc.update("UPDATE taller SET nit_taller=?,dir_taller=?,fk_id_ciu=?,tel_taller=?,act_taller=? WHERE id_taller=?",nit,dir,ciudad,tel,activo,id))
                }
            }
            "ubicacion" -> {
                val id=d.id("id_ubicacion");val nombre=d.obligatorio("nom_ubicacion",255);val tipo=d.texto("tipo_ubicacion",50);val dir=d.texto("dir_ubicacion",255);val ciudad=d.id("fk_id_ciu");val activo=d.booleano("act_ubicacion");val publica=d.booleano("publica",false)
                if(id==null)jdbc.update("INSERT INTO ubicacion(nom_ubicacion,tipo_ubicacion,dir_ubicacion,fk_id_ciu,act_ubicacion,publica) VALUES(?,?,?,?,?,?)",nombre,tipo,dir,ciudad,activo,publica)
                else comprobar(jdbc.update("UPDATE ubicacion SET tipo_ubicacion=?,dir_ubicacion=?,fk_id_ciu=?,act_ubicacion=?,publica=? WHERE id_ubicacion=?",tipo,dir,ciudad,activo,publica,id))
            }
            "conductor" -> {
                val id=d.obligatorio("id_conductor",30);val n=id.substringAfter('-').toLongOrNull() ?: throw IllegalArgumentException("Conductor inválido")
                val sql=when { id.startsWith("A-")->"UPDATE conductores_autorizados SET es_diplomatico=? WHERE id=?";id.startsWith("P-")->"UPDATE conductores_profesionales SET es_diplomatico=? WHERE id=?";else->throw IllegalArgumentException("Conductor inválido") }
                comprobar(jdbc.update(sql,d.booleano("es_diplomatico",false),n))
            }
            "proveedor" -> {
                val id=d.id("id_proveedor");val nombre=d.obligatorio("nombre",150);val activo=d.booleano("activo")
                if(id==null)jdbc.update("INSERT INTO proveedor(nombre,activo) VALUES(?,?)",nombre,activo)
                else comprobar(jdbc.update("UPDATE proveedor SET nombre=?,activo=? WHERE id_proveedor=?",nombre,activo,id))
            }
            "blindaje" -> {
                val id=d.idObligatorio("id"); val nivel=d.obligatorio("nivel_blindaje",50)
                bloquearVehiculo(id)
                if(nivel in setOf("NO_DECLARADO","SIN_BLINDAJE") && existeReserva(id,true))throw ConflictoException("La unidad tiene reservas que requieren blindaje")
                comprobar(jdbc.update("UPDATE vehiculos SET nivel_blindaje=?,tipo_vehiculo=?,proveedor_id=?,actualizado_en=now() WHERE id=?",nivel,d.obligatorio("tipo_vehiculo",50),d.id("proveedor_id"),id))
            }
            "baja" -> {
                val id=d.idObligatorio("fk_id_vehiculo"); val estado=bloquearVehiculo(id)
                if(estado=="BAJA")throw ConflictoException("La unidad ya está dada de baja")
                if(existeReserva(id,false) || (jdbc.queryForObject("SELECT count(*) FROM mantenimientos WHERE vehiculo_id=? AND estado IN ('PROGRAMADO','EN_PROCESO')",Long::class.java,id) ?: 0L)>0L)throw ConflictoException("Cierre o cancele las operaciones pendientes antes de dar de baja la unidad")
                jdbc.update("INSERT INTO baja_vehiculo(fk_id_vehiculo,motivo,fk_id_tip_baja,fk_id_usu,observaciones) VALUES(?,?,?,?,?)",id,d.obligatorio("motivo",500),d.obligatorio("fk_id_tip_baja",50),auth.perfil(authentication.name).id,d.texto("observaciones",500))
                comprobar(jdbc.update("UPDATE vehiculos SET activo=false,estado='INACTIVO',estado_admin='BAJA',actualizado_en=now() WHERE id=?",id))
            }
            else -> throw RecursoNoEncontradoException("Operación no disponible")
        }
        return mapOf("ok" to true)
    }

    /** Bloqueo compartido con las transacciones de reserva y mantenimiento. */
    private fun bloquearVehiculo(id:Long):String = jdbc.query("SELECT estado_admin FROM vehiculos WHERE id=? FOR UPDATE",{r,_->r.getString(1)},id).firstOrNull() ?: throw RecursoNoEncontradoException("Vehículo inexistente")
    private fun existeReserva(id:Long,soloBlindaje:Boolean):Boolean = (jdbc.queryForObject("""SELECT COUNT(*) FROM solicitudes WHERE vehiculo_id=? AND estado IN ('RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION','CONFIRMADA','ASIGNADA','EN_SERVICIO') AND (?=false OR requiere_blindaje=true)""",Long::class.java,id,soloBlindaje) ?: 0L)>0L
}

/** Información de disponibilidad del backend para el balanceador de Render. */
@RestController
class SaludController(private val jdbc:JdbcTemplate) {
    /** Nombres de ubicaciones activas para sugerencias del formulario. */
    @GetMapping("/api/public/ubicaciones")
    fun ubicaciones(): List<String> = jdbc.query("SELECT nom_ubicacion FROM ubicacion WHERE act_ubicacion=true AND publica=true ORDER BY nom_ubicacion",{r,_->r.getString(1)})
    @GetMapping("/api/public/salud")
    fun salud(): Map<String,String> { jdbc.queryForObject("SELECT 1",Int::class.java);return mapOf("estado" to "OK") }
}
