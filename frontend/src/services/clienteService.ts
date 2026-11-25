import { api } from './api'

/**
 * Clasificaciones institucionales admitidas por el backend.
 */
export type TipoEntidad =
  | 'EMBAJADA'
  | 'CONSULADO'
  | 'ORGANISMO_INTERNACIONAL'
  | 'OTRO'

/**
 * Datos necesarios para registrar o actualizar
 * un cliente institucional.
 */
export interface ClienteInstitucionalRequest {
  nombreEntidad: string
  tipoEntidad: TipoEntidad
  numeroIdentificacionFiscal: string | null
  nombreContacto: string
  correoContacto: string
  telefonoContacto: string | null
  direccion: string | null
  ciudad: string | null
  activo: boolean
}

/**
 * Información de un cliente institucional devuelta por la API.
 */
export interface ClienteInstitucionalResponse {
  id: number
  nombreEntidad: string
  tipoEntidad: TipoEntidad
  numeroIdentificacionFiscal: string | null
  nombreContacto: string
  correoContacto: string
  telefonoContacto: string | null
  direccion: string | null
  ciudad: string | null
  activo: boolean
  creadoEn: string
  actualizadoEn: string
}

/** Consultas y escritura institucional con la sesión revocable común. */
export const listarClientes=()=>api<ClienteInstitucionalResponse[]>('/api/clientes')
export const obtenerClientePorId=(id:number)=>api<ClienteInstitucionalResponse>(`/api/clientes/${id}`)
export const registrarCliente=(request:ClienteInstitucionalRequest)=>api<ClienteInstitucionalResponse>('/api/clientes',{method:'POST',body:JSON.stringify(request)})
export const actualizarCliente=(id:number,request:ClienteInstitucionalRequest)=>api<ClienteInstitucionalResponse>(`/api/clientes/${id}`,{method:'PUT',body:JSON.stringify(request)})
