<script setup lang="ts">
/** Componente MisReservas: vista navegable conectada con las rutas y servicios del sistema. */
import {onMounted,ref} from 'vue'
import FleetNav from '@/components/layout/FleetNav.vue'
import DialogoBase from '@/components/layout/DialogoBase.vue'
import DialogoConfirmacion from '@/components/layout/DialogoConfirmacion.vue'
import {useConfirmacion} from '@/composables/useConfirmacion'
import {misReservas,cancelarReserva,pesos,fecha,reportarPago,reprogramarReserva,isoFecha,fechaBogota,proximaFecha,nombreMetodo,nombrePago,type Reserva} from '@/services/reservaService'
const {pregunta,solicitar,responder}=useConfirmacion()
const operando=ref(false)
const rows=ref<Reserva[]>([]),busy=ref(false),error=ref(''),message=ref('')
/** Recupera únicamente las reservas de la institución autenticada. */
async function cargar(){busy.value=true;error.value='';try{rows.value=await misReservas()}catch(e){error.value=e instanceof Error?e.message:'Error al consultar'}finally{busy.value=false}}
/** Solicita el motivo y anula solo solicitudes con estado permitido. */
async function cancelar(r:Reserva){if(operando.value)return;const motivo=await solicitar({titulo:'Cancelar solicitud',mensaje:`La solicitud ${r.codigo} quedará cancelada. Esta acción libera el periodo si no hay otros bloqueos.`,campo:'Motivo de cancelación',minimo:4,maximo:500});if(!motivo)return
  operando.value=true;try{await cancelarReserva(r.id,motivo);message.value=`Solicitud ${r.codigo} cancelada.`;await cargar()}catch(e){error.value=e instanceof Error?e.message:'Error cancelando'}finally{operando.value=false}}
/** El usuario declara la referencia de una transferencia; el administrador la verificara. */
async function comunicarTransferencia(r:Reserva){
 if(operando.value)return
 const referencia=await solicitar({titulo:'Informar transferencia',mensaje:'Registre únicamente la referencia de una transferencia de prueba o realizada fuera de la plataforma. No introduzca contraseñas bancarias.',campo:'Referencia',minimo:4,maximo:120})
 if(!referencia)return
 operando.value=true;try{await reportarPago(r.id,referencia);message.value=`Referencia comunicada para ${r.codigo}. En espera de revision.`;await cargar()}
 catch(e){error.value=e instanceof Error?e.message:'No se pudo registrar la referencia'}finally{operando.value=false}
}
/** Abre un formulario de fechas para solicitudes que aún pueden modificarse. */
const editando=ref<Reserva|null>(null),nuevoInicio=ref(''),nuevoFin=ref(''),errorPeriodo=ref('')
function abrirPeriodo(r:Reserva){editando.value=r;nuevoInicio.value=fechaBogota(r.inicio);nuevoFin.value=fechaBogota(r.fin);errorPeriodo.value=''}
/** Solicita reprogramación y conserva el diálogo si el servidor detecta un conflicto. */
async function guardarPeriodo(){
 if(!editando.value||operando.value)return
 if(nuevoFin.value<=nuevoInicio.value){errorPeriodo.value='La devolución debe ser posterior a la recogida.';return}
 operando.value=true;errorPeriodo.value=''
 try{const r=await reprogramarReserva(editando.value.id,isoFecha(nuevoInicio.value),isoFecha(nuevoFin.value));editando.value=null;message.value=`Periodo de ${r.codigo} actualizado. Nuevo estimado: ${pesos(r.valorEstimado)}.`;await cargar()}
 catch(e){errorPeriodo.value=e instanceof Error?e.message:'No se pudo modificar el periodo'}finally{operando.value=false}
}
onMounted(cargar)
</script>
<template><div class="df-page"><FleetNav/><main class="df-wrap"><RouterLink to="/catalogo" class="df-topback">← Volver al catálogo</RouterLink><div class="df-heading"><div><span class="df-kicker">Centro del cliente</span><h2>Mis solicitudes<span class="df-highlight">.</span></h2><p>Consulta el estado de tus reservas institucionales.</p></div><div class="df-actions"><RouterLink class="df-button mini" to="/mis-cotizaciones">Cotizaciones</RouterLink><button class="df-button dark mini" @click="cargar">Actualizar ↗</button></div></div><p v-if="error" class="df-alert error">{{error}}</p><p v-if="message" class="df-alert ok">{{message}}</p>
 <div v-if="busy" class="df-empty">Consultando solicitudes…</div><div v-else-if="!rows.length" class="df-empty">Aún no tienes solicitudes. <RouterLink to="/catalogo"><u>Explora vehículos aquí</u></RouterLink>.</div>
 <div v-else class="df-panel df-tablescroll"><table class="df-table"><thead><tr><th>Código</th><th>Vehículo</th><th>Fechas</th><th>Estado</th><th>Estimado</th><th>Pago externo</th><th>Acción</th></tr></thead><tbody><tr v-for="r in rows" :key="r.id"><td><b>{{r.codigo}}</b></td><td>{{r.vehiculo||'Sin asignar'}}<br><small>{{r.modalidad==='CON_CONDUCTOR'?'Con conductor':'Sin conductor'}}</small><br><small v-if="r.conductor">{{r.conductor}}</small><small v-if="r.requiereBlindaje"> · Requiere blindaje</small></td><td>{{fecha(r.inicio)}} – {{fecha(r.fin)}}<br><small>{{r.recogida}} → {{r.devolucion}}</small><br><small v-if="r.fechaInicioReal">Entregado: {{fecha(r.fechaInicioReal)}}</small><br><small v-if="r.fechaFinReal">Devuelto: {{fecha(r.fechaFinReal)}}</small></td><td><span class="df-status" :class="r.estado">{{r.estado.replaceAll('_',' ')}}</span></td><td>{{pesos(r.valorEstimado)}}</td>
 <td><b>{{nombreMetodo(r.metodoPago)}}</b><br><small>{{nombrePago(r.estadoPago)}}</small><br><small v-if="r.estadoPago==='REEMBOLSO_PENDIENTE'">Contacta a administración para devolución manual.</small></td>
 <td><div class="df-actions"><button v-if="r.editable" :disabled="operando" @click="abrirPeriodo(r)">Modificar fechas</button>
 <button v-if="r.metodoPago==='TRANSFERENCIA' && ['PENDIENTE','RECHAZADO','REPORTADO'].includes(r.estadoPago||'') && !['CANCELADA','RECHAZADA','FINALIZADA'].includes(r.estado)" :disabled="operando" @click="comunicarTransferencia(r)">Informar pago</button>
 <button v-if="['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION','CONFIRMADA','ASIGNADA'].includes(r.estado) && new Date(r.inicio)>new Date()" class="df-button mini" :disabled="operando" @click="cancelar(r)">Cancelar</button>
 </div></td></tr></tbody></table></div><p class="df-meta">El estado de pago se verifica manualmente y no representa cargos efectuados por la página. Solo se puede cancelar antes de la recogida; una reserva iniciada requiere gestión de administración.</p></main><DialogoBase v-if="editando" titulo="Modificar periodo" :ocupado="operando" @cerrar="editando=null">
 <p class="df-meta">Solicitud {{editando.codigo}}. El servidor comprobará el calendario y recalculará el importe con la tarifa vigente. Solo se modifican solicitudes pendientes sin cotización ni pago reportado.</p>
 <form @submit.prevent="guardarPeriodo"><div class="df-fields"><label class="df-field df-dialog-field">Recogida<input v-model="nuevoInicio" type="date" :min="proximaFecha(1)" required /></label><label class="df-field df-dialog-field">Devolución<input v-model="nuevoFin" type="date" :min="nuevoInicio" required /></label></div><p v-if="errorPeriodo" role="alert" class="df-alert error">{{errorPeriodo}}</p><button class="df-button" :disabled="operando" type="submit">{{operando?'Guardando…':'Guardar nuevo periodo'}}</button></form>
 </DialogoBase><DialogoConfirmacion v-if="pregunta" :pregunta="pregunta" @responder="responder" /></div></template>
