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

/**
 * Error estructurado proporcionado por el backend.
 */
interface ApiErrorResponse {
  mensaje?: string
  error?: string
  validaciones?: Record<string, string>
}

/**
 * Credenciales temporales almacenadas durante la sesión.
 */
interface ApiCredentials {
  username: string
  password: string
}

const API_URL = '/api/clientes'
const CREDENTIALS_KEY = 'diplomaticFleetCredentials'

/**
 * Recupera las credenciales temporales almacenadas
 * durante el inicio de sesión.
 *
 * @returns Credenciales para HTTP Basic.
 * @throws Error cuando no existe una sesión activa.
 */
function obtenerCredenciales(): ApiCredentials {
  const storedCredentials =
    sessionStorage.getItem(CREDENTIALS_KEY)

  if (!storedCredentials) {
    throw new Error(
      'No existe una sesión activa. Inicia sesión nuevamente.',
    )
  }

  try {
    return JSON.parse(
      storedCredentials,
    ) as ApiCredentials
  } catch {
    sessionStorage.removeItem(CREDENTIALS_KEY)

    throw new Error(
      'La sesión no es válida. Inicia sesión nuevamente.',
    )
  }
}

/**
 * Construye las cabeceras requeridas por la API.
 *
 * El encabezado X-Requested-With evita que el navegador
 * muestre el diálogo nativo de HTTP Basic cuando ocurre
 * un error de autenticación.
 *
 * @returns Cabeceras con contenido JSON y autenticación.
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
    'X-Requested-With': 'XMLHttpRequest',
  }
}

/**
 * Convierte una respuesta HTTP fallida en un mensaje
 * comprensible para la interfaz.
 *
 * @param response Respuesta devuelta por el backend.
 * @returns Mensaje preparado para mostrar al usuario.
 */
async function obtenerMensajeError(
  response: Response,
): Promise<string> {
  if (response.status === 401) {
    return 'La sesión no es válida. Inicia sesión nuevamente.'
  }

  try {
    const error =
      (await response.json()) as ApiErrorResponse

    if (error.validaciones) {
      return Object.values(
        error.validaciones,
      ).join(' ')
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
 * Consulta todos los clientes institucionales.
 *
 * @returns Instituciones registradas en PostgreSQL.
 * @throws Error cuando la petición no puede completarse.
 */
export async function listarClientes():
  Promise<ClienteInstitucionalResponse[]> {
  const response = await fetch(API_URL, {
    method: 'GET',
    headers: crearHeaders(),
  })

  if (!response.ok) {
    throw new Error(
      await obtenerMensajeError(response),
    )
  }

  return (
    await response.json()
  ) as ClienteInstitucionalResponse[]
}

/**
 * Consulta un cliente institucional mediante su identificador.
 *
 * @param id Identificador del cliente.
 * @returns Institución encontrada.
 * @throws Error cuando el cliente no existe.
 */
export async function obtenerClientePorId(
  id: number,
): Promise<ClienteInstitucionalResponse> {
  const response = await fetch(
    `${API_URL}/${id}`,
    {
      method: 'GET',
      headers: crearHeaders(),
    },
  )

  if (!response.ok) {
    throw new Error(
      await obtenerMensajeError(response),
    )
  }

  return (
    await response.json()
  ) as ClienteInstitucionalResponse
}

/**
 * Registra un nuevo cliente institucional.
 *
 * @param request Datos validados de la institución.
 * @returns Cliente creado por el backend.
 * @throws Error cuando existen datos inválidos o duplicados.
 */
export async function registrarCliente(
  request: ClienteInstitucionalRequest,
): Promise<ClienteInstitucionalResponse> {
  const response = await fetch(API_URL, {
    method: 'POST',
    headers: crearHeaders(),
    body: JSON.stringify(request),
  })

  if (!response.ok) {
    throw new Error(
      await obtenerMensajeError(response),
    )
  }

  return (
    await response.json()
  ) as ClienteInstitucionalResponse
}

/**
 * Actualiza un cliente institucional existente.
 *
 * @param id Identificador del cliente.
 * @param request Nuevos datos validados.
 * @returns Cliente actualizado por el backend.
 * @throws Error cuando el cliente no existe o hay datos duplicados.
 */
export async function actualizarCliente(
  id: number,
  request: ClienteInstitucionalRequest,
): Promise<ClienteInstitucionalResponse> {
  const response = await fetch(
    `${API_URL}/${id}`,
    {
      method: 'PUT',
      headers: crearHeaders(),
      body: JSON.stringify(request),
    },
  )

  if (!response.ok) {
    throw new Error(
      await obtenerMensajeError(response),
    )
  }

  return (
    await response.json()
  ) as ClienteInstitucionalResponse
}
