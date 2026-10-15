import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from 'vue-router'

/**
 * Rutas demostrativas que comparten la vista genérica de módulos.
 */
const demoRoutes: RouteRecordRaw[] = [
  {
    path: '/inicio',
    name: 'inicio',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/clientes',
    name: 'clientes',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/documentos',
    name: 'documentos',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/mantenimientos',
    name: 'mantenimientos',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/disponibilidad',
    name: 'disponibilidad',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/solicitudes',
    name: 'solicitudes',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/validacion-conductor',
    name: 'conductor',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/cotizaciones',
    name: 'cotizaciones',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
  {
    path: '/usuarios',
    name: 'usuarios',
    component: () =>
      import('@/views/demo/DemoModulesView.vue'),
  },
]

/**
 * Enrutador principal de Diplomatic Fleet.
 *
 * Mantiene el acceso, la gestión funcional de vehículos y los módulos
 * navegables utilizados durante la demo académica.
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/vehiculos',
      name: 'vehiculos',
      component: () =>
        import('@/views/VehiculosView.vue'),
    },
    ...demoRoutes,
    {
      path: '/:pathMatch(.*)*',
      redirect: '/inicio',
    },
  ],
})

export default router
