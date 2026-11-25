# Diplomatic Fleet — ampliación funcional y documentación de código

**Fecha de referencia:** 23 de noviembre de 2026. **Entrega académica prevista:** 24 de noviembre de 2026.

## 1. Alcance

La ampliación parte de la primera entrega real del equipo (Vue 3 + TypeScript / Kotlin + Spring Boot / PostgreSQL + Flyway) y conserva la identidad roja, negra y blanca de la interfaz. Se elimina el complemento visual flotante de Vue DevTools de la configuración de Vite (`plugins: [vue()]`). El login original se conserva.

El diagrama del profesor **NO** corresponde 1:1 con las tablas de la primera entrega. El archivo `INVENTARIO_DIAGRAMA_PROFESOR_33_ENTIDADES.md` identifica las 33 entidades originales, los campos visibles del SVG y cuáles siguen pendientes de normalización.

## 2. Operaciones implementadas por la ampliación

| Módulo | Pantalla | API real | Persistencia | Reglas principales |
|---|---|---|---|---|
| Catálogo de vehículos | `/catalogo` | `GET /api/public/vehiculos` | `vehiculos`, `solicitudes`, `mantenimientos` | Excluye unidades inactivas, sin tarifa, no disponibles, reservadas o en mantenimiento |
| Registro / login | `/registro`, `/login` | `/api/auth/registro`, `/api/auth/me` | `usuarios`, `roles`, `usuarios_roles`, `clientes_institucionales` | BCrypt; perfiles CLIENTE y ADMINISTRADOR |
| Mis solicitudes | `/mis-reservas` | `GET /api/solicitudes/mias`, `POST /api/solicitudes/{id}/cancelar` | `solicitudes` | Restringidas al cliente institucional autenticado |
| Gestión de solicitudes | `/solicitudes` | `GET /api/admin/solicitudes`, `PATCH /api/admin/solicitudes/{id}/estado` | `solicitudes` | Transiciones administradas; sin pagos |
| Vehículos e instituciones | `/vehiculos`, `/clientes` | APIs `/api/vehiculos`, `/api/clientes` | V1–V2 | Se conservan los CRUD originales |
| Documentos | `/documentos` | `GET/POST /api/admin/documentos`, `PUT /api/admin/documentos/{id}` | `documentos_vehiculo` | Vencimiento calculado según la fecha actual |
| Mantenimiento | `/mantenimientos` | `GET/POST /api/admin/mantenimientos`, `PATCH /api/admin/mantenimientos/{id}/estado` | `mantenimientos` y `vehiculos` | PROGRAMADO → EN_PROCESO → FINALIZADO/CANCELADO |
| Disponibilidad | `/disponibilidad` | API pública por periodo | `vehiculos`, `solicitudes`, `mantenimientos` | Respeta intervalos de solicitudes y mantenimiento programado |
| Conductores | `/validacion-conductor` | `GET/POST /api/admin/conductores`, `PATCH /api/admin/conductores/{id}/activo` | `conductores_autorizados` | Rechaza licencias vencidas; vínculo institucional |
| Cotizaciones | `/cotizaciones` | `GET/POST /api/admin/cotizaciones`, `PATCH /api/admin/cotizaciones/{id}/estado` | `cotizaciones` | Versiones; estimación comercial, impuestos no asumidos |
| Usuarios | `/usuarios` | `GET /api/admin/usuarios`, `PATCH /api/admin/usuarios/{id}/activo` | `usuarios`, `roles` | No devuelve hashes; evita autodesactivar al administrador |
| Reportes | `/reportes` | `GET /api/admin/reportes` | Agregaciones SQL | Cifras reales, valor estimado ≠ dinero recaudado |

Todos los endpoints de `/api/admin/**` están protegidos por Spring Security; no basta con ocultar botones en Vue.

## 3. Arquitectura y criterios de documentación

```text
frontend/src/views/                  Pantallas Vue y formularios
frontend/src/services/               Contratos tipados TS + llamadas REST
frontend/src/router/index.ts         Reglas de navegación por rol
frontend/src/components/layout/      Menú y composición visual
src/main/kotlin/.../flota/           Entidades JPA, repositorios y CRUD
src/main/kotlin/.../reservas/        Reservas y disponibilidad por fechas
src/main/kotlin/.../operaciones/      Endpoints de mantenimiento, documentos,
                                    conductores, cotizaciones, usuarios y reportes
src/main/kotlin/.../seguridad/       Autenticación/BCrypt y registro
src/main/resources/db/migration/     SQL versionado con Flyway
```

Se documentan clases, interfaces, responsabilidades, funciones, reglas y decisiones que no son obvias mediante **KDoc** (Kotlin), **JSDoc** (TypeScript) y comentarios SQL. Los comentarios explican **por qué** se comprueban los datos, qué operaciones realizan y a qué recursos acceden, no repiten cada asignación.

## 4. Compilación y pruebas en IntelliJ IDEA

Usar JDK 21 y Node compatible con Vite 8; la base PostgreSQL debe existir y el backend debe tener las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

Terminal backend:

```powershell
.\gradlew.bat clean compileKotlin
.\gradlew.bat test
.\gradlew.bat bootRun --console=plain
```

Terminal frontend:

```powershell
cd frontend
npm ci
npm run type-check
npm run build
npm run dev
```

Abre `http://localhost:5173/login`. El panel flotante de Vue DevTools no debe aparecer. La API se ejecuta en `http://localhost:8080` y Vite redirige las llamadas `/api` al servidor Kotlin.

### Pruebas manuales de extremo a extremo

1. Ingresar como administrador, abrir `/vehiculos` y comprobar que la flota aparece desde PostgreSQL.
2. Abrir `/documentos`, registrar un SOAT con fecha futura y verificarlo en la lista. Editar la fecha y comprobar que cambia el estado.
3. Abrir `/mantenimientos`, programar un trabajo; iniciarlo y comprobar que el vehículo no aparece en el catálogo; finalizarlo y comprobar su reaparición.
4. Registrar un conductor asociado a una institución y comprobar la vigencia de licencia. Desactivarlo.
5. Abrir `/usuarios` y verificar la lista sin contraseñas. Desactivar una cuenta de prueba y comprobar que ya no permite iniciar sesión.
6. Crear institución y cuenta de cliente desde `/registro`, ingresar, seleccionar fechas y registrar una solicitud.
7. Revisar la solicitud desde `/solicitudes`, confirmar y consultar `/mis-reservas` con la cuenta de cliente.
8. Crear una cotización versionada para la solicitud; cambiar su estado; verificar los contadores en `/reportes`.
9. Comprobar autorización con una cuenta CLIENTE intentando acceder a `/api/admin/usuarios`: debe responder HTTP 403.
10. Probar dos solicitudes que se cruzan en fechas para el mismo vehículo: la segunda debe rechazarse (HTTP 409).

## 5. Restricciones y pendientes de cumplimiento docente

- **No son 33 entidades de JPA conforme al ER del profesor.** El diagrama presenta personas individuales, misiones, acreditaciones, ubicaciones, catálogos detallados y `reserva` normalizada. La implementación operativa sigue utilizando el esquema V1–V8 de la primera entrega, para no perder la base ya funcional.
- El servicio con conductor profesional, el nivel de blindaje certificado, la acreditación diplomática y la facturación fiscal/electrónica requieren reglas, relaciones y pruebas adicionales. No se presentan como implementados.
- La autenticación HTTP Basic con credenciales en `sessionStorage` funciona como **prototipo local universitario**. No publicar en Internet sin cambiar a sesiones seguras/cookies o tokens, CORS/CSRF apropiados, HTTPS, verificación de identidades y auditoría.
- Las imágenes y placas `DF-DEMOxx` son ilustrativas. Tarifas y valores estimados no prueban pagos.
- Las migraciones V1–V8 ya aplicadas **no se deben editar ni renumerar**. Para ampliar el modelo ER se necesita plan de migraciones V9+ y copia de seguridad.
- **Verificación:** esta ampliación se revisó estáticamente, pero **no fue posible ejecutar Gradle, npm y PostgreSQL juntos en el entorno de generación**. Las pruebas de esta sección deben realizarse en el equipo donde ya arrancó Spring Boot.

## 6. Referencias internas

- `docs/Documentacion_Final_Primera_Entrega_Diplomatic_Fleet.pdf`: primera entrega y alcance original.
- `docs/Manual_Tecnico_Diplomatic_Fleet.pdf`: arquitectura y configuración inicial.
- `docs/Manual_de_Usuario_Diplomatic_Fleet.pdf`: módulos de primera entrega y manual de uso anterior.
- `docs/PLAN_ALINEACION_DIAGRAMA_PROFESOR.md` e `INVENTARIO_DIAGRAMA_PROFESOR_33_ENTIDADES.md`: diferencias con el ER suministrado.

> Los PDF originales de primera entrega se conservan como historial académico, **no** se han reescrito para presentar una integración ficticia. El presente documento describe el alcance de la ampliación.
