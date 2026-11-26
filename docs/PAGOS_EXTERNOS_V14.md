# Control de pagos externos — entrega final

## Alcance
El sistema gestiona reservas, cotizaciones y pagos externos reales declarados por los usuarios. No debita fondos, no es una pasarela y no recoge datos de tarjetas. Para cobrar automáticamente se necesita un comercio habilitado, una pasarela y pruebas específicas con sus credenciales. No se inventan cuentas bancarias. La constancia descargable acredita únicamente el registro administrativo, no sustituye factura ni comprobante de un banco. El monto debe coincidir con el total de la reserva; no se admiten abonos parciales. La tarifa no incluye servicios o impuestos no parametrizados. Los registros y fotografías precargados son datos académicos, no acreditación de una flota o un comercio real.

## Flujo de aceptación
1. Cliente selecciona fechas, vehículo y método de pago previsto; solicita cotización o reserva directa.
2. Si solicitó cotización, administración revisa y publica el borrador; el cliente acepta o rechaza. Administración confirma la reserva.
3. Transferencia: cliente paga fuera del sistema a los datos confirmados por el proveedor y reporta importe y referencia; administración compara el comprobante con el abono recibido y registra importe y referencia de verificación. Si rechaza, registra un motivo; el cliente puede corregir el reporte.
4. Efectivo/tarjeta presencial: el operador cobra fuera de la plataforma y administración registra el recibo, importe y referencia. No introducir números completos de tarjeta, CVV ni credenciales bancarias.
5. La entrega del vehículo requiere pago VERIFICADO y las demás restricciones de disponibilidad/conductor existentes. Al finalizar se conserva la fecha real de devolución.
6. Si se cancela tras un pago verificado, queda REEMBOLSO_PENDIENTE. El administrador registra la devolución ya realizada, con importe y referencia. Si una transferencia reportada se concilia después del cierre, se marca reembolso pendiente. No hay devolución automática.
7. Desde la tabla de reservas, descargar la constancia del registro administrativo después de verificar o devolver el pago.

## Base de datos
V14 añade diez columnas a pagos_reserva. Los registros históricos sin importe y evidencia quedan NULL, nunca se reconstruye un pago a partir del estimado. Las migraciones anteriores, tablas, usuarios y configuraciones cloud no cambian. Las autorizaciones permanecen en Spring Security y las operaciones de escritura utilizan las transacciones y bloqueos existentes. Dos índices únicos impiden reutilizar una referencia de verificación o de reembolso para reservas del mismo método; si el recibo solo tiene un consecutivo local, registre también su emisor o terminal en la referencia.

## Pruebas obligatorias antes de publicar
- Gradle compileKotlin test; frontend npm run build y npm run test:unit -- --run.
- Ejecutar con PostgreSQL LOCAL y cuentas de prueba. Verificar Flyway V14. Ejecutar scripts/PROBAR_COTIZACIONES_V13.ps1: cotización, aceptación, confirmación, importe inválido, reporte, verificación, cancelación y reembolso. Son datos ficticios en entorno local, no pagos bancarios.
- Desde navegador: comprobar fechas, contraseñas, cotización y reserva directa. Repetir para efectivo y tarjeta presencial: confirmar, registrar comprobante, verificar, iniciar, finalizar. Probar transferencia rechazada y corregida; una reserva cancelada con transferencia reportada debe permitir conciliación administrativa. Comprobar permisos de otro cliente y ausencia de cobros en línea.
- Consultar en pgAdmin, sin mostrar referencias sensibles:

```sql
SELECT s.codigo,p.metodo,p.estado,p.monto_reportado,p.monto_verificado,p.monto_reembolsado,p.verificado_en,p.reembolsado_en
FROM pagos_reserva p JOIN solicitudes s ON s.id=p.solicitud_id ORDER BY p.id DESC;
```

- Antes de fusionar, respaldar Neon y comprobar que la rama main sigue siendo la base esperada. Una vez aprobado, Render aplica V14 y Cloudflare compila Vue. Esperar ambos despliegues y verificar en producción con cuentas ficticias, sin transferir dinero. No usar las credenciales de Neon en el frontend.
