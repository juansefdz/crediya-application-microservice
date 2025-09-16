CREATE TABLE IF NOT EXISTS estados (
    id_estado VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT
);

CREATE TABLE IF NOT EXISTS tipo_prestamo (
    id_tipo_prestamo BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    monto_minimo DECIMAL(19,2) NOT NULL,
    monto_maximo DECIMAL(19,2) NOT NULL,
    tasa_interes DECIMAL(7,4) NOT NULL,
    validacion_automatica BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS solicitud (
    id_solicitud CHAR(36) PRIMARY KEY,
    usuario_id VARCHAR(50) NOT NULL,
    nombre_cliente VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    plazo INT NOT NULL,
    id_estado VARCHAR(50),
    id_tipo_prestamo BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitud_estado FOREIGN KEY (id_estado) REFERENCES estados(id_estado),
    CONSTRAINT fk_solicitud_tipo FOREIGN KEY (id_tipo_prestamo) REFERENCES tipo_prestamo(id_tipo_prestamo)
);

-- Insertar estados
INSERT INTO estados (id_estado, nombre, descripcion)
VALUES
    ('PENDIENTE_REVISION', 'Pendiente de Revisión', 'La solicitud ha sido recibida.'),
    ('PENDIENTE_VALIDACION', 'Pendiente de Validación', 'Esperando validación automática.'),
    ('APROBADA', 'Aprobada', 'La solicitud ha sido aprobada.'),
    ('RECHAZADA', 'Rechazada', 'La solicitud ha sido rechazada.'),
    ('REVISION_MANUAL', 'Revisión Manual', 'La solicitud requiere revisión de un asesor.')
AS new_values(id_estado, nombre, descripcion)
ON DUPLICATE KEY UPDATE
    nombre = new_values.nombre,
    descripcion = new_values.descripcion;

-- Insertar tipos de préstamo
INSERT INTO tipo_prestamo (nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica)
VALUES
    ('Crédito de Libre Inversión', 1000000.00, 50000000.00, 1.5000, TRUE),
    ('Crédito de Vivienda', 50000000.00, 1000000000.00, 0.9000, FALSE),
    ('Crédito de Vehículo', 5000000.00, 200000000.00, 1.2000, TRUE),
    ('Crédito Empresarial', 10000000.00, 5000000000.00, 1.8000, FALSE),
    ('Crédito Educativo', 2000000.00, 100000000.00, 1.1000, TRUE)
AS new_values(nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica)
ON DUPLICATE KEY UPDATE
    monto_minimo = new_values.monto_minimo,
    monto_maximo = new_values.monto_maximo,
    tasa_interes = new_values.tasa_interes,
    validacion_automatica = new_values.validacion_automatica;