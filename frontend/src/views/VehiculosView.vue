<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  eliminarCredenciales,
  listarVehiculos,
  registrarVehiculo,
  type EstadoVehiculo,
  type TipoCombustible,
  type VehiculoRequest,
  type VehiculoResponse,
} from '@/services/vehiculoService'

/**
 * Vista de gestión de vehículos de Diplomatic Fleet.
 *
 * Permite consultar los vehículos almacenados en PostgreSQL
 * y registrar nuevos vehículos mediante la API REST.
 *
 * La presentación utiliza una identidad visual angular, dinámica
 * y de alto contraste, manteniendo la legibilidad necesaria para
 * formularios y tablas administrativas.
 */
const router = useRouter()

const vehiculos = ref<VehiculoResponse[]>([])
const cargando = ref(false)
const guardando = ref(false)
const mensaje = ref('')
const esError = ref(false)
const busqueda = ref('')
const menuMovilVisible = ref(false)

const formulario = reactive<VehiculoRequest>({
  categoriaId: 2,
  placa: '',
  marca: '',
  modelo: '',
  anio: new Date().getFullYear(),
  color: '',
  combustible: 'GASOLINA',
  capacidadPasajeros: 1,
  capacidadEquipaje: 0,
  activo: true,
})

const categorias = [
  {
    id: 1,
    codigo: 'ECONOMICO',
    nombre: 'Económico',
  },
  {
    id: 2,
    codigo: 'EJECUTIVO',
    nombre: 'Ejecutivo',
  },
  {
    id: 3,
    codigo: 'PREMIUM',
    nombre: 'Premium',
  },
]

const combustibles: TipoCombustible[] = [
  'GASOLINA',
  'DIESEL',
  'HIBRIDO',
  'ELECTRICO',
]

/**
 * Indica si existen vehículos cargados desde la API.
 */
const hayVehiculos = computed(
  (): boolean => vehiculos.value.length > 0,
)

/**
 * Filtra los vehículos según placa, marca, modelo,
 * categoría o estado operativo.
 */
const vehiculosFiltrados = computed(
  (): VehiculoResponse[] => {
    const termino = busqueda.value
      .trim()
      .toLocaleLowerCase('es')

    if (!termino) {
      return vehiculos.value
    }

    return vehiculos.value.filter((vehiculo) => {
      const datosConsultables = [
        vehiculo.placa,
        vehiculo.marca,
        vehiculo.modelo,
        vehiculo.nombreCategoria,
        vehiculo.estado,
        vehiculo.combustible,
      ]
        .join(' ')
        .toLocaleLowerCase('es')

      return datosConsultables.includes(termino)
    })
  },
)

/**
 * Cantidad total de vehículos registrados.
 */
const totalVehiculos = computed(
  (): number => vehiculos.value.length,
)

/**
 * Cantidad de vehículos activos y disponibles.
 */
const vehiculosDisponibles = computed(
  (): number =>
    vehiculos.value.filter(
      (vehiculo) =>
        vehiculo.activo &&
        vehiculo.estado === 'DISPONIBLE',
    ).length,
)

/**
 * Cantidad de vehículos actualmente bloqueados
 * por servicio, reserva o mantenimiento.
 */
const vehiculosNoDisponibles = computed(
  (): number =>
    vehiculos.value.filter(
      (vehiculo) =>
        vehiculo.estado !== 'DISPONIBLE',
    ).length,
)

/**
 * Número de categorías diferentes presentes en la flota.
 */
const categoriasUtilizadas = computed(
  (): number =>
    new Set(
      vehiculos.value.map(
        (vehiculo) => vehiculo.codigoCategoria,
      ),
    ).size,
)

/**
 * Recupera los vehículos registrados desde la API.
 */
async function cargarVehiculos(): Promise<void> {
  cargando.value = true
  mensaje.value = ''
  esError.value = false

  try {
    vehiculos.value = await listarVehiculos()
  } catch (error) {
    esError.value = true
    mensaje.value =
      error instanceof Error
        ? error.message
        : 'No fue posible consultar los vehículos.'
  } finally {
    cargando.value = false
  }
}

/**
 * Registra los datos introducidos y actualiza el listado.
 */
async function guardarVehiculo(): Promise<void> {
  guardando.value = true
  mensaje.value = ''
  esError.value = false

  try {
    const vehiculoCreado = await registrarVehiculo({
      ...formulario,
      placa: formulario.placa.trim().toUpperCase(),
      marca: formulario.marca.trim(),
      modelo: formulario.modelo.trim(),
      color: formulario.color.trim(),
    })

    vehiculos.value.unshift(vehiculoCreado)

    mensaje.value =
      `Vehículo ${vehiculoCreado.placa} registrado correctamente.`

    limpiarFormulario()
  } catch (error) {
    esError.value = true
    mensaje.value =
      error instanceof Error
        ? error.message
        : 'No fue posible registrar el vehículo.'
  } finally {
    guardando.value = false
  }
}

/**
 * Restablece el formulario conservando valores iniciales válidos.
 */
function limpiarFormulario(): void {
  formulario.categoriaId = 2
  formulario.placa = ''
  formulario.marca = ''
  formulario.modelo = ''
  formulario.anio = new Date().getFullYear()
  formulario.color = ''
  formulario.combustible = 'GASOLINA'
  formulario.capacidadPasajeros = 1
  formulario.capacidadEquipaje = 0
  formulario.activo = true
}

/**
 * Traduce el estado operativo a un texto visible.
 *
 * @param estado Estado proporcionado por la API.
 * @returns Texto preparado para la interfaz.
 */
function formatearEstado(
  estado: EstadoVehiculo,
): string {
  return estado.replaceAll('_', ' ')
}

/**
 * Obtiene la clase visual correspondiente al estado.
 *
 * @param estado Estado operativo del vehículo.
 * @returns Clase utilizada por la etiqueta visual.
 */
function obtenerClaseEstado(
  estado: EstadoVehiculo,
): string {
  return `status-${estado.toLowerCase()}`
}

/**
 * Alterna la navegación lateral en pantallas pequeñas.
 */
function alternarMenuMovil(): void {
  menuMovilVisible.value = !menuMovilVisible.value
}

/**
 * Cierra la navegación móvil.
 */
function cerrarMenuMovil(): void {
  menuMovilVisible.value = false
}

/**
 * Finaliza la sesión temporal y regresa al inicio de sesión.
 */
async function cerrarSesion(): Promise<void> {
  eliminarCredenciales()
  cerrarMenuMovil()
  await router.push('/login')
}

onMounted(cargarVehiculos)
</script>

<template>
  <main class="fleet-app">
    <div
      class="background-pattern"
      aria-hidden="true"
    ></div>

    <button
      class="mobile-menu-button"
      type="button"
      aria-label="Abrir navegación"
      :aria-expanded="menuMovilVisible"
      @click="alternarMenuMovil"
    >
      <span></span>
      <span></span>
      <span></span>
    </button>

    <div
      v-if="menuMovilVisible"
      class="mobile-overlay"
      aria-hidden="true"
      @click="cerrarMenuMovil"
    ></div>

    <aside
      class="sidebar"
      :class="{ 'is-visible': menuMovilVisible }"
    >
      <div class="brand-block">
        <div class="brand-mark">
          <span>DF</span>
        </div>

        <div>
          <p class="brand-eyebrow">Diplomatic</p>
          <strong>Fleet</strong>
        </div>
      </div>

      <div class="sidebar-divider"></div>

      <p class="menu-caption">Control central</p>

      <nav
        class="main-navigation"
        aria-label="Navegación principal"
      >
        <a
          class="navigation-item"
          href="#resumen"
          @click="cerrarMenuMovil"
        >
          <span class="navigation-number">01</span>

          <span>
            <strong>Resumen</strong>
            <small>Estado de la flota</small>
          </span>
        </a>

        <a
          class="navigation-item is-active"
          href="#vehiculos"
          @click="cerrarMenuMovil"
        >
          <span class="navigation-number">02</span>

          <span>
            <strong>Vehículos</strong>
            <small>Registro y consulta</small>
          </span>
        </a>

        <button
          class="navigation-item is-disabled"
          type="button"
          disabled
        >
          <span class="navigation-number">03</span>

          <span>
            <strong>Documentos</strong>
            <small>Próxima integración</small>
          </span>
        </button>

        <button
          class="navigation-item is-disabled"
          type="button"
          disabled
        >
          <span class="navigation-number">04</span>

          <span>
            <strong>Mantenimiento</strong>
            <small>Próxima integración</small>
          </span>
        </button>
      </nav>

      <div class="sidebar-status">
        <span class="connection-light"></span>

        <div>
          <strong>Sistema conectado</strong>
          <small>Spring Boot + PostgreSQL</small>
        </div>
      </div>

      <button
        class="logout-button"
        type="button"
        @click="cerrarSesion"
      >
        <span>Cerrar sesión</span>
        <strong>↗</strong>
      </button>
    </aside>

    <section class="workspace">
      <header class="hero-header">
        <div class="hero-black-shape"></div>
        <div class="hero-red-shape"></div>

        <div class="hero-content">
          <p class="hero-kicker">
            Operaciones / Módulo de flota
          </p>

          <h1>
            Gestión de
            <span>vehículos</span>
          </h1>

          <p class="hero-description">
            Control operativo de la flota institucional
            de Diplomatic Fleet.
          </p>
        </div>

        <div class="hero-counter">
          <span>Total</span>
          <strong>
            {{ String(totalVehiculos).padStart(2, '0') }}
          </strong>
          <small>unidades registradas</small>
        </div>
      </header>

      <section
        id="resumen"
        class="summary-section"
        aria-labelledby="summary-title"
      >
        <div class="section-heading">
          <span class="section-number">01</span>

          <div>
            <p>Resumen operativo</p>
            <h2 id="summary-title">
              Estado actual
            </h2>
          </div>
        </div>

        <div class="summary-grid">
          <article class="summary-card card-total">
            <span class="summary-symbol">◆</span>

            <div>
              <small>Flota total</small>
              <strong>{{ totalVehiculos }}</strong>
              <p>Vehículos registrados</p>
            </div>
          </article>

          <article class="summary-card card-available">
            <span class="summary-symbol">✓</span>

            <div>
              <small>Disponibles</small>
              <strong>{{ vehiculosDisponibles }}</strong>
              <p>Preparados para servicio</p>
            </div>
          </article>

          <article class="summary-card card-blocked">
            <span class="summary-symbol">!</span>

            <div>
              <small>No disponibles</small>
              <strong>{{ vehiculosNoDisponibles }}</strong>
              <p>Reservados o bloqueados</p>
            </div>
          </article>

          <article class="summary-card card-category">
            <span class="summary-symbol">#</span>

            <div>
              <small>Categorías</small>
              <strong>{{ categoriasUtilizadas }}</strong>
              <p>Clasificaciones en uso</p>
            </div>
          </article>
        </div>
      </section>

      <p
        v-if="mensaje"
        class="system-message"
        :class="{
          'is-error': esError,
          'is-success': !esError,
        }"
        role="status"
        aria-live="polite"
      >
        <span class="message-symbol">
          {{ esError ? '!' : '✓' }}
        </span>

        {{ mensaje }}

        <button
          type="button"
          aria-label="Cerrar mensaje"
          @click="mensaje = ''"
        >
          ×
        </button>
      </p>

      <section
        id="vehiculos"
        class="management-grid"
      >
        <article class="registration-panel">
          <div class="registration-shadow"></div>

          <header class="panel-title">
            <span class="panel-index">NEW</span>

            <div>
              <p>Alta de unidad</p>
              <h2>Registrar vehículo</h2>
            </div>
          </header>

          <form
            class="vehicle-form"
            @submit.prevent="guardarVehiculo"
          >
            <fieldset>
              <legend>
                <span>01</span>
                Identificación
              </legend>

              <div class="fields-grid">
                <label>
                  <span>Categoría</span>

                  <select
                    v-model.number="formulario.categoriaId"
                    required
                  >
                    <option
                      v-for="categoria in categorias"
                      :key="categoria.id"
                      :value="categoria.id"
                    >
                      {{ categoria.nombre }}
                    </option>
                  </select>
                </label>

                <label>
                  <span>Placa</span>

                  <input
                    v-model="formulario.placa"
                    type="text"
                    maxlength="12"
                    placeholder="DFT123"
                    autocomplete="off"
                    required
                  />
                </label>
              </div>
            </fieldset>

            <fieldset>
              <legend>
                <span>02</span>
                Datos generales
              </legend>

              <div class="fields-grid">
                <label>
                  <span>Marca</span>

                  <input
                    v-model="formulario.marca"
                    type="text"
                    maxlength="60"
                    placeholder="Toyota"
                    autocomplete="off"
                    required
                  />
                </label>

                <label>
                  <span>Modelo</span>

                  <input
                    v-model="formulario.modelo"
                    type="text"
                    maxlength="60"
                    placeholder="Corolla"
                    autocomplete="off"
                    required
                  />
                </label>

                <label>
                  <span>Año</span>

                  <input
                    v-model.number="formulario.anio"
                    type="number"
                    min="1950"
                    max="2100"
                    required
                  />
                </label>

                <label>
                  <span>Color</span>

                  <input
                    v-model="formulario.color"
                    type="text"
                    maxlength="40"
                    placeholder="Negro"
                    autocomplete="off"
                    required
                  />
                </label>
              </div>
            </fieldset>

            <fieldset>
              <legend>
                <span>03</span>
                Capacidad
              </legend>

              <div class="fields-grid">
                <label>
                  <span>Combustible</span>

                  <select
                    v-model="formulario.combustible"
                    required
                  >
                    <option
                      v-for="combustible in combustibles"
                      :key="combustible"
                      :value="combustible"
                    >
                      {{ combustible.replace('_', ' ') }}
                    </option>
                  </select>
                </label>

                <label>
                  <span>Pasajeros</span>

                  <input
                    v-model.number="
                      formulario.capacidadPasajeros
                    "
                    type="number"
                    min="1"
                    max="100"
                    required
                  />
                </label>

                <label>
                  <span>Equipaje</span>

                  <input
                    v-model.number="
                      formulario.capacidadEquipaje
                    "
                    type="number"
                    min="0"
                    required
                  />
                </label>

                <label class="active-control">
                  <input
                    v-model="formulario.activo"
                    type="checkbox"
                  />

                  <span class="custom-checkbox"></span>

                  <span>
                    Unidad activa
                    <small>
                      Disponible para operaciones
                    </small>
                  </span>
                </label>
              </div>
            </fieldset>

            <button
              class="register-button"
              type="submit"
              :disabled="guardando"
            >
              <span>
                {{
                  guardando
                    ? 'Registrando unidad...'
                    : 'Confirmar registro'
                }}
              </span>

              <strong>➜</strong>
            </button>
          </form>
        </article>

        <article class="fleet-panel">
          <div class="fleet-panel-accent"></div>

          <header class="fleet-header">
            <div class="panel-title">
              <span class="panel-index">LIST</span>

              <div>
                <p>Flota institucional</p>
                <h2>Vehículos registrados</h2>
              </div>
            </div>

            <button
              class="refresh-button"
              type="button"
              :disabled="cargando"
              @click="cargarVehiculos"
            >
              <span :class="{ rotating: cargando }">
                ↻
              </span>

              {{
                cargando
                  ? 'Consultando'
                  : 'Actualizar'
              }}
            </button>
          </header>

          <div class="fleet-tools">
            <label class="search-control">
              <span aria-hidden="true">⌕</span>

              <input
                v-model="busqueda"
                type="search"
                placeholder="Buscar por placa, marca, categoría..."
                aria-label="Buscar vehículos"
              />
            </label>

            <span class="result-counter">
              {{ vehiculosFiltrados.length }}
              resultado{{
                vehiculosFiltrados.length === 1 ? '' : 's'
              }}
            </span>
          </div>

          <div
            v-if="cargando"
            class="loading-state"
          >
            <span class="loading-shape"></span>
            <strong>Consultando flota</strong>
            <small>
              Espera mientras recuperamos los datos.
            </small>
          </div>

          <div
            v-else-if="!hayVehiculos"
            class="empty-state"
          >
            <span>0</span>
            <strong>No hay vehículos</strong>
            <p>
              Utiliza el formulario para registrar
              la primera unidad.
            </p>
          </div>

          <div
            v-else-if="vehiculosFiltrados.length === 0"
            class="empty-state"
          >
            <span>?</span>
            <strong>Sin coincidencias</strong>
            <p>
              Prueba con otra placa, marca,
              categoría o estado.
            </p>
          </div>

          <div
            v-else
            class="vehicles-list"
          >
            <article
              v-for="(vehiculo, index) in vehiculosFiltrados"
              :key="vehiculo.id"
              class="vehicle-card"
            >
              <div class="vehicle-order">
                {{ String(index + 1).padStart(2, '0') }}
              </div>

              <div class="vehicle-identity">
                <span class="vehicle-category">
                  {{ vehiculo.codigoCategoria }}
                </span>

                <strong>{{ vehiculo.placa }}</strong>

                <p>
                  {{ vehiculo.marca }}
                  {{ vehiculo.modelo }}
                </p>
              </div>

              <dl class="vehicle-details">
                <div>
                  <dt>Año</dt>
                  <dd>{{ vehiculo.anio }}</dd>
                </div>

                <div>
                  <dt>Color</dt>
                  <dd>{{ vehiculo.color }}</dd>
                </div>

                <div>
                  <dt>Motor</dt>
                  <dd>
                    {{
                      vehiculo.combustible.replace(
                        '_',
                        ' ',
                      )
                    }}
                  </dd>
                </div>

                <div>
                  <dt>Capacidad</dt>
                  <dd>
                    {{
                      vehiculo.capacidadPasajeros
                    }}
                    pasajeros
                  </dd>
                </div>
              </dl>

              <div class="vehicle-status">
                <span
                  class="status-badge"
                  :class="
                    obtenerClaseEstado(
                      vehiculo.estado,
                    )
                  "
                >
                  <i></i>

                  {{
                    formatearEstado(
                      vehiculo.estado,
                    )
                  }}
                </span>

                <small>
                  {{ vehiculo.nombreCategoria }}
                </small>
              </div>
            </article>
          </div>
        </article>
      </section>

      <footer class="app-footer">
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
:global(select) {
  font: inherit;
}

:global(button) {
  -webkit-tap-highlight-color: transparent;
}

.fleet-app {
  --red: #d71920;
  --red-dark: #a4090f;
  --black: #111111;
  --white: #ffffff;
  --cream: #f6f2e9;
  --gold: #c9a34f;
  --grey: #d6d2c9;

  position: relative;
  min-height: 100vh;
  background:
    linear-gradient(
      115deg,
      rgb(255 255 255 / 88%),
      rgb(239 236 229 / 92%)
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
      circle at 15% 15%,
      rgb(215 25 32 / 14%) 0 2px,
      transparent 3px
    ),
    radial-gradient(
      circle at 85% 70%,
      rgb(17 17 17 / 10%) 0 2px,
      transparent 3px
    );
  background-size: 28px 28px, 34px 34px;
  mask-image:
    linear-gradient(
      to bottom,
      black,
      transparent 85%
    );
}

.sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: 20;
  display: flex;
  width: 270px;
  flex-direction: column;
  padding: 28px 22px;
  overflow: hidden;
  color: var(--white);
  background: var(--black);
  box-shadow: 14px 0 0 var(--red);
}

.sidebar::before {
  position: absolute;
  top: -70px;
  right: -75px;
  width: 190px;
  height: 210px;
  content: "";
  background: var(--red);
  transform: rotate(25deg);
}

.sidebar::after {
  position: absolute;
  right: -55px;
  bottom: -65px;
  width: 170px;
  height: 190px;
  border: 22px solid rgb(255 255 255 / 5%);
  content: "";
  transform: rotate(-25deg);
}

.brand-block {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  display: grid;
  width: 58px;
  height: 58px;
  place-items: center;
  color: var(--black);
  background: var(--white);
  clip-path:
    polygon(
      8% 11%,
      100% 0,
      90% 88%,
      0 100%
    );
  transform: rotate(-4deg);
}

.brand-mark span {
  font-size: 1.45rem;
  font-weight: 1000;
  transform: rotate(4deg);
}

.brand-eyebrow {
  margin: 0;
  color: var(--gold);
  font-size: 0.7rem;
  font-weight: 900;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.brand-block strong {
  font-size: 1.55rem;
  font-weight: 1000;
  letter-spacing: -0.06em;
  text-transform: uppercase;
}

.sidebar-divider {
  width: calc(100% + 70px);
  height: 11px;
  margin: 28px 0 24px -35px;
  background: var(--red);
  transform: rotate(-3deg);
}

.menu-caption {
  margin: 0 0 12px 12px;
  color: rgb(255 255 255 / 45%);
  font-size: 0.66rem;
  font-weight: 900;
  letter-spacing: 0.17em;
  text-transform: uppercase;
}

.main-navigation {
  position: relative;
  z-index: 1;
  display: grid;
  gap: 7px;
}

.navigation-item {
  position: relative;
  display: flex;
  width: 100%;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 0;
  color: var(--white);
  background: transparent;
  text-align: left;
  text-decoration: none;
  cursor: pointer;
  transform: skew(-4deg);
  transition:
    color 150ms ease,
    background 150ms ease,
    transform 150ms ease;
}

.navigation-item > * {
  transform: skew(4deg);
}

.navigation-item:hover:not(.is-disabled),
.navigation-item:focus-visible:not(.is-disabled) {
  color: var(--black);
  background: var(--white);
  outline: none;
  transform: skew(-4deg) translateX(7px);
}

.navigation-item.is-active {
  color: var(--white);
  background: var(--red);
  box-shadow: -7px 7px 0 var(--white);
  transform: skew(-4deg) translateX(5px);
}

.navigation-item.is-active:hover {
  color: var(--white);
  background: var(--red);
}

.navigation-item.is-disabled {
  cursor: not-allowed;
  opacity: 0.32;
}

.navigation-number {
  display: grid;
  width: 33px;
  height: 33px;
  flex: 0 0 33px;
  place-items: center;
  border: 2px solid currentColor;
  border-radius: 50%;
  font-size: 0.66rem;
  font-weight: 1000;
}

.navigation-item strong,
.navigation-item small {
  display: block;
}

.navigation-item strong {
  font-size: 0.9rem;
  font-weight: 950;
  text-transform: uppercase;
}

.navigation-item small {
  margin-top: 2px;
  font-size: 0.66rem;
  opacity: 0.7;
}

.sidebar-status {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 11px;
  margin-top: auto;
  padding: 14px 13px;
  border: 1px solid rgb(255 255 255 / 14%);
  background: rgb(255 255 255 / 5%);
  transform: skew(-3deg);
}

.sidebar-status > * {
  transform: skew(3deg);
}

.connection-light {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #52db85;
  box-shadow: 0 0 13px #52db85;
}

.sidebar-status strong,
.sidebar-status small {
  display: block;
}

.sidebar-status strong {
  font-size: 0.72rem;
  text-transform: uppercase;
}

.sidebar-status small {
  margin-top: 3px;
  color: rgb(255 255 255 / 55%);
  font-size: 0.62rem;
}

.logout-button {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding: 12px 15px;
  border: 0;
  color: var(--black);
  background: var(--white);
  font-size: 0.75rem;
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
  transition:
    color 150ms ease,
    background 150ms ease,
    transform 150ms ease;
}

.logout-button:hover {
  color: var(--white);
  background: var(--red);
  transform: translateX(5px);
}

.workspace {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  margin-left: 285px;
  padding: 30px 34px 22px;
}

.hero-header {
  position: relative;
  min-height: 205px;
  padding: 38px 42px;
  overflow: hidden;
  color: var(--white);
  background: var(--red);
  clip-path:
    polygon(
      0 5%,
      96% 0,
      100% 80%,
      93% 100%,
      3% 93%
    );
}

.hero-black-shape {
  position: absolute;
  inset: -80px auto -90px -60px;
  width: 69%;
  background: var(--black);
  transform: rotate(-4deg);
}

.hero-black-shape::after {
  position: absolute;
  top: 35px;
  right: -42px;
  width: 80px;
  height: 100%;
  content: "";
  background: var(--white);
  transform: skew(-9deg);
}

.hero-red-shape {
  position: absolute;
  top: -60px;
  right: 12%;
  width: 210px;
  height: 370px;
  border: 22px solid rgb(17 17 17 / 72%);
  content: "";
  transform: rotate(31deg);
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 700px;
}

.hero-kicker {
  display: inline-block;
  margin: 0 0 7px;
  padding: 5px 10px;
  color: var(--black);
  background: var(--gold);
  font-size: 0.68rem;
  font-weight: 950;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  transform: rotate(-1deg);
}

.hero-content h1 {
  margin: 0;
  font-size: clamp(2.8rem, 5.2vw, 5.6rem);
  font-weight: 1000;
  letter-spacing: -0.09em;
  line-height: 0.83;
  text-transform: uppercase;
  transform: rotate(-1.4deg);
}

.hero-content h1 span {
  display: block;
  width: fit-content;
  margin-top: 4px;
  padding: 1px 13px 7px;
  color: var(--black);
  background: var(--white);
  transform: rotate(1.8deg);
}

.hero-description {
  max-width: 500px;
  margin: 16px 0 0;
  color: rgb(255 255 255 / 72%);
  font-size: 0.93rem;
}

.hero-counter {
  position: absolute;
  right: 5%;
  bottom: 25px;
  z-index: 3;
  display: grid;
  min-width: 150px;
  justify-items: center;
  padding: 15px 22px;
  color: var(--black);
  background: var(--white);
  clip-path:
    polygon(
      8% 0,
      100% 10%,
      92% 100%,
      0 89%
    );
  transform: rotate(2deg);
}

.hero-counter span,
.hero-counter small {
  font-size: 0.62rem;
  font-weight: 900;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.hero-counter strong {
  margin: -4px 0;
  color: var(--red);
  font-size: 3.7rem;
  font-weight: 1000;
  letter-spacing: -0.12em;
}

.summary-section {
  margin-top: 30px;
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-bottom: 14px;
}

.section-number {
  display: grid;
  width: 45px;
  height: 45px;
  place-items: center;
  color: var(--white);
  background: var(--black);
  font-size: 0.8rem;
  font-weight: 1000;
  clip-path:
    polygon(
      13% 0,
      100% 9%,
      88% 100%,
      0 85%
    );
}

.section-heading p {
  margin: 0;
  color: var(--red);
  font-size: 0.64rem;
  font-weight: 950;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.section-heading h2 {
  margin: 2px 0 0;
  font-size: 1.55rem;
  font-weight: 1000;
  letter-spacing: -0.05em;
  text-transform: uppercase;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  position: relative;
  min-height: 132px;
  padding: 22px 20px 18px 72px;
  overflow: hidden;
  color: var(--white);
  background: var(--black);
  clip-path:
    polygon(
      4% 0,
      100% 6%,
      96% 100%,
      0 91%
    );
  transition:
    transform 160ms ease,
    filter 160ms ease;
}

.summary-card:nth-child(even) {
  transform: rotate(0.7deg);
}

.summary-card:hover {
  z-index: 2;
  filter: drop-shadow(8px 10px 0 rgb(215 25 32 / 30%));
  transform: translateY(-5px) rotate(-1deg);
}

.summary-card::after {
  position: absolute;
  right: -36px;
  bottom: -70px;
  width: 145px;
  height: 145px;
  border: 18px solid rgb(255 255 255 / 8%);
  border-radius: 50%;
  content: "";
}

.summary-symbol {
  position: absolute;
  top: 27px;
  left: 20px;
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  color: var(--white);
  background: var(--red);
  font-size: 1.15rem;
  font-weight: 1000;
  transform: rotate(-7deg);
}

.summary-card small {
  color: rgb(255 255 255 / 65%);
  font-size: 0.65rem;
  font-weight: 900;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.summary-card strong {
  display: block;
  margin-top: -2px;
  font-size: 2.7rem;
  font-weight: 1000;
  letter-spacing: -0.1em;
  line-height: 1;
}

.summary-card p {
  margin: 6px 0 0;
  color: rgb(255 255 255 / 60%);
  font-size: 0.72rem;
}

.card-available {
  color: var(--black);
  background: var(--white);
}

.card-available small,
.card-available p {
  color: rgb(17 17 17 / 58%);
}

.card-blocked {
  background: var(--red);
}

.card-blocked .summary-symbol {
  color: var(--red);
  background: var(--white);
}

.card-category {
  color: var(--black);
  background: var(--gold);
}

.card-category small,
.card-category p {
  color: rgb(17 17 17 / 62%);
}

.card-category .summary-symbol {
  color: var(--white);
  background: var(--black);
}

.system-message {
  position: relative;
  display: flex;
  align-items: center;
  gap: 11px;
  margin: 24px 0 0;
  padding: 13px 45px 13px 15px;
  border: 3px solid var(--black);
  color: var(--black);
  background: var(--white);
  font-size: 0.82rem;
  font-weight: 800;
  box-shadow: 6px 6px 0 var(--black);
  transform: rotate(-0.25deg);
}

.system-message.is-error {
  color: var(--white);
  background: var(--red);
}

.message-symbol {
  display: grid;
  width: 27px;
  height: 27px;
  place-items: center;
  color: var(--white);
  background: var(--black);
  font-weight: 1000;
  transform: rotate(-6deg);
}

.system-message button {
  position: absolute;
  top: 4px;
  right: 11px;
  border: 0;
  color: inherit;
  background: transparent;
  font-size: 1.6rem;
  font-weight: 1000;
  cursor: pointer;
}

.management-grid {
  display: grid;
  grid-template-columns: minmax(330px, 410px) minmax(0, 1fr);
  gap: 24px;
  margin-top: 29px;
}

.registration-panel,
.fleet-panel {
  position: relative;
}

.registration-panel {
  z-index: 2;
  align-self: start;
  padding: 25px;
  color: var(--white);
  background: var(--black);
  clip-path:
    polygon(
      3% 0,
      100% 1.5%,
      97% 100%,
      0 97%
    );
}

.registration-shadow {
  position: absolute;
  inset: 12px -11px -11px 12px;
  z-index: -1;
  background: var(--red);
  clip-path:
    polygon(
      3% 0,
      100% 1.5%,
      97% 100%,
      0 97%
    );
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-index {
  display: grid;
  min-width: 53px;
  height: 40px;
  place-items: center;
  color: var(--white);
  background: var(--red);
  font-size: 0.68rem;
  font-weight: 1000;
  letter-spacing: 0.08em;
  transform: skew(-8deg);
}

.panel-title p {
  margin: 0;
  color: var(--gold);
  font-size: 0.63rem;
  font-weight: 950;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}

.panel-title h2 {
  margin: 2px 0 0;
  font-size: 1.45rem;
  font-weight: 1000;
  letter-spacing: -0.055em;
  text-transform: uppercase;
}

.vehicle-form {
  display: grid;
  gap: 18px;
  margin-top: 23px;
}

.vehicle-form fieldset {
  min-width: 0;
  margin: 0;
  padding: 14px 0 0;
  border: 0;
  border-top: 1px solid rgb(255 255 255 / 16%);
}

.vehicle-form legend {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 9px 0 0;
  color: rgb(255 255 255 / 65%);
  background: var(--black);
  font-size: 0.64rem;
  font-weight: 900;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

.vehicle-form legend span {
  color: var(--red);
  font-size: 0.75rem;
}

.fields-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 13px;
  margin-top: 13px;
}

.vehicle-form label {
  display: grid;
  gap: 6px;
}

.vehicle-form label > span:first-child {
  color: rgb(255 255 255 / 72%);
  font-size: 0.67rem;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.vehicle-form input,
.vehicle-form select {
  width: 100%;
  min-width: 0;
  padding: 11px 12px;
  border: 2px solid transparent;
  border-radius: 0;
  color: var(--black);
  background: var(--white);
  font-size: 0.83rem;
  font-weight: 750;
  outline: none;
  clip-path:
    polygon(
      2% 0,
      100% 3%,
      97% 100%,
      0 95%
    );
  transition:
    border 140ms ease,
    box-shadow 140ms ease,
    transform 140ms ease;
}

.vehicle-form input:focus,
.vehicle-form select:focus {
  border-color: var(--red);
  box-shadow: 5px 5px 0 var(--red);
  transform: translate(-2px, -2px);
}

.active-control {
  display: flex !important;
  align-items: center;
  gap: 10px !important;
  padding: 9px 10px;
  border: 1px solid rgb(255 255 255 / 16%);
  cursor: pointer;
}

.active-control input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.custom-checkbox {
  position: relative;
  display: grid;
  width: 25px;
  height: 25px;
  flex: 0 0 25px;
  place-items: center;
  border: 2px solid var(--white);
  background: transparent;
  transform: rotate(-4deg);
}

.active-control input:checked + .custom-checkbox {
  border-color: var(--red);
  background: var(--red);
}

.active-control input:checked + .custom-checkbox::after {
  color: var(--white);
  content: "✓";
  font-weight: 1000;
}

.active-control > span:last-child {
  color: var(--white) !important;
  font-size: 0.72rem !important;
}

.active-control small {
  display: block;
  margin-top: 2px;
  color: rgb(255 255 255 / 48%);
  font-size: 0.57rem;
}

.register-button {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  padding: 14px 17px;
  border: 0;
  color: var(--white);
  background: var(--red);
  font-size: 0.78rem;
  font-weight: 1000;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  cursor: pointer;
  clip-path:
    polygon(
      2% 0,
      100% 8%,
      97% 100%,
      0 90%
    );
  transition:
    color 150ms ease,
    background 150ms ease,
    transform 150ms ease;
}

.register-button:hover:not(:disabled) {
  color: var(--black);
  background: var(--white);
  transform: translateX(5px) skew(-2deg);
}

.register-button strong {
  font-size: 1.2rem;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.57;
}

.fleet-panel {
  min-width: 0;
  padding: 26px;
  overflow: hidden;
  color: var(--black);
  background: var(--white);
  box-shadow: 12px 12px 0 rgb(17 17 17 / 13%);
  clip-path:
    polygon(
      1% 0,
      99% 1%,
      100% 97%,
      2% 100%,
      0 4%
    );
}

.fleet-panel-accent {
  position: absolute;
  top: 0;
  right: -65px;
  width: 260px;
  height: 15px;
  background: var(--red);
  transform: rotate(2deg);
}

.fleet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.refresh-button {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 13px;
  border: 2px solid var(--black);
  color: var(--black);
  background: var(--white);
  font-size: 0.7rem;
  font-weight: 950;
  text-transform: uppercase;
  cursor: pointer;
  transform: skew(-4deg);
}

.refresh-button:hover:not(:disabled) {
  color: var(--white);
  background: var(--black);
}

.refresh-button span {
  display: inline-block;
  font-size: 1rem;
}

.rotating {
  animation: rotate-refresh 850ms linear infinite;
}

.fleet-tools {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 23px;
  padding: 12px;
  background: var(--black);
  transform: skew(-1deg);
}

.search-control {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 8px;
  padding: 7px 11px;
  background: var(--white);
}

.search-control span {
  color: var(--red);
  font-size: 1.4rem;
  font-weight: 1000;
}

.search-control input {
  width: 100%;
  border: 0;
  color: var(--black);
  background: transparent;
  font-size: 0.78rem;
  outline: none;
}

.result-counter {
  color: var(--white);
  font-size: 0.67rem;
  font-weight: 900;
  white-space: nowrap;
  text-transform: uppercase;
}

.vehicles-list {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}

.vehicle-card {
  position: relative;
  display: grid;
  grid-template-columns:
    52px minmax(150px, 0.8fr)
    minmax(300px, 1.45fr) minmax(135px, 0.55fr);
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 13px 15px 13px 7px;
  border-bottom: 2px solid var(--black);
  overflow: hidden;
  background: #f1eee7;
  transition:
    color 150ms ease,
    background 150ms ease,
    transform 150ms ease;
}

.vehicle-card::before {
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
  content: "";
  background: var(--red);
}

.vehicle-card:hover {
  z-index: 2;
  color: var(--white);
  background: var(--black);
  transform: translateX(7px) skew(-0.5deg);
}

.vehicle-order {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: var(--white);
  background: var(--red);
  font-size: 0.74rem;
  font-weight: 1000;
  clip-path:
    polygon(
      9% 0,
      100% 9%,
      90% 100%,
      0 88%
    );
}

.vehicle-category {
  display: inline-block;
  padding: 2px 6px;
  color: var(--white);
  background: var(--black);
  font-size: 0.52rem;
  font-weight: 900;
  letter-spacing: 0.08em;
}

.vehicle-card:hover .vehicle-category {
  color: var(--black);
  background: var(--gold);
}

.vehicle-identity strong {
  display: block;
  margin-top: 4px;
  font-size: 1.25rem;
  font-weight: 1000;
  letter-spacing: -0.05em;
}

.vehicle-identity p {
  margin: 2px 0 0;
  color: #53606d;
  font-size: 0.72rem;
}

.vehicle-card:hover .vehicle-identity p {
  color: rgb(255 255 255 / 62%);
}

.vehicle-details {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 9px;
  margin: 0;
}

.vehicle-details div {
  min-width: 0;
}

.vehicle-details dt {
  color: #7c848d;
  font-size: 0.52rem;
  font-weight: 900;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.vehicle-details dd {
  margin: 3px 0 0;
  overflow: hidden;
  font-size: 0.7rem;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.vehicle-card:hover .vehicle-details dt {
  color: rgb(255 255 255 / 47%);
}

.vehicle-status {
  display: grid;
  justify-items: end;
  gap: 5px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  color: #116335;
  background: #dff4e7;
  font-size: 0.56rem;
  font-weight: 1000;
  text-transform: uppercase;
  clip-path:
    polygon(
      4% 0,
      100% 7%,
      95% 100%,
      0 90%
    );
}

.status-badge i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
}

.status-en_mantenimiento,
.status-inactivo {
  color: #9a080d;
  background: #ffdadd;
}

.status-reservado,
.status-en_servicio {
  color: #7c5100;
  background: #fff0bd;
}

.vehicle-card:hover .status-badge {
  color: var(--black);
  background: var(--white);
}

.vehicle-status small {
  color: #66717d;
  font-size: 0.58rem;
  font-weight: 800;
}

.vehicle-card:hover .vehicle-status small {
  color: rgb(255 255 255 / 58%);
}

.loading-state,
.empty-state {
  display: grid;
  min-height: 290px;
  place-content: center;
  justify-items: center;
  margin-top: 17px;
  padding: 35px;
  text-align: center;
}

.loading-shape {
  width: 64px;
  height: 64px;
  margin-bottom: 17px;
  border: 8px solid var(--grey);
  border-top-color: var(--red);
  border-radius: 50%;
  animation: rotate-refresh 850ms linear infinite;
}

.loading-state strong,
.empty-state strong {
  font-size: 1.2rem;
  font-weight: 1000;
  text-transform: uppercase;
}

.loading-state small,
.empty-state p {
  margin: 6px 0 0;
  color: #6c7782;
  font-size: 0.75rem;
}

.empty-state > span {
  display: grid;
  width: 75px;
  height: 75px;
  margin-bottom: 14px;
  place-items: center;
  color: var(--white);
  background: var(--red);
  font-size: 2.5rem;
  font-weight: 1000;
  clip-path:
    polygon(
      8% 0,
      100% 11%,
      88% 100%,
      0 87%
    );
  transform: rotate(-4deg);
}

.app-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 39px;
  padding: 14px 19px;
  color: var(--white);
  background: var(--black);
  font-size: 0.66rem;
  text-transform: uppercase;
  transform: skew(-1deg);
}

.app-footer div {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-footer strong {
  color: var(--gold);
}

.app-footer p {
  margin: 0;
  color: rgb(255 255 255 / 55%);
}

.mobile-menu-button {
  display: none;
}

.mobile-overlay {
  display: none;
}

@keyframes rotate-refresh {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1250px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .management-grid {
    grid-template-columns: 350px minmax(0, 1fr);
  }

  .vehicle-card {
    grid-template-columns: 46px minmax(140px, 1fr) 1fr;
  }

  .vehicle-details {
    grid-template-columns: repeat(2, 1fr);
  }

  .vehicle-status {
    grid-column: 2 / -1;
    grid-row: 2;
    justify-items: start;
  }
}

@media (max-width: 1050px) {
  .sidebar {
    width: 235px;
  }

  .workspace {
    margin-left: 249px;
    padding-right: 22px;
    padding-left: 24px;
  }

  .management-grid {
    grid-template-columns: 1fr;
  }

  .registration-panel {
    max-width: none;
  }

  .fields-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .active-control {
    grid-column: span 1;
  }
}

@media (max-width: 820px) {
  .mobile-menu-button {
    position: fixed;
    top: 17px;
    left: 16px;
    z-index: 50;
    display: grid;
    width: 48px;
    height: 43px;
    align-content: center;
    gap: 5px;
    padding: 8px;
    border: 3px solid var(--white);
    background: var(--red);
    box-shadow: 4px 4px 0 var(--black);
    cursor: pointer;
    transform: rotate(-2deg);
  }

  .mobile-menu-button span {
    display: block;
    width: 100%;
    height: 3px;
    background: var(--white);
  }

  .mobile-overlay {
    position: fixed;
    inset: 0;
    z-index: 35;
    display: block;
    background: rgb(0 0 0 / 62%);
  }

  .sidebar {
    z-index: 40;
    transform: translateX(-112%);
    transition: transform 190ms ease;
  }

  .sidebar.is-visible {
    transform: translateX(0);
  }

  .workspace {
    margin-left: 0;
    padding: 76px 18px 20px;
  }

  .hero-header {
    min-height: 260px;
    padding: 36px 25px;
  }

  .hero-black-shape {
    width: 86%;
  }

  .hero-counter {
    right: 8%;
    bottom: 20px;
  }

  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .fields-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 570px) {
  .workspace {
    padding-right: 12px;
    padding-left: 12px;
  }

  .hero-header {
    min-height: 295px;
  }

  .hero-content h1 {
    font-size: 2.8rem;
  }

  .hero-counter {
    right: 8%;
    bottom: 14px;
    transform: scale(0.85) rotate(2deg);
    transform-origin: right bottom;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .summary-card:nth-child(even) {
    transform: none;
  }

  .fields-grid {
    grid-template-columns: 1fr;
  }

  .fleet-panel,
  .registration-panel {
    padding: 21px 17px;
  }

  .fleet-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .fleet-tools {
    align-items: stretch;
    flex-direction: column;
  }

  .vehicle-card {
    grid-template-columns: 42px 1fr;
    align-items: start;
    padding: 15px 12px 15px 6px;
  }

  .vehicle-details {
    grid-column: 2;
    grid-template-columns: repeat(2, 1fr);
  }

  .vehicle-status {
    grid-column: 2;
    grid-row: auto;
  }

  .app-footer {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .app-footer div {
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
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
