# Diplomatic Fleet — actualización V12

## Objetivo y alcance
Esta actualización se aplica a la V11 instalada y corregida en el equipo de desarrollo. La migración V11 ya aplicada debe permanecer idéntica. No se modifica el esquema ni se crean, eliminan o restablecen cuentas, vehículos, reservas o pagos. Se actualizan hasta 20 URLs de fotografía de unidades demostrativas únicamente si todavía utilizan recursos genéricos o carecen de imagen. Las URLs personalizadas se conservan.

## Implementación
- `SecurityConfig.kt`: punto de entrada REST común para credenciales ausentes o inválidas. Responde HTTP 401 JSON, sin cabecera `WWW-Authenticate`; el filtro de autenticación y todas las reglas de autorización continúan activos. No sustituye la autenticación ni corrige contraseñas incorrectas. Los accesos sin permisos continúan respondiendo 403.
- `api.ts`: las solicitudes de Vue declaran `X-Requested-With: XMLHttpRequest`. El formulario muestra el mensaje de error; solo guarda una sesión cuando la API valida las credenciales. HTTP Basic se mantiene para la práctica local, no para producción sin HTTPS y una arquitectura de sesión endurecida.
- `CatalogoView.vue` y `fleet.css`: portada angular, banda roja y negra en movimiento, área de consulta oscura, indicadores y filtros legibles, tarjetas con contraste. Los importes, intervalos y disponibilidad proceden de la API existente. Se eliminan los conflictos de animación entre la tarjeta y Vue TransitionGroup.
- `aparecer.ts`: un IntersectionObserver compartido revela cada ficha al entrar en el área visible y deja de observarla después. No utiliza temporizadores ni ciclos de renderizado JavaScript. El contenido no se oculta cuando el navegador solicita movimiento reducido.
- `login.css`, `VisualPanel.vue`, `LoginView.vue`: se conserva la composición original del acceso y se restablece la rotación continua de las estrellas; entrada del título y del automóvil, franja inferior animada. Los estilos de formularios se acotan al login. El recurso WebP reduce el peso del arte institucional de aproximadamente 1,8 MB a 95 KB.
- `VehiculoFoto.vue` y `fotografias.ts`: consumen la URL devuelta por el backend, comprueban el esquema HTTPS o recurso local permitido, cargan de forma diferida y muestran un aviso si falla. No sustituyen una fotografía por otro modelo. Los créditos se vinculan a la fuente y licencia de cada URL incluida; `FOTOGRAFIAS_V12.md` registra la procedencia. Las fotos son de referencia de modelo, no verifican el año, color o unidad real.
- `VehiculosView.vue`: mantiene el formulario administrativo de URL y añade vista previa. La edición utiliza la API de vehículos, como antes. Una fotografía personalizada requiere que su administrador tenga permiso de uso.
- `V12__fotografias_catalogo.sql`: actualización protegida por placa, marca, modelo y URL previa; no altera migraciones anteriores. Los disparadores existentes registrarán la actualización de las unidades en la auditoría técnica.

## Validación realizada y pendiente
Se comprobó sintaxis de los ocho scripts TypeScript modificados o nuevos, análisis CSS y correspondencia de las veinte referencias con los datos de demostración. Se verificaron en Chromium estático la composición de escritorio, el ancho móvil, la animación de la franja, la rotación de las estrellas y movimiento reducido. La vista estática no ejecuta Vue ni el backend. No se dispone de dependencias completas de npm/Gradle ni de PostgreSQL en este entorno: compilación, Flyway V12, autenticación y funcionamiento de todas las URLs deben probarse en el equipo del usuario. Se verificaron las páginas de origen de Wikimedia; no se garantiza disponibilidad futura de la red ni el FPS de un dispositivo concreto.

## Prueba local mínima
1. Detener los servidores, ejecutar APLICAR_V12.ps1, luego `npm run build` en frontend.
2. Iniciar backend con las variables DB_URL, DB_USERNAME, DB_PASSWORD existentes. Confirmar que Flyway aplica exclusivamente V12.
3. Ejecutar `scripts/VERIFICAR_V12.ps1` con Kotlin activo para comprobar el rechazo HTTP 401 sin reto de navegador y el acceso al catálogo público.
4. Iniciar Vue, abrir `/`, verificar entrada al catálogo y fotografias. Iniciar sesión con una cuenta válida. Probar una contraseña deliberadamente incorrecta en el formulario: debe mostrar error sin ventana nativa. Probar administrador y cliente con credenciales válidas.
5. En Vehículos, comprobar que se muestra la URL actual y su vista previa. Conservar la URL si no requiere cambios. Si un servidor externo no responde, la ficha debe conservar disponibilidad y botón de solicitud.
6. Guardar únicamente los cambios de código y documentación en Git; no publicar contraseñas ni respaldos.

Las limitaciones académicas previas y la trazabilidad de las entidades del diagrama permanecen descritas en `TRAZABILIDAD_V10.md`; esta actualización no incorpora funcionalidades adicionales de dominio.
