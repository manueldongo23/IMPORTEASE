package com.importease.proyecto.repository.guidedflow;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuidedFlowStepRepository {
    public String getPasoEstado(int expedienteId, int paso) {
        String sql = "SELECT estado FROM expediente_guided_steps WHERE expediente_id = ? AND paso_numero = ?";
        try (Connection con = ConexionDB.obtenerConexion()) {
            return getStepEstado(con, expedienteId, paso);
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener estado del paso", e);
            return null;
        }
    }

    public void initStepsIfNeeded(Connection con, int expedienteId, int totalPasos) {
        String sql = "INSERT IGNORE INTO expediente_guided_steps (expediente_id, paso_numero, estado) VALUES (?, ?, 'PENDIENTE')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= totalPasos; i++) {
                ps.setInt(1, expedienteId);
                ps.setInt(2, i);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            LoggerUtil.error("Error al inicializar pasos guiados", e);
        }
    }

    public String getStepEstado(Connection con, int expedienteId, int paso) {
        String sql = "SELECT estado FROM expediente_guided_steps WHERE expediente_id = ? AND paso_numero = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            ps.setInt(2, paso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("estado");
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener estado del paso", e);
        }
        return null;
    }

    public String getMotivoBloqueo(Connection con, int expedienteId, int paso) {
        String sql = "SELECT motivo_bloqueo FROM expediente_guided_steps WHERE expediente_id = ? AND paso_numero = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            ps.setInt(2, paso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("motivo_bloqueo");
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener motivo de bloqueo", e);
        }
        return null;
    }

    public void setStepEstado(Connection con, int expedienteId, int paso, String estado) {
        setStepEstado(con, expedienteId, paso, estado, null);
    }

    public void setStepEstado(Connection con, int expedienteId, int paso, String estado, String motivo) {
        String sql = "INSERT INTO expediente_guided_steps (expediente_id, paso_numero, estado, motivo_bloqueo) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE estado = VALUES(estado), motivo_bloqueo = VALUES(motivo_bloqueo)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            ps.setInt(2, paso);
            ps.setString(3, estado);
            if (motivo != null) {
                ps.setString(4, motivo);
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Error al actualizar estado del paso", e);
        }
    }

    public int countCompletedSteps(Connection con, int expedienteId) {
        String sql = "SELECT COUNT(*) FROM expediente_guided_steps WHERE expediente_id = ? AND estado = 'COMPLETO'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al contar pasos completados", e);
        }
        return 0;
    }
}
