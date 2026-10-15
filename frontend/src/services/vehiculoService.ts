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
  creadoEn: string
  actualizadoEn: string
}

/**
 * Estructura de error utilizada por la API.
 */
interface ApiErrorResponse {
  mensaje?: string
  error?: string
  validaciones?: Record<string, string>
}

/**
 * Credenciales temporales utilizadas durante la demo académica.
 */
interface ApiCredentials {
  username: string
  password: string
}

const API_URL = '/api/vehiculos'
const CREDENTIALS_KEY = 'diplomaticFleetCredentials'

/**
 * Guarda temporalmente las credenciales durante la sesión del navegador.
 *
 * Las credenciales se eliminan al cerrar la pestaña o finalizar la sesión.
 *
 * @param username Usuario utilizado por Spring Security.
 * @param password Contraseña de desarrollo.
 */
export function guardarCredenciales(
  username: string,
  password: string,
): void {
  const credentials: ApiCredentials = {
    username,
    password,
  }

  sessionStorage.setItem(
    CREDENTIALS_KEY,
    JSON.stringify(credentials),
  )
}

/**
 * Elimina las credenciales temporales de la sesión.
 */
export function eliminarCredenciales(): void {
  sessionStorage.removeItem(CREDENTIALS_KEY)
}

/**
 * Recupera las credenciales almacenadas para construir
 * el encabezado HTTP Basic.
 *
 * @returns Credenciales activas.
 * @throws Error cuando no existe una sesión disponible.
 */
function obtenerCredenciales(): ApiCredentials {
  const storedCredentials = sessionStorage.getItem(CREDENTIALS_KEY)

  if (!storedCredentials) {
    throw new Error(
      'No existe una sesión activa. Inicia sesión nuevamente.',
    )
  }

  return JSON.parse(storedCredentials) as ApiCredentials
}

/**
 * Construye las cabeceras utilizadas por las peticiones autenticadas.
 *
 * @returns Cabeceras HTTP con autenticación Basic y contenido JSON.
 */
function crearHeaders(): HeadersInit {
  const credentials = obtenerCredenciales()

  const basicToken = btoa(
    `${credentials.username}:${credentials.password}`,
  )

  return {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Basic ${basicToken}`,
  }
}

/**
 * Obtiene un mensaje comprensible desde una respuesta de error.
 *
 * @param response Respuesta HTTP no satisfactoria.
 * @returns Mensaje preparado para mostrar en la interfaz.
 */
async function obtenerMensajeError(
  response: Response,
): Promise<string> {
  try {
    const error = (await response.json()) as ApiErrorResponse

    if (error.validaciones) {
      return Object.values(error.validaciones).join(' ')
    }

    return (
      error.mensaje ??
      error.error ??
      'No fue posible completar la operación.'
    )
  } catch {
    return `La operación falló con el estado HTTP ${response.status}.`
  }
}

/**
 * Consulta todos los vehículos registrados.
 *
 * @returns Vehículos proporcionados por la API.
 * @throws Error cuando la petición no puede completarse.
 */
export async function listarVehiculos(): Promise<
  VehiculoResponse[]
> {
  const response = await fetch(API_URL, {
    method: 'GET',
    headers: crearHeaders(),
  })

  if (!response.ok) {
    throw new Error(await obtenerMensajeError(response))
  }

  return (await response.json()) as VehiculoResponse[]
}

/**
 * Registra un nuevo vehículo en la flota.
 *
 * @param request Datos validados del vehículo.
 * @returns Vehículo creado por el backend.
 * @throws Error cuando los datos son inválidos o la placa está duplicada.
 */
export async function registrarVehiculo(
  request: VehiculoRequest,
): Promise<VehiculoResponse> {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: crearHeaders(),
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw new Error(await obtenerMensajeError(response))
  }

  return (await response.json()) as VehiculoResponse
}
