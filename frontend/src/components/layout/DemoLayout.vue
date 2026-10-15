<script setup lang="ts">
import { ref } from 'vue'
import DemoSidebar from '@/components/layout/DemoSidebar.vue'

/**
 * Propiedades utilizadas para identificar cada módulo.
 */
interface DemoLayoutProps {
  moduleNumber: string
  eyebrow: string
  title: string
  description: string
}

defineProps<DemoLayoutProps>()

const mobileMenuVisible = ref(false)

/**
 * Alterna la navegación en pantallas pequeñas.
 */
function toggleMobileMenu(): void {
  mobileMenuVisible.value = !mobileMenuVisible.value
}

/**
 * Cierra la navegación móvil.
 */
function closeMobileMenu(): void {
  mobileMenuVisible.value = false
}
</script>

<template>
  <main class="demo-layout">
    <div
      class="background-pattern"
      aria-hidden="true"
    ></div>

    <button
      class="mobile-menu-button"
      type="button"
      aria-label="Abrir navegación"
      :aria-expanded="mobileMenuVisible"
      @click="toggleMobileMenu"
    >
      <span></span>
      <span></span>
      <span></span>
    </button>

    <button
      v-if="mobileMenuVisible"
      class="mobile-overlay"
      type="button"
      aria-label="Cerrar navegación"
      @click="closeMobileMenu"
    ></button>

    <DemoSidebar
      :mobile-visible="mobileMenuVisible"
      @close-mobile="closeMobileMenu"
    />

    <section class="module-workspace">
      <header class="module-hero">
        <div
          class="hero-black-shape"
          aria-hidden="true"
        ></div>

        <div
          class="hero-red-shape"
          aria-hidden="true"
        ></div>

        <div class="hero-content">
          <p class="hero-eyebrow">
            {{ eyebrow }}
          </p>

          <h1>
            {{ title }}
          </h1>

          <p class="hero-description">
            {{ description }}
          </p>
        </div>

        <div
          class="module-number"
          aria-hidden="true"
        >
          <span>Módulo</span>
          <strong>{{ moduleNumber }}</strong>
        </div>
      </header>

      <section class="module-content">
        <slot />
      </section>

      <footer class="module-footer">
        <div>
          <strong>Diplomatic Fleet</strong>
          <span>Demo académica · 2026</span>
        </div>

        <p>
          Vue.js · Spring Boot · PostgreSQL
        </p>
      </footer>
    </section>
  </main>
</template>

<style scoped>
:global(*) {
  box-sizing: border-box;
}

:global(html) {
  scroll-behavior: smooth;
}

:global(body) {
  margin: 0;
  overflow-x: hidden;
  color: #151515;
  background: #efece5;
  font-family:
    Inter, Arial, Helvetica, sans-serif;
}

:global(button),
:global(input),
:global(select),
:global(textarea) {
  font: inherit;
}

.demo-layout {
  position: relative;
  min-height: 100vh;
  background:
    linear-gradient(
      118deg,
      rgb(255 255 255 / 90%),
      rgb(239 236 229 / 94%)
    );
}

.background-pattern {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  opacity: 0.28;
  background-image:
    radial-gradient(
      circle at 14% 16%,
      rgb(215 25 32 / 16%) 0 2px,
      transparent 3px
    ),
    radial-gradient(
      circle at 86% 72%,
      rgb(17 17 17 / 11%) 0 2px,
      transparent 3px
    );
  background-size: 28px 28px, 35px 35px;
  mask-image:
    linear-gradient(
      to bottom,
      black,
      transparent 90%
    );
}

.module-workspace {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  margin-left: 298px;
  padding: 30px 34px 22px;
}

.module-hero {
  position: relative;
  min-height: 190px;
  padding: 35px 42px;
  overflow: hidden;
  color: #ffffff;
  background: #d71920;
  clip-path:
    polygon(
      0 5%,
      96% 0,
      100% 79%,
      93% 100%,
      3% 93%
    );
}

.hero-black-shape {
  position: absolute;
  inset: -75px auto -90px -65px;
  width: 72%;
  background: #111111;
  transform: rotate(-4deg);
}

.hero-black-shape::after {
  position: absolute;
  top: 35px;
  right: -43px;
  width: 82px;
  height: 100%;
  content: "";
  background: #ffffff;
  transform: skew(-9deg);
}

.hero-red-shape {
  position: absolute;
  top: -70px;
  right: 11%;
  width: 205px;
  height: 350px;
  border: 21px solid rgb(17 17 17 / 70%);
  transform: rotate(31deg);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 720px;
}

.hero-eyebrow {
  display: inline-block;
  margin: 0 0 8px;
  padding: 5px 10px;
  color: #111111;
  background: #c9a34f;
  font-size: 0.67rem;
  font-weight: 950;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  transform: rotate(-1deg);
}

.hero-content h1 {
  margin: 0;
  font-size: clamp(2.7rem, 5vw, 5.2rem);
  font-weight: 1000;
  letter-spacing: -0.08em;
  line-height: 0.9;
  text-transform: uppercase;
  transform: rotate(-1deg);
}

.hero-description {
  max-width: 560px;
  margin: 16px 0 0;
  color: rgb(255 255 255 / 74%);
  font-size: 0.92rem;
  line-height: 1.55;
}

.module-number {
  position: absolute;
  right: 5%;
  bottom: 25px;
  z-index: 3;
  display: grid;
  min-width: 145px;
  justify-items: center;
  padding: 14px 20px;
  color: #111111;
  background: #ffffff;
  clip-path:
    polygon(
      8% 0,
      100% 10%,
      92% 100%,
      0 89%
    );
  transform: rotate(2deg);
}

.module-number span {
  font-size: 0.61rem;
  font-weight: 900;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.module-number strong {
  margin: -5px 0;
  color: #d71920;
  font-size: 3.5rem;
  font-weight: 1000;
  letter-spacing: -0.12em;
}

.module-content {
  margin-top: 30px;
}

.module-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 38px;
  padding: 14px 19px;
  color: #ffffff;
  background: #111111;
  font-size: 0.65rem;
  text-transform: uppercase;
  transform: skew(-1deg);
}

.module-footer div {
  display: flex;
  align-items: center;
  gap: 10px;
}

.module-footer strong {
  color: #c9a34f;
}

.module-footer p {
  margin: 0;
  color: rgb(255 255 255 / 54%);
}

.mobile-menu-button {
  display: none;
}

.mobile-overlay {
  display: none;
}

@media (max-width: 1050px) {
  .module-workspace {
    margin-left: 298px;
    padding-right: 22px;
    padding-left: 24px;
  }
}

@media (max-width: 820px) {
  .mobile-menu-button {
    position: fixed;
    top: 17px;
    left: 16px;
    z-index: 60;
    display: grid;
    width: 48px;
    height: 43px;
    align-content: center;
    gap: 5px;
    padding: 8px;
    border: 3px solid #ffffff;
    background: #d71920;
    box-shadow: 4px 4px 0 #111111;
    cursor: pointer;
    transform: rotate(-2deg);
  }

  .mobile-menu-button span {
    display: block;
    width: 100%;
    height: 3px;
    background: #ffffff;
  }

  .mobile-overlay {
    position: fixed;
    inset: 0;
    z-index: 35;
    display: block;
    border: 0;
    background: rgb(0 0 0 / 64%);
  }

  .module-workspace {
    margin-left: 0;
    padding: 76px 18px 20px;
  }

  .module-hero {
    min-height: 250px;
    padding: 35px 25px;
  }

  .hero-black-shape {
    width: 87%;
  }

  .module-number {
    right: 8%;
    bottom: 18px;
  }
}

@media (max-width: 560px) {
  .module-workspace {
    padding-right: 12px;
    padding-left: 12px;
  }

  .module-hero {
    min-height: 285px;
  }

  .hero-content h1 {
    font-size: 2.7rem;
  }

  .module-number {
    right: 7%;
    bottom: 13px;
    transform: scale(0.84) rotate(2deg);
    transform-origin: right bottom;
  }

  .module-footer {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .module-footer div {
    align-items: flex-start;
    flex-direction: column;
    gap: 3px;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    scroll-behavior: auto !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
