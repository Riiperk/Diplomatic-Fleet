# Propuesta de Pull Request — Catálogo comercial y seguimiento de contactos

**Rama sugerida:** `feature/catalogo-diplomatico`
**Título:** `feat: implementar vitrina Persona 5 y recepción de solicitudes comerciales`

## Descripción (copiar en GitHub)

Se incorpora una vitrina pública de vehículos diplomáticos, inspirada en el patrón de listado de las tiendas digitales, **manteniendo la identidad visual Persona 5 del proyecto**. Los datos se consultan desde PostgreSQL a través de un endpoint específico que no expone la placa. Se complementa el módulo administrativo existente con datos comerciales y edición de vehículos; se agrega el registro y seguimiento de solicitudes preliminares de cotización.

## Cambios principales

- `GET /api/catalogo/vehiculos` y `GET /api/catalogo/vehiculos/{id}`: listado y ficha pública de vehículos **activos**.
- `POST /api/catalogo/solicitudes`: formulario de interés con validación de datos y fechas; responde HTTP 201 con identificador.
- `GET /api/gestion/solicitudes-catalogo` y `PATCH /api/gestion/solicitudes-catalogo/{id}/estado`: bandeja y seguimiento **autenticados**.
- `GET /api/vehiculos/categorias`: categorías reales de PostgreSQL, sin depender de identificadores fijos.
- El API de vehículos ahora permite modificar tarifa indicativa en COP, foto HTTPS, descripción y clasificación de blindaje.
- Se preserva el panel `/vehiculos`, agregando edición y activación/desactivación del registro.
- Pantalla pública `/catalogo` con filtros, orden, tarjetas, ficha individual y solicitud de cotización.
- Bandeja `/solicitudes-catalogo` con estados de seguimiento.
- Migraciones Flyway `V5` y `V6`, datos demostrativos opcionales y documentación de instalación.
- Pruebas unitarias de la lógica pura de filtros y ordenamiento.

## Diseño y arquitectura

La página pública se inspira en la distribución visual de un catálogo de comercio electrónico (filtros laterales, tarjetas y detalle). La paleta, diagonales, sombras y tipografía en negrita siguen la estética existente. La lógica se distribuye en controlador, servicio, DTO, repositorio y migración. Las páginas usan `fetch` con tipos TypeScript y Vue Router.

## Criterios de aceptación

1. Un visitante abre `/catalogo` sin iniciar sesión y consulta únicamente vehículos activos.
2. El catálogo filtra por texto, categoría, combustible, blindaje y disponibilidad; ordena por precio y año.
3. El detalle presenta características e indica claramente cuando el precio requiere cotización.
4. Un vehículo no disponible no permite enviar una solicitud desde la interfaz o la API.
5. El envío válido guarda la solicitud y regresa un número de referencia, sin simular pago o confirmación.
6. Un usuario autenticado visualiza y cambia el estado de solicitudes recibidas.
7. La gestión de vehículos permite editar atributos comerciales y activar/desactivar la publicación.
8. No se muestra la placa del vehículo ni datos de contactos en el endpoint público de consulta.

## Pasos para probar

Ver `docs/entrega-catalogo.md`, apartados **Ejecución**, **Pruebas manuales** y **API**.

## Base de datos

- `V5__ampliar_catalogo_vehiculos.sql`: columnas opcionales en `vehiculos`; mantiene registros existentes.
- `V6__solicitudes_catalogo.sql`: tabla independiente para contactos preliminares. **No** cambia ni confirma automáticamente el flujo de `solicitudes` institucionales existente en V3.
- `docs/flota-demo-opcional.sql`: datos ficticios de prueba para una base local; no forman parte de Flyway automático.

## Pruebas y limitaciones

Las pruebas de frontend están en `frontend/src/__tests__/CatalogoFilters.spec.ts`. Deben ejecutarse junto con `npm run type-check` y `npm run build`. Backend: `bash ./gradlew test` en Linux/macOS o `gradlew.bat test` en Windows. **En este entorno no fue posible instalar dependencias Gradle/npm por falta de acceso a los repositorios de paquetes, por lo que no se puede afirmar que estas pruebas se hayan ejecutado aquí.**

No se ha implementado todavía confirmación de reserva, motor de conflictos de fechas, cobros, liquidación, roles persistidos ni control normativo de vehículos blindados. Esta entrega habilita catálogo y pre-solicitudes, no un sistema de reservas de producción.

## Evidencia solicitada para el PR

Agregar 3 capturas reales: catálogo con datos ficticios, ficha/solicitud guardada y bandeja administrativa; adjuntar captura de tests y migraciones aplicadas. No adjuntar datos privados reales.
