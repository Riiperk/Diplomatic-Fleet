# Validación y pruebas de regresión — V10

## Evidencia disponible

La V9 se probó en el equipo del usuario mediante consultas Postman, pantallas Vue y consultas de solo lectura a PostgreSQL. Se observaron la autenticación, catálogo, reserva, registro de pago externo, entrega, devolución anticipada, cancelación, restricción de rol, clientes, documentos, mantenimiento, disponibilidad, conductores autorizados, cotizaciones y reportes. La V10 modifica reglas y añade funciones, por lo que estas evidencias históricas no sustituyen las pruebas nuevas.

Comprobaciones de preparación: identidad byte a byte de las migraciones V1–V9; análisis sintáctico de 52 archivos Kotlin mediante PSI (sin errores sintácticos); transpilación sintáctica de 33 archivos TypeScript y bloques de script Vue (sin errores); lectura XML de 16 ilustraciones SVG; estructura JSON de la colección y entorno, con 52 peticiones y variables declaradas. No equivalen a compilación completa, comprobación de tipos, ejecución SQL ni prueba del navegador. Gradle no pudo descargar su distribución por falta de resolución de red; la instalación npm tampoco se completó. No se ejecutó la nueva versión sobre PostgreSQL. La versión debe compilarse y validarse en el equipo antes de subirla como entrega estable.

## Secuencia en Windows

1. Respalde PostgreSQL. Aplique el instalador por manifiesto desde una copia extraída en una carpeta independiente. No modifique migraciones aplicadas ni ejecute Flyway repair para ocultar discrepancias.
2. Ejecute `./gradlew.bat clean compileKotlin` y `./gradlew.bat test`; arranque `./gradlew.bat bootRun --console=plain`. Verifique la migración V10, conexión de base de datos y puerto 8080. En otra terminal ejecute `npm run build` y `npm run dev` dentro de frontend (instale dependencias si faltan, respetando la versión de Node del proyecto).
3. Cree las cuentas de demostración con el script de la guía. Importe la colección y el entorno V10 en Postman. Configure correos y contraseñas localmente; no exporte credenciales. No use Run Collection para ejecutar todas las peticiones de escritura.
4. Ejecute las consultas seguras. Compruebe el acceso administrador/cliente, el rechazo 403 al cliente en administración, la restricción al operador y la agenda del conductor. Compruebe catálogo por fechas antes de cada reserva, conserve las variables de fecha que genera esa consulta y seleccione un vehículo disponible. En modalidad use `SIN_CONDUCTOR` o `CON_CONDUCTOR` según el caso.
5. Cree una solicitud independiente pendiente. Genere y publique la cotización; compruebe que la cuenta propietaria pueda aceptarla y que una cuenta ajena no pueda verla ni decidirla. Verifique que una solicitud terminal no admite una nueva cotización. Confirme la solicitud después de aceptar su última cotización. No es envío de correo.
5a. Cree otra solicitud pendiente y reprogramable, consulte disponibilidad para otras fechas y configure `nuevoInicioISO` y `nuevoFinISO`. Ejecute la carpeta 05, verifique fechas e importe. Para la prueba negativa use una solicitud propia finalizada; debe devolver 409.
6. Registre referencia ficticia de pago externo y verifíquela como administrador. Intente iniciar antes de verificar el pago en otra solicitud; debe rechazarse. Para entrega anticipada pruebe primero sin motivo y después con motivo. Finalice con motivo de devolución anticipada y compruebe fechas reales. No se cobran fondos.
7. Cree otra reserva con conductor, asigne un profesional habilitado y verifique su propia agenda. Pruebe una asignación incompatible sobre otra reserva y compruebe que el servidor la rechaza. No reutilice una reserva finalizada en casos alternativos.
8. Cancele una reserva independiente antes de la recogida; use Consultar las mismas fechas tras cancelación (sin script que regenere el intervalo) y compruebe disponibilidad. Para cancelación con pago verificado, valide el estado de reembolso pendiente y el registro manual de reembolso, sin dinero real. Repita las pruebas de mantenimiento programado y documentos, incluida disponibilidad por fechas de control.
9. Compruebe reportes frente a los datos reales, usuarios activos, protección de la cuenta administradora actual, y auditoría técnica. Revise el catálogo, registro, navegación, menús, transiciones y pantallas de cliente, administrador, operador y conductor; compruebe que la preferencia de movimiento reducido limita animaciones. Revise también tamaños de pantalla y mensajes de error.
10. Conserve capturas con datos ficticios y sin contraseñas. Revise `git status`, `git diff` y los archivos nuevos antes de preparar el commit. No publique secretos ni copias de la base de datos.

## Criterio de cierre

Cada caso requiere resultado observado, código HTTP, evidencia y resultado esperado. Un 403 o 409 solo es una aprobación cuando la prueba espera explícitamente ese rechazo. No se deben cambiar las expectativas para ocultar fallos. Los casos no ejecutados permanecen pendientes. Véase `TRAZABILIDAD_V10.md` para límites del modelo académico.

### Pruebas unitarias añadidas

`ReglasAlquilerTest.kt` verifica duración exacta, fracciones de día, máximo de 90 días y rechazo de entradas inválidas. Se ejecuta en Windows con `./gradlew.bat test --tests '*ReglasAlquilerTest'`. La lógica pura también se ejecutó en el entorno de preparación sin Spring, usando el compilador Kotlin local y un comprobador independiente: ocho casos correctos. Las pruebas integradas siguen pendientes.
