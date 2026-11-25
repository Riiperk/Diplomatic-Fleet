<script setup lang="ts">
/**
 * Presenta la URL registrada en PostgreSQL y su crédito cuando corresponde.
 * Una imagen inválida o caída muestra un estado explícito, nunca otra marca.
 */
import { computed, ref, watch } from 'vue'
import { creditoFoto, urlFoto } from '@/services/fotografias'
const props = defineProps<{ url?: string | null; modelo: string }>()
const fuente = computed(() => urlFoto(props.url))
const credito = computed(() => creditoFoto(fuente.value))
const fallo = ref(false)
watch(fuente, () => { fallo.value = false })
/** El error de red no bloquea el resto de la ficha ni la reserva. */
function mostrarError() { fallo.value = true }
</script>
<template>
  <figure class="df-foto">
    <div class="df-foto-lienzo">
      <img v-if="fuente && !fallo" :key="fuente" :src="fuente" :alt="'Fotografía de referencia: ' + modelo" loading="lazy" decoding="async" referrerpolicy="no-referrer" @error="mostrarError" />
      <div v-else class="df-foto-vacia" role="status"><span aria-hidden="true">DF</span><strong>Fotografía no disponible</strong><small>{{ modelo }}</small></div>
    </div>
    <figcaption v-if="credito" class="df-foto-credito"><a :href="credito.fuente" target="_blank" rel="noopener noreferrer">{{ credito.autor }}</a> · <a :href="credito.licenciaUrl" target="_blank" rel="noopener noreferrer">{{ credito.licencia }}</a></figcaption>
    <figcaption v-else-if="fuente && !fallo" class="df-foto-credito">Imagen registrada por administración</figcaption>
  </figure>
</template>
<style scoped>
.df-foto{margin:0;display:flex;flex-direction:column;width:100%;min-width:0;height:100%;background:#121216;color:#f2efea;}
.df-foto-lienzo{position:relative;flex:1;min-height:0;aspect-ratio:16/10;overflow:hidden;background:#19191e;display:grid;place-items:center;}
.df-foto-lienzo img{display:block;width:100%;height:100%;position:absolute;inset:0;object-fit:cover;object-position:center;transition:transform .32s cubic-bezier(.2,.8,.2,1);}
.df-foto-vacia{display:grid;text-align:center;justify-items:center;gap:5px;padding:15px;font-size:.8rem;}
.df-foto-vacia span{font:900 2rem/1 Impact,sans-serif;color:#ff455d;transform:skew(-8deg);}
.df-foto-vacia small{color:#bfbfc5;}
.df-foto-credito{padding:6px 10px;font-size:10px;line-height:1.3;min-height:26px;background:#101014;color:#d3d3d8;position:relative;z-index:3;}
.df-foto-credito a{color:inherit;text-decoration:underline;text-underline-offset:2px;}
@media(prefers-reduced-motion:reduce){.df-foto-lienzo img{transition:none!important}}
</style>
