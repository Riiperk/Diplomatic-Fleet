# Entrega funcional: catálogo diplomático y recepción comercial

## 1. Objetivo

Permitir que el profesor explore un **catálogo de vehículos** organizado como una tienda virtual, manteniendo el tema visual Persona 5 del proyecto. El visitante debe poder filtrar, conocer las características de una unidad y registrar una **solicitud de contacto/cotización**. El operador autenticado debe editar la ficha comercial y gestionar el seguimiento de contactos.

> **Alcance real:** ninguna solicitud desde el catálogo es una reserva confirmada. No se asegura exclusividad de fechas ni se ejecutan cobros. El proceso definitivo sigue pendiente de las reglas del módulo institucional de V3.

## 2. Arquitectura

| Capa | Ubicación | Responsabilidad |
|---|---|---|
| Vista pública | `frontend/src/views/CatalogoView.vue` | Catálogo, filtros, ficha y formulario |
| Administración de contactos | `frontend/src/views/SolicitudesCatalogoView.vue` | Listado, búsqueda, cambio de estado |
| Administración de flota | `frontend/src/views/VehiculosView.vue` | Alta, edición, publicación y ficha comercial |
| Servicios frontend | `frontend/src/services/` | `fetch`, tipos y errores de la API |
| Controladores Kotlin | `.../catalogo/controller/` | Endpoints públicos y privados |
| Servicio Kotlin | `.../catalogo/service/` | Reglas de visibilidad, fechas y seguimiento |
| Repositorio JPA | `.../catalogo/repository/` | Persistencia de contactos |
| Migraciones | `src/main/resources/db/migration/V5*` y `V6*` | Modificaciones SQL versionadas |
| Pruebas de filtros | `frontend/src/__tests__/CatalogoFilters.spec.ts` | Búsqueda, filtros, orden y no mutabilidad |

**Flujo de consulta:** `CatalogoView → catalogoService.ts → GET /api/catalogo/vehiculos → CatalogoController → CatalogoVehiculoService → VehiculoRepository → PostgreSQL`.

**Flujo de contacto:** `CatalogoView → POST /api/catalogo/solicitudes → SolicitudCatalogoController → SolicitudCatalogoService → SolicitudCatalogoRepository → solicitudes_catalogo`. La tabla es independiente de `solicitudes` para evitar pretender que un contacto público cumple los requisitos formales institucionales.

## 3. Instalación en IntelliJ IDEA y PostgreSQL

1. Instalar **JDK 21**, **PostgreSQL** y **Node.js compatible con `frontend/package.json`**. El proyecto original usa Gradle Wrapper y Spring Boot 4; no cambiar versiones sin validar.
2. Crear la base de datos local, por ejemplo desde pgAdmin o `psql`: `CREATE DATABASE diplomatic_fleet;`.
3. Abrir **la carpeta raíz del proyecto** en IntelliJ IDEA como proyecto Gradle (no abrir solo `frontend`). Dejar sincronizar las dependencias.
4. Configurar en **Run → Edit Configurations → DiplomaticFleetApplication → Environment variables**: `DB_URL=jdbc:postgresql://localhost:5432/diplomatic_fleet`, `DB_USERNAME=postgres`, `DB_PASSWORD=TU_CLAVE_LOCAL`. El archivo `.env.example` es ilustrativo: Spring Boot **no carga automáticamente** un `.env` desde IntelliJ.
5. Para la demo con HTTP Basic, definir opcionalmente `SPRING_SECURITY_USER_NAME=user` y `SPRING_SECURITY_USER_PASSWORD=CONTRASENA_TEMPORAL_SEGURA` como variables locales (no se deben subir a GitHub). El login actual del proyecto utiliza el correo demostrativo `admin@diplomaticfleet.co` y comprueba esa contraseña contra Spring Security. **No es todavía autenticación persistida de usuarios y roles.**
6. Ejecutar `DiplomaticFleetApplication.kt`. Flyway ejecutará automáticamente las migraciones nuevas V5 y V6 sobre la base configurada. No editar V1–V4 ya aplicadas.
7. Abrir otra terminal en el proyecto, ejecutar:

   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

8. Abrir la dirección indicada por Vite (normalmente `http://localhost:5173/catalogo`). El proxy existente redirige `/api` al backend en `http://localhost:8080`.
9. Opcional para una demostración visual: ejecutar **manualmente** el archivo `docs/flota-demo-opcional.sql` en la base *de pruebas*, después de Flyway. Inserta seis unidades con nombres/placas/tarifas **ficticias**; la foto genérica del repositorio se usará como alternativa cuando no haya URL HTTPS. Para fotografías reales, editar la ficha desde `/vehiculos`, usando imágenes de uso autorizado.

### Windows PowerShell — variables de entorno temporales

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/diplomatic_fleet'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD='TU_CLAVE_LOCAL'
$env:SPRING_SECURITY_USER_NAME='user'
$env:SPRING_SECURITY_USER_PASSWORD='TU_CLAVE_DEMO'
.\gradlew.bat bootRun
```

En otra terminal: `cd frontend; npm ci; npm run dev`.

## 4. Rutas visibles

| Ruta | Acceso | Propósito |
|---|---|---|
| `/catalogo` | Público | Vitrina con filtros y detalle |
| `/login` | Público | Login demostrativo existente |
| `/vehiculos` | Desde panel (API autenticada) | Alta/edición de flota |
| `/solicitudes-catalogo` | Desde panel (API autenticada) | Seguimiento comercial |

> Las rutas Vue del panel aún no implementan un guardado de navegación basado en roles; la protección de **datos** está en los endpoints Spring. No usar en producción como un control de seguridad completo.

## 5. API HTTP (contratos)

| Método | Ruta | Autenticación | Respuesta |
|---|---|---|---|
| `GET` | `/api/catalogo/vehiculos` | No | 200 + array de fichas activas sin placa |
| `GET` | `/api/catalogo/vehiculos/{id}` | No | 200 + ficha o 404 |
| `POST` | `/api/catalogo/solicitudes` | No | 201 + ID y estado `PENDIENTE` |
| `GET` | `/api/gestion/solicitudes-catalogo` | Sí | 200 + contactos privados |
| `PATCH` | `/api/gestion/solicitudes-catalogo/{id}/estado` | Sí | 200 + solicitud actualizada |
| `GET` | `/api/vehiculos` | Sí | Lista administrativa existente |
| `GET` | `/api/vehiculos/categorias` | Sí | Categorías desde PostgreSQL |
| `POST` | `/api/vehiculos` | Sí | 201 + vehículo nuevo |
| `PUT` | `/api/vehiculos/{id}` | Sí | 200 + vehículo actualizado |

### Ejemplos con curl

```bash
# Catálogo público
curl http://localhost:8080/api/catalogo/vehiculos

# Enviar un contacto de prueba (utilizar fechas FUTURAS)
curl -X POST http://localhost:8080/api/catalogo/solicitudes \
  -H 'Content-Type: application/json' \
  -d '{"vehiculoId":1,"nombreContacto":"Contacto Demo","correoContacto":"demo@example.org","entidad":"Misión de Prueba","telefono":"3000000000","fechaInicio":"2027-01-20","fechaFin":"2027-01-22","mensaje":"Requerimos transporte para una agenda de trabajo","aceptaContacto":true}'

# Listar contactos (sustituir credenciales locales, jamás en el repositorio)
curl -u 'user:TU_CLAVE_DEMO' http://localhost:8080/api/gestion/solicitudes-catalogo

# Actualizar seguimiento de una solicitud existente
curl -u 'user:TU_CLAVE_DEMO' -X PATCH \
  http://localhost:8080/api/gestion/solicitudes-catalogo/1/estado \
  -H 'Content-Type: application/json' -d '{"estado":"CONTACTADA"}'
```

La fecha del ejemplo de solicitud debe reemplazarse por una fecha igual o posterior al día de prueba. `vehiculoId` debe ser el ID real de una unidad **activa y disponible**.

## 6. Pruebas manuales para la sustentación

1. **Sin vehículos:** abrir `/catalogo` sin datos activos y comprobar el estado vacío (no inventa unidades).
2. **Con datos:** cargar las seis unidades ficticias mediante el script opcional o dar de alta desde `/vehiculos`. Abrir el catálogo y comprobar tarjetas e imagen local alternativa.
3. **Filtros:** buscar `Prado`, filtrar `PREMIUM`, alternar disponibilidad y ordenar por tarifa.
4. **Detalle:** abrir una tarjeta; comprobar categoría, año, pasajeros, combustible y tarifa o indicador de cotización.
5. **Solicitud:** elegir unidad disponible, fechas futuras, nombre/correo/entidad, aceptar contacto y enviar. Aparecerá número de solicitud y confirmación **de recepción**, no de reserva.
6. **No disponible:** abrir una unidad reservada o en mantenimiento y comprobar que no permite enviar solicitudes. El backend debe bloquear igualmente un POST directo.
7. **Seguimiento:** iniciar sesión como personal de demo, abrir `/solicitudes-catalogo`, ubicar el contacto y pasar de `PENDIENTE` a `CONTACTADA`.
8. **Edición:** desde `/vehiculos`, editar tarifa, foto, descripción y activar/desactivar. Volver al catálogo, comprobar actualización y ocultamiento de inactivos.
9. **Protección:** `GET /api/gestion/solicitudes-catalogo` sin autorización debe responder con 401; el GET público nunca presenta placas ni correos de solicitantes.

### Comandos de pruebas automáticas

```bash
# Windows (desde la raíz)
gradlew.bat test

# Linux / macOS (desde la raíz)
bash ./gradlew test

# Frontend
cd frontend
npm ci
npm run type-check
npm run build
npm run test:unit -- --run
```

**Estado de verificación:** los cambios se prepararon sin posibilidad de descargar dependencias desde este entorno de ejecución. No se ejecutaron aquí la compilación completa de Kotlin, el build de Vue ni las pruebas Vitest. Los comandos anteriores son parte de la validación obligatoria antes del PR.

## 7. Observaciones de seguridad y límites

- La protección de endpoints administrativos se basa en el **HTTP Basic temporal ya existente**, no en usuarios/roles de PostgreSQL. El correo de inicio de sesión está fijo para la demo. Pendiente: autenticación real, autorización por rol y sesiones/tokens de acceso. No publicar el sistema así en Internet.
- `solicitudes_catalogo` almacena nombres, correos, entidad, teléfono opcional y fecha de consentimiento. Los datos son privados; solo la administración autenticada debe poder leerlos. Antes de operar realmente: aviso de privacidad, retención y eliminación de datos, controles antispam y limitación de solicitudes.
- `blindado=true` es una clasificación comercial y **no** comprueba resoluciones, certificaciones, permisos o requisitos legales colombianos. Dichas verificaciones deben implementarse antes de ofrecer servicios reales.
- Una unidad marcada `DISPONIBLE` no garantiza disponibilidad en un intervalo de fechas. Faltan validación de solapamientos con `solicitudes`, asignación, conductores, pagos y facturación. No usar la pre-solicitud como reserva confirmada.
- El campo `tarifaDiaria` es meramente indicativo. No calcula impuestos, conductor, protección, peajes o recargos; si es `NULL`, la ficha muestra "A cotizar".
- Las URL de imágenes externas se admiten **solo por HTTPS**. La aplicación utiliza la imagen de sedán negro que ya estaba en el repositorio como alternativa. Se deben respetar los derechos de uso de fotos.
- Un cambio de `activo` oculta la unidad de la vitrina pública pero no modifica automáticamente su estado operativo histórico; este último seguirá siendo responsabilidad del módulo institucional.

## 8. Referentes funcionales (sin copiar su identidad visual)

Como referencia de campos comerciales y servicios diplomáticos: la flota de <https://diplomaticsolutionsgroup.com/en/home> muestra SUVs y sedanes para misiones; <https://avantcolombia.com/alquiler/> utiliza tarjetas por categoría/capacidad; y <https://velice.co/es/servicios/vehiculos-blindados> distingue pasajeros/equipaje y oferta blindada. **No se copiaron imágenes, logotipos ni CSS de esos sitios.**

## 9. Trabajo posterior (no incluido)

Integrar `solicitudes_catalogo` al flujo institucional formal, roles por usuario, gestión documental de blindados, control de disponibilidad por periodos, contratos, pagos, seguimiento de mantenimientos, notificaciones y auditoría. Las vistas de documentos, mantenimientos y solicitudes institucionales previas siguen siendo módulos de demostración del repositorio original.
