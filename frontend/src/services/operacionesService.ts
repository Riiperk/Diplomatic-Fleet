import { api } from './api'

/** Tipos de respuesta para los módulos administrativos con respaldo PostgreSQL. */
export interface Documento {
  id:number; vehiculo_id:number; placa:string; tipo_documento:string; numero_documento:string|null
  fecha_emision:string|null; fecha_vencimiento:string; estado:string; observaciones:string|null
}
export interface Mantenimiento {
  id:number; vehiculo_id:number; placa:string; tipo:string; estado:string; descripcion:string
  fecha_programada:string; fecha_inicio:string|null; fecha_fin:string|null; costo:number|null; proveedor:string|null
}
export interface Conductor {
  id:number; cliente_institucional_id:number; nombre_entidad:string; nombre_completo:string
  tipo_documento:string; numero_documento:string; numero_licencia:string; categoria_licencia:string|null
  fecha_vencimiento_licencia:string; telefono:string|null; correo:string|null; activo:boolean; licencia_vigente:boolean
}
export interface Cotizacion {
  id:number; solicitud_id:number; codigo:string; numero_version:number; estado:string
  valida_hasta:string; subtotal:number; impuestos:number; descuentos:number; valor_total:number; observaciones:string|null
}
export interface UsuarioSeguro {
  id:number; nombre:string; apellido:string; correo:string; activo:boolean; roles:string; creado_en:string
}
export interface Reporte {
  vehiculos:number; clientes:number; reservas:number; pendientes:number; mantenimientos:number
  documentosVencidos:number; cotizaciones:number; valorEstimadoConfirmado:number
  vehiculosTotales:number; clientesTotales:number; mantenimientosProgramados:number; mantenimientosTotales:number; reservasFinalizadas:number; reservasCanceladas:number; pagosVerificados:number
}
export interface DocumentoPayload {
  vehiculoId:number; tipoDocumento:string; numeroDocumento:string|null; fechaEmision:string|null
  fechaVencimiento:string; observaciones:string|null
}
export interface MantenimientoPayload {
  vehiculoId:number; tipo:string; descripcion:string; fechaProgramada:string; proveedor:string|null; costo:number|null
}
export interface ConductorPayload {
  clienteInstitucionalId:number; nombreCompleto:string; tipoDocumento:string; numeroDocumento:string
  numeroLicencia:string; fechaVencimientoLicencia:string; categoriaLicencia:string|null; telefono:string|null; correo:string|null
}
export interface CotizacionPayload { solicitudId:number; validaHasta:string; observaciones:string|null }

/** Consulta registros según el recurso fijo: nunca concatena rutas externas. */
export const documentos = () => api<Documento[]>('/api/admin/documentos')
/** Registra la vigencia documental de una unidad. */
export const crearDocumento = (data:DocumentoPayload) => api<Documento>('/api/admin/documentos',{method:'POST',body:JSON.stringify(data)})
/** Actualiza un documento existente sin duplicar su identidad. */
export const editarDocumento = (id:number,data:DocumentoPayload) => api<Documento>(`/api/admin/documentos/${id}`,{method:'PUT',body:JSON.stringify(data)})
/** Consulta el historial de mantenimientos. */
export const mantenimientos = () => api<Mantenimiento[]>('/api/admin/mantenimientos')
/** Programa un trabajo de mantenimiento con costo y fecha. */
export const crearMantenimiento = (data:MantenimientoPayload) => api<Mantenimiento>('/api/admin/mantenimientos',{method:'POST',body:JSON.stringify(data)})
/** Aplica una transición de mantenimiento autorizada. */
export const cambiarMantenimiento = (id:number,estado:string) => api<Mantenimiento>(`/api/admin/mantenimientos/${id}/estado`,{method:'PATCH',body:JSON.stringify({estado})})
/** Consulta conductores institucionales y vigencia declarada. */
export const conductores = () => api<Conductor[]>('/api/admin/conductores')
/** Registra un conductor institucional con su licencia. */
export const crearConductor = (data:ConductorPayload) => api<Conductor>('/api/admin/conductores',{method:'POST',body:JSON.stringify(data)})
/** Activa o suspende una autorización institucional. */
export const activarConductor = (id:number,activo:boolean) => api<Conductor>(`/api/admin/conductores/${id}/activo`,{method:'PATCH',body:JSON.stringify({activo})})
/** Lista las versiones económicas disponibles para administración. */
export const cotizaciones = () => api<Cotizacion[]>('/api/admin/cotizaciones')
/** Genera una propuesta a partir del importe de la solicitud. */
export const crearCotizacion = (data:CotizacionPayload) => api<Cotizacion>('/api/admin/cotizaciones',{method:'POST',body:JSON.stringify(data)})
/** Modifica exclusivamente los datos editables de una propuesta no publicada. */
export const editarCotizacion=(id:number,data:{validaHasta:string;observaciones:string|null})=>api<Cotizacion>('/api/admin/cotizaciones/'+id+'/borrador',{method:'PATCH',body:JSON.stringify(data)})
/** Publica o anula una propuesta administrativa. */
export const cambiarCotizacion = (id:number,estado:string) => api<Cotizacion>(`/api/admin/cotizaciones/${id}/estado`,{method:'PATCH',body:JSON.stringify({estado})})
/** Consulta cuentas sin exponer hashes. */
export const usuarios = () => api<UsuarioSeguro[]>('/api/admin/usuarios')
/** Cambia el estado de una cuenta según los permisos del servidor. */
export const activarUsuario = (id:number,activo:boolean) => api<UsuarioSeguro>(`/api/admin/usuarios/${id}/activo`,{method:'PATCH',body:JSON.stringify({activo})})
/** Recupera los indicadores agregados en PostgreSQL. */
export const reporte = () => api<Reporte>('/api/admin/reportes')

/** Alta administrativa de cuenta con rol y licencia opcional. Nunca devuelve la contraseña. */
export interface CuentaNueva {nombre:string;apellido:string;correo:string;password:string;rol:string;clienteInstitucionalId?:number;numeroLicencia?:string;categoriaLicencia?:string;vencimientoLicencia?:string}
export const crearCuenta=(data:CuentaNueva)=>api<{id:number;correo:string;rol:string}>('/api/admin/usuarios',{method:'POST',body:JSON.stringify(data)})
/** Datos disponibles para asignar personal profesional a una solicitud. */
export interface Profesional {id:number;usuario_id:number;nombre:string;numero_licencia:string;categoria_licencia:string;vencimiento:string;habilitado:boolean}
/** Lista conductores profesionales para asignación. */
export const profesionales=()=>api<Profesional[]>('/api/admin/conductores-profesionales')
/** Solicita asignación; el servidor verifica vigencia y fechas. */
export const asignarProfesional=(id:number,conductorId:number)=>api<{id:number;estado:string;conductorProfesionalId:number}>(`/api/admin/solicitudes/${id}/conductor`,{method:'PATCH',body:JSON.stringify({conductorId})})
/** La propiedad de las cotizaciones y las asignaciones se verifica en Kotlin. */
export interface PropuestaCliente extends Cotizacion {estado_reserva:string;decidible:boolean}
export const misCotizaciones=()=>api<PropuestaCliente[]>('/api/cliente/cotizaciones')
/** Envia la decisión institucional a la API protegida. */
export const decidirCotizacion=(id:number,estado:'ACEPTADA'|'RECHAZADA')=>api<PropuestaCliente>(`/api/cliente/cotizaciones/${id}/estado`,{method:'PATCH',body:JSON.stringify({estado})})
export interface Asignacion {id:number;codigo:string;estado:string;vehiculo:string;inicio:string;fin:string;recogida:string;devolucion:string}
/** Consulta la agenda del conductor autenticado. */
export const misAsignaciones=()=>api<Asignacion[]>('/api/conductor/asignaciones')
