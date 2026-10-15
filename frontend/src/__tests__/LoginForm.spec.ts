import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import LoginForm from '@/components/login/LoginForm.vue'

describe('LoginForm', () => {
  /**
   * Comprueba que el formulario rechaza un envío sin datos
   * y presenta mensajes comprensibles para cada campo obligatorio.
   */
  it('muestra errores cuando se envía vacío', async () => {
    const wrapper = mount(LoginForm)

    await wrapper.find('form').trigger('submit')

    expect(wrapper.get('#email-error').text()).toBe('Ingresa tu correo electrónico.')

    expect(wrapper.get('#password-error').text()).toBe('Ingresa tu contraseña.')

    expect(wrapper.get('#legal-error').text()).toBe(
      'Debes aceptar los términos y la política de privacidad.',
    )

    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  /**
   * Comprueba que una dirección con formato incorrecto
   * no se entrega al componente padre.
   */
  it('rechaza un correo con formato inválido', async () => {
    const wrapper = mount(LoginForm)

    await wrapper.get('#email').setValue('correo-invalido')
    await wrapper.get('#password').setValue('Demo1234')
    await wrapper.get('#accept-legal').setValue(true)
    await wrapper.find('form').trigger('submit')

    expect(wrapper.get('#email-error').text()).toBe(
      'El correo electrónico no tiene un formato válido.',
    )

    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  /**
   * Comprueba que las credenciales válidas se normalizan
   * y se emiten sin almacenarse dentro del componente.
   */
  it('emite las credenciales cuando el formulario es válido', async () => {
    const wrapper = mount(LoginForm)

    await wrapper.get('#email').setValue('  ADMIN@DIPLOMATICFLEET.CO  ')

    await wrapper.get('#password').setValue('Demo1234')
    await wrapper.get('#accept-legal').setValue(true)
    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toEqual([
      [
        {
          email: 'admin@diplomaticfleet.co',
          password: 'Demo1234',
        },
      ],
    ])
  })

  /**
   * Comprueba que el usuario puede alternar la visibilidad
   * de la contraseña sin modificar su contenido.
   */
  it('permite mostrar y ocultar la contraseña', async () => {
    const wrapper = mount(LoginForm)

    const passwordInput = wrapper.get('#password')
    const toggleButton = wrapper.get('.password-toggle')

    expect(passwordInput.attributes('type')).toBe('password')

    await toggleButton.trigger('click')

    expect(passwordInput.attributes('type')).toBe('text')
    expect(toggleButton.attributes('aria-pressed')).toBe('true')

    await toggleButton.trigger('click')

    expect(passwordInput.attributes('type')).toBe('password')
    expect(toggleButton.attributes('aria-pressed')).toBe('false')
  })

  /**
   * Comprueba que los enlaces legales notifican al componente padre
   * qué documento debe presentarse.
   */
  it('emite la solicitud de apertura de los documentos legales', async () => {
    const wrapper = mount(LoginForm)
    const legalButtons = wrapper.findAll('.legal-link')

    await legalButtons[0]!.trigger('click')
    await legalButtons[1]!.trigger('click')

    expect(wrapper.emitted('openLegal')).toEqual([['terms'], ['privacy']])
  })
})
