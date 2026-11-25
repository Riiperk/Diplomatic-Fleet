<script setup lang="ts">
/** Historial técnico de cambios persistidos desde V10. No contiene datos sensibles ni reconstruye eventos anteriores. */
import {ref,computed,onMounted} from 'vue'
import DemoLayout from '@/components/layout/DemoLayout.vue'
import {api} from '@/services/api'
interface Evento {id:number;tabla:string;registro_id:number;operacion:string;estado_anterior:string|null;estado_nuevo:string|null;fecha:string;usuario_bd:string}
const rows=ref<Evento[]>([]),error=ref(''),buscando=ref(false),filtro=ref('')
const filtrados=computed(()=>rows.value.filter(r=>[r.tabla,r.registro_id,r.operacion,r.estado_nuevo].join(' ').toLowerCase().includes(filtro.value.toLowerCase())))
/** Solicita el historial al endpoint restringido al administrador. */
async function cargar(){buscando.value=true;error.value='';try{rows.value=await api<Evento[]>('/api/admin/auditoria')}catch(e){error.value=e instanceof Error?e.message:'Error de consulta'}finally{buscando.value=false}}
onMounted(cargar)
</script>
<template><DemoLayout module-number="12" eyebrow="Control de cambios" title="Auditoría" description="Historial técnico de operaciones registradas en PostgreSQL desde V10."><section class="df-panel"><div class="df-heading"><h2>Historial de cambios.</h2><button class="df-button dark mini" :disabled="buscando" @click="cargar">Actualizar</button></div><label class="df-field">Filtrar eventos<input v-model="filtro" type="search" placeholder="Tabla, registro, operación o estado"/></label><p v-if="error" class="df-alert error" role="alert">{{error}}</p><div v-if="buscando" class="df-empty">Consultando historial…</div><div v-else-if="!filtrados.length" class="df-empty">No hay eventos que coincidan.</div><div v-else class="df-tablescroll"><table class="df-table"><thead><tr><th>Fecha</th><th>Tabla</th><th>Registro</th><th>Operación</th><th>Anterior</th><th>Nuevo</th><th>Cuenta BD</th></tr></thead><tbody><tr v-for="r in filtrados" :key="r.id"><td>{{r.fecha}}</td><td>{{r.tabla}}</td><td>{{r.registro_id}}</td><td>{{r.operacion}}</td><td>{{r.estado_anterior||'—'}}</td><td>{{r.estado_nuevo||'—'}}</td><td>{{r.usuario_bd}}</td></tr></tbody></table></div><p class="df-meta">Últimos 500 eventos técnicos. La cuenta de base de datos no identifica por sí sola al usuario web. Las acciones anteriores a V10 no se reconstruyen.</p></section></DemoLayout></template>
