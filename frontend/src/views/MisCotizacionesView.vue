<script setup lang="ts">
/** El cliente ve la fase de preparación sin acceder a datos de borradores internos. */
import {computed,ref,onMounted} from 'vue'
import {useRoute} from 'vue-router'
import FleetNav from '@/components/layout/FleetNav.vue'
import DialogoConfirmacion from '@/components/layout/DialogoConfirmacion.vue'
import {useConfirmacion} from '@/composables/useConfirmacion'
import {misCotizaciones,decidirCotizacion,type PropuestaCliente} from '@/services/operacionesService'
import {misReservas,pesos,fecha,proximaFecha,nombreCotizacion,type Reserva} from '@/services/reservaService'
const route=useRoute()
const {pregunta,solicitar,responder}=useConfirmacion()
const rows=ref<PropuestaCliente[]>([]),reservas=ref<Reserva[]>([]),error=ref(''),mensaje=ref(''),busy=ref(false),guardando=ref(false)
const errorFila=ref<{id:number;mensaje:string}|null>(null)
const pendiente=computed(()=>reservas.value.filter(r=>r.estadoCotizacion==='BORRADOR'||(r.estadoCotizacion==='VENCIDA'&&!rows.value.some(q=>q.solicitud_id===r.id))))
const reservaDe=(q:PropuestaCliente)=>reservas.value.find(r=>r.id===q.solicitud_id)
function periodoQ(q:PropuestaCliente){const r=reservaDe(q);return r?fecha(r.inicio)+' – '+fecha(r.fin):'—'}
const estadoVisible=(q:PropuestaCliente)=>q.estado==='ENVIADA'&&q.valida_hasta<proximaFecha(0)?'VENCIDA':q.estado
/** Recupera el historial y las solicitudes propias; se verifica la propiedad en Kotlin. */
async function cargar(){busy.value=true;error.value='';try{[rows.value,reservas.value]=await Promise.all([misCotizaciones(),misReservas()])}catch(e){error.value=e instanceof Error?e.message:'Error de consulta'}finally{busy.value=false}}
async function decidir(q:PropuestaCliente,estado:'ACEPTADA'|'RECHAZADA'){
 if(guardando.value||!await solicitar({titulo:estado==='ACEPTADA'?'Aceptar propuesta':'Rechazar propuesta',mensaje:'Confirme su decisión para '+q.codigo+'. No se realizará ningún cobro. Si acepta, administración podrá confirmar la reserva.'}))return
 guardando.value=true;errorFila.value=null;mensaje.value=''
 try{await decidirCotizacion(q.id,estado);await cargar();mensaje.value=estado==='ACEPTADA'?'Propuesta aceptada. Administración puede confirmar su reserva.':'Propuesta rechazada. Administración podrá emitir una nueva versión.'}catch(e){errorFila.value={id:q.id,mensaje:e instanceof Error?e.message:'Error de actualización'}}finally{guardando.value=false}
}
onMounted(cargar)
</script>
<template>
<div class="df-page df-catalog-page"><FleetNav/><main class="df-wrap">
  <div class="df-heading"><div><span class="df-kicker">Centro del cliente</span><h2>Mis cotizaciones<span class="df-highlight">.</span></h2><p>Solicite, revise y decida propuestas económicas de su institución.</p></div><div class="df-actions"><RouterLink class="df-button mini" to="/catalogo">Cotizar un vehículo ↗</RouterLink><button class="df-button dark mini" :disabled="busy" @click="cargar">Actualizar</button></div></div>
  <section class="df-quote-guide"><h3>Cómo funciona</h3><p>1. Elija vehículo y fechas en el catálogo y pulse Cotizar. 2. Administración revisa y publica la propuesta. 3. Acepte o rechace aquí. 4. Administración confirma la reserva; el pago es externo.</p><RouterLink to="/mis-reservas" class="df-button dark mini">Consultar mis reservas</RouterLink></section>
  <p v-if="route.query.creada" class="df-alert ok" role="status">Solicitud {{route.query.creada}} enviada. Consulte su estado en esta página; todavía no se ha confirmado ningún alquiler.</p>
  <p v-if="error" role="alert" class="df-alert error">{{error}}</p><p v-if="mensaje" role="status" class="df-alert ok">{{mensaje}}</p>
  <div v-if="busy" class="df-empty">Consultando sus propuestas…</div>
  <template v-else-if="!error">
    <section v-if="pendiente.length" class="df-quote-guide"><h3>Solicitudes en preparación</h3><div v-for="r in pendiente" :key="r.id" class="df-quote-item"><b>{{r.codigo}} · {{r.vehiculo}}</b><span>{{fecha(r.inicio)}} – {{fecha(r.fin)}} · {{pesos(r.valorEstimado)}} estimados</span><span class="df-status">{{nombreCotizacion(r.estadoCotizacion)}}</span><small>{{r.estadoCotizacion==='VENCIDA'?'La propuesta preliminar venció. Contacte a administración.':'En espera de revisión y publicación por administración.'}}</small></div></section>
    <div v-if="!rows.length" class="df-empty">{{pendiente.length?'Las propuestas publicadas aparecerán aquí.':'Todavía no tiene cotizaciones publicadas. Solicite una desde el catálogo.'}}</div>
    <div v-else class="df-quote-grid"><article v-for="q in rows" :key="q.id" class="df-quote-item df-quote-propuesta"><div><span class="df-kicker">Propuesta {{q.numero_version}}</span><h3>{{q.codigo}}</h3><p>{{reservaDe(q)?.vehiculo}}</p></div><p v-if="reservaDe(q)">{{periodoQ(q)}}</p><span class="df-status">{{nombreCotizacion(estadoVisible(q))}}</span><p>Vigente hasta {{q.valida_hasta}}</p><dl><dt>Subtotal</dt><dd>{{pesos(q.subtotal)}}</dd><dt>Impuestos</dt><dd>{{pesos(q.impuestos)}}</dd><dt>Descuentos</dt><dd>{{pesos(q.descuentos)}}</dd><dt><b>Total estimado</b></dt><dd><b>{{pesos(q.valor_total)}}</b></dd></dl><p v-if="q.observaciones">{{q.observaciones}}</p><div v-if="q.decidible" class="df-actions"><button class="df-button mini" :disabled="guardando" @click="decidir(q,'ACEPTADA')">Aceptar propuesta</button><button class="df-button dark mini" :disabled="guardando" @click="decidir(q,'RECHAZADA')">Rechazar</button></div><RouterLink v-else-if="q.estado==='ACEPTADA'" class="df-button light mini" to="/mis-reservas">Ver estado del alquiler</RouterLink><p v-else class="df-meta">Sin decisión disponible.</p><p v-if="errorFila?.id===q.id" role="alert" class="df-alert error">{{errorFila.mensaje}}</p></article></div>
  </template>
  <p class="df-meta">Aceptar una cotización no constituye un pago ni reemplaza la confirmación administrativa.</p>
</main><DialogoConfirmacion v-if="pregunta" :pregunta="pregunta" @responder="responder" /></div>
</template>
