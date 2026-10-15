<script setup lang="ts">
import diplomaticSedanUrl from '@/assets/images/diplomatic-sedan.png'
import starThreeUrl from '@/assets/images/estrella_3.png'
import starSixUrl from '@/assets/images/estrella_6.png'
import starSevenUrl from '@/assets/images/estrella_7.png'
import starNineUrl from '@/assets/images/estrella_9.png'

/**
 * Panel visual del inicio de sesión.
 *
 * Presenta la identidad gráfica de Diplomatic Fleet mediante el sedán
 * ejecutivo, las estrellas decorativas y el mensaje institucional.
 *
 * El componente no contiene lógica de autenticación porque su
 * responsabilidad es exclusivamente visual.
 */
</script>

<template>
  <section class="visual-panel" aria-labelledby="brand-title">
    <div class="halftone-texture" aria-hidden="true"></div>
    <div class="shard shard-black" aria-hidden="true"></div>
    <div class="shard shard-white" aria-hidden="true"></div>
    <div class="shard shard-red-dark" aria-hidden="true"></div>
    <div class="institutional-silhouette" aria-hidden="true"></div>

    <!-- Las estrellas son decorativas y no aportan información al lector de pantalla. -->
    <img class="star star-one" :src="starThreeUrl" alt="" aria-hidden="true" />
    <img class="star star-two" :src="starSixUrl" alt="" aria-hidden="true" />
    <img class="star star-three" :src="starSevenUrl" alt="" aria-hidden="true" />
    <img class="star star-four" :src="starNineUrl" alt="" aria-hidden="true" />

    <div class="visual-stack">
      <div id="brand-title" class="brand-lockup">
        <span class="brand-main">Diplomatic</span>
        <span class="brand-secondary">Fleet</span>
        <span class="brand-subtitle">Movilidad institucional</span>
      </div>

      <div class="visual-content">
        <p class="visual-heading">
          <span>Servicios vehiculares para</span>
        </p>

        <p class="visual-heading">
          <span>misiones diplomáticas</span>
        </p>

        <p class="visual-description">
          Gestión segura de vehículos, solicitudes y conductores para entidades institucionales.
        </p>
      </div>
    </div>

    <div class="vehicle-container">
      <div class="car-shard" aria-hidden="true"></div>

      <img :src="diplomaticSedanUrl" alt="Sedán ejecutivo sin distintivos comerciales" />
    </div>

    <div class="bottom-stripes" aria-hidden="true"></div>

    <p class="decorative-caption decorative-caption-left">Destinos que representan</p>
  </section>
</template>
