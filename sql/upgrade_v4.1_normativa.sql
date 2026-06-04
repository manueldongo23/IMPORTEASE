-- upgrade_v4.1_normativa.sql
-- Mejoras a restricciones_formal y nueva tabla fuente_metadata

ALTER TABLE restricciones_formal
  ADD COLUMN hs_regex VARCHAR(80) NULL AFTER hs_categoria,
  ADD COLUMN source_type VARCHAR(30) NOT NULL DEFAULT 'OFFICIAL_PROCEDURE' AFTER hs_regex,
  ADD COLUMN confidence DECIMAL(5,2) NOT NULL DEFAULT 0.95 AFTER source_type,
  ADD COLUMN last_checked_at TIMESTAMP NULL AFTER vigente;

CREATE TABLE IF NOT EXISTS fuente_metadata (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  source_name VARCHAR(80) NOT NULL,
  source_url VARCHAR(500) NOT NULL,
  source_type VARCHAR(30) NOT NULL,
  fetched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  content_hash CHAR(64) NOT NULL,
  version_label VARCHAR(40) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agregar columnas adicionales a reglas_plazo para mejor soporte desde codigo
ALTER TABLE reglas_plazo
  ADD COLUMN IF NOT EXISTS plazo_meses INT NULL AFTER plazo_dias,
  ADD COLUMN IF NOT EXISTS plazo_dias_alterno INT NULL AFTER plazo_meses,
  ADD COLUMN IF NOT EXISTS fuente_referencia VARCHAR(255) NULL AFTER norma_fuente,
  ADD COLUMN IF NOT EXISTS mensaje TEXT NULL AFTER fuente_referencia;
