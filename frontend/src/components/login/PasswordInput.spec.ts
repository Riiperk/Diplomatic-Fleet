import {mount} from '@vue/test-utils'
import {describe,it,expect} from 'vitest'
import PasswordInput from './PasswordInput.vue'

describe('PasswordInput',()=>{
 it('muestra y oculta la misma clave sin enviar el formulario',async()=>{
   const wrapper=mount(PasswordInput,{props:{id:'prueba-clave',modelValue:'ClaveDePrueba123!'}})
   const input=wrapper.get('input');const button=wrapper.get('button')
   expect(input.attributes('type')).toBe('password')
   expect(button.attributes('type')).toBe('button')
   await button.trigger('click')
   expect(input.attributes('type')).toBe('text')
   expect(input.element.value).toBe('ClaveDePrueba123!')
   expect(button.attributes('aria-pressed')).toBe('true')
   await button.trigger('click')
   expect(input.attributes('type')).toBe('password')
   expect(input.element.value).toBe('ClaveDePrueba123!')
 })
})
