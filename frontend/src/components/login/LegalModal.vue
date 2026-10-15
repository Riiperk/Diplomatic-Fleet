<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'

type LegalModalType = 'terms' | 'privacy'

interface Props {
  /**
   * Indica si el cuadro de diálogo debe permanecer abierto.
   */
  open: boolean

  /**
   * Define el documento legal que se mostrará.
   */
  type: LegalModalType
}

const props = defineProps<Props>()

const emit = defineEmits<{
  /**
   * Solicita al componente padre cerrar el cuadro de diálogo.
   */
  close: []
}>()

const dialogRef = ref<HTMLDialogElement | null>(null)

/**
 * Obtiene el título correspondiente al documento seleccionado.
 */
const modalTitle = computed(() =>
  props.type === 'terms' ? 'Términos de uso' : 'Política de privacidad',
)

/**
 * Abre o cierra el elemento dialog de acuerdo con la propiedad `open`.
 */
async function syncDialogState(): Promise<void> {
  await nextTick()

  const dialog = dialogRef.value

  if (!dialog) {
    return
  }

  if (props.open && !dialog.open) {
    dialog.showModal()
    return
  }

  if (!props.open && dialog.open) {
    dialog.close()
  }
}

/*
 * El modal se sincroniza al montarse porque LoginView lo crea
 * directamente con la propiedad `open` establecida en true.
 */
onMounted(syncDialogState)

watch(() => props.open, syncDialogState, {
  flush: 'post',
})

/**
 * Solicita al componente padre cerrar el modal.
 */
function closeModal(): void {
  emit('close')
}

/**
 * Permite cerrar el modal al seleccionar el fondo oscuro exterior.
 *
 * @param event Evento de selección recibido por el elemento dialog.
 */
function closeOnBackdrop(event: MouseEvent): void {
  if (event.target === dialogRef.value) {
    closeModal()
  }
}
</script>

<template>
  <dialog
    ref="dialogRef"
    class="legal-modal"
    :aria-labelledby="`${type}-modal-title`"
    @click="closeOnBackdrop"
    @close="closeModal"
  >
    <article>
      <header class="modal-header">
        <h2 :id="`${type}-modal-title`">
          {{ modalTitle }}
        </h2>

        <button
          class="modal-close"
          type="button"
          :aria-label="`Cerrar ${modalTitle.toLowerCase()}`"
          @click="closeModal"
        >
          ×
        </button>
      </header>

      <div v-if="type === 'terms'" class="modal-content">
        <p>
          Diplomatic Fleet es una aplicación académica creada para demostrar procesos de gestión de
          servicios vehiculares institucionales.
        </p>

        <h3>Uso autorizado</h3>

        <p>
          El acceso está limitado a las personas autorizadas por el equipo responsable del proyecto.
        </p>

        <h3>Datos de demostración</h3>

        <p>
          Durante las pruebas deben utilizarse datos ficticios. No deben registrarse contraseñas
          reales, documentos de identidad ni información institucional confidencial.
        </p>

        <h3>Alcance</h3>

        <p>
          La demostración no garantiza la disponibilidad ni la conservación permanente de la
          información introducida.
        </p>
      </div>

      <div v-else class="modal-content">
        <p>
          Los datos introducidos se utilizan únicamente para demostrar las funcionalidades
          académicas del sistema.
        </p>

        <h3>Credenciales</h3>

        <p>
          Las contraseñas no deben almacenarse en texto plano. El backend deberá protegerlas
          mediante un algoritmo seguro.
        </p>

        <h3>Información personal</h3>

        <p>No deben utilizarse datos personales reales sin autorización expresa.</p>
      </div>
    </article>
  </dialog>
</template>
