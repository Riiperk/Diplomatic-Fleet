import { createRouter, createWebHistory } from 'vue-router'

/**
 * Enrutador principal de Diplomatic Fleet.
 *
 * Utiliza el historial del navegador para ofrecer direcciones limpias.
 * Las vistas se cargan de forma diferida para reducir el tamaño inicial
 * del paquete y separar cada módulo funcional.
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

      /**
       * La importación dinámica carga la vista de acceso únicamente
       * cuando el usuario navega a la ruta de inicio de sesión.
       */
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/vehiculos',
      name: 'vehiculos',

      /**
       * Carga de forma diferida la vista de gestión de vehículos.
       */
      component: () => import('@/views/VehiculosView.vue'),
    },
  ],
})

export default router
