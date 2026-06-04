package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestriccionesFormalService {

    private static final String QUERY_LOOKUP = "SELECT 1 FROM restricciones_formal WHERE ? LIKE CONCAT(rango_hs, '%') LIMIT 1";
    private static final String QUERY_LOOKUP_DETAIL = "SELECT entidad, permiso_requerido, fuente_base_legal FROM restricciones_formal WHERE ? LIKE CONCAT(rango_hs, '%') LIMIT 1";
    private static final String QUERY_ALL_ACTIVE = "SELECT id, rango_hs, descripcion, entidad, permiso_requerido, vigencia, fuente_base_legal, fuente_url, version FROM restricciones_formal WHERE activo = TRUE ORDER BY rango_hs";

    public static boolean esRestringido(String hsCode) {
        if (hsCode == null || hsCode.isBlank()) return false;
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(QUERY_LOOKUP)) {
            ps.setString(1, hsCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException e) {
            LoggerUtil.error("RestriccionesFormalService: error en BD, fallback a heuristica", e);
        }
        return NormalizadorUtil.looksRestricted(hsCode);
    }

    public static Map<String, Object> verificarRestriccion(String hsCode) {
        if (hsCode == null || hsCode.isBlank()) {
            Map<String, Object> result = new HashMap<>();
            result.put("restringido", false);
            return result;
        }
        String code = hsCode.trim();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(QUERY_LOOKUP_DETAIL)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("restringido", true);
                    result.put("entidad", rs.getString("entidad"));
                    result.put("permiso_requerido", rs.getString("permiso_requerido"));
                    result.put("fuente_base_legal", rs.getString("fuente_base_legal"));
                    result.put("sourceType", "OFFICIAL_PROCEDURE");
                    result.put("confidence", 0.95);
                    return result;
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("RestriccionesFormalService: error en BD al verificar restriccion", e);
        }
        if (NormalizadorUtil.looksRestricted(code)) {
            Map<String, Object> result = new HashMap<>();
            result.put("restringido", true);
            result.put("entidad", "Heuristica");
            result.put("permiso_requerido", "Por confirmar con entidad sectorial");
            result.put("sourceType", "REFERENTIAL");
            result.put("confidence", 0.50);
            return result;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("restringido", false);
        return result;
    }

    public static List<Map<String, Object>> obtenerTodasRestricciones() {
        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(QUERY_ALL_ACTIVE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("rango_hs", rs.getString("rango_hs"));
                row.put("descripcion", rs.getString("descripcion"));
                row.put("entidad", rs.getString("entidad"));
                row.put("permiso_requerido", rs.getString("permiso_requerido"));
                row.put("vigencia", rs.getString("vigencia"));
                row.put("fuente_base_legal", rs.getString("fuente_base_legal"));
                row.put("fuente_url", rs.getString("fuente_url"));
                row.put("version", rs.getString("version"));
                lista.add(row);
            }
        } catch (SQLException e) {
            LoggerUtil.error("RestriccionesFormalService: error al obtener todas las restricciones", e);
        }
        return lista;
    }
}


