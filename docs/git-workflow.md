# Guía GitHub para la entrega del catálogo

Este repositorio se entrega como archivo ZIP con los cambios. El ZIP **no contiene la carpeta `.git`** del repositorio remoto original, así que debe integrarse a un clon de GitHub, **no** subirse como una carpeta nueva con historial independiente si ya existe el repositorio del equipo.

## Procedimiento recomendado para el equipo

1. En GitHub, abrir el repositorio existente de `Diplomatic-Fleet` y copiar su URL HTTPS desde **Code**. Cada integrante debe clonar ese mismo repositorio.
2. En IntelliJ IDEA, abrir el proyecto **clonado**, no el ZIP. Crear una rama a partir de la principal actualizada.
3. Copiar los archivos modificados de esta entrega **sobre la misma estructura** del repositorio clonado, conservando `.git`. No reemplazar manualmente migraciones V1–V4, que no fueron modificadas.
4. Revisar los cambios en **Git → Show Diff** antes de hacer commit. Comprobar que no se agregaron `.env`, secretos, `node_modules`, `build`, `dist`, ni datos privados reales.
5. Ejecutar pruebas e incorporar capturas de evidencia al PR, no al código si son privadas.
6. Hacer push de la rama y abrir un PR hacia `main` con la descripción de `docs/PR-catalogo-vitrina.md`.
7. Pedir revisión a otro integrante y fusionar solo cuando la compilación y la migración funcionen en una base local.

### Comandos desde terminal de IntelliJ

```bash
# En un CLON existente, con remote origin ya configurado
git checkout main
git pull origin main
git checkout -b feature/catalogo-diplomatico

# Copiar las modificaciones del ZIP sobre este clon antes del add
git status
git add .
git diff --cached --stat

git commit -m "feat(catalogo): crear vitrina de vehiculos y solicitudes de cotizacion"
git push -u origin feature/catalogo-diplomatico
```

**Comprobar que `git remote -v` apunta al repositorio correcto.** Si el remoto se llama distinto, adaptar los comandos. No hacer `git push --force` ni sobrescribir el historial principal.

### Propuesta de commits más pequeños

Si prefieren un historial más fácil de revisar:

- `feat(db): incorporar ficha comercial y contactos preliminares en Flyway`
- `feat(api): publicar catalogo y seguimiento autenticado`
- `feat(ui): mostrar vitrina Persona 5 y bandeja comercial`
- `test(catalogo): cubrir filtros y orden de vehiculos`
- `docs: documentar instalacion, limitaciones y pruebas del catalogo`

Los commits deben agrupar únicamente los archivos vinculados al cambio indicado. No crear commits sin contenido ni afirmar resultados de pruebas no ejecutadas.

## Descripción del Pull Request

Usar [`PR-catalogo-vitrina.md`](PR-catalogo-vitrina.md). Adjuntar: captura de la vitrina, solicitud recibida (datos ficticios), bandeja de contactos, resultado de `npm run type-check` y salida de `gradlew.bat test` o de `./gradlew test`.

## Revisión del profesor

Puede seguir el recorrido **Catálogo público → Ficha → Solicitud de cotización → Login de demo → Bandeja de contactos → Editar flota**. La recepción comercial no implica aceptación ni reserva; debe decirse explícitamente durante la sustentación.
