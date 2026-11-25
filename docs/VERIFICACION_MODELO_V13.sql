-- Comprobacion del modelo docente. Solo lectura; ejecutar tras V13 en pgAdmin.
WITH esperado(nombre) AS (VALUES
 ('pais'),
 ('ciudad'),
 ('persona'),
 ('persona_direccion'),
 ('taller'),
 ('ubicacion'),
 ('cat_tipo_documento'),
 ('cat_genero'),
 ('cat_tipo_doc_persona'),
 ('persona_documento'),
 ('cat_tipo_cliente'),
 ('cliente_diplomatico'),
 ('cat_tipo_mision'),
 ('cat_rango_diplomatico'),
 ('cat_estado_acreditacion'),
 ('cat_categoria_licencia'),
 ('conductor'),
 ('cat_tipo_vehiculo'),
 ('vehiculo'),
 ('cat_nivel_blindaje'),
 ('cat_estado_admin'),
 ('cat_estado_operativo'),
 ('cat_tipo_mantenimiento'),
 ('mantenimiento'),
 ('cat_estado_mant'),
 ('cat_tipo_baja'),
 ('baja_vehiculo'),
 ('cat_tipo_servicio'),
 ('reserva'),
 ('cat_estado_reserva'),
 ('cat_perfil'),
 ('usuario'),
 ('persona_contacto')
)
SELECT e.nombre,t.table_type AS implementacion,
       CASE WHEN t.table_name IS NULL THEN 'FALTA' ELSE 'PRESENTE' END AS resultado
FROM esperado e LEFT JOIN information_schema.tables t
  ON t.table_schema='public' AND t.table_name=e.nombre
ORDER BY e.nombre;

-- Coherencia de los vínculos en la base operativa; todos los faltantes deben ser 0.
SELECT (SELECT COUNT(*) FROM usuarios WHERE persona_id IS NULL) AS usuarios_sin_persona,
       (SELECT COUNT(*) FROM conductores_autorizados WHERE persona_id IS NULL) AS conductores_sin_persona,
       (SELECT COUNT(*) FROM solicitudes WHERE ubicacion_origen_id IS NULL OR ubicacion_destino_id IS NULL) AS reservas_sin_ubicacion,
       (SELECT COUNT(*) FROM clientes_institucionales c LEFT JOIN cliente_diplomatico d ON d.id_cliente=c.id WHERE d.id_cliente IS NULL) AS instituciones_sin_perfil;
SELECT version,description,success FROM flyway_schema_history ORDER BY installed_rank;
-- Los datos anteriores siguen en sus tablas de origen; no consultar contraseñas ni tokens.
SELECT (SELECT COUNT(*) FROM vehiculos) AS vehiculos,
       (SELECT COUNT(*) FROM clientes_institucionales) AS clientes,
       (SELECT COUNT(*) FROM solicitudes) AS reservas,
       (SELECT COUNT(*) FROM usuarios) AS usuarios;
