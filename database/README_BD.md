<<<<<<< HEAD
-Base de Datos Diplomatic Fleet

-Descripción


-Base de datos desarrollada en PostgreSQL para la gestión de  alquiler de vehículos diplomáticos.


-Tablas principales

-clientes

-vehiculos

-reservas

-pagos

-Relaciones

-Un cliente puede realizar varias reservas.

-Un vehículo puede tener varias reservas.

-Cada reserva genera un pago.

-Tecnologías utilizadas

-PostgreSQL

Autora

Sirley Xiomara Lobatón
=======
Base de Datos - Diplomatic Fleet
Descripción
Base de datos desarrollada en PostgreSQL para la gestión de vehículos diplomáticos.

Tecnologías Utilizadas
PostgreSQL
SQL
Git
GitHub
Estructura de la Base de Datos
Clientes
Almacena la información de los usuarios que realizan reservas.

Vehículos
Contiene los datos de los vehículos disponibles.

Reservas
Gestiona las reservas realizadas por los clientes.

Pagos
Registra los pagos asociados a las reservas.

Relaciones
CLIENTES
    |
    +----< RESERVAS >---- VEHICULOS
               |
               |
               v
             PAGOS
>>>>>>> 2efa841 (Agrega documentacion de la base de datos)
