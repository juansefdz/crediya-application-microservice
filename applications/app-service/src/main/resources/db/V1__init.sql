
-- =========================
-- Tabla: estados
-- =========================
CREATE TABLE IF NOT EXISTS estados (
    id_estado BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- Tabla: tipo_prestamo
-- =========================
CREATE TABLE IF NOT EXISTS tipo_prestamo (
    id_tipo_prestamo BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    monto_minimo BIGINT NOT NULL,
    monto_maximo BIGINT NOT NULL,
    tasa_interes DECIMAL(7,4) NOT NULL, -- p.ej. 1.5000%
    validacion_automatica BOOLEAN DEFAULT FALSE, -- BOOLEAN=TINYINT(1) en MySQL
    CHECK (monto_minimo <= monto_maximo),
    CHECK (tasa_interes >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Índices útiles (además de los UNIQUE)
CREATE INDEX idx_tipo_prestamo_rangos ON tipo_prestamo (monto_minimo, monto_maximo);

-- =========================
-- Tabla: solicitud
-- =========================
CREATE TABLE IF NOT EXISTS solicitud (
    id_solicitud CHAR(36) PRIMARY KEY, -- UUID textual; si quieres perf: BINARY(16)
    documento_cliente VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    monto BIGINT NOT NULL,
    plazo INT NOT NULL,
    id_estado BIGINT,
    id_tipo_prestamo BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitud_estado
        FOREIGN KEY (id_estado) REFERENCES estados(id_estado)
        ON UPDATE RESTRICT ON DELETE SET NULL,
    CONSTRAINT fk_solicitud_tipo
        FOREIGN KEY (id_tipo_prestamo) REFERENCES tipo_prestamo(id_tipo_prestamo)
        ON UPDATE RESTRICT ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Índices para búsquedas típicas
CREATE INDEX idx_solicitud_doc ON solicitud (documento_cliente);
CREATE INDEX idx_solicitud_email ON solicitud (email);
CREATE INDEX idx_solicitud_estado ON solicitud (id_estado);
CREATE INDEX idx_solicitud_tipo ON solicitud (id_tipo_prestamo);

-- =========================
-- Datos iniciales
-- =========================
INSERT INTO estados (nombre, descripcion)
VALUES
  ('Pendiente de revisión', 'La solicitud ha sido recibida y está esperando ser evaluada.'),
  ('Aprobada', 'La solicitud de préstamo ha sido aprobada.'),
  ('Rechazada', 'La solicitud de préstamo ha sido rechazada.')
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);

INSERT INTO tipo_prestamo (nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica)
VALUES
  ('Crédito de Libre Inversión', 1000000,  50000000,    1.5000, TRUE),
  ('Crédito de Vivienda',        50000000, 1000000000,  0.9000, FALSE)
ON DUPLICATE KEY UPDATE
  monto_minimo = VALUES(monto_minimo),
  monto_maximo = VALUES(monto_maximo),
  tasa_interes = VALUES(tasa_interes),
  validacion_automatica = VALUES(validacion_automatica);