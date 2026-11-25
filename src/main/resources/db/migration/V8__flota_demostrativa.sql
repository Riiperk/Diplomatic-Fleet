-- Datos ficticios de practica, identificados con placas de demostracion.
-- El administrador puede desactivarlos desde el modulo de flota.
INSERT INTO vehiculos
(categoria_id,placa,marca,modelo,anio,color,combustible,capacidad_pasajeros,
 capacidad_equipaje,estado,activo,tarifa_diaria,imagen_url)
SELECT c.id, v.placa,v.marca,v.modelo,v.anio,v.color,v.combustible,
 v.pasajeros,v.equipaje,'DISPONIBLE',TRUE,v.tarifa,'/assets/images/diplomatic-sedan.png'
FROM (VALUES
 ('ECONOMICO','DF-DEMO01','Toyota','Camry Executive',2025,'Negro','HIBRIDO',4,3,290000.00),
 ('EJECUTIVO','DF-DEMO02','Mercedes-Benz','Clase E',2025,'Negro','GASOLINA',4,3,470000.00),
 ('PREMIUM','DF-DEMO03','BMW','Serie 7',2026,'Negro','HIBRIDO',4,4,850000.00),
 ('PREMIUM','DF-DEMO04','Volvo','XC90',2025,'Gris','HIBRIDO',6,5,690000.00)
) AS v(categoria,placa,marca,modelo,anio,color,combustible,pasajeros,equipaje,tarifa)
JOIN categorias_vehiculo c ON c.codigo=v.categoria
WHERE TRUE
ON CONFLICT DO NOTHING;
