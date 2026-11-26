<script setup lang="ts">
import { reactive, ref } from 'vue'
import PasswordInput from './PasswordInput.vue'

/**
 * Credenciales validadas que el formulario entrega a la vista de acceso.
 */
export interface LoginCredentials {
  email: string
  password: string
}

/**
 * Contenido legal que puede solicitar el formulario.
 */
export type LegalDocumentType = 'terms' | 'privacy'

/** Estado de la petición gestionada por LoginView; evita envíos duplicados. */
const props = withDefaults(defineProps<{ ocupado?: boolean; mensaje?: string; errorAcceso?: boolean }>(), { ocupado: false, mensaje: '', errorAcceso: false })

const emit = defineEmits<{
  /**
   * Entrega las credenciales validadas al componente padre.
   *
   * LoginForm no realiza directamente la petición HTTP. La integración
   * con Spring Boot se ejecuta en LoginView mediante el servicio de acceso.
   */
  submit: [credentials: LoginCredentials]

  /**
   * Solicita la apertura de los términos o la política de privacidad.
   */
  openLegal: [type: LegalDocumentType]
}>()

const email = ref('')
const password = ref('')
const acceptLegal = ref(false)

const errors = reactive({
  email: '',
  password: '',
  legal: '',
})

/**
 * Elimina los mensajes generados durante la validación anterior.
 */
function clearErrors(): void {
  errors.email = ''
  errors.password = ''
  errors.legal = ''
}

/**
 * Comprueba si el correo tiene una estructura válida.
 *
 * La validación definitiva de la cuenta corresponde al backend.
 *
 * @param value Correo introducido por el usuario.
 * @returns `true` cuando el formato general del correo es válido.
 */
function isValidEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
}

/**
 * Valida los campos obligatorios antes de emitir las credenciales.
 *
 * @returns `true` cuando el formulario puede enviarse.
 */
function validateForm(): boolean {
  clearErrors()

  const normalizedEmail = email.value.trim()

  if (!normalizedEmail) {
    errors.email = 'Ingresa tu correo electrónico.'
  } else if (!isValidEmail(normalizedEmail)) {
    errors.email = 'El correo electrónico no tiene un formato válido.'
  }

  if (!password.value) {
    errors.password = 'Ingresa tu contraseña.'
  } else if (password.value.length < 8) {
    errors.password = 'La contraseña debe tener al menos 8 caracteres.'
  }

  if (!acceptLegal.value) {
    errors.legal = 'Debes aceptar los términos y la política de privacidad.'
  }

  return !errors.email && !errors.password && !errors.legal
}

/**
 * Valida el formulario y entrega los datos al componente padre.
 */
function submitForm(): void {
  if (props.ocupado || !validateForm()) {
    return
  }

  emit('submit', {
    email: email.value.trim().toLowerCase(),
    password: password.value,
  })
}
</script>

<template>
  <article class="login-card">
    <header class="login-header">
      <h1 id="login-title">
        <span>Iniciar sesión</span>
      </h1>
    </header>

    <form id="login-form" :aria-busy="ocupado" novalidate @submit.prevent="submitForm">
      <div class="form-field">
        <label for="email">Correo electrónico</label>

        <input
          id="email"
          v-model="email"
          name="email"
          type="email"
          autocomplete="username"
          placeholder="nombre@institucion.gov"
          :aria-invalid="Boolean(errors.email)"
          aria-describedby="email-error"
          required
        />

        <small id="email-error" class="field-error" aria-live="polite">
          {{ errors.email }}
        </small>
      </div>

      <div class="form-field">
        <label for="password">Contraseña</label>

        <PasswordInput id="password" v-model="password" name="password" autocomplete="current-password" placeholder="Ingresa tu contraseña" minlength="8" :aria-invalid="Boolean(errors.password)" aria-describedby="password-error" required />

        <small id="password-error" class="field-error" aria-live="polite">
          {{ errors.password }}
        </small>
      </div>

      <label class="legal-consent">
        <input
          id="accept-legal"
          v-model="acceptLegal"
          name="acceptLegal"
          type="checkbox"
          required
        />

        <span>
          He leído y acepto los

          <button class="legal-link" type="button" @click="emit('openLegal', 'terms')">
            Términos de uso
          </button>

          y la

          <button class="legal-link" type="button" @click="emit('openLegal', 'privacy')">
            Política de privacidad</button
          >.
        </span>
      </label>

      <small id="legal-error" class="field-error" aria-live="polite">
        {{ errors.legal }}
      </small>

      <p v-if="mensaje && !ocupado" class="df-alert" :class="errorAcceso?'error':'ok'" :role="errorAcceso?'alert':'status'">{{mensaje}}</p>
      <button class="submit-button" type="submit" :disabled="ocupado">
        <span v-if="ocupado" class="df-inline-spinner" aria-hidden="true"></span>
        <span>{{ ocupado ? 'Verificando acceso…' : 'Iniciar sesión' }}</span>
      </button>
    </form>
    <!-- Acceso al alta institucional claramente visible en la tarjeta. -->
    <div class="account-access-links" aria-label="Opciones de acceso">
      <p>¿Aún no tienes cuenta?</p>
      <RouterLink class="create-account-link" to="/registro">Crear cuenta de cliente →</RouterLink>
      <RouterLink class="browse-catalog-link" to="/catalogo">Ver vehículos sin registrarme</RouterLink>
    </div>
  </article>
</template>
