import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

/**
 * Punto de entrada principal del frontend Diplomatic Fleet.
 *
 * Crea la aplicación Vue e instala los complementos compartidos:
 *
 * - Pinia administra el estado global, incluida la sesión autenticada.
 * - Vue Router controla la navegación entre las vistas del sistema.
 *
 * La aplicación se monta en el elemento con identificador `app`,
 * definido en el archivo `frontend/index.html`.
 */
const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
