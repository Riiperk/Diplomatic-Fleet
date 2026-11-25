# Pruebas manuales de Diplomatic Fleet

Ejecutar después de instalar Node, Gradle y PostgreSQL. Usar entorno de pruebas; no datos institucionales reales.

## Base de datos e inicio

- [ ] Arranca Spring Boot, Flyway reporta migraciones V1–V6 aplicadas sin errores.
- [ ] `GET /api/public/vehiculos` devuelve los 4 vehículos ficticios o los activos si ya hay más registros.
- [ ] Abrir `/catalogo` sin login muestra vehículos, ilustraciones y COP/día.
- [ ] Cambiar fechas devuelve disponibilidad consistente.

## Cuentas y permisos

- [ ] Registrar nueva institución con email único y contraseña de mínimo 8 caracteres.
- [ ] Ingresar como cliente y comprobar `/mis-reservas`.
- [ ] Ingresar con ADMIN_EMAIL/ADMIN_PASSWORD creados en primer arranque.
- [ ] Probar contraseña incorrecta (401).
- [ ] Cliente llama `GET /api/admin/solicitudes` y recibe 403.
- [ ] Cliente llama `POST /api/vehiculos` y recibe 403.
- [ ] Sin login `POST /api/solicitudes` recibe 401.

## Solicitudes

- [ ] Cliente A solicita un coche para un periodo futuro, aparece en sus reservas y panel admin.
- [ ] Cliente B intenta reservar el **mismo vehículo y fechas solapadas**: mensaje 409 / coche ausente de resultados de búsqueda.
- [ ] Otro coche en mismas fechas sí se puede solicitar.
- [ ] Admin revisa y confirma la reserva, cliente ve cambio de estado.
- [ ] Cancelar reserva elegible antes del inicio, fecha se libera para otro cliente.
- [ ] Admin rechaza una solicitud en revisión, fecha se libera.
- [ ] Admin no permite transición desde RECHAZADA a CONFIRMADA.
- [ ] Filtro de fechas inválidas o aforo excedido produce error sin guardar solicitud.

## Gestión

- [ ] Admin registra vehículo con placa única, precio diario e imagen.
- [ ] Admin edita tarifa e imagen, ve reflejados los datos en `/catalogo`.
- [ ] Admin agrega institución desde `/clientes` y aparece en panel.
- [ ] Verificar que los botones de módulos en desarrollo no prometen que ya guardan datos.

## Comandos para frontend

```powershell
cd frontend
npm ci
npm run type-check
npm run build
npm run test:unit -- --run
```

La prueba en navegador y contra PostgreSQL es necesaria para validar la integración. El paquete no incluye una base de datos ya instalada ni credenciales reales.
