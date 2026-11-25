<script setup lang="ts">
/** Agenda privada del conductor profesional; la API filtra por el usuario autenticado. */
import {ref,onMounted} from 'vue'
import FleetNav from '@/components/layout/FleetNav.vue'
import {misAsignaciones,type Asignacion} from '@/services/operacionesService'
import {fecha} from '@/services/reservaService'
const rows=ref<Asignacion[]>([]),busy=ref(false),error=ref('')
/** Consulta los servicios sin exponer información de pago o cuentas de clientes. */
async function cargar(){busy.value=true;error.value='';try{rows.value=await misAsignaciones()}catch(e){error.value=e instanceof Error?e.message:'Error de consulta'}finally{busy.value=false}}
onMounted(cargar)
</script>
<template><div class="df-page"><FleetNav/><main class="df-wrap"><div class="df-heading"><div><span class="df-kicker">Operación profesional</span><h2>Servicios asignados<span class="df-highlight">.</span></h2><p>Agenda de recogida y devolución de vehículos.</p></div><button class="df-button dark mini" @click="cargar">Actualizar</button></div><p v-if="error" class="df-alert error" role="alert">{{error}}</p><div v-if="busy" class="df-empty">Consultando servicios…</div><div v-else-if="!rows.length" class="df-empty">No hay servicios asignados.</div><div v-else class="df-grid"><article v-for="(r,i) in rows" :key="r.id" class="df-card" :style="{'--df-delay':Math.min(i,9)*45+'ms'}"><div class="df-card-body"><span class="df-pill">{{r.codigo}}</span><h3>{{r.vehiculo}}</h3><p>{{fecha(r.inicio)}} – {{fecha(r.fin)}}</p><p><b>Recogida:</b> {{r.recogida}}</p><p><b>Devolución:</b> {{r.devolucion}}</p><span class="df-status" :class="r.estado">{{r.estado}}</span></div></article></div></main></div></template>
