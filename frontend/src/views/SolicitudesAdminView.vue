<script setup lang="ts">
/** Componente SolicitudesAdmin: vista navegable conectada con las rutas y servicios del sistema. */
import {ref,onMounted,computed} from 'vue'
import DemoLayout from '@/components/layout/DemoLayout.vue'
import DialogoConfirmacion from '@/components/layout/DialogoConfirmacion.vue'
import {useConfirmacion} from '@/composables/useConfirmacion'
import {todasReservas,cambiarEstado,cancelarReserva,revisarPago,inicializarPago,pesos,fecha,nombreMetodo,nombrePago,type Reserva} from '@/services/reservaService'
import {profesionales,asignarProfesional,type Profesional} from '@/services/operacionesService'
const {pregunta,solicitar,responder}=useConfirmacion()
const conductores=ref<Profesional[]>([])
const asignaciones=ref<Record<number,number>>({})
const rows=ref<Reserva[]>([]),error=ref(''),message=ref(''),busy=ref(false),query=ref('')
const filtradas=computed(()=>rows.value.filter(r=>[r.codigo,r.cliente,r.vehiculo,r.estado].join(' ').toLowerCase().includes(query.value.toLowerCase())))
/** Carga las solicitudes reales para la bandeja administrativa. */
async function cargar(){busy.value=true;error.value='';try{[rows.value,conductores.value]=await Promise.all([todasReservas(),profesionales()])}catch(e){error.value=e instanceof Error?e.message:'Error'}finally{busy.value=false}}
/** Envía al servidor una transición de estado y vuelve a consultar la bandeja. */
async function actualizar(r:Reserva,estado:string){
 error.value='';message.value=''
 let motivoCierre:string|undefined
 let motivoInicio:string|undefined
 if(estado==='EN_SERVICIO' && !await solicitar({titulo:'Iniciar servicio',mensaje:'Confirme que el vehículo se entregó al cliente y comienza el servicio real.'}))return
 if(estado==='EN_SERVICIO' && new Date(r.inicio)>new Date()){
  const texto=await solicitar({titulo:'Entrega anticipada',mensaje:'La fecha real precede al periodo contratado. Registre la justificación administrativa.',campo:'Motivo de entrega anticipada',minimo:4,maximo:500})
  if(!texto || texto.trim().length<4)return
  motivoInicio=texto.trim()
 }
 if(estado==='FINALIZADA'){
  if(!await solicitar({titulo:'Finalizar servicio',mensaje:'Confirme que el vehículo fue devuelto y recibido por la flota.'}))return
  if(new Date(r.fin)>new Date()){
   const texto=await solicitar({titulo:'Devolución anticipada',mensaje:'Registre el motivo por el cual el vehículo se recibió antes de la fecha contratada.',campo:'Motivo de cierre',minimo:4,maximo:500})
   if(!texto || texto.trim().length<4)return
   motivoCierre=texto.trim()
  }
 }
 try{await cambiarEstado(r.id,estado,motivoCierre,motivoInicio);message.value=`Solicitud ${r.codigo}: ${estado}`;await cargar()}
 catch(e){error.value=e instanceof Error?e.message:'No se pudo actualizar'}
}
/** El operador puede anular una solicitud no iniciada, aun si vencio el horario contratado. */
async function cancelar(r:Reserva){
 const motivo=await solicitar({titulo:'Cancelar solicitud',mensaje:`La solicitud ${r.codigo} se cancelará. Si existe pago verificado se registrará reembolso pendiente.`,campo:'Motivo',minimo:4,maximo:500})
 if(!motivo || motivo.trim().length<4)return
 try{await cancelarReserva(r.id,motivo);message.value=`Solicitud ${r.codigo} cancelada.`;await cargar()}
 catch(e){error.value=e instanceof Error?e.message:'No se pudo cancelar'}
}
/** Confirma recepcion de pago realizado fuera del portal; no procesa un cobro. */
async function pago(r:Reserva,estado:'VERIFICADO'|'RECHAZADO'|'REEMBOLSADO'){
 let observacion:string|undefined
 if(estado==='VERIFICADO' && !await solicitar({titulo:'Verificar pago externo',mensaje:'Confirme que comprobó la recepción del pago externo. Esta acción no genera ningún cobro.'}))return
 if(estado==='REEMBOLSADO'){
  const texto=await solicitar({titulo:'Registrar reembolso',mensaje:'Confirme únicamente un reembolso realizado por fuera de la plataforma.',campo:'Referencia u observación',minimo:4,maximo:500})
  if(!texto || texto.trim().length<4)return
  observacion=texto.trim()
 }
 try{await revisarPago(r.id,estado,observacion);message.value=`Pago de ${r.codigo}: ${estado}`;await cargar()}
 catch(e){error.value=e instanceof Error?e.message:'No se pudo actualizar el pago'}
}
/** Regulariza reservas antiguas sin inventar un pago verificado. */
async function regularizar(r:Reserva){
 const metodo=await solicitar({titulo:'Registrar medio de pago',mensaje:'Esta reserva histórica todavía no tiene medio de pago. Seleccione el medio acordado.',campo:'Medio de pago',inicial:'TRANSFERENCIA',opciones:[{valor:'TRANSFERENCIA',etiqueta:'Transferencia bancaria'},{valor:'EFECTIVO',etiqueta:'Efectivo'},{valor:'TARJETA_PRESENCIAL',etiqueta:'Tarjeta presencial'}]})
 if(metodo!=='TRANSFERENCIA'&&metodo!=='EFECTIVO'&&metodo!=='TARJETA_PRESENCIAL')return
 try{await inicializarPago(r.id,metodo);await cargar();message.value='Medio registrado; pago pendiente de verificación.'}catch(e){error.value=e instanceof Error?e.message:'Error'}
}
/** Registra la asignación seleccionada; Kotlin comprueba licencia y solapamientos. */
async function asignar(r:Reserva){const id=asignaciones.value[r.id];if(!id)return
 try{await asignarProfesional(r.id,id);await cargar();message.value='Conductor asignado.'}catch(e){error.value=e instanceof Error?e.message:'No se pudo asignar'}}
onMounted(cargar)
</script>
<template><DemoLayout module-number="07" eyebrow="Centro de operaciones" title="Solicitudes" description="Reservas reales, clientes y fechas registradas en PostgreSQL."><section class="df-panel"><div class="df-heading"><div><span class="df-kicker">Bandeja de entrada</span><h2>Gestión de reservas.</h2></div><button class="df-button dark mini" @click="cargar">Actualizar ↗</button></div><div class="df-field"><label>Buscar por código, cliente o estado</label><input v-model="query" placeholder="Filtrar solicitudes..." /></div><p v-if="error" class="df-alert error">{{error}}</p><p v-if="message" class="df-alert ok">{{message}}</p><div v-if="busy" class="df-empty">Cargando…</div><div v-else-if="!filtradas.length" class="df-empty">No hay solicitudes para mostrar.</div><div v-else class="df-tablescroll"><table class="df-table"><thead><tr><th>Código</th><th>Institución</th><th>Vehículo</th><th>Fechas</th><th>Estado</th><th>Estimado</th><th>Pago externo</th><th>Gestión</th></tr></thead><tbody><tr v-for="r in filtradas" :key="r.id"><td><b>{{r.codigo}}</b></td><td>{{r.cliente}}</td><td>{{r.vehiculo}}<br><small>{{r.modalidad==='CON_CONDUCTOR'?'Con conductor':'Sin conductor'}}</small><br><small v-if="r.conductor">{{r.conductor}}</small><small v-if="r.requiereBlindaje"> · Requiere blindaje</small></td><td>{{fecha(r.inicio)}} – {{fecha(r.fin)}}<br><small v-if="r.fechaInicioReal">Entrega real: {{fecha(r.fechaInicioReal)}}</small><br><small v-if="r.motivoInicioAnticipado">Entrega anticipada: {{r.motivoInicioAnticipado}}</small><br><small v-if="r.fechaFinReal">Devolución real: {{fecha(r.fechaFinReal)}}</small><br><small v-if="r.motivoCierreAnticipado">Motivo: {{r.motivoCierreAnticipado}}</small></td><td><span class="df-status" :class="r.estado">{{r.estado.replaceAll('_',' ')}}</span></td><td>{{pesos(r.valorEstimado)}}</td><td><b>{{nombreMetodo(r.metodoPago)}}</b><br>{{nombrePago(r.estadoPago)}}<br><small v-if="r.referenciaPago">Ref: {{r.referenciaPago}}</small><div class="df-actions">
 <button v-if="!r.metodoPago && ['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION','CONFIRMADA','ASIGNADA'].includes(r.estado)" @click="regularizar(r)">Registrar medio</button>
 <button v-if="['PENDIENTE','REPORTADO','RECHAZADO'].includes(r.estadoPago||'') && (r.metodoPago!=='TRANSFERENCIA' || r.estadoPago==='REPORTADO') && !['RECHAZADA','CANCELADA'].includes(r.estado)" @click="pago(r,'VERIFICADO')">Verificar pago</button>
 <button v-if="r.estadoPago==='REPORTADO'" @click="pago(r,'RECHAZADO')">Rechazar pago</button>
 <button v-if="r.estadoPago==='REEMBOLSO_PENDIENTE'" @click="pago(r,'REEMBOLSADO')">Marcar reembolsado</button></div></td><td><div class="df-actions"><button v-if="r.estado==='RECIBIDA'" @click="actualizar(r,'EN_REVISION')">Revisar</button><button v-if="['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION'].includes(r.estado)" @click="actualizar(r,'CONFIRMADA')">Confirmar</button><button v-if="['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION'].includes(r.estado)" @click="actualizar(r,'RECHAZADA')">Rechazar</button><template v-if="r.modalidad==='CON_CONDUCTOR' && ['CONFIRMADA','ASIGNADA'].includes(r.estado)"><select v-model.number="asignaciones[r.id]" aria-label="Seleccionar conductor"><option :value="0">Seleccione conductor</option><option v-for="c in conductores.filter(x=>x.habilitado)" :key="c.id" :value="c.id">{{c.nombre}}</option></select><button :disabled="!asignaciones[r.id]" @click="asignar(r)">Asignar conductor</button></template><button v-if="['CONFIRMADA','ASIGNADA'].includes(r.estado)" :disabled="r.estadoPago!=='VERIFICADO'||(r.modalidad==='CON_CONDUCTOR'&&!r.conductorProfesionalId)||new Date(r.fin)<=new Date()" @click="actualizar(r,'EN_SERVICIO')">Iniciar servicio</button><small v-if="['CONFIRMADA','ASIGNADA'].includes(r.estado)&&r.estadoPago!=='VERIFICADO'">Requiere pago verificado.</small>
 <button v-if="r.estado==='EN_SERVICIO'" @click="actualizar(r,'FINALIZADA')">Finalizar servicio</button>
 <button v-if="['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION','CONFIRMADA','ASIGNADA'].includes(r.estado)" @click="cancelar(r)">Cancelar</button></div></td></tr></tbody></table></div><p class="df-meta">Sólo se permiten transiciones de estado válidas. Los pagos se registran y verifican externamente, sin cobro bancario en esta web. Para iniciar el servicio se requiere verificacion del pago. La devolucion anticipada exige observacion. Los servicios con conductor requieren una asignación profesional vigente. La entrega anticipada exige justificación.</p><DialogoConfirmacion v-if="pregunta" :pregunta="pregunta" @responder="responder" /></section></DemoLayout></template>
