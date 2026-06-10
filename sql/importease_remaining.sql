CREATE INDEX idx_reglas_entidad ON reglas_restriccion(codigo_entidad);
CREATE INDEX idx_reglas_partida ON reglas_restriccion(partida_hs);
CREATE INDEX idx_solicitudes_usuario ON solicitudes_permiso(usuario_id);
CREATE INDEX idx_solicitudes_operacion ON solicitudes_permiso(operacion_id);
CREATE INDEX idx_solicitudes_estado ON solicitudes_permiso(estado);
CREATE INDEX idx_preguntas_entidad ON preguntas_permiso(codigo_entidad);
CREATE INDEX idx_documentos_entidad ON documentos_permiso(codigo_entidad);

-- ============================================================
-- ImportEase v2.2 - Semillas Adicionales (SERFOR, PRODUCE, SANIPES) y Tipo de Cambio
-- ============================================================

-- Tabla de Tipo de Cambio Diario
CREATE TABLE IF NOT EXISTS tipo_cambio_diario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE UNIQUE NOT NULL,
    compra DECIMAL(10,4) NOT NULL,
    venta DECIMAL(10,4) NOT NULL,
    fuente VARCHAR(50) DEFAULT 'BCRP',
    fecha_consulta TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Preguntas de permiso adicionales
INSERT INTO preguntas_permiso (codigo_entidad, pregunta, tipo_respuesta, obligatoria, orden) VALUES
('SERFOR', 'Â¿El producto forestal proviene de una especie protegida CITES (ApÃ©ndice I, II o III)?', 'BOOLEAN', TRUE, 1),
('SERFOR', 'Â¿Cuenta con Certificado de Origen emitido por la autoridad forestal del paÃ­s exportador?', 'BOOLEAN', TRUE, 2),
('SERFOR', 'Â¿CuÃ¡l es el volumen en metros cÃºbicos o kilogramos de la madera/flora?', 'TEXT', TRUE, 3),

('PRODUCE', 'Â¿El insumo quÃ­mico estÃ¡ incluido en la lista de Insumos QuÃ­micos y Bienes Fiscalizados (IQBF)?', 'BOOLEAN', TRUE, 1),
('PRODUCE', 'Â¿El importador cuenta con Registro Vigente en el Panel de Control de IQBF de SUNAT/PRODUCE?', 'BOOLEAN', TRUE, 2),
('PRODUCE', 'Â¿CuÃ¡l es el uso final declarado del producto quÃ­mico fiscalizado?', 'TEXT', TRUE, 3),

('SANIPES', 'Â¿Los productos hidrobiolÃ³gicos cuentan con Certificado Sanitario oficial de origen?', 'BOOLEAN', TRUE, 1),
('SANIPES', 'Â¿El establecimiento pesquero extranjero de origen estÃ¡ habilitado por SANIPES?', 'BOOLEAN', TRUE, 2),
('SANIPES', 'Â¿El lote requiere inspecciÃ³n fÃ­sico-organolÃ©ptica en el punto de ingreso?', 'BOOLEAN', TRUE, 3)
ON DUPLICATE KEY UPDATE pregunta = VALUES(pregunta);

-- Documentos de permiso adicionales
INSERT INTO documentos_permiso (codigo_entidad, tipo_permiso, nombre_documento, descripcion, obligatorio, formato_aceptado) VALUES
('SERFOR', 'Permiso CITES / AutorizaciÃ³n Forestal', 'Permiso CITES de ImportaciÃ³n (si aplica)', 'AutorizaciÃ³n CITES oficial emitida por SERFOR para especies protegidas.', FALSE, 'PDF'),
('SERFOR', 'Permiso CITES / AutorizaciÃ³n Forestal', 'Certificado de Origen Forestal', 'Documento oficial del paÃ­s exportador que certifica el origen legal de la madera/flora.', TRUE, 'PDF'),
('SERFOR', 'Permiso CITES / AutorizaciÃ³n Forestal', 'GuÃ­a de Transporte Forestal o de Fauna', 'DocumentaciÃ³n de trazabilidad de transporte en origen.', TRUE, 'PDF'),

('PRODUCE', 'Registro IQBF (Insumos QuÃ­micos y Bienes Fiscalizados)', 'Registro de Control de IQBF', 'AutorizaciÃ³n vigente para internamiento de insumos fiscalizados.', TRUE, 'PDF'),
('PRODUCE', 'Registro IQBF (Insumos QuÃ­micos y Bienes Fiscalizados)', 'Ficha de Registro Ãšnico de Control', 'DocumentaciÃ³n SUNAT/PRODUCE del importador fiscalizado.', TRUE, 'PDF'),
('PRODUCE', 'Registro IQBF (Insumos QuÃ­micos y Bienes Fiscalizados)', 'Hoja de Datos de Seguridad (MSDS)', 'Hoja de seguridad quÃ­mica internacional del fabricante.', TRUE, 'PDF'),

('SANIPES', 'HabilitaciÃ³n Sanitaria de Establecimiento Pesquero', 'Certificado Sanitario de Origen Pesquero', 'Emitido por la autoridad sanitaria pesquera del paÃ­s exportador.', TRUE, 'PDF'),
('SANIPES', 'HabilitaciÃ³n Sanitaria de Establecimiento Pesquero', 'Protocolo de AnÃ¡lisis del Lote', 'AnÃ¡lisis microbiolÃ³gico y quÃ­mico del lote emitido en origen.', TRUE, 'PDF'),
('SANIPES', 'HabilitaciÃ³n Sanitaria de Establecimiento Pesquero', 'HabilitaciÃ³n Sanitaria del Establecimiento Extranjero', 'ResoluciÃ³n de habilitaciÃ³n emitida por SANIPES para la planta de origen.', TRUE, 'PDF')
ON DUPLICATE KEY UPDATE nombre_documento = VALUES(nombre_documento);

-- ============================================================
-- ImportEase v2.3 - Semillas Operacionales y Expedientes pre-VUCE
-- ============================================================

-- Limpieza de semillas previas
DELETE FROM solicitud_permiso_datos WHERE solicitud_permiso_id IN (201, 202, 203, 204, 205, 206, 207);
DELETE FROM solicitudes_permiso WHERE id IN (201, 202, 203, 204, 205, 206, 207);
DELETE FROM operaciones WHERE id IN (101, 102, 103, 104, 105, 106, 107);

-- Insertar Operaciones reales
INSERT INTO operaciones (
    id, usuario_id, producto_desc, hs_code, pais_origen, incoterm, fob, flete, seguro, cif,
    tipo_cambio, ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado,
    percepcion_aplicada, total_impuestos, canal_asignado, estado, numero_dam,
    fecha_creacion, documento_factura, documento_bl, documento_certificado_origen, permiso_vuce_obtenido, entidad_vuce
) VALUES
(101, 1, 'Xiaomi Redmi Note 13 5G - Smartphones con WiFi y Bluetooth', '8517130000', 'CN', 'FOB', 12500.00, 850.00, 210.00, 13560.00,
 3.745, 0.00, 0.00, 2170.00, 271.00, 480.00, 2921.00, 'NARANJA', 'LISTA_DESPACHO', 'PRE-DAM-118-2026-10-043219',
 '2026-05-10 10:00:00', 1, 1, 0, 1, 'MTC'),

(102, 1, 'Whey Protein Gold Standard - Suplemento Alimenticio 5Lbs', '2106909000', 'US', 'CIF', 6400.00, 450.00, 80.00, 6930.00,
 3.745, 415.00, 0.00, 1180.00, 147.00, 250.00, 1992.00, 'VERDE', 'NACIONALIZADA', 'PRE-DAM-118-2026-10-184920',
 '2026-05-12 14:30:00', 1, 1, 1, 1, 'DIGESA'),

(103, 1, 'Aparato de Rayos X PortÃ¡til de DiagnÃ³stico ClÃ­nico', '9022140000', 'DE', 'FOB', 45000.00, 3200.00, 890.00, 49090.00,
 3.745, 0.00, 0.00, 7854.00, 981.00, 1750.00, 10585.00, 'ROJO', 'TRANSITO', 'PRE-DAM-118-2026-10-942851',
 '2026-05-18 09:15:00', 1, 1, 0, 0, 'DIGEMID'),

(104, 1, 'Uvas Frescas Red Globe de ExportaciÃ³n', '0806100000', 'CL', 'FOB', 3200.00, 350.00, 50.00, 3600.00,
 3.745, 320.00, 0.00, 576.00, 72.00, 0.00, 968.00, 'PENDIENTE', 'COTIZACION', NULL,
 '2026-05-22 16:45:00', 0, 0, 0, 0, 'SENASA'),

(105, 1, 'Sillas de Comedor de Madera de Roble Macizo', '9401610000', 'CN', 'FOB', 7500.00, 950.00, 120.00, 8570.00,
 3.745, 750.00, 0.00, 1350.00, 168.00, 320.00, 2588.00, 'PENDIENTE', 'DOCUMENTACION', NULL,
 '2026-05-21 11:20:00', 1, 0, 0, 0, 'SERFOR'),

(106, 1, 'Acetona Pura para Laboratorio - Reactivo QuÃ­mico Fiscalizado', '2914110000', 'US', 'FOB', 18000.00, 1950.00, 420.00, 20370.00,
 3.745, 1080.00, 0.00, 3240.00, 405.00, 820.00, 5545.00, 'ROJO', 'TRANSITO', NULL,
 '2026-05-15 08:00:00', 1, 1, 0, 0, 'PRODUCE'),

(107, 1, 'Filetes de SalmÃ³n del AtlÃ¡ntico Congelados', '0304410000', 'CL', 'CIF', 9200.00, 850.00, 110.00, 10160.00,
 3.745, 0.00, 0.00, 1656.00, 207.00, 390.00, 2253.00, 'VERDE', 'LISTA_DESPACHO', 'PRE-DAM-118-2026-10-859420',
 '2026-05-19 13:10:00', 1, 1, 1, 1, 'SANIPES');


-- Insertar Solicitudes de Permiso VUCE
INSERT INTO solicitudes_permiso (
    id, operacion_id, usuario_id, codigo_entidad, tipo_permiso, estado, numero_suce, numero_documento_resolutivo, fecha_creacion, fecha_envio_vuce, fecha_aprobacion, observaciones
) VALUES
(201, 101, 1, 'MTC', 'Certificado de HomologaciÃ³n de Equipos de Telecomunicaciones', 'APROBADO', '20260403192043', 'RD-2026-MTC-08429', '2026-05-10 10:15:00', '2026-05-10 11:00:00', '2026-05-12 16:30:00', 'Aprobado automÃ¡ticamente por homologaciÃ³n simplificada.'),
(202, 102, 1, 'DIGESA', 'Registro Sanitario de Alimentos y Bebidas', 'APROBADO', '20260512083921', 'RS-2026-DIGESA-18402', '2026-05-12 14:45:00', '2026-05-12 15:00:00', '2026-05-15 10:20:00', 'Registro sanitario otorgado por un aÃ±o.'),
(203, 103, 1, 'DIGEMID', 'Registro Sanitario de Dispositivos MÃ©dicos', 'EN_EVALUACION', '20260518938102', NULL, '2026-05-18 09:30:00', '2026-05-18 10:15:00', NULL, 'Expediente tÃ©cnico en evaluaciÃ³n por el Ã¡rea mÃ©dica.'),
(205, 105, 1, 'SERFOR', 'Permiso CITES / AutorizaciÃ³n Forestal', 'EXPEDIENTE_GENERADO', NULL, NULL, '2026-05-21 11:45:00', NULL, NULL, 'Esperando firma digital del representante legal.'),
(206, 106, 1, 'PRODUCE', 'Registro IQBF (Insumos QuÃ­micos y Bienes Fiscalizados)', 'OBSERVADO', '20260515482019', NULL, '2026-05-15 08:30:00', '2026-05-15 09:00:00', NULL, 'ObservaciÃ³n: Falta adjuntar Hoja de Datos de Seguridad MSDS en espaÃ±ol.');


-- Insertar Datos del Expediente
INSERT INTO solicitud_permiso_datos (solicitud_permiso_id, campo, valor, origen_dato) VALUES
(201, 'ruc_importador', '10733100571', 'usuarios.ruc'),
(201, 'razon_social', 'DONGO PALZA MANUEL ANDREE', 'usuarios.razon_social'),
(201, 'email_contacto', 'importador.demo@importease.local', 'usuarios.email'),
(201, 'producto_descripcion', 'Xiaomi Redmi Note 13 5G - Smartphones con WiFi y Bluetooth', 'operaciones.producto_desc'),
(201, 'codigo_hs', '8517130000', 'operaciones.hs_code'),
(201, 'pais_origen', 'China', 'operaciones.pais_origen'),
(201, 'incoterm', 'FOB', 'operaciones.incoterm'),
(201, 'valor_fob_usd', '12500.00', 'operaciones.fob'),
(201, 'valor_flete_usd', '850.00', 'operaciones.flete'),
(201, 'valor_seguro_usd', '210.00', 'operaciones.seguro'),
(201, 'valor_cif_usd', '13560.00', 'operaciones.cif'),
(201, 'fecha_solicitud', '2026-05-10', 'system'),

(202, 'ruc_importador', '10733100571', 'usuarios.ruc'),
(202, 'razon_social', 'DONGO PALZA MANUEL ANDREE', 'usuarios.razon_social'),
(202, 'email_contacto', 'importador.demo@importease.local', 'usuarios.email'),
(202, 'producto_descripcion', 'Whey Protein Gold Standard - Suplemento Alimenticio 5Lbs', 'operaciones.producto_desc'),
(202, 'codigo_hs', '2106909000', 'operaciones.hs_code'),
(202, 'pais_origen', 'Estados Unidos', 'operaciones.pais_origen'),
(202, 'incoterm', 'CIF', 'operaciones.incoterm'),
(202, 'valor_fob_usd', '6400.00', 'operaciones.fob'),
(202, 'valor_flete_usd', '450.00', 'operaciones.flete'),
(202, 'valor_seguro_usd', '80.00', 'operaciones.seguro'),
(202, 'valor_cif_usd', '6930.00', 'operaciones.cif'),
(202, 'fecha_solicitud', '2026-05-12', 'system');

SELECT 'MigraciÃ³n v2.3 y Semillas Operacionales completadas exitosamente.' AS resultado;

-- ============================================================
-- ImportEase v3.0 - MÃ³dulo de SinÃ³nimos, Flujo de Estados y AuditorÃ­a
-- Script de migraciÃ³n para importease_db
-- ============================================================

-- (Railway: ya conectado a la BD)

-- 1. TABLA DE SINÃ“NIMOS ARANCELARIOS (hs_sinonimos)
CREATE TABLE IF NOT EXISTS hs_sinonimos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    termino_usuario VARCHAR(255) NOT NULL UNIQUE,
    termino_tecnico VARCHAR(255) NOT NULL,
    codigo_hs_sugerido VARCHAR(20) NOT NULL,
    prioridad INT DEFAULT 1,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Semillas de SinÃ³nimos Arancelarios
INSERT INTO hs_sinonimos (termino_usuario, termino_tecnico, codigo_hs_sugerido, prioridad) VALUES
('laptop', 'mÃ¡quina automÃ¡tica para tratamiento de datos portÃ¡til', '8471300000', 1),
('notebook', 'mÃ¡quina automÃ¡tica para tratamiento de datos portÃ¡til', '8471300000', 1),
('celular', 'telÃ©fono inteligente', '8517130000', 1),
('smartphone', 'telÃ©fono inteligente', '8517130000', 1),
('router', 'aparato para la recepciÃ³n, conversiÃ³n y transmisiÃ³n de voz/datos', '8517622000', 1),
('perfume', 'perfumes y aguas de tocador', '3303000000', 1),
('crema', 'preparaciones de belleza o de maquillaje y para el cuidado de la piel', '3304990000', 1),
('cosmetico', 'preparaciones de belleza o de maquillaje', '3304990000', 1),
('medicina', 'medicamento para uso humano dosificado', '3004902900', 1),
('medicamento', 'medicamento para uso humano dosificado', '3004902900', 1),
('suplemento', 'preparaciones alimenticias no expresadas ni comprendidas en otra parte', '2106909000', 1),
('juguete', 'juguetes que representan animales o seres no humanos', '9503002200', 1),
('dron', 'vehÃ­culo aÃ©reo no tripulado', '8806210000', 1)
ON DUPLICATE KEY UPDATE termino_tecnico = VALUES(termino_tecnico), codigo_hs_sugerido = VALUES(codigo_hs_sugerido);

-- 2. TABLA DE HISTORIAL DE ESTADOS DE LA OPERACIÃ“N (historial_estado_operacion)
CREATE TABLE IF NOT EXISTS historial_estado_operacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    operacion_id INT NOT NULL,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    usuario_id INT,
    FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. TABLA DE AUDITORÃA Y TRAZABILIDAD DE EVENTOS (auditoria_eventos)
CREATE TABLE IF NOT EXISTS auditoria_eventos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    accion VARCHAR(255) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_direccion VARCHAR(50),
    recurso_afectado VARCHAR(255),
    estado_anterior TEXT,
    estado_nuevo TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. TABLA DE FUENTES Y REVISIÃ“N DE REGLAS ADUANERAS (fuentes_revision_reglas)
CREATE TABLE IF NOT EXISTS fuentes_revision_reglas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    regla_id INT NOT NULL,
    fuente_legal VARCHAR(255) NOT NULL,
    fecha_revision DATE NOT NULL,
    vigente BOOLEAN DEFAULT TRUE,
    observacion TEXT,
    usuario_modifico_id INT,
    FOREIGN KEY (regla_id) REFERENCES reglas_restriccion(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_modifico_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insertar fuentes de revisiÃ³n tÃ©cnica para las reglas actuales
INSERT INTO fuentes_revision_reglas (regla_id, fuente_legal, fecha_revision, vigente, observacion, usuario_modifico_id)
SELECT id, 'Ley de Telecomunicaciones MTC - Directiva NÂ° 004-2022', '2026-05-22', TRUE, 'RevisiÃ³n y validaciÃ³n de la regla de homologaciÃ³n obligatoria para smartphones y equipos con WiFi.', 1
FROM reglas_restriccion WHERE codigo_entidad = 'MTC' LIMIT 3;

INSERT INTO fuentes_revision_reglas (regla_id, fuente_legal, fecha_revision, vigente, observacion, usuario_modifico_id)
SELECT id, 'Reglamento de Inocuidad Alimentaria DIGESA - D.S. NÂ° 007-98-SA', '2026-05-22', TRUE, 'ActualizaciÃ³n del requerimiento de Registro Sanitario para suplementos y alimentos.', 1
FROM reglas_restriccion WHERE codigo_entidad = 'DIGESA' LIMIT 2;

SELECT 'MigraciÃ³n v3.0 y tablas de auditorÃ­a/sinÃ³nimos completadas exitosamente.' AS resultado;

-- ImportEase v3.1 - Base confiable QA
-- Objetivo: trazabilidad minima, fuente/confianza, eventos de usuario y eventos de fuentes.
-- Ejecutar despues de schema.sql y upgrades previos.

DELIMITER $$

CREATE PROCEDURE importease_add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL importease_add_column_if_missing('tipo_cambio_diario', 'fuente_url', 'VARCHAR(500) NULL');
CALL importease_add_column_if_missing('tipo_cambio_diario', 'status_http', 'INT NULL');
CALL importease_add_column_if_missing('tipo_cambio_diario', 'es_fallback', 'BOOLEAN DEFAULT FALSE');
CALL importease_add_column_if_missing('tipo_cambio_diario', 'raw_response_hash', 'VARCHAR(128) NULL');
CALL importease_add_column_if_missing('tipo_cambio_diario', 'fecha_actualizacion', 'TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP');
CALL importease_add_column_if_missing('tipo_cambio_diario', 'fuente_dato', 'VARCHAR(40) DEFAULT ''BD_LOCAL''');
CALL importease_add_column_if_missing('tipo_cambio_diario', 'confianza', 'DECIMAL(5,2) DEFAULT 0.80');

CALL importease_add_column_if_missing('usuarios', 'ruc_validado', 'BOOLEAN DEFAULT FALSE');
CALL importease_add_column_if_missing('usuarios', 'fuente_ruc', 'VARCHAR(80) NULL');
CALL importease_add_column_if_missing('usuarios', 'fecha_validacion_ruc', 'TIMESTAMP NULL');
CALL importease_add_column_if_missing('usuarios', 'estado_ruc', 'VARCHAR(80) NULL');
CALL importease_add_column_if_missing('usuarios', 'condicion_ruc', 'VARCHAR(80) NULL');
CALL importease_add_column_if_missing('usuarios', 'ruc_confianza', 'DECIMAL(5,2) DEFAULT 0.00');

CALL importease_add_column_if_missing('log_busquedas', 'session_id', 'VARCHAR(128) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'termino_normalizado', 'VARCHAR(255) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'hs_seleccionado', 'VARCHAR(20) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'capitulo_hs', 'VARCHAR(2) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'pais_origen', 'VARCHAR(80) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'valor_fob_estimado', 'DECIMAL(12,2) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'modulo_origen', 'VARCHAR(80) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'resultado_count', 'INT DEFAULT 0');
CALL importease_add_column_if_missing('log_busquedas', 'hubo_click', 'BOOLEAN DEFAULT FALSE');
CALL importease_add_column_if_missing('log_busquedas', 'ip_hash', 'VARCHAR(128) NULL');
CALL importease_add_column_if_missing('log_busquedas', 'user_agent_hash', 'VARCHAR(128) NULL');


CREATE TABLE IF NOT EXISTS catalogo_fuentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(80) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    tipo ENUM('OFICIAL','TERCERO','INTERNA','SIMULADA') NOT NULL,
    url_base VARCHAR(500) NULL,
    requiere_credenciales BOOLEAN DEFAULT FALSE,
    estado ENUM('ACTIVA','INACTIVA','PENDIENTE','DEPRECATED') DEFAULT 'ACTIVA',
    prioridad INT DEFAULT 1,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fuente_eventos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuente VARCHAR(80) NOT NULL,
    tipo_evento VARCHAR(80) NOT NULL,
    referencia VARCHAR(120) NULL,
    url VARCHAR(500) NULL,
    metodo_http VARCHAR(10) NULL,
    status_http INT NULL,
    payload_hash VARCHAR(128) NULL,
    resultado ENUM('OK','ERROR','FALLBACK','SIMULADO','TIMEOUT','CACHE') NOT NULL,
    mensaje_error TEXT NULL,
    duracion_ms INT NULL,
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_fuente_eventos_fuente_fecha (fuente, fecha_evento),
    INDEX idx_fuente_eventos_resultado_fecha (resultado, fecha_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eventos_usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NULL,
    session_id VARCHAR(128) NULL,
    evento VARCHAR(80) NOT NULL,
    modulo VARCHAR(80) NOT NULL,
    entidad_tipo VARCHAR(80) NULL,
    entidad_id VARCHAR(80) NULL,
    detalle JSON NULL,
    ip_hash VARCHAR(128) NULL,
    user_agent_hash VARCHAR(128) NULL,
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_eventos_usuario_evento_fecha (evento, fecha_evento),
    INDEX idx_eventos_usuario_usuario_fecha (usuario_id, fecha_evento),
    INDEX idx_eventos_usuario_modulo_fecha (modulo, fecha_evento),
    CONSTRAINT fk_eventos_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO catalogo_fuentes (codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad) VALUES
('BCRP_API', 'Banco Central de Reserva del Peru - API de series estadisticas', 'OFICIAL', 'https://estadisticas.bcrp.gob.pe/estadisticas/series/api/', FALSE, 'ACTIVA', 1),
('SUNAT_ARANCEL', 'SUNAT/Aduanet - tratamiento arancelario referencial', 'OFICIAL', 'https://www.sunat.gob.pe/', FALSE, 'PENDIENTE', 2),
('VUCE_WEB', 'Ventanilla Unica de Comercio Exterior', 'OFICIAL', 'https://www.vuce.gob.pe/', FALSE, 'PENDIENTE', 3),
('BD_LOCAL', 'Base local ImportEase', 'INTERNA', NULL, FALSE, 'ACTIVA', 4),
('FALLBACK_INTERNO', 'Fallback interno de contingencia', 'SIMULADA', NULL, FALSE, 'ACTIVA', 9),
('SIMULADOR_RUC', 'Simulador RUC local solo para demo', 'SIMULADA', NULL, FALSE, 'DEPRECATED', 10)
ON DUPLICATE KEY UPDATE
    nombre = VALUES(nombre),
    tipo = VALUES(tipo),
    url_base = VALUES(url_base),
    requiere_credenciales = VALUES(requiere_credenciales),
    estado = VALUES(estado),
    prioridad = VALUES(prioridad);

DELIMITER $$

CREATE PROCEDURE importease_add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table_name, '(', p_index_definition, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL importease_add_index_if_missing('log_busquedas', 'idx_log_busquedas_termino_fecha', 'termino, fecha_busqueda');
CALL importease_add_index_if_missing('log_busquedas', 'idx_log_busquedas_hs_fecha', 'resultado_hs_code, fecha_busqueda');


-- ImportEase v3.2 - Observatorio Comercial por HS
-- Version limpia para presentacion: no incluye Radar Logistico.
-- Ejecutar despues de upgrade_v3.1_trazabilidad_base.sql.

CREATE TABLE IF NOT EXISTS trade_import_stats (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20) NOT NULL,
  hs6 VARCHAR(6) NOT NULL,
  pais_origen VARCHAR(120) NOT NULL,
  pais_destino VARCHAR(80) DEFAULT 'Peru',
  anio INT NOT NULL,
  valor_usd DECIMAL(18,2) NULL,
  cantidad DECIMAL(18,2) NULL,
  unidad VARCHAR(40) NULL,
  fuente VARCHAR(80) DEFAULT 'UN_COMTRADE_API',
  source_type VARCHAR(40) DEFAULT 'CACHE',
  confidence DECIMAL(5,2) DEFAULT 0.80,
  fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_trade_hs_anio (hs6, anio),
  INDEX idx_trade_origen (pais_origen)
);

CREATE TABLE IF NOT EXISTS trade_oportunidades (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20) NOT NULL,
  hs6 VARCHAR(6) NOT NULL,
  score_oportunidad INT NOT NULL,
  tendencia VARCHAR(40) NULL,
  pais_recomendado VARCHAR(120) NULL,
  justificacion TEXT NULL,
  fuente VARCHAR(80) DEFAULT 'UN_COMTRADE_API',
  source_type VARCHAR(40) DEFAULT 'CACHE',
  confidence DECIMAL(5,2) DEFAULT 0.80,
  fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_trade_oportunidad_hs_fecha (hs6, fecha_calculo)
);

CREATE TABLE IF NOT EXISTS trade_sync_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20) NOT NULL,
  endpoint VARCHAR(500) NULL,
  status_http INT NULL,
  resultado ENUM('OK','ERROR','PENDIENTE_CREDENCIALES','CACHE','SIMULADO') NOT NULL,
  mensaje TEXT NULL,
  fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_trade_sync_hs_fecha (hs_code, fecha_sync),
  INDEX idx_trade_sync_resultado_fecha (resultado, fecha_sync)
);

INSERT INTO catalogo_fuentes
  (codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad)
VALUES
  ('UN_COMTRADE_API', 'UN Comtrade API', 'OFICIAL', 'https://comtradeplus.un.org/', TRUE, 'PENDIENTE', 2)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  tipo = VALUES(tipo),
  url_base = VALUES(url_base),
  requiere_credenciales = VALUES(requiere_credenciales),
  estado = VALUES(estado),
  prioridad = VALUES(prioridad);

-- ImportEase v3.2 - Radar logistico y Observatorio Comercial por HS
-- Ejecutar despues de upgrade_v3.1_trazabilidad_base.sql.

CREATE TABLE IF NOT EXISTS tracking_fuentes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  proveedor VARCHAR(40) UNIQUE NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  api_url VARCHAR(500) NULL,
  developer_url VARCHAR(500) NULL,
  requiere_credenciales BOOLEAN DEFAULT TRUE,
  estado ENUM('ACTIVA','INACTIVA','PENDIENTE_CREDENCIALES','DEPRECATED') DEFAULT 'PENDIENTE_CREDENCIALES',
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tracking_fuentes (proveedor, nombre, api_url, developer_url, requiere_credenciales, estado) VALUES
('DHL', 'DHL Shipment Tracking API', 'https://api-eu.dhl.com/track/shipments', 'https://developer.dhl.com/api-reference/shipment-tracking', TRUE, 'PENDIENTE_CREDENCIALES'),
('FEDEX', 'FedEx Track API', 'https://apis.fedex.com/track/v1/trackingnumbers', 'https://developer.fedex.com/api/en-fm/catalog/track.html', TRUE, 'PENDIENTE_CREDENCIALES'),
('UPS', 'UPS Tracking API', 'https://onlinetools.ups.com/api/track/v1/details', 'https://developer.ups.com/', TRUE, 'PENDIENTE_CREDENCIALES'),
('MAERSK', 'Maersk Track and Trace API', 'https://api.maersk.com/track-and-trace', 'https://developer.maersk.com/api-catalogue', TRUE, 'PENDIENTE_CREDENCIALES'),
('OTRO', 'Registro manual verificable', NULL, NULL, FALSE, 'ACTIVA')
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  api_url = VALUES(api_url),
  developer_url = VALUES(developer_url),
  requiere_credenciales = VALUES(requiere_credenciales);

CREATE TABLE IF NOT EXISTS tracking_envios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NULL,
  operacion_id INT NULL,
  proveedor VARCHAR(40) NOT NULL,
  tracking_number VARCHAR(120) NULL,
  bl_number VARCHAR(120) NULL,
  container_number VARCHAR(120) NULL,
  eta DATE NULL,
  estado_actual VARCHAR(80) DEFAULT 'REGISTRADO',
  source VARCHAR(80) DEFAULT 'MANUAL_VERIFICADO',
  source_type VARCHAR(40) DEFAULT 'MANUAL_VERIFICADO',
  confidence DECIMAL(5,2) DEFAULT 0.65,
  ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tracking_usuario_fecha (usuario_id, fecha_creacion),
  INDEX idx_tracking_operacion (operacion_id),
  INDEX idx_tracking_proveedor (proveedor)
);

CREATE TABLE IF NOT EXISTS tracking_eventos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tracking_id BIGINT NOT NULL,
  fecha_evento TIMESTAMP NULL,
  ubicacion VARCHAR(160) NULL,
  estado VARCHAR(80) NOT NULL,
  descripcion TEXT NULL,
  fuente VARCHAR(80) DEFAULT 'MANUAL',
  source_type VARCHAR(40) DEFAULT 'MANUAL_VERIFICADO',
  confidence DECIMAL(5,2) DEFAULT 0.65,
  raw_payload_json JSON NULL,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_tracking_eventos_envio FOREIGN KEY (tracking_id) REFERENCES tracking_envios(id) ON DELETE CASCADE,
  INDEX idx_tracking_eventos_tracking_fecha (tracking_id, fecha_evento)
);

CREATE TABLE IF NOT EXISTS tracking_sync_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tracking_id BIGINT NULL,
  proveedor VARCHAR(40) NOT NULL,
  endpoint VARCHAR(500) NULL,
  status_http INT NULL,
  resultado ENUM('OK','ERROR','PENDIENTE_CREDENCIALES','CACHE','MANUAL_VERIFICADO','SIMULADO') NOT NULL,
  mensaje TEXT NULL,
  fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tracking_sync_proveedor_fecha (proveedor, fecha_sync),
  INDEX idx_tracking_sync_resultado_fecha (resultado, fecha_sync)
);

CREATE TABLE IF NOT EXISTS trade_import_stats (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20) NOT NULL,
  hs6 VARCHAR(6) NOT NULL,
  pais_origen VARCHAR(120) NOT NULL,
  pais_destino VARCHAR(80) DEFAULT 'Peru',
  anio INT NOT NULL,
  valor_usd DECIMAL(18,2) NULL,
  cantidad DECIMAL(18,2) NULL,
  unidad VARCHAR(40) NULL,
  fuente VARCHAR(80) DEFAULT 'UN_COMTRADE_API',
  source_type VARCHAR(40) DEFAULT 'CACHE',
  confidence DECIMAL(5,2) DEFAULT 0.80,
  fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_trade_hs_anio (hs6, anio),
  INDEX idx_trade_origen (pais_origen)
);

CREATE TABLE IF NOT EXISTS trade_oportunidades (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20) NOT NULL,
  hs6 VARCHAR(6) NOT NULL,
  score_oportunidad INT NOT NULL,
  tendencia VARCHAR(40) NULL,
  pais_recomendado VARCHAR(120) NULL,
  justificacion TEXT NULL,
  fuente VARCHAR(80) DEFAULT 'UN_COMTRADE_API',
  source_type VARCHAR(40) DEFAULT 'CACHE',
  confidence DECIMAL(5,2) DEFAULT 0.80,
  fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_trade_oportunidad_hs_fecha (hs6, fecha_calculo)
);

CREATE TABLE IF NOT EXISTS trade_sync_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20) NOT NULL,
  endpoint VARCHAR(500) NULL,
  status_http INT NULL,
  resultado ENUM('OK','ERROR','PENDIENTE_CREDENCIALES','CACHE','SIMULADO') NOT NULL,
  mensaje TEXT NULL,
  fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_trade_sync_hs_fecha (hs_code, fecha_sync),
  INDEX idx_trade_sync_resultado_fecha (resultado, fecha_sync)
);

INSERT INTO catalogo_fuentes (codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad)
VALUES
('DHL_API', 'DHL Shipment Tracking API', 'TERCERO', 'https://developer.dhl.com/api-reference/shipment-tracking', TRUE, 'PENDIENTE', 3),
('FEDEX_API', 'FedEx Track API', 'TERCERO', 'https://developer.fedex.com/api/en-fm/catalog/track.html', TRUE, 'PENDIENTE', 3),
('UPS_API', 'UPS Developer Portal', 'TERCERO', 'https://developer.ups.com/', TRUE, 'PENDIENTE', 3),
('MAERSK_API', 'Maersk API Catalogue', 'TERCERO', 'https://developer.maersk.com/api-catalogue', TRUE, 'PENDIENTE', 3),
('UN_COMTRADE_API', 'UN Comtrade API', 'OFICIAL', 'https://comtradeplus.un.org/', TRUE, 'PENDIENTE', 2)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  url_base = VALUES(url_base),
  requiere_credenciales = VALUES(requiere_credenciales);

-- ImportEase v3.3 - Incoterms Lab didactico
-- Mantiene el modulo sin depender de APIs pagadas.

CREATE TABLE IF NOT EXISTS incoterms_2020 (
  codigo VARCHAR(3) PRIMARY KEY,
  nombre VARCHAR(80) NOT NULL,
  modalidad VARCHAR(30) NOT NULL,
  descripcion_resumida TEXT NOT NULL,
  incluye_flete_internacional BOOLEAN DEFAULT FALSE,
  incluye_seguro_internacional BOOLEAN DEFAULT FALSE
);

INSERT INTO incoterms_2020
  (codigo, nombre, modalidad, descripcion_resumida, incluye_flete_internacional, incluye_seguro_internacional)
VALUES
  ('EXW', 'Ex Works', 'MULTIMODAL', 'Retiro en almacen del vendedor; el comprador asume casi toda la operacion.', FALSE, FALSE),
  ('FCA', 'Free Carrier', 'MULTIMODAL', 'Entrega al transportista designado por el comprador.', FALSE, FALSE),
  ('FOB', 'Free On Board', 'MARITIMO', 'Entrega a bordo en puerto de embarque; comun para carga maritima.', FALSE, FALSE),
  ('CFR', 'Cost and Freight', 'MARITIMO', 'Vendedor paga flete hasta destino; seguro no incluido.', TRUE, FALSE),
  ('CIF', 'Cost, Insurance and Freight', 'MARITIMO', 'Vendedor paga costo, seguro y flete hasta destino.', TRUE, TRUE),
  ('CPT', 'Carriage Paid To', 'MULTIMODAL', 'Vendedor paga transporte hasta destino pactado; seguro no incluido.', TRUE, FALSE),
  ('CIP', 'Carriage and Insurance Paid To', 'MULTIMODAL', 'Vendedor paga transporte y seguro hasta destino pactado.', TRUE, TRUE),
  ('DAP', 'Delivered At Place', 'MULTIMODAL', 'Entrega en lugar convenido sin despacho de importacion.', TRUE, FALSE),
  ('DDP', 'Delivered Duty Paid', 'MULTIMODAL', 'Promete entrega con tributos pagados; requiere validar si aplica en Peru.', TRUE, FALSE)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  modalidad = VALUES(modalidad),
  descripcion_resumida = VALUES(descripcion_resumida),
  incluye_flete_internacional = VALUES(incluye_flete_internacional),
  incluye_seguro_internacional = VALUES(incluye_seguro_internacional);

INSERT INTO catalogo_fuentes
  (codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad)
VALUES
  ('ICC_2020_REFERENCIAL', 'ICC Incoterms 2020 - resumen didactico local', 'INTERNA', 'https://iccwbo.org/business-solutions/incoterms-rules/', FALSE, 'ACTIVA', 2)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  tipo = VALUES(tipo),
  url_base = VALUES(url_base),
  requiere_credenciales = VALUES(requiere_credenciales),
  estado = VALUES(estado),
  prioridad = VALUES(prioridad);

-- El Radar Logistico queda fuera de la demo actual.
-- Si existen fuentes courier heredadas en catalogo_fuentes, se bajan de
-- prioridad para no vender una integracion que aun no se usara.
UPDATE catalogo_fuentes
SET estado = 'DEPRECATED', prioridad = 9
WHERE codigo IN ('DHL_API', 'FEDEX_API', 'UPS_API', 'MAERSK_API');

-- ImportEase v3.4 - Expediente Aduanero Inteligente Guiado
-- Capa academica/referencial para regimenes, PRE-DAM, manifiesto, DTA,
-- timeline, alertas, garantias y base legal por operacion.

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS regimenes_aduaneros (
  codigo VARCHAR(20) PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  tipo VARCHAR(80),
  descripcion TEXT,
  permite_consumo BOOLEAN DEFAULT FALSE,
  suspende_tributos BOOLEAN DEFAULT FALSE,
  requiere_garantia BOOLEAN DEFAULT FALSE,
  requiere_regularizacion BOOLEAN DEFAULT FALSE,
  fuente_procedimiento VARCHAR(80),
  source_type VARCHAR(40) DEFAULT 'BD_LOCAL',
  confidence DECIMAL(5,2) DEFAULT 0.85
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS modalidades_despacho (
  id INT AUTO_INCREMENT PRIMARY KEY,
  regimen_codigo VARCHAR(20) NOT NULL,
  codigo VARCHAR(20) NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT,
  plazo_dias INT NULL,
  momento_solicitud VARCHAR(255),
  obligatorio BOOLEAN DEFAULT FALSE,
  source_type VARCHAR(40) DEFAULT 'BD_LOCAL',
  confidence DECIMAL(5,2) DEFAULT 0.85,
  UNIQUE KEY uk_modalidad_regimen_codigo (regimen_codigo, codigo),
  CONSTRAINT fk_modalidad_regimen FOREIGN KEY (regimen_codigo) REFERENCES regimenes_aduaneros(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS normas_aduaneras (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(80) NOT NULL UNIQUE,
  nombre VARCHAR(255) NOT NULL,
  tipo_norma VARCHAR(80),
  entidad VARCHAR(80),
  fecha_publicacion DATE NULL,
  fecha_vigencia DATE NULL,
  version VARCHAR(50),
  url_fuente VARCHAR(500),
  resumen TEXT,
  vigente BOOLEAN DEFAULT TRUE,
  source_type VARCHAR(40) DEFAULT 'BD_LOCAL',
  confidence DECIMAL(5,2) DEFAULT 0.85
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS procedimientos_aduaneros (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(80) NOT NULL UNIQUE,
  nombre VARCHAR(255) NOT NULL,
  regimen_codigo VARCHAR(20),
  objetivo TEXT,
  alcance TEXT,
  responsabilidad TEXT,
  version VARCHAR(50),
  fecha_vigencia DATE NULL,
  url_fuente VARCHAR(500),
  estado VARCHAR(50) DEFAULT 'VIGENTE',
  source_type VARCHAR(40) DEFAULT 'BD_LOCAL',
  confidence DECIMAL(5,2) DEFAULT 0.85,
  CONSTRAINT fk_procedimiento_regimen FOREIGN KEY (regimen_codigo) REFERENCES regimenes_aduaneros(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS procedimiento_base_legal (
  id INT AUTO_INCREMENT PRIMARY KEY,
  procedimiento_id INT NOT NULL,
  norma_id INT NOT NULL,
  articulo VARCHAR(80),
  descripcion TEXT,
  UNIQUE KEY uk_proc_norma_articulo (procedimiento_id, norma_id, articulo),
  CONSTRAINT fk_pbl_proc FOREIGN KEY (procedimiento_id) REFERENCES procedimientos_aduaneros(id),
  CONSTRAINT fk_pbl_norma FOREIGN KEY (norma_id) REFERENCES normas_aduaneras(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS operadores_comercio_exterior (
  id INT AUTO_INCREMENT PRIMARY KEY,
  ruc VARCHAR(20),
  razon_social VARCHAR(255),
  tipo_operador VARCHAR(80),
  codigo_operador VARCHAR(80),
  estado VARCHAR(50) DEFAULT 'REFERENCIAL',
  fuente_validacion VARCHAR(80) DEFAULT 'MANUAL_REFERENCIAL',
  fecha_validacion DATETIME NULL,
  INDEX idx_operadores_tipo (tipo_operador),
  INDEX idx_operadores_ruc (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS manifiestos_carga (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operacion_id INT NULL,
  numero_manifiesto VARCHAR(80) NOT NULL,
  tipo VARCHAR(50),
  via_transporte VARCHAR(30),
  transportista_id INT NULL,
  fecha_llegada DATETIME NULL,
  fecha_termino_descarga DATETIME NULL,
  puerto_arribo VARCHAR(120),
  aduana_codigo VARCHAR(20),
  deposito_temporal VARCHAR(160),
  estado VARCHAR(80) DEFAULT 'REFERENCIAL',
  fuente VARCHAR(80) DEFAULT 'MANUAL_REFERENCIAL',
  source_type VARCHAR(40) DEFAULT 'MANUAL',
  confidence DECIMAL(5,2) DEFAULT 0.50,
  INDEX idx_manifiesto_operacion (operacion_id),
  CONSTRAINT fk_manifiesto_operacion FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE SET NULL,
  CONSTRAINT fk_manifiesto_transportista FOREIGN KEY (transportista_id) REFERENCES operadores_comercio_exterior(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS documentos_transporte (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  manifiesto_id BIGINT,
  tipo_documento VARCHAR(50),
  numero_documento VARCHAR(100),
  master_house VARCHAR(20),
  consignatario_id INT NULL,
  agente_carga_id INT NULL,
  fecha_embarque DATETIME NULL,
  puerto_origen VARCHAR(120),
  puerto_destino VARCHAR(120),
  peso_bruto DECIMAL(15,3),
  bultos INT,
  INDEX idx_doc_transporte_manifiesto (manifiesto_id),
  CONSTRAINT fk_doc_transporte_manifiesto FOREIGN KEY (manifiesto_id) REFERENCES manifiestos_carga(id) ON DELETE CASCADE,
  CONSTRAINT fk_doc_transporte_consignatario FOREIGN KEY (consignatario_id) REFERENCES operadores_comercio_exterior(id) ON DELETE SET NULL,
  CONSTRAINT fk_doc_transporte_agente FOREIGN KEY (agente_carga_id) REFERENCES operadores_comercio_exterior(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contenedores (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  documento_transporte_id BIGINT,
  numero_contenedor VARCHAR(30),
  tipo_contenedor VARCHAR(30),
  precinto_origen VARCHAR(80),
  precinto_aduanero VARCHAR(80),
  estado_precinto VARCHAR(50) DEFAULT 'NO_VERIFICADO',
  peso_manifestado DECIMAL(15,3),
  peso_recibido DECIMAL(15,3),
  INDEX idx_contenedor_doc (documento_transporte_id),
  CONSTRAINT fk_contenedor_doc FOREIGN KEY (documento_transporte_id) REFERENCES documentos_transporte(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dam_cabecera (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operacion_id INT NOT NULL,
  numero_dam VARCHAR(50),
  regimen_codigo VARCHAR(20) NOT NULL,
  modalidad_codigo VARCHAR(20),
  aduana_codigo VARCHAR(20) DEFAULT '118',
  importador_id INT NULL,
  declarante_id INT NULL,
  consignatario_id INT NULL,
  manifiesto_id BIGINT NULL,
  documento_transporte_id BIGINT NULL,
  canal_control VARCHAR(20),
  canal_es_oficial BOOLEAN DEFAULT FALSE,
  estado VARCHAR(80) DEFAULT 'PRE_DAM_REFERENCIAL',
  fecha_numeracion DATETIME NULL,
  fecha_levante DATETIME NULL,
  fecha_regularizacion DATETIME NULL,
  fuente VARCHAR(80) DEFAULT 'SIMULACION_ACADEMICA',
  source_type VARCHAR(40) DEFAULT 'SIMULADO',
  confidence DECIMAL(5,2) DEFAULT 0.20,
  UNIQUE KEY uk_dam_operacion (operacion_id),
  INDEX idx_dam_regimen (regimen_codigo),
  CONSTRAINT fk_dam_operacion FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_dam_regimen FOREIGN KEY (regimen_codigo) REFERENCES regimenes_aduaneros(codigo),
  CONSTRAINT fk_dam_manifiesto FOREIGN KEY (manifiesto_id) REFERENCES manifiestos_carga(id) ON DELETE SET NULL,
  CONSTRAINT fk_dam_doc_transporte FOREIGN KEY (documento_transporte_id) REFERENCES documentos_transporte(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS dam_series (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dam_id BIGINT NOT NULL,
  numero_serie INT NOT NULL,
  hs_code VARCHAR(20),
  descripcion_mercancia TEXT,
  marca VARCHAR(120),
  modelo VARCHAR(120),
  estado_mercancia VARCHAR(50) DEFAULT 'NUEVO',
  pais_origen VARCHAR(20),
  cantidad DECIMAL(15,3) DEFAULT 1.000,
  unidad_medida VARCHAR(20) DEFAULT 'NIU',
  peso_neto DECIMAL(15,3) NULL,
  peso_bruto DECIMAL(15,3) NULL,
  valor_fob DECIMAL(15,2),
  flete DECIMAL(15,2),
  seguro DECIMAL(15,2),
  valor_cif DECIMAL(15,2),
  ad_valorem_pct DECIMAL(5,2) DEFAULT 0.00,
  igv_pct DECIMAL(5,2) DEFAULT 16.00,
  ipm_pct DECIMAL(5,2) DEFAULT 2.00,
  isc_pct DECIMAL(5,2) DEFAULT 0.00,
  requiere_permiso BOOLEAN DEFAULT FALSE,
  UNIQUE KEY uk_dam_serie (dam_id, numero_serie),
  CONSTRAINT fk_dam_series_dam FOREIGN KEY (dam_id) REFERENCES dam_cabecera(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS deuda_tributaria_aduanera (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operacion_id INT NOT NULL,
  dam_id BIGINT NULL,
  fecha_nacimiento DATETIME NULL,
  fecha_exigibilidad DATETIME NULL,
  fecha_cancelacion DATETIME NULL,
  base_imponible_cif DECIMAL(15,2),
  ad_valorem DECIMAL(15,2),
  isc DECIMAL(15,2),
  igv DECIMAL(15,2),
  ipm DECIMAL(15,2),
  percepcion DECIMAL(15,2),
  recargos DECIMAL(15,2) DEFAULT 0.00,
  multas DECIMAL(15,2) DEFAULT 0.00,
  intereses DECIMAL(15,2) DEFAULT 0.00,
  total DECIMAL(15,2),
  estado VARCHAR(50) DEFAULT 'REFERENCIAL',
  source_type VARCHAR(40) DEFAULT 'ESTIMADO',
  confidence DECIMAL(5,2) DEFAULT 0.60,
  UNIQUE KEY uk_dta_operacion (operacion_id),
  CONSTRAINT fk_dta_operacion FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_dta_dam FOREIGN KEY (dam_id) REFERENCES dam_cabecera(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eventos_aduaneros (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operacion_id INT NOT NULL,
  dam_id BIGINT NULL,
  evento_codigo VARCHAR(80) NOT NULL,
  evento_nombre VARCHAR(255) NOT NULL,
  fecha_evento DATETIME NOT NULL,
  responsable_tipo VARCHAR(80),
  responsable_id INT NULL,
  documento_asociado VARCHAR(120),
  efecto_legal TEXT,
  fuente VARCHAR(80) DEFAULT 'SIMULACION_ACADEMICA',
  observacion TEXT,
  source_type VARCHAR(40) DEFAULT 'SIMULADO',
  confidence DECIMAL(5,2) DEFAULT 0.20,
  INDEX idx_eventos_aduaneros_operacion (operacion_id, fecha_evento),
  INDEX idx_eventos_aduaneros_codigo (evento_codigo),
  CONSTRAINT fk_evento_operacion FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_evento_dam FOREIGN KEY (dam_id) REFERENCES dam_cabecera(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS matriz_restricciones_hs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hs_code VARCHAR(20),
  capitulo INT,
  entidad_codigo VARCHAR(20),
  tipo_control VARCHAR(120),
  documento_control VARCHAR(255),
  momento_exigencia VARCHAR(120),
  es_prohibida BOOLEAN DEFAULT FALSE,
  es_restringida BOOLEAN DEFAULT FALSE,
  base_legal TEXT,
  fuente_url VARCHAR(500),
  vigente BOOLEAN DEFAULT TRUE,
  source_type VARCHAR(40) DEFAULT 'BD_LOCAL',
  confidence DECIMAL(5,2) DEFAULT 0.85,
  INDEX idx_matriz_hs (hs_code),
  INDEX idx_matriz_capitulo (capitulo),
  INDEX idx_matriz_entidad (entidad_codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS alertas_regulatorias (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operacion_id INT NOT NULL,
  hs_code VARCHAR(20),
  tipo_alerta VARCHAR(80),
  severidad VARCHAR(30),
  mensaje TEXT,
  base_legal TEXT,
  accion_recomendada TEXT,
  estado VARCHAR(50) DEFAULT 'PENDIENTE',
  fuente VARCHAR(80) DEFAULT 'DESPA_REFERENCIAL',
  source_type VARCHAR(40) DEFAULT 'ESTIMADO',
  confidence DECIMAL(5,2) DEFAULT 0.60,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_alertas_operacion (operacion_id, estado),
  INDEX idx_alertas_severidad (severidad),
  CONSTRAINT fk_alerta_operacion FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS garantias_aduaneras (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operacion_id INT NOT NULL,
  dam_id BIGINT NULL,
  regimen_codigo VARCHAR(20) NOT NULL,
  tipo_garantia VARCHAR(80) DEFAULT 'CARTA_FIANZA',
  monto_minimo DECIMAL(15,2),
  moneda VARCHAR(10) DEFAULT 'USD',
  fecha_emision DATE NULL,
  fecha_vencimiento DATE NULL,
  estado VARCHAR(50) DEFAULT 'PENDIENTE',
  fuente VARCHAR(80) DEFAULT 'DESPA_REFERENCIAL',
  source_type VARCHAR(40) DEFAULT 'ESTIMADO',
  confidence DECIMAL(5,2) DEFAULT 0.60,
  INDEX idx_garantia_operacion (operacion_id),
  CONSTRAINT fk_garantia_operacion FOREIGN KEY (operacion_id) REFERENCES operaciones(id) ON DELETE CASCADE,
  CONSTRAINT fk_garantia_dam FOREIGN KEY (dam_id) REFERENCES dam_cabecera(id) ON DELETE SET NULL,
  CONSTRAINT fk_garantia_regimen FOREIGN KEY (regimen_codigo) REFERENCES regimenes_aduaneros(codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO regimenes_aduaneros
  (codigo, nombre, tipo, descripcion, permite_consumo, suspende_tributos, requiere_garantia, requiere_regularizacion, fuente_procedimiento)
VALUES
  ('10', 'Importacion para el consumo', 'INGRESO', 'Ingreso de mercancias para consumo luego de cumplir formalidades y pago o garantia de tributos.', TRUE, FALSE, FALSE, FALSE, 'DESPA-PG.01'),
  ('80', 'Transito aduanero', 'TRANSITO', 'Traslado bajo control aduanero de una aduana a otra o hacia el exterior con suspension de tributos.', FALSE, TRUE, TRUE, TRUE, 'DESPA-PG.08'),
  ('TAI', 'Transito internacional CAN-ALADI', 'TRANSITO', 'Transito internacional amparado por normativa comunitaria o regional.', FALSE, TRUE, TRUE, TRUE, 'CAN-ALADI'),
  ('TRANSBORDO', 'Transbordo', 'SALIDA', 'Transferencia de mercancias descargadas a otro medio de transporte para su salida.', FALSE, TRUE, FALSE, TRUE, 'DESPA-PG.11'),
  ('ADM_TEMP', 'Admision temporal para reexportacion en el mismo estado', 'TEMPORAL', 'Ingreso temporal con suspension de tributos para fin determinado y reexportacion sin modificacion sustancial.', FALSE, TRUE, TRUE, TRUE, 'DESPA-PG.04'),
  ('36', 'Reimportacion en el mismo estado', 'RETORNO', 'Ingreso de mercancias exportadas definitivamente, sin transformacion, dentro del plazo permitido.', FALSE, FALSE, FALSE, FALSE, 'DESPA-PG.26')
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  descripcion = VALUES(descripcion),
  permite_consumo = VALUES(permite_consumo),
  suspende_tributos = VALUES(suspende_tributos),
  requiere_garantia = VALUES(requiere_garantia),
  requiere_regularizacion = VALUES(requiere_regularizacion),
  fuente_procedimiento = VALUES(fuente_procedimiento);

INSERT INTO modalidades_despacho
  (regimen_codigo, codigo, nombre, descripcion, plazo_dias, momento_solicitud, obligatorio)
VALUES
  ('10', 'ANTICIPADO', 'Despacho anticipado', 'Modalidad general recomendada antes de la llegada, salvo excepciones referenciales.', 30, 'Antes del arribo o dentro del plazo permitido.', TRUE),
  ('10', 'DIFERIDO', 'Despacho diferido', 'Modalidad posterior al arribo cuando aplica una excepcion o decision operativa.', 15, 'Luego del termino de descarga.', FALSE),
  ('10', 'URGENTE', 'Despacho urgente', 'Modalidad para casos que requieren tratamiento inmediato.', 15, 'Antes o despues del arribo segun caso.', FALSE),
  ('80', 'TRANSITO_NACIONAL', 'Transito aduanero nacional', 'Traslado bajo control desde aduana de partida hacia aduana de destino.', 30, 'Desde autorizacion de transito.', FALSE),
  ('TRANSBORDO', 'M1_DIRECTO', 'Directo de un medio a otro', 'Transbordo sin permanencia operativa en deposito.', 30, 'Desde numeracion de la declaracion.', FALSE),
  ('TRANSBORDO', 'M2_TIERRA', 'Con descarga a tierra', 'Transbordo con descarga temporal en zona primaria.', 30, 'Desde numeracion de la declaracion.', FALSE),
  ('TRANSBORDO', 'M3_DEPOSITO', 'Con ingreso a deposito temporal', 'Transbordo con control de deposito temporal antes de salida.', 30, 'Desde numeracion de la declaracion.', FALSE),
  ('ADM_TEMP', 'TEMPORAL', 'Admision temporal', 'Ingreso por plazo maximo referencial de 18 meses, sujeto a garantia y control.', 540, 'Desde levante.', FALSE),
  ('36', 'DIFERIDO_ROJO', 'Despacho diferido con reconocimiento fisico', 'Reimportacion con canal rojo obligatorio en la simulacion academica.', 12, 'Dentro de 12 meses desde termino de embarque previo.', TRUE)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  descripcion = VALUES(descripcion),
  plazo_dias = VALUES(plazo_dias),
  momento_solicitud = VALUES(momento_solicitud),
  obligatorio = VALUES(obligatorio);

INSERT INTO normas_aduaneras
  (codigo, nombre, tipo_norma, entidad, url_fuente, resumen)
VALUES
  ('LGA_REFERENCIAL', 'Ley General de Aduanas - referencia academica', 'LEY', 'SUNAT', 'https://www.sunat.gob.pe/', 'Marco general aduanero usado de forma didactica en ImportEase.'),
  ('DESPA-PG.01', 'Procedimiento de importacion para el consumo - referencia academica', 'PROCEDIMIENTO', 'SUNAT', 'https://www.sunat.gob.pe/', 'Base referencial para modalidad, documentos, canal y plazos de importacion.'),
  ('DESPA-PG.08', 'Procedimiento de transito aduanero - referencia academica', 'PROCEDIMIENTO', 'SUNAT', 'https://www.sunat.gob.pe/', 'Base referencial para transito, garantia y reconocimiento fisico.'),
  ('DESPA-PG.11', 'Procedimiento de transbordo - referencia academica', 'PROCEDIMIENTO', 'SUNAT', 'https://www.sunat.gob.pe/', 'Base referencial para modalidades, plazo de 30 dias y regularizacion.'),
  ('DESPA-PG.04', 'Procedimiento de admision temporal - referencia academica', 'PROCEDIMIENTO', 'SUNAT', 'https://www.sunat.gob.pe/', 'Base referencial para garantia, plazo y reexportacion.'),
  ('DESPA-PG.26', 'Procedimiento de reimportacion en el mismo estado - referencia academica', 'PROCEDIMIENTO', 'SUNAT', 'https://www.sunat.gob.pe/', 'Base referencial para retorno sin transformacion y plazo de 12 meses.')
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  resumen = VALUES(resumen),
  url_fuente = VALUES(url_fuente);

INSERT INTO procedimientos_aduaneros
  (codigo, nombre, regimen_codigo, objetivo, alcance, responsabilidad, version, url_fuente)
VALUES
  ('DESPA-PG.01', 'Importacion para el consumo', '10', 'Destinar mercancias al consumo con pago o garantia de tributos.', 'Operaciones de ingreso definitivo.', 'Importador, agente de aduana y operadores.', 'REFERENCIAL', 'https://www.sunat.gob.pe/'),
  ('DESPA-PG.08', 'Transito aduanero', '80', 'Trasladar mercancias bajo control aduanero.', 'Transito interno y hacia salida/destino.', 'Transportista, declarante y aduanas.', 'REFERENCIAL', 'https://www.sunat.gob.pe/'),
  ('DESPA-PG.11', 'Transbordo', 'TRANSBORDO', 'Transferir mercancias a otro medio para salida.', 'Operaciones de transbordo en zona primaria o deposito.', 'Transportista, agente y deposito.', 'REFERENCIAL', 'https://www.sunat.gob.pe/'),
  ('DESPA-PG.04', 'Admision temporal', 'ADM_TEMP', 'Permitir ingreso temporal con suspension de tributos.', 'Mercancias identificables con fin determinado.', 'Beneficiario, declarante y garante.', 'REFERENCIAL', 'https://www.sunat.gob.pe/'),
  ('DESPA-PG.26', 'Reimportacion en el mismo estado', '36', 'Reingresar mercancias exportadas sin transformacion.', 'Mercancias exportadas definitivamente que retornan.', 'Importador/exportador y aduanas.', 'REFERENCIAL', 'https://www.sunat.gob.pe/')
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  objetivo = VALUES(objetivo),
  alcance = VALUES(alcance),
  responsabilidad = VALUES(responsabilidad);

INSERT INTO matriz_restricciones_hs
  (hs_code, capitulo, entidad_codigo, tipo_control, documento_control, momento_exigencia, es_restringida, base_legal, fuente_url)
VALUES
  ('8517130000', 85, 'MTC', 'Homologacion/equipo de telecomunicaciones', 'Permiso o validacion MTC referencial', 'Antes de numerar o previo al levante segun caso', TRUE, 'Referencia academica vinculada a mercancias restringidas y VUCE.', 'https://www.vuce.gob.pe/'),
  ('2106909000', 21, 'DIGESA', 'Alimentos y suplementos', 'Registro sanitario o autorizacion sanitaria referencial', 'Antes de importacion o nacionalizacion', TRUE, 'Referencia academica vinculada a control sanitario.', 'https://www.vuce.gob.pe/'),
  ('3004902900', 30, 'DIGEMID', 'Productos farmaceuticos', 'Registro sanitario o autorizacion DIGEMID referencial', 'Antes de importacion', TRUE, 'Referencia academica vinculada a control farmaceutico.', 'https://www.vuce.gob.pe/'),
  ('1209999000', 12, 'SENASA', 'Semillas y productos vegetales', 'Permiso fitosanitario referencial', 'Antes de ingreso o nacionalizacion', TRUE, 'Referencia academica vinculada a SENASA.', 'https://www.vuce.gob.pe/')
ON DUPLICATE KEY UPDATE
  entidad_codigo = VALUES(entidad_codigo),
  tipo_control = VALUES(tipo_control),
  documento_control = VALUES(documento_control),
  momento_exigencia = VALUES(momento_exigencia),
  es_restringida = VALUES(es_restringida),
  base_legal = VALUES(base_legal),
  fuente_url = VALUES(fuente_url);

INSERT INTO catalogo_fuentes
  (codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad)
VALUES
  ('DESPA_REFERENCIAL', 'SUNAT DESPA - lectura academica referencial', 'INTERNA', 'https://www.sunat.gob.pe/', FALSE, 'ACTIVA', 2),
  ('SIMULACION_ACADEMICA', 'Simulacion academica ImportEase', 'SIMULADA', NULL, FALSE, 'ACTIVA', 8)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  tipo = VALUES(tipo),
  url_base = VALUES(url_base),
  estado = VALUES(estado),
  prioridad = VALUES(prioridad);

-- ImportEase v3.5 - Informacion real trazable
-- Ejecutar despues de upgrade_v3.4_expediente_aduanero.sql.
-- Objetivo: registrar API oficial, web scraping controlado, web tracking y cache/fallback sin presentar datos simulados como oficiales.

DELIMITER $$

CREATE PROCEDURE importease_add_column_if_missing_v35(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD COLUMN ', p_column_name, ' ', p_column_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE importease_add_index_if_missing_v35(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' ADD INDEX ', p_index_name, ' ', p_index_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CREATE TABLE IF NOT EXISTS fuente_eventos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fuente VARCHAR(80) NOT NULL,
    tipo_evento VARCHAR(80) NOT NULL,
    referencia VARCHAR(120) NULL,
    url VARCHAR(500) NULL,
    metodo_http VARCHAR(10) NULL,
    status_http INT NULL,
    payload_hash VARCHAR(128) NULL,
    resultado ENUM('OK','ERROR','FALLBACK','SIMULADO','TIMEOUT','CACHE') NOT NULL,
    mensaje_error TEXT NULL,
    duracion_ms INT NULL,
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS catalogo_fuentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(80) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    tipo ENUM('OFICIAL','TERCERO','INTERNA','SIMULADA') NOT NULL,
    url_base VARCHAR(500) NULL,
    requiere_credenciales BOOLEAN DEFAULT FALSE,
    estado ENUM('ACTIVA','INACTIVA','PENDIENTE','DEPRECATED') DEFAULT 'ACTIVA',
    prioridad INT DEFAULT 1,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL importease_add_column_if_missing_v35('fuente_eventos', 'tipo_fuente', 'VARCHAR(40) DEFAULT ''BD_LOCAL''');
CALL importease_add_column_if_missing_v35('fuente_eventos', 'entidad_afectada', 'VARCHAR(80) NULL');
CALL importease_add_column_if_missing_v35('fuente_eventos', 'source_type', 'VARCHAR(40) DEFAULT ''BD_LOCAL''');
CALL importease_add_column_if_missing_v35('fuente_eventos', 'confianza', 'DECIMAL(5,2) DEFAULT 0.80');

CALL importease_add_index_if_missing_v35('fuente_eventos', 'idx_fuente_eventos_tipo_fuente_fecha', '(tipo_fuente, fecha_evento)');
CALL importease_add_index_if_missing_v35('fuente_eventos', 'idx_fuente_eventos_source_type_fecha', '(source_type, fecha_evento)');

CALL importease_add_column_if_missing_v35('tipo_cambio_diario', 'moneda_origen', 'VARCHAR(10) DEFAULT ''USD''');
CALL importease_add_column_if_missing_v35('tipo_cambio_diario', 'moneda_destino', 'VARCHAR(10) DEFAULT ''PEN''');
CALL importease_add_column_if_missing_v35('tipo_cambio_diario', 'promedio', 'DECIMAL(10,4) NULL');
CALL importease_add_column_if_missing_v35('tipo_cambio_diario', 'metodo_obtencion', 'VARCHAR(40) DEFAULT ''API_OFICIAL''');

CREATE TABLE IF NOT EXISTS arancel_fuente_sunat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hs_code VARCHAR(20) NOT NULL,
    descripcion TEXT NULL,
    ad_valorem DECIMAL(8,2) NULL,
    restricciones_texto TEXT NULL,
    regimen_asociado VARCHAR(160) NULL,
    fuente_url VARCHAR(500) NULL,
    metodo_obtencion VARCHAR(40) DEFAULT 'WEB_SCRAPING',
    fecha_scraping TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_http INT NULL,
    hash_contenido VARCHAR(128) NULL,
    vigente BOOLEAN DEFAULT TRUE,
    confianza DECIMAL(5,2) DEFAULT 0.90,
    source_type VARCHAR(40) DEFAULT 'OFICIAL_WEB',
    UNIQUE KEY uk_arancel_sunat_hs (hs_code),
    INDEX idx_arancel_sunat_fecha (fecha_scraping),
    INDEX idx_arancel_sunat_vigente (vigente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vuce_tramites_fuente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidad VARCHAR(80) NOT NULL,
    tramite VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(160) NULL,
    hs_code VARCHAR(20) NULL,
    requisito TEXT NULL,
    plazo_estimado VARCHAR(120) NULL,
    fuente_url VARCHAR(500) NULL,
    metodo_obtencion VARCHAR(40) DEFAULT 'BD_LOCAL',
    fecha_revision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado_fuente VARCHAR(80) DEFAULT 'REFERENCIAL',
    confianza DECIMAL(5,2) DEFAULT 0.65,
    source_type VARCHAR(40) DEFAULT 'BD_LOCAL',
    INDEX idx_vuce_hs (hs_code),
    INDEX idx_vuce_entidad (entidad)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS validaciones_ruc (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruc VARCHAR(20) NOT NULL,
    razon_social VARCHAR(255) NULL,
    estado VARCHAR(80) NULL,
    condicion VARCHAR(80) NULL,
    fuente VARCHAR(80) DEFAULT 'PENDIENTE_VALIDACION',
    metodo_obtencion VARCHAR(40) DEFAULT 'MANUAL',
    fecha_validacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status_http INT NULL,
    confianza DECIMAL(5,2) DEFAULT 0.10,
    resultado VARCHAR(80) DEFAULT 'PENDIENTE_VALIDACION',
    UNIQUE KEY uk_validaciones_ruc_ruc (ruc),
    INDEX idx_validaciones_ruc_resultado_fecha (resultado, fecha_validacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comtrade_estadisticas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hs_code VARCHAR(20) NOT NULL,
    hs6 VARCHAR(6) NULL,
    pais_origen VARCHAR(120) NOT NULL,
    pais_destino VARCHAR(80) DEFAULT 'Peru',
    anio INT NOT NULL,
    valor_usd DECIMAL(18,2) NULL,
    cantidad DECIMAL(18,2) NULL,
    unidad VARCHAR(40) NULL,
    flujo VARCHAR(40) DEFAULT 'IMPORT',
    fuente VARCHAR(80) DEFAULT 'UN_COMTRADE_API',
    metodo_obtencion VARCHAR(40) DEFAULT 'API_OFICIAL',
    source_type VARCHAR(40) DEFAULT 'CACHE',
    confianza DECIMAL(5,2) DEFAULT 0.80,
    fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comtrade_hs_anio (hs6, anio),
    INDEX idx_comtrade_pais (pais_origen)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tracking_fuentes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  proveedor VARCHAR(40) UNIQUE NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  api_url VARCHAR(500) NULL,
  developer_url VARCHAR(500) NULL,
  requiere_credenciales BOOLEAN DEFAULT TRUE,
  estado ENUM('ACTIVA','INACTIVA','PENDIENTE_CREDENCIALES','DEPRECATED') DEFAULT 'PENDIENTE_CREDENCIALES',
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tracking_envios (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NULL,
  operacion_id INT NULL,
  proveedor VARCHAR(40) NOT NULL,
  tracking_number VARCHAR(120) NULL,
  bl_number VARCHAR(120) NULL,
  container_number VARCHAR(120) NULL,
  eta DATE NULL,
  estado_actual VARCHAR(80) DEFAULT 'REGISTRADO',
  source VARCHAR(80) DEFAULT 'MANUAL_VERIFICADO',
  source_type VARCHAR(40) DEFAULT 'MANUAL_VERIFICADO',
  confidence DECIMAL(5,2) DEFAULT 0.65,
  ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tracking_usuario_fecha (usuario_id, fecha_creacion),
  INDEX idx_tracking_operacion (operacion_id),
  INDEX idx_tracking_proveedor (proveedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tracking_eventos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tracking_id BIGINT NOT NULL,
  fecha_evento TIMESTAMP NULL,
  ubicacion VARCHAR(160) NULL,
  estado VARCHAR(80) NOT NULL,
  descripcion TEXT NULL,
  fuente VARCHAR(80) DEFAULT 'MANUAL',
  source_type VARCHAR(40) DEFAULT 'MANUAL_VERIFICADO',
  confidence DECIMAL(5,2) DEFAULT 0.65,
  raw_payload_json JSON NULL,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tracking_eventos_tracking_fecha (tracking_id, fecha_evento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tracking_sync_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tracking_id BIGINT NULL,
  proveedor VARCHAR(40) NOT NULL,
  endpoint VARCHAR(500) NULL,
  status_http INT NULL,
  resultado ENUM('OK','ERROR','PENDIENTE_CREDENCIALES','CACHE','MANUAL_VERIFICADO','SIMULADO') NOT NULL,
  mensaje TEXT NULL,
  fecha_sync TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_tracking_sync_proveedor_fecha (proveedor, fecha_sync),
  INDEX idx_tracking_sync_resultado_fecha (resultado, fecha_sync)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tracking_fuentes (proveedor, nombre, api_url, developer_url, requiere_credenciales, estado) VALUES
('DHL', 'DHL Shipment Tracking API', 'https://api-eu.dhl.com/track/shipments', 'https://developer.dhl.com/api-reference/shipment-tracking', TRUE, 'PENDIENTE_CREDENCIALES'),
('FEDEX', 'FedEx Track API', 'https://apis.fedex.com/track/v1/trackingnumbers', 'https://developer.fedex.com/api/en-gl/catalog/track.html', TRUE, 'PENDIENTE_CREDENCIALES'),
('UPS', 'UPS Tracking API', 'https://onlinetools.ups.com/api/track/v1/details', 'https://developer.ups.com/', TRUE, 'PENDIENTE_CREDENCIALES'),
('MAERSK', 'Maersk Ocean Track and Trace API', 'https://api.maersk.com/track-and-trace', 'https://developer.maersk.com/api-catalogue', TRUE, 'PENDIENTE_CREDENCIALES'),
('OTRO', 'Registro manual verificable', NULL, NULL, FALSE, 'ACTIVA')
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  api_url = VALUES(api_url),
  developer_url = VALUES(developer_url),
  requiere_credenciales = VALUES(requiere_credenciales),
  estado = VALUES(estado);

INSERT INTO catalogo_fuentes (codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad) VALUES
('BCRP_API', 'Banco Central de Reserva del Peru - API de series estadisticas', 'OFICIAL', 'https://estadisticas.bcrp.gob.pe/estadisticas/series/api/', FALSE, 'ACTIVA', 1),
('SUNAT_ARANCEL', 'SUNAT/Aduanet - tratamiento arancelario referencial', 'OFICIAL', 'https://www.sunat.gob.pe/orientacionaduanera/nomenclaturaarancelaria/tratamiento.html', FALSE, 'PENDIENTE', 2),
('VUCE_WEB', 'Ventanilla Unica de Comercio Exterior', 'OFICIAL', 'https://www.vuce.gob.pe/', FALSE, 'PENDIENTE', 3),
('UN_COMTRADE_API', 'UN Comtrade API', 'OFICIAL', 'https://comtradeplus.un.org/', TRUE, 'PENDIENTE', 2),
('DHL_API', 'DHL Shipment Tracking API', 'TERCERO', 'https://developer.dhl.com/api-reference/shipment-tracking', TRUE, 'PENDIENTE', 3),
('FEDEX_API', 'FedEx Track API', 'TERCERO', 'https://developer.fedex.com/api/en-gl/catalog/track.html', TRUE, 'PENDIENTE', 3),
('UPS_API', 'UPS Developer Portal', 'TERCERO', 'https://developer.ups.com/', TRUE, 'PENDIENTE', 3),
('MAERSK_API', 'Maersk API Catalogue', 'TERCERO', 'https://developer.maersk.com/api-catalogue', TRUE, 'PENDIENTE', 3)
ON DUPLICATE KEY UPDATE
  nombre = VALUES(nombre),
  url_base = VALUES(url_base),
  requiere_credenciales = VALUES(requiere_credenciales),
  estado = VALUES(estado),
  prioridad = VALUES(prioridad);

INSERT INTO arancel_fuente_sunat
  (hs_code, descripcion, ad_valorem, restricciones_texto, regimen_asociado, fuente_url, metodo_obtencion, status_http, hash_contenido, confianza, source_type)
VALUES
  ('8517130000', 'Telefonos inteligentes y equipos de telecomunicacion. Registro referencial para demo.', 0.00, 'Puede requerir homologacion MTC segun caracteristicas tecnicas.', 'Importacion para consumo / mercancia restringida referencial', 'https://www.sunat.gob.pe/orientacionaduanera/nomenclaturaarancelaria/tratamiento.html', 'BD_LOCAL', NULL, SHA2('8517130000-MTC', 256), 0.85, 'BD_LOCAL'),
  ('2106909000', 'Preparaciones alimenticias diversas. Registro referencial para demo.', 6.00, 'Puede requerir evaluacion DIGESA o SENASA segun composicion.', 'Importacion para consumo / mercancia restringida referencial', 'https://www.sunat.gob.pe/orientacionaduanera/nomenclaturaarancelaria/tratamiento.html', 'BD_LOCAL', NULL, SHA2('2106909000-DIGESA', 256), 0.85, 'BD_LOCAL')
ON DUPLICATE KEY UPDATE
  descripcion = VALUES(descripcion),
  restricciones_texto = VALUES(restricciones_texto),
  regimen_asociado = VALUES(regimen_asociado),
  confianza = VALUES(confianza),
  source_type = VALUES(source_type);

INSERT INTO vuce_tramites_fuente
  (entidad, tramite, tipo_documento, hs_code, requisito, plazo_estimado, fuente_url, metodo_obtencion, estado_fuente, confianza, source_type)
VALUES
  ('MTC', 'Homologacion o validacion de equipos de telecomunicaciones', 'Autorizacion/validacion sectorial', '8517130000', 'Ficha tecnica, modelo, marca, fabricante y soporte de radiofrecuencia.', 'Referencial: validar en VUCE/MTC', 'https://www.vuce.gob.pe/', 'BD_LOCAL', 'REFERENCIAL', 0.65, 'BD_LOCAL'),
  ('DIGESA', 'Evaluacion sanitaria de alimentos o suplementos', 'Registro o autorizacion sanitaria', '2106909000', 'Composicion, rotulado, fabricante y certificado sanitario segun caso.', 'Referencial: validar en VUCE/DIGESA', 'https://www.vuce.gob.pe/', 'BD_LOCAL', 'REFERENCIAL', 0.65, 'BD_LOCAL')
ON DUPLICATE KEY UPDATE
  requisito = VALUES(requisito),
  plazo_estimado = VALUES(plazo_estimado),
  estado_fuente = VALUES(estado_fuente),
  confianza = VALUES(confianza);

UPDATE fuente_eventos
SET tipo_fuente = CASE
    WHEN fuente IN ('BCRP_API','UN_COMTRADE_API') THEN 'API_OFICIAL'
    WHEN fuente LIKE '%DHL%' OR fuente LIKE '%FEDEX%' OR fuente LIKE '%UPS%' OR fuente LIKE '%MAERSK%' THEN 'WEB_TRACKING'
    WHEN fuente LIKE '%SUNAT%' OR fuente LIKE '%VUCE%' THEN 'WEB_SCRAPING'
    WHEN fuente LIKE '%FALLBACK%' THEN 'BD_LOCAL'
    ELSE COALESCE(tipo_fuente, 'BD_LOCAL')
  END,
  source_type = CASE
    WHEN resultado = 'OK' AND fuente IN ('BCRP_API','UN_COMTRADE_API') THEN 'OFICIAL_API'
    WHEN resultado = 'OK' AND (fuente LIKE '%SUNAT%' OR fuente LIKE '%VUCE%') THEN 'OFICIAL_WEB'
    WHEN resultado = 'CACHE' THEN 'CACHE'
    WHEN resultado = 'FALLBACK' THEN 'FALLBACK'
    WHEN resultado = 'SIMULADO' THEN 'SIMULADO'
    WHEN resultado = 'ERROR' THEN 'PENDIENTE_VALIDACION'
    ELSE COALESCE(source_type, 'BD_LOCAL')
  END,
  confianza = CASE
    WHEN resultado = 'OK' AND fuente IN ('BCRP_API','UN_COMTRADE_API') THEN 0.98
    WHEN resultado = 'OK' AND (fuente LIKE '%SUNAT%' OR fuente LIKE '%VUCE%') THEN 0.90
    WHEN resultado = 'CACHE' THEN 0.80
    WHEN resultado = 'FALLBACK' THEN 0.35
    WHEN resultado = 'SIMULADO' THEN 0.20
    ELSE COALESCE(confianza, 0.50)
  END
WHERE id IS NOT NULL;


-- ALTER wrapped with IF NOT EXISTS column check via helper procedure
CALL importease_add_column_if_missing('operaciones', 'usado', 'BOOLEAN DEFAULT FALSE');
-- Hash Chain columns wrapped with IF NOT EXISTS checks
CALL importease_add_column_if_missing('expediente_eventos_auditoria', 'prev_hash', 'VARCHAR(64) NULL');
CALL importease_add_column_if_missing('expediente_eventos_auditoria', 'current_hash', 'VARCHAR(64) NULL');
CALL importease_add_column_if_missing('expediente_eventos_auditoria', 'firma_digital', 'VARCHAR(512) NULL');

-- ====================================================================
-- LIMPIEZA DE PROCEDIMIENTOS AUXILIARES
-- ====================================================================
DROP PROCEDURE IF EXISTS importease_add_column_if_missing;
DROP PROCEDURE IF EXISTS importease_add_column_if_missing_v35;
DROP PROCEDURE IF EXISTS importease_add_index_if_missing_v35;

-- ====================================================================
-- ROLLBACK v3.1 (solo como referencia - no ejecutar en produccion)
-- ====================================================================
/*
-- ImportEase v3.1 - Rollback de trazabilidad base
-- Usar solo si los datos generados por v3.1 NO son criticos.
-- Elimina tablas nuevas y columnas agregadas en upgrade_v3.1_trazabilidad_base.sql.

DROP TABLE IF EXISTS eventos_usuario;
DROP TABLE IF EXISTS fuente_eventos;
DROP TABLE IF EXISTS catalogo_fuentes;

DELIMITER $$

CREATE PROCEDURE importease_drop_column_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

CREATE PROCEDURE importease_drop_index_if_exists(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64)
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl = CONCAT('DROP INDEX ', p_index_name, ' ON ', p_table_name);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

CALL importease_drop_index_if_exists('log_busquedas', 'idx_log_busquedas_termino_fecha');
CALL importease_drop_index_if_exists('log_busquedas', 'idx_log_busquedas_hs_fecha');

CALL importease_drop_column_if_exists('tipo_cambio_diario', 'fuente_url');
CALL importease_drop_column_if_exists('tipo_cambio_diario', 'status_http');
CALL importease_drop_column_if_exists('tipo_cambio_diario', 'es_fallback');
CALL importease_drop_column_if_exists('tipo_cambio_diario', 'raw_response_hash');
CALL importease_drop_column_if_exists('tipo_cambio_diario', 'fecha_actualizacion');
CALL importease_drop_column_if_exists('tipo_cambio_diario', 'fuente_dato');
CALL importease_drop_column_if_exists('tipo_cambio_diario', 'confianza');

CALL importease_drop_column_if_exists('usuarios', 'ruc_validado');
CALL importease_drop_column_if_exists('usuarios', 'fuente_ruc');
CALL importease_drop_column_if_exists('usuarios', 'fecha_validacion_ruc');
CALL importease_drop_column_if_exists('usuarios', 'estado_ruc');
CALL importease_drop_column_if_exists('usuarios', 'condicion_ruc');
CALL importease_drop_column_if_exists('usuarios', 'ruc_confianza');

CALL importease_drop_column_if_exists('log_busquedas', 'session_id');
CALL importease_drop_column_if_exists('log_busquedas', 'termino_normalizado');
CALL importease_drop_column_if_exists('log_busquedas', 'hs_seleccionado');
CALL importease_drop_column_if_exists('log_busquedas', 'capitulo_hs');
CALL importease_drop_column_if_exists('log_busquedas', 'pais_origen');
CALL importease_drop_column_if_exists('log_busquedas', 'valor_fob_estimado');
CALL importease_drop_column_if_exists('log_busquedas', 'modulo_origen');
CALL importease_drop_column_if_exists('log_busquedas', 'resultado_count');
CALL importease_drop_column_if_exists('log_busquedas', 'hubo_click');
CALL importease_drop_column_if_exists('log_busquedas', 'ip_hash');
CALL importease_drop_column_if_exists('log_busquedas', 'user_agent_hash');

DROP PROCEDURE importease_drop_column_if_exists;
DROP PROCEDURE importease_drop_index_if_exists;

*/

SELECT 'Consolidacion importease_full_schema completada exitosamente.' AS resultado;


-- ====== SCHEMA UPDATES (schema_updates.sql) ======

CREATE TABLE IF NOT EXISTS expediente_plazos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  expediente_id BIGINT NOT NULL,
  codigo_plazo VARCHAR(80) NOT NULL,
  regimen VARCHAR(80),
  evento_base VARCHAR(80),
  fecha_base DATETIME,
  fecha_limite DATETIME,
  estado VARCHAR(40),
  riesgo VARCHAR(40),
  source VARCHAR(255),
  source_type VARCHAR(80),
  confidence DECIMAL(4,2),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expediente_eventos_auditoria (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  expediente_id BIGINT NOT NULL,
  usuario_id BIGINT,
  tipo_evento VARCHAR(80) NOT NULL,
  descripcion TEXT,
  before_json JSON,
  after_json JSON,
  source_type VARCHAR(80),
  ip_address VARCHAR(80),
  user_agent VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expediente_validaciones (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  expediente_id BIGINT NOT NULL,
  codigo_validacion VARCHAR(100),
  resultado VARCHAR(40),
  severidad VARCHAR(40),
  campos_faltantes TEXT,
  mensaje TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- v3.6: Agregar columna 'usado' a operaciones para percepción del 10%
ALTER TABLE operaciones ADD COLUMN usado BOOLEAN DEFAULT FALSE;



-- ====== SEED DATOS REALES (seed_datos_reales.sql) ======

-- ============================================================================================
-- IMPORTEASE ADUANERO - SCRIPT DE DATOS REALES MASIVO
-- ============================================================================================
-- Version: 2026-05-27
-- Fuentes: SUNAT (Arancel de Aduanas 2022 DS 404-2021-EF), MINCETUR (Acuerdos Comerciales),
--          BCRP (Tipo de Cambio), VUCE (Mercancias Restringidas)
-- Alcance: 55+ subpartidas HS reales, 25 TLC vigentes, 12+ depositos temporales,
--          11 intendencias de aduana, 3 casos de estudio con manifiestos.
-- IMPORTANTE: Todos los INSERT usan INSERT IGNORE para evitar duplicados.
-- ============================================================================================

-- (Railway: ya conectado a la BD)
SET NAMES utf8mb4;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================================
-- SECCION 1: CATALOGO MASIVO DE HS CODES (55 SUBPARTIDAS NACIONALES PERUANAS REALES)
-- ============================================================================================
-- Fuente oficial: Arancel de Aduanas 2022, aprobado por Decreto Supremo 404-2021-EF
-- Publicado en El Peruano el 31/12/2021, vigente desde 01/01/2022.
-- Los derechos Ad Valorem en Peru solo tienen 4 niveles: 0%, 4%, 6% y 11%.
-- El IGV es 16% y el IPM es 2% (totalizando 18%), ambos se aplican sobre CIF + Ad Valorem + ISC.
-- El ISC se aplica solo a productos especificos (licores, combustibles, vehiculos de lujo, etc.)
-- ============================================================================================

-- --------------------
-- 1.1 SECTOR TECNOLOGIA (10 subpartidas)
-- --------------------
-- Cap. 84-85: Maquinas, aparatos y material electrico
-- Todos con Ad Valorem 0% por ser bienes de capital/tecnologia

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('8471300000', 'Maquinas automaticas para tratamiento o procesamiento de datos, portatiles, de peso inferior o igual a 10 kg, que esten constituidas, al menos, por una unidad central de proceso, un teclado y un visualizador', 'Portable automatic data processing machines, weighing not more than 10 kg, consisting of at least a central processing unit, a keyboard and a display', 84, 8471, 847130, 8471300000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8517130000', 'Telefonos inteligentes (smartphones)', 'Smartphones', 85, 8517, 851713, 8517130000, 0.00, 0.00, 16.00, TRUE, 'MTC', TRUE),
('8528720000', 'Los demas aparatos receptores de television, en colores, con pantalla plana (por ejemplo, de cristal liquido o de plasma)', 'Other television receivers, colour, with flat panel screen (LCD, plasma)', 85, 8528, 852872, 8528720000, 6.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8471490000', 'Las demas maquinas automaticas para tratamiento o procesamiento de datos, presentadas en forma de sistemas', 'Other automatic data processing machines, presented in the form of systems', 84, 8471, 847149, 8471490000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8443321000', 'Impresoras, copiadoras y telecopiadoras (fax), incluso combinadas entre si, que operan conectadas a una maquina automatica para tratamiento de datos o a una red', 'Printers, copying machines and facsimile machines, whether or not combined, capable of connecting to an automatic data processing machine or to a network', 84, 8443, 844332, 8443321000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8471700000', 'Unidades de memoria', 'Storage units', 84, 8471, 847170, 8471700000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8517620000', 'Aparatos para la recepcion, conversion y transmision o regeneracion de voz, imagen u otros datos, incluidos los de conmutacion y encaminamiento (switching and routing apparatus)', 'Machines for the reception, conversion and transmission or regeneration of voice, images or other data, including switching and routing apparatus', 85, 8517, 851762, 8517620000, 0.00, 0.00, 16.00, TRUE, 'MTC', TRUE),
('8473300000', 'Partes y accesorios de maquinas de la partida 84.71', 'Parts and accessories of machines of heading 84.71', 84, 8473, 847330, 8473300000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8542310000', 'Procesadores y controladores, incluso combinados con memorias, convertidores, circuitos logicos, amplificadores, relojes y circuitos de sincronizacion', 'Processors and controllers, whether or not combined with memories, converters, logic circuits, amplifiers, clock and timing circuits', 85, 8542, 854231, 8542310000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8471410000', 'Las demas maquinas automaticas para tratamiento o procesamiento de datos, que comprendan en la misma envoltura, al menos, una unidad central de proceso y una unidad de entrada y de salida, incluso combinadas', 'Other automatic data processing machines comprising in the same housing at least a central processing unit and an input and output unit, whether or not combined', 84, 8471, 847141, 8471410000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE);

-- --------------------
-- 1.2 SECTOR BELLEZA Y SALUD (8 subpartidas)
-- --------------------
-- Cap. 33: Aceites esenciales, perfumeria, cosmeticos
-- Cap. 30: Productos farmaceuticos
-- Cosmeticos: Ad Valorem 6%, requieren DIGEMID (registro sanitario)
-- Medicamentos: Ad Valorem 0% (exonerados), requieren DIGEMID

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('3304990000', 'Las demas preparaciones de belleza, maquillaje y para el cuidado de la piel, excepto los medicamentos, incluidas las preparaciones antisolares y las bronceadoras; preparaciones para manicuras o pedicuros', 'Other beauty or make-up preparations and preparations for the care of the skin (other than medicaments), including sunscreen or suntan preparations; manicure or pedicure preparations', 33, 3304, 330499, 3304990000, 6.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3304100000', 'Preparaciones para el maquillaje de los labios', 'Lip make-up preparations', 33, 3304, 330410, 3304100000, 6.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3304200000', 'Preparaciones para el maquillaje de los ojos', 'Eye make-up preparations', 33, 3304, 330420, 3304200000, 6.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3304300000', 'Preparaciones para manicuras o pedicuros', 'Manicure or pedicure preparations', 33, 3304, 330430, 3304300000, 6.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3305100000', 'Champues (shampoos)', 'Shampoos', 33, 3305, 330510, 3305100000, 6.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3004902900', 'Los demas medicamentos (excepto los productos de las partidas 30.02, 30.05 o 30.06) constituidos por productos mezclados o sin mezclar, preparados para usos terapeuticos o profilacticos, dosificados', 'Other medicaments (excluding goods of heading 30.02, 30.05 or 30.06) consisting of mixed or unmixed products for therapeutic or prophylactic uses, put up in measured doses', 30, 3004, 300490, 3004902900, 0.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3003900000', 'Los demas medicamentos (excepto los productos de las partidas 30.02, 30.05 o 30.06) constituidos por productos mezclados entre si, sin dosificar ni acondicionar para la venta al por menor', 'Other medicaments (excluding goods of heading 30.02, 30.05 or 30.06) consisting of two or more constituents mixed together, not put up in measured doses or in forms or packings for retail sale', 30, 3003, 300390, 3003900000, 0.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE),
('3006100000', 'Catgut esteril y ligaduras esteriles similares, para suturas quirurgicas y adhesivos esteriles para tejidos organicos utilizados en cirugia para cerrar heridas', 'Sterile surgical catgut, similar sterile suture materials, sterile tissue adhesives for surgical wound closure', 30, 3006, 300610, 3006100000, 0.00, 0.00, 16.00, TRUE, 'DIGEMID', TRUE);

-- --------------------
-- 1.3 SECTOR ALIMENTOS Y BEBIDAS (10 subpartidas)
-- --------------------
-- Cap. 09: Cafe, te, yerba mate, especias
-- Cap. 22: Bebidas, liquidos alcoholicos y vinagre
-- Cap. 17: Azucares y articulos de confiteria
-- Cap. 08: Frutas y frutos comestibles
-- Cap. 20: Preparaciones de hortalizas, frutas u otros frutos
-- Alimentos basicos: Ad Valorem 0%
-- Vinos: Ad Valorem 6% + ISC 25%
-- Confiteria: Ad Valorem 6%

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('0901110000', 'Cafe sin tostar, sin descafeinar', 'Coffee, not roasted, not decaffeinated', 9, 901, 90111, 901110000, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('2204210000', 'Vino de uvas frescas, incluso encabezado; mosto de uva, en recipientes con capacidad inferior o igual a 2 litros', 'Wine of fresh grapes, including fortified wines; grape must, in containers holding 2 litres or less', 22, 2204, 220421, 2204210000, 6.00, 25.00, 16.00, TRUE, 'DIGESA', TRUE),
('1704900000', 'Los demas articulos de confiteria sin cacao (incluido el chocolate blanco)', 'Other sugar confectionery (including white chocolate), not containing cocoa', 17, 1704, 170490, 1704900000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE),
('0803901100', 'Bananas o platanos tipo Cavendish Valery, frescos', 'Bananas, Cavendish Valery type, fresh', 8, 803, 80390, 803901100, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('2009110000', 'Jugo de naranja, congelado', 'Orange juice, frozen', 20, 2009, 200911, 2009110000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE),
('1806320000', 'Chocolate y demas preparaciones alimenticias que contengan cacao, en bloques, tabletas o barras, sin rellenar', 'Chocolate and other food preparations containing cocoa, in blocks, slabs or bars, not filled', 18, 1806, 180632, 1806320000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE),
('2106909000', 'Las demas preparaciones alimenticias no expresadas ni comprendidas en otra parte', 'Other food preparations not elsewhere specified or included', 21, 2106, 210690, 2106909000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE),
('2202100000', 'Agua, incluidas el agua mineral y la gasificada, con adicion de azucar u otro edulcorante o aromatizada', 'Waters, including mineral waters and aerated waters, containing added sugar or other sweetening matter or flavoured', 22, 2202, 220210, 2202100000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE),
('2208300000', 'Whisky', 'Whiskies', 22, 2208, 220830, 2208300000, 6.00, 20.00, 16.00, TRUE, 'DIGESA', TRUE),
('0402211900', 'Las demas leches en polvo, granulos o demas formas solidas, con un contenido de materias grasas superior al 1.5% en peso, sin adicion de azucar ni otro edulcorante', 'Other milk in powder, granules or other solid forms, of a fat content exceeding 1.5% by weight, not containing added sugar or other sweetening matter', 4, 402, 40221, 402211900, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE);

-- --------------------
-- 1.4 SECTOR TEXTILES Y CONFECCIONES (8 subpartidas)
-- --------------------
-- Cap. 61: Prendas y complementos de vestir, de punto
-- Cap. 62: Prendas y complementos de vestir, excepto los de punto
-- Proteccion a la industria nacional: Ad Valorem 11% para prendas de vestir

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('6109100000', 'T-shirts y camisetas interiores, de punto, de algodon', 'T-shirts, singlets and other vests, knitted or crocheted, of cotton', 61, 6109, 610910, 6109100000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6110200000', 'Sueteres (jerseis), pullovers, cardiganes, chalecos y articulos similares, de punto, de algodon', 'Jerseys, pullovers, cardigans, waistcoats and similar articles, knitted or crocheted, of cotton', 61, 6110, 611020, 6110200000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6204620000', 'Pantalones, pantalones con peto o tirantes, pantalones cortos (calzones) y shorts, de algodon, para mujeres o ninas', 'Womens or girls trousers, bib and brace overalls, breeches and shorts, of cotton', 62, 6204, 620462, 6204620000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6116100000', 'Guantes, mitones y manoplas, de punto, impregnados, recubiertos o revestidos con plastico o caucho', 'Gloves, mittens and mitts, knitted or crocheted, impregnated, coated or covered with plastics or rubber', 61, 6116, 611610, 6116100000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6203420000', 'Pantalones, pantalones con peto o tirantes, pantalones cortos (calzones) y shorts, de algodon, para hombres o ninos', 'Mens or boys trousers, bib and brace overalls, breeches and shorts, of cotton', 62, 6203, 620342, 6203420000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6205200000', 'Camisas para hombres o ninos, de algodon', 'Mens or boys shirts, of cotton', 62, 6205, 620520, 6205200000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6302600000', 'Ropa de tocador o cocina, de tejido con bucles del tipo toalla, de algodon', 'Toilet linen and kitchen linen, of terry towelling or similar terry fabrics, of cotton', 63, 6302, 630260, 6302600000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE),
('6402190000', 'Los demas calzados de deporte, con suela y parte superior de caucho o plastico', 'Other sports footwear, with outer soles and uppers of rubber or plastics', 64, 6402, 640219, 6402190000, 11.00, 0.00, 16.00, FALSE, NULL, TRUE);

-- --------------------
-- 1.5 SECTOR AUTOMOTRIZ (5 subpartidas)
-- --------------------
-- Cap. 87: Vehiculos automoviles, tractores, velocipedos
-- Cap. 40: Caucho y sus manufacturas
-- Vehiculos: Ad Valorem 6%, ISC variable segun cilindrada

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('8708999090', 'Las demas partes y accesorios de vehiculos automoviles de las partidas 87.01 a 87.05', 'Other parts and accessories of motor vehicles of headings 87.01 to 87.05', 87, 8708, 870899, 8708999090, 6.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8703230000', 'Los demas vehiculos con motor de embolo (piston) alternativo, de encendido por chispa, de cilindrada superior a 1500 cm3 pero inferior o igual a 3000 cm3', 'Other vehicles with spark-ignition internal combustion reciprocating piston engine, of a cylinder capacity exceeding 1,500 cc but not exceeding 3,000 cc', 87, 8703, 870323, 8703230000, 6.00, 10.00, 16.00, FALSE, NULL, TRUE),
('4011100000', 'Neumaticos (llantas neumaticas) nuevos de caucho, de los tipos utilizados en automoviles de turismo (incluidos los vehiculos de tipo familiar "break" o "station wagon" y los de carreras)', 'New pneumatic tyres, of rubber, of a kind used on motor cars (including station wagons and racing cars)', 40, 4011, 401110, 4011100000, 6.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8703240000', 'Los demas vehiculos con motor de embolo (piston) alternativo, de encendido por chispa, de cilindrada superior a 3000 cm3', 'Other vehicles with spark-ignition internal combustion reciprocating piston engine, of a cylinder capacity exceeding 3,000 cc', 87, 8703, 870324, 8703240000, 6.00, 30.00, 16.00, FALSE, NULL, TRUE),
('8711200000', 'Motocicletas (incluidos los ciclomotores) y velocipedos con motor auxiliar, de cilindrada superior a 50 cm3 pero inferior o igual a 250 cm3', 'Motorcycles (including mopeds) and cycles fitted with an auxiliary motor, with reciprocating internal combustion piston engine of a cylinder capacity exceeding 50 cc but not exceeding 250 cc', 87, 8711, 871120, 8711200000, 6.00, 0.00, 16.00, FALSE, NULL, TRUE);

-- --------------------
-- 1.6 SECTOR MAQUINARIA E INDUSTRIA (4 subpartidas)
-- --------------------
-- Cap. 84: Maquinas y aparatos mecanicos
-- Bienes de capital: Ad Valorem 0% para fomentar la industrializacion

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('8429110000', 'Topadoras frontales (bulldozers) y topadoras angulares (angledozers), de orugas', 'Track laying bulldozers and angledozers', 84, 8429, 842911, 8429110000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8430200000', 'Quitanieves', 'Snowploughs and snowblowers', 84, 8430, 843020, 8430200000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8474100000', 'Maquinas y aparatos de clasificar, cribar, separar o lavar tierras, piedras, menas u otras materias minerales solidas', 'Sorting, screening, separating or washing machines for earth, stones, ores or other mineral substances, in solid form', 84, 8474, 847410, 8474100000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('8428330000', 'Los demas aparatos elevadores o transportadores, de accion continua, para mercancias, de cinta o correa', 'Other continuous-action elevators and conveyors for goods or materials, belt type', 84, 8428, 842833, 8428330000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE);

-- --------------------
-- 1.7 SECTOR QUIMICOS Y COMBUSTIBLES (4 subpartidas)
-- --------------------
-- Cap. 27: Combustibles minerales, aceites minerales
-- Cap. 38: Productos diversos de las industrias quimicas
-- Gasolina: Ad Valorem 0% + ISC especifico por galon
-- Desinfectantes: Ad Valorem 6%

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('2710192100', 'Gasolinas sin tetraetilo de plomo, para motores, con un numero de octano Research (RON) superior o igual a 97', 'Motor spirit (gasoline), unleaded, with a Research Octane Number (RON) of 97 or more', 27, 2710, 271019, 2710192100, 0.00, 1.27, 16.00, TRUE, 'OSINERGMIN', FALSE),
('3808940000', 'Desinfectantes', 'Disinfectants', 38, 3808, 380894, 3808940000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE),
('3402209000', 'Los demas preparaciones tensoactivas, preparaciones para lavar y preparaciones de limpieza, acondicionadas para la venta al por menor', 'Other surface-active preparations, washing preparations and cleaning preparations, put up for retail sale', 34, 3402, 340220, 3402209000, 6.00, 0.00, 16.00, FALSE, NULL, TRUE),
('2711110000', 'Gas natural licuado', 'Natural gas, liquefied', 27, 2711, 271111, 2711110000, 0.00, 0.00, 16.00, TRUE, 'OSINERGMIN', FALSE);

-- --------------------
-- 1.8 SECTOR JUGUETES Y ENTRETENIMIENTO (4 subpartidas)
-- --------------------
-- Cap. 95: Juguetes, juegos y articulos para recreo o deporte
-- Juguetes: Ad Valorem 6%
-- Consolas de videojuegos: Ad Valorem 0%

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('9503000000', 'Triciclos, patinetes, coches de pedal y juguetes similares con ruedas; coches y sillas de ruedas para munecas o munecos; munecas o munecos; los demas juguetes; modelos reducidos', 'Tricycles, scooters, pedal cars and similar wheeled toys; dolls carriages; dolls; other toys; reduced-size models', 95, 9503, 950300, 9503000000, 6.00, 0.00, 16.00, FALSE, NULL, TRUE),
('9504500000', 'Consolas y maquinas de videojuego, excepto las de la subpartida 9504.30', 'Video game consoles and machines, other than those of subheading 9504.30', 95, 9504, 950450, 9504500000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE),
('9506620000', 'Pelotas inflables', 'Inflatable balls', 95, 9506, 950662, 9506620000, 6.00, 0.00, 16.00, FALSE, NULL, TRUE),
('9504300000', 'Los demas juegos activados con monedas, billetes de banco, tarjetas bancarias, fichas o cualquier otro medio de pago, excepto los juegos de bolos automaticos (bowlings)', 'Other games, operated by coins, banknotes, bank cards, tokens or by any other means of payment, other than automatic bowling alley equipment', 95, 9504, 950430, 9504300000, 0.00, 0.00, 16.00, FALSE, NULL, TRUE);

-- --------------------
-- 1.9 SECTOR AGROPECUARIO (6 subpartidas)
-- --------------------
-- Cap. 10: Cereales
-- Cap. 02: Carne y despojos comestibles
-- Insumos agricolas basicos: Ad Valorem 0%
-- Pollo congelado: Ad Valorem 0% (con derechos antidumping historicos contra EEUU)

INSERT IGNORE INTO hs_codes (codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, requiere_vuce, entidad_vuce, tlc_china) VALUES
('1005900000', 'Maiz (excepto para siembra)', 'Maize (corn), other than seed', 10, 1005, 100590, 1005900000, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('1001190000', 'Los demas trigos (excepto para siembra)', 'Other wheat (other than seed)', 10, 1001, 100119, 1001190000, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('0207140090', 'Trozos y despojos de gallo o gallina, congelados: Los demas', 'Cuts and offal of fowls of the species Gallus domesticus, frozen: Other', 2, 207, 20714, 207140090, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('1006300000', 'Arroz semiblanqueado o blanqueado, incluso pulido o glaseado', 'Semi-milled or wholly milled rice, whether or not polished or glazed', 10, 1006, 100630, 1006300000, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('1201900000', 'Las demas habas (porotos, frijoles, frejoles) de soja (soya), incluso quebrantadas', 'Other soya beans, whether or not broken', 12, 1201, 120190, 1201900000, 0.00, 0.00, 16.00, TRUE, 'SENASA', TRUE),
('0402101000', 'Leche en polvo, granulos o demas formas solidas, con un contenido de materias grasas inferior o igual al 1.5% en peso, en envases inmediatos de contenido neto inferior o igual a 2.5 kg', 'Milk in powder, granules or other solid forms, of a fat content not exceeding 1.5% by weight, in immediate packings of a net content not exceeding 2.5 kg', 4, 402, 40210, 402101000, 6.00, 0.00, 16.00, TRUE, 'DIGESA', TRUE);

-- ============================================================================================
-- SECCION 1B: SINCRONIZACION CON TABLA partidas_arancelarias
-- ============================================================================================
-- La tabla partidas_arancelarias es usada por el calculador de tributos.
-- Incluye el detalle de IGV (16%), IPM (2%) y seguro referencial (1.5%).

INSERT IGNORE INTO partidas_arancelarias (hs_code, descripcion, ad_valorem_pct, isc_pct, igb_pct, ipm_pct, seguro_pct, tendencia_actual) VALUES
('8471300000', 'Laptops y computadoras portatiles', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('8517130000', 'Telefonos inteligentes (smartphones)', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('8528720000', 'Televisores de pantalla plana (LCD, plasma)', 6.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('8471490000', 'Computadoras de escritorio en forma de sistemas', 0.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('8443321000', 'Impresoras y multifuncionales', 0.00, 0.00, 16.00, 2.00, 1.50, 'DECADENCIA'),
('3304990000', 'Cosmeticos y preparaciones de belleza', 6.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('3304100000', 'Preparaciones para maquillaje de labios', 6.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('3304200000', 'Preparaciones para maquillaje de ojos', 6.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('3004902900', 'Medicamentos preparados dosificados', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('3003900000', 'Medicamentos sin dosificar ni acondicionar', 0.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('0901110000', 'Cafe sin tostar, sin descafeinar', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('2204210000', 'Vino de uvas frescas en recipientes <= 2L', 6.00, 25.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('1704900000', 'Articulos de confiteria sin cacao', 6.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('0803901100', 'Bananas o platanos Cavendish Valery', 0.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('2009110000', 'Jugo de naranja congelado', 6.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('6109100000', 'T-shirts y camisetas de algodon de punto', 11.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('6110200000', 'Sueteres y pullovers de algodon de punto', 11.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('6204620000', 'Pantalones de algodon para mujeres o ninas', 11.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('6116100000', 'Guantes de punto impregnados con plastico', 11.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('8708999090', 'Partes y accesorios de vehiculos automoviles', 6.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('8703230000', 'Vehiculos motor gasolina 1500-3000 cm3', 6.00, 10.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('4011100000', 'Neumaticos nuevos para automoviles', 6.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('8429110000', 'Bulldozers de orugas', 0.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('8430200000', 'Quitanieves', 0.00, 0.00, 16.00, 2.00, 1.50, 'ESTANCADO'),
('2710192100', 'Gasolina sin plomo RON >= 97', 0.00, 1.27, 16.00, 2.00, 1.50, 'CRECIENTE'),
('3808940000', 'Desinfectantes', 6.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('9503000000', 'Juguetes diversos', 6.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('9504500000', 'Consolas y maquinas de videojuego', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('1005900000', 'Maiz excepto para siembra', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('1001190000', 'Trigo excepto para siembra', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE'),
('0207140090', 'Pollo congelado trozos y despojos', 0.00, 0.00, 16.00, 2.00, 1.50, 'CRECIENTE');


-- ============================================================================================
-- SECCION 2: TABLA TLC_ACUERDOS - 25 ACUERDOS COMERCIALES VIGENTES DE PERU
-- ============================================================================================
-- Fuente: MINCETUR - Acuerdos Comerciales del Peru
-- URL: https://www.acuerdoscomerciales.gob.pe/
-- Peru tiene 22 acuerdos comerciales vigentes + CAN + alianzas regionales.
-- La reduccion_advalorem es NULL porque depende de la subpartida, cronograma y certificado de origen.
-- ============================================================================================

-- Primero vaciamos la tabla existente y reinseramos con datos completos
-- (usamos INSERT IGNORE + ON DUPLICATE KEY para no perder datos si ya existen)

INSERT INTO tlc_acuerdos (pais_codigo, pais_nombre, acuerdo_nombre, reduccion_advalorem, requiere_certificado_origen, fuente_codigo) VALUES
('CN', 'China', 'Tratado de Libre Comercio Peru - China', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('US', 'Estados Unidos', 'Acuerdo de Promocion Comercial Peru - Estados Unidos', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('EU', 'Union Europea', 'Acuerdo Comercial Peru - Union Europea', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('CAN', 'Comunidad Andina (Bolivia, Colombia, Ecuador)', 'Comunidad Andina de Naciones - CAN', 0.00, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('MERCOSUR', 'MERCOSUR (Argentina, Brasil, Paraguay, Uruguay)', 'Acuerdo de Complementacion Economica No. 58 Peru - MERCOSUR', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('CPTPP', 'CPTPP (11 paises miembros)', 'Tratado Integral y Progresista de Asociacion Transpacifico - CPTPP', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('CL', 'Chile', 'Acuerdo de Libre Comercio Peru - Chile (ACE No. 38)', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('MX', 'Mexico', 'Acuerdo de Integracion Comercial Peru - Mexico', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('CA', 'Canada', 'Tratado de Libre Comercio Peru - Canada', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('SG', 'Singapur', 'Tratado de Libre Comercio Peru - Singapur', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('KR', 'Corea del Sur', 'Tratado de Libre Comercio Peru - Corea del Sur', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('JP', 'Japon', 'Acuerdo de Asociacion Economica Peru - Japon', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('TH', 'Tailandia', 'Protocolo entre Peru y Tailandia para acelerar la liberalizacion del comercio de mercancias', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('PA', 'Panama', 'Tratado de Libre Comercio Peru - Panama', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('CR', 'Costa Rica', 'Tratado de Libre Comercio Peru - Costa Rica', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('GT', 'Guatemala', 'Tratado de Libre Comercio Peru - Guatemala', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('HN', 'Honduras', 'Tratado de Libre Comercio Peru - Honduras', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('CU', 'Cuba', 'Acuerdo de Complementacion Economica No. 50 Peru - Cuba', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('EFTA', 'EFTA (Suiza, Noruega, Islandia, Liechtenstein)', 'Tratado de Libre Comercio Peru - EFTA (Asociacion Europea de Libre Comercio)', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('GB', 'Reino Unido', 'Acuerdo Comercial Peru - Reino Unido', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('AU', 'Australia', 'Tratado de Libre Comercio Peru - Australia (via CPTPP)', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('HK', 'Hong Kong', 'Tratado de Libre Comercio Peru - Hong Kong SAR', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('AP', 'Alianza del Pacifico (Chile, Colombia, Mexico, Peru)', 'Protocolo Adicional al Acuerdo Marco de la Alianza del Pacifico', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('NZ', 'Nueva Zelanda', 'Tratado de Libre Comercio Peru - Nueva Zelanda (via CPTPP)', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU'),
('VN', 'Vietnam', 'Tratado de Libre Comercio Peru - Vietnam (via CPTPP)', NULL, TRUE, 'ACUERDOS_COMERCIALES_PERU')
ON DUPLICATE KEY UPDATE
  pais_nombre = VALUES(pais_nombre),
  acuerdo_nombre = VALUES(acuerdo_nombre),
  requiere_certificado_origen = VALUES(requiere_certificado_origen);


-- ============================================================================================
-- SECCION 3: TABLA DEPOSITOS_TEMPORALES (Terminales Portuarios, Depositos y Almacenes)
-- ============================================================================================
-- Fuente: SUNAT - Relacion de Depositos Temporales autorizados
-- Todos ubicados en la jurisdiccion de la Intendencia Maritima del Callao (118)
-- y la Intendencia de Aduana Aerea y Postal (235)
-- ============================================================================================

CREATE TABLE IF NOT EXISTS depositos_temporales (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo_sigad VARCHAR(10),
  nombre VARCHAR(150) NOT NULL,
  tipo ENUM('TERMINAL_PORTUARIO','DEPOSITO_TEMPORAL','ALMACEN_ADUANERO','AEREO') DEFAULT 'DEPOSITO_TEMPORAL',
  direccion VARCHAR(255),
  distrito VARCHAR(80),
  intendencia_codigo VARCHAR(5) DEFAULT '118',
  telefono VARCHAR(30),
  activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO depositos_temporales (codigo_sigad, nombre, tipo, direccion, distrito, intendencia_codigo, telefono, activo) VALUES
('3012', 'APM Terminals Callao S.A.', 'TERMINAL_PORTUARIO',
 'Terminal Norte Multiproposito del Puerto del Callao, Av. Contralmirante Mora s/n',
 'Callao', '118', '(01) 625-9300', TRUE),

('3014', 'DP World Callao S.R.L.', 'TERMINAL_PORTUARIO',
 'Terminal Sur del Puerto del Callao, Muelle Sur s/n',
 'Callao', '118', '(01) 411-6800', TRUE),

('3036', 'Fargoline S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Argentina 3696, Callao',
 'Callao', '118', '(01) 614-3100', TRUE),

('3037', 'Ransa Comercial S.A.C.', 'DEPOSITO_TEMPORAL',
 'Calle Los Alcanfores 425, Miraflores (sede administrativa) / Av. Argentina 5800, Carmen de la Legua (almacen)',
 'Carmen de la Legua Reynoso', '118', '(01) 517-1800', TRUE),

('3046', 'Neptunia S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta Km 14.5, Ventanilla',
 'Ventanilla', '118', '(01) 614-0500', TRUE),

('3048', 'LICSA - Lima Container Service S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta 4631, Callao',
 'Callao', '118', '(01) 453-5555', TRUE),

('3024', 'Depositos Quimicos Mineros S.A. (DQM)', 'ALMACEN_ADUANERO',
 'Jr. Julio Corrales Melgarejo 198, Callao',
 'Callao', '118', '(01) 429-3720', TRUE),

('3059', 'Almacenes Mundo S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta 5875, Carmen de la Legua Reynoso',
 'Carmen de la Legua Reynoso', '118', '(01) 574-2700', TRUE),

('3041', 'Savar Agentes de Aduana S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Elmer Faucett 3348, Callao',
 'Callao', '118', '(01) 630-4000', TRUE),

('3501', 'Talma Servicios Aeroportuarios S.A.', 'AEREO',
 'Aeropuerto Internacional Jorge Chavez, Av. Elmer Faucett s/n, Callao',
 'Callao', '235', '(01) 517-0820', TRUE),

('3042', 'Imupesa - Inversiones Maritimas Universales Peru S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta 7089, Callao',
 'Callao', '118', '(01) 577-1040', TRUE),

('3054', 'Unimar S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta Km 11.5, Callao',
 'Callao', '118', '(01) 614-2700', TRUE),

('3060', 'Tramarsa - Trabajos Maritimos S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta 5198, Callao',
 'Callao', '118', '(01) 215-3100', TRUE),

('3502', 'Shohin S.A.C.', 'AEREO',
 'Aeropuerto Internacional Jorge Chavez, Av. Elmer Faucett s/n, Terminal de Carga',
 'Callao', '235', '(01) 517-3500', TRUE),

('3039', 'Alconsa - Almacenera y Agenciamiento del Pacifico S.A.', 'DEPOSITO_TEMPORAL',
 'Av. Nestor Gambetta 4825, Callao',
 'Callao', '118', '(01) 577-0808', TRUE);


-- ============================================================================================
-- SECCION 4: TABLA INTENDENCIAS_ADUANA (11 intendencias reales de SUNAT)
-- ============================================================================================
-- Fuente: SUNAT - Estructura organizacional aduanera
-- Cada intendencia tiene jurisdiccion sobre un area geografica del Peru.
-- El codigo es asignado por SUNAT para el SIGAD (Sistema Integrado de Gestion Aduanera).
-- ============================================================================================

CREATE TABLE IF NOT EXISTS intendencias_aduana (
  id INT AUTO_INCREMENT PRIMARY KEY,
  codigo VARCHAR(5) NOT NULL UNIQUE,
  nombre VARCHAR(150) NOT NULL,
  tipo ENUM('MARITIMA','AEREA','TERRESTRE','POSTAL','FLUVIAL') DEFAULT 'MARITIMA',
  departamento VARCHAR(50),
  activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO intendencias_aduana (codigo, nombre, tipo, departamento, activo) VALUES
('118', 'Intendencia de Aduana Maritima del Callao', 'MARITIMA', 'Callao', TRUE),
('235', 'Intendencia de Aduana Aerea y Postal', 'AEREA', 'Callao', TRUE),
('046', 'Intendencia de Aduana de Paita', 'MARITIMA', 'Piura', TRUE),
('154', 'Intendencia de Aduana de Tacna', 'TERRESTRE', 'Tacna', TRUE),
('262', 'Intendencia de Aduana de Ilo', 'MARITIMA', 'Moquegua', TRUE),
('019', 'Intendencia de Aduana de Tumbes', 'TERRESTRE', 'Tumbes', TRUE),
('082', 'Intendencia de Aduana de Puno', 'TERRESTRE', 'Puno', TRUE),
('172', 'Intendencia de Aduana de Arequipa', 'TERRESTRE', 'Arequipa', TRUE),
('245', 'Intendencia de Aduana de Cusco', 'TERRESTRE', 'Cusco', TRUE),
('055', 'Intendencia de Aduana de Chimbote', 'MARITIMA', 'Ancash', TRUE),
('028', 'Intendencia de Aduana de Talara', 'MARITIMA', 'Piura', TRUE);


-- ============================================================================================
-- SECCION 5: TABLAS DE NORMATIVA (reglas_normativas, restricciones_hs, reglas_plazo)
-- ============================================================================================

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


-- ============================================================================================
-- SECCION 6: USUARIO DE PRUEBA PARA CASOS DE ESTUDIO
-- ============================================================================================
-- Se requiere un usuario base para asociar las operaciones (importaciones).
-- Si ya existe el usuario con id=1, se ignora.

INSERT IGNORE INTO usuarios (id, ruc, razon_social, email, password_hash, buen_contribuyente, perfil)
VALUES (1, '20100130204', 'IMPORTACIONES DEMO S.A.C.', 'demo@importease.pe',
        '$2a$12$5AmvQe6p7JNoGSlemaqne.G7BhYehr85Y8/nKrO6zqOPs3MEpinOG', TRUE, 'IMPORTADOR');


-- ============================================================================================
-- SECCION 6: CASOS DE ESTUDIO - 3 EXPEDIENTES DE IMPORTACION
-- ============================================================================================
-- Insertamos en la tabla `operaciones` (nombre real en el schema).
-- Tipo de cambio referencial SUNAT: 3.75 PEN/USD (mayo 2026)
-- Formula tributaria peruana:
--   Base imponible = CIF
--   Ad Valorem = CIF * %AV
--   ISC = (CIF + AV) * %ISC
--   IGV = (CIF + AV + ISC) * 16%
--   IPM = (CIF + AV + ISC) * 2%
--   Percepcion (si primer importador) = (CIF + AV + ISC + IGV + IPM) * 3.5%
-- ============================================================================================

-- ----------------------------
-- CASO 1: 100 Laptops Lenovo desde China via CIF
-- Canal Verde, Nacionalizada
-- HS: 8471300000 (Ad Valorem 0%, ISC 0%)
-- FOB = USD 45,000.00
-- Flete = USD 1,200.00
-- Seguro = USD 300.00
-- CIF = USD 46,500.00
-- Ad Valorem = 46500 * 0% = 0.00
-- ISC = 0.00
-- Base IGV = 46500 + 0 + 0 = 46500
-- IGV = 46500 * 16% = 7,440.00
-- IPM = 46500 * 2% = 930.00
-- Percepcion = (46500+0+0+7440+930) * 3.5% = 1,920.45
-- Total tributos = 0 + 0 + 7440 + 930 + 1920.45 = 10,290.45
-- ----------------------------

INSERT IGNORE INTO operaciones
  (id, usuario_id, producto_desc, hs_code, pais_origen, incoterm,
   fob, flete, seguro, cif, tipo_cambio,
   ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos,
   canal_asignado, estado, numero_dam, fecha_numeracion,
   documento_factura, documento_bl, documento_certificado_origen, permiso_vuce_obtenido, entidad_vuce)
VALUES
  (1, 1, 'Importacion de 100 Laptops Lenovo ThinkPad T14 Gen 5 - Lote Comercial',
   '8471300000', 'CN', 'CIF',
   45000.00, 1200.00, 300.00, 46500.00, 3.750,
   0.00, 0.00, 7440.00, 930.00, 1920.45, 10290.45,
   'VERDE', 'NACIONALIZADA', '118-2026-10-000456', '2026-05-15 09:30:00',
   TRUE, TRUE, TRUE, FALSE, NULL);

-- ----------------------------
-- CASO 2: Cosmeticos desde EEUU - Cotizacion
-- Canal Rojo potencial (restringido por DIGEMID)
-- HS: 3304990000 (Ad Valorem 6%, ISC 0%)
-- FOB = USD 8,500.00
-- Flete = USD 800.00
-- Seguro = USD 100.00
-- CIF = USD 9,400.00
-- Ad Valorem = 9400 * 6% = 564.00
-- ISC = 0.00
-- Base IGV = 9400 + 564 + 0 = 9964
-- IGV = 9964 * 16% = 1,594.24
-- IPM = 9964 * 2% = 199.28
-- Percepcion = (9400+564+0+1594.24+199.28) * 3.5% = 411.51
-- Total tributos = 564 + 0 + 1594.24 + 199.28 + 411.51 = 2,769.03
-- NOTA: Requiere Registro Sanitario de DIGEMID. Canal Rojo probable por ser mercancia restringida.
-- ----------------------------

INSERT IGNORE INTO operaciones
  (id, usuario_id, producto_desc, hs_code, pais_origen, incoterm,
   fob, flete, seguro, cif, tipo_cambio,
   ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos,
   canal_asignado, estado, numero_dam,
   documento_factura, documento_bl, documento_certificado_origen, permiso_vuce_obtenido, entidad_vuce)
VALUES
  (2, 1, 'Paletas de Maquillaje, Cremas Faciales y Productos de Cuidado de Piel (Sujeto a Registro Sanitario DIGEMID)',
   '3304990000', 'US', 'FOB',
   8500.00, 800.00, 100.00, 9400.00, 3.750,
   564.00, 0.00, 1594.24, 199.28, 411.51, 2769.03,
   NULL, 'COTIZACION', NULL,
   FALSE, FALSE, FALSE, FALSE, 'DIGEMID');

-- ----------------------------
-- CASO 3: Repuestos automotrices en transito hacia Bolivia
-- Regimen: Transito Aduanero (Codigo 80)
-- HS: 8708999090 (Ad Valorem 6%, ISC 0%)
-- FOB = USD 22,000.00
-- Flete = USD 2,500.00
-- Seguro = USD 450.00
-- CIF = USD 24,950.00
-- En transito aduanero los tributos quedan suspendidos (no se pagan en Peru).
-- Se requiere garantia aduanera por el monto equivalente a los tributos suspendidos.
-- Ad Valorem referencial = 24950 * 6% = 1,497.00
-- Total tributos suspendidos = referencial, no pagados
-- ----------------------------

INSERT IGNORE INTO operaciones
  (id, usuario_id, producto_desc, hs_code, pais_origen, incoterm,
   fob, flete, seguro, cif, tipo_cambio,
   ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos,
   canal_asignado, estado, numero_dam,
   documento_factura, documento_bl, documento_certificado_origen, permiso_vuce_obtenido, entidad_vuce)
VALUES
  (3, 1, 'Transito Aduanero de Repuestos Automotrices (discos de freno, filtros, pastillas) con destino final Bolivia',
   '8708999090', 'JP', 'FOB',
   22000.00, 2500.00, 450.00, 24950.00, 3.750,
   0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
   NULL, 'TRAMITE', '118-2026-80-000789',
   TRUE, TRUE, FALSE, FALSE, NULL);


-- ============================================================================================
-- SECCION 7: MANIFIESTOS DE CARGA ASOCIADOS A LOS CASOS DE ESTUDIO
-- ============================================================================================
-- Tabla: manifiestos_carga (creada en upgrade_v3.4_expediente_aduanero.sql)
-- Columnas: operacion_id, numero_manifiesto, tipo, via_transporte, fecha_llegada,
--           puerto_arribo, aduana_codigo, deposito_temporal, estado, fuente
-- ============================================================================================

-- Manifiesto del Caso 1: Laptops desde Shanghai
INSERT IGNORE INTO manifiestos_carga
  (operacion_id, numero_manifiesto, tipo, via_transporte,
   fecha_llegada, fecha_termino_descarga,
   puerto_arribo, aduana_codigo, deposito_temporal,
   estado, fuente, source_type, confidence)
VALUES
  (1, '2026-118-MAN-000567', 'MARITIMO', 'MARITIMA',
   '2026-05-12 06:00:00', '2026-05-12 18:00:00',
   'CALLAO (PECEL)', '118', 'DP World Callao S.R.L.',
   'DESCARGADO', 'SEED_DATOS_REALES', 'SIMULADO', 0.90);

-- Manifiesto del Caso 3: Repuestos desde Yokohama (en transito hacia Bolivia)
INSERT IGNORE INTO manifiestos_carga
  (operacion_id, numero_manifiesto, tipo, via_transporte,
   fecha_llegada, fecha_termino_descarga,
   puerto_arribo, aduana_codigo, deposito_temporal,
   estado, fuente, source_type, confidence)
VALUES
  (3, '2026-118-MAN-000999', 'MARITIMO', 'MARITIMA',
   '2026-05-20 08:30:00', '2026-05-20 20:00:00',
   'CALLAO (PECEL)', '118', 'APM Terminals Callao S.A.',
   'EN_TRANSITO', 'SEED_DATOS_REALES', 'SIMULADO', 0.90);


-- ============================================================================================
-- SECCION 8: DOCUMENTOS DE TRANSPORTE (BL) PARA LOS MANIFIESTOS
-- ============================================================================================

-- BL del Caso 1: Laptops
INSERT IGNORE INTO documentos_transporte
  (manifiesto_id, tipo_documento, numero_documento, master_house,
   fecha_embarque, puerto_origen, puerto_destino,
   peso_bruto, bultos)
VALUES
  ((SELECT id FROM manifiestos_carga WHERE numero_manifiesto = '2026-118-MAN-000567' LIMIT 1),
   'BL', 'HLCU2026SH00123456', 'MASTER',
   '2026-04-28 00:00:00', 'SHANGHAI (CNSHA)', 'CALLAO (PECEL)',
   350.500, 100);

-- BL del Caso 3: Repuestos
INSERT IGNORE INTO documentos_transporte
  (manifiesto_id, tipo_documento, numero_documento, master_house,
   fecha_embarque, puerto_origen, puerto_destino,
   peso_bruto, bultos)
VALUES
  ((SELECT id FROM manifiestos_carga WHERE numero_manifiesto = '2026-118-MAN-000999' LIMIT 1),
   'BL', 'ONEY2026YK00654321', 'MASTER',
   '2026-05-05 00:00:00', 'YOKOHAMA (JPYOK)', 'CALLAO (PECEL)',
   1200.000, 50);


-- ============================================================================================
-- SECCION 9: CONTENEDORES ASOCIADOS
-- ============================================================================================

-- Contenedor del Caso 1
INSERT IGNORE INTO contenedores
  (documento_transporte_id, numero_contenedor, tipo_contenedor,
   precinto_origen, precinto_aduanero, estado_precinto,
   peso_manifestado, peso_recibido)
VALUES
  ((SELECT id FROM documentos_transporte WHERE numero_documento = 'HLCU2026SH00123456' LIMIT 1),
   'HLXU9876543', '20GP',
   'CN-SHA-2026-098765', 'PE-118-2026-001234', 'VERIFICADO',
   350.500, 349.800);

-- Contenedor del Caso 3
INSERT IGNORE INTO contenedores
  (documento_transporte_id, numero_contenedor, tipo_contenedor,
   precinto_origen, precinto_aduanero, estado_precinto,
   peso_manifestado, peso_recibido)
VALUES
  ((SELECT id FROM documentos_transporte WHERE numero_documento = 'ONEY2026YK00654321' LIMIT 1),
   'TGHU1234567', '40HC',
   'JP-YOK-2026-112233', NULL, 'NO_VERIFICADO',
   1200.000, NULL);


-- ============================================================================================
-- SECCION 10: DAM (DECLARACION ADUANERA DE MERCANCIAS) PARA CASO 1
-- ============================================================================================

INSERT IGNORE INTO dam_cabecera
  (operacion_id, numero_dam, regimen_codigo, modalidad_codigo,
   aduana_codigo, canal_control, canal_es_oficial,
   estado, fecha_numeracion, fecha_levante,
   fuente, source_type, confidence)
VALUES
  (1, '118-2026-10-000456', '10', 'ANTICIPADO',
   '118', 'VERDE', TRUE,
   'LEVANTE_AUTORIZADO', '2026-05-15 09:30:00', '2026-05-15 14:00:00',
   'SEED_DATOS_REALES', 'SIMULADO', 0.90);

-- Serie 1 de la DAM del Caso 1
INSERT IGNORE INTO dam_series
  (dam_id, numero_serie, hs_code, descripcion_mercancia,
   marca, modelo, estado_mercancia, pais_origen,
   cantidad, unidad_medida, peso_neto, peso_bruto,
   valor_fob, flete, seguro, valor_cif,
   ad_valorem_pct, igv_pct, ipm_pct, isc_pct, requiere_permiso)
VALUES
  ((SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   1, '8471300000', 'LAPTOPS LENOVO THINKPAD T14 GEN 5, 14 PULGADAS, AMD RYZEN 7 PRO, 16GB RAM, 512GB SSD, NUEVAS EN CAJA',
   'LENOVO', 'THINKPAD T14 GEN 5 21MCS00100', 'NUEVO', 'CN',
   100.000, 'NIU', 280.000, 350.500,
   45000.00, 1200.00, 300.00, 46500.00,
   0.00, 16.00, 2.00, 0.00, FALSE);


-- ============================================================================================
-- SECCION 11: DEUDA TRIBUTARIA ADUANERA - CASO 1
-- ============================================================================================

INSERT IGNORE INTO deuda_tributaria_aduanera
  (operacion_id, dam_id,
   fecha_nacimiento, fecha_exigibilidad, fecha_cancelacion,
   base_imponible_cif, ad_valorem, isc, igv, ipm, percepcion,
   recargos, multas, intereses, total,
   estado, source_type, confidence)
VALUES
  (1,
   (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   '2026-05-15 09:30:00', '2026-05-15 09:30:00', '2026-05-15 10:15:00',
   46500.00, 0.00, 0.00, 7440.00, 930.00, 1920.45,
   0.00, 0.00, 0.00, 10290.45,
   'CANCELADA', 'SIMULADO', 0.90);


-- ============================================================================================
-- SECCION 12: EVENTOS ADUANEROS (TIMELINE) - CASO 1
-- ============================================================================================

INSERT IGNORE INTO eventos_aduaneros
  (operacion_id, dam_id, evento_codigo, evento_nombre, fecha_evento,
   responsable_tipo, documento_asociado, efecto_legal,
   fuente, observacion, source_type, confidence)
VALUES
  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'EMBARQUE', 'Embarque en puerto de origen', '2026-04-28 14:00:00',
   'EXPORTADOR', 'BL HLCU2026SH00123456', 'Inicio del transporte internacional',
   'SEED_DATOS_REALES', 'Embarque desde Shanghai, China. Naviera Hapag-Lloyd.', 'SIMULADO', 0.90),

  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'ARRIBO_NAVE', 'Arribo de la nave al puerto del Callao', '2026-05-12 06:00:00',
   'TRANSPORTISTA', 'Manifiesto 2026-118-MAN-000567', 'Nacimiento de la obligacion tributaria aduanera',
   'SEED_DATOS_REALES', 'Nave MV HAMBURG EXPRESS, bandera alemana.', 'SIMULADO', 0.90),

  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'DESCARGA', 'Termino de descarga en DP World Callao', '2026-05-12 18:00:00',
   'TERMINAL_PORTUARIO', 'Nota de tarja', 'Inicia plazo para despacho diferido (15 dias)',
   'SEED_DATOS_REALES', 'Descarga completada en Muelle Sur, DP World.', 'SIMULADO', 0.90),

  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'NUMERACION_DAM', 'Numeracion de la DAM', '2026-05-15 09:30:00',
   'AGENTE_ADUANA', 'DAM 118-2026-10-000456', 'Formaliza la declaracion ante SUNAT',
   'SEED_DATOS_REALES', 'Despacho anticipado. Agente de aduana: Agencia Lima S.A.C.', 'SIMULADO', 0.90),

  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'CANAL_ASIGNADO', 'Asignacion de canal de control: VERDE', '2026-05-15 09:35:00',
   'SISTEMA_SUNAT', 'DAM 118-2026-10-000456', 'No requiere revision documentaria ni reconocimiento fisico',
   'SEED_DATOS_REALES', 'Canal verde asignado por sistema GCA/SIGAD de SUNAT.', 'SIMULADO', 0.90),

  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'PAGO_TRIBUTOS', 'Cancelacion de tributos aduaneros', '2026-05-15 10:15:00',
   'IMPORTADOR', 'Liquidacion de cobranza', 'Extingue la deuda tributaria aduanera',
   'SEED_DATOS_REALES', 'Pago total USD 10,290.45 via Banco de la Nacion.', 'SIMULADO', 0.90),

  (1, (SELECT id FROM dam_cabecera WHERE numero_dam = '118-2026-10-000456' LIMIT 1),
   'LEVANTE', 'Autorizacion de levante', '2026-05-15 14:00:00',
   'SISTEMA_SUNAT', 'DAM 118-2026-10-000456', 'Mercancia puede ser retirada del deposito temporal',
   'SEED_DATOS_REALES', 'Levante automatico tras verificacion de pago y canal verde.', 'SIMULADO', 0.90);


-- ============================================================================================
-- SECCION 13: VERIFICACION VUCE - CASO 2 (COSMETICOS CON DIGEMID)
-- ============================================================================================

INSERT IGNORE INTO vuce_verificaciones
  (operacion_id, entidad, requiere_permiso, permiso_obtenido, observaciones)
VALUES
  (2, 'DIGEMID', TRUE, FALSE,
   'Los cosmeticos y productos de cuidado de la piel (HS 3304) requieren Notificacion Sanitaria Obligatoria (NSO) ante DIGEMID. Sin este documento, la mercancia sera retenida en canal rojo y no se autorizara el levante. Plazo estimado de tramite: 15-30 dias habiles. Base legal: D.S. 016-2011-SA y Decision 516 CAN.');


-- ============================================================================================
-- SECCION 14: ALERTAS REGULATORIAS - CASO 2
-- ============================================================================================

INSERT IGNORE INTO alertas_regulatorias
  (operacion_id, hs_code, tipo_alerta, severidad,
   mensaje, base_legal, accion_recomendada,
   estado, fuente, source_type, confidence)
VALUES
  (2, '3304990000', 'MERCANCIA_RESTRINGIDA', 'ALTA',
   'La subpartida 3304990000 (cosmeticos y preparaciones de belleza) esta clasificada como mercancia RESTRINGIDA. Requiere Notificacion Sanitaria Obligatoria (NSO) emitida por DIGEMID antes de la importacion.',
   'Decreto Supremo 016-2011-SA, Reglamento para el Registro, Control y Vigilancia Sanitaria de Productos Farmaceuticos. Decision 516 de la Comunidad Andina.',
   'Tramitar la Notificacion Sanitaria Obligatoria ante DIGEMID a traves de la Ventanilla Unica de Comercio Exterior (VUCE). Presentar: formulario de solicitud, certificado de libre comercializacion del pais de origen, formula cualitativa y cuantitativa, especificaciones tecnicas del producto, rotulado y etiquetado propuesto.',
   'PENDIENTE', 'SEED_DATOS_REALES', 'SIMULADO', 0.85);


-- ============================================================================================
-- SECCION 15: GARANTIA ADUANERA - CASO 3 (TRANSITO ADUANERO)
-- ============================================================================================

INSERT IGNORE INTO garantias_aduaneras
  (operacion_id, regimen_codigo, tipo_garantia,
   monto_minimo, moneda,
   estado, fuente, source_type, confidence)
VALUES
  (3, '80', 'CARTA_FIANZA',
   5988.00, 'USD',
   'PENDIENTE', 'SEED_DATOS_REALES', 'SIMULADO', 0.85);


-- ============================================================================================
-- SECCION 16: SINCRONIZACION CON TABLA arancel_hs_peru_2022
-- ============================================================================================
-- Las subpartidas clave ya deben existir en arancel_hs_peru_2022.
-- Insertamos las que pueden faltar (solo las de nuestros casos de estudio).

INSERT IGNORE INTO arancel_hs_peru_2022
  (codigo, codigo_sin_puntos, descripcion, ad_valorem, capitulo, capitulo_nombre, partida, partida_desc)
VALUES
('8471.30.00.00', '8471300000', 'Maquinas automaticas para tratamiento o procesamiento de datos, portatiles, de peso inferior o igual a 10 kg', 0.00, 84, 'Maquinas y aparatos mecanicos', '84.71', 'Maquinas automaticas para tratamiento o procesamiento de datos y sus unidades'),
('8517.13.00.00', '8517130000', 'Telefonos inteligentes (smartphones)', 0.00, 85, 'Maquinas, aparatos y material electrico', '85.17', 'Telefonos, incluidos los telefonos moviles (celulares) y los de otras redes inalambricas'),
('8528.72.00.00', '8528720000', 'Los demas aparatos receptores de television, en colores, con pantalla plana', 6.00, 85, 'Maquinas, aparatos y material electrico', '85.28', 'Monitores y proyectores, que no incorporen aparato receptor de television; aparatos receptores de television'),
('3304.99.00.00', '3304990000', 'Las demas preparaciones de belleza, maquillaje y para el cuidado de la piel', 6.00, 33, 'Aceites esenciales, perfumeria, cosmeticos', '33.04', 'Preparaciones de belleza, maquillaje y para el cuidado de la piel'),
('3304.10.00.00', '3304100000', 'Preparaciones para el maquillaje de los labios', 6.00, 33, 'Aceites esenciales, perfumeria, cosmeticos', '33.04', 'Preparaciones de belleza, maquillaje y para el cuidado de la piel'),
('3304.20.00.00', '3304200000', 'Preparaciones para el maquillaje de los ojos', 6.00, 33, 'Aceites esenciales, perfumeria, cosmeticos', '33.04', 'Preparaciones de belleza, maquillaje y para el cuidado de la piel'),
('3004.90.29.00', '3004902900', 'Los demas medicamentos dosificados para usos terapeuticos o profilacticos', 0.00, 30, 'Productos farmaceuticos', '30.04', 'Medicamentos dosificados o acondicionados para la venta al por menor'),
('3003.90.00.00', '3003900000', 'Los demas medicamentos sin dosificar ni acondicionar', 0.00, 30, 'Productos farmaceuticos', '30.03', 'Medicamentos sin dosificar ni acondicionar para la venta al por menor'),
('0901.11.00.00', '0901110000', 'Cafe sin tostar, sin descafeinar', 0.00, 9, 'Cafe, te, yerba mate y especias', '09.01', 'Cafe, incluso tostado o descafeinado'),
('2204.21.00.00', '2204210000', 'Vino de uvas frescas en recipientes de capacidad inferior o igual a 2 litros', 6.00, 22, 'Bebidas, liquidos alcoholicos y vinagre', '22.04', 'Vino de uvas frescas, incluso encabezado; mosto de uva'),
('1704.90.00.00', '1704900000', 'Los demas articulos de confiteria sin cacao', 6.00, 17, 'Azucares y articulos de confiteria', '17.04', 'Articulos de confiteria sin cacao (incluido el chocolate blanco)'),
('6109.10.00.00', '6109100000', 'T-shirts y camisetas interiores, de punto, de algodon', 11.00, 61, 'Prendas y complementos de vestir, de punto', '61.09', 'T-shirts y camisetas interiores, de punto'),
('6110.20.00.00', '6110200000', 'Sueteres, pullovers, cardiganes, chalecos y articulos similares, de punto, de algodon', 11.00, 61, 'Prendas y complementos de vestir, de punto', '61.10', 'Sueteres, pullovers, cardiganes, chalecos y articulos similares, de punto'),
('8708.99.90.90', '8708999090', 'Las demas partes y accesorios de vehiculos automoviles', 6.00, 87, 'Vehiculos automoviles, tractores', '87.08', 'Partes y accesorios de vehiculos automoviles'),
('8703.23.00.00', '8703230000', 'Vehiculos con motor de gasolina de cilindrada superior a 1500 cm3 pero inferior o igual a 3000 cm3', 6.00, 87, 'Vehiculos automoviles, tractores', '87.03', 'Automoviles de turismo y demas vehiculos'),
('4011.10.00.00', '4011100000', 'Neumaticos nuevos de caucho para automoviles de turismo', 6.00, 40, 'Caucho y sus manufacturas', '40.11', 'Neumaticos nuevos de caucho'),
('8429.11.00.00', '8429110000', 'Topadoras frontales (bulldozers) y topadoras angulares, de orugas', 0.00, 84, 'Maquinas y aparatos mecanicos', '84.29', 'Topadoras, niveladoras, traillas, palas mecanicas, excavadoras'),
('8430.20.00.00', '8430200000', 'Quitanieves', 0.00, 84, 'Maquinas y aparatos mecanicos', '84.30', 'Las demas maquinas y aparatos para explanar, nivelar, cortar'),
('2710.19.21.00', '2710192100', 'Gasolinas sin tetraetilo de plomo para motores, RON >= 97', 0.00, 27, 'Combustibles minerales, aceites minerales', '27.10', 'Aceites de petroleo o de mineral bituminoso'),
('3808.94.00.00', '3808940000', 'Desinfectantes', 6.00, 38, 'Productos de las industrias quimicas', '38.08', 'Insecticidas, raticidas, fungicidas, herbicidas, desinfectantes'),
('9503.00.00.00', '9503000000', 'Juguetes diversos: triciclos, munecas, modelos reducidos', 6.00, 95, 'Juguetes, juegos y articulos para recreo', '95.03', 'Triciclos, patinetes, juguetes, modelos reducidos'),
('9504.50.00.00', '9504500000', 'Consolas y maquinas de videojuego', 0.00, 95, 'Juguetes, juegos y articulos para recreo', '95.04', 'Consolas y maquinas de videojuego, articulos para juegos de feria'),
('1005.90.00.00', '1005900000', 'Maiz excepto para siembra', 0.00, 10, 'Cereales', '10.05', 'Maiz'),
('1001.19.00.00', '1001190000', 'Los demas trigos excepto para siembra', 0.00, 10, 'Cereales', '10.01', 'Trigo y morcajo (tranquillon)'),
('0207.14.00.90', '0207140090', 'Trozos y despojos de gallo o gallina, congelados: Los demas', 0.00, 2, 'Carne y despojos comestibles', '02.07', 'Carne y despojos comestibles de aves de la partida 01.05');


-- ============================================================================================
-- SECCION 17: DATOS DE VERIFICACION - SELECT FINALES
-- ============================================================================================

SELECT '======================================================' AS '';
SELECT 'VERIFICACION DE DATOS INSERTADOS - SEED_DATOS_REALES' AS '';
SELECT '======================================================' AS '';

SELECT 'TABLA hs_codes' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM hs_codes;
SELECT 'TABLA partidas_arancelarias' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM partidas_arancelarias;
SELECT 'TABLA tlc_acuerdos' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM tlc_acuerdos;
SELECT 'TABLA depositos_temporales' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM depositos_temporales;
SELECT 'TABLA intendencias_aduana' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM intendencias_aduana;
SELECT 'TABLA operaciones (casos)' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM operaciones;
SELECT 'TABLA manifiestos_carga' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM manifiestos_carga;
SELECT 'TABLA documentos_transporte' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM documentos_transporte;
SELECT 'TABLA contenedores' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM contenedores;
SELECT 'TABLA dam_cabecera' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM dam_cabecera;
SELECT 'TABLA dam_series' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM dam_series;
SELECT 'TABLA deuda_tributaria_aduanera' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM deuda_tributaria_aduanera;
SELECT 'TABLA eventos_aduaneros' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM eventos_aduaneros;
SELECT 'TABLA vuce_verificaciones' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM vuce_verificaciones;
SELECT 'TABLA alertas_regulatorias' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM alertas_regulatorias;
SELECT 'TABLA garantias_aduaneras' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM garantias_aduaneras;
SELECT 'TABLA arancel_hs_peru_2022' AS TABLA, COUNT(*) AS TOTAL_REGISTROS FROM arancel_hs_peru_2022;

SELECT '======================================================' AS '';
SELECT 'DETALLE: Subpartidas por sector (hs_codes)' AS '';
SELECT '======================================================' AS '';

SELECT
  CASE
    WHEN codigo LIKE '84%' OR codigo LIKE '85%' THEN 'TECNOLOGIA'
    WHEN codigo LIKE '33%' OR codigo LIKE '30%' THEN 'BELLEZA_SALUD'
    WHEN codigo LIKE '09%' OR codigo LIKE '22%' OR codigo LIKE '17%' OR codigo LIKE '08%' OR codigo LIKE '20%' OR codigo LIKE '18%' OR codigo LIKE '21%' OR codigo LIKE '04%' THEN 'ALIMENTOS_BEBIDAS'
    WHEN codigo LIKE '61%' OR codigo LIKE '62%' OR codigo LIKE '63%' OR codigo LIKE '64%' THEN 'TEXTILES'
    WHEN codigo LIKE '87%' OR codigo LIKE '40%' THEN 'AUTOMOTRIZ'
    WHEN codigo LIKE '27%' OR codigo LIKE '34%' OR codigo LIKE '38%' THEN 'QUIMICOS'
    WHEN codigo LIKE '95%' THEN 'JUGUETES'
    WHEN codigo LIKE '10%' OR codigo LIKE '02%' OR codigo LIKE '12%' THEN 'AGROPECUARIO'
    ELSE 'OTRO'
  END AS SECTOR,
  COUNT(*) AS CANTIDAD,
  MIN(ad_valorem) AS AD_VALOREM_MIN,
  MAX(ad_valorem) AS AD_VALOREM_MAX
FROM hs_codes
GROUP BY SECTOR
ORDER BY CANTIDAD DESC;

SELECT '======================================================' AS '';
SELECT 'DETALLE: Casos de estudio (operaciones)' AS '';
SELECT '======================================================' AS '';

SELECT id, producto_desc, hs_code, pais_origen, estado, canal_asignado, total_impuestos
FROM operaciones
ORDER BY id;

SELECT '======================================================' AS '';
SELECT 'SEED COMPLETO - FIN DEL SCRIPT' AS '';
SELECT '======================================================' AS '';

SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;

-- >>> FIN: sql\importease_full_schema.sql <<<

-- >>> INICIO: sql\seed_normativa.sql <<<
-- =====================================================================
-- IMPORTEASE ADUANERO - SEED DE NORMATIVA ADUANERA PERUANA
-- =====================================================================
-- Version: 1.0
-- Fuente: Ley General de Aduanas (Decreto Supremo), DESPA-PG, VUCE
-- =====================================================================

-- (Railway: ya conectado a la BD)
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

-- >>> FIN: sql\seed_normativa.sql <<<

-- >>> INICIO: sql\upgrade_v3.7_borrador_guiado.sql <<<
-- ================================================================
-- UPGRADE v3.7 - PERSISTENCIA DE BORRADOR DE IMPORTACION GUIADA
-- ================================================================

-- (Railway: ya conectado a la BD)

-- 1. Crear la tabla de borradores de importaciÃ³n guiada
CREATE TABLE IF NOT EXISTS importacion_borrador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    paso_actual INT NOT NULL DEFAULT 1,
    json_borrador MEDIUMTEXT NOT NULL,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    estado VARCHAR(50) DEFAULT 'BORRADOR',
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY uk_usuario_borrador (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- >>> FIN: sql\upgrade_v3.7_borrador_guiado.sql <<<

-- >>> INICIO: sql\upgrade_v4.0_flujo_guiado.sql <<<
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

-- >>> FIN: sql\upgrade_v4.0_flujo_guiado.sql <<<

-- >>> INICIO: sql\upgrade_v4.1_normativa.sql <<<
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

-- >>> FIN: sql\upgrade_v4.1_normativa.sql <<<

-- >>> INICIO: sql\upgrade_v5.0_fuentes_reales.sql <<<
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

-- >>> FIN: sql\upgrade_v5.0_fuentes_reales.sql <<<

-- >>> INICIO: sql\upgrade_v5.1_password_reset.sql <<<
-- ====================================================================
-- v5.1 - Recuperacion de contrasena segura
-- ====================================================================
-- Guarda solo hash SHA-256 del token; nunca guardar token plano.
-- El token expira y se invalida tras un unico uso.
-- ====================================================================

SET NAMES utf8mb4;
-- (Railway: ya conectado a la BD)

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    UNIQUE KEY uk_password_reset_token_hash (token_hash),
    INDEX idx_password_reset_usuario_estado (usuario_id, used_at, expires_at),
    INDEX idx_password_reset_created_ip (created_ip, created_at),
    CONSTRAINT fk_password_reset_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- >>> FIN: sql\upgrade_v5.1_password_reset.sql <<<


