# Inventario de 33 entidades del diagrama del profesor

**Fuente:** `Diag_Clases_20261117.svg`, diagrama ER entregado por el usuario. Los nombres y campos se extrajeron de sus etiquetas SVG, sin reemplazarlos por un modelo inventado.

La solución existente (primera entrega) utiliza tablas y cardinalidades distintas. La tabla siguiente distingue correspondencia **aproximada** de implementación idéntica. **No se afirma cobertura completa del diagrama.**

| # | Entidad en el ER | Implementación disponible |
|---:|---|---|
| 1 | `pais` | No implementada en la base operativa |
| 2 | `ciudad` | No implementada en la base operativa |
| 3 | `persona` | usuarios (solo nombre/apellido/correo) |
| 4 | `persona_direccion` | No implementada en la base operativa |
| 5 | `taller` | No implementada en la base operativa |
| 6 | `ubicacion` | No implementada en la base operativa |
| 7 | `cat_tipo_documento` | documentos_vehiculo.tipo_documento |
| 8 | `cat_genero` | No implementada en la base operativa |
| 9 | `cat_tipo_doc_persona` | conductores_autorizados.tipo_documento |
| 10 | `persona_documento` | No implementada en la base operativa |
| 11 | `cat_tipo_cliente` | clientes_institucionales.tipo_entidad |
| 12 | `cliente_diplomatico` | clientes_institucionales (solo institución, no persona) |
| 13 | `cat_tipo_mision` | No implementada en la base operativa |
| 14 | `cat_rango_diplomatico` | No implementada en la base operativa |
| 15 | `cat_estado_acreditacion` | No implementada en la base operativa |
| 16 | `cat_categoria_licencia` | conductores_autorizados.categoria_licencia |
| 17 | `conductor` | conductores_autorizados (modelado institucional) |
| 18 | `cat_tipo_vehiculo` | categorias_vehiculo |
| 19 | `vehiculo` | vehiculos |
| 20 | `cat_nivel_blindaje` | No implementada en la base operativa |
| 21 | `cat_estado_admin` | vehiculos.activo |
| 22 | `cat_estado_operativo` | vehiculos.estado |
| 23 | `cat_tipo_mantenimiento` | mantenimientos.tipo |
| 24 | `mantenimiento` | mantenimientos |
| 25 | `cat_estado_mant` | mantenimientos.estado |
| 26 | `cat_tipo_baja` | No implementada en la base operativa |
| 27 | `baja_vehiculo` | No implementada en la base operativa |
| 28 | `cat_tipo_servicio` | solicitudes.modalidad |
| 29 | `reserva` | solicitudes (sin conductor) |
| 30 | `cat_estado_reserva` | solicitudes.estado |
| 31 | `cat_perfil` | roles |
| 32 | `usuario` | usuarios + usuarios_roles |
| 33 | `persona_contacto` | No implementada en la base operativa |

## Campos visibles en el diagrama SVG

En los catálogos mostrados únicamente con el nombre de la entidad, **el SVG no aporta definición de columnas**. No se inventan columnas faltantes.

### `pais`

*La imagen del profesor no detalla atributos para esta entidad.*

### `ciudad`

*La imagen del profesor no detalla atributos para esta entidad.*

### `persona`

- `varchar id_persona PK`
- `varchar pri_nom_per`
- `varchar seg_nom_per`
- `varchar pri_ape_per`
- `varchar seg_ape_per`
- `date fech_nac_per`
- `varchar fk_id_tip_doc FK`
- `varchar documento_per UK`
- `varchar fk_id_gen FK`
- `varchar fk_id_ciu FK`
- `tinyint act_per`

### `persona_direccion`

- `varchar id_direccion PK`
- `varchar fk_id_persona FK`
- `varchar dir_fisica`
- `varchar fk_id_ciu FK`
- `tinyint es_principal`
- `tinyint act_direccion`

### `taller`

- `varchar id_taller PK`
- `varchar nom_taller UK`
- `varchar nit_taller UK`
- `varchar dir_taller`
- `varchar fk_id_ciu FK`
- `varchar tel_taller`
- `tinyint act_taller`

### `ubicacion`

- `varchar id_ubicacion PK`
- `varchar nom_ubicacion UK`
- `varchar tipo_ubicacion`
- `varchar dir_ubicacion`
- `varchar fk_id_ciu FK`
- `tinyint act_ubicacion`

### `cat_tipo_documento`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_genero`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_tipo_doc_persona`

*La imagen del profesor no detalla atributos para esta entidad.*

### `persona_documento`

- `varchar id_doc_persona PK`
- `varchar fk_id_persona FK`
- `varchar fk_id_tip_doc_per FK`
- `varchar num_doc`
- `date fech_expedicion`
- `date fech_vencimiento`
- `varchar archivo_url`
- `tinyint act_doc`

### `cat_tipo_cliente`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cliente_diplomatico`

- `varchar id_cliente PK`
- `varchar fk_id_persona FK`
- `varchar fk_id_tip_cli FK`
- `varchar fk_id_tip_mision FK`
- `varchar nom_mision`
- `varchar fk_id_pais_acred FK`
- `varchar fk_id_rango FK`
- `varchar num_cedula_diplo`
- `date fech_acreditacion`
- `date fech_venc_acred`
- `varchar fk_id_est_acred FK`
- `tinyint act_cliente`

### `cat_tipo_mision`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_rango_diplomatico`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_estado_acreditacion`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_categoria_licencia`

*La imagen del profesor no detalla atributos para esta entidad.*

### `conductor`

- `varchar id_conductor PK`
- `varchar fk_id_persona FK`
- `varchar num_licencia UK`
- `varchar fk_id_cat_lic FK`
- `date fech_venc_licencia`
- `tinyint es_diplomatico`
- `tinyint act_conductor`

### `cat_tipo_vehiculo`

*La imagen del profesor no detalla atributos para esta entidad.*

### `vehiculo`

- `varchar id_vehiculo PK`
- `varchar placa UK`
- `varchar marca`
- `varchar modelo`
- `int anio`
- `varchar fk_id_tip_veh FK`
- `varchar fk_id_nivel_blindaje FK`
- `varchar fk_id_est_admin FK`
- `varchar fk_id_est_op FK`
- `date fecha_baja`
- `varchar motivo_baja`
- `varchar fk_id_proveedor FK`

### `cat_nivel_blindaje`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_estado_admin`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_estado_operativo`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_tipo_mantenimiento`

*La imagen del profesor no detalla atributos para esta entidad.*

### `mantenimiento`

- `varchar id_mantenimiento PK`
- `varchar fk_id_vehiculo FK`
- `varchar fk_id_tip_mant FK`
- `varchar fk_id_taller FK`
- `date fecha_inicio`
- `date fecha_fin`
- `decimal costo`
- `varchar descripcion`
- `varchar fk_id_est_mant FK`
- `varchar fk_id_usu FK`

### `cat_estado_mant`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_tipo_baja`

*La imagen del profesor no detalla atributos para esta entidad.*

### `baja_vehiculo`

- `varchar id_baja PK`
- `varchar fk_id_vehiculo FK`
- `date fecha_baja`
- `varchar motivo`
- `varchar fk_id_tip_baja FK`
- `varchar fk_id_usu FK`
- `varchar observaciones`

### `cat_tipo_servicio`

*La imagen del profesor no detalla atributos para esta entidad.*

### `reserva`

- `varchar id_reserva PK`
- `varchar fk_id_cliente FK`
- `varchar fk_id_vehiculo FK`
- `varchar fk_id_conductor FK`
- `varchar fk_id_ubic_origen FK`
- `varchar fk_id_ubic_destino FK`
- `datetime fecha_hora_inicio`
- `datetime fecha_hora_fin`
- `varchar fk_id_tip_serv FK`
- `tinyint requiere_blindaje`
- `varchar fk_id_est_res FK`
- `tinyint act_reserva`

### `cat_estado_reserva`

*La imagen del profesor no detalla atributos para esta entidad.*

### `cat_perfil`

*La imagen del profesor no detalla atributos para esta entidad.*

### `usuario`

- `varchar id_usu PK`
- `varchar nom_usu UK`
- `varchar pass_usu`
- `varchar fk_id_persona FK`
- `varchar fk_id_perf FK`
- `tinyint act_usu`
- `datetime fech_creacion`

### `persona_contacto`

- `varchar id_contacto PK`
- `varchar fk_id_persona FK`
- `varchar tipo_contacto`
- `varchar valor_contacto UK`
- `tinyint es_principal`
- `tinyint act_contacto`

## Correspondencias funcionales existentes

- **Cliente:** institución, usuario de contacto y solicitudes en V1, V3 y V7.
- **Vehículo:** modelo y categoría en V2, tarifa e imagen en V5 y flota ficticia en V8.
- **Reserva:** flujo de solicitud sin conductor; se validan fechas, capacidad y superposición temporal.
- **Mantenimiento, conductor, usuarios y cotizaciones:** usan tablas de V1–V3; panel administrativo disponible tras la ampliación.
- **Pendiente de homologación:** persona normalizada, cliente diplomático individual, acreditación, geografía, taller como entidad, bajas, estados en tablas catálogo y servicio con conductor.

## Recomendación de migración posterior

La forma segura de llegar a una correspondencia completa con el ER consiste en diseñar V9+ que normalice `persona`, `ubicacion`, `cliente_diplomatico` y `reserva`, con traslado reversible de datos, claves foráneas y pruebas de migración. **No ejecutar DDL masivo inventado sobre la base usada en la entrega.**
