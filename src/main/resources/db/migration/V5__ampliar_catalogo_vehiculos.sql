-- Catálogo comercial: ampliar la ficha sin alterar registros existentes.
-- La tarifa es opcional porque algunos servicios diplomáticos se cotizan a medida.
ALTER TABLE vehiculos
    ADD COLUMN tarifa_diaria NUMERIC(12, 2),
    ADD COLUMN imagen_url VARCHAR(500),
    ADD COLUMN descripcion VARCHAR(600),
    ADD COLUMN blindado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE vehiculos
    ADD CONSTRAINT chk_vehiculos_tarifa_diaria
    CHECK (tarifa_diaria IS NULL OR tarifa_diaria >= 0);

COMMENT ON COLUMN vehiculos.tarifa_diaria IS 'Tarifa indicativa por día, en COP. NULL significa cotización personalizada, no precio cero.';
COMMENT ON COLUMN vehiculos.imagen_url IS 'URL HTTPS de la fotografía comercial. NULL utiliza la imagen local predeterminada.';
COMMENT ON COLUMN vehiculos.descripcion IS 'Descripción comercial visible en el catálogo público.';
COMMENT ON COLUMN vehiculos.blindado IS 'Clasificación informativa; no sustituye certificaciones ni autorizaciones legales.';
