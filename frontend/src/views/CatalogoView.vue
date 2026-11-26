<script setup lang="ts">
/**
 * Componente Catalogo:
 * vista pública principal del proyecto. La entrega final refuerza la dirección
 * artística de alto contraste con planos inclinados y animaciones de entrada
 * limitadas a la presentación, sin alterar la lógica real de reservas.
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import FleetNav from '@/components/layout/FleetNav.vue'
import DialogoBase from '@/components/layout/DialogoBase.vue'
import VehiculoFoto from '@/components/catalogo/VehiculoFoto.vue'
import { vAparecer } from '@/directives/aparecer'
import { api,sesion } from '@/services/api'
import {
  catalogo,
  crearReserva,
  pesos,
  isoFecha,
  proximaFecha,
  diasEntre,
  type Coche,
  type MetodoPago,
} from '@/services/reservaService'

const router = useRouter()
const inicio = ref(proximaFecha(1))
const fin = ref(proximaFecha(3))
const cars = ref<Coche[]>([])
const cargando = ref(false)
const error = ref('')
const periodoTocado = ref(false)
const errorTarjeta = ref<{id:number;mensaje:string}|null>(null)
const cotizando = ref(false)
const confirmacion = ref('')
const seleccion = ref<Coche | null>(null)
const guardando = ref(false)

const recogida = ref('Aeropuerto El Dorado, Bogotá')
const devolucion = ref('Bogotá, Colombia')
const pasajeros = ref(1)
const observaciones = ref('')
const metodoPago = ref<MetodoPago>('TRANSFERENCIA')

/**
 * Filtros de presentación.
 * No modifican la disponibilidad real, que siempre se consulta en el servidor.
 */
const texto = ref('')
const categoria = ref('')
const orden = ref('precio-asc')
const capacidad = ref(0)
const presupuesto = ref(0)
const modalidad = ref<'SIN_CONDUCTOR' | 'CON_CONDUCTOR'>('SIN_CONDUCTOR')
/** Solo los vehículos con nivel declarado pueden satisfacer el filtro. */
const requiereBlindaje=ref(false)
const blindado=(c:Coche)=>!!c.nivelBlindaje&&!['NO_DECLARADO','SIN_BLINDAJE'].includes(c.nivelBlindaje)
const ubicaciones=ref<string[]>([])

const visibles = computed(() => cars.value
  .filter(c => (!categoria.value || c.categoria === categoria.value)
    && c.pasajeros >= capacidad.value
    && (!requiereBlindaje.value || blindado(c))
    && (!presupuesto.value || c.tarifaDiaria <= presupuesto.value)
    && `${c.marca} ${c.modelo}`.toLowerCase().includes(texto.value.toLowerCase()))
  .sort((a, b) => {
    if (orden.value === 'precio-desc') return b.tarifaDiaria - a.tarifaDiaria
    if (orden.value === 'capacidad') return b.pasajeros - a.pasajeros
    return a.tarifaDiaria - b.tarifaDiaria
  }))

/** Limpia los filtros locales sin modificar el periodo ya consultado. */
function limpiarFiltros() {
  texto.value = ''
  categoria.value = ''
  orden.value = 'precio-asc'
  capacidad.value = 0
  presupuesto.value = 0
  requiereBlindaje.value=false
}

/**
 * El periodo consultado se conserva para impedir reservas con datos obsoletos.
 */
const periodoConsultado = ref('')
const errorDialogo = ref('')
const periodoActual = computed(() => `${inicio.value}/${fin.value}`)
const consultaVigente = computed(() => periodoConsultado.value === periodoActual.value)

watch(periodoActual, () => {
  confirmacion.value = ''
  error.value = ''
  errorTarjeta.value = null
  periodoTocado.value = true
  seleccion.value = null
})

const categorias = computed(() => Array.from(new Set(cars.value.map(c => c.categoria))))
const dias = computed(() => inicio.value && fin.value ? diasEntre(isoFecha(inicio.value), isoFecha(fin.value)) : 0)
const errorFechas = computed(() => {
  if (!inicio.value || !fin.value) return 'Complete las dos fechas.'
  if (inicio.value < proximaFecha(1)) return 'La recogida debe ser a partir de mañana.'
  if (fin.value <= inicio.value) return 'La fecha de devolución debe ser posterior a la fecha de recogida.'
  if (diasEntre(isoFecha(inicio.value),isoFecha(fin.value)) > 90) return 'El periodo máximo es de 90 días.'
  return ''
})
const fechasValidas = computed(() => !errorFechas.value)
const tarifaMinima = computed(() => visibles.value.length ? Math.min(...visibles.value.map(c => c.tarifaDiaria)) : 0)
const capacidadMaxima = computed(() => visibles.value.length ? Math.max(...visibles.value.map(c => c.pasajeros)) : 0)
const categoriasActivas = computed(() => new Set(visibles.value.map(c => c.categoria)).size)

/** Consulta la flota disponible para el periodo seleccionado. */
async function cargar() {
  if (cargando.value) return
  periodoTocado.value = true
  error.value = ''
  if (!fechasValidas.value) return

  const periodo = periodoActual.value
  cargando.value = true
  error.value = ''
  confirmacion.value = ''
  seleccion.value = null
  periodoConsultado.value = ''

  try {
    const datos = await catalogo(isoFecha(inicio.value), isoFecha(fin.value))
    if (periodo === periodoActual.value) {
      cars.value = datos
      periodoConsultado.value = periodo
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Error consultando vehículos'
  } finally {
    cargando.value = false
  }
}

/**
 * Solicita autenticación antes de abrir el formulario de reserva.
 * Solo CLIENTE puede crear solicitudes desde el catálogo público.
 */
function reservar(c: Coche, solicitarCotizacion = false) {
  errorTarjeta.value = null
  if (!consultaVigente.value) {
    error.value = 'Consulte la disponibilidad para las fechas seleccionadas.'
    return
  }
  if (!sesion()) {
    router.push({ path: '/login', query: { destino: '/catalogo' } })
    return
  }
  if (sesion()?.rol !== 'CLIENTE') {
    errorTarjeta.value = {id:c.id,mensaje:'Ingrese con una cuenta de cliente institucional para continuar.'}
    return
  }
  cotizando.value = solicitarCotizacion
  seleccion.value = c
  pasajeros.value = Math.max(1, capacidad.value)
  observaciones.value = ''
  modalidad.value = 'SIN_CONDUCTOR'
  errorDialogo.value = ''
}

/** Registra la solicitud mediante la API del backend. */
async function confirmar() {
  if (guardando.value || !seleccion.value || !fechasValidas.value || !consultaVigente.value) return
  guardando.value = true
  errorDialogo.value = ''
  try {
    const r = await crearReserva({
      vehiculoId: seleccion.value.id,
      inicio: isoFecha(inicio.value),
      fin: isoFecha(fin.value),
      recogida: recogida.value,
      devolucion: devolucion.value,
      pasajeros: pasajeros.value,
      observaciones: observaciones.value,
      metodoPago: metodoPago.value,
      modalidad: modalidad.value,
      requiereBlindaje: requiereBlindaje.value,
      solicitarCotizacion: cotizando.value,
    })
    seleccion.value = null
    if (cotizando.value) {
      if (r.estadoCotizacion !== 'BORRADOR') {
        await cargar()
        confirmacion.value = 'Solicitud ' + r.codigo + ' registrada, pero el servidor no ha generado el borrador. Consulte administración y no repita la solicitud.'
        return
      }
      await router.push({path:'/mis-cotizaciones',query:{creada:r.codigo}})
      return
    }
    await cargar()
    confirmacion.value = `Solicitud ${r.codigo} creada correctamente. Importe estimado: ${pesos(r.valorEstimado)}. Pago externo pendiente: consulta su estado en Mis reservas.`
  } catch (e) {
    errorDialogo.value = e instanceof Error ? e.message : 'No fue posible reservar'
  } finally {
    guardando.value = false
  }
}

onMounted(()=>{void cargar();void api<string[]>('/api/public/ubicaciones',{},true).then(v=>ubicaciones.value=v).catch(()=>{})})
</script>

<template>
  <div class="df-page df-catalog-page">
    <FleetNav />

    <!-- Capas geométricas independientes: el fondo permanece oscuro también al desplazar. -->
    <section class="df-hero" aria-labelledby="catalogo-titulo">
      <div class="df-hero-noise" aria-hidden="true"></div>
      <div class="df-hero-slash" aria-hidden="true"></div>
      <div class="df-hero-burst" aria-hidden="true"></div>
      <div class="df-hero-art" aria-hidden="true">
        <span class="df-hero-index">DF / 26</span>
        <img class="df-hero-car" src="/assets/images/portada-flota.webp" alt="" fetchpriority="high" />
        <span class="df-hero-art-caption">DIPLOMATIC FLEET / MOVILIDAD INSTITUCIONAL</span>
      </div>
      <div class="df-hero-copy">
        <span class="eyebrow">Transporte institucional / Colombia</span>
        <h1 id="catalogo-titulo"><span>Alquiler</span><strong>vehicular</strong><span>diplomático.</span></h1>
        <p>Seleccione sus fechas, consulte la flota y reserve el vehículo adecuado para su institución.</p>
        <a class="df-hero-cta" href="#flota">Consultar vehículos <span aria-hidden="true">↓</span></a>
        <span class="df-hero-note">Consulta pública · Sin registro previo</span>
      </div>
    </section>
    <div class="df-motion-band" aria-hidden="true"><div></div></div>

    <main class="df-wrap" id="flota">
      <form class="df-filter df-filter-hero df-periodo" novalidate @submit.prevent="cargar">
        <div>
          <label for="inicio">Recogida</label>
          <input id="inicio" v-model="inicio" type="date" :min="proximaFecha(1)" :disabled="cargando" required />
        </div>
        <div>
          <label for="fin">Devolución</label>
          <input id="fin" v-model="fin" type="date" :min="inicio" :disabled="cargando" :aria-invalid="periodoTocado && !!errorFechas" aria-describedby="error-periodo" required />
        </div>
        <button class="df-button" :disabled="cargando" type="submit">
          <span v-if="cargando" class="df-inline-spinner" aria-hidden="true"></span>
          {{ cargando ? 'Consultando…' : 'Ver disponibilidad ↗' }}
        </button>
        <p v-if="periodoTocado && errorFechas" id="error-periodo" class="df-alert error df-periodo-error" role="alert">{{errorFechas}}</p>
        <p v-else-if="error" id="error-periodo" class="df-alert error df-periodo-error" role="alert">{{error}}</p>
        <p v-else id="error-periodo" class="df-periodo-error df-meta">Elija las fechas del servicio. La cotización no realiza ningún cobro.</p>
      </form>

      <section class="df-intro-ribbon" aria-label="Resumen del catálogo">
        <article class="df-ribbon-card">
          <span class="df-kicker">Flota visible</span>
          <strong>{{ consultaVigente ? visibles.length : "—" }}</strong>
          <small>vehículos según filtros</small>
        </article>
        <article class="df-ribbon-card">
          <span class="df-kicker">Tarifa inicial</span>
          <strong>{{ consultaVigente && tarifaMinima ? pesos(tarifaMinima) : '—' }}</strong>
          <small>valor diario más bajo</small>
        </article>
        <article class="df-ribbon-card">
          <span class="df-kicker">Capacidad máxima</span>
          <strong>{{ consultaVigente ? capacidadMaxima : "—" }}</strong>
          <small>pasajeros por unidad</small>
        </article>
        <article class="df-ribbon-card">
          <span class="df-kicker">Categorías</span>
          <strong>{{ consultaVigente ? categoriasActivas : "—" }}</strong>
          <small>líneas disponibles</small>
        </article>
      </section>

      <div class="df-heading">
        <div>
          <span class="df-kicker">01 / Catálogo institucional</span>
          <h2>Flota disponible<span class="df-highlight">.</span></h2>
          <p>Vehículos disponibles para el periodo elegido · Precios de referencia en COP.</p>
        </div>
        <div class="df-kicker">{{ consultaVigente ? `${visibles.length} de ${cars.length} vehículos` : 'Consulta por periodo' }}</div>
      </div>

      <div class="df-searchbar">
        <label>
          Buscar modelo
          <input v-model="texto" type="search" placeholder="Marca o modelo" />
        </label>
        <label>
          Categoría
          <select v-model="categoria">
            <option value="">Todas</option>
            <option v-for="c in categorias" :key="c" :value="c">{{ c }}</option>
          </select>
        </label>
        <label>
          Orden
          <select v-model="orden">
            <option value="precio-asc">Menor tarifa</option>
            <option value="precio-desc">Mayor tarifa</option>
            <option value="capacidad">Mayor capacidad</option>
          </select>
        </label>
        <label>
          Pasajeros mínimos
          <select v-model.number="capacidad">
            <option :value="0">Todos</option>
            <option v-for="n in [4, 5, 6, 7, 8]" :key="n" :value="n">{{ n }} o más</option>
          </select>
        </label>
        <label>
          Tarifa diaria máxima (COP)
          <select v-model.number="presupuesto">
            <option :value="0">Sin límite</option>
            <option v-for="n in [300000, 500000, 750000, 1000000, 1500000]" :key="n" :value="n">{{ pesos(n) }}</option>
          </select>
        </label>
        <label>Blindaje requerido<select v-model="requiereBlindaje"><option :value="false">Todas las unidades</option><option :value="true">Nivel declarado</option></select></label>
        <button type="button" class="df-button light mini df-reset-filters" @click="limpiarFiltros">Restablecer filtros</button>
      </div>

      <p v-if="confirmacion" class="df-alert ok" role="status">
        {{ confirmacion }} <RouterLink to="/mis-reservas"><u>Ver mis reservas</u></RouterLink>
      </p>

      <div v-if="cargando" role="status" aria-live="polite">
        <p class="df-meta">Consultando disponibilidad en PostgreSQL…</p>
        <div class="df-grid" aria-hidden="true">
          <div v-for="n in 6" :key="n" class="df-skeleton"><span></span><i></i><i></i></div>
        </div>
      </div>

      <div v-else-if="!consultaVigente" class="df-empty">Seleccione las fechas y pulse Ver disponibilidad para consultar la flota.</div>
      <div v-else-if="!visibles.length" class="df-empty">No hay vehículos que coincidan con la consulta. Ajuste las fechas o los filtros.</div>

      <div v-else class="df-grid">
        <article
          v-for="(car, i) in visibles"
          :key="car.id"
          class="df-card"
          v-aparecer
        >
          <div class="df-card-media">
            <span class="df-card-num">{{ String(i + 1).padStart(2, '0') }}</span>
            <span class="df-card-corner" aria-hidden="true"></span>
            <VehiculoFoto :url="car.imagenUrl" :modelo="car.marca + ' ' + car.modelo" />
          </div>
          <div class="df-card-body">
            <span class="df-pill">{{ car.categoria }}</span>
            <h3>{{ car.marca }} {{ car.modelo }}</h3>
            <div class="df-specs">
              <span>◉ {{ car.anio }}</span>
              <span>♙ {{ car.pasajeros }} pasajeros</span>
              <span>▣ {{ car.equipaje }} maletas</span>
              <span v-if="blindado(car)">Blindaje: {{ car.nivelBlindaje?.replaceAll('_',' ') }}</span>
            </div>
            <div class="df-card-bottom">
              <div class="df-price">
                <strong>{{ pesos(car.tarifaDiaria) }}</strong>
                <small>por día · {{ pesos(car.tarifaDiaria * dias) }} estimado</small>
              </div>
              <div class="df-catalog-actions">
                <button class="df-button mini" type="button" @click="reservar(car,true)">Cotizar ↗</button>
                <button class="df-button light mini" type="button" @click="reservar(car,false)">Reservar</button>
              </div>
            </div>
            <p v-if="errorTarjeta?.id===car.id" class="df-alert error" role="alert">{{errorTarjeta.mensaje}}</p>
          </div>
        </article>
      </div>

      <p class="df-meta" style="margin-top: 30px">
        * Flota precargada de demostración. Las fotografías son referencias de los modelos; el año, color o versión pueden diferir de la unidad registrada.
        El servicio con conductor requiere asignación administrativa. Las tarifas mostradas
        son de demostración y no incluyen cargos adicionales de conductor. No se procesa pago en línea.
      </p>
    </main>

    <footer class="df-footer">
      DIPLOMATIC FLEET · SERVICIOS DE MOVILIDAD INSTITUCIONAL
    </footer>

    <DialogoBase v-if="seleccion" :titulo="seleccion.marca + ' ' + seleccion.modelo" :ocupado="guardando" @cerrar="seleccion = null">
      <span class="df-kicker">{{cotizando ? '02 / SOLICITAR COTIZACIÓN' : '02 / SOLICITAR RESERVA'}}</span>
      <p v-if="cotizando" class="df-meta">Enviaremos una propuesta preliminar a administración. El vehículo quedará solicitado para estas fechas hasta resolver la solicitud. Puede consultar el estado y aceptar o rechazar la propuesta en Mis cotizaciones. No se confirma ni se cobra el alquiler todavía.</p>
      <p class="df-meta">{{ inicio }} → {{ fin }} · {{ dias }} día(s) · {{ pesos(seleccion.tarifaDiaria * dias) }} estimados.</p>
      <form @submit.prevent="confirmar">
        <div class="df-fields">
          <div class="df-field">
            <label>Lugar de recogida</label>
            <input v-model="recogida" list="df-ubicaciones" minlength="4" maxlength="255" required />
          </div>
          <div class="df-field">
            <label>Lugar de devolución</label>
            <input v-model="devolucion" list="df-ubicaciones" minlength="4" maxlength="255" required />
            <datalist id="df-ubicaciones"><option v-for="u in ubicaciones" :key="u" :value="u" /></datalist>
          </div>
        </div>
        <div class="df-fields">
          <div class="df-field">
            <label>Pasajeros</label>
            <input v-model.number="pasajeros" type="number" min="1" :max="seleccion.pasajeros" required />
          </div>
          <div class="df-field">
            <label>Observaciones (opcional)</label>
            <input v-model="observaciones" maxlength="1000" />
          </div>
        </div>
        <!-- Pago externo: no se reciben datos de tarjeta ni se registra un cobro real desde esta web. -->
        <div class="df-field">
          <label>Modalidad de servicio</label>
          <select v-model="modalidad">
            <option value="SIN_CONDUCTOR">Sin conductor</option>
            <option value="CON_CONDUCTOR">Con conductor profesional (sujeto a asignación)</option>
          </select>
        </div>
        <div class="df-field">
          <label>Método de pago previsto (sin cobro) *</label>
          <select v-model="metodoPago" required>
            <option value="TRANSFERENCIA">Transferencia bancaria</option>
            <option value="EFECTIVO">Efectivo en la entrega</option>
            <option value="TARJETA_PRESENCIAL">Tarjeta presencial con el operador</option>
          </select>
        </div>
        <p v-if="cotizando" class="df-meta">Este dato solo indica su preferencia. No informe transferencias ni realice pagos hasta aceptar la cotización y coordinar con administración.</p>
        <p v-else class="df-meta">
          El pago queda <b>PENDIENTE</b>. Espere la confirmación administrativa antes de efectuar el pago externo.
          Transferencias: solicite los datos bancarios al proveedor del servicio; después podrá
          informar la referencia en Mis reservas. Para tarjeta, <b>no introduzca números ni códigos CVV en esta página</b>.
          No se realiza ningún cargo en línea.
        </p>
        <p v-if="errorDialogo" class="df-alert error" role="alert">{{ errorDialogo }}</p>
        <button class="df-button" type="submit" :disabled="guardando">
          <span v-if="guardando" class="df-inline-spinner" aria-hidden="true"></span>
          {{ guardando ? 'Enviando…' : cotizando ? 'Enviar solicitud de cotización →' : 'Crear solicitud de reserva →' }}
        </button>
      </form>
    </DialogoBase>
  </div>
</template>
