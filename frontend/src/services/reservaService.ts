/**
 * Cliente HTTP de reservas institucionales.
 * Comparte los contratos tipados con las vistas Vue y mantiene el cálculo
 * monetario solo para visualización; el backend determina el valor real.
 */
import { api } from './api'
export interface Coche {id:number;marca:string;modelo:string;anio:number;categoria:string;pasajeros:number;equipaje:number;tarifaDiaria:number;imagenUrl:string|null;nivelBlindaje?:string}
export type EstadoReserva='RECIBIDA'|'EN_REVISION'|'COTIZADA'|'PENDIENTE_APROBACION'|'CONFIRMADA'|'ASIGNADA'|'EN_SERVICIO'|'FINALIZADA'|'RECHAZADA'|'CANCELADA'|'ARCHIVADA'
/** Estado del pago externo; no implica un cargo por la plataforma. */
export type MetodoPago = 'TRANSFERENCIA' | 'EFECTIVO' | 'TARJETA_PRESENCIAL'
export type EstadoPago = 'PENDIENTE' | 'REPORTADO' | 'VERIFICADO' | 'RECHAZADO' | 'REEMBOLSO_PENDIENTE' | 'REEMBOLSADO'
export interface Reserva {id:number;codigo:string;vehiculoId:number|null;vehiculo:string|null;cliente:string;inicio:string;fin:string;recogida:string;devolucion:string;pasajeros:number;estado:EstadoReserva;valorEstimado:number;metodoPago:MetodoPago|null;estadoPago:EstadoPago|null;referenciaPago:string|null;fechaInicioReal:string|null;fechaFinReal:string|null;motivoCierreAnticipado:string|null;modalidad:string;conductorProfesionalId:number|null;conductor:string|null;motivoInicioAnticipado:string|null;editable:boolean;requiereBlindaje?:boolean}
export interface CrearReserva {vehiculoId:number;inicio:string;fin:string;recogida:string;devolucion:string;pasajeros:number;observaciones:string;metodoPago:MetodoPago;modalidad?:'SIN_CONDUCTOR'|'CON_CONDUCTOR';requiereBlindaje?:boolean}
/** Consulta las unidades libres en el intervalo (sin autenticación). */
export const catalogo=(inicio?:string,fin?:string)=>api<Coche[]>('/api/public/vehiculos'+(inicio&&fin?'?'+new URLSearchParams({inicio,fin}):''),{},true)
/** Envía una solicitud de alquiler al backend bajo la sesión del cliente. */
export const crearReserva=(data:CrearReserva)=>api<Reserva>('/api/solicitudes',{method:'POST',body:JSON.stringify(data)})
/** Lista solicitudes pertenecientes a la institución de la sesión. */
export const misReservas=()=>api<Reserva[]>('/api/solicitudes/mias')
/** Consulta la bandeja de solicitudes para administración. */
export const todasReservas=()=>api<Reserva[]>('/api/admin/solicitudes')
/** Anula una solicitud indicando el motivo obligatorio. */
export const cancelarReserva=(id:number,motivo:string)=>api<Reserva>(`/api/solicitudes/${id}/cancelar`,{method:'POST',body:JSON.stringify({motivo})})
/** Aplica una transición validada por Spring Boot. */
/** Cierra anticipadamente solo con evidencia/observacion de devolucion efectiva. */
export const cambiarEstado=(id:number,estado:string,motivoCierre?:string,motivoInicio?:string)=>api<Reserva>(`/api/admin/solicitudes/${id}/estado`,{method:'PATCH',body:JSON.stringify({estado,motivoCierre,motivoInicio})})
/** El cliente comunica la referencia; no constituye comprobante bancario validado. */
export const reportarPago=(id:number,referencia:string)=>api<Reserva>(`/api/solicitudes/${id}/pago/reportar`,{method:'POST',body:JSON.stringify({referencia})})
/** Administracion revisa el pago externo o registra un reembolso ya efectuado. */
export const revisarPago=(id:number,estado:string,observacion?:string)=>api<Reserva>(`/api/admin/solicitudes/${id}/pago`,{method:'PATCH',body:JSON.stringify({estado,observacion})})
export const nombreMetodo=(metodo:MetodoPago|null)=>({TRANSFERENCIA:'Transferencia bancaria',EFECTIVO:'Efectivo al recibir',TARJETA_PRESENCIAL:'Tarjeta presencial'} as Record<MetodoPago,string>)[metodo as MetodoPago]||'No registrado'
export const nombrePago=(estado:EstadoPago|null)=>(estado||'NO REGISTRADO').replaceAll('_',' ')
/** Formatea números de manera legible para Colombia; no modifica el valor. */
export const pesos=(value:number)=>new Intl.NumberFormat('es-CO',{style:'currency',currency:'COP',maximumFractionDigits:0}).format(value)
/** Convierte un instante ISO a fecha legible en Colombia. */
export const fecha=(value:string)=>new Date(value).toLocaleDateString('es-CO',{day:'2-digit',month:'short',year:'numeric',timeZone:'America/Bogota'})
/** Convierte la fecha de recogida a las 09:00 en Colombia (UTC-05:00). */
export function isoFecha(date:string){return new Date(`${date}T09:00:00-05:00`).toISOString()}
/** Obtiene una cadena YYYY-MM-DD respetando zona horaria local. */
export function fechaLocal(d:Date){const z=new Date(d.getTime()-d.getTimezoneOffset()*60000);return z.toISOString().slice(0,10)}
/** Calcula la fecha de hoy más un número de días. */
export function proximaFecha(dias:number){const d=new Date();d.setDate(d.getDate()+dias);return fechaLocal(d)}
/** Calcula la duración aproximada para mostrar estimados. */
export function diasEntre(a:string,b:string){return Math.max(1,Math.ceil((new Date(b).getTime()-new Date(a).getTime())/86400000))}

/** Crea el registro de pago faltante de una reserva anterior a V9. */
export const inicializarPago=(id:number,metodo:MetodoPago)=>api<Reserva>(`/api/admin/solicitudes/${id}/pago/inicializar`,{method:'POST',body:JSON.stringify({metodo})})

/** Modifica solo el periodo de una solicitud que Kotlin considera editable. */
export const reprogramarReserva=(id:number,inicio:string,fin:string)=>api<Reserva>(`/api/solicitudes/${id}/periodo`,{method:'PATCH',body:JSON.stringify({inicio,fin})})
/** Fecha de calendario de Bogotá para rellenar los controles date del navegador. */
export function fechaBogota(iso:string):string {
  const partes=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Bogota',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(new Date(iso))
  const parte=(tipo:string)=>partes.find(p=>p.type===tipo)?.value ?? ''
  return `${parte('year')}-${parte('month')}-${parte('day')}`
}
