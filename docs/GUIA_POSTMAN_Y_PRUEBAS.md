# Pruebas de la API con Postman

## ¿Postman es necesario?

Sí es útil para demostrar que **Spring Boot comunica Vue y PostgreSQL**. No es parte obligatoria de la interfaz ni reemplaza las pruebas del navegador. En la presentación, usa Postman para respaldar el funcionamiento de la API.

1. Mantén Spring Boot en el puerto 8080. Si ves `Application run failed`, la API no está lista.
2. En Postman: **Import** → selecciona `postman/Diplomatic_Fleet.postman_collection.json` y `postman/Diplomatic_Fleet_LOCAL.postman_environment.json`.
3. Selecciona el environment **Diplomatic Fleet LOCAL** y completa **Current Value / Local Value** de `adminPassword`, `clientEmail`, `clientPassword`, `nombreEntidad` sin subir contraseñas a GitHub.
4. Ejecuta **Consultar catálogo público**. Si devuelve una lista, se actualizará `vehiculoId` con el primer vehículo disponible.
5. Verifica **login ADMINISTRADOR**. Para un cliente nuevo, ejecuta **Registrar cliente institucional** solo una vez con un nombre de institución nuevo. Después ejecuta **Verificar login CLIENTE**.
6. Ejecuta **Consultar catálogo por fechas** y luego **Crear solicitud de alquiler** (genera una fila real en PostgreSQL). Se guardará automáticamente `reservaId`.
7. Ejecuta **Listar mis reservas** como cliente y **Listar solicitudes (admin)** para confirmar que ambos ven la solicitud. Finaliza con **Aprobar solicitud (admin)** o **Rechazar solicitud (admin)**, no ambos para la misma reserva.
8. Verifica el control de acceso ejecutando **Validación de roles: cliente NO accede a admin**. Debe devolver `403`.

### Interpretación

- `200 OK`: consulta u operación satisfactoria; `201 Created`: recurso nuevo.
- `400`: datos/fechas inválidos; `401`: credenciales ausentes o incorrectas; `403`: rol insuficiente; `409`: conflicto de reserva/negocio; `500`: error de backend que requiere revisar consola Spring.
- **No ejecutes la carpeta de escritura completa en Collection Runner**: sus pruebas se afectan mutuamente (por ejemplo, aprobar y después rechazar la misma solicitud), y crean datos reales.
- Si una institución ya existe, el registro de otro usuario con ese mismo nombre devuelve conflicto según el diseño actual; utiliza una institución ficticia nueva para la prueba.
- El login usa **HTTP Basic en `sessionStorage` solo en desarrollo local**. No publicar en Internet sin TLS, gestión de sesiones segura y revisión de autenticación/autoridades.

### Ejemplo mínimo sin Postman

```powershell
Invoke-RestMethod 'http://localhost:8080/api/public/vehiculos'
```

### Seguridad y privacidad

Nunca compartas ni subas al repositorio passwords, tokens, `.env` o datos reales de instituciones. Las variables de Postman incluidas están vacías a propósito.
