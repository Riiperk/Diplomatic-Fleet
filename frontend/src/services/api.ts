/**
 * Cliente REST común. La contraseña se utiliza únicamente al iniciar sesión;
 * sessionStorage conserva un token revocable y un perfil de presentación.
 * En producción VITE_API_URL señala el servicio HTTPS; en local el proxy Vite
 * mantiene las rutas relativas. No se introducen secretos en variables VITE.
 */
export type Perfil = { id:number; nombre:string; apellido:string; correo:string; clienteInstitucionalId:number|null; rol:'ADMINISTRADOR'|'CLIENTE'|'OPERACIONES'|'CONDUCTOR'|'SIN_ROL' }
export type SesionWeb = { token:string; perfil:Perfil }
const KEY = 'dfSesionWebV13'
const BASE = (import.meta.env.VITE_API_URL || '').replace(/\/+$/, '')
// Eliminar las credenciales V12 en texto claro al cargar esta versión.
if (sessionStorage.getItem('diplomaticFleetCredentials')) {
  sessionStorage.removeItem('diplomaticFleetCredentials')
  sessionStorage.removeItem('dfPerfil')
}
/** Revoca el token en el servidor y elimina inmediatamente la información local. */
export function logout(): void {
  const token = sessionStorage.getItem(KEY)
  sessionStorage.removeItem(KEY); sessionStorage.removeItem('dfPerfil')
  if(token) void fetch(BASE+'/api/auth/salir',{method:'POST',headers:{Authorization:'Bearer '+token},keepalive:true}).catch(()=>{})
}
/** Guarda exclusivamente el token opaco y el perfil visible. */
export function guardarSesion(s:SesionWeb):void {
  sessionStorage.setItem(KEY,s.token)
  sessionStorage.setItem('dfPerfil',JSON.stringify(s.perfil))
}
/** El perfil local sirve para navegar, nunca para conceder permisos. */
export function sesion():Perfil|null {
  if(!sessionStorage.getItem(KEY))return null
  try{return JSON.parse(sessionStorage.getItem('dfPerfil')||'null') as Perfil|null}catch{return null}
}
/** Ejecuta una petición y traduce errores sin registrar credenciales. */
export async function api<T>(path:string,init:RequestInit={},publicRoute=false):Promise<T>{
  const headers=new Headers(init.headers)
  headers.set('Accept','application/json')
  if(init.body)headers.set('Content-Type','application/json')
  if(!publicRoute){const token=sessionStorage.getItem(KEY);if(!token)throw new Error('Inicie sesión para continuar.');headers.set('Authorization','Bearer '+token)}
  let response:Response
  try{response=await fetch(BASE+path,{...init,headers,cache:'no-store'})}catch{throw new Error('No se pudo conectar con el servicio. Compruebe la conexión y vuelva a intentarlo.')}
  if(!response.ok){
    let error:any={};try{error=await response.json()}catch{/* El servidor puede estar iniciando. */}
    if(response.status===401){if(!publicRoute){sessionStorage.removeItem(KEY);sessionStorage.removeItem('dfPerfil')}throw new Error('Credenciales incorrectas o sesión vencida. Inicie sesión nuevamente.')}
    if(response.status===403)throw new Error('No tiene permisos para realizar esta operación.')
    throw new Error((error.validaciones?Object.values(error.validaciones).join(' '):error.mensaje||error.error)||`Error HTTP ${response.status}`)
  }
  return await response.json() as T
}
/** Spring Security valida HTTP Basic una sola vez y devuelve una sesión revocable. */
export async function acceso(correo:string,password:string):Promise<SesionWeb>{
  const encoded=btoa(unescape(encodeURIComponent(correo+':'+password)))
  return api<SesionWeb>('/api/auth/sesion',{method:'POST',headers:{Authorization:'Basic '+encoded}},true)
}
export type Registro={nombre:string;apellido:string;correo:string;password:string;nombreEntidad:string;tipoEntidad:string;ciudad:string;direccion:string;numeroIdentificacionFiscal?:string;telefono?:string}
export const registrar=(data:Registro)=>api<Perfil>('/api/auth/registro',{method:'POST',body:JSON.stringify(data)},true)
/** Ruta inicial autorizada para cada perfil. */
export function inicioPerfil(rol:Perfil['rol']|undefined):string{return rol==='ADMINISTRADOR'?'/inicio':rol==='OPERACIONES'?'/vehiculos':rol==='CONDUCTOR'?'/asignaciones':'/catalogo'}
