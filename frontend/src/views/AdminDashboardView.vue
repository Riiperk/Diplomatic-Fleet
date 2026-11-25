<script setup lang="ts">
/** Componente AdminDashboard: vista navegable conectada con las rutas y servicios del sistema. */
import {ref,computed,onMounted} from 'vue'
import DemoLayout from '@/components/layout/DemoLayout.vue'
import {listarVehiculos,type VehiculoResponse} from '@/services/vehiculoService'
import {listarClientes,type ClienteInstitucionalResponse} from '@/services/clienteService'
import {todasReservas,pesos,fecha,type Reserva} from '@/services/reservaService'
const cars=ref<VehiculoResponse[]>([]),instituciones=ref<ClienteInstitucionalResponse[]>([]),reservas=ref<Reserva[]>([])
const busy=ref(false),error=ref('')
const pendientes=computed(()=>reservas.value.filter(r=>['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION'].includes(r.estado)).length)
const valor=computed(()=>reservas.value.filter(r=>['CONFIRMADA','ASIGNADA','EN_SERVICIO','FINALIZADA'].includes(r.estado)).reduce((a,r)=>a+r.valorEstimado,0))
/** Carga en paralelo el inventario, las instituciones y las solicitudes. */
async function cargar(){busy.value=true;error.value=''
 try{const result=await Promise.all([listarVehiculos(),listarClientes(),todasReservas()]);cars.value=result[0];instituciones.value=result[1];reservas.value=result[2]}
 catch(e){error.value=e instanceof Error?e.message:'No se pudo cargar el panel'}finally{busy.value=false}}
onMounted(cargar)
</script>
<template><DemoLayout module-number="01" eyebrow="Centro de control" title="Panel principal" description="Información actualizada desde los módulos operativos de Diplomatic Fleet."><div>
<div class="df-heading"><div><span class="df-kicker">Visión global</span><h2>Operación de hoy<span class="df-highlight">.</span></h2><p>Resumen calculado a partir de datos registrados, no cifras simuladas.</p></div><button class="df-button dark mini" @click="cargar">{{busy?'Cargando…':'Actualizar datos ↗'}}</button></div>
<p v-if="error" class="df-alert error">{{error}}</p>
<div class="df-overview"><RouterLink to="/vehiculos" class="df-tile"><span>01 / FLOTA</span><strong>{{cars.length}}</strong><small>Vehículos registrados</small></RouterLink><RouterLink to="/clientes" class="df-tile"><span>02 / CLIENTES</span><strong>{{instituciones.length}}</strong><small>Instituciones registradas</small></RouterLink><RouterLink to="/solicitudes" class="df-tile"><span>03 / POR REVISAR</span><strong>{{pendientes}}</strong><small>Solicitudes pendientes</small></RouterLink><RouterLink to="/solicitudes" class="df-tile"><span>04 / VALOR ESTIMADO</span><strong style="font-size:35px">{{pesos(valor)}}</strong><small>Reservas confirmadas, asignadas, en servicio y finalizadas; no ingresos cobrados</small></RouterLink></div>
<div class="df-panel" style="margin-top:28px"><div class="df-heading"><div><span class="df-kicker">Movimiento reciente</span><h2>Últimas solicitudes.</h2></div><RouterLink class="df-button mini" to="/solicitudes">Ver todas →</RouterLink></div><p v-if="!reservas.length" class="df-meta">Aún no existen solicitudes.</p><div v-else class="df-tablescroll"><table class="df-table"><thead><tr><th>Código</th><th>Institución</th><th>Vehículo</th><th>Inicio</th><th>Estado</th></tr></thead><tbody><tr v-for="r in reservas.slice(0,6)" :key="r.id"><td><b>{{r.codigo}}</b></td><td>{{r.cliente}}</td><td>{{r.vehiculo}}</td><td>{{fecha(r.inicio)}}</td><td><span class="df-status" :class="r.estado">{{r.estado.replaceAll('_',' ')}}</span></td></tr></tbody></table></div></div><p class="df-meta" style="margin-top:20px">Documentos, mantenimientos, conductores, cotizaciones, usuarios y reportes tienen consultas y acciones conectadas a PostgreSQL. Facturación electrónica y acreditación diplomática siguen fuera del alcance operativo de este prototipo.</p>
</div></DemoLayout></template>
<style scoped>.df-overview{display:grid;grid-template-columns:repeat(4,1fr);gap:18px}.df-tile{min-height:150px;padding:20px;background:#161616;color:white;box-shadow:5px 5px #df273c;display:flex;flex-direction:column}.df-tile:hover{transform:translateY(-3px)}.df-tile span{font-size:11px;font-weight:900;color:#f34b5a;letter-spacing:.08em}.df-tile strong{font:900 55px 'Barlow Condensed',Impact;margin:auto 0 7px}.df-tile small{font-size:11px;font-weight:800;color:#aaa}@media(max-width:950px){.df-overview{grid-template-columns:repeat(2,1fr)}}@media(max-width:540px){.df-overview{grid-template-columns:1fr 1fr;gap:12px}.df-tile{padding:12px}.df-tile strong{font-size:39px}}</style>
