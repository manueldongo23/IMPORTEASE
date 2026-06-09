-- ================================================================
-- UPGRADE v3.7 - PERSISTENCIA DE BORRADOR DE IMPORTACION GUIADA
-- ================================================================

USE importease_db;

-- 1. Crear la tabla de borradores de importación guiada
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
