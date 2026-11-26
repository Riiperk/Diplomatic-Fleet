# Corrección de interacción y cotizaciones V13

Base de referencia: commit 218f53635377c98f5ccc514354c2a440bdd2951f. No se modifican las migraciones V1–V13 ni las variables del entorno cloud.

## Alcance

- Validaciones de fechas junto al formulario de disponibilidad; el catálogo exige una devolución posterior y un máximo de 90 días.
- Control de contraseña con texto e icono SVG, utilizable con teclado y sin depender de iconos del navegador. Se incorpora al acceso, registro y gestión de usuarios.
- Cotizar y Reservar son acciones separadas. Cotizar envía la solicitud con una bandera opcional al endpoint existente y genera un borrador y un detalle económico en la misma transacción. La unidad queda solicitada para el intervalo; no hay cobro, ni reserva confirmada. La tarifa es calculada por el servidor. El medio de pago es solo una preferencia.
- Administración puede revisar vigencia y observaciones, guardar el borrador, publicar o anular. El cliente ve la fase de preparación y decide únicamente propuestas propias publicadas; aceptar no equivale a confirmar el alquiler. Se protege la confirmación y el pago mientras una propuesta no haya sido aceptada. Las reservas directas y las previamente confirmadas mantienen su funcionamiento.
- Las cotizaciones anteriores permanecen en su estado histórico. No se añaden detalles de manera retroactiva. La vigencia inicial del borrador es de 15 días; se puede editar. Una propuesta vencida no puede aceptarse. La solicitud mantiene el bloqueo según las reglas actuales hasta cancelarse/rechazarse o resolverse; no hay liberación automática al vencer.

## Pruebas antes de integrar en main

En un clon limpio y una base local, nunca con credenciales de Neon en el entorno local de pruebas:

1. Ejecutar gradlew.bat test --console=plain y, en frontend, npm ci, npm run build y npm run test:unit -- --run. Resolver cualquier fallo antes de hacer push.
2. Ejecutar scripts/PROBAR_COTIZACIONES_V13.ps1 contra una base de pruebas local. El guion crea una solicitud ficticia y la cancela al finalizar; quedan registros de auditoría e historial. No usar credenciales de producción.
3. Catálogo: fechas iguales, devolución anterior, periodo superior a 90 días, fechas válidas. Confirmar mensaje junto al periodo y recuperación de resultados sin errores residuales.
4. Acceso: teclear clave, pulsar Mostrar/Ocultar, cambiar foco, comprobar que no se pierde el valor. Repetir en registro y administración. Probar teclas Tab, Espacio y Enter; comprobar vista estrecha. Probar clave incorrecta sin diálogo Basic.
5. Cliente ficticio: catálogo, fechas futuras disponibles, Cotizar, completar solicitud. Comprobar que Mis cotizaciones muestra En preparación y que no se confirma ni se cobra. No seleccionar una unidad reservada para la prueba.
6. Administrador: Solicitudes y Cotizaciones. Localizar el código, revisar el importe y vigencia, guardar borrador y publicar. Comprobar que no puede confirmar ni verificar el pago antes de la aceptación.
7. Cliente: actualizar Mis cotizaciones, ver importe y vigencia, aceptar o rechazar. Verificar que otro cliente no puede leer ni decidir la propuesta. Probar doble decisión (debe fallar), propuesta vencida y anulación. Para una rechazada, crear una nueva versión desde administración.
8. Aceptar una propuesta y confirmar el alquiler desde Solicitudes. Informar y verificar pago de demostración, iniciar/finalizar o cancelar según fechas reales. Probar una reserva directa sin cotización; debe funcionar como antes.
9. Comprobar persistencia y los conteos con SQL de solo lectura. No mostrar datos personales, hashes ni tokens.

```sql
SELECT s.codigo, s.estado AS estado_reserva, c.numero_version, c.estado AS estado_cotizacion,
       c.valor_total, c.valida_hasta,
       (SELECT COUNT(*) FROM detalles_cotizacion d WHERE d.cotizacion_id=c.id) AS detalles
FROM solicitudes s JOIN cotizaciones c ON c.solicitud_id=s.id
ORDER BY c.id DESC;
```

El instalador comprueba el contenido base de Git y prepara todos los cambios antes de escribir. En el paquete combinado V14 se agrega una migración de pagos y el protocolo docs/PAGOS_EXTERNOS_V14.md. No despliega ni ejecuta pruebas integrales. Crear un Pull Request y revisar las pruebas antes de fusionar. Render y Cloudflare se desplegarán según su configuración de main. Mantener el registro público limitado a los datos ficticios de la demostración.
