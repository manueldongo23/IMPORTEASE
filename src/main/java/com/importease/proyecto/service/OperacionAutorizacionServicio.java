package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servicio de autorizacion para evitar IDOR en endpoints basados en operacion.
 */
public class OperacionAutorizacionServicio {

    public boolean isOperacionOwnedByUser(int operacionId, int usuarioId) {
        if (operacionId <= 0 || usuarioId <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM operaciones WHERE id = ? AND usuario_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            ps.setInt(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al validar propietario de operacion", e);
            return false;
        }
    }
}

