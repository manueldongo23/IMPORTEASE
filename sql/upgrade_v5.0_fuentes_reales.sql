-- upgrade_v5.0_fuentes_reales.sql
-- Migracion para fuentes de datos reales: SUNAT bulk, VUCE PAM, Veritrade, DR/SUCE

-- 1. Nuevas columnas en hs_codes para datos enriquecidos de SUNAT
ALTER TABLE hs_codes
  ADD COLUMN IF NOT EXISTS ipm DECIMAL(5,2) DEFAULT 0.00 AFTER igv,
  ADD COLUMN IF NOT EXISTS antidumping BOOLEAN DEFAULT FALSE AFTER entidad_vuce,
  ADD COLUMN IF NOT EXISTS restricciones TEXT NULL AFTER antidumping,
  ADD COLUMN IF NOT EXISTS prohibiciones TEXT NULL AFTER restricciones;

-- 2. Tabla de procedimientos VUCE (Lista de Procedimientos Incorporados)
CREATE TABLE IF NOT EXISTS vuce_procedimientos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  entidad VARCHAR(50) NOT NULL,
  codigo_tupa VARCHAR(50) NULL,
  detalle VARCHAR(500) NOT NULL,
  plazo VARCHAR(100) NULL,
  costo VARCHAR(100) NULL,
  base_legal VARCHAR(255) NULL,
  ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_vuce_procedimiento (entidad, codigo_tupa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tabla de verificaciones documentales VUCE (DR/SUCE)
CREATE TABLE IF NOT EXISTS vuce_verificaciones_documentales (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tipo_verificacion VARCHAR(10) NOT NULL COMMENT 'DR o SUCE',
  numero_documento VARCHAR(100) NOT NULL,
  ruc VARCHAR(11) NULL,
  verificado BOOLEAN DEFAULT FALSE,
  detalle TEXT NULL,
  codigo_qr TEXT NULL,
  usuario_id INT NULL,
  operacion_id INT NULL,
  fecha_verificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_vuce_verif_tipo_num (tipo_verificacion, numero_documento),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
  FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Tabla de importaciones historicas (Veritrade / SUNAT operatividad)
CREATE TABLE IF NOT EXISTS historico_importaciones_partida (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(10) NOT NULL,
  descripcion VARCHAR(500) NULL,
  importador VARCHAR(255) NULL,
  exportador VARCHAR(255) NULL,
  pais_origen VARCHAR(100) NULL,
  valor_fob DECIMAL(15,2) DEFAULT 0.00,
  valor_cif DECIMAL(15,2) DEFAULT 0.00,
  peso DECIMAL(12,3) DEFAULT 0.00,
  via_transporte VARCHAR(50) NULL,
  fuente VARCHAR(50) NOT NULL DEFAULT 'VERITRADE',
  usuario_id INT NULL,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_hist_hs (hs_code),
  INDEX idx_hist_fuente (fuente),
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Agregar columna source_type a vuce_restricciones para trazabilidad
ALTER TABLE vuce_restricciones
  ADD COLUMN IF NOT EXISTS source_type VARCHAR(30) DEFAULT 'BD_LOCAL' AFTER enlace_tupa;
