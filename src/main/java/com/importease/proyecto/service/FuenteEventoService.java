package com.importease.proyecto.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class FuenteEventoService {
    public void registrarOk(String fuente, String tipoEvento, String referencia, String url, String metodoHttp, int statusHttp, String payload, long duracionMs) {
        registrar(fuente, tipoEvento, referencia, url, metodoHttp, statusHttp, payload, "OK", null, duracionMs);
    }

    public void registrarError(String fuente, String tipoEvento, String referencia, String url, String metodoHttp, int statusHttp, String mensajeError, long duracionMs) {
        registrar(fuente, tipoEvento, referencia, url, metodoHttp, statusHttp, null, "ERROR", mensajeError, duracionMs);
    }

    public void registrarFallback(String fuente, String tipoEvento, String referencia, String mensajeError) {
        registrar(fuente, tipoEvento, referencia, null, null, 0, null, "FALLBACK", mensajeError, 0);
    }

    public void registrarSimulado(String fuente, String tipoEvento, String referencia, String detalle) {
        registrar(fuente, tipoEvento, referencia, null, null, 0, detalle, "SIMULADO", null, 0);
    }

    public void registrarCache(String fuente, String tipoEvento, String referencia) {
        registrar(fuente, tipoEvento, referencia, null, null, 0, null, "CACHE", null, 0);
    }

    public void registrar(String fuente, String tipoEvento, String referencia, String url, String metodoHttp, int statusHttp, String payload, String resultado, String mensajeError, long duracionMs) {
        String sourceType = sourceTypeFor(fuente, resultado);
        String tipoFuente = tipoFuenteFor(fuente, sourceType);
        double confianza = DataConfidenceService.confidenceFor(sourceType);
        String sqlV35 = "INSERT INTO fuente_eventos (fuente, tipo_evento, referencia, url, metodo_http, status_http, payload_hash, resultado, mensaje_error, duracion_ms, tipo_fuente, entidad_afectada, source_type, confianza) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sqlV35)) {
            ps.setString(1, safe(fuente, "DESCONOCIDA", 80));
            ps.setString(2, safe(tipoEvento, "EVENTO", 80));
            ps.setString(3, safe(referencia, null, 120));
            ps.setString(4, safe(url, null, 500));
            ps.setString(5, safe(metodoHttp, null, 10));
            if (statusHttp > 0) ps.setInt(6, statusHttp);
            else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, hash(payload));
            ps.setString(8, safe(resultado, "ERROR", 20));
            ps.setString(9, mensajeError);
            if (duracionMs > 0) ps.setInt(10, (int) Math.min(duracionMs, Integer.MAX_VALUE));
            else ps.setNull(10, java.sql.Types.INTEGER);
            ps.setString(11, tipoFuente);
            ps.setString(12, safe(tipoEvento, null, 80));
            ps.setString(13, sourceType);
            ps.setBigDecimal(14, java.math.BigDecimal.valueOf(confianza));
            ps.executeUpdate();
        } catch (Exception e) {
            registrarLegacy(fuente, tipoEvento, referencia, url, metodoHttp, statusHttp, payload, resultado, mensajeError, duracionMs, e);
        }
    }

    private void registrarLegacy(String fuente, String tipoEvento, String referencia, String url, String metodoHttp, int statusHttp, String payload, String resultado, String mensajeError, long duracionMs, Exception original) {
        String sql = "INSERT INTO fuente_eventos (fuente, tipo_evento, referencia, url, metodo_http, status_http, payload_hash, resultado, mensaje_error, duracion_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, safe(fuente, "DESCONOCIDA", 80));
            ps.setString(2, safe(tipoEvento, "EVENTO", 80));
            ps.setString(3, safe(referencia, null, 120));
            ps.setString(4, safe(url, null, 500));
            ps.setString(5, safe(metodoHttp, null, 10));
            if (statusHttp > 0) ps.setInt(6, statusHttp);
            else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, hash(payload));
            ps.setString(8, safe(resultado, "ERROR", 20));
            ps.setString(9, mensajeError);
            if (duracionMs > 0) ps.setInt(10, (int) Math.min(duracionMs, Integer.MAX_VALUE));
            else ps.setNull(10, java.sql.Types.INTEGER);
            ps.executeUpdate();
        } catch (Exception legacy) {
            LoggerUtil.warn("No se pudo registrar fuente_eventos: " + original.getMessage());
        }
    }

    public static String hash(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : encoded) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String value, String fallback, int max) {
        String result = (value == null || value.isBlank()) ? fallback : value.trim();
        if (result == null) return null;
        return result.length() > max ? result.substring(0, max) : result;
    }

    private String tipoFuenteFor(String fuente, String sourceType) {
        String f = fuente == null ? "" : fuente.toUpperCase();
        if ("OFICIAL_API".equals(sourceType)) return "API_OFICIAL";
        if (f.contains("DHL") || f.contains("FEDEX") || f.contains("UPS") || f.contains("MAERSK") || f.contains("TRACKING")) return "WEB_TRACKING";
        if (f.contains("SUNAT") || f.contains("VUCE")) return "WEB_SCRAPING";
        if ("MANUAL".equals(sourceType) || "MANUAL_VERIFICADO".equals(sourceType)) return "MANUAL";
        return "BD_LOCAL";
    }

    private String sourceTypeFor(String fuente, String resultado) {
        String f = fuente == null ? "" : fuente.toUpperCase();
        String r = resultado == null ? "" : resultado.toUpperCase();
        if ("OK".equals(r) && (f.contains("BCRP") || f.contains("COMTRADE"))) return "OFICIAL_API";
        if ("OK".equals(r) && (f.contains("SUNAT") || f.contains("VUCE"))) return "OFICIAL_WEB";
        if ("CACHE".equals(r)) return "CACHE";
        if ("FALLBACK".equals(r)) return "FALLBACK";
        if ("SIMULADO".equals(r)) return "SIMULADO";
        if ("ERROR".equals(r) && (f.contains("DHL") || f.contains("FEDEX") || f.contains("UPS") || f.contains("MAERSK"))) return "PENDIENTE_CREDENCIALES";
        if ("ERROR".equals(r)) return "PENDIENTE_VALIDACION";
        return "BD_LOCAL";
    }
}

