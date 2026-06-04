-- ====================================================================
-- v5.1 - Recuperacion de contrasena segura
-- ====================================================================
-- Guarda solo hash SHA-256 del token; nunca guardar token plano.
-- El token expira y se invalida tras un unico uso.
-- ====================================================================

SET NAMES utf8mb4;
USE importease_db;

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
