# Diplomatic Fleet — V9: registro, pagos y ciclo de alquiler

**Fecha de entrega:** 24 de noviembre de 2026. **Tecnologías:** Kotlin/Spring Boot, PostgreSQL/Flyway y Vue 3/TypeScript.

## Qué se implementó

1. **Registro institucional:** `ciudad` y `direccion` obligatorias; NIT opcional. Se guardan en `clientes_institucionales`, una entidad existente. Los datos de cuentas antiguas **no se inventan**: se completan con **Clientes → Editar datos** en el panel de administración.
2. **Pago externo, no pasarela:** al solicitar se escoge `TRANSFERENCIA`, `EFECTIVO` o `TARJETA_PRESENCIAL`. Se registra en la nueva tabla `pagos_reserva` con `PENDIENTE`. Si es transferencia, el cliente registra referencia en **Mis reservas**; el administrador verifica el pago **después** de comprobarlo por fuera del sistema. Para efectivo/tarjeta presencial, solo administración puede registrar la verificación cuando realmente se haya recibido. Esta app **no procesa cargos**, no pide números de tarjeta, no guarda CVV y no emite factura electrónica.
3. **Estados de servicio:** `RECIBIDA → EN_REVISION → CONFIRMADA → EN_SERVICIO → FINALIZADA`. Desde `RECIBIDA` puede ir directo a `CONFIRMADA`, y `RECIBIDA`/`EN_REVISION` pueden pasar a `RECHAZADA`. Para pasar a `EN_SERVICIO` se debe haber verificado el pago externo de las reservas nuevas. La devolución real puede ser **antes** de `fecha_fin`: exige constancia con motivo; se guardan `fecha_inicio_real` y `fecha_fin_real`.
4. **Cancelación:** cliente antes de la recogida en `RECIBIDA`, `EN_REVISION`, `COTIZADA`, `PENDIENTE_APROBACION` o `CONFIRMADA`; administrador puede cancelar esos estados incluso si venció la recogida cuando el vehículo no se entregó. No se cancela una reserva en curso o finalizada. Si había pago verificado, queda `REEMBOLSO_PENDIENTE` hasta que un operador haya efectuado la devolución y registre `REEMBOLSADO`.
5. **Compatibilidad:** historial V1–V8 sin cambios. Migración nueva `V9__pagos_y_ciclo_reservas.sql`, sin borrar datos. Las solicitudes anteriores no tienen registro en `pagos_reserva` (su método aparecerá «No registrado»), y aún pueden gestionarse según las reglas de estados.

## Relación con el diagrama del profesor

El SVG adjunto contiene 33 entidades. El módulo de reservas se vincula conceptualmente a **`reserva`, `cat_estado_reserva`, `vehiculo`, `cliente_diplomatico` y `usuario`**. El proyecto heredado mantiene, para preservar historial, las tablas equivalentes `solicitudes`, `vehiculos`, `clientes_institucionales`, `usuarios` y `roles`. `pagos_reserva` es **una extensión funcional propia**, no una entidad indicada en el dibujo del profesor.

**No es una reproducción exacta de las 33 entidades:** faltan tablas normalizadas de `persona`, `persona_direccion`, `pais`, `ciudad`, acreditación diplomática, baja de vehículo y otros catálogos, según `docs/INVENTARIO_DIAGRAMA_PROFESOR_33_ENTIDADES.md`. No renombrar las tablas existentes ni recrear la base en la víspera de entrega. Alinear el ER completo requiere migración incremental de datos y pruebas por entidad.

## Instalación segura desde IntelliJ IDEA (PowerShell)

1. **Respalda** tu repositorio y base de datos (opción Backup de pgAdmin). Verifica que los servidores `bootRun` y Vite estén detenidos antes de copiar.
2. Extrae el ZIP de parche en cualquier carpeta, navega a la raíz de tu repositorio en la terminal y ejecuta (cambia la ruta del script si lo extrajiste en otro lugar):

```powershell
$raiz = (git rev-parse --show-toplevel).Trim()
powershell -NoProfile -ExecutionPolicy Bypass -File "C:\Ruta\Parche\APLICAR_V9.ps1" -Proyecto "$raiz"
```

3. El script crea copias de los archivos reemplazados bajo `%TEMP%`, copia solamente los archivos listados y añade V9. No toca `.git`, `.env`, migraciones anteriores ni registros PostgreSQL.
4. En la terminal con `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` configurados:

```powershell
.\gradlew.bat clean compileKotlin
.\gradlew.bat bootRun --console=plain
```

5. En otra terminal:

```powershell
cd frontend
npm run build
npm run dev
```

6. Flyway debe indicar `Successfully validated 9 migrations` y Spring Boot `Tomcat started on port 8080`. Si falta una variable, configúrala en **esa misma terminal**, no modifiques el SQL.

## Pruebas manuales necesarias (no son resultados automáticos)

| Paso | Cuenta | Acción y resultado esperado |
|---|---|---|
| 1 | público | `/registro` exige ciudad y dirección; no acepta campos vacíos |
| 2 | cliente | Registro nuevo crea `usuarios` y `clientes_institucionales` con ciudad/dirección |
| 3 | admin | `Clientes → Editar datos` completa ubicación de institución antigua |
| 4 | cliente | Catálogo, seleccionar fechas, coche y método; reservar; aparece `PENDIENTE` |
| 5 | cliente | Si transferencia, `Mis reservas → Informar pago`; pasa a `REPORTADO` |
| 6 | admin | `Solicitudes → Verificar pago`; pasa a `VERIFICADO` solo tras verificación externa |
| 7 | admin | `Confirmar → Iniciar servicio → Finalizar servicio` (si hay devolución anticipada, indicar motivo). Fechas reales persistentes |
| 8 | cliente | Reserva nueva distinta: `Cancelar` antes de recogida incluso si estaba `CONFIRMADA` |
| 9 | admin | Reserva distinta: cancelar `CONFIRMADA` no iniciada; si pagada, `REEMBOLSO_PENDIENTE` hasta registrar devolución real |
| 10 | seguridad | Cliente obtiene 403 intentando usar `/api/admin/solicitudes` |
| 11 | prevención | Reservas con fechas superpuestas deben bloquear el mismo vehículo; se excluyen estados finalizados/cancelados |

**No ejecutes simultáneamente las acciones de finalizar y cancelar sobre una misma reserva**; usa dos solicitudes independientes.

## Endpoints nuevos y actualizados

- `POST /api/auth/registro`: ahora requiere `ciudad` y `direccion`; opcional `numeroIdentificacionFiscal`.
- `POST /api/solicitudes`: campo `metodoPago` (`TRANSFERENCIA` por defecto en API para compatibilidad de clientes antiguos).
- `POST /api/solicitudes/{id}/pago/reportar`: solo la institución del cliente; JSON `{"referencia":"TRX-12345"}`.
- `PATCH /api/admin/solicitudes/{id}/pago`: admin, JSON `{"estado":"VERIFICADO"}` / `RECHAZADO` / `{"estado":"REEMBOLSADO","observacion":"Transferencia de reembolso #1234"}`.
- `PATCH /api/admin/solicitudes/{id}/estado`: admin, `EN_REVISION`, `CONFIRMADA`, `RECHAZADA`, `EN_SERVICIO`, `FINALIZADA`; finalización anticipada `{ "estado":"FINALIZADA", "motivoCierre":"Devolución anticipada verificada" }`.
- `POST /api/solicitudes/{id}/cancelar`: cliente o admin (con permisos): `{ "motivo": "Cambio de itinerario" }`.
- `GET /api/solicitudes/mias` y `GET /api/admin/solicitudes` ahora exponen `metodoPago`, `estadoPago`, `referenciaPago`, `fechaInicioReal`, `fechaFinReal`, `motivoCierreAnticipado`.

## Limitaciones de la entrega

El código pasó **compilación aislada de sintaxis Kotlin del controlador**, y se verificaron los scripts TypeScript sintácticamente. En el entorno de preparación faltan dependencias de Gradle/npm y no hay acceso a tu PostgreSQL. **No se ha probado la integración completa**: ejecuta ambos builds y el recorrido en tu PC antes de presentar. Para un sitio público real harían falta pasarela certificada, HTTPS, sesión segura, auditoría extendida, acreditaciones verificadas y facturación electrónica según normativa aplicable.
