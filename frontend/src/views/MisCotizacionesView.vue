<script setup lang="ts">
/** Portal de propuestas: permite decidir exclusivamente cotizaciones de la institución autenticada. */
import {ref,onMounted} from 'vue'
import FleetNav from '@/components/layout/FleetNav.vue'
import DialogoConfirmacion from '@/components/layout/DialogoConfirmacion.vue'
import {useConfirmacion} from '@/composables/useConfirmacion'
import {misCotizaciones,decidirCotizacion,type PropuestaCliente} from '@/services/operacionesService'
import {pesos} from '@/services/reservaService'
const {pregunta,solicitar,responder}=useConfirmacion()
const rows=ref<PropuestaCliente[]>([]),error=ref(''),mensaje=ref(''),busy=ref(false),guardando=ref(false)
/** Recupera propuestas publicadas, omitiendo borradores administrativos. */
async function cargar(){busy.value=true;error.value='';try{rows.value=await misCotizaciones()}catch(e){error.value=e instanceof Error?e.message:'Error de consulta'}finally{busy.value=false}}
/** Registra la decisión comercial; no confirma el alquiler ni ejecuta cobros. */
async function decidir(q:PropuestaCliente,estado:'ACEPTADA'|'RECHAZADA'){
 if(guardando.value||!await solicitar({titulo:estado==='ACEPTADA'?'Aceptar propuesta':'Rechazar propuesta',mensaje:`Confirme la decisión ${estado} para ${q.codigo}. No se realizará un cobro.`}))return
 guardando.value=true;try{await decidirCotizacion(q.id,estado);await cargar();mensaje.value='Decisión registrada correctamente.'}catch(e){error.value=e instanceof Error?e.message:'Error de actualización'}finally{guardando.value=false}}
onMounted(cargar)
</script>
<template><div class="df-page"><FleetNav/><main class="df-wrap"><div class="df-heading"><div><span class="df-kicker">Centro del cliente</span><h2>Mis cotizaciones<span class="df-highlight">.</span></h2><p>Propuestas económicas de su institución.</p></div><button class="df-button dark mini" @click="cargar">Actualizar</button></div><p v-if="error" role="alert" class="df-alert error">{{error}}</p><p v-if="mensaje" role="status" class="df-alert ok">{{mensaje}}</p><div v-if="busy" class="df-empty">Consultando propuestas…</div><div v-else-if="!rows.length" class="df-empty">No hay propuestas publicadas.</div><div v-else class="df-panel df-tablescroll"><table class="df-table"><thead><tr><th>Solicitud</th><th>Versión</th><th>Vigencia</th><th>Valor</th><th>Estado</th><th>Decisión</th></tr></thead><tbody><tr v-for="q in rows" :key="q.id"><td>{{q.codigo}}<p class="df-meta">{{q.observaciones}}</p></td><td>{{q.numero_version}}</td><td>{{q.valida_hasta}}</td><td>{{pesos(q.valor_total)}}</td><td><span class="df-status">{{q.estado}}</span></td><td><div v-if="q.decidible" class="df-actions"><button :disabled="guardando" @click="decidir(q,'ACEPTADA')">Aceptar</button><button :disabled="guardando" @click="decidir(q,'RECHAZADA')">Rechazar</button></div><span v-else>Sin acción disponible</span></td></tr></tbody></table></div><p class="df-meta">Aceptar una cotización no constituye un pago ni reemplaza la confirmación administrativa.</p></main><DialogoConfirmacion v-if="pregunta" :pregunta="pregunta" @responder="responder" /></div></template>
