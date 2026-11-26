<script setup lang="ts">
/** Presenta pagos externos y conserva la autorización y el cálculo en Kotlin. */
import {computed,ref} from 'vue'
import DialogoBase from './DialogoBase.vue'
import {nombreMetodo,nombrePago,reportarPago,revisarPago,type Reserva} from '@/services/reservaService'
const props=defineProps<{reserva:Reserva;admin?:boolean}>()
const emit=defineEmits<{actualizado:[]}>()
type Accion='REPORTAR'|'VERIFICADO'|'RECHAZADO'|'REEMBOLSADO'
const accion=ref<Accion|null>(null),monto=ref<number|string>(''),referencia=ref(''),observacion=ref(''),error=ref(''),ocupado=ref(false)
const r=computed(()=>props.reserva)
const confirmado=computed(()=>['CONFIRMADA','ASIGNADA'].includes(r.value.estado))
const cerrado=computed(()=>['CANCELADA','RECHAZADA','ARCHIVADA'].includes(r.value.estado))
const conciliacion=computed(()=>cerrado.value&&r.value.metodoPago==='TRANSFERENCIA'&&r.value.estadoPago==='REPORTADO')
const verificar=computed(()=>!!props.admin&&(confirmado.value||conciliacion.value)&&['PENDIENTE','REPORTADO','RECHAZADO'].includes(r.value.estadoPago||'')&&(r.value.metodoPago!=='TRANSFERENCIA'||r.value.estadoPago==='REPORTADO'))
const informar=computed(()=>!props.admin&&confirmado.value&&r.value.metodoPago==='TRANSFERENCIA'&&['PENDIENTE','REPORTADO','RECHAZADO'].includes(r.value.estadoPago||''))
const total=computed(()=>accion.value==='REEMBOLSADO'?r.value.montoVerificado??null:r.value.valorEstimado)
const titulo=computed(()=>({REPORTAR:'Informar transferencia',VERIFICADO:'Verificar pago externo',RECHAZADO:'Rechazar pago',REEMBOLSADO:'Registrar reembolso externo'}[accion.value??'REPORTAR']))
const dinero=(v:number|null|undefined)=>v==null?'No registrado':new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:2}).format(v)
const instante=(v:string|null|undefined)=>v?new Date(v).toLocaleString('es-CO',{timeZone:'America/Bogota'}):'No registrado'
function abrir(a:Accion){accion.value=a;error.value='';monto.value='';referencia.value='';observacion.value=''}
async function guardar(){
 if(!accion.value||ocupado.value)return
 const a=accion.value;error.value=''
 if(a!=='RECHAZADO'){
   const valor=Number(monto.value)
   if(!Number.isFinite(valor)||valor<=0){error.value='Introduzca el importe real en COP.';return}
   if(total.value!=null&&Math.round(valor*100)!==Math.round(total.value*100)){error.value='El importe debe coincidir con el total indicado.';return}
   if(referencia.value.trim().length<4){error.value='Introduzca la referencia del comprobante externo.';return}
 }else if(observacion.value.trim().length<4){error.value='Indique el motivo del rechazo.';return}
 ocupado.value=true
 try{
   if(a==='REPORTAR')await reportarPago(r.value.id,referencia.value.trim(),Number(monto.value))
   else await revisarPago(r.value.id,a,observacion.value.trim()||undefined,a==='RECHAZADO'?undefined:Number(monto.value),a==='RECHAZADO'?undefined:referencia.value.trim())
   accion.value=null;emit('actualizado')
 }catch(e){error.value=e instanceof Error?e.message:'No se pudo registrar la operación.'}finally{ocupado.value=false}
}
function descargar(){
 const x=r.value
 if(x.montoVerificado==null&&x.montoReembolsado==null)return
 const texto=['DIPLOMATIC FLEET — CONSTANCIA DE REGISTRO DE PAGO EXTERNO','Solicitud: '+x.codigo,'Método: '+nombreMetodo(x.metodoPago),'Estado: '+nombrePago(x.estadoPago),'Total del alquiler: '+dinero(x.valorEstimado),'Importe verificado: '+dinero(x.montoVerificado),'Fecha de verificación: '+instante(x.verificadoEn),'Referencia de verificación: '+(x.referenciaVerificacion||'No registrada'),'Importe reembolsado: '+dinero(x.montoReembolsado),'Fecha de reembolso: '+instante(x.reembolsadoEn),'Referencia de reembolso: '+(x.referenciaReembolso||'No registrada'),'Registro administrativo. No es factura fiscal ni comprobante expedido por un banco.'].join('\r\n')
 const url=URL.createObjectURL(new Blob([texto],{type:'text/plain;charset=utf-8'}));const a=document.createElement('a');a.href=url;a.download=x.codigo+'-registro-pago.txt';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000)
}
</script>
<template>
 <div class="df-pago-resumen">
  <b>{{nombreMetodo(r.metodoPago)}}</b><span>{{nombrePago(r.estadoPago)}}</span>
  <small>Total: {{dinero(r.valorEstimado)}}</small>
  <small v-if="r.referenciaPago">Referencia informada: {{r.referenciaPago}}</small>
  <small v-if="r.montoReportado!=null">Informado: {{dinero(r.montoReportado)}}</small>
  <small v-if="r.montoVerificado!=null">Verificado: {{dinero(r.montoVerificado)}} · {{instante(r.verificadoEn)}}</small>
  <small v-if="r.montoReembolsado!=null">Devuelto: {{dinero(r.montoReembolsado)}}</small>
  <small v-if="r.observacionPago && r.estadoPago==='RECHAZADO'">Motivo: {{r.observacionPago}}</small>
  <small v-if="!confirmado && ['RECIBIDA','EN_REVISION','COTIZADA','PENDIENTE_APROBACION'].includes(r.estado)">Espere la confirmación del alquiler antes de pagar.</small>
  <small v-if="!admin && confirmado && r.estadoPago!=='VERIFICADO' && r.metodoPago!=='TRANSFERENCIA'">Pague presencialmente al operador; administración registrará el recibo.</small>
  <small v-if="!admin && informar">Realice la transferencia solo a los datos bancarios confirmados por el proveedor y conserve el comprobante.</small>
  <small v-if="r.estadoPago==='REEMBOLSO_PENDIENTE'">Reembolso externo pendiente; no se devuelve dinero automáticamente.</small>
  <div class="df-actions">
   <button v-if="informar" type="button" @click="abrir('REPORTAR')">Informar transferencia</button>
   <button v-if="verificar" type="button" @click="abrir('VERIFICADO')">{{conciliacion?'Conciliar y marcar reembolso':'Verificar pago'}}</button>
   <button v-if="admin && r.estadoPago==='REPORTADO'" type="button" @click="abrir('RECHAZADO')">Rechazar pago</button>
   <button v-if="admin && r.estadoPago==='REEMBOLSO_PENDIENTE'" type="button" @click="abrir('REEMBOLSADO')">Registrar reembolso</button>
   <button v-if="r.montoVerificado!=null||r.montoReembolsado!=null" type="button" @click="descargar">Constancia de pago ↓</button>
  </div>
  <small v-if="r.estadoPago==='VERIFICADO' && r.montoVerificado==null">Registro histórico: no se conservó el importe verificado.</small>
  <DialogoBase v-if="accion" :titulo="titulo" :ocupado="ocupado" @cerrar="accion=null">
   <p class="df-meta">Solicitud {{r.codigo}} · {{nombreMetodo(r.metodoPago)}}. {{accion==='REPORTAR'?'Informe un abono efectuado fuera de la aplicación.':accion==='REEMBOLSADO'?'Registre únicamente un reembolso ya efectuado.':accion==='VERIFICADO'?'Compruebe el abono o recibo real antes de confirmar.':'Registre el motivo por el cual no pudo comprobarse el abono.'}} No introduzca números de tarjeta, CVV ni claves bancarias.</p>
   <p v-if="conciliacion && accion==='VERIFICADO'" class="df-alert">La solicitud está cerrada: se registrará el abono recibido con reembolso pendiente.</p>
   <form @submit.prevent="guardar">
    <template v-if="accion!=='RECHAZADO'"><p><b>Importe esperado: {{dinero(total)}}</b></p><label class="df-field df-dialog-field">Importe realmente {{accion==='REEMBOLSADO'?'devuelto':'pagado'}} (COP)<input v-model="monto" type="number" min="0.01" max="999999999999.99" step="0.01" required :disabled="ocupado" /></label><label class="df-field df-dialog-field">Referencia externa o número de recibo<input v-model="referencia" minlength="4" maxlength="120" required :disabled="ocupado" /></label></template>
    <label v-if="accion!=='REPORTAR'" class="df-field df-dialog-field">{{accion==='RECHAZADO'?'Motivo del rechazo':'Observación (opcional)'}}<textarea v-model="observacion" maxlength="500" :required="accion==='RECHAZADO'" :disabled="ocupado"></textarea></label>
    <p v-if="error" class="df-alert error" role="alert">{{error}}</p>
    <button class="df-button" :disabled="ocupado" type="submit">{{ocupado?'Registrando…':'Registrar operación →'}}</button>
   </form>
  </DialogoBase>
 </div>
</template>
<style scoped>
.df-pago-resumen{display:grid;gap:5px;min-width:170px;max-width:270px}.df-pago-resumen small{line-height:1.35;overflow-wrap:anywhere}.df-pago-resumen .df-actions{margin:4px 0}.df-pago-resumen .df-actions button{text-align:left}
</style>
