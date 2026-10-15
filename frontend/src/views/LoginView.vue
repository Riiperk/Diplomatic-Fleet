<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import '@/assets/styles/login.css'
import LegalModal from '@/components/login/LegalModal.vue'
import LoginForm, {
  type LegalDocumentType,
  type LoginCredentials,
} from '@/components/login/LoginForm.vue'
import VisualPanel from '@/components/login/VisualPanel.vue'
import { guardarCredenciales } from '@/services/vehiculoService'

/**
 * Vista principal del inicio de sesión.
 *
 * Coordina el panel visual, el formulario y los documentos legales.
 * Las credenciales validadas se almacenan temporalmente durante la
 * sesión para autenticar las peticiones enviadas a la API.
 */
const router = useRouter()

const activeLegalModal = ref<LegalDocumentType | null>(null)
const loginMessage = ref('')
const isLoginError = ref(false)

/**
 * Abre el documento legal solicitado desde el formulario.
 *
 * @param type Documento legal que debe mostrarse.
 */
function openLegalModal(type: LegalDocumentType): void {
  activeLegalModal.value = type
}

/**
 * Cierra el documento legal activo.
 */
function closeLegalModal(): void {
  activeLegalModal.value = null
}

/**
 * Guarda temporalmente las credenciales de la demo y abre
 * la vista de gestión de vehículos.
 *
 * Durante esta fase, Spring Security utiliza el usuario técnico `user`.
 * La contraseña introducida será validada cuando la vista de vehículos
 * realice la primera petición a la API protegida.
 *
 * @param credentials Credenciales normalizadas por el formulario.
 */
async function handleLogin(
  credentials: LoginCredentials,
): Promise<void> {
  isLoginError.value = false
  loginMessage.value = 'Iniciando sesión...'

  guardarCredenciales(
    'user',
    credentials.password,
  )

  await router.push('/vehiculos')
}
</script>

<template>
  <main class="login-page">
    <VisualPanel />

    <section class="access-panel" aria-labelledby="login-title">
      <div class="access-halftone" aria-hidden="true"></div>
      <div class="access-shard" aria-hidden="true"></div>

      <LoginForm
        @submit="handleLogin"
        @open-legal="openLegalModal"
      />

      <p
        v-if="loginMessage"
        class="form-message login-view-message"
        :class="{
          'is-error': isLoginError,
          'is-ok': !isLoginError,
        }"
        role="status"
        aria-live="polite"
      >
        {{ loginMessage }}
      </p>

      <div class="academic-seal" aria-hidden="true">
        <span>DF</span>
        <small>Demo académica</small>
      </div>

      <p class="decorative-caption-right">
        Control · Seguridad · Confianza
      </p>
    </section>

    <LegalModal
      v-if="activeLegalModal"
      :open="Boolean(activeLegalModal)"
      :type="activeLegalModal"
      @close="closeLegalModal"
    />
  </main>
</template>
