import { api } from './api'

/**
 * Estados operativos admitidos para los vehículos.
 */
export type EstadoVehiculo =
  | 'DISPONIBLE'
  | 'RESERVADO'
  | 'EN_SERVICIO'
  | 'EN_MANTENIMIENTO'
  | 'INACTIVO'

/**
 * Tipos de combustible admitidos por el backend.
 */
export type TipoCombustible =
  | 'GASOLINA'
  | 'DIESEL'
  | 'HIBRIDO'
  | 'ELECTRICO'

/**
 * Códigos de categoría existentes en Diplomatic Fleet.
 */
export type CodigoCategoriaVehiculo =
  | 'ECONOMICO'
  | 'EJECUTIVO'
  | 'PREMIUM'

/**
 * Datos requeridos para registrar un vehículo.
 */
export interface VehiculoRequest {
  categoriaId: number
  placa: string
  marca: string
  modelo: string
  anio: number
  color: string
  combustible: TipoCombustible
  capacidadPasajeros: number
  capacidadEquipaje: number
  activo: boolean
  tarifaDiaria: number
  imagenUrl?: string | null
}

/**
 * Información de un vehículo devuelta por la API.
 */
export interface VehiculoResponse {
  id: number
  categoriaId: number
  codigoCategoria: CodigoCategoriaVehiculo
  nombreCategoria: string
  placa: string
  marca: string
  modelo: string
  anio: number
  color: string
  combustible: TipoCombustible
  capacidadPasajeros: number
  capacidadEquipaje: number
  estado: EstadoVehiculo
  activo: boolean
  tarifaDiaria: number | null
  imagenUrl: string | null
  creadoEn: string
  actualizadoEn: string
}

/** Consulta y escritura de flota mediante el servicio HTTP común. */
export const listarVehiculos=()=>api<VehiculoResponse[]>('/api/vehiculos')
export const registrarVehiculo=(request:VehiculoRequest)=>api<VehiculoResponse>('/api/vehiculos',{method:'POST',body:JSON.stringify(request)})
export const actualizarVehiculo=(id:number,request:VehiculoRequest)=>api<VehiculoResponse>(`/api/vehiculos/${id}`,{method:'PUT',body:JSON.stringify(request)})
