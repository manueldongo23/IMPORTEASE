package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EventoUsuarioServicio {
    public void registrar(Integer usuarioId, String sessionId, String evento, String modulo, String entidadTipo, String entidadId, String detalleJson, String ip, String userAgent) {
        String sql = "INSERT INTO eventos_usuario (usuario_id, session_id, evento, modulo, entidad_tipo, entidad_id, detalle, ip_hash, user_agent_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usuarioId != null) ps.setInt(1, usuarioId);
            else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, truncate(sessionId, 128));
            ps.setString(3, truncate(evento, 80));
            ps.setString(4, truncate(modulo, 80));
            ps.setString(5, truncate(entidadTipo, 80));
            ps.setString(6, truncate(entidadId, 80));
            ps.setString(7, (detalleJson == null || detalleJson.isBlank()) ? "{}" : detalleJson);
            ps.setString(8, FuenteEventoServicio.hash(ip));
            ps.setString(9, FuenteEventoServicio.hash(userAgent));
            ps.executeUpdate();
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo registrar eventos_usuario: " + e.getMessage());
        }
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.length() > max ? trimmed.substring(0, max) : trimmed;
    }
}

