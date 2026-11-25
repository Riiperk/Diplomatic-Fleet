# Diagrama del profesor: correspondencia y pendientes

Documento de comparación con el archivo proporcionado `Diag_Clases_20261117.svg` (diagrama ER) y el proyecto recibido. El archivo ER original no está incorporado al ZIP por ser una entrega separada del usuario; se puede añadir a `docs/diagramas` para futuras revisiones.

## Entidades del diagrama recibido

Identificadas 33: `pais`, `ciudad`, `persona`, `persona_direccion`, `taller`, `ubicacion`, `cat_tipo_documento`, `cat_genero`, `cat_tipo_doc_persona`, `persona_documento`, `cat_tipo_cliente`, `cliente_diplomatico`, `cat_tipo_mision`, `cat_rango_diplomatico`, `cat_estado_acreditacion`, `cat_categoria_licencia`, `conductor`, `cat_tipo_vehiculo`, `vehiculo`, `cat_nivel_blindaje`, `cat_estado_admin`, `cat_estado_operativo`, `cat_tipo_mantenimiento`, `mantenimiento`, `cat_estado_mant`, `cat_tipo_baja`, `baja_vehiculo`, `cat_tipo_servicio`, `reserva`, `cat_estado_reserva`, `cat_perfil`, `usuario`, `persona_contacto`.

## Correspondencias parciales

| ER del profesor | Base actual del proyecto | Diferencia importante |
| --- | --- | --- |
| `usuario`, `cat_perfil`, `persona` | `usuarios`, `roles`, `usuarios_roles` | No existe entidad `persona` normalizada ni relación 1:1 persona/usuario |
| `cliente_diplomatico` | `clientes_institucionales` | Son instituciones, no personas diplomáticas acreditadas individualmente |
| `vehiculo`, `cat_tipo_vehiculo` | `vehiculos`, `categorias_vehiculo` | Faltan blindaje, proveedores y catálogos admin/operativo separados |
| `reserva`, `cat_estado_reserva` | `solicitudes` con columna `estado` | No están normalizados los estados; modalidad sin conductor es el MVP |
| `conductor` | `conductores_autorizados` | Estructura distinta, sin persona normalizada |
| `mantenimiento` | `mantenimientos` | Falta asociar un `taller` como entidad independiente |
| `ubicacion`, `pais`, `ciudad` | Campos de texto en solicitudes/clientes | Falta geografía normalizada |

## Orden sugerido de implementación restante

1. Acordar si el docente exige **la nomenclatura y cardinalidades exactas** del ER entregado. Si sí, planear una migración nueva o un esquema nuevo; no renombrar tablas a ciegas: puede romper V1–V6 y referencias.
2. Normalizar `persona`, documentos, contactos y ubicaciones.
3. Implementar `cliente_diplomatico`, estado de acreditación, misión y rango.
4. Extender vehículos con blindaje, estados admin/operativo y baja.
5. Modelar conductor, licencias y validación; habilitar servicio CON_CONDUCTOR.
6. Extender reservas con origen/destino como FK, tipo de servicio, blindaje y catálogo de estados.
7. Integrar mantenimiento/talleres, documento vehicular, cotizaciones, pagos/facturación, auditoría, notificaciones y reportes.
8. Agregar integración/automatización de pruebas y endurecimiento de seguridad.

**No afirmar que el MVP reproduce estrictamente el ER del profesor.** Es una extensión ejecutable orientada a flujos de usuario construida sobre la estructura preexistente.
