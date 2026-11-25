import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Configuración de desarrollo del frontend de Diplomatic Fleet.
 *
 * El proxy permite que Vue consuma la API de Spring Boot mediante
 * rutas relativas y evita problemas de origen cruzado durante
 * el desarrollo local.
 */
export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
