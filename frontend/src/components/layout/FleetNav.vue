<script setup lang="ts">
/** Navegación pública que adapta enlaces según el perfil confirmado por el servidor. */
import { ref,onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { sesion,logout,api,inicioPerfil,type Perfil } from '@/services/api'
const router=useRouter();const perfil=ref<Perfil|null>(sesion())
onMounted(async()=>{if(perfil.value){try{perfil.value=await api<Perfil>('/api/auth/me')}catch{logout();perfil.value=null}}})
/** Elimina la sesión local y vuelve al formulario de acceso. */
async function salir(){logout();perfil.value=null;await router.push('/login')}
</script>
<template><header class="df-nav"><RouterLink to="/catalogo" class="df-logo"><b>DF</b><span>DIPLOMATIC <em>FLEET</em></span></RouterLink>
<nav class="df-links"><RouterLink to="/catalogo">Catálogo</RouterLink><RouterLink v-if="perfil?.rol==='CLIENTE'" to="/mis-reservas">Mis reservas</RouterLink><RouterLink v-if="perfil?.rol==='CLIENTE'" to="/mis-cotizaciones">Cotizaciones</RouterLink><RouterLink v-if="perfil?.rol==='ADMINISTRADOR'||perfil?.rol==='OPERACIONES'" :to="inicioPerfil(perfil.rol)">Administración</RouterLink><RouterLink v-if="perfil?.rol==='CONDUCTOR'" to="/asignaciones">Mis servicios</RouterLink><RouterLink v-if="!perfil" to="/login">Ingresar</RouterLink><RouterLink v-if="!perfil" to="/registro">Registrarme</RouterLink><button v-if="perfil" type="button" @click="salir">Salir · {{perfil.nombre}}</button></nav></header></template>
