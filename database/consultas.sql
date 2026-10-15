-- Ver clientes
SELECT * FROM clientes;

-- Ver vehículos
SELECT * FROM vehiculos;

-- Ver reservas
SELECT * FROM reservas;

-- Ver pagos
SELECT * FROM pagos;

---Consulta  id, clientese:nombre apellido,forma de pago,valor del pago y fecha de pagp
SELECT
p.id_pago,
c.nombre,
c.apellido,
mp.forma_pago,
p.valor,
p.fecha_pago
FROM pagos p
INNER JOIN reservas r
ON p.id_reserva = r.id_reserva
INNER JOIN clientes c
ON r.id_cliente = c.id_cliente
INNER JOIN metodo_pagos mp
ON p.id_metdo_pago = mp.id_metodo_pago
ORDER BY p.id_pago;
---consulta sobre id reservas,cliente.marca del carro,placa,modelo
SELECT
r.id_reserva,
c.nombre,
c.apellido,
v.placa,
v.marca,
v.modelo,
r.fecha_incio,
r.fecha_fin,
r.valor_total,
e.nombre_estado
FROM reservas r
INNER JOIN clientes c
ON r.id_cliente = c.id_cliente
INNER JOIN vehiculos v
ON r.id_vehiculo = v.id_vehiculo
INNER JOIN estado_reserva e
ON r.id_estado = e.id_estado
ORDER BY r.id_reserva;
---Muestra cunatas cuentas hay registradas en cada categoría
SELECT COUNT(*) FROM clientes;
---Estado de reserva
SELECT
r.id_reserva,
c.nombre,
e.nombre_estado
FROM reservas r
INNER JOIN clientes c
ON r.id_cliente = c.id_cliente
INNER JOIN estado_reserva e
ON r.id_estado = e.id_estado;
---CLiente y vehiculo reservado
SELECT
c.nombre,
c.apellido,
v.marca,
v.modelo
FROM reservas r
INNER JOIN clientes c
ON r.id_cliente = c.id_cliente
INNER JOIN vehiculos v
ON r.id_vehiculo = v.id_vehiculo;

----Cantidad de vehiculos por categoría

SELECT
c.nombre_categoria,
COUNT(v.id_vehiculo) AS cantidad
FROM categoria_vehiculos c
LEFT JOIN vehiculos v
ON c.id_categoria = v.id_categoria
GROUP BY c.nombre_categoria;

---Cuentas existente en cada categoría


SELECT COUNT(*) FROM vehiculos;
SELECT COUNT(*) FROM reservas;
SELECT COUNT(*) FROM pagos;
SELECT COUNT(*) FROM estado_reserva;
SELECT COUNT(*) FROM metodo_pagos;
SELECT COUNT(*) FROM categoria_vehiculos;

----Clientes activos
SELECT *
FROM clientes
WHERE activo = TRUE;

SELECT nombre, apellido, fecha_registro
FROM clientes
ORDER BY fecha_registro DESC;

