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
import {
  eliminarCredenciales,
  guardarCredenciales,
  listarVehiculos,
} from '@/services/vehiculoService'

/**
 * Vista principal del inicio de sesión.
 *
 * Coordina el panel visual, el formulario y los documentos legales.
 * Antes de permitir el acceso, verifica el correo autorizado para
 * la demo y comprueba la contraseña mediante una petición protegida
 * al backend.
 */
const router = useRouter()

const activeLegalModal = ref<LegalDocumentType | null>(null)
const loginMessage = ref('')
const isLoginError = ref(false)
const isAuthenticating = ref(false)

/**
 * Correo temporal habilitado para acceder a la demo académica.
 *
 * La autenticación definitiva utilizará los usuarios y roles
 * almacenados en PostgreSQL.
 */
const DEMO_EMAIL = 'admin@diplomaticfleet.co'

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
 * Comprueba el correo autorizado y valida la contraseña
 * contra la API antes de permitir el acceso.
 *
 * Durante esta fase, Spring Security utiliza el usuario técnico
 * `user`. La contraseña se comprueba mediante una consulta
 * autenticada al endpoint de vehículos.
 *
 * @param credentials Credenciales normalizadas por el formulario.
 */
async function handleLogin(
  credentials: LoginCredentials,
): Promise<void> {
  if (isAuthenticating.value) {
    return
  }

  isAuthenticating.value = true
  isLoginError.value = false
  loginMessage.value = 'Verificando credenciales...'

  const normalizedEmail = credentials.email
    .trim()
    .toLowerCase()

  if (normalizedEmail !== DEMO_EMAIL) {
    eliminarCredenciales()
    isLoginError.value = true
    loginMessage.value =
      'Correo o contraseña incorrectos. Verifica los datos e inténtalo nuevamente.'
    isAuthenticating.value = false
    return
  }

  guardarCredenciales(
    'user',
    credentials.password,
  )

  try {
    // Esta consulta protegida confirma que la contraseña
    // proporcionada es aceptada por Spring Security.
    await listarVehiculos()

    loginMessage.value = 'Acceso autorizado.'

    await router.push('/inicio')
  } catch {
    eliminarCredenciales()
    isLoginError.value = true
    loginMessage.value =
      'Correo o contraseña incorrectos. Verifica los datos e inténtalo nuevamente.'
  } finally {
    isAuthenticating.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <VisualPanel />

    <section
      class="access-panel"
      aria-labelledby="login-title"
    >
      <div
        class="access-halftone"
        aria-hidden="true"
      ></div>

      <div
        class="access-shard"
        aria-hidden="true"
      ></div>

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

      <div
        class="academic-seal"
        aria-hidden="true"
      >
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
