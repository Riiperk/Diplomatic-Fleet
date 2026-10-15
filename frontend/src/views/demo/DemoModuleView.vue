<script setup lang="ts">
import { computed } from 'vue'
import DemoLayout from '@/components/layout/DemoLayout.vue'

/**
 * Registro demostrativo mostrado dentro de un módulo.
 */
export interface DemoRecord {
  primary: string
  secondary: string
  detail: string
  status: string
  statusType?: 'success' | 'warning' | 'danger' | 'neutral'
}

/**
 * Propiedades utilizadas para configurar el módulo.
 */
interface DemoModuleProps {
  moduleNumber: string
  eyebrow: string
  title: string
  description: string
  sectionTitle: string
  sectionDescription: string
  actionLabel?: string
  records?: DemoRecord[]
  emptyMessage?: string
}

const props = withDefaults(
  defineProps<DemoModuleProps>(),
  {
    actionLabel: '',
    records: () => [],
    emptyMessage: 'No existen registros disponibles.',
  },
)

const emit = defineEmits<{
  /**
   * Solicita la acción principal del módulo.
   */
  action: []
}>()

/**
 * Indica si existen registros para representar.
 */
const hasRecords = computed(
  (): boolean => props.records.length > 0,
)
</script>

<template>
  <DemoLayout
    :module-number="moduleNumber"
    :eyebrow="eyebrow"
    :title="title"
    :description="description"
  >
    <section class="module-panel">
      <div
        class="panel-accent"
        aria-hidden="true"
      ></div>

      <header class="panel-header">
        <div class="panel-heading">
          <span class="panel-label">
            Gestión operativa
          </span>

          <h2>{{ sectionTitle }}</h2>

          <p>{{ sectionDescription }}</p>
        </div>

        <button
          v-if="actionLabel"
          class="action-button"
          type="button"
          @click="emit('action')"
        >
          <span>{{ actionLabel }}</span>
          <strong aria-hidden="true">➜</strong>
        </button>
      </header>

      <div
        v-if="hasRecords"
        class="records-grid"
      >
        <article
          v-for="(record, index) in records"
          :key="`${record.primary}-${index}`"
          class="record-card"
        >
          <span class="record-number">
            {{ String(index + 1).padStart(2, '0') }}
          </span>

          <div class="record-main">
            <small>{{ record.secondary }}</small>
            <strong>{{ record.primary }}</strong>
            <p>{{ record.detail }}</p>
          </div>

          <span
            class="status-badge"
            :class="record.statusType ?? 'neutral'"
          >
            {{ record.status }}
          </span>
        </article>
      </div>

      <div
        v-else
        class="empty-state"
      >
        <span aria-hidden="true">0</span>

        <div>
          <strong>Sin registros</strong>
          <p>{{ emptyMessage }}</p>
        </div>
      </div>

      <aside class="integration-note">
        <span aria-hidden="true">i</span>

        <div>
          <strong>Módulo navegable</strong>

          <p>
            Esta sección forma parte de la demo académica.
            La integración con la API se realizará de manera
            incremental.
          </p>
        </div>
      </aside>
    </section>
  </DemoLayout>
</template>

<style scoped>
.module-panel {
  position: relative;
  padding: 28px;
  overflow: hidden;
  color: #111111;
  background: #ffffff;
  box-shadow: 12px 12px 0 rgb(17 17 17 / 14%);
  clip-path: polygon(
    1% 0,
    99% 1%,
    100% 97%,
    2% 100%,
    0 4%
  );
}

.panel-accent {
  position: absolute;
  top: 0;
  right: -55px;
  width: 290px;
  height: 14px;
  background: #d71920;
  transform: rotate(2deg);
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
}

.panel-label {
  display: inline-block;
  padding: 4px 9px;
  color: #ffffff;
  background: #d71920;
  font-size: 0.62rem;
  font-weight: 1000;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  transform: rotate(-1deg);
}

.panel-heading h2 {
  margin: 9px 0 0;
  font-size: clamp(1.8rem, 3vw, 2.8rem);
  font-weight: 1000;
  letter-spacing: -0.06em;
  text-transform: uppercase;
}

.panel-heading p {
  max-width: 700px;
  margin: 8px 0 0;
  color: #617080;
  font-size: 0.86rem;
}

.action-button {
  display: flex;
  min-width: 180px;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 13px 17px;
  border: 0;
  color: #ffffff;
  background: #d71920;
  font-size: 0.72rem;
  font-weight: 1000;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  cursor: pointer;
  clip-path: polygon(
    3% 0,
    100% 8%,
    96% 100%,
    0 90%
  );
}

.action-button:hover {
  background: #111111;
  transform: translateX(5px);
}

.records-grid {
  display: grid;
  gap: 11px;
  margin-top: 28px;
}

.record-card {
  position: relative;
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  align-items: center;
  gap: 15px;
  padding: 15px 17px 15px 9px;
  border-bottom: 2px solid #111111;
  overflow: hidden;
  background: #f1eee7;
  transition:
    color 150ms ease,
    background 150ms ease,
    transform 150ms ease;
}

.record-card::before {
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
  content: "";
  background: #d71920;
}

.record-card:hover {
  color: #ffffff;
  background: #111111;
  transform: translateX(7px);
}

.record-number {
  display: grid;
  width: 39px;
  height: 39px;
  place-items: center;
  color: #ffffff;
  background: #d71920;
  font-size: 0.7rem;
  font-weight: 1000;
  clip-path: polygon(
    9% 0,
    100% 9%,
    90% 100%,
    0 88%
  );
}

.record-main small {
  color: #7b8792;
  font-size: 0.57rem;
  font-weight: 950;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.record-main strong {
  display: block;
  margin-top: 3px;
  font-size: 1rem;
  font-weight: 1000;
}

.record-main p {
  margin: 4px 0 0;
  color: #617080;
  font-size: 0.72rem;
}

.record-card:hover .record-main small,
.record-card:hover .record-main p {
  color: rgb(255 255 255 / 62%);
}

.status-badge {
  padding: 7px 10px;
  font-size: 0.6rem;
  font-weight: 1000;
  text-transform: uppercase;
  clip-path: polygon(
    4% 0,
    100% 7%,
    95% 100%,
    0 90%
  );
}

.status-badge.success {
  color: #116335;
  background: #dff4e7;
}

.status-badge.warning {
  color: #7c5100;
  background: #fff0bd;
}

.status-badge.danger {
  color: #9a080d;
  background: #ffdadd;
}

.status-badge.neutral {
  color: #ffffff;
  background: #52606d;
}

.record-card:hover .status-badge {
  color: #111111;
  background: #ffffff;
}

.empty-state {
  display: flex;
  min-height: 260px;
  align-items: center;
  justify-content: center;
  gap: 18px;
  margin-top: 24px;
}

.empty-state > span {
  display: grid;
  width: 74px;
  height: 74px;
  place-items: center;
  color: #ffffff;
  background: #d71920;
  font-size: 2.4rem;
  font-weight: 1000;
  transform: rotate(-4deg);
}

.empty-state strong {
  font-size: 1.2rem;
  font-weight: 1000;
  text-transform: uppercase;
}

.empty-state p {
  margin: 5px 0 0;
  color: #617080;
  font-size: 0.78rem;
}

.integration-note {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 28px;
  padding: 15px;
  color: #ffffff;
  background: #111111;
  transform: skew(-1deg);
}

.integration-note > * {
  transform: skew(1deg);
}

.integration-note > span {
  display: grid;
  width: 29px;
  height: 29px;
  flex: 0 0 29px;
  place-items: center;
  color: #111111;
  background: #c9a34f;
  font-weight: 1000;
}

.integration-note strong {
  font-size: 0.72rem;
  text-transform: uppercase;
}

.integration-note p {
  margin: 4px 0 0;
  color: rgb(255 255 255 / 60%);
  font-size: 0.66rem;
  line-height: 1.5;
}

@media (max-width: 700px) {
  .module-panel {
    padding: 23px 17px;
  }

  .panel-header {
    flex-direction: column;
  }

  .action-button {
    width: 100%;
  }

  .record-card {
    grid-template-columns: 43px 1fr;
  }

  .status-badge {
    grid-column: 2;
    justify-self: start;
  }
}
</style>
