# Módulo de clientes institucionales

## 1. Descripción general

El módulo de clientes institucionales permite administrar las entidades que solicitan servicios de transporte a Diplomatic Fleet.

Las instituciones pueden corresponder a embajadas, consulados, organismos internacionales u otras entidades autorizadas.

El módulo permite:

- Registrar clientes institucionales.
- Consultar las instituciones almacenadas.
- Buscar clientes por sus datos principales.
- Consultar un cliente mediante su identificador.
- Actualizar la información institucional.
- Activar o inactivar clientes sin eliminar su historial.
- Validar nombres, correos e identificaciones fiscales duplicadas.
- Conservar fechas de creación y actualización.
- Integrar el frontend Vue.js con la API REST de Spring Boot.
- Persistir la información en PostgreSQL.

## 2. Historia de usuario relacionada

### Gestión de clientes institucionales

Como usuario autorizado de Diplomatic Fleet, quiero registrar y administrar clientes institucionales para asociarlos posteriormente con solicitudes, cotizaciones y servicios de transporte.

### Criterios implementados

- El nombre de la entidad es obligatorio.
- El tipo de entidad es obligatorio.
- El nombre del contacto es obligatorio.
- El correo del contacto es obligatorio y debe ser válido.
- El nombre de la entidad no puede repetirse.
- El correo de contacto no puede pertenecer a otra institución.
- La identificación fiscal no puede repetirse cuando se proporciona.
- Un cliente puede inactivarse sin eliminarse de la base de datos.
- Los clientes se consultan desde PostgreSQL.
- El frontend actualiza el listado inmediatamente después de registrar o modificar una institución.

## 3. Arquitectura

El módulo sigue una arquitectura organizada por responsabilidades:

```text
Frontend Vue.js
      |
      | HTTP Basic + JSON
      v
ClienteInstitucionalController
      |
      v
ClienteInstitucionalService
      |
      v
ClienteInstitucionalRepository
      |
      v
PostgreSQL

```

Las responsabilidades se distribuyen de la siguiente manera:

- **Vista Vue:** presenta formularios, indicadores y registros.
- **Servicio TypeScript:** centraliza las peticiones HTTP.
- **Controlador REST:** recibe y responde solicitudes HTTP.
- **Servicio Kotlin:** aplica validaciones y reglas de negocio.
- **Repositorio JPA:** realiza operaciones de persistencia.
- **Entidad JPA:** representa la tabla de PostgreSQL.
- **DTO:** separa los datos de la API de las entidades persistentes.
- **PostgreSQL:** conserva la información institucional.

## 4. Estructura de PostgreSQL

La información se almacena en la tabla:

```text
clientes_institucionales
```

### Columnas principales

| Columna | Tipo | Obligatorio | Descripción |
|---|---|---:|---|
| `id` | `BIGINT` | Sí | Identificador generado por PostgreSQL |
| `nombre_entidad` | `VARCHAR(150)` | Sí | Nombre oficial de la institución |
| `tipo_entidad` | `VARCHAR(40)` | Sí | Clasificación institucional |
| `numero_identificacion_fiscal` | `VARCHAR(40)` | No | Identificación fiscal o institucional |
| `nombre_contacto` | `VARCHAR(140)` | Sí | Persona responsable de la comunicación |
| `correo_contacto` | `VARCHAR(150)` | Sí | Correo electrónico del contacto |
| `telefono_contacto` | `VARCHAR(30)` | No | Teléfono de contacto |
| `direccion` | `VARCHAR(255)` | No | Dirección física |
| `ciudad` | `VARCHAR(100)` | No | Ciudad de ubicación |
| `activo` | `BOOLEAN` | Sí | Determina si puede solicitar servicios |
| `creado_en` | `TIMESTAMP WITH TIME ZONE` | Sí | Fecha y hora de creación |
| `actualizado_en` | `TIMESTAMP WITH TIME ZONE` | Sí | Fecha y hora de actualización |

## 5. Tipos de entidad

El enum `TipoEntidad` admite los siguientes valores:

```text
EMBAJADA
CONSULADO
ORGANISMO_INTERNACIONAL
OTRO
```

Los valores coinciden con la restricción:

```text
chk_clientes_tipo_entidad
```

definida en la migración V1.

## 6. Restricciones de unicidad

La migración contiene índices únicos para evitar registros duplicados.

### Nombre institucional

```text
uq_clientes_nombre_entidad_normalizado
```

Impide registrar instituciones con el mismo nombre, ignorando espacios externos y diferencias entre mayúsculas y minúsculas.

### Correo de contacto

```text
uq_clientes_correo_contacto_normalizado
```

Impide asignar el mismo correo principal a instituciones diferentes.

### Identificación fiscal

```text
uq_clientes_identificacion_fiscal
```

Impide repetir una identificación fiscal cuando el campo contiene un valor.

La capa de servicios también valida estos datos antes de enviar la operación a PostgreSQL.

## 7. Endpoints REST

La ruta base del módulo es:

```text
/api/clientes
```

### Registrar cliente

```http
POST /api/clientes
```

Respuesta satisfactoria:

```text
HTTP 201 Created
```

### Listar clientes

```http
GET /api/clientes
```

Respuesta satisfactoria:

```text
HTTP 200 OK
```

### Consultar cliente por identificador

```http
GET /api/clientes/{id}
```

Respuesta satisfactoria:

```text
HTTP 200 OK
```

Si el cliente no existe:

```text
HTTP 404 Not Found
```

### Actualizar cliente

```http
PUT /api/clientes/{id}
```

Respuesta satisfactoria:

```text
HTTP 200 OK
```

Si otro registro utiliza un dato único:

```text
HTTP 409 Conflict
```

Si la solicitud contiene datos inválidos:

```text
HTTP 400 Bad Request
```

## 8. Ejemplo de registro

```json
{
  "nombreEntidad": "Embajada de Aurora",
  "tipoEntidad": "EMBAJADA",
  "numeroIdentificacionFiscal": "AUR-900001",
  "nombreContacto": "Laura Gómez",
  "correoContacto": "contacto@aurora.example",
  "telefonoContacto": "+57 300 000 0000",
  "direccion": "Carrera 10 # 20-30",
  "ciudad": "Bogotá",
  "activo": true
}
```

## 9. Ejemplo de respuesta

```json
{
  "id": 1,
  "nombreEntidad": "Embajada de Aurora",
  "tipoEntidad": "EMBAJADA",
  "numeroIdentificacionFiscal": "AUR-900001",
  "nombreContacto": "Laura Gómez",
  "correoContacto": "contacto@aurora.example",
  "telefonoContacto": "+57 300 000 0000",
  "direccion": "Carrera 10 # 20-30",
  "ciudad": "Bogotá",
  "activo": true,
  "creadoEn": "2026-10-15T12:00:00-05:00",
  "actualizadoEn": "2026-10-15T12:00:00-05:00"
}
```

## 10. Backend implementado

### Dominio

```text
src/main/kotlin/com/diplomaticfleet/backend/clientes/domain/TipoEntidad.kt
src/main/kotlin/com/diplomaticfleet/backend/clientes/domain/ClienteInstitucional.kt
```

### Repositorio

```text
src/main/kotlin/com/diplomaticfleet/backend/clientes/repository/ClienteInstitucionalRepository.kt
```

### DTO

```text
src/main/kotlin/com/diplomaticfleet/backend/clientes/dto/ClienteInstitucionalRequest.kt
src/main/kotlin/com/diplomaticfleet/backend/clientes/dto/ClienteInstitucionalResponse.kt
```

### Servicio

```text
src/main/kotlin/com/diplomaticfleet/backend/clientes/service/ClienteInstitucionalService.kt
```

### Controlador

```text
src/main/kotlin/com/diplomaticfleet/backend/clientes/controller/ClienteInstitucionalController.kt
```

## 11. Frontend implementado

### Servicio HTTP

```text
frontend/src/services/clienteService.ts
```

El servicio implementa:

- Consulta de clientes.
- Consulta por identificador.
- Registro de instituciones.
- Actualización de instituciones.
- Autenticación HTTP Basic.
- Interpretación de errores del backend.
- Prevención del cuadro nativo de autenticación del navegador.

### Vista funcional

```text
frontend/src/views/ClientesView.vue
```

La vista contiene:

- Formulario de registro.
- Listado de clientes institucionales.
- Buscador.
- Indicador de clientes totales.
- Indicador de clientes activos.
- Indicador de clientes inactivos.
- Indicador de clasificaciones utilizadas.
- Activación e inactivación de instituciones.
- Mensajes de éxito y error.
- Diseño adaptable a diferentes tamaños de pantalla.
- Identidad visual roja, negra, blanca y dorada.

### Enrutamiento

La ruta funcional es:

```text
/clientes
```

La configuración se encuentra en:

```text
frontend/src/router/index.ts
```

## 12. Flujo funcional

```text
1. El usuario inicia sesión.
2. El usuario entra en Clientes.
3. Vue consulta GET /api/clientes.
4. Spring Boot recupera los clientes desde PostgreSQL.
5. La vista muestra indicadores y registros.
6. El usuario completa el formulario.
7. Vue envía POST /api/clientes.
8. El servicio valida datos obligatorios y únicos.
9. JPA guarda la institución.
10. La API devuelve HTTP 201.
11. Vue añade el cliente al listado.
12. El usuario puede activar o inactivar la institución.
13. Vue envía PUT /api/clientes/{id}.
14. PostgreSQL conserva el nuevo estado.
```

## 13. Manejo de errores

El módulo utiliza las excepciones comunes:

```text
ConflictoException
RecursoNoEncontradoException
```

El `GlobalExceptionHandler` transforma las excepciones en respuestas uniformes.

### Errores controlados

- Datos obligatorios ausentes.
- Correo con formato incorrecto.
- Nombre institucional duplicado.
- Correo de contacto duplicado.
- Identificación fiscal duplicada.
- Cliente inexistente.
- Sesión inválida.
- Error de conexión con la API.

## 14. Validaciones realizadas

### Backend

Se ejecutó:

```powershell
.\gradlew.bat compileKotlin
```

Resultado:

```text
BUILD SUCCESSFUL
```

También se ejecutó:

```powershell
.\gradlew.bat test
```

Resultado:

```text
BUILD SUCCESSFUL
```

### Frontend

Se ejecutó:

```powershell
npm run build
```

Resultados:

```text
Validación TypeScript completada
59 módulos transformados
Compilación finalizada correctamente
```

### Prueba funcional

Se verificaron los siguientes procesos:

- Registro de un cliente institucional.
- Persistencia en PostgreSQL.
- Consulta del listado.
- Búsqueda de instituciones.
- Actualización inmediata de los indicadores.
- Inactivación de un cliente.
- Activación de un cliente.
- Permanencia de los registros después de recargar.
- Navegación desde la barra lateral.

## 15. Seguridad de la demo

Durante la demostración se utiliza autenticación HTTP Basic temporal.

```text
Usuario técnico: user
```

La contraseña se proporciona mediante una variable de entorno y no se almacena en el repositorio.

El frontend guarda las credenciales únicamente en `sessionStorage`, por lo que se eliminan al cerrar la sesión del navegador.

La configuración actual es adecuada para la demostración académica, pero deberá sustituirse por autenticación basada en usuarios y roles almacenados en PostgreSQL.

## 16. Limitaciones actuales

- No existe eliminación física de clientes.
- La actualización desde el frontend se utiliza actualmente para cambiar el estado activo.
- No se ha implementado un formulario específico de edición completa.
- Los usuarios y roles reales todavía no controlan el acceso al módulo.
- Las solicitudes de servicio todavía no seleccionan clientes desde este módulo.
- La autenticación HTTP Basic es temporal para la demo.

## 17. Próximos pasos

- Implementar edición completa desde Vue.
- Asociar clientes con solicitudes de servicio.
- Implementar usuarios y roles reales.
- Incorporar paginación.
- Añadir filtros por tipo y estado.
- Incorporar pruebas unitarias específicas del servicio.
- Incorporar pruebas de integración de los endpoints.
- Sustituir la autenticación temporal por un flujo definitivo.

## 18. Responsabilidad del desarrollo

```text
Responsable: Xiomara
Módulo: Clientes institucionales
Rama: feature/clientes-xiomara
```

Commits principales:

```text
feat(clientes): implementar API de clientes institucionales
feat(clientes): integrar gestión institucional en frontend
docs(clientes): documentar módulo de clientes institucionales
```