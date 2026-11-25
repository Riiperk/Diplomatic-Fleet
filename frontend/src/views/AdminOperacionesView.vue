<script setup lang="ts">
import {computed, onMounted, reactive, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import {api,logout,sesion} from '@/services/api'
import {useRouter} from 'vue-router'
import DialogoBase from '@/components/layout/DialogoBase.vue'
import {crearCuenta,type CuentaNueva} from '@/services/operacionesService'
import DemoLayout from '@/components/layout/DemoLayout.vue'
import {listarVehiculos, type VehiculoResponse} from '@/services/vehiculoService'
import {listarClientes, type ClienteInstitucionalResponse} from '@/services/clienteService'
import {todasReservas, catalogo, pesos, proximaFecha, isoFecha, type Coche, type Reserva} from '@/services/reservaService'
import {
  documentos, crearDocumento, editarDocumento, mantenimientos, crearMantenimiento, cambiarMantenimiento,
  conductores, crearConductor, activarConductor, cotizaciones, crearCotizacion, cambiarCotizacion,
  usuarios, activarUsuario, reporte, type Documento, type Mantenimiento, type Conductor,
  type Cotizacion, type UsuarioSeguro, type Reporte,
} from '@/services/operacionesService'

/**
 * Pantalla administrativa compartida para módulos con operaciones persistidas.
 * Usa una vista diferente por ruta, sin registros de ejemplo codificados a mano.
 * Los errores del backend se presentan al usuario y los formularios se limpian
 * solo después de recibir una respuesta exitosa.
 */
const route=useRoute()
const modulo=computed(()=>route.path.slice(1))
const titulos:Record<string,{numero:string;titulo:string;texto:string}>={
  documentos:{numero:'04',titulo:'Documentos',texto:'Vigencia de SOAT, revisión, pólizas y certificados por vehículo.'},
  mantenimientos:{numero:'05',titulo:'Mantenimientos',texto:'Programa trabajos y controla las transiciones de mantenimiento.'},
  disponibilidad:{numero:'06',titulo:'Disponibilidad',texto:'Compara fechas y consulta unidades sin solicitudes superpuestas.'},
  'validacion-conductor':{numero:'08',titulo:'Conductores',texto:'Registro institucional y control de vigencia de licencias declaradas.'},
  cotizaciones:{numero:'09',titulo:'Cotizaciones',texto:'Propuestas económicas versionadas a partir de solicitudes.'},
  usuarios:{numero:'10',titulo:'Usuarios y roles',texto:'Consulta cuentas y habilita o suspende su acceso sin eliminar registros.'},
  reportes:{numero:'11',titulo:'Reportes',texto:'Indicadores obtenidos en tiempo real de PostgreSQL.'},
}
const titulo=computed(()=>titulos[modulo.value]??titulos.documentos!)
const loading=ref(false),saving=ref(false),error=ref(''),success=ref('')
const rowsD=ref<Documento[]>([]),rowsM=ref<Mantenimiento[]>([]),rowsC=ref<Conductor[]>([])
const rowsQ=ref<Cotizacion[]>([]),rowsU=ref<UsuarioSeguro[]>([]),rowsV=ref<Coche[]>([])
const vehiculos=ref<VehiculoResponse[]>([]),clientes=ref<ClienteInstitucionalResponse[]>([]),solicitudes=ref<Reserva[]>([])
const resumen=ref<Reporte|null>(null)
const buscar=ref('')
const inicio=ref(proximaFecha(1)),fin=ref(proximaFecha(3))
const hoy=proximaFecha(0)
const nuevoDoc=reactive({id:0,vehiculoId:0,tipoDocumento:'SOAT',numeroDocumento:'',fechaEmision:'',fechaVencimiento:proximaFecha(30),observaciones:''})
const nuevoM=reactive({vehiculoId:0,tipo:'PREVENTIVO',descripcion:'',fechaProgramada:proximaFecha(7),proveedor:'',costo:0})
const nuevoC=reactive({clienteInstitucionalId:0,nombreCompleto:'',tipoDocumento:'CEDULA_CIUDADANIA',numeroDocumento:'',numeroLicencia:'',fechaVencimientoLicencia:proximaFecha(365),categoriaLicencia:'B1',telefono:'',correo:''})
/** Alta administrativa: credenciales no persistidas en el formulario una vez creado el usuario. */
const cuenta=reactive({nombre:'',apellido:'',correo:'',password:'',rol:'CLIENTE',clienteInstitucionalId:0,numeroLicencia:'',categoriaLicencia:'B1',vencimientoLicencia:proximaFecha(365)})
const propio=sesion()?.id
const router=useRouter()
const cuentaClave=ref<UsuarioSeguro|null>(null),claveNueva=ref(''),claveRepetida=ref(''),errorClave=ref('')
/** Rotación administrativa: las claves no se muestran ni se escriben en registros. */
function abrirClave(u:UsuarioSeguro){cuentaClave.value=u;claveNueva.value='';claveRepetida.value='';errorClave.value=''}
async function guardarClave(){
 if(!cuentaClave.value||saving.value)return
 if(claveNueva.value!==claveRepetida.value){errorClave.value='Las contraseñas no coinciden';return}
 const id=cuentaClave.value.id;saving.value=true;errorClave.value=''
 try{await api<{ok:boolean}>(`/api/admin/usuarios/${id}/credencial`,{method:'POST',body:JSON.stringify({password:claveNueva.value})});claveNueva.value='';claveRepetida.value='';cuentaClave.value=null;if(id===propio){logout();await router.push('/login');return}success.value='Credencial actualizada. Las sesiones anteriores se revocaron.'}
 catch(e){errorClave.value=e instanceof Error?e.message:'No fue posible actualizar la credencial'}finally{saving.value=false}
}
const etiquetas:Record<string,string>={vehiculos:'Vehículos activos',vehiculosTotales:'Vehículos registrados',clientes:'Clientes activos',clientesTotales:'Clientes registrados',mantenimientos:'Mantenimientos en proceso',mantenimientosProgramados:'Mantenimientos programados',mantenimientosTotales:'Mantenimientos registrados',reservas:'Reservas totales',pendientes:'Solicitudes pendientes',documentosVencidos:'Documentos vencidos',cotizaciones:'Cotizaciones',valorEstimadoConfirmado:'Valor estimado confirmado, asignado, en servicio y finalizado',reservasFinalizadas:'Reservas finalizadas',reservasCanceladas:'Reservas canceladas',pagosVerificados:'Pagos externos verificados'}
async function altaCuenta(){saving.value=true;error.value='';success.value=''
 try{const payload:CuentaNueva={nombre:cuenta.nombre,apellido:cuenta.apellido,correo:cuenta.correo,password:cuenta.password,rol:cuenta.rol}
  if(cuenta.rol==='CLIENTE')payload.clienteInstitucionalId=cuenta.clienteInstitucionalId
  if(cuenta.rol==='CONDUCTOR')Object.assign(payload,{numeroLicencia:cuenta.numeroLicencia,categoriaLicencia:cuenta.categoriaLicencia,vencimientoLicencia:cuenta.vencimientoLicencia})
  await crearCuenta(payload);cuenta.password='';cuenta.nombre='';cuenta.apellido='';cuenta.correo='';cuenta.numeroLicencia='';await cargar();success.value='Cuenta creada correctamente.'
 }catch(e){error.value=e instanceof Error?e.message:'No se pudo crear la cuenta'}finally{saving.value=false}}
const nuevaQ=reactive({solicitudId:0,validaHasta:proximaFecha(15),observaciones:''})

/** Recupera registros reales según la ruta y carga listas auxiliares. */
async function cargar():Promise<void>{
  loading.value=true;error.value=''
  try{
    switch(modulo.value){
      case 'documentos':
        [rowsD.value,vehiculos.value]=await Promise.all([documentos(),listarVehiculos()]);break
      case 'mantenimientos':
        [rowsM.value,vehiculos.value]=await Promise.all([mantenimientos(),listarVehiculos()]);break
      case 'validacion-conductor':
        [rowsC.value,clientes.value]=await Promise.all([conductores(),listarClientes()]);break
      case 'cotizaciones':
        [rowsQ.value,solicitudes.value]=await Promise.all([cotizaciones(),todasReservas()]);break
      case 'usuarios': [rowsU.value,clientes.value]=await Promise.all([usuarios(),listarClientes()]);break
      case 'reportes': resumen.value=await reporte();break
      case 'disponibilidad': await consultarDisponibilidad();break
    }
  }catch(e){error.value=e instanceof Error?e.message:'No fue posible consultar los datos.'}
  finally{loading.value=false}
}

/** Filtra las tablas con los datos que ya devolvió la API. */
const filtrados=computed(()=>{
  const fuente:unknown[]=modulo.value==='documentos'?rowsD.value:
    modulo.value==='mantenimientos'?rowsM.value:
    modulo.value==='validacion-conductor'?rowsC.value:
    modulo.value==='cotizaciones'?rowsQ.value:rowsU.value
  return fuente.filter(row=>JSON.stringify(row).toLowerCase().includes(buscar.value.trim().toLowerCase()))
})

/** Envía solo el formulario pertinente al módulo actual. */
async function guardar():Promise<void>{
  saving.value=true;error.value='';success.value=''
  try{
    if(modulo.value==='documentos'){
      const payload={vehiculoId:nuevoDoc.vehiculoId,tipoDocumento:nuevoDoc.tipoDocumento,
        numeroDocumento:nuevoDoc.numeroDocumento||null,fechaEmision:nuevoDoc.fechaEmision||null,
        fechaVencimiento:nuevoDoc.fechaVencimiento,observaciones:nuevoDoc.observaciones||null}
      if(nuevoDoc.id) await editarDocumento(nuevoDoc.id,payload)
      else await crearDocumento(payload)
      nuevoDoc.id=0;nuevoDoc.numeroDocumento='';nuevoDoc.observaciones=''
    }
    if(modulo.value==='mantenimientos'){
      await crearMantenimiento({vehiculoId:nuevoM.vehiculoId,tipo:nuevoM.tipo,
        descripcion:nuevoM.descripcion,fechaProgramada:nuevoM.fechaProgramada,
        proveedor:nuevoM.proveedor||null,costo:nuevoM.costo??null})
      nuevoM.descripcion='';nuevoM.proveedor=''
    }
    if(modulo.value==='validacion-conductor'){
      await crearConductor({clienteInstitucionalId:nuevoC.clienteInstitucionalId,
        nombreCompleto:nuevoC.nombreCompleto,tipoDocumento:nuevoC.tipoDocumento,
        numeroDocumento:nuevoC.numeroDocumento,numeroLicencia:nuevoC.numeroLicencia,
        fechaVencimientoLicencia:nuevoC.fechaVencimientoLicencia,categoriaLicencia:nuevoC.categoriaLicencia||null,
        telefono:nuevoC.telefono||null,correo:nuevoC.correo||null})
      nuevoC.nombreCompleto='';nuevoC.numeroDocumento='';nuevoC.numeroLicencia='';nuevoC.correo=''
    }
    if(modulo.value==='cotizaciones'){
      await crearCotizacion({solicitudId:nuevaQ.solicitudId,validaHasta:nuevaQ.validaHasta,
        observaciones:nuevaQ.observaciones||null});nuevaQ.observaciones=''
    }
    await cargar();success.value='El registro se guardó correctamente en PostgreSQL.'
  }catch(e){error.value=e instanceof Error?e.message:'No se pudo registrar.'}
  finally{saving.value=false}
}

/** Actualiza un estado confirmado por el operador. */
async function cambiar(id:number,estado:string):Promise<void>{
  error.value='';success.value=''
  try{
    if(modulo.value==='mantenimientos')await cambiarMantenimiento(id,estado)
    if(modulo.value==='cotizaciones')await cambiarCotizacion(id,estado)
    await cargar();success.value='Estado actualizado correctamente.'
  }catch(e){error.value=e instanceof Error?e.message:'Error de actualización.'}
}

/** Abre un documento existente para editar los campos legales. */
function editar(r:Documento):void{
  Object.assign(nuevoDoc,{id:r.id,vehiculoId:r.vehiculo_id,tipoDocumento:r.tipo_documento,
    numeroDocumento:r.numero_documento??'',fechaEmision:r.fecha_emision??'',
    fechaVencimiento:r.fecha_vencimiento,observaciones:r.observaciones??''})
  window.scrollTo({top:0,behavior:'smooth'})
}

/** Cambia el permiso operativo tras confirmar la decisión. */
async function activar(tipo:'usuario'|'conductor',id:number,activo:boolean):Promise<void>{
  if(!window.confirm(`¿Quieres ${activo?'activar':'desactivar'} este registro?`))return
  error.value='';success.value=''
  try{
    if(tipo==='usuario')await activarUsuario(id,activo)
    else await activarConductor(id,activo)
    await cargar();success.value='Cambio guardado.'
  }catch(e){error.value=e instanceof Error?e.message:'No fue posible actualizar el registro.'}
}

/** Consulta disponibilidad por periodo usando el mismo endpoint del cliente. */
async function consultarDisponibilidad():Promise<void>{
  if(fin.value<=inicio.value){error.value='La devolución debe ser posterior a la recogida.';return}
  rowsV.value=await catalogo(isoFecha(inicio.value),isoFecha(fin.value))
}

onMounted(cargar)
watch(()=>route.path,()=>{buscar.value='';success.value='';void cargar()})
</script>

<template>
  <DemoLayout :module-number="titulo.numero" eyebrow="Gestión administrativa" :title="titulo.titulo" :description="titulo.texto">
    <section class="df-panel df-ops">
      <div class="df-heading"><div><span class="df-kicker">Datos registrados</span><h2>{{titulo.titulo}}<span class="df-highlight">.</span></h2></div>
        <button type="button" class="df-button dark mini" @click="cargar">Actualizar ↗</button>
      </div>
      <p v-if="error" role="alert" class="df-alert error">{{error}}</p>
      <p v-if="success" role="status" class="df-alert ok">{{success}}</p>
      <form v-if="['documentos','mantenimientos','validacion-conductor','cotizaciones'].includes(modulo)" class="df-ops-form" @submit.prevent="guardar">
        <h3>{{modulo==='documentos'&&nuevoDoc.id?'Editar documento':'Registrar datos'}}</h3>
        <template v-if="modulo==='documentos'">
          <label class="df-field">Vehículo<select v-model.number="nuevoDoc.vehiculoId" required><option :value="0" disabled>Seleccionar vehículo</option><option v-for="v in vehiculos" :key="v.id" :value="v.id">{{v.placa}} · {{v.marca}} {{v.modelo}}</option></select></label>
          <label class="df-field">Tipo<select v-model="nuevoDoc.tipoDocumento"><option v-for="t in ['SOAT','REVISION_TECNOMECANICA','TARJETA_PROPIEDAD','POLIZA','OTRO']" :key="t">{{t}}</option></select></label>
          <label class="df-field">Número o referencia<input v-model="nuevoDoc.numeroDocumento" maxlength="80"></label>
          <label class="df-field">Fecha de emisión<input v-model="nuevoDoc.fechaEmision" type="date"></label>
          <label class="df-field">Vencimiento<input v-model="nuevoDoc.fechaVencimiento" type="date" required></label>
          <label class="df-field">Observaciones<input v-model="nuevoDoc.observaciones" maxlength="500"></label>
        </template>
        <template v-else-if="modulo==='mantenimientos'">
          <label class="df-field">Vehículo<select v-model.number="nuevoM.vehiculoId" required><option :value="0" disabled>Seleccionar vehículo</option><option v-for="v in vehiculos" :key="v.id" :value="v.id">{{v.placa}} · {{v.marca}} {{v.modelo}}</option></select></label>
          <label class="df-field">Tipo<select v-model="nuevoM.tipo"><option value="PREVENTIVO">Preventivo</option><option value="CORRECTIVO">Correctivo</option></select></label>
          <label class="df-field">Trabajo a realizar<input v-model="nuevoM.descripcion" maxlength="500" required></label>
          <label class="df-field">Fecha programada<input v-model="nuevoM.fechaProgramada" type="date" required></label>
          <label class="df-field">Proveedor o taller<input v-model="nuevoM.proveedor" maxlength="150"></label>
          <label class="df-field">Costo estimado COP<input v-model.number="nuevoM.costo" type="number" min="0" step="0.01"></label>
        </template>
        <template v-else-if="modulo==='validacion-conductor'">
          <label class="df-field">Institución<select v-model.number="nuevoC.clienteInstitucionalId" required><option :value="0" disabled>Seleccionar institución</option><option v-for="c in clientes" :key="c.id" :value="c.id">{{c.nombreEntidad}}</option></select></label>
          <label class="df-field">Nombre completo<input v-model="nuevoC.nombreCompleto" required maxlength="140"></label>
          <label class="df-field">Tipo de documento<select v-model="nuevoC.tipoDocumento"><option value="CEDULA_CIUDADANIA">Cédula de ciudadanía</option><option value="CEDULA_EXTRANJERIA">Cédula de extranjería</option><option value="PASAPORTE">Pasaporte</option><option value="OTRO">Otro</option></select></label>
          <label class="df-field">Número de documento<input v-model="nuevoC.numeroDocumento" required maxlength="40"></label>
          <label class="df-field">Número de licencia<input v-model="nuevoC.numeroLicencia" required maxlength="60"></label>
          <label class="df-field">Categoría licencia<input v-model="nuevoC.categoriaLicencia" maxlength="20"></label>
          <label class="df-field">Vencimiento de licencia<input v-model="nuevoC.fechaVencimientoLicencia" type="date" :min="hoy" required></label>
          <label class="df-field">Teléfono<input v-model="nuevoC.telefono" maxlength="30"></label>
          <label class="df-field">Correo<input v-model="nuevoC.correo" type="email" maxlength="150"></label>
        </template>
        <template v-else-if="modulo==='cotizaciones'">
          <label class="df-field">Solicitud<select v-model.number="nuevaQ.solicitudId" required><option :value="0" disabled>Seleccionar solicitud</option><option v-for="s in solicitudes.filter(s=>['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION'].includes(s.estado)&&!rowsQ.some(q=>q.solicitud_id===s.id&&['BORRADOR','ENVIADA','ACEPTADA'].includes(q.estado)&&q.valida_hasta>=hoy))" :key="s.id" :value="s.id">{{s.codigo}} · {{s.cliente}} · {{pesos(s.valorEstimado)}}</option></select></label>
          <label class="df-field">Válida hasta<input v-model="nuevaQ.validaHasta" type="date" :min="hoy" required></label>
          <label class="df-field">Observaciones<input v-model="nuevaQ.observaciones" maxlength="1000"></label>
          <p class="df-meta">El subtotal procede de la solicitud. No se asumen impuestos ni se procesa ningún pago.</p>
        </template>
        <div class="df-ops-submit"><button class="df-button" type="submit" :disabled="saving">{{saving?'Guardando…':nuevoDoc.id&&modulo==='documentos'?'Guardar edición ↗':'Registrar ↗'}}</button>
          <button v-if="nuevoDoc.id&&modulo==='documentos'" class="df-button dark" type="button" @click="nuevoDoc.id=0">Cancelar edición</button>
        </div>
      </form>
      <!-- Alta de usuarios con roles reales; las contraseñas se transmiten solo al backend local. -->
      <form v-if="modulo==='usuarios'" class="df-ops-form" @submit.prevent="altaCuenta">
        <h3>Crear cuenta de acceso</h3>
        <label class="df-field">Nombre<input v-model="cuenta.nombre" required maxlength="70"/></label><label class="df-field">Apellido<input v-model="cuenta.apellido" required maxlength="70"/></label>
        <label class="df-field">Correo<input v-model="cuenta.correo" required type="email" maxlength="150"/></label><label class="df-field">Contraseña temporal<input v-model="cuenta.password" required type="password" minlength="8" maxlength="72" autocomplete="new-password"/></label>
        <label class="df-field">Rol<select v-model="cuenta.rol"><option value="CLIENTE">Cliente institucional</option><option value="ADMINISTRADOR">Administrador</option><option value="OPERACIONES">Operador de flota</option><option value="CONDUCTOR">Conductor profesional</option></select></label>
        <label v-if="cuenta.rol==='CLIENTE'" class="df-field">Institución<select v-model.number="cuenta.clienteInstitucionalId" required><option :value="0" disabled>Seleccione institución</option><option v-for="c in clientes.filter(c=>c.activo)" :key="c.id" :value="c.id">{{c.nombreEntidad}}</option></select></label>
        <template v-if="cuenta.rol==='CONDUCTOR'"><label class="df-field">Licencia<input v-model="cuenta.numeroLicencia" required maxlength="60"/></label><label class="df-field">Categoría<input v-model="cuenta.categoriaLicencia" required maxlength="20"/></label><label class="df-field">Vencimiento<input v-model="cuenta.vencimientoLicencia" required type="date" :min="hoy"/></label></template>
        <div class="df-ops-submit"><button class="df-button" :disabled="saving">Crear cuenta</button></div>
      </form>
      <template v-if="modulo==='disponibilidad'">
        <form class="df-fields df-ops-filters" @submit.prevent="consultarDisponibilidad">
          <label class="df-field">Recogida<input v-model="inicio" type="date" :min="hoy" required></label>
          <label class="df-field">Devolución<input v-model="fin" type="date" :min="inicio" required></label>
          <button class="df-button" type="submit">Ver disponibilidad ↗</button>
        </form>
        <div v-if="!rowsV.length" class="df-empty">No hay vehículos disponibles en estas fechas.</div>
        <div v-else class="df-tablescroll"><table class="df-table"><thead><tr><th>Modelo</th><th>Año</th><th>Categoría</th><th>Capacidad</th><th>Tarifa / día</th></tr></thead><tbody><tr v-for="v in rowsV" :key="v.id"><td>{{v.marca}} {{v.modelo}}</td><td>{{v.anio}}</td><td>{{v.categoria}}</td><td>{{v.pasajeros}} pasajeros</td><td>{{pesos(v.tarifaDiaria)}}</td></tr></tbody></table></div>
      </template>
      <template v-else-if="modulo==='reportes'">
        <div v-if="resumen" class="df-ops-stats">
          <div v-for="(value,key) in resumen" :key="key" class="df-ops-stat"><span>{{etiquetas[String(key)]||String(key)}}</span><strong>{{key==='valorEstimadoConfirmado'?pesos(value):value}}</strong></div>
        </div>
        <p class="df-meta">El valor confirmado es una estimación comercial y no representa dinero cobrado.</p>
      </template>
      <template v-else>
        <label class="df-field df-ops-search">Filtrar registros<input v-model="buscar" type="search" placeholder="Buscar en la tabla…"></label>
        <div v-if="loading" class="df-empty">Consultando base de datos…</div>
        <div v-else-if="!filtrados.length" class="df-empty">No hay registros que coincidan con el filtro.</div>
        <div v-else class="df-tablescroll"><table class="df-table">
          <thead v-if="modulo==='documentos'"><tr><th>Vehículo</th><th>Tipo</th><th>Referencia</th><th>Vencimiento</th><th>Estado</th><th>Acción</th></tr></thead>
          <thead v-else-if="modulo==='mantenimientos'"><tr><th>Vehículo</th><th>Trabajo</th><th>Programación</th><th>Estado</th><th>Acción</th></tr></thead>
          <thead v-else-if="modulo==='validacion-conductor'"><tr><th>Conductor</th><th>Institución</th><th>Licencia</th><th>Vigencia</th><th>Acción</th></tr></thead>
          <thead v-else-if="modulo==='cotizaciones'"><tr><th>Solicitud</th><th>Versión</th><th>Vigencia</th><th>Valor</th><th>Estado</th><th>Acción</th></tr></thead>
          <thead v-else><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th><th>Acción</th></tr></thead>
          <tbody v-if="modulo==='documentos'"><tr v-for="d in rowsD.filter(x=>JSON.stringify(x).toLowerCase().includes(buscar.toLowerCase()))" :key="d.id"><td>{{d.placa}}</td><td>{{d.tipo_documento}}</td><td>{{d.numero_documento||'—'}}</td><td>{{d.fecha_vencimiento}}</td><td><span class="df-status">{{d.estado}}</span></td><td><button class="df-button mini" @click="editar(d)">Editar</button></td></tr></tbody>
          <tbody v-else-if="modulo==='mantenimientos'"><tr v-for="m in rowsM.filter(x=>JSON.stringify(x).toLowerCase().includes(buscar.toLowerCase()))" :key="m.id"><td>{{m.placa}}</td><td><b>{{m.tipo}}</b><div>{{m.descripcion}}</div></td><td>{{m.fecha_programada}}</td><td><span class="df-status">{{m.estado}}</span></td><td><div class="df-actions"><button v-if="m.estado==='PROGRAMADO'" @click="cambiar(m.id,'EN_PROCESO')">Iniciar</button><button v-if="m.estado==='EN_PROCESO'" @click="cambiar(m.id,'FINALIZADO')">Finalizar</button><button v-if="['PROGRAMADO','EN_PROCESO'].includes(m.estado)" @click="cambiar(m.id,'CANCELADO')">Cancelar</button></div></td></tr></tbody>
          <tbody v-else-if="modulo==='validacion-conductor'"><tr v-for="c in rowsC.filter(x=>JSON.stringify(x).toLowerCase().includes(buscar.toLowerCase()))" :key="c.id"><td><b>{{c.nombre_completo}}</b><div>{{c.numero_documento}}</div></td><td>{{c.nombre_entidad}}</td><td>{{c.numero_licencia}}</td><td><span class="df-status">{{c.licencia_vigente?'Vigente':'No habilitada'}}</span><div>{{c.fecha_vencimiento_licencia}}</div></td><td><button class="df-button mini" @click="activar('conductor',c.id,!c.activo)">{{c.activo?'Desactivar':'Activar'}}</button></td></tr></tbody>
          <tbody v-else-if="modulo==='cotizaciones'"><tr v-for="q in rowsQ.filter(x=>JSON.stringify(x).toLowerCase().includes(buscar.toLowerCase()))" :key="q.id"><td>{{q.codigo}}</td><td>{{q.numero_version}}</td><td>{{q.valida_hasta}}</td><td>{{pesos(q.valor_total)}}</td><td><span class="df-status">{{q.estado}}</span></td><td><div class="df-actions"><button v-if="q.estado==='BORRADOR'" @click="cambiar(q.id,'ENVIADA')">Enviar</button><small v-if="q.estado==='ENVIADA'">En espera de decisión del cliente</small><button v-if="['BORRADOR','ENVIADA'].includes(q.estado)" @click="cambiar(q.id,'ANULADA')">Anular</button></div></td></tr></tbody>
          <tbody v-else><tr v-for="u in rowsU.filter(x=>JSON.stringify(x).toLowerCase().includes(buscar.toLowerCase()))" :key="u.id"><td>{{u.nombre}} {{u.apellido}}</td><td>{{u.correo}}</td><td>{{u.roles}}</td><td><span class="df-status">{{u.activo?'Activa':'Suspendida'}}</span></td><td><button class="df-button mini" :disabled="u.id===propio" @click="activar('usuario',u.id,!u.activo)">{{u.activo?'Desactivar':'Activar'}}</button> <button class="df-button mini" @click="abrirClave(u)">Cambiar contraseña</button></td></tr></tbody>
        </table></div>
      </template>
      <DialogoBase v-if="cuentaClave" titulo="Actualizar credencial" :ocupado="saving" @cerrar="cuentaClave=null;claveNueva='';claveRepetida=''">
        <p class="df-meta">Cuenta: {{cuentaClave.correo}}. Se cerrarán todas sus sesiones anteriores.</p>
        <form @submit.prevent="guardarClave"><label class="df-field">Nueva contraseña (mínimo 12 caracteres)<input v-model="claveNueva" type="password" minlength="12" maxlength="72" autocomplete="new-password" required /></label><label class="df-field">Confirmar contraseña<input v-model="claveRepetida" type="password" minlength="12" maxlength="72" autocomplete="new-password" required /></label><p v-if="errorClave" class="df-alert error" role="alert">{{errorClave}}</p><button class="df-button" :disabled="saving" type="submit">Actualizar contraseña</button></form>
      </DialogoBase>
    </section>
  </DemoLayout>
</template>
<style scoped>
.df-ops{max-width:1340px}.df-ops-form{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:15px;background:#f5f3ed;padding:22px;border-top:8px solid #e92f43;margin-bottom:28px}.df-ops-form h3,.df-ops-submit{grid-column:1/-1}.df-ops-form h3{font-size:30px}.df-ops-form .df-field{display:block;font-size:12px;font-weight:800}.df-ops-form .df-field :is(input,select){margin-top:9px}.df-ops-submit{display:flex;gap:14px;flex-wrap:wrap}.df-ops-search{display:block;max-width:480px}.df-ops-filters{align-items:end;margin-bottom:22px}.df-ops-filters button{margin-bottom:17px}.df-ops-stats{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:15px}.df-ops-stat{background:#171717;color:white;padding:22px;border-bottom:6px solid #e92f43}.df-ops-stat span{display:block;color:#ff8991;text-transform:uppercase;font-size:11px;letter-spacing:.05em}.df-ops-stat strong{font:900 38px 'Barlow Condensed',Impact;word-break:break-word}@media(max-width:760px){.df-ops-form{grid-template-columns:1fr}.df-ops-stats{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:480px){.df-ops-stats{grid-template-columns:1fr}}
</style>
