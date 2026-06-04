CREATE INDEX IF NOT EXISTS idx_auditoria_usuario_fecha ON auditoria_eventos(usuario_id, created_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_expires ON password_reset_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_tipo_cambio_fecha ON tipo_cambio_diario(fecha);
CREATE INDEX IF NOT EXISTS idx_validaciones_ruc_resultado ON validaciones_ruc(resultado, fecha_validacion);
