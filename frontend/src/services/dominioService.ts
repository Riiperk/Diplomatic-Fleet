import { api } from './api'
/** Registro de lectura de las proyecciones del modelo docente. No incluye hashes. */
export type RegistroDominio = Record<string, string | number | boolean | null>
export type DatosDominio = Record<string, RegistroDominio[]> & { catalogos: Record<string,RegistroDominio[]> }
/** Recupera listas de selección y registros de administración. */
export const cargarDominio = () => api<DatosDominio>('/api/admin/dominio')
/** El servidor restringe las operaciones a un conjunto fijo de comandos. */
export const guardarDominio = (operacion:string,datos:Record<string,string>) => api<{ok:boolean}>(`/api/admin/dominio/${operacion}`,{method:'POST',body:JSON.stringify(datos)})
