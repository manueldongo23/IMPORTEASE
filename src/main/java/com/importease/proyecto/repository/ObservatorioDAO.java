package com.importease.proyecto.repository;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ObservatorioDAO {

    public List<Map<String, Object>> obtenerStatsCache(String hs6) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT * FROM trade_import_stats WHERE hs6 = ? ORDER BY anio DESC, valor_usd DESC LIMIT 80";
        try (Connection con = ConexionDB.obtenerConexionSecundaria();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hs6);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("hsCode", rs.getString("hs_code"));
                    item.put("hs6", rs.getString("hs6"));
                    item.put("paisOrigen", rs.getString("pais_origen"));
                    item.put("paisDestino", rs.getString("pais_destino"));
                    item.put("anio", rs.getInt("anio"));
                    item.put("valorUsd", rs.getBigDecimal("valor_usd"));
                    item.put("cantidad", rs.getBigDecimal("cantidad"));
                    item.put("unidad", rs.getString("unidad"));
                    item.put("fuente", rs.getString("fuente"));
                    item.put("sourceType", rs.getString("source_type"));
                    item.put("confidence", rs.getBigDecimal("confidence"));
                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.warn("trade_import_stats no disponible para cache: " + e.getMessage());
        }
        return lista;
    }

    public void guardarStats(String hsCode, String hs6, List<Map<String, Object>> stats, String sourceType, double confidence) {
        if (stats == null || stats.isEmpty()) return;
        String sql = "INSERT INTO trade_import_stats " +
                "(hs_code, hs6, pais_origen, pais_destino, anio, valor_usd, cantidad, unidad, fuente, source_type, confidence) " +
                "VALUES (?, ?, ?, 'Peru', ?, ?, ?, ?, 'UN_COMTRADE_API', ?, ?)";
        try (Connection con = ConexionDB.obtenerConexionSecundaria();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map<String, Object> item : stats) {
                ps.setString(1, hsCode);
                ps.setString(2, hs6);
                ps.setString(3, str(item.get("paisOrigen")));
                ps.setInt(4, asInt(item.get("anio"), 0));
                ps.setBigDecimal(5, decimal(item.get("valorUsd")));
                ps.setBigDecimal(6, decimal(item.get("cantidad")));
                ps.setString(7, str(item.get("unidad")));
                ps.setString(8, sourceType);
                ps.setBigDecimal(9, BigDecimal.valueOf(confidence));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            LoggerUtil.warn("No se pudo guardar cache UN Comtrade: " + e.getMessage());
        }
    }

    public void registrarSync(String hsCode, String endpoint, Integer statusHttp, String resultado, String mensaje) {
        String sql = "INSERT INTO trade_sync_log (hs_code, endpoint, status_http, resultado, mensaje) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexionSecundaria();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hsCode);
            ps.setString(2, endpoint);
            if (statusHttp != null) ps.setInt(3, statusHttp); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, resultado);
            ps.setString(5, mensaje);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.warn("trade_sync_log no disponible: " + e.getMessage());
        }
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value, int fallback) {
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }

    private BigDecimal decimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try { return new BigDecimal(String.valueOf(value)); } catch (Exception e) { return null; }
    }
}


