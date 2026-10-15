-- ============================================================
-- Diplomatic Fleet
-- Migración V4: catálogos iniciales
--
-- Esta migración inserta los valores controlados necesarios
-- para que la aplicación pueda comenzar a operar.
--
-- No se insertan:
-- - Usuarios.
-- - Contraseñas.
-- - Clientes institucionales.
-- - Conductores.
-- - Vehículos.
-- - Solicitudes.
-- - Información personal.
-- ============================================================


-- ============================================================
-- CATÁLOGO: roles
-- ============================================================

INSERT INTO roles (
    nombre,
    descripcion,
    activo
)
VALUES
    (
        'ADMINISTRADOR',
        'Administra usuarios, roles, configuración y acceso general al sistema.',
        TRUE
    ),
    (
        'OPERACIONES',
        'Gestiona solicitudes, asignaciones, vehículos y conductores autorizados.',
        TRUE
    ),
    (
        'COMERCIAL',
        'Gestiona clientes institucionales, cotizaciones y seguimiento comercial.',
        TRUE
    ),
    (
        'MANTENIMIENTO',
        'Gestiona documentos, mantenimientos y estado operativo de los vehículos.',
        TRUE
    )
ON CONFLICT (nombre)
DO UPDATE SET
    descripcion = EXCLUDED.descripcion,
    activo = EXCLUDED.activo;


-- ============================================================
-- CATÁLOGO: categorías de vehículos
-- ============================================================

INSERT INTO categorias_vehiculo (
    codigo,
    nombre,
    descripcion,
    activo
)
VALUES
    (
        'ECONOMICO',
        'Económico',
        'Vehículos funcionales para traslados institucionales de bajo coste.',
        TRUE
    ),
    (
        'EJECUTIVO',
        'Ejecutivo',
        'Vehículos para servicios institucionales y delegaciones diplomáticas.',
        TRUE
    ),
    (
        'PREMIUM',
        'Premium',
        'Vehículos de gama superior para servicios de protocolo y representación.',
        TRUE
    )
ON CONFLICT (codigo)
DO UPDATE SET
    nombre = EXCLUDED.nombre,
    descripcion = EXCLUDED.descripcion,
    activo = EXCLUDED.activo;


-- ============================================================
-- COMENTARIOS DEL CATÁLOGO INICIAL
-- ============================================================

COMMENT ON TABLE roles IS
    'Catálogo de roles disponibles para controlar el acceso y las responsabilidades del sistema.';

COMMENT ON TABLE categorias_vehiculo IS
    'Catálogo inicial de categorías utilizadas para clasificar la flota institucional.';