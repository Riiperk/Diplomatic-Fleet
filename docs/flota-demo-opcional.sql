-- =====================================================================
-- SOLO PARA DEMOSTRACIÓN ACADÉMICA / BASE DE DATOS LOCAL
-- Tarifas, placas, blindajes y registros 100% FICTICIOS.
-- NO ejecutar en una base de producción ni presentar como flota real.
--
-- Requisitos: ejecutar previamente las migraciones V1 a V6 con Flyway.
-- No crea usuarios, contraseñas ni datos personales.
-- El catálogo utiliza la imagen local del proyecto si imagen_url es NULL.
-- =====================================================================

INSERT INTO vehiculos (
    categoria_id, placa, marca, modelo, anio, color, combustible,
    capacidad_pasajeros, capacidad_equipaje, estado, activo,
    tarifa_diaria, descripcion, blindado
)
SELECT c.id, d.placa, d.marca, d.modelo, d.anio, d.color, d.combustible,
       d.pasajeros, d.equipaje, d.estado, TRUE,
       d.tarifa, d.descripcion, d.blindado
FROM (
    VALUES
        ('DF-DEMO-01', 'Toyota', 'Corolla', 2025, 'Blanco', 'HIBRIDO', 4, 2, 'DISPONIBLE',
         'ECONOMICO', 280000.00, 'DEMO: movilidad para reuniones y recorridos institucionales.', FALSE),
        ('DF-DEMO-02', 'Toyota', 'Prado', 2025, 'Negro', 'DIESEL', 5, 4, 'DISPONIBLE',
         'PREMIUM', 780000.00, 'DEMO: SUV de representación para delegaciones y comitivas.', FALSE),
        ('DF-DEMO-03', 'Audi', 'A6', 2024, 'Negro', 'GASOLINA', 4, 3, 'DISPONIBLE',
         'EJECUTIVO', 550000.00, 'DEMO: sedán ejecutivo para traslados de protocolo.', FALSE),
        ('DF-DEMO-04', 'Hyundai', 'Staria', 2025, 'Gris', 'DIESEL', 8, 5, 'RESERVADO',
         'EJECUTIVO', 490000.00, 'DEMO: transporte de grupos y equipos de apoyo.', FALSE),
        ('DF-DEMO-05', 'Chevrolet', 'Suburban', 2025, 'Negro', 'GASOLINA', 6, 5, 'DISPONIBLE',
         'PREMIUM', NULL, 'DEMO: servicio premium de gran capacidad con cotización individual.', FALSE),
        ('DF-DEMO-06', 'Mercedes-Benz', 'Clase E', 2023, 'Blanco', 'HIBRIDO', 4, 3, 'EN_MANTENIMIENTO',
         'PREMIUM', 680000.00, 'DEMO: sedán ejecutivo en revisión; no admite solicitudes.', FALSE)
) AS d (placa, marca, modelo, anio, color, combustible,
        pasajeros, equipaje, estado, categoria, tarifa, descripcion, blindado)
JOIN categorias_vehiculo c ON c.codigo = d.categoria
WHERE NOT EXISTS (
    SELECT 1 FROM vehiculos v WHERE UPPER(TRIM(v.placa)) = d.placa
);

-- Verificar las unidades de ejemplo:
SELECT placa, marca, modelo, estado, tarifa_diaria, activo
FROM vehiculos WHERE placa LIKE 'DF-DEMO-%' ORDER BY placa;
