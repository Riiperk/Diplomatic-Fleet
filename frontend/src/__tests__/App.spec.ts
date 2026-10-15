import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import App from '../App.vue'

describe('App', () => {
  /**
   * Comprueba que el componente raíz reserva correctamente el espacio
   * donde Vue Router representará la vista correspondiente a la ruta activa.
   *
   * RouterView se sustituye por un componente simulado porque esta prueba
   * evalúa App.vue de forma aislada, no el funcionamiento interno del router.
   */
  it('renderiza el contenedor de las vistas enrutadas', () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          RouterView: {
            template: '<div data-testid="router-view"></div>',
          },
        },
      },
    })

    expect(wrapper.find('[data-testid="router-view"]').exists()).toBe(true)
  })
})
