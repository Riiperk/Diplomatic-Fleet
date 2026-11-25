import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { sesion, inicioPerfil } from '@/services/api'

const operaciones = () => import('@/views/AdminOperacionesView.vue')

/**
 * Rutas públicas y privadas de la interfaz.
 *
 * Decisión de entrega final:
 * la raíz dirige al catálogo público para que la plataforma abra primero en la
 * experiencia visual principal solicitada por el usuario. El servidor conserva
 * la autorización real sobre los recursos protegidos.
 */
const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/catalogo' },
  { path: '/catalogo', component: () => import('@/views/CatalogoView.vue') },
  { path: '/registro', component: () => import('@/views/RegistroView.vue'), meta: { guest: true } },
  { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
  { path: '/mis-cotizaciones', component: () => import('@/views/MisCotizacionesView.vue'), meta: { role: 'CLIENTE' } },
  { path: '/dominio', component: () => import('@/views/DominioView.vue'), meta: { role: 'ADMINISTRADOR' } },
  { path: '/auditoria', component: () => import('@/views/AuditoriaView.vue'), meta: { role: 'ADMINISTRADOR' } },
  { path: '/asignaciones', component: () => import('@/views/AsignacionesView.vue'), meta: { role: 'CONDUCTOR' } },
  { path: '/mis-reservas', component: () => import('@/views/MisReservasView.vue'), meta: { role: 'CLIENTE' } },
  { path: '/inicio', component: () => import('@/views/AdminDashboardView.vue'), meta: { role: 'ADMINISTRADOR' } },
  { path: '/vehiculos', component: () => import('@/views/VehiculosView.vue'), meta: { role: 'ADMINISTRADOR' } },
  { path: '/clientes', component: () => import('@/views/ClientesView.vue'), meta: { role: 'ADMINISTRADOR' } },
  { path: '/solicitudes', component: () => import('@/views/SolicitudesAdminView.vue'), meta: { role: 'ADMINISTRADOR' } },
  ...['documentos', 'mantenimientos', 'disponibilidad', 'validacion-conductor', 'cotizaciones', 'usuarios', 'reportes']
    .map(path => ({ path: '/' + path, component: operaciones, meta: { role: 'ADMINISTRADOR' } })),
  { path: '/:pathMatch(.*)*', redirect: '/catalogo' },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

/**
 * Evita mostrar interfaces de otros roles y dirige al acceso cuando no hay
 * sesión. El rol OPERACIONES conserva acceso limitado a módulos concretos.
 */
router.beforeEach(to => {
  const role = to.meta.role as string | undefined
  const perfil = sesion()
  const operador = perfil?.rol === 'OPERACIONES' && ['/vehiculos', '/documentos', '/mantenimientos', '/disponibilidad'].includes(to.path)

  if (role && !perfil) return { path: '/login', query: { destino: to.fullPath } }
  if (role && perfil?.rol !== role && !operador) return inicioPerfil(perfil?.rol)
  if (to.meta.guest && perfil) return inicioPerfil(perfil.rol)
})

export default router
