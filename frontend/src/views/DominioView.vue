<script setup lang="ts">
/**
 * Consola administrativa del modelo diplomático. Los formularios y tablas
 * utilizan un catálogo de campos explícito, con claves foráneas seleccionables.
 * No se permite SQL desde el navegador. Las validaciones definitivas son REST/BD.
 */
import {computed,onMounted,ref} from 'vue'
import DemoLayout from '@/components/layout/DemoLayout.vue'
import {cargarDominio,guardarDominio,type DatosDominio,type RegistroDominio} from '@/services/dominioService'

type Opcion = {value:string,label:string}
type Campo = {key:string,label:string,type?:'date'|'text'|'select'|'boolean',source?:string,required?:boolean,max?:number,options?:Opcion[]}
type Modulo = {title:string,resource:string,operation:string,id:string,fields:Campo[],columns:{key:string,label:string}[],existingOnly?:boolean,readOnly?:boolean}
const f=(key:string,label:string,required=false,max=255):Campo=>({key,label,required,max,type:'text'})
const sel=(key:string,label:string,source:string,required=false):Campo=>({key,label,source,required,type:'select'})
const date=(key:string,label:string):Campo=>({key,label,type:'date'})
const bool=(key:string,label:string):Campo=>({key,label,type:'boolean'})
const col=(key:string,label:string)=>({key,label})
const modulos:Modulo[]=[
 {title:'Personas',resource:'personas',operation:'persona',id:'id_persona',existingOnly:true,fields:[sel('id_persona','Persona','personas',true),f('seg_nom_per','Segundo nombre',false,100),f('seg_ape_per','Segundo apellido',false,100),date('fech_nac_per','Fecha de nacimiento'),sel('fk_id_tip_doc','Tipo de documento','cat_tipo_doc_persona'),f('documento_per','Documento principal',false,80),sel('fk_id_gen','Género','cat_genero'),sel('fk_id_ciu','Ciudad','ciudades')],columns:[col('id_persona','ID'),col('pri_nom_per','Nombre'),col('pri_ape_per','Apellido'),col('documento_per','Documento'),col('act_per','Activo')]},
 {title:'Acreditaciones',resource:'clientes',operation:'acreditacion',id:'id_cliente',existingOnly:true,fields:[sel('id_cliente','Institución','clientes',true),sel('fk_id_tip_cli','Tipo de cliente','cat_tipo_cliente',true),sel('fk_id_pais_acred','País acreditante','paises'),sel('fk_id_rango','Rango','cat_rango_diplomatico'),f('num_cedula_diplo','Cédula diplomática',false,80),date('fech_acreditacion','Fecha de acreditación'),date('fech_venc_acred','Vencimiento'),sel('fk_id_est_acred','Estado','cat_estado_acreditacion',true)],columns:[col('nom_mision','Institución'),col('fk_id_tip_mision','Tipo de entidad'),col('fk_id_rango','Rango'),col('fech_venc_acred','Vencimiento'),col('estado_efectivo','Estado efectivo')]},
 {title:'Documentos personales',resource:'documentos',operation:'documento',id:'id_doc_persona',fields:[sel('fk_id_persona','Persona','personas',true),sel('fk_id_tip_doc_per','Tipo documental','cat_tipo_doc_persona',true),f('num_doc','Número',true,80),date('fech_expedicion','Expedición'),date('fech_vencimiento','Vencimiento'),f('archivo_url','URL HTTPS de referencia',false,800),bool('act_doc','Activo')],columns:[col('fk_id_persona','Persona'),col('fk_id_tip_doc_per','Tipo'),col('num_doc','Número'),col('fech_vencimiento','Vencimiento'),col('act_doc','Activo')]},
 {title:'Direcciones',resource:'direcciones',operation:'direccion',id:'id_direccion',fields:[sel('fk_id_persona','Persona','personas',true),f('dir_fisica','Dirección',true),sel('fk_id_ciu','Ciudad','ciudades'),bool('es_principal','Principal'),bool('act_direccion','Activa')],columns:[col('fk_id_persona','Persona'),col('dir_fisica','Dirección'),col('fk_id_ciu','Ciudad ID'),col('es_principal','Principal'),col('act_direccion','Activa')]},
 {title:'Contactos',resource:'contactos',operation:'contacto',id:'id_contacto',fields:[sel('fk_id_persona','Persona','personas',true),{key:'tipo_contacto',label:'Tipo',type:'select',required:true,options:['CORREO','TELEFONO','OTRO'].map(value=>({value,label:value}))},f('valor_contacto','Valor',true),bool('es_principal','Principal'),bool('act_contacto','Activo')],columns:[col('fk_id_persona','Persona'),col('tipo_contacto','Tipo'),col('valor_contacto','Contacto'),col('es_principal','Principal'),col('act_contacto','Activo')]},
 {title:'Países',resource:'paises',operation:'pais',id:'id_pais',fields:[f('id_pais','Código de país',true,3),f('nombre','Nombre',true,100)],columns:[col('id_pais','Código'),col('nombre','País')]},
 {title:'Ciudades',resource:'ciudades',operation:'ciudad',id:'id_ciudad',fields:[sel('fk_id_pais','País','paises',true),f('nombre','Ciudad',true,100)],columns:[col('id_ciudad','ID'),col('nombre','Ciudad'),col('fk_id_pais','País')]},
 {title:'Talleres',resource:'talleres',operation:'taller',id:'id_taller',fields:[f('nom_taller','Nombre (referencia operativa)',true,150),f('nit_taller','NIT',false,40),f('dir_taller','Dirección'),sel('fk_id_ciu','Ciudad','ciudades'),f('tel_taller','Teléfono',false,30),bool('act_taller','Activo')],columns:[col('nom_taller','Nombre'),col('nit_taller','NIT'),col('dir_taller','Dirección'),col('act_taller','Activo')]},
 {title:'Ubicaciones',resource:'ubicaciones',operation:'ubicacion',id:'id_ubicacion',fields:[f('nom_ubicacion','Nombre (origen / destino)',true),f('tipo_ubicacion','Tipo de ubicación',false,50),f('dir_ubicacion','Dirección'),sel('fk_id_ciu','Ciudad','ciudades'),bool('act_ubicacion','Activa'),bool('publica','Visible en sugerencias públicas (no use direcciones privadas)')],columns:[col('nom_ubicacion','Nombre'),col('tipo_ubicacion','Tipo'),col('dir_ubicacion','Dirección'),col('act_ubicacion','Activa'),col('publica','Pública')]},
 {title:'Conductores',resource:'conductores',operation:'conductor',id:'id_conductor',existingOnly:true,fields:[sel('id_conductor','Conductor','conductores',true),bool('es_diplomatico','Conductor diplomático')],columns:[col('id_conductor','ID'),col('nombre','Conductor'),col('fk_id_cat_lic','Categoría'),col('fech_venc_licencia','Vencimiento'),col('es_diplomatico','Diplomático')]},
 {title:'Vehículo y blindaje',resource:'vehiculos',operation:'blindaje',id:'id',existingOnly:true,fields:[sel('id','Vehículo','vehiculos',true),sel('nivel_blindaje','Nivel declarado','cat_nivel_blindaje',true),sel('tipo_vehiculo','Tipo de vehículo','cat_tipo_vehiculo',true),sel('proveedor_id','Proveedor','proveedores')],columns:[col('placa','Placa'),col('marca','Marca'),col('modelo','Modelo'),col('nivel_blindaje','Blindaje'),col('tipo_vehiculo','Tipo'),col('estado_admin','Estado')]},
 {title:'Proveedores',resource:'proveedores',operation:'proveedor',id:'id_proveedor',fields:[f('nombre','Nombre',true,150),bool('activo','Activo')],columns:[col('nombre','Proveedor'),col('activo','Activo')]},
 {title:'Bajas de flota',resource:'bajas',operation:'baja',id:'id_baja',fields:[sel('fk_id_vehiculo','Vehículo','vehiculos',true),sel('fk_id_tip_baja','Tipo de baja','cat_tipo_baja',true),f('motivo','Motivo',true,500),f('observaciones','Observaciones',false,500)],columns:[col('placa','Placa'),col('fecha_baja','Fecha'),col('motivo','Motivo'),col('fk_id_tip_baja','Tipo'),col('fk_id_usu','Usuario ID')]},
]
const datos=ref<DatosDominio|null>(null),indice=ref(0),form=ref<Record<string,string>>({}),mensaje=ref(''),error=ref(false),ocupado=ref(false),busqueda=ref(''),verCatalogos=ref(false),confirmarBaja=ref(false)
const modulo=computed(()=>modulos[indice.value]!)
const registros=computed(()=>datos.value?.[modulo.value.resource]??[])
const filas=computed(()=>registros.value.filter(r=>Object.values(r).join(' ').toLowerCase().includes(busqueda.value.toLowerCase())))
const ids:Record<string,string>={personas:'id_persona',paises:'id_pais',ciudades:'id_ciudad',clientes:'id_cliente',vehiculos:'id',conductores:'id_conductor',proveedores:'id_proveedor'}
/** Opciones extraídas de tablas normalizadas, nunca de IDs ficticios. */
function opciones(c:Campo):Opcion[]{
 if(c.options)return c.options
 const source=c.source||''
 if(source.startsWith('cat_'))return (datos.value?.catalogos[source]??[]).map(r=>({value:String(r.codigo),label:String(r.codigo).replaceAll('_',' ')}))
 return (datos.value?.[source]??[]).map(r=>({value:String(r[ids[source]||'id']),label: source==='personas'?`${r.pri_nom_per??''} ${r.pri_ape_per??''} · ${r.id_persona}`:source==='clientes'?String(r.nom_mision):source==='vehiculos'?`${r.placa} · ${r.marca} ${r.modelo} · ${r.estado_admin}`:source==='ciudades'?`${r.nombre} · ${r.fk_id_pais??'País pendiente'}`:source==='conductores'?`${r.nombre} · ${r.id_conductor}`:String(r.nombre)}))
}
/** Restablece campos al cambiar de módulo, conservando únicamente la sesión. */
function nuevo(){form.value=Object.fromEntries(modulo.value.fields.map(c=>[c.key,c.type==='boolean'?((c.key.startsWith('act_')||c.key==='activo')?'true':'false'):'']));confirmarBaja.value=false}
function cambiar(i:number){indice.value=i;busqueda.value='';mensaje.value='';verCatalogos.value=false;nuevo()}
/** Copia los datos del registro para edición; la baja no se puede modificar. */
function editar(r:RegistroDominio){if(modulo.value.operation==='baja')return;nuevo();form.value[modulo.value.id]=String(r[modulo.value.id]??'');for(const c of modulo.value.fields)if(r[c.key]!==null&&r[c.key]!==undefined)form.value[c.key]=String(r[c.key]);confirmarBaja.value=false}
function cambiarCampo(c:Campo){if(modulo.value.existingOnly&&c.key===modulo.value.id){const r=registros.value.find(x=>String(x[modulo.value.id])===form.value[c.key]);if(r)editar(r)}}
function fijo(c:Campo):boolean{return !!form.value[modulo.value.id]&&!modulo.value.existingOnly&&((modulo.value.operation==='taller'&&c.key==='nom_taller')||(modulo.value.operation==='ubicacion'&&c.key==='nom_ubicacion')||(['documento','direccion','contacto'].includes(modulo.value.operation)&&c.key==='fk_id_persona'))}
async function cargar(){ocupado.value=true;try{datos.value=await cargarDominio()}catch(e){error.value=true;mensaje.value=e instanceof Error?e.message:'No fue posible consultar el modelo'}finally{ocupado.value=false}}
async function guardar(){if(ocupado.value)return;if(modulo.value.operation==='baja'&&!confirmarBaja.value){error.value=true;mensaje.value='Confirme la baja definitiva';return}ocupado.value=true;mensaje.value='';error.value=false;try{await guardarDominio(modulo.value.operation,form.value);await cargar();if(!error.value){mensaje.value='Registro guardado correctamente.';nuevo()}}catch(e){error.value=true;mensaje.value=e instanceof Error?e.message:'No fue posible guardar'}finally{ocupado.value=false}}
function mostrar(v:unknown):string{return v===true?'Sí':v===false?'No':v===null||v===undefined||v===''?'Sin dato':String(v)}
onMounted(()=>{nuevo();void cargar()})
</script>
<template>
 <DemoLayout module-number="13" eyebrow="Administración institucional" title="Modelo diplomático" description="Personas, acreditaciones, geografía, blindaje y retiro de unidades.">
  <div class="df-panel dominio-panel">
   <p class="df-meta">Los datos históricos sin información acreditada se mantienen como pendientes. Utilice únicamente registros ficticios en la demostración pública. El nivel de blindaje es una declaración administrativa, no una certificación.</p>
   <nav class="df-actions dominio-nav" aria-label="Módulos de datos diplomáticos">
    <button v-for="(m,i) in modulos" :key="m.operation" type="button" :aria-pressed="indice===i&&!verCatalogos" @click="cambiar(i)">{{m.title}}</button>
    <button type="button" :aria-pressed="verCatalogos" @click="verCatalogos=!verCatalogos">Catálogos</button>
   </nav>
   <p v-if="mensaje" :class="['df-alert',error?'error':'ok']" role="status">{{mensaje}}</p>
   <button class="df-button mini" type="button" :disabled="ocupado" @click="cargar">{{ocupado?'Consultando…':'Actualizar datos'}}</button>
   <section v-if="verCatalogos && datos" class="dominio-catalogos"><h2>Catálogos de clasificación</h2><details v-for="(items,nombre) in datos.catalogos" :key="nombre"><summary>{{String(nombre).replaceAll('_',' ')}} · {{items.length}}</summary><p>{{items.map(i=>i.codigo).join(' · ')}}</p></details></section>
   <template v-else>
    <h2>{{modulo.title}}</h2>
    <p v-if="modulo.existingOnly" class="df-meta">Seleccione un registro existente. Las altas de cuentas, clientes y conductores se realizan en sus módulos originales.</p>
    <p v-if="modulo.operation==='taller'||modulo.operation==='ubicacion'" class="df-meta">El nombre de referencia no se modifica al editar, para mantener los historiales existentes.</p>
    <form class="dominio-form" @submit.prevent="guardar">
     <div class="df-fields">
      <label v-for="c in modulo.fields" :key="c.key" class="df-field">{{c.label}}
       <select v-if="c.type==='select'" v-model="form[c.key]" :required="c.required" :disabled="fijo(c)||ocupado" @change="cambiarCampo(c)"><option value="">Seleccione</option><option v-for="o in opciones(c)" :key="o.value" :value="o.value">{{o.label}}</option></select>
       <select v-else-if="c.type==='boolean'" v-model="form[c.key]" :disabled="ocupado"><option value="true">Sí</option><option value="false">No</option></select>
       <input v-else v-model="form[c.key]" :type="c.type==='date'?'date':'text'" :maxlength="c.max" :required="c.required" :readonly="fijo(c)" :disabled="ocupado" />
      </label>
     </div>
     <label v-if="modulo.operation==='baja'" class="df-field"><span>Confirmación de retiro irreversible</span><span><input v-model="confirmarBaja" type="checkbox" required /> Confirmo que se retirará la unidad del catálogo y no podrá reactivarse.</span></label>
     <div class="df-actions"><button class="df-button" :disabled="ocupado" type="submit">{{modulo.operation==='baja'?'Registrar baja':'Guardar registro'}}</button><button type="button" @click="nuevo" :disabled="ocupado">Limpiar formulario</button></div>
    </form>
    <label class="df-field dominio-busqueda">Buscar en registros<input v-model="busqueda" type="search" placeholder="Nombre, código o estado" /></label>
    <div class="df-table-wrap"><table class="df-table"><thead><tr><th v-for="c in modulo.columns" :key="c.key">{{c.label}}</th><th v-if="modulo.operation!=='baja'">Acción</th></tr></thead><tbody><tr v-for="r in filas" :key="String(r[modulo.id])"><td v-for="c in modulo.columns" :key="c.key">{{mostrar(r[c.key])}}</td><td v-if="modulo.operation!=='baja'"><button class="df-button mini" type="button" :disabled="ocupado" @click="editar(r)">Editar</button></td></tr></tbody></table><p v-if="!filas.length" class="df-meta">No hay registros para mostrar.</p></div>
   </template>
  </div>
 </DemoLayout>
</template>
<style scoped>
/* Se reutilizan la composición, formularios y colores de administración V12. */
.dominio-panel{padding:24px;overflow:hidden}.dominio-panel h2{margin:22px 0 14px;text-transform:uppercase;font-weight:900}.dominio-nav{margin:18px 0;gap:8px}.dominio-nav button[aria-pressed='true']{background:#d71920;color:white}.dominio-form{border-block:2px solid #ded9d3;padding:16px 0;margin-bottom:20px}.dominio-busqueda{margin:20px 0}.df-table-wrap{overflow:auto}.dominio-catalogos details{border-bottom:1px solid #bbb;padding:12px}.dominio-catalogos summary{font-weight:800;cursor:pointer;text-transform:uppercase}.dominio-form input[type='checkbox']{width:auto;min-height:0}
</style>
