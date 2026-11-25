# Trazabilidad de requisitos y diagrama — V10

El archivo `diagramas/Diagrama_Profesor_20261117.svg` permanece como referencia original. El inventario `INVENTARIO_DIAGRAMA_PROFESOR_33_ENTIDADES.md` documenta sus 33 entidades. Esta versión amplía el modelo operativo sin reemplazar el esquema instalado por otro incompatible. No afirma homologación idéntica de las 33 entidades.

| Caso de uso documentado | Implementación V10 | Observación |
|---|---|---|
| US01 Iniciar sesión | Spring Security, UsuarioAuth, LoginView | Prueba V9 de administrador/cliente; nuevos roles pendientes de prueba integrada. |
| US02 Usuarios y roles | Alta, consulta, activación; 4 perfiles de uso | Edición de roles existentes no implementada; solo alta por rol. |
| US03 Registrar clientes | Registro público, alta/edición institucional | Ciudad y dirección como texto, no catálogos normalizados. |
| US04 Vehículos | Registro/edición, tarifa, imagen, consulta | Baja formal y catálogo de blindaje del profesor pendientes. |
| US05 Documentos y vigencias | Alta, edición, estado según fecha | No es validación oficial ni carga de certificados. |
| US06 Mantenimiento | Programación y transiciones; conflicto de fechas | Taller almacenado como texto. |
| US07 Disponibilidad | Consulta por intervalo, bloqueos | Nueva prueba de regresión requerida. |
| US08 Solicitudes | Alta, consulta, modalidad, ciclo real | Historial previo preservado. |
| US09 Validar conductor autorizado | Registro, vigencia declarada, activación | No consulta registro oficial ni asigna conductor institucional a la reserva desde interfaz. |
| US10 Cotizaciones | Creación, versión, envío y decisión institucional | No envía correo, impuestos/recargos no configurados. |
| US11 Asignar conductor profesional | V10 incorpora tabla y asignación | Prueba integrada pendiente. |
| US12 Consultar asignaciones | V10 incorpora agenda privada | Prueba integrada pendiente. |
| US13 Tarifas, depósitos y recargos | Tarifa diaria por vehículo | Depósitos y recargos parametrizados pendientes. |
| US14 Liquidación o factura | Estimado comercial y registro de pago externo | No se implementa liquidación fiscal/facturación electrónica. |
| US15 Reportes de uso, ingresos y mantenimiento | Indicadores de estados y estimados | No sustituye contabilidad, faltan informes periódicos detallados. |
| US16 Modificar/cancelar solicitud | Cancelación previa a inicio; cierre real; reprogramación | Edición del periodo de solicitudes pendientes sin cotización ni pago reportado; contratos confirmados no se modifican. |
| US17 Historial de auditoría | V10 registra cambios técnicos desde su instalación | No atribuye todos los eventos al usuario web; no reconstruye historial previo. |

## Relación con el ER de 33 entidades

La V10 amplía la entidad conceptual `conductor` mediante `conductores_profesionales`, relacionada con `usuarios` y `solicitudes`. Las entidades `reserva`, `usuario`, `cat_perfil`, `vehiculo`, `mantenimiento` y `conductor` tienen correspondencias operativas funcionales, pero con nombres, claves y atributos distintos de la propuesta. El modelo real usa IDs BIGINT, estados CHECK y categorías persistidas. No se sustituyen estos contratos por claves VARCHAR sin una migración específicamente diseñada.

Aún faltan la normalización de país, ciudad, persona, dirección, persona_documento, persona_contacto, taller y ubicación; los catálogos separados de género, acreditación, rango, tipo de representación, nivel de blindaje, bajas y otros estados; la acreditación del cliente diplomático individual y la baja formal del vehículo. Se conservan las decisiones históricas y las relaciones existentes. Esta lista es una limitación explícita de la entrega, no una afirmación de cobertura completa.
