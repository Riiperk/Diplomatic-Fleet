-- V13: extensión relacional del diagrama ER del profesor.
-- Mantiene las tablas operativas V1-V12 como fuente única de reservas y cuentas.
-- Los atributos desconocidos quedan NULL; no se inventan acreditaciones ni blindaje.
-- Catálogos sin atributos en el SVG: código estable, nombre y estado activo.

CREATE TABLE cat_tipo_documento (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_documento(codigo,nombre) VALUES
 ('SOAT','Soat'),
 ('REVISION_TECNOMECANICA','Revision Tecnomecanica'),
 ('TARJETA_PROPIEDAD','Tarjeta Propiedad'),
 ('POLIZA','Poliza'),
 ('OTRO','Otro');
COMMENT ON TABLE cat_tipo_documento IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_genero (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_genero(codigo,nombre) VALUES
 ('NO_DECLARADO','No Declarado'),
 ('FEMENINO','Femenino'),
 ('MASCULINO','Masculino'),
 ('OTRO','Otro');
COMMENT ON TABLE cat_genero IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_tipo_doc_persona (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_doc_persona(codigo,nombre) VALUES
 ('CEDULA_CIUDADANIA','Cedula Ciudadania'),
 ('CEDULA_EXTRANJERIA','Cedula Extranjeria'),
 ('PASAPORTE','Pasaporte'),
 ('CEDULA_DIPLOMATICA','Cedula Diplomatica'),
 ('OTRO','Otro');
COMMENT ON TABLE cat_tipo_doc_persona IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_tipo_cliente (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_cliente(codigo,nombre) VALUES
 ('INSTITUCIONAL','Institucional'),
 ('DIPLOMATICO','Diplomatico');
COMMENT ON TABLE cat_tipo_cliente IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_tipo_mision (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_mision(codigo,nombre) VALUES
 ('EMBAJADA','Embajada'),
 ('CONSULADO','Consulado'),
 ('ORGANISMO_INTERNACIONAL','Organismo Internacional'),
 ('OTRO','Otro');
COMMENT ON TABLE cat_tipo_mision IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_rango_diplomatico (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_rango_diplomatico(codigo,nombre) VALUES
 ('NO_DECLARADO','No Declarado'),
 ('EMBAJADOR','Embajador'),
 ('CONSUL','Consul'),
 ('CONSEJERO','Consejero'),
 ('SECRETARIO','Secretario'),
 ('AGREGADO','Agregado'),
 ('OTRO','Otro');
COMMENT ON TABLE cat_rango_diplomatico IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_estado_acreditacion (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_estado_acreditacion(codigo,nombre) VALUES
 ('PENDIENTE','Pendiente'),
 ('VIGENTE','Vigente'),
 ('VENCIDA','Vencida'),
 ('REVOCADA','Revocada');
COMMENT ON TABLE cat_estado_acreditacion IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_categoria_licencia (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_categoria_licencia(codigo,nombre) VALUES
 ('A1','A1'),
 ('A2','A2'),
 ('B1','B1'),
 ('B2','B2'),
 ('B3','B3'),
 ('C1','C1'),
 ('C2','C2'),
 ('C3','C3');
COMMENT ON TABLE cat_categoria_licencia IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_nivel_blindaje (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_nivel_blindaje(codigo,nombre) VALUES
 ('NO_DECLARADO','No Declarado'),
 ('SIN_BLINDAJE','Sin Blindaje'),
 ('NIVEL_I','Nivel I'),
 ('NIVEL_II','Nivel Ii'),
 ('NIVEL_III','Nivel Iii'),
 ('NIVEL_IIIA','Nivel Iiia'),
 ('NIVEL_IV','Nivel Iv'),
 ('NIVEL_V','Nivel V'),
 ('NIVEL_VI','Nivel Vi');
COMMENT ON TABLE cat_nivel_blindaje IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_estado_admin (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_estado_admin(codigo,nombre) VALUES
 ('ACTIVO','Activo'),
 ('INACTIVO','Inactivo'),
 ('BAJA','Baja');
COMMENT ON TABLE cat_estado_admin IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_estado_operativo (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_estado_operativo(codigo,nombre) VALUES
 ('DISPONIBLE','Disponible'),
 ('RESERVADO','Reservado'),
 ('EN_SERVICIO','En Servicio'),
 ('EN_MANTENIMIENTO','En Mantenimiento'),
 ('INACTIVO','Inactivo');
COMMENT ON TABLE cat_estado_operativo IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_tipo_mantenimiento (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_mantenimiento(codigo,nombre) VALUES
 ('PREVENTIVO','Preventivo'),
 ('CORRECTIVO','Correctivo');
COMMENT ON TABLE cat_tipo_mantenimiento IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_estado_mant (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_estado_mant(codigo,nombre) VALUES
 ('PROGRAMADO','Programado'),
 ('EN_PROCESO','En Proceso'),
 ('FINALIZADO','Finalizado'),
 ('CANCELADO','Cancelado');
COMMENT ON TABLE cat_estado_mant IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_tipo_baja (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_baja(codigo,nombre) VALUES
 ('VENTA','Venta'),
 ('SINIESTRO','Siniestro'),
 ('FIN_VIDA_UTIL','Fin Vida Util'),
 ('OTRO','Otro');
COMMENT ON TABLE cat_tipo_baja IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_tipo_servicio (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_servicio(codigo,nombre) VALUES
 ('SIN_CONDUCTOR','Sin Conductor'),
 ('CON_CONDUCTOR','Con Conductor');
COMMENT ON TABLE cat_tipo_servicio IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';

CREATE TABLE cat_estado_reserva (codigo VARCHAR(50) PRIMARY KEY, nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_estado_reserva(codigo,nombre) VALUES
 ('RECIBIDA','Recibida'),
 ('EN_REVISION','En Revision'),
 ('COTIZADA','Cotizada'),
 ('PENDIENTE_APROBACION','Pendiente Aprobacion'),
 ('CONFIRMADA','Confirmada'),
 ('ASIGNADA','Asignada'),
 ('EN_SERVICIO','En Servicio'),
 ('FINALIZADA','Finalizada'),
 ('RECHAZADA','Rechazada'),
 ('CANCELADA','Cancelada'),
 ('ARCHIVADA','Archivada');
COMMENT ON TABLE cat_estado_reserva IS 'Catálogo normalizado del diagrama; códigos internos de la práctica.';


-- El tipo de carrocería no equivale a la categoría comercial de alquiler.
CREATE TABLE cat_tipo_vehiculo(codigo VARCHAR(50) PRIMARY KEY,nombre VARCHAR(100) NOT NULL,activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO cat_tipo_vehiculo(codigo,nombre) VALUES ('NO_DECLARADO','No declarado'),('SEDAN','Sedán'),('SUV','SUV'),('VAN','Van'),('HATCHBACK','Hatchback'),('PICKUP','Pickup'),('OTRO','Otro');
-- El SVG señala fk_id_proveedor pero no define la entidad. Se añade un catálogo
-- auxiliar explícito para garantizar una referencia real sin inventar proveedores.
CREATE TABLE proveedor(id_proveedor BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, nombre VARCHAR(150) NOT NULL UNIQUE, activo BOOLEAN NOT NULL DEFAULT TRUE);

-- Incorporar categorías de licencia históricas sin alterar su contenido.
INSERT INTO cat_categoria_licencia(codigo,nombre)
 SELECT DISTINCT categoria_licencia,categoria_licencia FROM conductores_autorizados WHERE categoria_licencia IS NOT NULL ON CONFLICT DO NOTHING;
INSERT INTO cat_categoria_licencia(codigo,nombre)
 SELECT DISTINCT categoria_licencia,categoria_licencia FROM conductores_profesionales ON CONFLICT DO NOTHING;

-- Geografía y persona: campos definidos por el profesor. Los identificadores
-- textuales de persona se derivan de la fuente histórica, sin exponer documentos.
CREATE TABLE pais(id_pais VARCHAR(10) PRIMARY KEY, nombre VARCHAR(100) NOT NULL UNIQUE, activo BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO pais VALUES('CO','Colombia',TRUE);
CREATE TABLE ciudad(id_ciudad BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, fk_id_pais VARCHAR(10) REFERENCES pais(id_pais), nombre VARCHAR(100) NOT NULL, activo BOOLEAN NOT NULL DEFAULT TRUE);
CREATE UNIQUE INDEX uq_ciudad_nombre_pais ON ciudad((COALESCE(fk_id_pais,'')),lower(trim(nombre)));
CREATE TABLE persona(
 id_persona VARCHAR(80) PRIMARY KEY, pri_nom_per VARCHAR(140) NOT NULL, seg_nom_per VARCHAR(100), pri_ape_per VARCHAR(100), seg_ape_per VARCHAR(100),
 fech_nac_per DATE, fk_id_tip_doc VARCHAR(50) REFERENCES cat_tipo_documento(codigo), documento_per VARCHAR(80) UNIQUE,
 fk_id_gen VARCHAR(50) REFERENCES cat_genero(codigo), fk_id_ciu BIGINT REFERENCES ciudad(id_ciudad), act_per BOOLEAN NOT NULL DEFAULT TRUE
);
-- El catálogo de documento de persona también admite los códigos de identidad.
INSERT INTO cat_tipo_documento(codigo,nombre) SELECT codigo,nombre FROM cat_tipo_doc_persona ON CONFLICT DO NOTHING;
CREATE TABLE persona_direccion(id_direccion VARCHAR(100) PRIMARY KEY, fk_id_persona VARCHAR(80) NOT NULL REFERENCES persona(id_persona), dir_fisica VARCHAR(255) NOT NULL, fk_id_ciu BIGINT REFERENCES ciudad(id_ciudad), es_principal BOOLEAN NOT NULL DEFAULT TRUE, act_direccion BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE persona_documento(id_doc_persona BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, fk_id_persona VARCHAR(80) NOT NULL REFERENCES persona(id_persona), fk_id_tip_doc_per VARCHAR(50) NOT NULL REFERENCES cat_tipo_doc_persona(codigo), num_doc VARCHAR(80) NOT NULL, fech_expedicion DATE, fech_vencimiento DATE, archivo_url VARCHAR(800), act_doc BOOLEAN NOT NULL DEFAULT TRUE, UNIQUE(fk_id_persona,fk_id_tip_doc_per,num_doc), CHECK(fech_vencimiento IS NULL OR fech_expedicion IS NULL OR fech_vencimiento>=fech_expedicion));
CREATE TABLE persona_contacto(id_contacto VARCHAR(140) PRIMARY KEY, fk_id_persona VARCHAR(80) NOT NULL REFERENCES persona(id_persona), tipo_contacto VARCHAR(30) NOT NULL, valor_contacto VARCHAR(255) NOT NULL UNIQUE, es_principal BOOLEAN NOT NULL DEFAULT TRUE, act_contacto BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE taller(id_taller BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, nom_taller VARCHAR(150) NOT NULL UNIQUE, nit_taller VARCHAR(40) UNIQUE, dir_taller VARCHAR(255), fk_id_ciu BIGINT REFERENCES ciudad(id_ciudad), tel_taller VARCHAR(30), act_taller BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE ubicacion(id_ubicacion BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, nom_ubicacion VARCHAR(255) NOT NULL UNIQUE, tipo_ubicacion VARCHAR(50), dir_ubicacion VARCHAR(255), fk_id_ciu BIGINT REFERENCES ciudad(id_ciudad), act_ubicacion BOOLEAN NOT NULL DEFAULT TRUE, publica BOOLEAN NOT NULL DEFAULT FALSE);
-- La institución sigue siendo propietaria contractual; el perfil diplomático es
-- su contacto responsable, no una acreditación automática de todo usuario.
CREATE TABLE cliente_diplomatico(
 id_cliente BIGINT PRIMARY KEY REFERENCES clientes_institucionales(id), fk_id_persona VARCHAR(80) NOT NULL UNIQUE REFERENCES persona(id_persona),
 fk_id_tip_cli VARCHAR(50) NOT NULL DEFAULT 'INSTITUCIONAL' REFERENCES cat_tipo_cliente(codigo), fk_id_tip_mision VARCHAR(50) NOT NULL REFERENCES cat_tipo_mision(codigo), nom_mision VARCHAR(150) NOT NULL,
 fk_id_pais_acred VARCHAR(10) REFERENCES pais(id_pais), fk_id_rango VARCHAR(50) REFERENCES cat_rango_diplomatico(codigo), num_cedula_diplo VARCHAR(80), fech_acreditacion DATE, fech_venc_acred DATE,
 fk_id_est_acred VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE' REFERENCES cat_estado_acreditacion(codigo), act_cliente BOOLEAN NOT NULL DEFAULT TRUE,
 CHECK(fech_venc_acred IS NULL OR fech_acreditacion IS NULL OR fech_venc_acred>=fech_acreditacion),
 CHECK(fk_id_est_acred<>'VIGENTE' OR (num_cedula_diplo IS NOT NULL AND fk_id_pais_acred IS NOT NULL AND fk_id_rango IS NOT NULL AND fech_acreditacion IS NOT NULL AND fech_venc_acred IS NOT NULL))
);
ALTER TABLE usuarios ADD COLUMN persona_id VARCHAR(80) UNIQUE REFERENCES persona(id_persona);
ALTER TABLE conductores_autorizados ADD COLUMN persona_id VARCHAR(80) REFERENCES persona(id_persona), ADD COLUMN es_diplomatico BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE conductores_profesionales ADD COLUMN es_diplomatico BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE conductores_autorizados ADD FOREIGN KEY(categoria_licencia) REFERENCES cat_categoria_licencia(codigo);
ALTER TABLE conductores_profesionales ADD FOREIGN KEY(categoria_licencia) REFERENCES cat_categoria_licencia(codigo);
ALTER TABLE vehiculos ADD COLUMN nivel_blindaje VARCHAR(50) NOT NULL DEFAULT 'NO_DECLARADO' REFERENCES cat_nivel_blindaje(codigo), ADD COLUMN estado_admin VARCHAR(50) NOT NULL DEFAULT 'ACTIVO' REFERENCES cat_estado_admin(codigo), ADD COLUMN tipo_vehiculo VARCHAR(50) NOT NULL DEFAULT 'NO_DECLARADO' REFERENCES cat_tipo_vehiculo(codigo), ADD COLUMN proveedor_id BIGINT REFERENCES proveedor(id_proveedor), ADD FOREIGN KEY(estado) REFERENCES cat_estado_operativo(codigo);
ALTER TABLE vehiculos ADD CONSTRAINT chk_df13_baja_estado CHECK(estado_admin<>'BAJA' OR (activo=false AND estado='INACTIVO'));
ALTER TABLE mantenimientos ADD COLUMN taller_id BIGINT REFERENCES taller(id_taller), ADD COLUMN usuario_responsable_id BIGINT REFERENCES usuarios(id), ADD FOREIGN KEY(tipo) REFERENCES cat_tipo_mantenimiento(codigo), ADD FOREIGN KEY(estado) REFERENCES cat_estado_mant(codigo);
ALTER TABLE documentos_vehiculo ADD FOREIGN KEY(tipo_documento) REFERENCES cat_tipo_documento(codigo);
ALTER TABLE solicitudes ADD COLUMN ubicacion_origen_id BIGINT REFERENCES ubicacion(id_ubicacion), ADD COLUMN ubicacion_destino_id BIGINT REFERENCES ubicacion(id_ubicacion), ADD COLUMN requiere_blindaje BOOLEAN NOT NULL DEFAULT FALSE, ADD FOREIGN KEY(modalidad) REFERENCES cat_tipo_servicio(codigo), ADD FOREIGN KEY(estado) REFERENCES cat_estado_reserva(codigo);
CREATE TABLE baja_vehiculo(id_baja BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, fk_id_vehiculo BIGINT NOT NULL UNIQUE REFERENCES vehiculos(id), fecha_baja DATE NOT NULL DEFAULT CURRENT_DATE, motivo VARCHAR(500) NOT NULL, fk_id_tip_baja VARCHAR(50) NOT NULL REFERENCES cat_tipo_baja(codigo), fk_id_usu BIGINT NOT NULL REFERENCES usuarios(id), observaciones VARCHAR(500));

-- Utilidades de sincronización: los textos históricos no se eliminan. Una ciudad
-- histórica carece de país verificado; NULL conserva esa incertidumbre.
CREATE FUNCTION df13_ciudad(p_nombre TEXT) RETURNS BIGINT LANGUAGE plpgsql AS $$
DECLARE resultado BIGINT;
BEGIN
 IF p_nombre IS NULL OR trim(p_nombre)='' THEN RETURN NULL; END IF;
 SELECT CASE WHEN count(*)=1 THEN min(id_ciudad) ELSE min(id_ciudad) FILTER (WHERE fk_id_pais IS NULL) END INTO resultado FROM ciudad WHERE lower(trim(nombre))=lower(trim(p_nombre));
 IF resultado IS NULL THEN
  INSERT INTO ciudad(nombre) VALUES(trim(p_nombre)) ON CONFLICT DO NOTHING;
  SELECT id_ciudad INTO resultado FROM ciudad WHERE lower(trim(nombre))=lower(trim(p_nombre)) AND fk_id_pais IS NULL;
 END IF;
 RETURN resultado;
END; $$;
CREATE FUNCTION df13_contacto(p_id TEXT,p_persona TEXT,p_tipo TEXT,p_valor TEXT,p_activo BOOLEAN) RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
 IF p_valor IS NULL OR trim(p_valor)='' THEN UPDATE persona_contacto SET act_contacto=FALSE WHERE id_contacto=p_id; RETURN; END IF;
 -- El diagrama exige valor único; si dos fuentes históricas comparten un dato,
 -- se conserva el valor original en su tabla y no se fuerza una duplicación.
 IF EXISTS(SELECT 1 FROM persona_contacto WHERE valor_contacto=trim(p_valor) AND id_contacto<>p_id) THEN RETURN; END IF;
 INSERT INTO persona_contacto(id_contacto,fk_id_persona,tipo_contacto,valor_contacto,act_contacto) VALUES(p_id,p_persona,p_tipo,trim(p_valor),p_activo)
 ON CONFLICT(id_contacto) DO UPDATE SET valor_contacto=EXCLUDED.valor_contacto,act_contacto=EXCLUDED.act_contacto;
END; $$;
CREATE FUNCTION df13_cliente() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p TEXT; c BIGINT;
BEGIN
 p:='CI-'||NEW.id; c:=df13_ciudad(NEW.ciudad);
 INSERT INTO persona(id_persona,pri_nom_per,fk_id_ciu,act_per) VALUES(p,NEW.nombre_contacto,c,NEW.activo)
 ON CONFLICT(id_persona) DO UPDATE SET fk_id_ciu=COALESCE(EXCLUDED.fk_id_ciu,persona.fk_id_ciu),act_per=EXCLUDED.act_per;
 INSERT INTO cliente_diplomatico(id_cliente,fk_id_persona,fk_id_tip_mision,nom_mision,act_cliente) VALUES(NEW.id,p,NEW.tipo_entidad,NEW.nombre_entidad,NEW.activo)
 ON CONFLICT(id_cliente) DO UPDATE SET fk_id_tip_mision=EXCLUDED.fk_id_tip_mision,nom_mision=EXCLUDED.nom_mision,act_cliente=EXCLUDED.act_cliente;
 IF NEW.direccion IS NOT NULL AND trim(NEW.direccion)<>'' THEN
  INSERT INTO persona_direccion(id_direccion,fk_id_persona,dir_fisica,fk_id_ciu,act_direccion) VALUES(p||'-DIR',p,NEW.direccion,c,NEW.activo)
  ON CONFLICT(id_direccion) DO UPDATE SET dir_fisica=EXCLUDED.dir_fisica,fk_id_ciu=EXCLUDED.fk_id_ciu,act_direccion=EXCLUDED.act_direccion;
 ELSE UPDATE persona_direccion SET act_direccion=FALSE WHERE id_direccion=p||'-DIR'; END IF;
 PERFORM df13_contacto(p||'-EMAIL',p,'CORREO',lower(NEW.correo_contacto),NEW.activo);
 PERFORM df13_contacto(p||'-TEL',p,'TELEFONO',NEW.telefono_contacto,NEW.activo);
 RETURN NEW;
END; $$;
CREATE TRIGGER df13_cliente_sync AFTER INSERT OR UPDATE OF nombre_entidad,tipo_entidad,nombre_contacto,correo_contacto,telefono_contacto,direccion,ciudad,activo ON clientes_institucionales FOR EACH ROW EXECUTE FUNCTION df13_cliente();
UPDATE clientes_institucionales SET nombre_entidad=nombre_entidad;
CREATE FUNCTION df13_usuario() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p TEXT;
BEGIN
 -- Reutilizar el contacto de la institución solo si es su correo principal.
 SELECT 'CI-'||id INTO p FROM clientes_institucionales WHERE id=NEW.cliente_institucional_id AND lower(trim(correo_contacto))=lower(trim(NEW.correo));
 p:=COALESCE(NEW.persona_id,p,'U-'||NEW.id);
 INSERT INTO persona(id_persona,pri_nom_per,pri_ape_per,act_per) VALUES(p,NEW.nombre,NEW.apellido,NEW.activo)
 ON CONFLICT(id_persona) DO UPDATE SET pri_nom_per=EXCLUDED.pri_nom_per,pri_ape_per=EXCLUDED.pri_ape_per,act_per=EXCLUDED.act_per;
 NEW.persona_id:=p;
 PERFORM df13_contacto(p||'-EMAIL',p,'CORREO',lower(NEW.correo),NEW.activo);
 IF NEW.telefono IS NOT NULL THEN PERFORM df13_contacto(p||'-TEL',p,'TELEFONO',NEW.telefono,NEW.activo); END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER df13_usuario_sync BEFORE INSERT OR UPDATE OF nombre,apellido,correo,telefono,activo,cliente_institucional_id ON usuarios FOR EACH ROW EXECUTE FUNCTION df13_usuario();
UPDATE usuarios SET nombre=nombre;
ALTER TABLE usuarios ALTER COLUMN persona_id SET NOT NULL;
CREATE FUNCTION df13_conductor() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p TEXT; doc TEXT;
BEGIN
 p:=COALESCE(NEW.persona_id,'CA-'||NEW.id); doc:=NEW.numero_documento;
 IF EXISTS(SELECT 1 FROM persona WHERE documento_per=doc AND id_persona<>p) THEN doc:=NULL; END IF;
 INSERT INTO persona(id_persona,pri_nom_per,fk_id_tip_doc,documento_per,act_per) VALUES(p,NEW.nombre_completo,NEW.tipo_documento,doc,NEW.activo)
 ON CONFLICT(id_persona) DO UPDATE SET act_per=EXCLUDED.act_per;
 NEW.persona_id:=p;
 INSERT INTO persona_documento(fk_id_persona,fk_id_tip_doc_per,num_doc) VALUES(p,NEW.tipo_documento,NEW.numero_documento) ON CONFLICT DO NOTHING;
 PERFORM df13_contacto(p||'-EMAIL',p,'CORREO',lower(NEW.correo),NEW.activo);
 PERFORM df13_contacto(p||'-TEL',p,'TELEFONO',NEW.telefono,NEW.activo);
 RETURN NEW;
END; $$;
CREATE TRIGGER df13_conductor_sync BEFORE INSERT OR UPDATE OF nombre_completo,tipo_documento,numero_documento,correo,telefono,activo ON conductores_autorizados FOR EACH ROW EXECUTE FUNCTION df13_conductor();
UPDATE conductores_autorizados SET nombre_completo=nombre_completo;
ALTER TABLE conductores_autorizados ALTER COLUMN persona_id SET NOT NULL;
-- No se infieren fechas de baja de unidades históricamente inactivas.
CREATE FUNCTION df13_estado_vehiculo() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='UPDATE' AND OLD.estado_admin='BAJA' AND (NEW.activo OR NEW.estado<>'INACTIVO' OR NEW.estado_admin<>'BAJA') THEN RAISE EXCEPTION 'Vehículo retirado definitivamente' USING ERRCODE='23514'; END IF;
 IF NEW.estado_admin<>'BAJA' THEN NEW.estado_admin:=CASE WHEN NEW.activo THEN 'ACTIVO' ELSE 'INACTIVO' END; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER df13_estado_vehiculo_sync BEFORE INSERT OR UPDATE ON vehiculos FOR EACH ROW EXECUTE FUNCTION df13_estado_vehiculo();
UPDATE vehiculos SET activo=activo;
CREATE FUNCTION df13_taller() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.proveedor IS NULL OR trim(NEW.proveedor)='' THEN NEW.taller_id:=NULL;
 ELSE
  INSERT INTO taller(nom_taller) VALUES(trim(NEW.proveedor)) ON CONFLICT(nom_taller) DO NOTHING;
  SELECT id_taller INTO NEW.taller_id FROM taller WHERE nom_taller=trim(NEW.proveedor);
 END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER df13_taller_sync BEFORE INSERT OR UPDATE OF proveedor ON mantenimientos FOR EACH ROW EXECUTE FUNCTION df13_taller();
UPDATE mantenimientos SET proveedor=proveedor;
CREATE FUNCTION df13_ubicaciones() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 INSERT INTO ubicacion(nom_ubicacion) VALUES(trim(NEW.lugar_recogida)),(trim(NEW.lugar_devolucion)) ON CONFLICT(nom_ubicacion) DO NOTHING;
 SELECT id_ubicacion INTO NEW.ubicacion_origen_id FROM ubicacion WHERE nom_ubicacion=trim(NEW.lugar_recogida);
 SELECT id_ubicacion INTO NEW.ubicacion_destino_id FROM ubicacion WHERE nom_ubicacion=trim(NEW.lugar_devolucion);
 RETURN NEW;
END; $$;
CREATE TRIGGER df13_ubicaciones_sync BEFORE INSERT OR UPDATE OF lugar_recogida,lugar_devolucion ON solicitudes FOR EACH ROW EXECUTE FUNCTION df13_ubicaciones();
UPDATE solicitudes SET lugar_recogida=lugar_recogida;
ALTER TABLE solicitudes ALTER COLUMN ubicacion_origen_id SET NOT NULL, ALTER COLUMN ubicacion_destino_id SET NOT NULL, ADD FOREIGN KEY(cliente_institucional_id) REFERENCES cliente_diplomatico(id_cliente);

-- Proyecciones relacionales del ER, sin segundas copias de las tablas operativas.
-- Las claves reales permanecen en las tablas base. Los IDs bigint se conservan
-- por compatibilidad; proveedor es una entidad auxiliar definida en V13.
CREATE VIEW cat_perfil AS SELECT id,nombre AS codigo,descripcion,activo FROM roles;
CREATE VIEW usuario AS SELECT u.id AS id_usu,u.correo AS nom_usu,u.password_hash AS pass_usu,u.persona_id AS fk_id_persona, (SELECT min(rol_id) FROM usuarios_roles WHERE usuario_id=u.id) AS fk_id_perf,u.activo AS act_usu,u.creado_en AS fech_creacion FROM usuarios u;
CREATE VIEW conductor AS SELECT 'A-'||c.id AS id_conductor,c.persona_id AS fk_id_persona,c.numero_licencia AS num_licencia,c.categoria_licencia AS fk_id_cat_lic,c.fecha_vencimiento_licencia AS fech_venc_licencia,c.es_diplomatico,c.activo AS act_conductor FROM conductores_autorizados c UNION ALL SELECT 'P-'||c.id,u.persona_id,c.numero_licencia,c.categoria_licencia,c.fecha_vencimiento_licencia,c.es_diplomatico,(c.activo AND u.activo) FROM conductores_profesionales c JOIN usuarios u ON u.id=c.usuario_id;
CREATE VIEW vehiculo AS SELECT v.id AS id_vehiculo,v.placa,v.marca,v.modelo,v.anio,v.tipo_vehiculo AS fk_id_tip_veh,v.nivel_blindaje AS fk_id_nivel_blindaje,v.estado_admin AS fk_id_est_admin,v.estado AS fk_id_est_op,b.fecha_baja,b.motivo AS motivo_baja,v.proveedor_id AS fk_id_proveedor FROM vehiculos v LEFT JOIN baja_vehiculo b ON b.fk_id_vehiculo=v.id;
CREATE VIEW mantenimiento AS SELECT id AS id_mantenimiento,vehiculo_id AS fk_id_vehiculo,tipo AS fk_id_tip_mant,taller_id AS fk_id_taller,fecha_inicio,fecha_fin,costo,descripcion,estado AS fk_id_est_mant,usuario_responsable_id AS fk_id_usu FROM mantenimientos;
CREATE VIEW reserva AS SELECT id AS id_reserva,cliente_institucional_id AS fk_id_cliente,vehiculo_id AS fk_id_vehiculo,CASE WHEN conductor_profesional_id IS NOT NULL THEN 'P-'||conductor_profesional_id WHEN conductor_autorizado_id IS NOT NULL THEN 'A-'||conductor_autorizado_id END AS fk_id_conductor,ubicacion_origen_id AS fk_id_ubic_origen,ubicacion_destino_id AS fk_id_ubic_destino,fecha_inicio AS fecha_hora_inicio,fecha_fin AS fecha_hora_fin,modalidad AS fk_id_tip_serv,requiere_blindaje,estado AS fk_id_est_res,estado NOT IN ('CANCELADA','RECHAZADA','ARCHIVADA','FINALIZADA') AS act_reserva FROM solicitudes;
-- Las sesiones opacas de la web se guardan como hash SHA-256, no contraseña.
CREATE TABLE sesiones_web(token_hash CHAR(64) PRIMARY KEY, usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE, creado_en TIMESTAMPTZ NOT NULL DEFAULT now(), expira_en TIMESTAMPTZ NOT NULL);
CREATE INDEX idx_sesiones_expira ON sesiones_web(expira_en);
COMMENT ON TABLE sesiones_web IS 'Tokens aleatorios revocables; no se almacena el token original ni la contraseña.';
