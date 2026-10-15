CREATE TABLE clientes(
id_cliente SERIAL PRIMARY KEY,
nombre VARCHAR (70) NOT NULL,
apellido VARCHAR(70) NOT NULL,
telefono VARCHAR (70) UNIQUE NOT NULL,
direccion TEXT,
correo VARCHAR(70) UNIQUE NOT NULL,
documento_identificacion VARCHAR (20) UNIQUE  NOT  NULL,
fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
activo BOOLEAN DEFAULT TRUE);



CREATE TABLE categoria_vehiculos(
id_categoria SERIAL PRIMARY KEY,
nombre_categoria VARCHAR(50) NOT NULL,
descripcion TEXT
);


INSERT INTO categoria_vehiculos
(nombre_categoria,descripcion)
VALUES
('LOW COST','Vehiculos economicos'),
('VIP','Vehiculos ejecutivos'),
('ALTA GAMA','Vehiculos premium y de lujo');


ALTER TABLE categoria_vehiculos
ADD CONSTRAINT chk_categoria_nombre
CHECK (
nombre_categoria IN ('LOW COST', 'VIP', 'ALTA GAMA')
);


CREATE TABLE vehiculos(
id_vehiculo SERIAL PRIMARY KEY,
id_categoria INTEGER
REFERENCES categoria_vehiculos (id_categoria),
 placa VARCHAR(10) UNIQUE NOT NULL,
 marca VARCHAR (30) NOT NULL,
 modelo VARCHAR(30) NOT NULL,
 color VARCHAR(20 ),
 combustible VARCHAR (20),
 num_pasajeros INTEGER,
 precio_dia NUMERIC(12,2) NOT NULL,
 disponible BOOLEAN  DEFAULT TRUE);



CREATE TABLE estado_reserva(
id_estado SERIAL PRIMARY KEY,
nombre_estado VARCHAR (20)  UNIQUE NOT NULL,
descripcion TEXT);

INSERT INTO estado_reserva
(nombre_estado,descripcion)
VALUES
('PENDIENTE', 'Reserva en lista'),
('CONFIRMADA', 'Reserva realizada con exito'),
('EN CURSO', 'Vehiculo entregado al usuario'),
('FINALIZADA', 'Reserva completada exitosamente'),
('CANCELADA', 'La reserva ha sido cancelada');


 CREATE TABLE reservas(
id_reserva SERIAL PRIMARY KEY,
id_cliente  INTEGER 
REFERENCES clientes  (id_cliente),

id_vehiculo INTEGER 
REFERENCES vehiculos (id_vehiculo),


fecha_incio TIMESTAMP NOT NULL,
fecha_fin TIMESTAMP  NOT NULL ,
valor_total NUMERIC (12,2),
id_estado INTEGER 
REFERENCES estado_reserva(id_estado)

 );
 

CREATE TABLE metodo_pagos(
id_metodo_pago SERIAL PRIMARY KEY,
forma_pago VARCHAR (50) UNIQUE NOT NULL);

 INSERT INTO metodo_pagos
 (forma_pago)

 VALUES
 ('TARJETA CREDITO'),
('TARJETA DEBITO'),
('EFECTIVO'),
('PSE'),
('TRANSFERENCIA'),
('NEQUI'),
('DAVIPLATA');

 CREATE TABLE pagos(
 id_pago SERIAL PRIMARY KEY,
 id_reserva INTEGER
 REFERENCES reservas(id_reserva ),
 
 id_metdo_pago INTEGER
 REFERENCES metodo_pagos(id_metodo_pago),
 fecha_pago TIMESTAMP
 DEFAULT CURRENT_TIMESTAMP,
 valor NUMERIC (12,2) NOT NULL);




INSERT INTO clientes
(
nombre,
apellido,
telefono,
direccion,
correo,
documento_identificacion
)
VALUES
(
'Xiomara',
'Lobato',
'3001238975',
'Bogota',
'exito_xiomara@,poligran.edu.co'
'19567565'),
('Juan David','Perez Garcia','3104567890','Cra 15 #80-20, Bogota','juan.perez@gmail.com','100000001'),
('Maria Fernanda','Gomez Ruiz','3112345678','Cra 7 #120-15, Bogota','maria.gomez@gmail.com','100000002'),
('Carlos Andres','Rodriguez Lopez','3123456789','Calle 45 #10-25, Bogota','carlos.rodriguez@gmail.com','100000003'),
('Laura Sofia','Martinez Castro','3134567890','Cra 30 #50-40, Bogota','laura.martinez@gmail.com','100000004'),
('Alejandro','Moreno','3104587123','Calle 80 #15-20, Bogota','alejandro.moreno@gmail.com','100000021'),
('Paula','Jimenez','3115678912','Cra 13 #72-15, Bogota','paula.jimenez@gmail.com','100000022'),
('Ricardo','Suarez','3126789123','Calle 127 #20-45, Bogota','ricardo.suarez@gmail.com','100000023'),
('Diana','Castillo','3137891234','Cra 9 #140-30, Bogota','diana.castillo@gmail.com','100000024'),
('Felipe','Rincon','3148912345','Calle 170 #8-25, Bogota','felipe.rincon@gmail.com','100000025'),
('Natalia','Parra','3159123456','Cra 30 #50-10, Bogota','natalia.parra@gmail.com','100000026'
);




INSERT INTO vehiculos 

(placa,
 marca ,
 modelo ,
 color ,
 combustible, 
 num_pasajeros, 
 precio_dia )
VALUES 
---LOW COST
('ABC106','KIA','HIBRID','BLANCO','GASOLINA',5,160.000),
('ABC107','RENAULT','KANOS','GRIS','GASOLINA',5,170.00),
('ABC108','CHEVROLET','RK','ROJO','GASOLINA',5,178.00),
('ABC109','HYUNDAI','ZTX','NEGRO','GASOLINA',5,180.00),
('ABC1010','SUZUKI','STT','PLATA','GASOLINA',5,190.00),
---VIP
('VIP206','TOYOTA','BRIDA','NEGRO','GASOLINA',5,260.00),
('VIP207','MAZDA','3','BLANCO','GASOLINA',5,250.00),
('VIP208','HONDA','CIVIC','GRIS','GASOLINA',5,300.00),
('VIP209','TOYOTA','CAMRY','NEGRO','GASOLINA',5,350.00),
('VIP2010','NISSAN','SENTRA','AZUL','GASOLINA',5,240.00),
---ALTA GAMA
('LUJ306','BMW','X6','NEGRO','GASOLINA',5,400.00),
('LUJ307','MERCEDES','C250','BLANCO','GASOLINA',5,450.00),
('LUJ308','AUDI','A6','ROJO','GASOLINA',5,460.00),
('LUJ309','PORSCHE','CAMSA','GRIS','GASOLINA',5,490.00),
('LUJ3010','RANGE ROVER','LUXURY','NEGRO','GASOLINA',5,500.000);

INSERT INTO reservas
(
id_cliente,
id_vehiculo,
fecha_incio,
fecha_fin,
valor_total,
id_estado
)
VALUES
(1,1,'2026-10-01 08:01:00','2026-10-03 08:00:00',320000,2),
(2,2,'2026-10-02 08:02:00','2026-10-05 08:00:00',510000,1),
(3,3,'2026-10-03 08:03:00','2026-10-06 08:00:00',534000,2),
(4,4,'2026-10-04 08:04:00','2026-10-06 08:00:00',360000,3),
(5,5,'2026-10-05 08:05:00','2026-10-08 08:00:00',570000,4),
(6,6,'2026-10-06 08:06:00','2026-10-09 08:00:00',780000,2),
(7,7,'2026-10-07 08:07:00','2026-10-10 08:00:00',750000,1),
(8,8,'2026-10-08 08:08:00','2026-10-11 08:00:00',900000,2),
(9,9,'2026-10-09 08:09:00','2026-10-11 08:00:00',700000,5),
(10,10,'2026-10-10 08:10:00','2026-10-13 08:00:00',720000,2),

(12,12,'2026-10-12 08:11:00','2026-10-15 08:00:00',1350000,3),
(13,13,'2026-10-13 08:12:00','2026-10-15 08:00:00',920000,4),
(14,14,'2026-10-14 08:13:00','2026-10-16 08:00:00',980000,2),
(15,15,'2026-10-15 08:14:00','2026-10-18 08:00:00',1500000,1),
(16,36,'2026-10-16 08:15:00','2026-10-18 08:00:00',420000,2),
(17,37,'2026-10-17 08:16:00','2026-10-20 08:00:00',750000,1),
(18,38,'2026-10-18 08:17:00','2026-10-21 08:00:00',900000,2),
(19,39,'2026-10-19 08:18:00','2026-10-22 08:00:00',1050000,3),
(20,40,'2026-10-20 08:19:00','2026-10-23 08:00:00',1200000,4);


INSERT INTO pagos
(
id_reserva,
id_metdo_pago,
valor
)
VALUES
(21,1,320000),
(22,4,510000),
(23,2,534000),
(24,3,360000),
(25,5,570000),
(26,6,780000),
(27,7,750000),
(28,1,900000),
(29,3,700000),
(30,4,720000),
(31,2,400000),
(32,5,1350000),
(33,1,920000),
(34,7,980000),
(35,4,1500000),
(36,6,420000),
(37,3,750000),
(38,2,900000),
(39,5,1050000);





SELECT * FROM clientes;

SELECT * FROM categoria_vehiculos;

SELECT * FROM vehiculos;

SELECT * FROM estado_reserva;

SELECT * FROM reservas;

SELECT * FROM metodo_pagos;

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


COMMENT Ver cuántos registros tiene cada tabla

SELECT COUNT(*) FROM clientes;
SELECT COUNT(*) FROM vehiculos;
SELECT COUNT(*) FROM reservas;
SELECT COUNT(*) FROM pagos;
SELECT COUNT(*) FROM estado_reserva;
SELECT COUNT(*) FROM metodo_pagos;
SELECT COUNT(*) FROM categoria_vehiculos;