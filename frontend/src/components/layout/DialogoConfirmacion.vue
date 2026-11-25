<script setup lang="ts">
/** Presentación de confirmaciones y motivos; no cambia estados por sí misma. */
import { computed, ref, watch } from 'vue'
import DialogoBase from './DialogoBase.vue'
import type { PreguntaOperacion } from '@/composables/useConfirmacion'
const props = defineProps<{ pregunta: PreguntaOperacion }>()
const emit = defineEmits<{ responder: [valor: string | null] }>()
const valor = ref(props.pregunta.inicial ?? '')
/** Restaura el valor inicial si una pregunta sucede inmediatamente a otra. */
watch(() => props.pregunta, p => { valor.value = p.inicial ?? '' })
/** Verificación de longitud y opciones antes de devolver el valor a la vista. */
const valido = computed(() => {
  const p = props.pregunta
  if (p.opciones) return p.opciones.some(o => o.valor === valor.value)
  if (!p.campo) return true
  const longitud = valor.value.trim().length
  return longitud >= (p.minimo ?? 1) && longitud <= (p.maximo ?? 500)
})
/** El formulario emite un valor o una confirmación explícita. */
function confirmar(): void { if (valido.value) emit('responder', props.pregunta.campo || props.pregunta.opciones ? valor.value.trim() : 'CONFIRMADO') }
</script>
<template>
  <DialogoBase :titulo="pregunta.titulo" @cerrar="emit('responder', null)">
    <p class="df-dialog-message">{{ pregunta.mensaje }}</p>
    <form @submit.prevent="confirmar">
      <label v-if="pregunta.opciones" class="df-field df-dialog-field">{{ pregunta.campo || 'Seleccione una opción' }}
        <select v-model="valor" required><option disabled value="">Seleccione</option><option v-for="o in pregunta.opciones" :key="o.valor" :value="o.valor">{{ o.etiqueta }}</option></select>
      </label>
      <label v-else-if="pregunta.campo" class="df-field df-dialog-field">{{ pregunta.campo }}
        <textarea v-model="valor" :minlength="pregunta.minimo ?? 1" :maxlength="pregunta.maximo ?? 500" required></textarea>
        <small class="df-meta">{{ valor.trim().length }} / {{ pregunta.maximo ?? 500 }} caracteres</small>
      </label>
      <div class="df-dialog-actions"><button type="button" class="df-button light" @click="emit('responder', null)">Volver</button><button type="submit" class="df-button" :disabled="!valido">Confirmar ↗</button></div>
    </form>
  </DialogoBase>
</template>
