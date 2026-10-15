<script setup lang="ts">
import { ref } from 'vue'
import '@/assets/styles/login.css'
import LegalModal from '@/components/login/LegalModal.vue'
import LoginForm, {
  type LegalDocumentType,
  type LoginCredentials,
} from '@/components/login/LoginForm.vue'
import VisualPanel from '@/components/login/VisualPanel.vue'

/**
 * Vista principal del inicio de sesión.
 *
 * Coordina el panel visual, el formulario y los documentos legales.
 * La vista recibe las credenciales validadas por LoginForm, pero todavía
 * no realiza la autenticación porque el endpoint de Spring Boot se
 * implementará en una fase posterior.
 */
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
 * Recibe las credenciales validadas por LoginForm.
 *
 * Esta implementación temporal no guarda ni compara contraseñas.
 * La autenticación real se conectará posteriormente con el endpoint
 * seguro del backend.
 *
 * @param credentials Credenciales normalizadas por el formulario.
 */
function handleLogin(credentials: LoginCredentials): void {
  isLoginError.value = false
  loginMessage.value =
    `Formulario validado para ${credentials.email}. ` + 'La conexión con el backend está pendiente.'
}
</script>

<template>
  <main class="login-page">
    <VisualPanel />

    <section class="access-panel" aria-labelledby="login-title">
      <div class="access-halftone" aria-hidden="true"></div>
      <div class="access-shard" aria-hidden="true"></div>

      <LoginForm @submit="handleLogin" @open-legal="openLegalModal" />

      <p
        v-if="loginMessage"
        class="form-message login-view-message"
        :class="{ 'is-error': isLoginError, 'is-ok': !isLoginError }"
        role="status"
        aria-live="polite"
      >
        {{ loginMessage }}
      </p>

      <div class="academic-seal" aria-hidden="true">
        <span>DF</span>
        <small>Demo académica</small>
      </div>

      <p class="decorative-caption-right">Control · Seguridad · Confianza</p>
    </section>

    <LegalModal
      v-if="activeLegalModal"
      :open="Boolean(activeLegalModal)"
      :type="activeLegalModal"
      @close="closeLegalModal"
    />
  </main>
</template>
