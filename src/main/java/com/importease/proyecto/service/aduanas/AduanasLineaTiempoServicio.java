package com.importease.proyecto.service.aduanas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AduanasLineaTiempoServicio {

    public List<Map<String, Object>> queryTimeline(Connection con, int operacionId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT evento_codigo, evento_nombre, fecha_evento, responsable_tipo, documento_asociado, efecto_legal, observacion, fuente, source_type, confidence FROM eventos_aduaneros WHERE operacion_id = ? ORDER BY fecha_evento ASC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = baseResult();
                    row.put("codigo", rs.getString("evento_codigo"));
                    row.put("nombre", rs.getString("evento_nombre"));
                    row.put("fecha", rs.getTimestamp("fecha_evento").toString());
                    row.put("responsable", rs.getString("responsable_tipo"));
                    row.put("documento", rs.getString("documento_asociado"));
                    row.put("efectoLegal", rs.getString("efecto_legal"));
                    row.put("observacion", rs.getString("observacion"));
                    row.put("source", rs.getString("fuente"));
                    row.put("sourceType", rs.getString("source_type"));
                    row.put("confidence", rs.getDouble("confidence"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public List<Map<String, Object>> queryAlertas(Connection con, int operacionId) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT tipo_alerta, severidad, mensaje, base_legal, accion_recomendada, estado, fuente, source_type, confidence FROM alertas_regulatorias WHERE operacion_id = ? ORDER BY FIELD(severidad, 'ALTA','MEDIA','BAJA'), fecha_creacion DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = baseResult();
                    row.put("tipo", rs.getString("tipo_alerta"));
                    row.put("severidad", rs.getString("severidad"));
                    row.put("mensaje", rs.getString("mensaje"));
                    row.put("baseLegal", rs.getString("base_legal"));
                    row.put("accion", rs.getString("accion_recomendada"));
                    row.put("estado", rs.getString("estado"));
                    row.put("source", rs.getString("fuente"));
                    row.put("sourceType", rs.getString("source_type"));
                    row.put("confidence", rs.getDouble("confidence"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    private Map<String, Object> baseResult() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("source", AduanasConstructorRespuesta.SOURCE);
        map.put("sourceType", AduanasConstructorRespuesta.SOURCE_TYPE);
        map.put("confidence", AduanasConstructorRespuesta.CONFIDENCE);
        return map;
    }
}
