package com.importease.proyecto.repository;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Repositorio de base de datos para la persistencia segura de tokens de recuperación de contraseña.
 * Guarda solo hash del token, nunca el token plano.
 */
public class PasswordResetTokenRepositorio {

    public void invalidateActiveTokens(int usuarioId) {
        String sql = "UPDATE password_reset_tokens SET used_at = NOW() WHERE usuario_id = ? AND used_at IS NULL";
        try (Connection con = ConexionDB.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("No se pudieron invalidar tokens previos de recuperacion", e);
            throw new IllegalStateException("No se pudieron invalidar tokens previos.", e);
        }
    }

    public void createToken(int usuarioId, String tokenHash, Instant expiresAt, String ip, String userAgent) {
        String sql = "INSERT INTO password_reset_tokens (usuario_id, token_hash, expires_at, created_ip, user_agent) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tokenHash);
            ps.setTimestamp(3, Timestamp.from(expiresAt));
            ps.setString(4, truncate(ip, 45));
            ps.setString(5, truncate(userAgent, 255));
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("No se pudo crear token de recuperacion", e);
            throw new IllegalStateException("No se pudo registrar el token de recuperacion.", e);
        }
    }

    public boolean isValidUnusedToken(int usuarioId, String tokenHash) {
        String sql = "SELECT id FROM password_reset_tokens "
                + "WHERE usuario_id = ? AND token_hash = ? AND used_at IS NULL AND expires_at > NOW() "
                + "ORDER BY id DESC LIMIT 1";
        try (Connection con = ConexionDB.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LoggerUtil.error("No se pudo validar token de recuperacion", e);
            return false;
        }
    }

    public void markUsed(int usuarioId, String tokenHash) {
        String sql = "UPDATE password_reset_tokens SET used_at = NOW() "
                + "WHERE usuario_id = ? AND token_hash = ? AND used_at IS NULL";
        try (Connection con = ConexionDB.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tokenHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("No se pudo marcar token de recuperacion como usado", e);
            throw new IllegalStateException("No se pudo invalidar el token utilizado.", e);
        }
    }

    public int countRecentRequestsByIp(String ip, int minutes) {
        String sql = "SELECT COUNT(*) FROM password_reset_tokens WHERE created_ip = ? AND created_at >= ?";
        return count(sql, ip, minutes);
    }

    public int countRecentRequestsByUsuario(int usuarioId, int minutes) {
        String sql = "SELECT COUNT(*) FROM password_reset_tokens WHERE usuario_id = ? AND created_at >= ?";
        return count(sql, usuarioId, minutes);
    }

    private int count(String sql, Object value, int minutes) {
        Instant since = Instant.now().minusSeconds(minutes * 60L);
        try (Connection con = ConexionDB.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (value instanceof Integer) {
                ps.setInt(1, (Integer) value);
            } else {
                ps.setString(1, String.valueOf(value));
            }
            ps.setTimestamp(2, Timestamp.from(since));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            LoggerUtil.warn("No se pudo calcular rate limit de recuperacion: " + e.getMessage());
            return 0;
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
