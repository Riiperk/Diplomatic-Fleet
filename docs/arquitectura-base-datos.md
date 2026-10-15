# Arquitectura de la base de datos

## Objetivo

La base de datos de Diplomatic Fleet centraliza la información necesaria para administrar:

- Usuarios y roles.
- Clientes institucionales.
- Vehículos y categorías.
- Documentos vehiculares.
- Mantenimientos.
- Conductores autorizados.
- Solicitudes de servicio.
- Cotizaciones y sus detalles.

El esquema utiliza PostgreSQL y se gestiona mediante migraciones versionadas de Flyway.

## Tecnologías

- PostgreSQL 18
- Flyway
- Spring Boot
- Spring Data JPA
- Hibernate
- Kotlin
- Gradle

## Ubicación de las migraciones

Los archivos se encuentran en:

```text
src/main/resources/db/migration/
```

La estructura actual es:

```text
db/migration/
├── V1__crear_usuarios_roles_clientes.sql
├── V2__crear_vehiculos_documentos_mantenimientos.sql
├── V3__crear_solicitudes_conductores_cotizaciones.sql
└── V4__insertar_catalogos_iniciales.sql
```

## Reglas de las migraciones

Cada migración debe:

- Tener una versión única.
- Utilizar dos guiones bajos entre la versión y la descripción.
- Ejecutarse en orden ascendente.
- Incluir comentarios sobre las decisiones relevantes.
- Definir nombres explícitos para las restricciones.
- Evitar datos personales reales.
- Ser comprobada sobre una base de datos controlada.
- Confirmarse en Git después de superar la validación.

Una migración aplicada no debe modificarse. Los cambios posteriores deben incorporarse mediante una nueva migración.

## V1: usuarios, roles y clientes institucionales

Archivo:

```text
V1__crear_usuarios_roles_clientes.sql
```

Tablas creadas:

```text
roles
usuarios
usuarios_roles
clientes_institucionales
```

### Roles

La tabla `roles` contiene los perfiles utilizados para controlar responsabilidades y permisos.

Campos principales:

- `id`
- `nombre`
- `descripcion`
- `activo`
- `creado_en`

El nombre del rol es único.

### Usuarios

La tabla `usuarios` representa las cuentas autorizadas para acceder al sistema.

Campos principales:

- `id`
- `nombre`
- `apellido`
- `correo`
- `password_hash`
- `telefono`
- `activo`
- `creado_en`
- `actualizado_en`

La contraseña nunca debe almacenarse en texto plano. La columna `password_hash` almacenará el resultado producido por el mecanismo seguro utilizado por el backend.

El correo se controla mediante un índice único normalizado para evitar duplicados que solo difieran en mayúsculas, minúsculas o espacios.

### Usuarios y roles

La tabla `usuarios_roles` implementa una relación muchos a muchos:

```text
Usuario N ─── N Rol
```

La clave primaria está compuesta por:

```text
usuario_id
rol_id
```

Esto impide asignar dos veces el mismo rol al mismo usuario.

### Clientes institucionales

La tabla `clientes_institucionales` sustituye el modelo anterior de clientes particulares.

Tipos permitidos:

```text
EMBAJADA
CONSULADO
ORGANISMO_INTERNACIONAL
OTRO
```

Campos principales:

- `nombre_entidad`
- `tipo_entidad`
- `numero_identificacion_fiscal`
- `nombre_contacto`
- `correo_contacto`
- `telefono_contacto`
- `direccion`
- `ciudad`
- `activo`

La inactivación permite conservar el historial sin eliminar registros.

## V2: vehículos, documentos y mantenimientos

Archivo:

```text
V2__crear_vehiculos_documentos_mantenimientos.sql
```

Tablas creadas:

```text
categorias_vehiculo
vehiculos
documentos_vehiculo
mantenimientos
```

### Categorías de vehículos

Códigos permitidos:

```text
ECONOMICO
EJECUTIVO
PREMIUM
```

Cada vehículo debe estar relacionado con una categoría.

```text
CategoriaVehiculo 1 ─── N Vehiculo
```

### Vehículos

La tabla `vehiculos` contiene la información operativa de la flota.

Campos principales:

- `categoria_id`
- `placa`
- `marca`
- `modelo`
- `anio`
- `color`
- `combustible`
- `capacidad_pasajeros`
- `capacidad_equipaje`
- `estado`
- `activo`

Estados permitidos:

```text
DISPONIBLE
RESERVADO
EN_SERVICIO
EN_MANTENIMIENTO
INACTIVO
```

Tipos de combustible permitidos:

```text
GASOLINA
DIESEL
HIBRIDO
ELECTRICO
```

La disponibilidad no se almacena mediante un booleano. Se determina utilizando el estado operativo, las solicitudes asignadas y los mantenimientos activos.

La placa tiene un índice único normalizado.

### Documentos vehiculares

La tabla `documentos_vehiculo` registra los documentos y sus fechas de vencimiento.

Tipos permitidos:

```text
SOAT
REVISION_TECNOMECANICA
TARJETA_PROPIEDAD
POLIZA
OTRO
```

Estados permitidos:

```text
VIGENTE
PROXIMO_A_VENCER
VENCIDO
```

Se valida que la fecha de vencimiento no sea anterior a la fecha de emisión.

### Mantenimientos

La tabla `mantenimientos` registra los trabajos preventivos y correctivos.

Tipos permitidos:

```text
PREVENTIVO
CORRECTIVO
```

Estados permitidos:

```text
PROGRAMADO
EN_PROCESO
FINALIZADO
CANCELADO
```

El coste debe ser mayor o igual que cero. Si existe una fecha de finalización, también debe existir una fecha de inicio y la finalización no puede ser anterior.

## V3: solicitudes, conductores y cotizaciones

Archivo:

```text
V3__crear_solicitudes_conductores_cotizaciones.sql
```

Tablas creadas:

```text
conductores_autorizados
solicitudes
cotizaciones
detalles_cotizacion
```

### Conductores autorizados

Cada conductor pertenece a un cliente institucional:

```text
ClienteInstitucional 1 ─── N ConductorAutorizado
```

Los documentos, licencias y correos se normalizan mediante índices únicos.

La fecha de vencimiento de la licencia permitirá comprobar si el conductor puede ser asignado a un servicio.

### Solicitudes

La tabla `solicitudes` representa las peticiones institucionales de servicios vehiculares.

Modalidades:

```text
CON_CONDUCTOR
SIN_CONDUCTOR
```

Estados:

```text
RECIBIDA
EN_REVISION
COTIZADA
PENDIENTE_APROBACION
CONFIRMADA
ASIGNADA
EN_SERVICIO
FINALIZADA
RECHAZADA
CANCELADA
ARCHIVADA
```

Reglas principales:

- `fecha_fin` debe ser posterior a `fecha_inicio`.
- La cantidad de pasajeros debe ser positiva.
- Una solicitud cancelada requiere un motivo.
- La modalidad `CON_CONDUCTOR` requiere un conductor asignado.
- El vehículo puede permanecer vacío mientras la solicitud esté en revisión.

### Cotizaciones

Una solicitud puede recibir varias versiones de cotización:

```text
Solicitud 1 ─── N Cotizacion
```

Estados:

```text
BORRADOR
ENVIADA
ACEPTADA
RECHAZADA
VENCIDA
ANULADA
```

La combinación de `solicitud_id` y `numero_version` es única.

Los valores monetarios cumplen:

```text
valor_total = subtotal + impuestos - descuentos
```

Todos los valores deben ser mayores o iguales que cero.

### Detalles de cotización

Cada detalle representa un concepto económico.

```text
Cotizacion 1 ─── N DetalleCotizacion
```

El subtotal se calcula automáticamente:

```text
subtotal = cantidad × valor_unitario
```

La columna generada evita que la aplicación almacene un cálculo diferente del valor real.

## V4: catálogos iniciales

Archivo:

```text
V4__insertar_catalogos_iniciales.sql
```

### Roles insertados

```text
ADMINISTRADOR
COMERCIAL
MANTENIMIENTO
OPERACIONES
```

### Categorías insertadas

```text
ECONOMICO
EJECUTIVO
PREMIUM
```

Esta migración no contiene:

- Usuarios.
- Contraseñas.
- Clientes institucionales.
- Conductores.
- Vehículos.
- Solicitudes.
- Datos personales.

## Transformación del prototipo anterior

El modelo inicial contenía:

```text
clientes
categoria_vehiculos
vehiculos
estado_reserva
reservas
metodo_pagos
pagos
```

La refactorización realizó los siguientes cambios:

```text
clientes
→ clientes_institucionales

reservas
→ solicitudes

valor_total de reservas
→ cotizaciones

clientes particulares
→ entidades institucionales

disponible BOOLEAN
→ estado operativo del vehículo

precio_dia
→ conceptos y detalles de cotización
```

Los módulos de liquidaciones y pagos se implementarán posteriormente mediante nuevas migraciones.

## Correcciones realizadas

La refactorización corrigió:

- Relaciones sin claves válidas.
- Vehículos sin categoría.
- Identificadores inexistentes en registros de prueba.
- Correos con formato incorrecto.
- Errores tipográficos en columnas.
- Valores monetarios ambiguos.
- Datos personales con apariencia real.
- Disponibilidad duplicada.
- Precios almacenados directamente en vehículos.
- Valores totales almacenados en reservas.
- Ausencia de documentos y mantenimientos.
- Ausencia de usuarios y roles.
- Ausencia de conductores y cotizaciones.

## Privacidad y datos de demostración

Las migraciones no deben contener información personal real.

Cuando se necesiten datos de prueba, deben utilizarse valores evidentemente ficticios, por ejemplo:

```text
Embajada Demo
contacto@embajada-demo.example
0000000000
```

Los archivos de configuración tampoco deben almacenar contraseñas reales de PostgreSQL.

## Verificación ejecutada

Las migraciones fueron ejecutadas mediante Spring Boot y Flyway.

Resultados:

```text
V1: aplicada correctamente
V2: aplicada correctamente
V3: aplicada correctamente
V4: aplicada correctamente
```

La tabla `flyway_schema_history` contiene las versiones:

```text
1
2
3
4
```

Todas registradas con:

```text
success = true
```

El esquema contiene actualmente trece tablas, incluida `flyway_schema_history`.

## Catálogos verificados

Roles encontrados:

```text
ADMINISTRADOR
COMERCIAL
MANTENIMIENTO
OPERACIONES
```

Categorías encontradas:

```text
ECONOMICO
EJECUTIVO
PREMIUM
```

Todos los registros iniciales aparecen activos.

## Flujo de trabajo

Para añadir cambios al esquema:

1. Actualizar la rama base.
2. Crear una rama específica.
3. Crear una migración con una versión nueva.
4. Documentar restricciones y decisiones.
5. Ejecutar Spring Boot.
6. Revisar la salida de Flyway.
7. Verificar tablas o datos en PostgreSQL.
8. Confirmar el archivo mediante Git.
9. Crear un pull request.
10. Solicitar revisión de otra persona del equipo.

## Estado actual

La base de datos:

- Utiliza migraciones versionadas.
- Se puede reconstruir en orden.
- Mantiene integridad referencial.
- Contiene restricciones de dominio.
- Incluye índices para consultas frecuentes.
- Documenta tablas y columnas.
- Evita datos personales en las migraciones.
- Está preparada para crear las entidades JPA en Kotlin.