-- V11: refuerzo visual del catálogo.
-- Se normalizan imágenes locales con estilo más fotográfico para que la vista
-- pública no dependa de recursos externos durante la sustentación.
-- También se amplía la flota de demostración con algunas referencias extra.

INSERT INTO vehiculos(
    categoria_id,placa,marca,modelo,anio,color,combustible,
    capacidad_pasajeros,capacidad_equipaje,estado,activo,tarifa_diaria,imagen_url
)
SELECT c.id,v.placa,v.marca,v.modelo,v.anio,v.color,v.combustible,
       v.pasajeros,v.equipaje,'DISPONIBLE',TRUE,v.tarifa,v.imagen
FROM (VALUES
 ('EJECUTIVO','DF-DEMO17','Honda','Accord',2025,'Blanco','HIBRIDO',5,3,410000.00,'/assets/images/catalogo/sedan-blanco.png'),
 ('EJECUTIVO','DF-DEMO18','Nissan','X-Trail',2025,'Plata','GASOLINA',5,4,430000.00,'/assets/images/catalogo/sedan-plata.png'),
 ('PREMIUM','DF-DEMO19','Skoda','Superb',2025,'Azul','HIBRIDO',5,4,540000.00,'/assets/images/catalogo/sedan-azul.png'),
 ('PREMIUM','DF-DEMO20','Kia','Carnival',2025,'Dorado','GASOLINA',7,5,730000.00,'/assets/images/catalogo/sedan-dorado.png')
) AS v(categoria,placa,marca,modelo,anio,color,combustible,pasajeros,equipaje,tarifa,imagen)
JOIN categorias_vehiculo c ON c.codigo=v.categoria
-- Evita registrar vehiculos de demostracion duplicados.
WHERE NOT EXISTS (
    SELECT 1
    FROM vehiculos AS existente
    WHERE UPPER(existente.placa) = UPPER(v.placa)
)
ON CONFLICT DO NOTHING;

UPDATE vehiculos SET imagen_url='/assets/images/catalogo/sedan-negro.png' WHERE placa IN ('DF-DEMO01','DF-DEMO10','DF-DEMO12','DF-DEMO15');
UPDATE vehiculos SET imagen_url='/assets/images/catalogo/sedan-rojo.png' WHERE placa IN ('DF-DEMO05','DF-DEMO09');
UPDATE vehiculos SET imagen_url='/assets/images/catalogo/sedan-plata.png' WHERE placa IN ('DF-DEMO04','DF-DEMO06','DF-DEMO08','DF-DEMO16','DF-DEMO18');
UPDATE vehiculos SET imagen_url='/assets/images/catalogo/sedan-blanco.png' WHERE placa IN ('DF-DEMO07','DF-DEMO13','DF-DEMO17');
UPDATE vehiculos SET imagen_url='/assets/images/catalogo/sedan-azul.png' WHERE placa IN ('DF-DEMO03','DF-DEMO11','DF-DEMO19');
UPDATE vehiculos SET imagen_url='/assets/images/catalogo/sedan-dorado.png' WHERE placa IN ('DF-DEMO02','DF-DEMO14','DF-DEMO20');
