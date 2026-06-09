package com.importease.proyecto.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio de base de datos para consultas de validación PREDAM.
 */
public class PredamValidacionRepositorio {

    public Map<String, Object> obtenerDatosUsuario(Connection con, int usuarioId) throws SQLException {
        String sql = "SELECT ruc, ruc_validado, estado_ruc, condicion_ruc FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("ruc", rs.getString("ruc"));
                    data.put("ruc_validado", rs.getBoolean("ruc_validado"));
                    data.put("estado_ruc", rs.getString("estado_ruc"));
                    data.put("condicion_ruc", rs.getString("condicion_ruc"));
                    return data;
                }
            }
        }
        return null;
    }

    public Boolean existeDocumentoTransporteVinculado(Connection con, int operacionId) throws SQLException {
        String sql = "SELECT 1 FROM documentos_transporte dt INNER JOIN manifiestos_carga m ON dt.manifiesto_id = m.id WHERE m.operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs == null) return null;
                return rs.next();
            }
        }
    }

    public boolean existeManifiesto(Connection con, int operacionId) throws SQLException {
        String sql = "SELECT 1 FROM manifiestos_carga WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs != null && rs.next();
            }
        }
    }

    public Map<String, Object> obtenerDocumentosMinimosOperacion(Connection con, int operacionId) throws SQLException {
        String sql = "SELECT documento_factura, documento_bl, permiso_vuce_obtenido FROM operaciones WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs == null) return null;
                if (rs.next()) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("documento_factura", rs.getBoolean("documento_factura"));
                    data.put("documento_bl", rs.getBoolean("documento_bl"));
                    data.put("permiso_vuce_obtenido", rs.getBoolean("permiso_vuce_obtenido"));
                    return data;
                }
            }
        }
        return null;
    }

    public Boolean existePermisoSectorial(Connection con, int operacionId) throws SQLException {
        String sql = "SELECT 1 FROM documentos_importacion WHERE importacion_id = ? AND tipo_documento = 'PERMISO_SECTORIAL' AND soft_delete = FALSE LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs == null) return null;
                return rs.next();
            }
        }
    }

    public Map<String, Object> obtenerOrigenYConfianzaDam(Connection con, int operacionId) throws SQLException {
        String sql = "SELECT source_type, confidence FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("source_type", rs.getString("source_type"));
                    data.put("confidence", rs.getDouble("confidence"));
                    return data;
                }
            }
        }
        return null;
    }

    public List<Map<String, Object>> obtenerFuentesResumen(Connection con, int expedienteId) throws SQLException {
        String sql = "SELECT source_type, fuente, COUNT(*) as total FROM ("
                + "SELECT source_type, fuente FROM manifiestos_carga WHERE operacion_id = ? "
                + "UNION ALL "
                + "SELECT source_type, fuente FROM dam_cabecera WHERE operacion_id = ? "
                + "UNION ALL "
                + "SELECT source_type, 'OPERACION' FROM operaciones WHERE id = ? "
                + ") AS fuentes GROUP BY source_type, fuente";
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            ps.setInt(2, expedienteId);
            ps.setInt(3, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("source_type", rs.getString("source_type"));
                    item.put("fuente", rs.getString("fuente"));
                    item.put("total", rs.getInt("total"));
                    list.add(item);
                }
            }
        }
        return list;
    }
}
