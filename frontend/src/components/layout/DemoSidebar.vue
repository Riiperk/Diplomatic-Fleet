<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { eliminarCredenciales } from '@/services/vehiculoService'

/**
 * Elemento disponible en la navegación principal.
 */
interface NavigationItem {
  number: string
  label: string
  description: string
  route: string
}

/**
 * Propiedades recibidas por la navegación lateral.
 */
interface SidebarProps {
  mobileVisible?: boolean
}

withDefaults(
  defineProps<SidebarProps>(),
  {
    mobileVisible: false,
  },
)

const emit = defineEmits<{
  /**
   * Solicita al componente padre cerrar la navegación móvil.
   */
  closeMobile: []
}>()

const route = useRoute()
const router = useRouter()

/**
 * Módulos funcionales mostrados en la demo académica.
 *
 * Cada elemento apunta a una vista específica para conservar
 * una navegación coherente entre las diferentes historias de usuario.
 */
const navigationItems: NavigationItem[] = [
  {
    number: '01',
    label: 'Inicio',
    description: 'Resumen general',
    route: '/inicio',
  },
  {
    number: '02',
    label: 'Clientes',
    description: 'Entidades institucionales',
    route: '/clientes',
  },
  {
    number: '03',
    label: 'Vehículos',
    description: 'Registro y consulta',
    route: '/vehiculos',
  },
  {
    number: '04',
    label: 'Documentos',
    description: 'Vigencias y control',
    route: '/documentos',
  },
  {
    number: '05',
    label: 'Mantenimientos',
    description: 'Historial operativo',
    route: '/mantenimientos',
  },
  {
    number: '06',
    label: 'Disponibilidad',
    description: 'Consulta por periodo',
    route: '/disponibilidad',
  },
  {
    number: '07',
    label: 'Solicitudes',
    description: 'Servicios institucionales',
    route: '/solicitudes',
  },
  {
    number: '08',
    label: 'Conductor',
    description: 'Validación documental',
    route: '/validacion-conductor',
  },
  {
    number: '09',
    label: 'Cotizaciones',
    description: 'Propuestas comerciales',
    route: '/cotizaciones',
  },
  {
    number: '10',
    label: 'Usuarios y roles',
    description: 'Seguridad y permisos',
    route: '/usuarios',
  },
]

/**
 * Obtiene la ruta que se encuentra seleccionada.
 */
const activeRoute = computed(
  (): string => route.path,
)

/**
 * Navega hacia el módulo seleccionado.
 *
 * @param target Dirección interna del módulo.
 */
async function navigateTo(target: string): Promise<void> {
  emit('closeMobile')

  if (route.path !== target) {
    await router.push(target)
  }
}

/**
 * Finaliza la sesión temporal y regresa al acceso.
 */
async function logout(): Promise<void> {
  eliminarCredenciales()
  emit('closeMobile')
  await router.push('/login')
}
</script>

<template>
  <aside
    class="demo-sidebar"
    :class="{ 'is-visible': mobileVisible }"
  >
    <div class="brand-block">
      <div class="brand-mark" aria-hidden="true">
        <span>DF</span>
      </div>

      <div>
        <p>Diplomatic</p>
        <strong>Fleet</strong>
      </div>
    </div>

    <div class="red-stripe" aria-hidden="true"></div>

    <p class="menu-caption">Control central</p>

    <nav
      class="module-navigation"
      aria-label="Módulos de Diplomatic Fleet"
    >
      <button
        v-for="item in navigationItems"
        :key="item.route"
        class="navigation-item"
        :class="{ active: activeRoute === item.route }"
        type="button"
        @click="navigateTo(item.route)"
      >
        <span class="navigation-number">
          {{ item.number }}
        </span>

        <span class="navigation-content">
          <strong>{{ item.label }}</strong>
          <small>{{ item.description }}</small>
        </span>
      </button>
    </nav>

    <div class="connection-status">
      <span class="connection-light"></span>

      <div>
        <strong>Sistema conectado</strong>
        <small>Vue + Spring Boot + PostgreSQL</small>
      </div>
    </div>

    <button
      class="logout-button"
      type="button"
      @click="logout"
    >
      <span>Cerrar sesión</span>
      <strong aria-hidden="true">↗</strong>
    </button>
  </aside>
</template>

<style scoped>
.demo-sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 40;
  display: flex;
  width: 285px;
  flex-direction: column;
  padding: 25px 20px 20px;
  overflow: hidden auto;
  color: #ffffff;
  background: #111111;
  box-shadow: 13px 0 0 #d71920;
}

.demo-sidebar::before {
  position: absolute;
  top: -85px;
  right: -85px;
  width: 210px;
  height: 215px;
  content: "";
  background: #d71920;
  transform: rotate(28deg);
}

.brand-block {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 13px;
}

.brand-mark {
  display: grid;
  width: 55px;
  height: 55px;
  place-items: center;
  color: #111111;
  background: #ffffff;
  clip-path:
    polygon(
      9% 10%,
      100% 0,
      91% 89%,
      0 100%
    );
  transform: rotate(-4deg);
}

.brand-mark span {
  font-size: 1.35rem;
  font-weight: 1000;
  transform: rotate(4deg);
}

.brand-block p {
  margin: 0;
  color: #c9a34f;
  font-size: 0.68rem;
  font-weight: 950;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.brand-block strong {
  font-size: 1.45rem;
  font-weight: 1000;
  letter-spacing: -0.06em;
  text-transform: uppercase;
}

.red-stripe {
  width: calc(100% + 70px);
  height: 10px;
  margin: 23px 0 20px -35px;
  background: #d71920;
  transform: rotate(-3deg);
}

.menu-caption {
  margin: 0 0 10px 11px;
  color: rgb(255 255 255 / 45%);
  font-size: 0.62rem;
  font-weight: 900;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.module-navigation {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 4px;
}

.navigation-item {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 10px;
  padding: 8px 11px;
  border: 0;
  color: #ffffff;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transform: skew(-3deg);
  transition:
    color 140ms ease,
    background 140ms ease,
    transform 140ms ease;
}

.navigation-item > * {
  transform: skew(3deg);
}

.navigation-item:hover,
.navigation-item:focus-visible {
  color: #111111;
  background: #ffffff;
  outline: none;
  transform: skew(-3deg) translateX(6px);
}

.navigation-item.active {
  color: #ffffff;
  background: #d71920;
  box-shadow: -5px 5px 0 #ffffff;
  transform: skew(-3deg) translateX(4px);
}

.navigation-item.active:hover {
  color: #ffffff;
  background: #d71920;
}

.navigation-number {
  display: grid;
  width: 29px;
  height: 29px;
  flex: 0 0 29px;
  place-items: center;
  border: 2px solid currentColor;
  border-radius: 50%;
  font-size: 0.56rem;
  font-weight: 1000;
}

.navigation-content strong,
.navigation-content small {
  display: block;
}

.navigation-content strong {
  font-size: 0.75rem;
  font-weight: 950;
  text-transform: uppercase;
}

.navigation-content small {
  margin-top: 1px;
  font-size: 0.55rem;
  opacity: 0.64;
}

.connection-status {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 9px;
  margin-top: auto;
  padding: 11px;
  border: 1px solid rgb(255 255 255 / 15%);
  background: rgb(255 255 255 / 5%);
  transform: skew(-2deg);
}

.connection-status > * {
  transform: skew(2deg);
}

.connection-light {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #52db85;
  box-shadow: 0 0 12px #52db85;
}

.connection-status strong,
.connection-status small {
  display: block;
}

.connection-status strong {
  font-size: 0.64rem;
  text-transform: uppercase;
}

.connection-status small {
  margin-top: 2px;
  color: rgb(255 255 255 / 48%);
  font-size: 0.52rem;
}

.logout-button {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 9px;
  padding: 10px 13px;
  border: 0;
  color: #111111;
  background: #ffffff;
  font-size: 0.68rem;
  font-weight: 950;
  text-transform: uppercase;
  cursor: pointer;
  clip-path:
    polygon(
      3% 0,
      100% 8%,
      96% 100%,
      0 90%
    );
}

.logout-button:hover {
  color: #ffffff;
  background: #d71920;
}

@media (max-width: 820px) {
  .demo-sidebar {
    transform: translateX(-112%);
    transition: transform 190ms ease;
  }

  .demo-sidebar.is-visible {
    transform: translateX(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .navigation-item,
  .demo-sidebar {
    transition: none;
  }
}
</style>
