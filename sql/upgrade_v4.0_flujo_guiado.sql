-- ================================================================
-- UPGRADE v4.0 - FLUJO GUIADO + MATRIZ RESTRICCIONES + NORMATIVA
-- ================================================================

-- 1. Tabla de persistencia para pasos del wizard guiado
CREATE TABLE IF NOT EXISTS expediente_guided_steps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    expediente_id INT NOT NULL,
    paso_numero INT NOT NULL CHECK (paso_numero BETWEEN 1 AND 8),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    bloqueado BOOLEAN DEFAULT FALSE,
    motivo_bloqueo TEXT,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_expediente_paso (expediente_id, paso_numero),
    INDEX idx_expediente (expediente_id),
    FOREIGN KEY (expediente_id) REFERENCES operaciones(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabla formal de restricciones por HS Code
CREATE TABLE IF NOT EXISTS restricciones_formal (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rango_hs VARCHAR(20) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    entidad VARCHAR(100) NOT NULL,
    permiso_requerido VARCHAR(255) NOT NULL,
    vigencia VARCHAR(20) DEFAULT 'Vigente',
    fuente_base_legal VARCHAR(255) NOT NULL,
    fuente_url VARCHAR(255),
    version VARCHAR(20) DEFAULT '1.0',
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rango_hs (rango_hs),
    INDEX idx_entidad (entidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO restricciones_formal (rango_hs, descripcion, entidad, permiso_requerido, vigencia, fuente_base_legal, fuente_url) VALUES
('01-02', 'Animales vivos y productos del reino animal', 'SENASA', 'Certificado Zoosanitario de Importacion', 'Vigente', 'DS 016-2009-AG', 'https://www.senasa.gob.pe'),
('03', 'Pescados y crustaceos', 'SANIPES / SENASA', 'Certificado Sanitario / Certificado Zoosanitario', 'Vigente', 'DS 011-2017-PRODUCE, DS 016-2009-AG', 'https://www.sanipes.gob.pe'),
('07-08', 'Legumbres, hortalizas y frutas', 'SENASA', 'Certificado Fitosanitario de Importacion', 'Vigente', 'DS 016-2009-AG', 'https://www.senasa.gob.pe'),
('28-38', 'Productos quimicos y farmaceuticos', 'DIGEMID / MINAM', 'Registro Sanitario / Certificado de Libre Venta / MSDS', 'Vigente', 'DS 007-98-SA, Ley 28256', 'https://www.digemid.minsa.gob.pe'),
('84-85', 'Maquinas, aparatos y material electrico', 'MTC', 'Homologacion / Certificado de Conformidad', 'Vigente', 'DS 008-2017-MTC', 'https://www.mtc.gob.pe'),
('87', 'Vehiculos automoviles', 'MTC', 'Certificado de Homologacion Vehicular', 'Vigente', 'DS 058-2003-MTC', 'https://www.mtc.gob.pe'),
('93', 'Armas y municiones', 'SUCAMEC', 'Licencia de Importacion / Certificado de Control', 'Vigente', 'Ley 30299, DS 010-2015-IN', 'https://www.sucamec.gob.pe'),
('95', 'Juguetes y articulos de recreo', 'DIGESA', 'Registro Sanitario / Certificado de Seguridad', 'Vigente', 'DS 010-2015-SA, Ley 28376', 'https://www.digesa.minsa.gob.pe'),
('CITES', 'Especies CITES (flora y fauna protegida)', 'SERFOR / ATFFS', 'Permiso CITES de exportacion / Certificado de origen legal', 'Vigente', 'Ley 29763, DS 019-2015-MINAGRI', 'https://www.serfor.gob.pe'),
('97', 'Obras de arte, antiguedades y colecciones', 'Ministerio de Cultura', 'Certificado de Inexportabilidad', 'Vigente', 'Ley 28296, DS 017-2003-ED', 'https://www.cultura.gob.pe'),
('4407', 'Madera aserrada o desbastada', 'SERFOR', 'Permiso de aprovechamiento forestal / Certificado CITES', 'Vigente', 'Ley 29763', 'https://www.serfor.gob.pe'),
('9018', 'Instrumentos y aparatos de medicina', 'DIGEMID / MTC', 'Registro Sanitario / Homologacion de Telecomunicaciones', 'Vigente', 'DS 007-98-SA, DS 008-2017-MTC', 'https://www.digemid.minsa.gob.pe'),
('2106', 'Preparaciones alimenticias no expresadas', 'DIGESA', 'Registro Sanitario de Alimentos', 'Vigente', 'DS 010-2015-SA', 'https://www.digesa.minsa.gob.pe'),
('3303-3304', 'Perfumes y cosmeticos', 'DIGESA', 'Registro Sanitario / Notificacion Obligatoria', 'Vigente', 'DS 010-2015-SA', 'https://www.digesa.minsa.gob.pe'),
('1209', 'Semillas y frutos para siembra', 'SENASA', 'Certificado Fitosanitario / Registro de Semillas', 'Vigente', 'DS 016-2009-AG', 'https://www.senasa.gob.pe'),
('8517', 'Aparatos de telecomunicacion', 'MTC', 'Homologacion de Equipos / Certificado de Conformidad', 'Vigente', 'DS 008-2017-MTC', 'https://www.mtc.gob.pe');

-- 3. Actualizar fechas en manifiestos_carga para evitar NULL en plazos
UPDATE manifiestos_carga SET fecha_llegada = CURRENT_TIMESTAMP WHERE fecha_llegada IS NULL;
UPDATE manifiestos_carga SET fecha_termino_descarga = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 2 DAY) WHERE fecha_termino_descarga IS NULL;

-- 4. Asegurar fuente estandarizada en tablas existentes
UPDATE dam_cabecera SET source_type = 'SIMULATED' WHERE source_type IN ('SIMULADO', 'SIMULACION');
UPDATE deuda_tributaria_aduanera SET source_type = 'REFERENTIAL' WHERE source_type IN ('ESTIMADO', 'ESTIMACION');
