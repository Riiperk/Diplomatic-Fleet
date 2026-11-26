-- V14: importes y evidencia declarada de pagos externos.
-- Los pagos historicos conservan NULL: no se inventan ingresos ni fechas.
ALTER TABLE pagos_reserva
 ADD COLUMN monto_reportado NUMERIC(14,2) CHECK (monto_reportado > 0),
 ADD COLUMN monto_verificado NUMERIC(14,2) CHECK (monto_verificado > 0),
 ADD COLUMN monto_reembolsado NUMERIC(14,2) CHECK (monto_reembolsado > 0),
 ADD COLUMN reportado_en TIMESTAMPTZ,
 ADD COLUMN verificado_en TIMESTAMPTZ,
 ADD COLUMN reembolsado_en TIMESTAMPTZ,
 ADD COLUMN referencia_verificacion VARCHAR(120),
 ADD COLUMN referencia_reembolso VARCHAR(120),
 ADD COLUMN verificado_por BIGINT REFERENCES usuarios(id) ON DELETE SET NULL,
 ADD COLUMN reembolsado_por BIGINT REFERENCES usuarios(id) ON DELETE SET NULL;
COMMENT ON COLUMN pagos_reserva.monto_reportado IS 'Importe declarado por la institucion; no representa un abono verificado.';
COMMENT ON COLUMN pagos_reserva.monto_verificado IS 'Importe recibido y comprobado externamente por administracion.';
COMMENT ON COLUMN pagos_reserva.monto_reembolsado IS 'Importe devuelto externamente, declarado por administracion.';
COMMENT ON COLUMN pagos_reserva.referencia_verificacion IS 'Referencia del comprobante externo o recibo; nunca datos completos de tarjeta.';
-- Una misma referencia de verificacion no puede acreditar dos reservas del mismo metodo.
CREATE UNIQUE INDEX uq_pagos_reserva_verificacion_ref ON pagos_reserva(metodo,lower(btrim(referencia_verificacion))) WHERE referencia_verificacion IS NOT NULL;
CREATE UNIQUE INDEX uq_pagos_reserva_reembolso_ref ON pagos_reserva(metodo,lower(btrim(referencia_reembolso))) WHERE referencia_reembolso IS NOT NULL;
