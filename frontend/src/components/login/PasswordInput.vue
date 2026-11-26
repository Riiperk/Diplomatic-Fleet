<script setup lang="ts">
import {ref} from 'vue'
defineOptions({inheritAttrs:false})
defineProps<{id:string}>()
const model=defineModel<string>({required:true})
const visible=ref(false)
</script>
<template>
  <div class="df-password-row">
    <input v-bind="$attrs" :id="id" v-model="model" :type="visible?'text':'password'" />
    <button class="df-password-action" type="button" :aria-controls="id" :aria-pressed="visible" :aria-label="visible?'Ocultar contraseña':'Mostrar contraseña'" @click="visible=!visible">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="2.5"/><path v-if="visible" d="m3 3 18 18"/></svg>
      <span>{{visible?'Ocultar':'Mostrar'}}</span>
    </button>
  </div>
</template>
<style scoped>
.df-password-row{display:grid;grid-template-columns:minmax(0,1fr) auto;align-items:stretch;gap:0;min-width:0;width:100%}
.df-password-row input{min-width:0;width:100%;margin:0;box-sizing:border-box}
.df-password-row input::-ms-reveal,.df-password-row input::-ms-clear{display:none}
.df-password-action{position:relative;z-index:2;display:flex;align-items:center;justify-content:center;gap:5px;min-width:84px;min-height:44px;padding:8px;color:#111;background:#f6f3ed;border:2px solid #111;border-left:0;font:700 11px/1.15 Arial,sans-serif;cursor:pointer;white-space:nowrap;transform:none;box-shadow:none}
.df-password-action:hover,.df-password-action[aria-pressed=true]{background:#d71934;color:#fff}
.df-password-action:focus-visible{outline:3px solid #e82a42;outline-offset:2px}
.df-password-action svg{flex:none}
@media(max-width:380px){.df-password-action{min-width:65px;padding:6px;gap:3px}.df-password-action svg{width:16px;height:16px}}
</style>
