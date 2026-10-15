<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import DemoLayout from '@/components/layout/DemoLayout.vue'
import {
  actualizarCliente,
  listarClientes,
  registrarCliente,
  type ClienteInstitucionalRequest,
  type ClienteInstitucionalResponse,
  type TipoEntidad,
} from '@/services/clienteService'

/**
 * Vista funcional de clientes institucionales.
 *
 * Permite registrar, consultar, buscar y cambiar el estado
 * de las instituciones almacenadas en PostgreSQL.
 */
const clientes = ref<ClienteInstitucionalResponse[]>([])
const cargando = ref(false)
const guardando = ref(false)
const mensaje = ref('')
const esError = ref(false)
const busqueda = ref('')

const formulario = reactive<ClienteInstitucionalRequest>({
  nombreEntidad: '',
  tipoEntidad: 'EMBAJADA',
  numeroIdentificacionFiscal: null,
  nombreContacto: '',
  correoContacto: '',
  telefonoContacto: null,
  direccion: null,
  ciudad: null,
  activo: true,
})

const tiposEntidad: Array<{
  value: TipoEntidad
  label: string
}> = [
  {
    value: 'EMBAJADA',
    label: 'Embajada',
  },
  {
    value: 'CONSULADO',
    label: 'Consulado',
  },
  {
    value: 'ORGANISMO_INTERNACIONAL',
    label: 'Organismo internacional',
  },
  {
    value: 'OTRO',
    label: 'Otro',
  },
]

/**
 * Indica si existen clientes en el listado.
 */
const hayClientes = computed(
  (): boolean => clientes.value.length > 0,
)

/**
 * Filtra clientes por institución, contacto, correo,
 * ciudad, identificación o tipo de entidad.
 */
const clientesFiltrados = computed(
  (): ClienteInstitucionalResponse[] => {
    const termino = busqueda.value
      .trim()
      .toLocaleLowerCase('es')

    if (!termino) {
      return clientes.value
    }

    return clientes.value.filter((cliente) => {
      const datos = [
        cliente.nombreEntidad,
        cliente.tipoEntidad,
        cliente.numeroIdentificacionFiscal ?? '',
        cliente.nombreContacto,
        cliente.correoContacto,
        cliente.ciudad ?? '',
        cliente.activo ? 'activo' : 'inactivo',
      ]
        .join(' ')
        .toLocaleLowerCase('es')

      return datos.includes(termino)
    })
  },
)

/**
 * Cantidad total de clientes institucionales.
 */
const totalClientes = computed(
  (): number => clientes.value.length,
)

/**
 * Cantidad de clientes activos.
 */
const clientesActivos = computed(
  (): number =>
    clientes.value.filter(
      (cliente) => cliente.activo,
    ).length,
)

/**
 * Cantidad de clientes inactivos.
 */
const clientesInactivos = computed(
  (): number =>
    clientes.value.filter(
      (cliente) => !cliente.activo,
    ).length,
)

/**
 * Cantidad de tipos institucionales diferentes.
 */
const tiposUtilizados = computed(
  (): number =>
    new Set(
      clientes.value.map(
        (cliente) => cliente.tipoEntidad,
      ),
    ).size,
)

/**
 * Recupera los clientes almacenados en PostgreSQL.
 */
async function cargarClientes(): Promise<void> {
  cargando.value = true
  mensaje.value = ''
  esError.value = false

  try {
    clientes.value = await listarClientes()
  } catch (error) {
    esError.value = true
    mensaje.value =
      error instanceof Error
        ? error.message
        : 'No fue posible consultar los clientes.'
  } finally {
    cargando.value = false
  }
}

/**
 * Registra una nueva institución mediante la API.
 */
async function guardarCliente(): Promise<void> {
  guardando.value = true
  mensaje.value = ''
  esError.value = false

  try {
    const clienteCreado = await registrarCliente({
      nombreEntidad: formulario.nombreEntidad.trim(),
      tipoEntidad: formulario.tipoEntidad,
      numeroIdentificacionFiscal:
        normalizarTextoOpcional(
          formulario.numeroIdentificacionFiscal,
        ),
      nombreContacto: formulario.nombreContacto.trim(),
      correoContacto:
        formulario.correoContacto.trim().toLowerCase(),
      telefonoContacto:
        normalizarTextoOpcional(
          formulario.telefonoContacto,
        ),
      direccion:
        normalizarTextoOpcional(formulario.direccion),
      ciudad:
        normalizarTextoOpcional(formulario.ciudad),
      activo: formulario.activo,
    })

    clientes.value.unshift(clienteCreado)

    mensaje.value =
      `Cliente ${clienteCreado.nombreEntidad} registrado correctamente.`

    limpiarFormulario()
  } catch (error) {
    esError.value = true
    mensaje.value =
      error instanceof Error
        ? error.message
        : 'No fue posible registrar el cliente.'
  } finally {
    guardando.value = false
  }
}

/**
 * Activa o inactiva una institución mediante PUT.
 *
 * @param cliente Cliente que se actualizará.
 */
async function cambiarEstado(
  cliente: ClienteInstitucionalResponse,
): Promise<void> {
  mensaje.value = ''
  esError.value = false

  try {
    const clienteActualizado = await actualizarCliente(
      cliente.id,
      {
        nombreEntidad: cliente.nombreEntidad,
        tipoEntidad: cliente.tipoEntidad,
        numeroIdentificacionFiscal:
          cliente.numeroIdentificacionFiscal,
        nombreContacto: cliente.nombreContacto,
        correoContacto: cliente.correoContacto,
        telefonoContacto: cliente.telefonoContacto,
        direccion: cliente.direccion,
        ciudad: cliente.ciudad,
        activo: !cliente.activo,
      },
    )

    const indice = clientes.value.findIndex(
      (item) => item.id === cliente.id,
    )

    if (indice >= 0) {
      clientes.value[indice] = clienteActualizado
    }

    mensaje.value =
      `${clienteActualizado.nombreEntidad} quedó ` +
      `${clienteActualizado.activo ? 'activo' : 'inactivo'}.`
  } catch (error) {
    esError.value = true
    mensaje.value =
      error instanceof Error
        ? error.message
        : 'No fue posible actualizar el estado.'
  }
}

/**
 * Convierte cadenas vacías en valores nulos.
 */
function normalizarTextoOpcional(
  value: string | null,
): string | null {
  const normalizado = value?.trim()

  return normalizado ? normalizado : null
}

/**
 * Restablece los valores iniciales del formulario.
 */
function limpiarFormulario(): void {
  formulario.nombreEntidad = ''
  formulario.tipoEntidad = 'EMBAJADA'
  formulario.numeroIdentificacionFiscal = null
  formulario.nombreContacto = ''
  formulario.correoContacto = ''
  formulario.telefonoContacto = null
  formulario.direccion = null
  formulario.ciudad = null
  formulario.activo = true
}

/**
 * Convierte el enum en un texto visible.
 */
function formatearTipo(
  tipo: TipoEntidad,
): string {
  return tipo
    .replaceAll('_', ' ')
    .toLocaleLowerCase('es')
    .replace(
      /^./,
      (letra) => letra.toLocaleUpperCase('es'),
    )
}

onMounted(cargarClientes)
</script>

<template>
  <DemoLayout
    module-number="02"
    eyebrow="Gestión institucional"
    title="Clientes"
    description="Administración real de embajadas, consulados y organismos internacionales conectados con PostgreSQL."
  >
    <section class="summary-grid">
      <article class="summary-card dark">
        <small>Total</small>
        <strong>{{ totalClientes }}</strong>
        <p>Instituciones registradas</p>
      </article>

      <article class="summary-card light">
        <small>Activos</small>
        <strong>{{ clientesActivos }}</strong>
        <p>Habilitados para solicitar servicios</p>
      </article>

      <article class="summary-card red">
        <small>Inactivos</small>
        <strong>{{ clientesInactivos }}</strong>
        <p>Instituciones temporalmente bloqueadas</p>
      </article>

      <article class="summary-card gold">
        <small>Clasificaciones</small>
        <strong>{{ tiposUtilizados }}</strong>
        <p>Tipos institucionales en uso</p>
      </article>
    </section>

    <p
      v-if="mensaje"
      class="system-message"
      :class="{ error: esError }"
      role="status"
      aria-live="polite"
    >
      <span>{{ esError ? '!' : '✓' }}</span>

      {{ mensaje }}

      <button
        type="button"
        aria-label="Cerrar mensaje"
        @click="mensaje = ''"
      >
        ×
      </button>
    </p>

    <section class="content-grid">
      <article class="registration-panel">
        <header class="panel-heading">
          <span>NEW</span>

          <div>
            <small>Alta institucional</small>
            <h2>Registrar cliente</h2>
          </div>
        </header>

        <form
          class="client-form"
          @submit.prevent="guardarCliente"
        >
          <label class="full-field">
            <span>Nombre de la entidad</span>

            <input
              v-model="formulario.nombreEntidad"
              type="text"
              maxlength="150"
              placeholder="Embajada de Aurora"
              required
            />
          </label>

          <label>
            <span>Tipo de entidad</span>

            <select
              v-model="formulario.tipoEntidad"
              required
            >
              <option
                v-for="tipo in tiposEntidad"
                :key="tipo.value"
                :value="tipo.value"
              >
                {{ tipo.label }}
              </option>
            </select>
          </label>

          <label>
            <span>Identificación fiscal</span>

            <input
              v-model="
                formulario.numeroIdentificacionFiscal
              "
              type="text"
              maxlength="40"
              placeholder="NIT o identificación"
            />
          </label>

          <label class="full-field">
            <span>Nombre del contacto</span>

            <input
              v-model="formulario.nombreContacto"
              type="text"
              maxlength="140"
              placeholder="Laura Gómez"
              required
            />
          </label>

          <label class="full-field">
            <span>Correo de contacto</span>

            <input
              v-model="formulario.correoContacto"
              type="email"
              maxlength="150"
              placeholder="contacto@institucion.gov"
              required
            />
          </label>

          <label>
            <span>Teléfono</span>

            <input
              v-model="formulario.telefonoContacto"
              type="tel"
              maxlength="30"
              placeholder="+57 300 000 0000"
            />
          </label>

          <label>
            <span>Ciudad</span>

            <input
              v-model="formulario.ciudad"
              type="text"
              maxlength="100"
              placeholder="Bogotá"
            />
          </label>

          <label class="full-field">
            <span>Dirección</span>

            <input
              v-model="formulario.direccion"
              type="text"
              maxlength="255"
              placeholder="Carrera 00 # 00-00"
            />
          </label>

          <label class="active-control full-field">
            <input
              v-model="formulario.activo"
              type="checkbox"
            />

            <span class="custom-checkbox"></span>

            <span>
              Cliente activo
              <small>
                Puede solicitar nuevos servicios.
              </small>
            </span>
          </label>

          <button
            class="primary-button full-field"
            type="submit"
            :disabled="guardando"
          >
            <span>
              {{
                guardando
                  ? 'Registrando...'
                  : 'Registrar cliente'
              }}
            </span>

            <strong>➜</strong>
          </button>
        </form>
      </article>

      <article class="clients-panel">
        <header class="clients-header">
          <div class="panel-heading dark-heading">
            <span>LIST</span>

            <div>
              <small>Directorio institucional</small>
              <h2>Clientes registrados</h2>
            </div>
          </div>

          <button
            class="refresh-button"
            type="button"
            :disabled="cargando"
            @click="cargarClientes"
          >
            {{ cargando ? 'Consultando...' : 'Actualizar' }}
          </button>
        </header>

        <div class="search-bar">
          <span aria-hidden="true">⌕</span>

          <input
            v-model="busqueda"
            type="search"
            placeholder="Buscar institución, contacto, correo o ciudad..."
            aria-label="Buscar clientes"
          />

          <strong>
            {{ clientesFiltrados.length }}
            resultado{{
              clientesFiltrados.length === 1 ? '' : 's'
            }}
          </strong>
        </div>

        <div
          v-if="cargando"
          class="empty-state"
        >
          <strong>Consultando clientes...</strong>
        </div>

        <div
          v-else-if="!hayClientes"
          class="empty-state"
        >
          <span>0</span>
          <strong>No hay clientes registrados</strong>
          <p>
            Utiliza el formulario para registrar la primera
            institución.
          </p>
        </div>

        <div
          v-else-if="clientesFiltrados.length === 0"
          class="empty-state"
        >
          <span>?</span>
          <strong>Sin coincidencias</strong>
          <p>Prueba con otro criterio de búsqueda.</p>
        </div>

        <div
          v-else
          class="clients-list"
        >
          <article
            v-for="(cliente, index) in clientesFiltrados"
            :key="cliente.id"
            class="client-card"
          >
            <span class="client-number">
              {{ String(index + 1).padStart(2, '0') }}
            </span>

            <div class="client-identity">
              <small>
                {{ formatearTipo(cliente.tipoEntidad) }}
              </small>

              <strong>{{ cliente.nombreEntidad }}</strong>

              <p>
                {{ cliente.nombreContacto }} ·
                {{ cliente.correoContacto }}
              </p>
            </div>

            <div class="client-location">
              <small>Ubicación</small>
              <strong>
                {{ cliente.ciudad || 'Sin ciudad' }}
              </strong>
              <p>
                {{
                  cliente.numeroIdentificacionFiscal ||
                  'Sin identificación fiscal'
                }}
              </p>
            </div>

            <div class="client-actions">
              <span
                class="status-badge"
                :class="{
                  active: cliente.activo,
                  inactive: !cliente.activo,
                }"
              >
                {{ cliente.activo ? 'Activo' : 'Inactivo' }}
              </span>

              <button
                type="button"
                @click="cambiarEstado(cliente)"
              >
                {{
                  cliente.activo
                    ? 'Inactivar'
                    : 'Activar'
                }}
              </button>
            </div>
          </article>
        </div>
      </article>
    </section>
  </DemoLayout>
</template>

<style scoped>
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  min-height: 125px;
  padding: 21px;
  color: #ffffff;
  background: #111111;
  clip-path: polygon(
    4% 0,
    100% 6%,
    96% 100%,
    0 91%
  );
}

.summary-card small {
  font-size: 0.64rem;
  font-weight: 950;
  letter-spacing: 0.11em;
  text-transform: uppercase;
}

.summary-card strong {
  display: block;
  margin-top: 2px;
  font-size: 2.7rem;
  font-weight: 1000;
  line-height: 1;
}

.summary-card p {
  margin: 8px 0 0;
  font-size: 0.7rem;
  opacity: 0.65;
}

.summary-card.light {
  color: #111111;
  background: #ffffff;
}

.summary-card.red {
  background: #d71920;
}

.summary-card.gold {
  color: #111111;
  background: #c9a34f;
}

.system-message {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 24px 0 0;
  padding: 13px 45px 13px 15px;
  border: 3px solid #111111;
  background: #ffffff;
  box-shadow: 6px 6px 0 #111111;
  font-size: 0.8rem;
  font-weight: 800;
}

.system-message.error {
  color: #ffffff;
  background: #d71920;
}

.system-message > span {
  display: grid;
  width: 27px;
  height: 27px;
  place-items: center;
  color: #ffffff;
  background: #111111;
}

.system-message button {
  position: absolute;
  top: 2px;
  right: 11px;
  border: 0;
  color: inherit;
  background: transparent;
  font-size: 1.7rem;
  cursor: pointer;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(340px, 420px) minmax(0, 1fr);
  gap: 24px;
  margin-top: 28px;
}

.registration-panel {
  align-self: start;
  padding: 25px;
  color: #ffffff;
  background: #111111;
  box-shadow: 11px 11px 0 #d71920;
  clip-path: polygon(
    3% 0,
    100% 2%,
    97% 100%,
    0 97%
  );
}

.panel-heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.panel-heading > span {
  display: grid;
  min-width: 53px;
  height: 40px;
  place-items: center;
  color: #ffffff;
  background: #d71920;
  font-size: 0.68rem;
  font-weight: 1000;
  transform: skew(-8deg);
}

.panel-heading small {
  color: #c9a34f;
  font-size: 0.62rem;
  font-weight: 950;
  letter-spacing: 0.13em;
  text-transform: uppercase;
}

.panel-heading h2 {
  margin: 2px 0 0;
  font-size: 1.38rem;
  font-weight: 1000;
  text-transform: uppercase;
}

.client-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 13px;
  margin-top: 22px;
}

.client-form label {
  display: grid;
  gap: 6px;
}

.client-form label > span:first-child {
  font-size: 0.65rem;
  font-weight: 900;
  letter-spacing: 0.07em;
  text-transform: uppercase;
}

.client-form input,
.client-form select {
  width: 100%;
  min-width: 0;
  padding: 11px 12px;
  border: 2px solid transparent;
  border-radius: 0;
  outline: none;
  clip-path: polygon(
    2% 0,
    100% 3%,
    97% 100%,
    0 95%
  );
}

.client-form input:focus,
.client-form select:focus {
  border-color: #d71920;
  box-shadow: 4px 4px 0 #d71920;
}

.full-field {
  grid-column: 1 / -1;
}

.active-control {
  display: flex !important;
  align-items: center;
  gap: 10px !important;
  padding: 10px;
  border: 1px solid rgb(255 255 255 / 17%);
  cursor: pointer;
}

.active-control input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.custom-checkbox {
  display: grid;
  width: 25px;
  height: 25px;
  flex: 0 0 25px;
  place-items: center;
  border: 2px solid #ffffff;
}

.active-control input:checked + .custom-checkbox {
  border-color: #d71920;
  background: #d71920;
}

.active-control input:checked + .custom-checkbox::after {
  content: "✓";
  color: #ffffff;
  font-weight: 1000;
}

.active-control small {
  display: block;
  margin-top: 2px;
  color: rgb(255 255 255 / 52%);
  font-size: 0.58rem;
}

.primary-button {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 17px;
  border: 0;
  color: #ffffff;
  background: #d71920;
  font-size: 0.75rem;
  font-weight: 1000;
  text-transform: uppercase;
  cursor: pointer;
}

.primary-button:hover:not(:disabled) {
  color: #111111;
  background: #ffffff;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.clients-panel {
  min-width: 0;
  padding: 26px;
  overflow: hidden;
  background: #ffffff;
  box-shadow: 12px 12px 0 rgb(17 17 17 / 13%);
  clip-path: polygon(
    1% 0,
    99% 1%,
    100% 97%,
    2% 100%,
    0 4%
  );
}

.clients-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.dark-heading small {
  color: #d71920;
}

.refresh-button {
  padding: 10px 13px;
  border: 2px solid #111111;
  color: #111111;
  background: #ffffff;
  font-size: 0.67rem;
  font-weight: 950;
  text-transform: uppercase;
  cursor: pointer;
}

.refresh-button:hover {
  color: #ffffff;
  background: #111111;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 22px;
  padding: 11px;
  color: #ffffff;
  background: #111111;
}

.search-bar > span {
  color: #d71920;
  font-size: 1.35rem;
}

.search-bar input {
  min-width: 0;
  flex: 1;
  padding: 8px 10px;
  border: 0;
  outline: none;
}

.search-bar strong {
  font-size: 0.63rem;
  text-transform: uppercase;
  white-space: nowrap;
}

.clients-list {
  display: grid;
  gap: 10px;
  margin-top: 18px;
}

.client-card {
  position: relative;
  display: grid;
  grid-template-columns:
    45px minmax(190px, 1.1fr)
    minmax(150px, 0.7fr) auto;
  align-items: center;
  gap: 14px;
  padding: 14px 15px 14px 8px;
  border-bottom: 2px solid #111111;
  background: #f1eee7;
}

.client-card::before {
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
  content: "";
  background: #d71920;
}

.client-number {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: #ffffff;
  background: #d71920;
  font-size: 0.7rem;
  font-weight: 1000;
}

.client-identity small,
.client-location small {
  color: #77828c;
  font-size: 0.54rem;
  font-weight: 950;
  text-transform: uppercase;
}

.client-identity strong,
.client-location strong {
  display: block;
  margin-top: 3px;
  font-size: 0.86rem;
}

.client-identity p,
.client-location p {
  margin: 4px 0 0;
  color: #617080;
  font-size: 0.66rem;
}

.client-actions {
  display: grid;
  justify-items: end;
  gap: 6px;
}

.status-badge {
  padding: 6px 9px;
  font-size: 0.56rem;
  font-weight: 1000;
  text-transform: uppercase;
}

.status-badge.active {
  color: #116335;
  background: #dff4e7;
}

.status-badge.inactive {
  color: #9a080d;
  background: #ffdadd;
}

.client-actions button {
  border: 0;
  color: #ffffff;
  background: #111111;
  padding: 6px 9px;
  font-size: 0.57rem;
  font-weight: 900;
  text-transform: uppercase;
  cursor: pointer;
}

.empty-state {
  display: grid;
  min-height: 280px;
  place-content: center;
  justify-items: center;
  gap: 7px;
  text-align: center;
}

.empty-state > span {
  display: grid;
  width: 70px;
  height: 70px;
  place-items: center;
  color: #ffffff;
  background: #d71920;
  font-size: 2.3rem;
  font-weight: 1000;
}

.empty-state p {
  margin: 0;
  color: #617080;
  font-size: 0.75rem;
}

@media (max-width: 1150px) {
  .summary-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .client-form {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .summary-grid,
  .client-form {
    grid-template-columns: 1fr;
  }

  .full-field {
    grid-column: auto;
  }

  .clients-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .search-bar {
    align-items: stretch;
    flex-direction: column;
  }

  .client-card {
    grid-template-columns: 42px 1fr;
  }

  .client-location,
  .client-actions {
    grid-column: 2;
  }

  .client-actions {
    justify-items: start;
  }
}
</style>
