<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import DemoModuleView, {
  type DemoRecord,
} from '@/views/demo/DemoModuleView.vue'

/**
 * Configuración de una vista demostrativa.
 */
interface ModuleConfiguration {
  moduleNumber: string
  eyebrow: string
  title: string
  description: string
  sectionTitle: string
  sectionDescription: string
  actionLabel: string
  emptyMessage: string
  records: DemoRecord[]
}

const route = useRoute()
const message = ref('')

/**
 * Configuraciones de los módulos incluidos en la demo.
 *
 * Estos datos permiten representar los flujos principales mientras
 * se completa progresivamente su integración con Spring Boot.
 */
const configurations: Record<string, ModuleConfiguration> = {
  inicio: {
    moduleNumber: '01',
    eyebrow: 'Centro de operaciones',
    title: 'Panel principal',
    description:
      'Resumen de los módulos operativos y administrativos de Diplomatic Fleet.',
    sectionTitle: 'Estado de la plataforma',
    sectionDescription:
      'Consulta el avance de los procesos principales de la aplicación.',
    actionLabel: 'Actualizar panel',
    emptyMessage: 'No existen indicadores disponibles.',
    records: [
      {
        primary: 'Gestión de vehículos',
        secondary: 'Módulo conectado',
        detail:
          'Registro y consulta con persistencia real en PostgreSQL.',
        status: 'Operativo',
        statusType: 'success',
      },
      {
        primary: 'Clientes institucionales',
        secondary: 'Gestión administrativa',
        detail:
          'Embajadas, consulados y organismos internacionales.',
        status: 'En integración',
        statusType: 'warning',
      },
      {
        primary: 'Solicitudes y cotizaciones',
        secondary: 'Flujo de servicio',
        detail:
          'Creación, validación y cotización de servicios diplomáticos.',
        status: 'Demo',
        statusType: 'neutral',
      },
    ],
  },

  clientes: {
    moduleNumber: '02',
    eyebrow: 'Gestión institucional',
    title: 'Clientes',
    description:
      'Administración de embajadas, consulados y organismos internacionales.',
    sectionTitle: 'Clientes institucionales',
    sectionDescription:
      'Entidades habilitadas para solicitar servicios de transporte.',
    actionLabel: 'Registrar cliente',
    emptyMessage: 'No existen clientes institucionales registrados.',
    records: [
      {
        primary: 'Embajada de Aurora',
        secondary: 'Embajada',
        detail:
          'Contacto: Laura Gómez · contacto@aurora.example',
        status: 'Activo',
        statusType: 'success',
      },
      {
        primary: 'Consulado Internacional',
        secondary: 'Consulado',
        detail:
          'Contacto administrativo disponible para servicios institucionales.',
        status: 'Activo',
        statusType: 'success',
      },
    ],
  },

  documentos: {
    moduleNumber: '04',
    eyebrow: 'Control documental',
    title: 'Documentos',
    description:
      'Seguimiento de documentos, pólizas y fechas de vencimiento.',
    sectionTitle: 'Documentos vehiculares',
    sectionDescription:
      'Control de vigencia para los vehículos registrados.',
    actionLabel: 'Registrar documento',
    emptyMessage: 'No existen documentos registrados.',
    records: [
      {
        primary: 'SOAT · DFT123',
        secondary: 'Documento vehicular',
        detail:
          'Documento asociado al Toyota Corolla de categoría Ejecutiva.',
        status: 'Vigente',
        statusType: 'success',
      },
      {
        primary: 'Revisión tecnomecánica · DFT123',
        secondary: 'Control técnico',
        detail:
          'Seguimiento preventivo de la documentación obligatoria.',
        status: 'Próximo a vencer',
        statusType: 'warning',
      },
    ],
  },

  mantenimientos: {
    moduleNumber: '05',
    eyebrow: 'Control operativo',
    title: 'Mantenimientos',
    description:
      'Programación e historial de trabajos preventivos y correctivos.',
    sectionTitle: 'Historial de mantenimiento',
    sectionDescription:
      'Control de intervenciones realizadas sobre la flota.',
    actionLabel: 'Programar mantenimiento',
    emptyMessage: 'No existen mantenimientos registrados.',
    records: [
      {
        primary: 'Mantenimiento preventivo · DFT123',
        secondary: 'Toyota Corolla',
        detail:
          'Inspección general, niveles, frenos y sistema eléctrico.',
        status: 'Programado',
        statusType: 'warning',
      },
    ],
  },

  disponibilidad: {
    moduleNumber: '06',
    eyebrow: 'Consulta operativa',
    title: 'Disponibilidad',
    description:
      'Validación de vehículos disponibles según periodo y capacidad.',
    sectionTitle: 'Disponibilidad de la flota',
    sectionDescription:
      'Cruce de estado operativo, solicitudes y mantenimientos activos.',
    actionLabel: 'Buscar vehículos',
    emptyMessage:
      'Selecciona un periodo para consultar vehículos disponibles.',
    records: [
      {
        primary: 'DFT123 · Toyota Corolla',
        secondary: 'Categoría Ejecutiva',
        detail:
          'Capacidad para 5 pasajeros y 3 piezas de equipaje.',
        status: 'Disponible',
        statusType: 'success',
      },
    ],
  },

  solicitudes: {
    moduleNumber: '07',
    eyebrow: 'Servicios institucionales',
    title: 'Solicitudes',
    description:
      'Registro y seguimiento de solicitudes de transporte diplomático.',
    sectionTitle: 'Solicitudes de servicio',
    sectionDescription:
      'Servicios con vehículo y modalidad de conductor seleccionada.',
    actionLabel: 'Nueva solicitud',
    emptyMessage: 'No existen solicitudes de servicio.',
    records: [
      {
        primary: 'SOL-001',
        secondary: 'Embajada de Aurora',
        detail:
          'Modalidad sin conductor · Periodo institucional programado.',
        status: 'Cotizada',
        statusType: 'warning',
      },
    ],
  },

  conductor: {
    moduleNumber: '08',
    eyebrow: 'Validación documental',
    title: 'Conductor',
    description:
      'Comprobación de documentación y condiciones para conducir la unidad.',
    sectionTitle: 'Validación del conductor',
    sectionDescription:
      'Licencia, identidad y requisitos asociados a la solicitud.',
    actionLabel: 'Validar conductor',
    emptyMessage: 'No existen conductores pendientes de validación.',
    records: [
      {
        primary: 'Validación pendiente',
        secondary: 'Solicitud de servicio',
        detail:
          'Revisión de licencia y documentos requeridos por la operación.',
        status: 'Pendiente',
        statusType: 'warning',
      },
    ],
  },

  cotizaciones: {
    moduleNumber: '09',
    eyebrow: 'Gestión comercial',
    title: 'Cotizaciones',
    description:
      'Cálculo y seguimiento de propuestas para servicios institucionales.',
    sectionTitle: 'Cotizaciones de servicio',
    sectionDescription:
      'Valores estimados según categoría, periodo y modalidad.',
    actionLabel: 'Nueva cotización',
    emptyMessage: 'No existen cotizaciones registradas.',
    records: [
      {
        primary: 'COT-001 · SOL-001',
        secondary: 'Embajada de Aurora',
        detail:
          'Propuesta asociada a vehículo de categoría Ejecutiva.',
        status: 'Generada',
        statusType: 'success',
      },
    ],
  },

  usuarios: {
    moduleNumber: '10',
    eyebrow: 'Seguridad y acceso',
    title: 'Usuarios y roles',
    description:
      'Administración de cuentas, perfiles y permisos de la aplicación.',
    sectionTitle: 'Control de acceso',
    sectionDescription:
      'Usuarios habilitados y funciones asignadas dentro del sistema.',
    actionLabel: 'Registrar usuario',
    emptyMessage: 'No existen usuarios registrados.',
    records: [
      {
        primary: 'Administrador del sistema',
        secondary: 'Rol administrador',
        detail:
          'Acceso a gestión de flota, clientes, solicitudes y seguridad.',
        status: 'Activo',
        statusType: 'success',
      },
      {
        primary: 'Gestor de flota',
        secondary: 'Rol operativo',
        detail:
          'Acceso a vehículos, documentos y mantenimientos.',
        status: 'Activo',
        statusType: 'success',
      },
    ],
  },
}

/**
 * Obtiene la configuración correspondiente a la ruta actual.
 */
const currentConfiguration = computed(
  (): ModuleConfiguration => {
    const routeName =
      typeof route.name === 'string'
        ? route.name
        : 'inicio'

    const selectedConfiguration =
      configurations[routeName]

    if (selectedConfiguration) {
      return selectedConfiguration
    }

    return configurations.inicio!
  },
)

/**
 * Representa temporalmente la acción principal de cada módulo.
 */
function handleAction(): void {
  message.value =
    `Acción "${currentConfiguration.value.actionLabel}" disponible en la demo.`
}
</script>

<template>
  <DemoModuleView
    :module-number="currentConfiguration.moduleNumber"
    :eyebrow="currentConfiguration.eyebrow"
    :title="currentConfiguration.title"
    :description="currentConfiguration.description"
    :section-title="currentConfiguration.sectionTitle"
    :section-description="
      currentConfiguration.sectionDescription
    "
    :action-label="currentConfiguration.actionLabel"
    :empty-message="currentConfiguration.emptyMessage"
    :records="currentConfiguration.records"
    @action="handleAction"
  />

  <p
    v-if="message"
    class="demo-notification"
    role="status"
    aria-live="polite"
  >
    <strong>Demo:</strong>
    {{ message }}

    <button
      type="button"
      aria-label="Cerrar mensaje"
      @click="message = ''"
    >
      ×
    </button>
  </p>
</template>

<style scoped>
.demo-notification {
  position: fixed;
  right: 26px;
  bottom: 25px;
  z-index: 80;
  max-width: 410px;
  margin: 0;
  padding: 15px 48px 15px 17px;
  border: 3px solid #111111;
  color: #111111;
  background: #ffffff;
  font-size: 0.77rem;
  box-shadow: 7px 7px 0 #d71920;
  transform: rotate(-1deg);
}

.demo-notification strong {
  color: #d71920;
  text-transform: uppercase;
}

.demo-notification button {
  position: absolute;
  top: 2px;
  right: 11px;
  border: 0;
  color: #111111;
  background: transparent;
  font-size: 1.7rem;
  font-weight: 1000;
  cursor: pointer;
}

@media (max-width: 560px) {
  .demo-notification {
    right: 14px;
    bottom: 14px;
    left: 14px;
  }
}
</style>
