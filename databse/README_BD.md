Documentación Funcional de la Base de Datos
Proyecto: Diplomatic Fleet

1. Descripción General

La base de datos Diplomatic Fleet ha sido diseñada para gestionar el proceso de alquiler de vehículos diplomáticos, permitiendo administrar la información de clientes, vehículos, reservas y pagos de forma organizada y eficiente.



Su propósito principal es centralizar la información necesaria para el control de la flota vehicular, el registro de clientes, la gestión de reservas realizadas y el seguimiento de los pagos asociados a cada servicio.

2. Objetivo de la Base de Datos

La base de datos permite:

Registrar clientes y mantener actualizada su información.
Gestionar una flota de vehículos clasificados por categorías.
Controlar la disponibilidad de los vehículos.
Registrar reservas realizadas por los clientes.
Hacer seguimiento al estado de cada reserva.
Gestionar los pagos generados por los alquileres.
Generar consultas e informes para la toma de decisiones.


3. Componentes de la Base de Datos

La estructura está conformada por siete tablas principales:

3.1 Clientes

Almacena la información de los usuarios que realizan reservas.

Información registrada

Identificación del cliente

Nombre

Apellido

Teléfono

Dirección

Correo electrónico

Documento de identificación

Fecha de registro

Estado activo/inactivo

Funcionalidad


Permite identificar de manera única a cada cliente y asociarlo con sus respectivas reservas.

3.2 Categoría de Vehículos

Permite clasificar los vehículos según su nivel de servicio.

Categorías implementadas
LOW COST

VIP

ALTA GAMA

Funcionalidad

Facilita la segmentación y organización de la flota según el tipo de experiencia ofrecida al cliente.

3.3 Vehículos

Almacena la información de todos los vehículos disponibles para alquiler.

Información registrada

Placa

Marca

Modelo

Color

Tipo de combustible

Capacidad de pasajeros

Precio por día

Disponibilidad

Categoría

Funcionalidad


Permite administrar la flota y conocer qué vehículos pueden ser reservados.

3.4 Estados de Reserva

Contiene los diferentes estados que puede tener una reserva.

Estados registrados

Pendiente

Confirmada

En curso

Finalizada

Cancelada

Funcionalidad

Permite controlar el ciclo de vida de una reserva desde su creación hasta su finalización.

3.5 Reservas

Representa el proceso principal del sistema.

Información registrada

Cliente asociado

Vehículo reservado

Fecha de inicio

Fecha de finalización

Valor total

Estado de la reserva

Funcionalidad

Conecta clientes, vehículos y estados de reserva para gestionar el alquiler de vehículos.

3.6 Métodos de Pago

Almacena los métodos de pago aceptados por el sistema.

Métodos registrados

Tarjeta Crédito

Tarjeta Débito

Efectivo

PSE

Transferencia

Nequi

Daviplata

Funcionalidad


Permite clasificar y controlar las transacciones realizadas.

3.7 Pagos

Registra los pagos efectuados por los clientes.

Información registrada

Reserva asociada

Método de pago

Valor pagado

Fecha de pago

Funcionalidad

Permite llevar el control financiero de las reservas realizadas.

4. Relaciones de la Base de Datos

La base de datos presenta las siguientes relaciones:


Cliente - Reserva



CLIENTES (1)
|
|
|------< RESERVAS (N)

Un cliente puede realizar múltiples reservas.

Vehículo - Reserva
Plain Text
VEHICULOS (1)
|
|
|------< RESERVAS (N)


Un vehículo puede estar asociado a múltiples reservas a lo largo del tiempo.

Estado - Reserva
Plain Text
ESTADO_RESERVA (1)
|
|
|------< RESERVAS (N)

Cada reserva posee un único estado.



Categoría - Vehículo
Plain Text
CATEGORIA_VEHICULOS (1)
|
|
|------< VEHICULOS (N)


Una categoría puede contener varios vehículos.

Reserva - Pago
Plain Text
RESERVAS (1)
|
|
|------< PAGOS (N)

Una reserva puede generar uno o varios registros de pago.

Método de Pago - Pago
Plain Text
METODO_PAGOS (1)
|
|
|------< PAGOS (N)

Un método de pago puede utilizarse en múltiples transacciones.

5. Funcionalidades que Brinda el Sistema

La base de datos permite realizar las siguientes operaciones:

Gestión de Clientes

Registro de clientes.

Modificación de información personal.

Consulta de historial de reservas.

Gestión de Vehículos

Registro de vehículos.

Consulta de disponibilidad.

Clasificación por categoría.

Gestión de precios de alquiler.

Gestión de Reservas

Registro de reservas.

Seguimiento de estados.

Control de fechas de alquiler.

Cálculo de valores a pagar.

Gestión de Pagos

Registro de transacciones.

Consulta de historial de pagos.

Validación de métodos de pago.

Análisis de Información


Permite generar reportes como:


Clientes registrados.

Vehículos disponibles.

Vehículos más reservados.

Reservas activas.

Reservas canceladas.

Ingresos obtenidos.

Métodos de pago más utilizados.

Ingresos por categoría de vehículo.





6. Beneficios de la Implementación

La base de datos proporciona:

Organización estructurada de la información.
Seguridad e integridad de los datos.
Reducción de inconsistencias mediante claves primarias y foráneas.
Automatización de procesos relacionados con alquileres.
Facilidad para generar informes y estadísticas.
Escalabilidad para futuras funcionalidades.

7. Conclusión

La base de datos Diplomatic Fleet constituye el núcleo de información del sistema de alquiler de vehículos. Su diseño relacional permite gestionar eficientemente clientes, vehículos, reservas y pagos, garantizando consistencia, integridad y disponibilidad de la información. Además, proporciona una base sólida para el desarrollo de aplicaciones web, reportes administrativos y futuras ampliaciones del proyecto.
