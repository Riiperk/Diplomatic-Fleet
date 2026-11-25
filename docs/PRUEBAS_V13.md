# Protocolo de validación de V13

No ejecutar pruebas de escritura sobre una base con información real. Usar datos académicos ficticios y una copia de la base. Conservar capturas de Vue y PostgreSQL sin contraseñas, tokens, identificaciones reales ni referencias bancarias.

## Comprobaciones antes de ejecutar la aplicación

1. `./gradlew.bat compileKotlin` y `./gradlew.bat test` en la raíz; `npm run build` en frontend. No confundir análisis sintáctico con compilación completa.
2. Arrancar Spring Boot: deben aparecer 13 migraciones validadas y el esquema V13, sin excepciones. Confirmar que los conteos de vehículos, usuarios, instituciones y solicitudes anteriores no cambian por la migración.
3. Ejecutar `docs/VERIFICACION_MODELO_V13.sql`. Se esperan 33 filas presentes, 27 BASE TABLE y 6 VIEW, y los cuatro contadores de relaciones faltantes en cero. La entidad auxiliar proveedor y sesiones_web no forman parte de las 33.
4. Ejecutar `scripts/VERIFICAR_V13.ps1`. Debe obtener 401 sin desafío para credenciales inválidas, identidad administrativa por token, 403 con cliente, y 401 tras revocar token. Si existe un error, conservar el mensaje y detener despliegue.

## Pruebas funcionales de escritura (pendientes de ejecutar en el destino)

| Prueba | Operación | Resultado esperado |
|---|---|---|
| P01 | En Modelo diplomático, crear país ficticio de prueba y ciudad asociada. Editar ciudad histórica con país real solo si conocido. | FK de ciudad válida; selección disponible en Persona y Taller. |
| P02 | Crear cuenta institucional ficticia con ciudad y dirección. | Aparecen persona, contacto, dirección y perfil institucional asociado; la acreditación inicia pendiente. |
| P03 | Editar persona con segundo nombre, fecha de nacimiento ficticia y tipo documental; añadir documento con vigencia y contacto adicional. | Datos persisten tras recargar; fechas inválidas se rechazan. |
| P04 | Acreditación VIGENTE sin fecha/país/número/rango. | Se rechaza. Completar valores ficticios y fechas vigentes: guardar; el estado efectivo se muestra en consulta. |
| P05 | Crear taller de prueba y seleccionar su nombre exacto en Mantenimientos. | `mantenimientos.taller_id` se vincula al taller y `usuario_responsable_id` identifica al administrador/operador. Taller inactivo conocido: se rechaza. |
| P06 | Crear una ubicación pública genérica y una ubicación privada de prueba. | Solo la pública activa figura en sugerencias; la privada no aparece en GET /api/public/ubicaciones. Crear reserva con textos nuevos: quedan vinculados a ubicaciones privadas. |
| P07 | Seleccionar un vehículo de prueba sin blindaje declarado. | El filtro de blindaje no lo muestra; enviar solicitud con requiereBlindaje=true por API retorna 409. Declarar un nivel ficticio explícitamente para prueba; aparece en filtro y se permite reservar si está disponible. |
| P08 | Crear una solicitud con blindaje y posteriormente intentar retirar su nivel declarado. | La administración recibe conflicto mientras exista una reserva incompatible. |
| P09 | Intentar dar de baja una unidad con reserva o mantenimiento activo/programado. | Se rechaza; tras cerrar o cancelar operaciones de una unidad de prueba sin uso, dar de baja registra autor, fecha y motivo y la excluye del catálogo. Intentar reactivarla: debe rechazarse. No usar una unidad necesaria para la sustentación. |
| P10 | Acceder con administrador y cliente; cambiar contraseña de una cuenta de prueba desde Usuarios. | Token anterior revocado, nueva clave permite entrar, anterior no. Nunca cambiar la única cuenta sin tener guardada la nueva clave. |
| P11 | Repetir flujo de solicitud, pago externo, cotización cliente, asignación profesional, cierre real y cancelación. | Se conservan las reglas V10–V12 y las nuevas relaciones. |
| P12 | Desde otro equipo y la URL Pages, abrir catálogo y recargar /login, /mis-reservas, /inicio. | Vue navega, API HTTPS acepta solo origen autorizado y PostgreSQL conserva los cambios. |

## Evidencia de preparación

El análisis Kotlin PSI y transpilación sintáctica de TypeScript no detectaron errores en el código fuente. Se compararon los nombres de las 33 entidades del SVG exacto con los CREATE TABLE/VIEW de V13 y se verificó que las migraciones anteriores no forman parte del paquete. No se ejecutaron Gradle completo, vue-tsc, Docker, PostgreSQL ni las pruebas HTTP en esta sesión de generación debido a dependencias y servidor no disponibles. Las pruebas P01–P12 son un protocolo, no resultados aprobados.
