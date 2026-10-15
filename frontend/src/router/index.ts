import { createRouter, createWebHistory } from 'vue-router'

/**
 * Enrutador principal de Diplomatic Fleet.
 *
 * Utiliza el historial del navegador para ofrecer direcciones limpias,
 * como `/login`, `/vehiculos` y `/solicitudes`.
 *
 * Las rutas se añadirán progresivamente cuando cada vista esté
 * implementada y revisada en su rama correspondiente.
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  /*
   * Las rutas permanecen vacías en la configuración inicial.
   * Camila añadirá la ruta del inicio de sesión durante la
   * migración del prototipo visual a componentes Vue.
   */
  routes: [],
})

export default router
