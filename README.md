# Actualización V10 — Diplomatic Fleet

**Guía actual:** [Instalación segura, cuentas y uso](docs/ENTREGA_V10.md). Consulte [documentación técnica](docs/DOCUMENTACION_CODIGO_V10.md), [trazabilidad](docs/TRAZABILIDAD_V10.md) y [validación](docs/VALIDACION_V10.md). El contenido original de este README se conserva a continuación como referencia histórica.

---

# Diplomatic Fleet — MVP de alquiler institucional

Proyecto universitario **Kotlin 2.3 + Spring Boot 4 + PostgreSQL + Vue 3**. El paquete parte del repositorio recibido y añade catálogo, usuarios, reservas y panel operativo. Se conserva el trabajo anterior y su documentación.

> **Estado:** implementación de MVP. Aún NO es una entrega certificada ni replica completamente el diagrama del profesor. Para usarlo hay que instalar dependencias, ejecutar Flyway y verificar los flujos en un equipo con PostgreSQL.

## 1. Requisitos

- JDK **21** configurado en IntelliJ IDEA.
- PostgreSQL con una base de datos vacía `diplomatic_fleet` o la misma base del proyecto con migraciones V1–V4 ya aplicadas.
- Node.js **24.15 o posterior** (algunas dependencias del frontend exigen más que 22.18).
- IntelliJ IDEA, terminal PowerShell, acceso a Maven Central y npm para obtener dependencias.

## 2. Base de datos

En pgAdmin o psql crea solo la base de datos, **no** las tablas:

```sql
CREATE DATABASE diplomatic_fleet;
```

Las migraciones `src/main/resources/db/migration/V1`…`V6` se aplican al iniciar Spring Boot (Flyway). **No edites V1–V4 si ya ejecutaste esas migraciones**: Flyway comprueba sus checksums.

En IntelliJ IDEA, configura las variables de entorno de la configuración de ejecución `DiplomaticFleetApplication` (Run > Edit Configurations > Environment variables), personalizando los valores:

```text
DB_URL=jdbc:postgresql://localhost:5432/diplomatic_fleet
DB_USERNAME=postgres
DB_PASSWORD=TU_CONTRASENA_LOCAL
ADMIN_EMAIL=admin@diplomaticfleet.co
ADMIN_PASSWORD=DEFINE_UNA_CONTRASENA_SEGURA
```

`ADMIN_PASSWORD` se usa **solo para crear el administrador si ese correo todavía no existe en la tabla `usuarios`**. No restablece cuentas existentes ni sustituye el hash guardado. La contraseña se cifra con BCrypt. No se entrega una contraseña fija dentro del repositorio.

> `.env.example` es una plantilla, no se importa automáticamente. La configuración de ejecución o las variables de entorno del sistema son necesarias. No subas contraseñas a GitHub.

## 3. Ejecutar Kotlin (backend)

Abre **la carpeta raíz** `Diplomatic-Fleet-main` como proyecto Gradle en IntelliJ, espera a que se resuelvan las dependencias y ejecuta `DiplomaticFleetApplication.kt` o:

```powershell
.\gradlew.bat bootRun
```

Servidor: **http://localhost:8080**. Comprueba `GET http://localhost:8080/api/public/vehiculos`. Debe devolver JSON con 4 vehículos de muestra, si Flyway aplicó V6 correctamente.

## 4. Ejecutar Vue (frontend)

En otra terminal, dentro de la raíz del proyecto:

```powershell
cd frontend
npm ci
npm run dev
```

Abre la URL que muestre Vite, normalmente **http://localhost:5173**. El proxy de Vite envía las solicitudes `/api` al backend en `localhost:8080`.

Verificación recomendada:

```powershell
npm run type-check
npm run build
npm run test:unit -- --run
```

## 5. Rutas y roles

| Ruta | Acceso | Acción principal |
| --- | --- | --- |
| `/catalogo` | Público | Explorar unidades, disponibilidad por fechas y tarifas |
| `/registro` | Público | Crear una cuenta vinculada a una institución nueva |
| `/login` | Público | Inicio de sesión real con usuarios PostgreSQL |
| `/mis-reservas` | Cliente autenticado | Consultar y cancelar solicitudes elegibles |
| `/inicio` | Administrador | Tablero con datos reales de vehículos, instituciones y reservas |
| `/vehiculos` | Administrador | Registrar, listar y editar flota, tarifa y URL de imagen |
| `/clientes` | Administrador | Gestión institucional disponible en el proyecto original |
| `/solicitudes` | Administrador | Revisar, confirmar, rechazar y finalizar solicitudes |

Los demás módulos heredados (documentos, mantenimiento, validación de conductor, cotizaciones y usuarios/roles) siguen siendo pantallas de demostración sin operaciones reales, a la espera de su implementación.

## 6. Flujo de alquiler implementado

1. Un visitante revisa el catálogo y elige fecha inicial y final.
2. Crea una cuenta institucional en `/registro` (requiere una institución no registrada).
3. Inicia sesión; accede a `/catalogo` y solicita un vehículo.
4. El backend comprueba la capacidad, las fechas y que no haya otra reserva activa solapada para el vehículo; utiliza bloqueo de fila al crear reservas concurrentes.
5. Guarda una `solicitud` en PostgreSQL con estado **RECIBIDA** y valor **estimado**, sin procesar pagos.
6. El administrador puede pasarla a **EN_REVISION**, **CONFIRMADA** o **RECHAZADA**; una confirmada puede pasar a **FINALIZADA**.
7. El cliente puede consultar y cancelar su solicitud antes del inicio cuando el estado lo permita. Una cancelación o rechazo libera el periodo reservado.

En esta etapa se soporta solo **SIN_CONDUCTOR** y **no** se cobra en línea ni se verifican las acreditaciones. La aprobación administrativa es un flujo académico, no una validación diplomática real.

## 7. Alcance e información importante

- V5 añade roles `CLIENTE`, vínculo institucional para usuario, precio/imagen por vehículo y valor estimado de solicitud. V6 inserta **cuatro vehículos ficticios** con placas `DF-DEMO01...04`. No es un inventario real. Una imagen genérica propia del proyecto ilustra todos los modelos para evitar imágenes inestables.
- La lógica de disponibilidad trabaja por **rango temporal**, no marca globalmente `estado = RESERVADO` porque bloquearía otras fechas.
- Se respetan las reglas de seguridad en la **API**, no solo ocultando botones en Vue. HTTP Basic con credenciales temporales en `sessionStorage` es adecuado solo para prototipo académico local; para publicar se necesita HTTPS, cookies HttpOnly o un flujo de tokens apropiado, protección anti-abuso, verificación de correo e instituciones, auditoría y pruebas de seguridad.
- Este repositorio contiene un esquema original anterior que **no coincide 1:1** con las entidades y nombres del diagrama del profesor. Véase `docs/PLAN_ALINEACION_DIAGRAMA_PROFESOR.md`.
- Antes de la entrega, corre las pruebas indicadas en `docs/GUIA_PRUEBAS_MVP.md` sobre tu instalación; no se garantiza el arranque sin realizar esta verificación.

## V13 — modelo diplomático y despliegue académico

- Instalación: [docs/ACTUALIZACION_V13.md](docs/ACTUALIZACION_V13.md)
- Matriz de 33 entidades: [docs/TRAZABILIDAD_V13.md](docs/TRAZABILIDAD_V13.md)
- Despliegue en la nube: [docs/DEPLOY_V13.md](docs/DEPLOY_V13.md)
- Validación: [docs/PRUEBAS_V13.md](docs/PRUEBAS_V13.md)

La versión conserva las tablas operativas y la interfaz V12, añade el módulo Modelo diplomático y sesiones revocables. El despliegue público debe utilizar datos ficticios y credenciales nuevas. No es una certificación de seguridad ni una implementación literal de las 33 tablas físicas del docente.
