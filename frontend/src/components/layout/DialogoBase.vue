<script setup lang="ts">
/**
 * Diálogo institucional accesible. El elemento HTML dialog coloca el contenido
 * en la capa superior, limita el foco y devuelve el foco al control de origen.
 * El componente solo solicita el cierre; el padre decide cuándo retirar la vista.
 */
import { onMounted, onBeforeUnmount, ref } from 'vue'
const props = withDefaults(defineProps<{ titulo: string; ocupado?: boolean }>(), { ocupado: false })
const emit = defineEmits<{ cerrar: [] }>()
const elemento = ref<HTMLDialogElement | null>(null)

/** Abre el diálogo modal una vez que Vue montó el elemento en el DOM. */
onMounted(() => elemento.value?.showModal())
/** Libera la modalidad si la ruta cambia o se completa la operación. */
onBeforeUnmount(() => elemento.value?.close())
/** No permite cerrar mientras una escritura está en curso. */
function cerrar(): void { if (!props.ocupado) emit('cerrar') }
/** Un clic en el fondo, no en el contenido, solicita el cierre. */
function fondo(event: MouseEvent): void { if (event.target === elemento.value) cerrar() }
</script>

<template>
  <Teleport to="body">
    <dialog ref="elemento" class="df-native-dialog" :aria-label="titulo" :aria-busy="ocupado" @cancel.prevent="cerrar" @click="fondo">
      <section class="df-dialog-content">
        <header class="df-dialog-header">
          <span class="df-kicker">Diplomatic Fleet / Gestión</span>
          <button class="df-close" type="button" aria-label="Cerrar ventana" :disabled="ocupado" @click="cerrar">×</button>
          <h2>{{ titulo }}</h2>
        </header>
        <!-- El contenido pertenece a la vista que realiza la operación. -->
        <slot />
      </section>
    </dialog>
  </Teleport>
</template>
