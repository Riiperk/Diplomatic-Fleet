<script setup lang="ts">
/**
 * Componente raíz de Diplomatic Fleet.
 *
 * Este componente funciona como contenedor principal de la aplicación.
 * El contenido visible depende de la ruta activa y se representa mediante
 * RouterView.
 *
 * Las vistas específicas, como el inicio de sesión y el panel principal,
 * se implementarán en componentes separados para mantener responsabilidades
 * claras y facilitar las pruebas.
 */
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>
