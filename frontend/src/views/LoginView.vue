<script setup lang="ts">
import { ref } from 'vue'
import { useRouter,useRoute } from 'vue-router'
import '@/assets/styles/login.css'
import LegalModal from '@/components/login/LegalModal.vue'
import LoginForm, { type LegalDocumentType, type LoginCredentials } from '@/components/login/LoginForm.vue'
import VisualPanel from '@/components/login/VisualPanel.vue'
import { acceso, guardarSesion,inicioPerfil } from '@/services/api'
const router=useRouter()
const route=useRoute()
const activeLegalModal=ref<LegalDocumentType|null>(null)
const loginMessage=ref(route.query.registrado==='1'?'Cuenta creada. Ahora inicia sesión.':'')
const isLoginError=ref(false)
const isAuthenticating=ref(false)
/** Valida la identidad contra Kotlin, establece la sesión y navega según el rol. */
async function handleLogin(credentials:LoginCredentials){
  if (isAuthenticating.value) return
  isAuthenticating.value=true;loginMessage.value='Comprobando acceso…';isLoginError.value=false
  try{
    const sesionWeb=await acceso(credentials.email.trim().toLowerCase(),credentials.password)
    guardarSesion(sesionWeb)
    const perfil=sesionWeb.perfil
    const destino=router.currentRoute.value.query.destino
    await router.push(typeof destino==='string'&&destino.startsWith('/')?destino:inicioPerfil(perfil.rol))
  }catch(e){isLoginError.value=true;loginMessage.value=e instanceof Error?e.message:'No se pudo iniciar sesión'}
  finally{isAuthenticating.value=false}
}
</script>
<template>
  <main class="login-page">
    <VisualPanel />
    <section class="access-panel" aria-labelledby="login-title">
      <div class="access-halftone" aria-hidden="true"></div>
      <div class="access-shard" aria-hidden="true"></div>
      <div class="login-stage">
        <LoginForm :ocupado="isAuthenticating" :mensaje="loginMessage" :error-acceso="isLoginError" @submit="handleLogin" @open-legal="activeLegalModal=$event" />
      </div>
      <!-- Crear cuenta y catálogo aparecen dentro de LoginForm. -->
      <div class="academic-seal" aria-hidden="true"><span>DF</span><small>Plataforma de alquiler</small></div>
      <p class="decorative-caption-right">Control · Seguridad · Confianza</p>
    </section>
    <LegalModal v-if="activeLegalModal" :open="Boolean(activeLegalModal)" :type="activeLegalModal" @close="activeLegalModal=null" />
  </main>
</template>
<style scoped>.df-login-links{position:relative;z-index:3;display:flex;flex-wrap:wrap;gap:15px;margin:12px auto 70px;justify-content:center;max-width:460px}.df-login-links a{font-size:12px;font-weight:900;text-transform:uppercase;color:#111;border-bottom:2px solid #d51d30}.df-login-links a:hover{color:#d51d30}</style>
