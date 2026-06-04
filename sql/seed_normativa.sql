-- =====================================================================
-- IMPORTEASE ADUANERO - SEED DE NORMATIVA ADUANERA PERUANA
-- =====================================================================
-- Version: 1.0
-- Fuente: Ley General de Aduanas (Decreto Supremo), DESPA-PG, VUCE
-- =====================================================================

USE importease_db;
SET NAMES utf8mb4;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- TABLA 1: reglas_normativas
-- =====================================================================
CREATE TABLE IF NOT EXISTS reglas_normativas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(80) NOT NULL UNIQUE,
  nombre VARCHAR(255) NOT NULL,
  tipo VARCHAR(20) NOT NULL COMMENT 'BLOCKING | WARNING | INFO',
  base_legal VARCHAR(255),
  fuente VARCHAR(255),
  version VARCHAR(50),
  vigente BOOLEAN DEFAULT TRUE,
  descripcion TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_rn_tipo (tipo),
  INDEX idx_rn_vigente (vigente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO reglas_normativas (codigo, nombre, tipo, base_legal, fuente, version, vigente, descripcion) VALUES
('IMP_CONSUMO_DESTINACION', 'Plazo destinacion aduanera importacion consumo', 'BLOCKING', 'LGA art 155', 'DESPA-PG.01', '1.0', TRUE, 'La mercancia importada debe destinarse dentro de 15 dias calendario desde el termino de la descarga.'),
('IMP_CONSUMO_ABANDONO', 'Abandono legal por vencimiento de plazo', 'BLOCKING', 'LGA art 158', 'DESPA-PG.01', '1.0', TRUE, 'Vencido el plazo de destinacion sin regularizar, la mercancia entra en abandono legal y puede ser rematada.'),
('REIMPORTACION_PLAZO', 'Plazo maximo para reimportacion en el mismo estado', 'BLOCKING', 'LGA art 108', 'DESPA-PG.26', '1.0', TRUE, 'La reimportacion en el mismo estado debe realizarse dentro de 36 meses desde la exportacion precedente.'),
('ADMISION_TEMPORAL_PLAZO', 'Plazo maximo admision temporal para reexportacion', 'BLOCKING', 'LGA art 112', 'DESPA-PG.04', '1.0', TRUE, 'La mercancia bajo admision temporal puede permanecer hasta 540 dias prorrogables.'),
('ADMISION_TEMPORAL_GARANTIA', 'Garantia obligatoria admision temporal', 'WARNING', 'LGA art 113', 'DESPA-PG.04', '1.0', TRUE, 'Se debe constituir garantia por tributos suspendidos ante la SAF.'),
('TRANSITO_RUTA', 'Ruta autorizada para transito aduanero', 'BLOCKING', 'LGA art 161', 'DESPA-PG.08', '1.0', TRUE, 'El transito debe realizarse por la ruta autorizada por SUNAT sin desviaciones.'),
('TRANSITO_PLAZO', 'Plazo general para transito aduanero', 'BLOCKING', 'LGA art 161', 'DESPA-PG.08', '1.0', TRUE, 'El transito aduanero debe completarse en un plazo maximo de 30 dias calendario desde el levante.'),
('TRANSITO_PRECINTOS', 'Precintos obligatorios en transito aduanero', 'BLOCKING', 'LGA art 162', 'DESPA-PG.08', '1.0', TRUE, 'Los precintos aduaneros son obligatorios para garantizar la integridad de la carga en transito.'),
('TRANSBORDO_MODALIDAD', 'Modalidades de transbordo autorizadas', 'WARNING', 'LGA art 165', 'DESPA-PG.11', '1.0', TRUE, 'El transbordo puede ser directo (buque a buque) o indirecto (con deposito temporal).'),
('TRANSBORDO_PLAZO', 'Plazo para reembarque en transbordo', 'BLOCKING', 'LGA art 165', 'DESPA-PG.11', '1.0', TRUE, 'El reembarque debe realizarse dentro de 30 dias calendario desde la descarga.'),
('MERCANCIA_RESTRINGIDA', 'Permiso sectorial obligatorio para mercancias restringidas', 'BLOCKING', 'LGA art 63, DS 007-98-SA', 'VUCE', '1.0', TRUE, 'Las mercancias restringidas requieren permiso sectorial previo (VUCE) antes del despacho.'),
('PREDAM_MANIFIESTO', 'PRE-DAM bloqueada sin manifiesto de carga', 'BLOCKING', 'LGA art 40, DESPA-PG.01', 'DESPA-PG.01', '1.0', TRUE, 'No se puede generar PRE-DAM sin manifiesto de carga registrado para la operacion.'),
('PREDAM_FOB', 'PRE-DAM bloqueada sin valor FOB valido', 'BLOCKING', 'LGA art 30, DESPA-PG.01', 'DESPA-PG.01', '1.0', TRUE, 'El valor FOB es obligatorio y debe ser mayor o igual a 1 USD para generar PRE-DAM.'),
('PREDAM_HS', 'PRE-DAM bloqueada sin codigo HS', 'BLOCKING', 'LGA art 30, DESPA-PG.01 anexo 8', 'DESPA-PG.01', '1.0', TRUE, 'El codigo de subpartida nacional (HS) es obligatorio para la clasificacion arancelaria.'),
('PREDAM_TRANSPORTE', 'PRE-DAM bloqueada sin documento de transporte', 'BLOCKING', 'LGA art 40', 'DESPA-PG.01', '1.0', TRUE, 'Debe existir un documento de transporte (BL/AWB) vinculado al manifiesto.'),
('DTA_TIPO_CAMBIO', 'DTA requiere tipo de cambio obligatorio', 'BLOCKING', 'LGA art 30, Ley 27681', 'DESPA-PG.01', '1.0', TRUE, 'El tipo de cambio es obligatorio para la conversion de valores en la DTA.');

-- =====================================================================
-- TABLA 2: restricciones_hs
-- =====================================================================
CREATE TABLE IF NOT EXISTS restricciones_hs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rango_hs VARCHAR(20) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  entidad VARCHAR(100) NOT NULL,
  documento_requerido VARCHAR(255),
  base_legal TEXT,
  vigencia VARCHAR(50) DEFAULT 'Vigente',
  fuente_url VARCHAR(500),
  version VARCHAR(50) DEFAULT '1.0',
  vigente BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_rh_rango (rango_hs),
  INDEX idx_rh_entidad (entidad),
  INDEX idx_rh_vigente (vigente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO restricciones_hs (rango_hs, descripcion, entidad, documento_requerido, base_legal, vigencia, fuente_url) VALUES
('01-02', 'Animales vivos y productos del reino animal', 'SENASA', 'Certificado Zoosanitario de Importacion', 'DS 016-2009-AG', 'Vigente', 'https://www.senasa.gob.pe'),
('03', 'Pescados y crustaceos', 'SANIPES / SENASA', 'Certificado Sanitario / Certificado Zoosanitario', 'DS 011-2017-PRODUCE, DS 016-2009-AG', 'Vigente', 'https://www.sanipes.gob.pe'),
('07-08', 'Legumbres, hortalizas y frutas', 'SENASA', 'Certificado Fitosanitario de Importacion', 'DS 016-2009-AG', 'Vigente', 'https://www.senasa.gob.pe'),
('28-38', 'Productos quimicos y farmaceuticos', 'DIGEMID / MINAM', 'Registro Sanitario / Certificado de Libre Venta / MSDS', 'DS 007-98-SA, Ley 28256', 'Vigente', 'https://www.digemid.minsa.gob.pe'),
('84-85', 'Maquinas, aparatos y material electrico', 'MTC', 'Homologacion / Certificado de Conformidad / Registro de Equipos', 'DS 008-2017-MTC', 'Vigente', 'https://www.mtc.gob.pe'),
('87', 'Vehiculos automoviles', 'MTC', 'Certificado de Homologacion Vehicular / Registro Unico de Vehiculos', 'DS 058-2003-MTC', 'Vigente', 'https://www.mtc.gob.pe'),
('93', 'Armas y municiones', 'SUCAMEC', 'Licencia de Importacion / Certificado de Control', 'Ley 30299, DS 010-2015-IN', 'Vigente', 'https://www.sucamec.gob.pe'),
('95', 'Juguetes y articulos de recreo', 'DIGESA', 'Registro Sanitario / Certificado de Seguridad', 'DS 010-2015-SA, Ley 28376', 'Vigente', 'https://www.digesa.minsa.gob.pe'),
('CITES', 'Especies CITES (flora y fauna protegida)', 'SERFOR / ATFFS', 'Permiso CITES de exportacion / Certificado de origen legal', 'Ley 29763, DS 019-2015-MINAGRI', 'Vigente', 'https://www.serfor.gob.pe'),
('97', 'Obras de arte, antiguedades y colecciones', 'Ministerio de Cultura', 'Certificado de Inexportabilidad / Registro de Obras', 'Ley 28296, DS 017-2003-ED', 'Vigente', 'https://www.cultura.gob.pe');

-- =====================================================================
-- TABLA 3: reglas_plazo
-- =====================================================================
CREATE TABLE IF NOT EXISTS reglas_plazo (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(80) NOT NULL UNIQUE,
  label VARCHAR(255) NOT NULL,
  evento_base VARCHAR(80),
  plazo_dias INT NOT NULL,
  regimen VARCHAR(20),
  modalidad VARCHAR(40),
  via VARCHAR(30),
  norma_fuente VARCHAR(255),
  version VARCHAR(50),
  vigente BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_rp_regimen (regimen),
  INDEX idx_rp_vigente (vigente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO reglas_plazo (codigo, label, evento_base, plazo_dias, regimen, modalidad, via, norma_fuente, version) VALUES
('DIFERIDO_10', 'Importacion diferida - plazo destinacion', 'TERMINO_DESCARGA', 15, '10', 'DIFERIDO', 'MARITIMA', 'DESPA-PG.01 / LGA art 150', '1.0'),
('ANTICIPADO_10', 'Importacion anticipada - plazo antes de llegada', 'FECHA_LLEGADA', 30, '10', 'ANTICIPADO', 'AEREA', 'DESPA-PG.01 / LGA art 150', '1.0'),
('REIMPORTACION_23', 'Reimportacion - plazo maximo desde exportacion', 'FECHA_EMBARQUE', 1095, '23', NULL, NULL, 'DESPA-PG.26 / LGA art 108', '1.0'),
('ADMISION_TEMPORAL_21', 'Admision temporal - plazo maximo', 'FECHA_LEVANTE', 540, '21', NULL, NULL, 'DESPA-PG.04 / LGA art 112', '1.0'),
('TRANSITO_50', 'Transito aduanero - plazo general', 'FECHA_LEVANTE', 30, '50', NULL, NULL, 'DESPA-PG.08 / LGA art 161', '1.0'),
('TRANSBORDO_60', 'Transbordo - plazo para reembarque', 'FECHA_NUMERACION', 30, '60', NULL, NULL, 'DESPA-PG.11 / LGA art 165', '1.0'),
('ABANDONO_DIFERIDO', 'Abandono legal - plazo desde termino descarga', 'TERMINO_DESCARGA', 30, '10', 'DIFERIDO', 'MARITIMA', 'DESPA-PG.01 / LGA art 155', '1.0'),
('ABANDONO_ANTICIPADO', 'Abandono legal - plazo desde llegada', 'FECHA_LLEGADA', 15, '10', 'ANTICIPADO', 'AEREA', 'DESPA-PG.01 / LGA art 155', '1.0');

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;

SELECT 'Seed normativa completado exitosamente.' AS resultado;
