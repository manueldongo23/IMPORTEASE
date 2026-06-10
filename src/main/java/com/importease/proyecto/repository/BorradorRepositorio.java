package com.importease.proyecto.repository;

import com.importease.proyecto.model.Borrador;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BorradorRepositorio {

    public boolean guardarBorrador(int usuarioId, int pasoActual, String jsonBorrador) {
        String sql = "INSERT INTO importacion_borrador (usuario_id, paso_actual, json_borrador, estado) " +
                     "VALUES (?, ?, ?, 'BORRADOR') " +
                     "ON DUPLICATE KEY UPDATE " +
                     "paso_actual = VALUES(paso_actual), " +
                     "json_borrador = VALUES(json_borrador), " +
                     "fecha_actualizacion = CURRENT_TIMESTAMP";

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, pasoActual);
            ps.setString(3, jsonBorrador);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al guardar borrador de importacion", e);
            return false;
        }
    }

    public Borrador obtenerBorrador(int usuarioId) {
        String sql = "SELECT id, usuario_id, paso_actual, json_borrador, fecha_actualizacion, estado " +
                     "FROM importacion_borrador WHERE usuario_id = ?";

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Borrador b = new Borrador();
                    b.setId(rs.getInt("id"));
                    b.setUsuarioId(rs.getInt("usuario_id"));
                    b.setPasoActual(rs.getInt("paso_actual"));
                    b.setJsonBorrador(rs.getString("json_borrador"));
                    b.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
                    b.setEstado(rs.getString("estado"));
                    return b;
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener borrador de importacion", e);
        }
        return null;
    }

    public boolean eliminarBorrador(int usuarioId) {
        String sql = "DELETE FROM importacion_borrador WHERE usuario_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al eliminar borrador de importacion", e);
            return false;
        }
    }
}
