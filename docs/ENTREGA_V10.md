# Diplomatic Fleet — Versión V10

## 1. Alcance y actualización

Sistema académico de alquiler vehicular institucional. Esta versión se construyó sobre el ZIP V9 actual proporcionado por el equipo. Conserva las nueve migraciones originales sin modificación de bytes. V10 incorpora una ampliación de flota, perfiles, asignación profesional, cotizaciones, reglas de inicio, métricas y auditoría técnica. Los datos de la base local no forman parte del ZIP.

**Estado de verificación:** la V9 tuvo pruebas manuales en el equipo del usuario mediante Vue, Postman y pgAdmin. La V10 requiere una nueva ejecución de compilación y pruebas integradas. No se afirma haber ejecutado la V10 contra la base del equipo ni que exista equivalencia completa con las 33 entidades del profesor. El entorno de preparación no logró descargar Gradle ni instalar dependencias npm; véase `VALIDACION_V10.md`.

## 2. Instalación segura sobre la V9

1. Realice una copia de seguridad de `diplomatic_fleet` en pgAdmin (clic derecho en la base > Backup, formato Custom). Conserve su configuración local. Detenga Kotlin y Vite con Ctrl+C en sus respectivas terminales.
2. Extraiga el ZIP V10 en una carpeta separada de la raíz Git. No utilice `git reset`, `git clean` ni borre archivos SQL. El archivo `APLICAR_V10.ps1` verifica hashes de los archivos de origen y de las migraciones, conserva un respaldo de archivos reemplazados y copia exclusivamente los archivos nuevos o modificados. No ejecuta SQL ni instala paquetes.
3. Desde PowerShell en la raíz de su proyecto V9, ejecute (adapte solamente la carpeta de extracción):

```powershell
$raiz = (git rev-parse --show-toplevel).Trim()
$origen = "$env:USERPROFILE\Downloads\Diplomatic-Fleet-V10"
powershell -NoProfile -ExecutionPolicy Bypass -File "$origen\APLICAR_V10.ps1" -Proyecto $raiz
```

4. Compile Kotlin antes de arrancar: ` .\gradlew.bat clean compileKotlin `. Compruebe que las variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` estén configuradas en el proceso, sin imprimir sus valores. Si faltan, configure las variables de Windows o el entorno de IntelliJ. No guarde la contraseña en archivos del proyecto.
5. Arranque ` .\gradlew.bat bootRun --console=plain `. Flyway debe validar las diez migraciones y aplicar únicamente V10 si V9 era la versión instalada. Si aparece un error, detenga el proceso y revise la causa; no modifique checksums de Flyway ni elimine la base.
6. En otra terminal: `cd frontend`, `npm ci`, `npm run build`, `npm run dev`. Requiere una versión de Node compatible con el `package.json` y `package-lock.json` originales. Acceda a `http://localhost:5173/`. La URL 8080 ofrece la API; ya no presenta un formulario de acceso simulado.

El ZIP contiene también un árbol completo de código fuente, útil para una copia limpia. La actualización por manifiesto es la opción recomendada para preservar el repositorio y los cambios locales del equipo.

## 3. Cuentas de demostración

El administrador existente se conserva. Con el backend V10 en ejecución, desde la raíz ejecute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\CREAR_CUENTAS_DEMO_V10.ps1
```

El script solicita la contraseña actual de administración mediante entrada oculta y verifica `/api/auth/me`. Crea solo correos inexistentes mediante la API. Genera una contraseña aleatoria distinta por cuenta; la imprime una vez y guarda una copia cifrada mediante DPAPI en `%LOCALAPPDATA%\DiplomaticFleet\CuentasDemoV10` (solo la misma cuenta de Windows y equipo pueden recuperarla). Conserve las credenciales en un gestor privado. Si un correo ya existe, no cambia su contraseña. Puede crear nuevas cuentas también en el módulo Usuarios y roles. Si utiliza otro correo de administrador, indique `-AdminEmail 'correo'`.

| Correo de prueba | Rol | Acceso |
|---|---|---|
| admin.secundario@example.com | ADMINISTRADOR | Gestión administrativa integral |
| operaciones.df@example.com | OPERACIONES | Flota, documentos, mantenimientos y disponibilidad |
| conductor.df@example.com | CONDUCTOR | Agenda de servicios asignados |
| cliente.a.df@example.com | CLIENTE | Reservas y propuestas de institución A |
| cliente.b.df@example.com | CLIENTE | Reservas y propuestas de institución B |

Las instituciones y las licencias de estas cuentas son ficticias. No acreditan identidad, facultades diplomáticas ni autorización de conducción real.

## 4. Manual de usuario resumido

**Cliente:** ingrese con su cuenta o cree una institución. Consulte catálogo por fechas; filtre por marca, categoría, capacidad mínima y presupuesto diario, ordene por tarifa o capacidad, seleccione modalidad con o sin conductor y medio de pago externo. Consulte Mis reservas y Mis cotizaciones. Reporte únicamente referencias de transferencias académicas; acepte/rechace una propuesta vigente cuando corresponda. Cancele antes de recogida y sin servicio iniciado. Puede modificar las fechas de solicitudes RECIBIDA/EN_REVISION futuras, sin cotización ni pago reportado; el servidor vuelve a validar disponibilidad y recalcula el valor.

**Administrador:** revise solicitudes, genere una cotización para una reserva pendiente, envíela y espere decisión del contacto institucional. Una solicitud sin cotización puede confirmarse directamente; si existe una cotización, se exige que la última versión esté aceptada. Para reservar con conductor, confirme y asigne un profesional habilitado. Registre/regularice el medio de pago de reservas antiguas sin pago, compruebe el pago externo, confirme entrega real y registre devolución. La entrega anticipada requiere justificación; la devolución anticipada exige motivo. No puede iniciar sin pago verificado ni con fechas finalizadas o recursos incompatibles. No puede desactivar la cuenta que utiliza ni dejar al sistema sin administradores activos.

**Operador:** puede administrar inventario, documentos y mantenimiento; no tiene acceso a usuarios, cotizaciones ni solicitudes administrativas. El estado operativo se modifica según los mantenimientos. Los datos documentales son declarativos; no equivalen a una consulta oficial.

**Conductor:** inicia sesión y consulta su propia agenda. La licencia se controla por fecha declarada y la asignación comprueba disponibilidad. Los servicios no se muestran a otros conductores mediante la ruta de agenda.

**Reportes y auditoría:** los indicadores distinguen activos, totales, programados y en proceso. El historial técnico registra cambios desde V10 con tabla, ID, acción, estado y usuario de base de datos. El actor web no se reconstruye para todos los eventos; la cuenta BD no equivale al usuario web. El historial no guarda hashes ni datos personales de contacto.

## 5. Reglas de negocio y cambios de estado

- Una solicitud se crea en `RECIBIDA`; la tarifa se calcula en Kotlin con días de alquiler redondeados y precio diario de PostgreSQL. El servidor bloquea la fila del vehículo para impedir reservas simultáneas incompatibles. El catálogo por fechas excluye reservas solapadas, mantenimiento programado incompatible y servicios en curso. Para reducir riesgo de doble entrega, un vehículo en servicio queda excluido del catálogo hasta finalizarlo.
- Las cotizaciones son versionadas: `BORRADOR` → `ENVIADA` → `ACEPTADA` o `RECHAZADA`; administración puede anular borradores y propuestas enviadas. Al generar una versión, la solicitud pasa a `COTIZADA`; al publicar, a `PENDIENTE_APROBACION`. No se cotizan solicitudes finalizadas, confirmadas o canceladas. La institución decide únicamente sus propias propuestas enviadas y vigentes. La publicación no envía correo electrónico.
- La reserva con conductor puede permanecer sin asignación hasta la confirmación. `CONFIRMADA` → `ASIGNADA` si requiere conductor profesional. La asignación usa una fila bloqueada del profesional, verifica vigencia y comprueba superposición. El inicio revalida los recursos.
- `CONFIRMADA` o `ASIGNADA` → `EN_SERVICIO` → `FINALIZADA`. El inicio anticipado requiere motivo; la devolución anticipada también. `CANCELADA` es una alternativa previa al inicio. Los pagos verificados de reservas canceladas pasan a `REEMBOLSO_PENDIENTE`; el reembolso se registra manualmente, no se ejecuta desde el portal.
- No existe integración con bancos ni pasarela de pago. `VERIFICADO` registra la revisión administrativa del pago externo. El valor estimado es comercial y no equivale a ingreso cobrado. La tarifa de demostración de vehículos no incluye cargos adicionales de conductor; no se simulan cargos fiscales ni facturas electrónicas.
- Los precios, imágenes y especificaciones precargados son ilustrativos. Las imágenes nuevas son dibujos originales SVG locales por categoría de carrocería y nombre de modelo, no fotografías certificadas. No se modifican imágenes personalizadas existentes, salvo la imagen genérica de los cuatro vehículos de demostración originales.

## 6. Documentación del código y pruebas

Consulte `DOCUMENTACION_CODIGO_V10.md` para el mapa técnico y `TRAZABILIDAD_V10.md` para la relación con los casos de uso y el diagrama. `diagramas/ER_OPERATIVO_V10.puml` representa el esquema operativo; el diagrama entregado por el profesor se conserva como referencia. Las guías anteriores corresponden a versiones históricas.

Importe `postman/Diplomatic_Fleet_V10.postman_collection.json` y `postman/Diplomatic_Fleet_V10.postman_environment.example.json` en Postman. Configure las credenciales localmente. Para evitar cambios masivos, no ejecute toda la colección a la vez: las pruebas con escritura requieren solicitudes independientes. El orden de regresión se especifica en `VALIDACION_V10.md`.

## 7. Interfaz y animaciones

La estética original del acceso se conserva. Se añadieron transiciones de rutas, entradas de encabezados y tarjetas escalonadas, navegación con indicadores animados, filtros de catálogo, estados reales de carga, y diálogos modales nativos con diseño institucional para reservar, reprogramar, cancelar, confirmar entregas, reportar pagos y decidir cotizaciones. No se simulan demoras ni respuestas. Los diálogos admiten teclado y la preferencia de movimiento reducido desactiva las animaciones.
