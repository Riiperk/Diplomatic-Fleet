<script setup lang="ts">
/** Componente Registro: vista navegable conectada con las rutas y servicios del sistema. */
import { reactive,ref } from 'vue'
import { useRouter,RouterLink } from 'vue-router'
import { registrar,type Registro } from '@/services/api'
import PasswordInput from '@/components/login/PasswordInput.vue'
const router=useRouter()
const form=reactive<Registro>({nombre:'',apellido:'',correo:'',password:'',nombreEntidad:'',tipoEntidad:'EMBAJADA',ciudad:'',direccion:'',numeroIdentificacionFiscal:'',telefono:''})
const mensaje=ref('');const guardando=ref(false);const confirmarPassword=ref('');const errorClave=ref('')
/** Valida el registro y solicita crear institución y cuenta en PostgreSQL. */
async function submit(){
  if (guardando.value) return
  mensaje.value='';errorClave.value=''
  if(form.password!==confirmarPassword.value){errorClave.value='Las contraseñas no coinciden.';return}
  guardando.value=true
  try{await registrar({...form,correo:form.correo.toLowerCase().trim()});await router.push({path:'/login',query:{registrado:'1'}})}
  catch(e){mensaje.value=e instanceof Error?e.message:'No se pudo registrar'}finally{guardando.value=false}
}
</script>
<template>
<div class="df-auth">
  <section class="df-auth-art"><span class="df-kicker">DIPLOMATIC FLEET / ACCESO</span><h1>Acceso<br><span class="df-highlight">institucional.</span></h1><p>Registre una cuenta institucional para solicitar vehículos y consultar sus reservas.</p></section>
  <section class="df-auth-main"><div class="df-auth-card"><span class="df-kicker">Nuevo registro</span><h2>Crear cuenta.</h2>
    <form @submit.prevent="submit">
      <div class="df-fields"><div class="df-field"><label>Nombre</label><input v-model="form.nombre" required maxlength="70" autocomplete="given-name" /></div><div class="df-field"><label>Apellido</label><input v-model="form.apellido" required maxlength="70" autocomplete="family-name" /></div></div>
      <div class="df-field"><label>Correo institucional</label><input v-model="form.correo" type="email" autocomplete="username" required /></div>
      <div class="df-field"><label for="registro-clave">Contraseña (mín. 8 caracteres)</label><PasswordInput id="registro-clave" v-model="form.password" minlength="8" maxlength="72" autocomplete="new-password" required /></div>
      <div class="df-field"><label for="registro-confirmar">Confirmar contraseña</label><PasswordInput id="registro-confirmar" v-model="confirmarPassword" minlength="8" maxlength="72" autocomplete="new-password" :aria-invalid="!!errorClave" aria-describedby="registro-clave-error" @input="errorClave=''" required /><p v-if="errorClave" id="registro-clave-error" class="df-alert error" role="alert">{{errorClave}}</p></div>
      <div class="df-field"><label>Nombre de la institución</label><input v-model="form.nombreEntidad" required maxlength="150" /></div>
      <div class="df-fields"><div class="df-field"><label>Tipo de entidad</label><select v-model="form.tipoEntidad"><option value="EMBAJADA">Embajada</option><option value="CONSULADO">Consulado</option><option value="ORGANISMO_INTERNACIONAL">Organismo internacional</option><option value="OTRO">Otra institución</option></select></div><div class="df-field"><label>Teléfono (opcional)</label><input v-model="form.telefono" maxlength="30" type="tel" /></div></div>
      <!-- El modelo institucional guarda ciudad y direccion al crear la cuenta, para mostrar ubicacion en administracion. -->
      <div class="df-fields"><div class="df-field"><label>Ciudad de la institución *</label><input v-model="form.ciudad" required minlength="2" maxlength="100" placeholder="Bogotá" autocomplete="address-level2" /></div><div class="df-field"><label>Identificación fiscal / NIT (opcional)</label><input v-model="form.numeroIdentificacionFiscal" maxlength="40" placeholder="NIT institucional" /></div></div>
      <div class="df-field"><label>Dirección institucional *</label><input v-model="form.direccion" required minlength="5" maxlength="255" placeholder="Calle 00 # 00-00" autocomplete="street-address" /></div>
      <p class="df-meta">* Ciudad y dirección son obligatorias y quedan guardadas en PostgreSQL. El registro no supone acreditación diplomática verificada.</p>
      <p v-if="mensaje" class="df-alert error" role="alert">{{mensaje}}</p>
      <button class="df-button" :disabled="guardando" type="submit"><span v-if="guardando" class="df-inline-spinner" aria-hidden="true"></span>{{guardando?'Creando cuenta…':'Crear cuenta →'}}</button>
    </form><div class="df-links-auth"><RouterLink to="/login">Ya tengo cuenta</RouterLink><RouterLink to="/catalogo">Ver catálogo</RouterLink></div>
  </div></section>
</div>
</template>
