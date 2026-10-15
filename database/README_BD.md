TABLA CLIENTES
Campo	Tipo de dato	Descripción
id_cliente	SERIAL	Identificador único
nombre	VARCHAR(70)	Nombre del cliente
apellido	VARCHAR(70)	Apellido del cliente
telefono	VARCHAR(70)	Número de contacto
correo	VARCHAR(70)	Correo electrónico

CLIENTES
    |
    | 1:N
    |
RESERVAS
    |
    | N:1
    |
VEHICULOS

RESERVAS
    |
    | 1:N
    |
PAGOS
``

REGLAS DE NEGOCIO
Un cliente puede realizar múltiples reservas.
Un vehículo puede estar asociado a múltiples reservas.
Cada reserva debe tener un estado.
Cada pago debe pertenecer a una reserva.
Las placas de los vehículos son únicas.
El correo de cada cliente es único.

FUNCIONALIDADES

Gestión de Clientes
Registro de clientes.
Actualización de información.
Consulta de historial.
Gestión de Vehículos
Registro de vehículos.
Consulta de disponibilidad.
Clasificación por categorías.
Gestión de Reservas
Creación de reservas.
Modificación de estado.
Seguimiento de alquileres.
Gestión de Pagos
Registro de transacciones.
Consulta de métodos de pago.

INDICADORES
Total de clientes registrados.
Total de vehículos disponibles.
Total de reservas realizadas.
Ingresos generados.
Vehículos más alquilados.
Categorías más solicitadas.



├── DiplomaticFleet.sql
├── README_BD.md
├── consultas.sql
├── diagrama_ER.png
├── diccionario_datos.md
├── reglas_negocio.md
└── imagenes/
    ├── informacion clientes.png
    ├── informacion reservas.png
    ├── estado de reservas.png
    ├── metodo de pagos.png

