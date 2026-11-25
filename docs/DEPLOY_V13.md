# Despliegue académico — Cloudflare Pages, Render y Neon

Fecha de preparación: 25 de noviembre de 2026. Los servicios deben configurarse en cuentas propias. No se ha publicado la aplicación ni se han creado recursos externos.

## Arquitectura

Navegador → Cloudflare Pages (Vue estático) → HTTPS/CORS → Render (Spring Boot/Kotlin en Docker) → JDBC/TLS → Neon (PostgreSQL). GitHub es la fuente de código. Las variables de entorno del backend son privadas; `VITE_API_URL` es pública y únicamente contiene la URL del API. No se requiere mantener encendido el computador local.

## 1. Validación y seguridad antes de publicar

Ejecutar las comprobaciones locales V13, completar datos ficticios y revisar el diff de Git. No subir copias de la base, contraseñas, archivos de ambiente, capturas con credenciales, documentos personales ni tokens. En Usuarios y roles utilizar Cambiar contraseña para reemplazar cada contraseña de demostración por una distinta y robusta. Las sesiones se revocan. Guardar las nuevas credenciales de forma privada. Para una instalación nueva se puede usar un administrador inicial mediante ADMIN_EMAIL y ADMIN_PASSWORD. No configurar credenciales débiles ni reutilizar contraseñas. Mantener el repositorio privado si contiene trabajo académico no publicable.

## 2. Neon

Abrir https://console.neon.tech/ y crear un proyecto. Seleccionar PostgreSQL 18 si está disponible (la base local usa 18.6), o validar compatibilidad antes de importar. Para una base de demostración nueva y vacía, Flyway creará las tablas y los vehículos semilla al arrancar Render; no se necesitan archivos SQL manuales ni restauración. Las cuentas adicionales se crean desde administración. Esta opción no conserva las reservas locales. Si es necesario conservarlas, importar una copia con datos exclusivamente ficticios, incluyendo `flyway_schema_history` para mantener las sumas de verificación de las migraciones.

Conexión: en Neon pulsar Connect, desactivar pooling para migraciones y copiar host, base de datos, rol y contraseña. En Render ingresar:

| Variable | Valor |
|---|---|
| DB_URL | `jdbc:postgresql://HOST/BASE?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory` (usar el host y base reales; emplea el almacén de confianza de Java; si falla la validación, configurar la CA conforme a pgJDBC sin deshabilitar la verificación) |
| DB_USERNAME | Rol de Neon |
| DB_PASSWORD | Contraseña del rol de Neon |
| DB_POOL_SIZE | 4 |

No copiar la URI `postgresql://usuario:clave@...` en DB_URL ni publicar las credenciales. El driver necesita el prefijo `jdbc:postgresql://`. Utilizar conexión directa y verificar TLS. Si el proveedor de destino no admite PostgreSQL 18, no asumir que restaurar de una versión mayor a una menor está soportado.

### Importar datos locales opcionalmente

Detener el backend local antes de crear el respaldo para congelar los cambios. En pgAdmin, Backup de `diplomatic_fleet`, formato Custom. La alternativa con los ejecutables de PostgreSQL 18 es:

```powershell
# Usar el directorio bin de PostgreSQL 18, ajustar si la instalación es distinta.
$pg = 'C:\Program Files\PostgreSQL\18\bin'
& "$pg\pg_dump.exe" -h localhost -p 5432 -U postgres -d diplomatic_fleet -Fc --no-owner --no-privileges -f "$env:USERPROFILE\Downloads\fleet-v13.backup"
```

Registrar el servidor Neon en pgAdmin con SSL requerido y host directo. Restaurar sobre **una base de destino vacía**, no sobre una que Flyway haya inicializado. Configurar opciones de Restore sin owner ni privileges. También puede utilizar `pg_restore --no-owner --no-privileges --exit-on-error` con destino explícito y contraseña ingresada interactivamente; no colocarla en la línea de comandos. Verificar la restauración, conteos e historial Flyway antes de arrancar Render. Si ya hay sesiones copiadas, ejecutar en destino `DELETE FROM sesiones_web;` (no elimina usuarios ni reservas). No borrar la base local hasta comprobar la nube. No mantener dos instancias escribiendo sobre copias independientes durante la demostración.

## 3. GitHub

Desde la raíz del repositorio instalado y después de comprobar las pruebas locales: revisar `git status --short` y `git diff --check`; seleccionar los archivos del proyecto y comprobar `git diff --cached --stat` y `git diff --cached` antes de `git commit -m "feat: modelo diplomatico y despliegue V13"`. No utilizar `git add .` indiscriminadamente si hay archivos de respaldo o secretos. Subir a la rama de entrega acordada. No se modifica Git desde el instalador.

## 4. Render

Abrir https://dashboard.render.com/. Seleccionar New → Web Service, conectar el repositorio y la rama, runtime Docker, Dockerfile en la raíz. Se puede utilizar el Blueprint `render.yaml` si se prefiere. Elegir una región cercana a Neon. Seleccionar plan según presupuesto; Free es suficiente para intentar la demostración, sujeto a límites de RAM y arranque. Configurar DB_URL, DB_USERNAME, DB_PASSWORD como arriba. Si la base es nueva, configurar ADMIN_EMAIL y ADMIN_PASSWORD con valores propios (clave robusta, 12 a 72 bytes). Si se importaron usuarios, dejar variables de bootstrap vacías si no se requiere cuenta adicional.

| Variable | Valor recomendado |
|---|---|
| DF_BASIC_LOCAL | `false` (Basic solo para crear sesión) |
| DF_PUBLIC_REGISTRATION | `false` (alta controlada; cambiar a `true` solo para una demostración de registro, con datos ficticios) |
| DF_FRONTEND_ORIGIN | URL HTTPS exacta de Pages, sin barra final; añadirla después de crear Pages |

Configurar health check `/api/public/salud`. Render proporciona PORT y HTTPS. Confirmar en logs la migración y `Started DiplomaticFleetApplicationKt`. Guardar la URL real `https://...onrender.com`; comprobar `/api/public/salud`. Si Free presenta límites de memoria, revisar los logs y elegir un plan con más memoria, no desactivar seguridad ni Flyway. Free puede suspender el proceso tras 15 minutos de inactividad y demorar aproximadamente un minuto en reactivarlo. Abrir el sistema antes de la sustentación.

## 5. Cloudflare Pages

Abrir https://dash.cloudflare.com/, Workers & Pages → Create application → Pages → Import an existing Git repository. Conectar el mismo repositorio. Configurar:

| Campo | Valor |
|---|---|
| Root directory | `frontend` |
| Production branch | rama de entrega (por ejemplo, `main`) |
| Build command | `npm run build` |
| Build output directory | `dist` |
| NODE_VERSION | `24.12.0` o una versión compatible indicada por package.json |
| VITE_API_URL | URL HTTPS real de Render, sin `/api` ni barra final |

El archivo `_redirects` conserva Vue Router al recargar una ruta; `_headers` define cabeceras básicas. Después del primer despliegue, copiar URL `https://...pages.dev` y configurar exactamente esa URL en DF_FRONTEND_ORIGIN de Render; guardar y volver a desplegar. No utilizar `*` como origen ni copiar claves de Neon en Cloudflare Pages. Las imágenes Wikimedia pueden requerir conexión externa.

## 6. Verificación mediante Internet

En incógnito, abrir URL Pages. Comprobar que el catálogo aparece primero y las rutas recargan. Probar acceso erróneo sin popup, administrador, cliente, cotizaciones, reservas, disponibilidad, persistencia y permisos. Ejecutar `scripts/VERIFICAR_V13.ps1 -BaseUrl https://URL-REAL.onrender.com` usando cuentas ficticias. Si se desactiva el registro público, la página de alta mostrará el error de permisos; para demostrar alta, habilitarla expresamente durante la sustentación y volver a deshabilitarla. No se realizan cobros ni acreditaciones oficiales. Verificar desde un segundo equipo con el PC local apagado. Los enlaces reales solo existen tras completar estos pasos.

## Referencias oficiales

- Cloudflare Pages Vue: https://developers.cloudflare.com/pages/framework-guides/deploy-a-vue-site/
- Cloudflare build: https://developers.cloudflare.com/pages/configuration/build-configuration/
- Render Web Services: https://render.com/docs/web-services
- Render Docker: https://render.com/docs/docker
- Render planes gratuitos: https://render.com/docs/free
- Neon migración: https://neon.com/docs/import/migrate-from-postgres
- Neon conexión: https://neon.com/docs/get-started/connect-neon
- pgJDBC SSL: https://jdbc.postgresql.org/documentation/ssl/
