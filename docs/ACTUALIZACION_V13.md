# Diplomatic Fleet — versión 13

## Alcance y requisitos

V13 amplía V12 sin cambiar la estética aprobada. Se aplica sobre la raíz del repositorio existente. No incluye credenciales, registros personales ni una copia de PostgreSQL. No reemplaza V1–V12, ni las imágenes, estilos o datos ya comprobados. Se recomienda conservar un respaldo Custom de pgAdmin y una copia del código antes de iniciar.

## Instalación

1. Detener Spring Boot y Vite. Descargar y extraer `Diplomatic-Fleet-V13-ACTUALIZACION-SEGURA.zip` fuera del repositorio.
2. Ejecutar `APLICAR_V13.ps1 -Proyecto <ruta de la raíz>` mediante PowerShell. El manifiesto CSV contiene los SHA-256 del paquete. Se respaldan los archivos existentes antes de escribir; la migración V13 no se sobrescribe si difiere. El instalador no ejecuta SQL, Git ni comandos de publicación.
3. En raíz: `./gradlew.bat compileKotlin` (Windows). En `frontend`: `npm run build`. Si hay un error, detener el proceso y revisar el diagnóstico.
4. Cargar DB_URL, DB_USERNAME y DB_PASSWORD como en V12 y ejecutar `./gradlew.bat bootRun --console=plain` desde la raíz. Debe validarse V1–V12 y aplicarse V13. Si Flyway falla, no ejecutar repair ni cambiar V1–V12. Verificar que el esquema siga en V12; corregir únicamente la migración que no se aplicó.
5. Ejecutar `npm run dev` en frontend. Abrir `http://localhost:5173/` e ingresar de nuevo; las credenciales de almacenamiento local V12 se eliminan deliberadamente.
6. En administración, abrir **13 Modelo diplomático**. Ejecutar `scripts/VERIFICAR_V13.ps1` con el backend iniciado y `docs/VERIFICACION_MODELO_V13.sql` en pgAdmin. Las pruebas de escritura se encuentran en `PRUEBAS_V13.md`.

## Cambios técnicos

- `V13__modelo_diplomatico.sql`: entidades nuevas, catálogos, FK, sincronización de tablas existentes y proyecciones del ER. Esquema de sesiones con hash de token.
- `DominioController.kt`: consulta administrativa y comandos de persona, acreditación, país, ciudad, documentos, contacto, dirección, talleres, ubicaciones, tipo vehicular, proveedor, conductor y baja. El catálogo no expone domicilios históricos ni identificaciones. El registro de baja es irreversible y comprueba reservas y mantenimiento pendientes.
- `SesionesWeb.kt` y `SecurityConfig.kt`: contraseña validada una vez mediante Basic, token aleatorio de 256 bits, hash SHA-256 y expiración de ocho horas; autorización por roles reevaluada en cada petición. CORS de origen explícito y opción de deshabilitar Basic para endpoints no destinados al acceso. No se utiliza cookie ni se guarda contraseña en el navegador.
- `UsuarioAuth.kt`: configuración de registro público; `AccesoYAsignacionesController.kt`: rotación de contraseñas con revocación de sesiones y normalización del código de licencia.
- `ReservaController.kt`: información de blindaje en catálogo, filtro, persistencia de requisito y validación bajo bloqueo de la unidad. La consulta de ubicaciones solo muestra las marcadas explícitamente públicas. Los datos históricos quedan privados por defecto.
- `OperacionAdminController.kt`: registra el usuario responsable de nuevos mantenimientos y enlaza el taller con la entidad normalizada; impide usar talleres inactivos conocidos.
- `DominioView.vue` y `dominioService.ts`: gestión de datos desde Vue utilizando identificadores reales, selectores vinculados a PostgreSQL y formularios documentados. `api.ts`, `clienteService.ts` y `vehiculoService.ts`: transporte HTTP centralizado sin Basic persistente. Los módulos de reserva muestran el requisito de blindaje. `AdminOperacionesView.vue`: permite rotar las contraseñas sin exponerlas.
- `Dockerfile`, `render.yaml`, `frontend/.env.example`, `_headers` y `_redirects`: configuración de despliegue. `DEPLOY_V13.md` explica cómo conectar los servicios.

## Decisiones y límites

Las 33 entidades se representan por tablas nuevas o vistas sobre tablas operativas reales (véase TRAZABILIDAD_V13.md). No se sustituyen todas las tablas originales por el modelo literal: se preservan los IDs numéricos, horarios con zona, relaciones contractuales institucionales, historial y múltiples roles. El proveedor se añade como entidad auxiliar porque el SVG menciona una clave foránea sin dibujar su destino. Los códigos de país, género, tipo, estado y blindaje son decisiones de implementación donde el diagrama no detalla atributos. No se deducen nacionalidades, certificaciones, fechas de baja ni acreditaciones desconocidas. Las operaciones de baja, blindaje y acreditación requieren pruebas integradas.

El modelo registra acreditaciones declaradas y vigencias; no valida ante organismos oficiales. El pago sigue siendo externo. No se implementa facturación electrónica DIAN ni pasarela bancaria. La autenticación con token mejora la V12, pero no convierte el sistema académico en un producto auditado para manejar información diplomática real. El token accesible a JavaScript presenta riesgo ante XSS, y no se incorpora MFA ni un sistema completo de mitigación de abuso. Utilizar únicamente datos ficticios y credenciales únicas antes de publicar.

Las pruebas de sintaxis ejecutadas no sustituyen una compilación Gradle, una comprobación de tipos Vue ni una migración real. La ejecución en la máquina de destino y la publicación en cuentas del usuario requieren validación posterior. No declarar aprobadas pruebas no ejecutadas.
