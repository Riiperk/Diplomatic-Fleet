# Arquitectura del frontend con Vue.js

## Objetivo

El frontend de Diplomatic Fleet utiliza Vue 3 como framework principal y Vite como herramienta de desarrollo y compilación.

La estructura inicial fue creada para facilitar:

- La separación de responsabilidades.
- La navegación entre vistas.
- La gestión global del estado.
- La conexión con el backend de Spring Boot.
- Las pruebas unitarias.
- La documentación y revisión del código.

## Tecnologías utilizadas

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Vitest
- Vue Test Utils
- ESLint
- Oxlint
- Prettier

## Ubicación

El proyecto frontend se encuentra en:

`frontend/`

El backend de Spring Boot permanece en:

`src/main/kotlin/`

Durante el desarrollo, ambos proyectos se ejecutan de manera independiente:

- Vue y Vite: puerto `5173`.
- Spring Boot: puerto `8080`.

## Estructura inicial

```text
frontend/
├── public/
├── src/
│   ├── __tests__/
│   │   └── App.spec.ts
│   ├── router/
│   │   └── index.ts
│   ├── App.vue
│   └── main.ts
├── env.d.ts
├── index.html
├── package.json
├── package-lock.json
├── tsconfig.json
├── vite.config.ts
└── vitest.config.ts
```

## Punto de entrada

El archivo `src/main.ts` es el punto de entrada del frontend.

Sus responsabilidades son:

1. Crear la aplicación Vue.
2. Registrar Pinia como administrador del estado global.
3. Registrar Vue Router.
4. Montar la aplicación en el elemento HTML con identificador `app`.

## Componente raíz

El archivo `src/App.vue` funciona como contenedor principal.

Utiliza `RouterView` para representar la vista asociada a la ruta activa. Las pantallas del sistema no deben implementarse directamente dentro de `App.vue`.

Cada módulo debe utilizar vistas y componentes independientes.

## Enrutamiento

El archivo `src/router/index.ts` centraliza las rutas del sistema.

Las rutas previstas incluyen:

```text
/login
/dashboard
/vehiculos
/solicitudes
/mantenimientos
/usuarios
```

Las rutas se añadirán cuando las vistas correspondientes estén implementadas y probadas.

## Gestión del estado

Pinia administrará información compartida entre componentes, incluyendo:

- Usuario autenticado.
- Roles y permisos.
- Estado de la sesión.
- Datos que deban mantenerse entre diferentes vistas.

Los almacenes de Pinia se ubicarán en:

```text
frontend/src/stores/
```

No deben almacenarse contraseñas en Pinia, `localStorage` ni `sessionStorage`.

## Componentes Vue

Los componentes reutilizables se ubicarán en:

```text
frontend/src/components/
```

Cada componente debe:

- Tener una responsabilidad concreta.
- Declarar sus propiedades mediante TypeScript.
- Documentar eventos y funciones relevantes.
- Evitar lógica de negocio compleja dentro de la plantilla.
- Incluir pruebas cuando tenga comportamiento propio.

## Vistas

Las vistas se ubicarán en:

```text
frontend/src/views/
```

Cada vista representa una pantalla asociada a una ruta.

La pantalla de inicio de sesión será migrada por Camila desde el prototipo estático existente hacia componentes Vue.

## Servicios HTTP

La comunicación con Spring Boot se centralizará en:

```text
frontend/src/services/
```

Los servicios serán responsables de:

- Construir solicitudes HTTP.
- Enviar datos al backend.
- Interpretar respuestas.
- Transformar errores técnicos en resultados controlados.

Los componentes no deben repetir llamadas HTTP directamente.

## Variables de entorno

Las variables públicas del frontend utilizarán el prefijo:

```text
VITE_
```

Ejemplo:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
```

No deben almacenarse contraseñas, tokens privados ni secretos del backend en variables de Vite, porque pueden quedar visibles en la compilación del frontend.

## Documentación dentro del código

El código debe documentar especialmente:

- Servicios.
- Funciones con parámetros.
- Valores retornados.
- Errores controlados.
- Reglas de negocio.
- Propiedades y eventos de componentes.
- Protección de rutas.
- Transformaciones de datos.

No deben añadirse comentarios que solo repitan instrucciones evidentes.

Ejemplo:

```typescript
/**
 * Envía las credenciales al endpoint de autenticación.
 *
 * @param credentials Correo y contraseña introducidos por el usuario.
 * @returns Información de la sesión autenticada.
 * @throws Error cuando el backend rechaza las credenciales.
 */
async function login(credentials: LoginCredentials): Promise<AuthSession> {
  // Integración pendiente con Spring Boot.
}
```

## Pruebas

Vitest y Vue Test Utils se utilizan para las pruebas unitarias y de componentes.

La prueba inicial se encuentra en:

```text
frontend/src/__tests__/App.spec.ts
```

La prueba comprueba que `App.vue` representa correctamente el espacio reservado para las vistas del Router.

Comando utilizado:

```powershell
npm run test:unit -- --run
```

Resultado inicial:

```text
1 archivo de pruebas superado
1 prueba superada
```

## Calidad del código

### Formato

```powershell
npm run format
```

### Análisis estático

```powershell
npm run lint
```

Resultado inicial:

```text
0 advertencias
0 errores
```

### Compilación

```powershell
npm run build
```

La compilación realiza:

1. Comprobación de tipos con `vue-tsc`.
2. Compilación optimizada con Vite.
3. Generación de archivos en `frontend/dist/`.

La carpeta `dist/` no debe confirmarse en Git.

## Auditoría de dependencias

La auditoría completa detectó alertas en herramientas de desarrollo relacionadas con ESLint.

La auditoría de dependencias de producción se ejecutó con:

```powershell
npm audit --omit=dev
```

Resultado:

```text
0 vulnerabilidades en dependencias de producción
```

No se utilizó `npm audit fix --force`, porque podía introducir cambios incompatibles en las herramientas configuradas.

## Archivos ignorados

El archivo `.gitignore` del frontend excluye:

```text
node_modules/
dist/
.eslintcache
.vscode/
```

Estos archivos no deben subirse al repositorio.

## Flujo de trabajo en GitHub

Cada módulo debe desarrollarse en una rama independiente.

Los pull requests deben incluir:

- Objetivo del cambio.
- Archivos modificados.
- Decisiones técnicas.
- Instrucciones de prueba.
- Resultados de las pruebas.
- Evidencias visuales cuando corresponda.
- Documentación actualizada.
- Revisión de otra persona del equipo.

Los comentarios de revisión deben explicar problemas, decisiones o mejoras concretas sobre las líneas de código correspondientes.

## Responsabilidades iniciales

### Bryan

- Creación de la base Vue.
- Configuración de TypeScript.
- Configuración de Vue Router.
- Configuración de Pinia.
- Configuración de Vitest.
- Configuración de ESLint, Oxlint y Prettier.
- Integración posterior con Spring Boot.

### Camila

- Migración del diseño del inicio de sesión.
- Creación de componentes visuales.
- Modales legales.
- Diseño adaptable.
- Pruebas del formulario y sus validaciones.

## Estado actual

La base técnica del frontend:

- Inicia correctamente con Vite.
- Supera el análisis estático.
- Supera la prueba unitaria inicial.
- Compila correctamente para producción.
- No presenta vulnerabilidades en dependencias de producción.
