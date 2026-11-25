# Matriz de trazabilidad del diagrama docente — V13

Fuente: `diagramas/Diagrama_Profesor_V13_original.svg`, copia exacta del SVG suministrado por el docente el 17 de noviembre de 2026. Los atributos del original se transcriben en `INVENTARIO_DIAGRAMA_PROFESOR_33_ENTIDADES.md`. Esta matriz sustituye el diagnóstico anterior de cobertura V10; no sustituye el original. La presencia se verificó estáticamente en el DDL, no mediante una instancia PostgreSQL ejecutada en el entorno de generación.

| N.º | Entidad docente | Representación | Integración funcional |
|---:|---|---|---|
| 1 | pais | Tabla nueva | Países; país acreditante y ciudades |
| 2 | ciudad | Tabla nueva | Ciudad, vínculo a país, referencia en personas y talleres |
| 3 | persona | Tabla nueva | Cuenta, contacto institucional y conductor; edición de datos personales complementarios |
| 4 | persona_direccion | Tabla nueva | Dirección institucional inicial y gestión de direcciones |
| 5 | taller | Tabla nueva | Talleres; vinculación automática de proveedor de mantenimiento |
| 6 | ubicacion | Tabla nueva | Origen/destino de cada reserva; sugerencias públicas optativas |
| 7 | cat_tipo_documento | Tabla nueva | Tipos de documento y FK en documentos vehiculares y persona |
| 8 | cat_genero | Tabla nueva | Clasificación optativa de persona |
| 9 | cat_tipo_doc_persona | Tabla nueva | Tipos de documento de identidad |
| 10 | persona_documento | Tabla nueva | Identificación, fechas y referencia de archivo |
| 11 | cat_tipo_cliente | Tabla nueva | Tipo de contacto diplomático/institucional |
| 12 | cliente_diplomatico | Tabla nueva | Perfil diplomático del contacto responsable de institución; acreditación, rango, país y vigencia |
| 13 | cat_tipo_mision | Tabla nueva | Clasificación de institución; FK en perfil diplomático |
| 14 | cat_rango_diplomatico | Tabla nueva | Rango declarado en acreditación |
| 15 | cat_estado_acreditacion | Tabla nueva | Estado y vigencia efectiva |
| 16 | cat_categoria_licencia | Tabla nueva | FK en ambos tipos de conductor |
| 17 | conductor | Vista sobre conductores_autorizados y conductores_profesionales | Registro/autorización, licencia y asignación; indicador diplomático |
| 18 | cat_tipo_vehiculo | Tabla nueva | Tipo físico de vehículo, distinto de categoría comercial |
| 19 | vehiculo | Vista sobre vehiculos y baja_vehiculo | Flota, estado, blindaje, tipo, proveedor y baja |
| 20 | cat_nivel_blindaje | Tabla nueva | Nivel declarado; filtro y restricción de reserva |
| 21 | cat_estado_admin | Tabla nueva | Activación y baja definitiva |
| 22 | cat_estado_operativo | Tabla nueva | FK de estado operativo |
| 23 | cat_tipo_mantenimiento | Tabla nueva | FK de tipo preventivo/correctivo |
| 24 | mantenimiento | Vista sobre mantenimientos | Historial, taller, usuario responsable y costo |
| 25 | cat_estado_mant | Tabla nueva | FK de estados de mantenimiento |
| 26 | cat_tipo_baja | Tabla nueva | Motivo clasificado de baja |
| 27 | baja_vehiculo | Tabla nueva | Baja definitiva con fecha, motivo, observación y autor |
| 28 | cat_tipo_servicio | Tabla nueva | FK de modalidad de reserva |
| 29 | reserva | Vista sobre solicitudes | Ciclo completo, origen/destino, vehículo, conductor y blindaje |
| 30 | cat_estado_reserva | Tabla nueva | FK de estados del ciclo |
| 31 | cat_perfil | Vista sobre roles | Catálogo de permisos reales; asociación usuarios_roles |
| 32 | usuario | Vista sobre usuarios y usuarios_roles | Cuenta, persona, perfil principal y hash BCrypt; no se expone en API de dominio |
| 33 | persona_contacto | Tabla nueva | Correo, teléfono y otros datos de contacto |

## Relaciones y diferencias deliberadas

- 27 nombres del diagrama corresponden a tablas físicas nuevas, 6 a vistas sobre tablas físicas ya utilizadas. Las vistas no poseen PK/FK independientes; las restricciones se encuentran en sus tablas base. No se duplican reservas, vehículos ni cuentas. En `usuario` se representa un perfil por proyección; el modelo operativo conserva la asignación de múltiples roles mediante `usuarios_roles`.
- Los identificadores históricos de vehículos, solicitudes, talleres, ciudades, contactos y cuentas se conservan; no todos son VARCHAR como en el dibujo. Las marcas de tiempo operativas conservan zona horaria y los indicadores son BOOLEAN, equivalencia PostgreSQL de los indicadores TINYINT del dibujo. Las columnas de los catálogos sin detalle se definen con código/nombre/activo. El proveedor auxiliar es una decisión adicional para materializar `fk_id_proveedor`, cuyo destino no aparece en las 33 entidades originales.
- El cliente contractual sigue siendo institucional; el perfil `cliente_diplomatico` representa su contacto responsable, no cada persona diplomática de forma independiente. Las relaciones a conductores se mantienen mediante dos FK en `solicitudes`. Los nombres históricos que no están separados por componentes se preservan sin inferir segundos nombres o apellidos. Las ciudades históricas cuyo país no se conoce quedan sin país hasta completar el dato. Un contacto duplicado en el legado no se clona en `persona_contacto`, que exige valor único.
- Las credenciales se guardan con BCrypt; el esquema de sesiones guarda solo el digest del token. La vista SQL `usuario` incluye el hash por correspondencia con `pass_usu`, por lo que no debe publicarse ni consultarse como parte de una captura de sustentación.
- Las acreditaciones, licencias y niveles de blindaje son registros administrativos, no validaciones externas o certificaciones oficiales. La acreditación vencida se calcula para consulta; no se impone un requisito de acreditación sobre todas las reservas históricas. Las bajas históricas desconocidas no se inventan.
- Esta ampliación no equivale a una réplica literal de la estructura docente. Si el docente exige exactamente 33 tablas físicas con los nombres, tipos y cardinalidades del dibujo, se requiere una migración adicional de sustitución de tablas y pruebas de compatibilidad. Tampoco se declaran implementadas pasarelas, factura DIAN o depósitos/recargos que no forman parte de este ER.

La consulta `VERIFICACION_MODELO_V13.sql` comprueba presencia, tipo de objeto, vínculos básicos y Flyway tras instalar V13. Debe complementarse con pruebas de escritura y revisión de todas las cardinalidades.
