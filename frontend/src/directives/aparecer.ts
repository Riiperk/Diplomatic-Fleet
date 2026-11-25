import type { Directive } from 'vue'

/** Una única observación de visibilidad para las fichas del catálogo.
 * Solo anima elementos cuando entran en pantalla; nunca mantiene un bucle JS. */
let observador: IntersectionObserver | undefined

export const vAparecer: Directive<HTMLElement> = {
  mounted(el) {
    if (typeof IntersectionObserver === 'undefined' || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    if (!observador) {
      observador = new IntersectionObserver(entradas => {
        for (const entrada of entradas) {
          if (!entrada.isIntersecting) continue
          ;(entrada.target as HTMLElement).dataset.aparecer = 'visible'
          observador?.unobserve(entrada.target)
        }
      }, { threshold: 0.12 })
    }
    el.dataset.aparecer = 'pendiente'
    observador.observe(el)
  },
  unmounted(el) { observador?.unobserve(el) },
}
