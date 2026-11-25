-- V12: fotografias externas de referencia por modelo, con atribucion en Vue.
-- No crea vehiculos ni cambia tarifas, estados, reservas o usuarios.
-- Solo sustituye recursos genericos de la flota demostrativa V8-V11.
-- Conserva cualquier URL externa que el administrador haya configurado.
UPDATE vehiculos AS destino
SET imagen_url = fotos.url
FROM (VALUES
    ('DF-DEMO01', 'Toyota', 'Camry Executive', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1c/Toyota_Camry_front_%28October_29%2C_2024%29.jpg/960px-Toyota_Camry_front_%28October_29%2C_2024%29.jpg'),
    ('DF-DEMO02', 'Mercedes-Benz', 'Clase E', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/Mercedes-Benz_E_200_AVANTGARDE_%284AA-214050C%29_front.jpg/960px-Mercedes-Benz_E_200_AVANTGARDE_%284AA-214050C%29_front.jpg'),
    ('DF-DEMO03', 'BMW', 'Serie 7', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/75/BMW_G70_740i_sDrive_Design_Pure_Excellence_Oxide_Grey_Tanzanite_Blue_Metallic_Two-tone_%2811%29.jpg/960px-BMW_G70_740i_sDrive_Design_Pure_Excellence_Oxide_Grey_Tanzanite_Blue_Metallic_Two-tone_%2811%29.jpg'),
    ('DF-DEMO04', 'Volvo', 'XC90', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e0/2025_Volvo_XC90_%28facelift%29%2C_front_4.11.25.jpg/960px-2025_Volvo_XC90_%28facelift%29%2C_front_4.11.25.jpg'),
    ('DF-DEMO05', 'Mazda', '3', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6d/Mazda_MAZDA3_Sedan_1th_gen_BP_1.png/960px-Mazda_MAZDA3_Sedan_1th_gen_BP_1.png'),
    ('DF-DEMO06', 'Kia', 'K3', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/09/Kia_K3_front_%28October_29%2C_2024%29.jpg/960px-Kia_K3_front_%28October_29%2C_2024%29.jpg'),
    ('DF-DEMO07', 'Hyundai', 'Tucson', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/26/Hyundai_Tucson_%28NX4%29_2.0D_GLS%2B_2022_%281%29.jpg/960px-Hyundai_Tucson_%28NX4%29_2.0D_GLS%2B_2022_%281%29.jpg'),
    ('DF-DEMO08', 'Toyota', 'RAV4', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2e/2022_Toyota_RAV4_Hybrid_LE_in_Midnight_Black_Metallic%2C_front_left.jpg/960px-2022_Toyota_RAV4_Hybrid_LE_in_Midnight_Black_Metallic%2C_front_left.jpg'),
    ('DF-DEMO09', 'Mazda', 'CX-5', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ec/2022_Mazda_CX-5_2.0_front.jpg/960px-2022_Mazda_CX-5_2.0_front.jpg'),
    ('DF-DEMO10', 'Audi', 'A6', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b3/Audi_A6_C8.jpg/960px-Audi_A6_C8.jpg'),
    ('DF-DEMO11', 'BMW', 'Serie 5', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/86/BMW_G60_520i_1X7A2443.jpg/960px-BMW_G60_520i_1X7A2443.jpg'),
    ('DF-DEMO12', 'Mercedes-Benz', 'GLE', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a5/Mercedes-Benz_GLE_450_d_4MATIC_Sports_Edition_Black_Stars_%28W167%29_front.jpg/960px-Mercedes-Benz_GLE_450_d_4MATIC_Sports_Edition_Black_Stars_%28W167%29_front.jpg'),
    ('DF-DEMO13', 'Toyota', 'Land Cruiser 300', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e4/2021_Toyota_Land_Cruiser_300_3.4_ZX_%28Colombia%29_front_view_01.png/960px-2021_Toyota_Land_Cruiser_300_3.4_ZX_%28Colombia%29_front_view_01.png'),
    ('DF-DEMO14', 'Lexus', 'LX 600', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/99/2023_Lexus_LX_600%2C_front_6.13.24.jpg/960px-2023_Lexus_LX_600%2C_front_6.13.24.jpg'),
    ('DF-DEMO15', 'Chevrolet', 'Suburban', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/2021_Chevrolet_Suburban_LT%2C_front_6.14.21.jpg/960px-2021_Chevrolet_Suburban_LT%2C_front_6.14.21.jpg'),
    ('DF-DEMO16', 'Mercedes-Benz', 'Clase V', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/51/2024_Mercedes_V_Class.jpg/960px-2024_Mercedes_V_Class.jpg'),
    ('DF-DEMO17', 'Honda', 'Accord', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1d/2023_Honda_Accord_EX%2C_front_2.12.23.jpg/960px-2023_Honda_Accord_EX%2C_front_2.12.23.jpg'),
    ('DF-DEMO18', 'Nissan', 'X-Trail', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a0/Nissan_X-TRAIL_G_e-4ORCE_%28SNT33%29_front.jpg/960px-Nissan_X-TRAIL_G_e-4ORCE_%28SNT33%29_front.jpg'),
    ('DF-DEMO19', 'Skoda', 'Superb', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/87/2024_%C5%A0koda_Superb.jpg/960px-2024_%C5%A0koda_Superb.jpg'),
    ('DF-DEMO20', 'Kia', 'Carnival', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/86/2025_Kia_Carnival_GT-Line_2.2_front.jpg/960px-2025_Kia_Carnival_GT-Line_2.2_front.jpg')
) AS fotos(placa,marca,modelo,url)
WHERE UPPER(destino.placa) = fotos.placa
  AND LOWER(TRIM(destino.marca)) = LOWER(fotos.marca)
  AND LOWER(TRIM(destino.modelo)) = LOWER(fotos.modelo)
  AND (destino.imagen_url IS NULL
       OR destino.imagen_url = '/assets/images/diplomatic-sedan.png'
       OR destino.imagen_url = '/assets/images/' || LOWER(destino.placa) || '.svg'
       OR destino.imagen_url LIKE '/assets/images/catalogo/sedan-%.png');
