ALTER TABLE usuarios
    ADD COLUMN cliente_institucional_id BIGINT
    REFERENCES clientes_institucionales(id);

CREATE INDEX idx_usuarios_cliente
    ON usuarios(cliente_institucional_id);

INSERT INTO roles(nombre, descripcion, activo)
VALUES (
    'CLIENTE',
    'Contacto institucional con acceso a sus solicitudes',
    TRUE
)
ON CONFLICT(nombre) DO NOTHING;

ALTER TABLE vehiculos
    ALTER COLUMN imagen_url TYPE VARCHAR(800);

ALTER TABLE solicitudes
    ADD COLUMN valor_estimado NUMERIC(14,2)
    NOT NULL DEFAULT 0
    CHECK (valor_estimado >= 0);