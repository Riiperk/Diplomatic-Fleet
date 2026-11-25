# Diplomatic Fleet — estado comprobable de la entrega (24 de noviembre de 2026)

## Objetivo y decisiones de experiencia

Se optó por **catálogo público como portada** (`/` → `/catalogo`). El usuario puede mirar vehículos sin iniciar sesión, pero debe crear cuenta o acceder antes de solicitar. Desde `/login` hay acciones visibles para **Crear cuenta de cliente** y **Ver vehículos sin registrarme**. Los administradores entran a `/inicio` al autenticarse. La estética de alto contraste roja, negra y blanca se conserva, sin alusiones a «misiones» en el contenido comercial editado.

## Tecnología y modelo realmente construido

- **Frontend:** Vue 3, TypeScript, Vue Router, Vite; peticiones REST vía proxy de desarrollo `/api` hacia puerto 8080.
- **Backend:** Java 21, Kotlin + Spring Boot, Spring Security y JPA/JDBC.
- **Base de datos:** PostgreSQL, migraciones Flyway **V1–V8**; no se editan migraciones aplicadas.
- **Roles:** `CLIENTE` y `ADMINISTRADOR` en backend. La ruta cliente `/mis-reservas` y las administrativas están protegidas; se valida rol también en Spring Security.
- **Módulos disponibles en código:** registro y login, catálogo, solicitud y seguimiento, aprobación/rechazo/cancelación; administración de clientes, vehículos, documentos, mantenimientos, conductores autorizados, cotizaciones, usuarios y reportes.
- **Precio:** registros históricos con tarifa `NULL` son válidos («A cotizar» en administración). El catálogo público sólo expone vehículos con tarifa establecida.

## Limitaciones que NO deben presentarse como implementadas

El SVG del profesor incluye **33 entidades**. La implementación funcional anterior usa un modelo institucional simplificado y no implementa exactamente todas las 33; consulta `docs/INVENTARIO_DIAGRAMA_PROFESOR_33_ENTIDADES.md` para la trazabilidad por entidad. No existe acreditación diplomática oficial automática, facturación electrónica, pasarela de pago, asignación profesional completa de conductores ni auditoría exhaustiva. El software se considera una **entrega académica funcional ampliada**, no un sistema comercial certificado.

## Verificaciones y pendientes

Se inspeccionaron las rutas y los contratos de frontend/backend, se corrigieron errores reportados en Kotlin (`tarifaDiaria` nula y mapas JDBC con nulos) y se prepararon pruebas Postman. **En este contenedor no se logró completar `npm ci` sin red (dependencia `yocto-queue` ausente) y no se puede acceder al PostgreSQL de tu equipo**. En tu PC ya reportaste compilación Vue, conexión a PostgreSQL y listas de vehículos/clientes funcionando. La nueva revisión debe compilarse y probarse allí de nuevo.

Antes de entregar valida:

- [ ] `./gradlew.bat clean compileKotlin` termina correctamente.
- [ ] `cd frontend; npm ci; npm run build` termina correctamente.
- [ ] Spring Boot inicia y Flyway valida V1–V8 sin errores.
- [ ] Portada `/catalogo` y `/login` con **Crear cuenta** visible.
- [ ] Registro crea institución + usuario, luego el login devuelve rol CLIENTE.
- [ ] Cliente solicita vehículo, ve la reserva; administrador la consulta y la confirma.
- [ ] Vehículos anteriores sin tarifa no provocan error HTTP 500.
- [ ] Pruebas GET de Postman y comprobación de 403 para cliente en `/api/admin/usuarios`.
- [ ] No se incluyen secretos en el repositorio.

## Comandos de inicio en PowerShell

Terminal 1 (raíz del proyecto, variables de BD configuradas en esta misma sesión):

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/diplomatic_fleet'
$env:DB_USERNAME='postgres'
$env:DB_PASSWORD=[System.Net.NetworkCredential]::new('',(Read-Host 'Clave PostgreSQL' -AsSecureString)).Password
$env:ADMIN_EMAIL='admin@diplomaticfleet.co'
$env:ADMIN_PASSWORD=[System.Net.NetworkCredential]::new('',(Read-Host 'Clave admin (solo si es alta inicial)' -AsSecureString)).Password
.\gradlew.bat bootRun --console=plain
```

Terminal 2:

```powershell
cd frontend
npm run dev
```

URL: `http://localhost:5173`. La API: `http://localhost:8080`.

## Datos y GitHub

**No borres registros ni edites migraciones Flyway V1–V8 que ya están aplicadas.** Haz copia de tu proyecto y de PostgreSQL antes de desplegar una actualización. Asegura `.env` y credenciales fuera de Git; para demostraciones usa datos ficticios. El archivo completo sirve de referencia; para el proyecto local con cambios propios usa el **parche no destructivo** incluido aparte.
