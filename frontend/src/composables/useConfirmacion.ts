import { ref, onBeforeUnmount } from 'vue'

/** Configuración de una pregunta modal; los valores se devuelven a la vista sin ejecutar HTTP. */
export interface PreguntaOperacion {
  titulo: string
  mensaje: string
  campo?: string
  minimo?: number
  maximo?: number
  inicial?: string
  opciones?: { valor: string; etiqueta: string }[]
}

/**
 * Controla una sola pregunta a la vez. La promesa se resuelve con el texto
 * confirmado o null al cancelar. La vista conserva la responsabilidad de llamar
 * al backend, que vuelve a validar los datos y permisos.
 */
export function useConfirmacion() {
  const pregunta = ref<PreguntaOperacion | null>(null)
  let resolver: ((valor: string | null) => void) | undefined

  /** Cierra la pregunta y resuelve una sola vez la operación pendiente. */
  function responder(valor: string | null): void {
    const anterior = resolver
    resolver = undefined
    pregunta.value = null
    anterior?.(valor)
  }

  /** Presenta una confirmación o entrada de datos sin bloquear el navegador. */
  function solicitar(datos: PreguntaOperacion): Promise<string | null> {
    responder(null)
    pregunta.value = datos
    return new Promise(resolve => { resolver = resolve })
  }
  onBeforeUnmount(() => responder(null))
  return { pregunta, solicitar, responder }
}
