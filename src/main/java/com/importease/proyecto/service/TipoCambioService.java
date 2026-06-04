package com.importease.proyecto.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class TipoCambioService {
    private static int fallosConsecutivos = 0;
    private static long tiempoCircuitoAbierto = 0;
    private static final long DURACION_CIRCUITO_ABIERTO = 3600000;
    private static final String BCRP_SERIES = "PD04639PD-PD04640PD";
    private static final String BCRP_URL = "https://estadisticas.bcrp.gob.pe/estadisticas/series/api/" + BCRP_SERIES + "/json";

    // Campos de estado locales a cada hilo de ejecuciÃ³n para seguridad multihilo
    private static final ThreadLocal<Integer> lastStatusHttp = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> lastFallback = ThreadLocal.withInitial(() -> true);
    private static final ThreadLocal<String> lastPayloadHash = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<String> lastFuenteDato = ThreadLocal.withInitial(() -> "FALLBACK");

    public static void limpiarThreadLocal() {
        lastStatusHttp.remove();
        lastFallback.remove();
        lastPayloadHash.remove();
        lastFuenteDato.remove();
    }

    public BigDecimal obtenerTipoCambioSimulado() {
        new FuenteEventoService().registrarSimulado("FALLBACK_INTERNO", "TIPO_CAMBIO", "3.75", "Valor fijo de contingencia academica");
        return new BigDecimal("3.75");
    }

    private static final com.importease.proyecto.config.CircuitBreaker cb = new com.importease.proyecto.config.CircuitBreaker("BCRP_API_CB", 10, 0.40, 60000);

    private BigDecimal[] obtenerUltimoTipoCambioDeDB() {
        String sql = "SELECT compra, venta FROM tipo_cambio_diario ORDER BY fecha DESC LIMIT 1";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new BigDecimal[]{rs.getBigDecimal("compra"), rs.getBigDecimal("venta")};
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener el ultimo tipo de cambio de la BD: " + e.getMessage());
        }
        return null;
    }

    public BigDecimal[] obtenerTipoCambioBCRP() {
        BigDecimal compra = new BigDecimal("3.74");
        BigDecimal venta = new BigDecimal("3.75");
        lastStatusHttp.set(0);
        lastFallback.set(true);
        lastPayloadHash.set(null);
        lastFuenteDato.set("FALLBACK");

        if (!cb.allowRequest()) {
            LoggerUtil.warn("CircuitBreaker [BCRP_API_CB] abierto: se omite llamada a BCRP API. Usando cache local.");
            new FuenteEventoService().registrarFallback("BCRP_API", "TIPO_CAMBIO", BCRP_SERIES, "CircuitBreaker [BCRP_API_CB] activo");
            
            BigDecimal[] cached = obtenerUltimoTipoCambioDeDB();
            if (cached != null) {
                lastFallback.set(false);
                lastFuenteDato.set("CACHE");
                return cached;
            }
            return new BigDecimal[]{compra, venta};
        }

        long start = System.currentTimeMillis();
        try {
            URL url = URI.create(BCRP_URL).toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json,text/plain,*/*");
            con.setRequestProperty("Accept-Language", "es-PE,es;q=0.9,en;q=0.8");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            int responseCode = con.getResponseCode();
            lastStatusHttp.set(responseCode);
            if (responseCode == 200) {
                lastFallback.set(false);
                lastFuenteDato.set("OFICIAL_API");

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                }
                String payload = sanitizeJson(response.toString());
                lastPayloadHash.set(FuenteEventoService.hash(payload));

                try {
                    JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                    JsonArray periods = json.getAsJsonArray("periods");
                    if (periods != null && periods.size() > 0) {
                        JsonObject lastPeriod = periods.get(periods.size() - 1).getAsJsonObject();
                        JsonArray values = lastPeriod.getAsJsonArray("values");
                        if (values != null && values.size() >= 2) {
                            compra = new BigDecimal(values.get(0).getAsString());
                            venta = new BigDecimal(values.get(1).getAsString());
                        } else if (values != null && values.size() == 1) {
                            venta = new BigDecimal(values.get(0).getAsString());
                            compra = venta.subtract(new BigDecimal("0.006"));
                        }
                    }
                } catch (Exception parseError) {
                    BigDecimal[] parsed = parseLastBcrpValues(payload);
                    if (parsed == null) throw parseError;
                    compra = parsed[0];
                    venta = parsed[1];
                }
                cb.recordSuccess();
                new FuenteEventoService().registrarOk("BCRP_API", "TIPO_CAMBIO", BCRP_SERIES, BCRP_URL, "GET", responseCode, payload, System.currentTimeMillis() - start);
            } else {
                cb.recordFailure();
                new FuenteEventoService().registrarError("BCRP_API", "TIPO_CAMBIO", BCRP_SERIES, BCRP_URL, "GET", responseCode, "BCRP retorno codigo HTTP " + responseCode, System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            cb.recordFailure();
            new FuenteEventoService().registrarFallback("BCRP_API", "TIPO_CAMBIO", BCRP_SERIES, e.getMessage());
        }
        return new BigDecimal[]{compra, venta};
    }

    private void registrarFallo(Exception e) {
        fallosConsecutivos++;
        lastFallback.set(true);
        lastFuenteDato.set("FALLBACK");
        if (fallosConsecutivos >= 3) {
            tiempoCircuitoAbierto = System.currentTimeMillis();
            LoggerUtil.error("BCRP API fallo 3 veces consecutivas. Circuit breaker abierto por 1 hora.", e);
        } else {
            LoggerUtil.warn("Error consultando BCRP API (" + e.getMessage() + "), intento fallido #" + fallosConsecutivos);
        }
    }

    public BigDecimal obtenerTipoCambio() {
        LocalDate hoy = LocalDate.now();

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT venta FROM tipo_cambio_diario WHERE fecha = ?")) {
            ps.setDate(1, Date.valueOf(hoy));
            ResultSet rs = ps.executeQuery();
            if (rs != null && rs.next()) {
                lastFallback.set(false);
                lastFuenteDato.set("CACHE");
                new FuenteEventoService().registrarCache("BCRP_API", "TIPO_CAMBIO", hoy.toString());
                return rs.getBigDecimal("venta");
            }
        } catch (Exception e) {
            LoggerUtil.error("Error al consultar tipo_cambio_diario en BD", e);
        }

        BigDecimal[] tc = obtenerTipoCambioBCRP();
        BigDecimal compra = tc[0];
        BigDecimal venta = tc[1];

        try (Connection con = ConexionDB.obtenerConexion()) {
            guardarTipoCambio(con, hoy, compra, venta);
        } catch (Exception e) {
            LoggerUtil.error("Error al guardar tipo de cambio en BD", e);
        }

        return venta;
    }

    public Map<String, Object> obtenerTipoCambioDetalle() {
        BigDecimal venta = obtenerTipoCambio();
        LocalDate hoy = LocalDate.now();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tipoCambio", venta);
        data.put("fecha", hoy.toString());
        data.put("fuente", lastFallback.get() ? "Fallback interno" : "BCRP");
        data.put("source", lastFallback.get() ? "FALLBACK_INTERNO" : "BCRP_API");
        data.put("sourceType", lastFuenteDato.get());
        data.put("confidence", DataConfidenceService.confidenceFor(lastFuenteDato.get()));
        data.put("estado", lastFallback.get() ? "FALLBACK" : lastFuenteDato.get());
        data.put("updatedAt", Instant.now().toString());

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tipo_cambio_diario WHERE fecha = ?")) {
            ps.setDate(1, Date.valueOf(hoy));
            ResultSet rs = ps.executeQuery();
            if (rs != null && rs.next()) {
                data.put("compra", rs.getBigDecimal("compra"));
                data.put("tipoCambio", rs.getBigDecimal("venta"));
                data.put("fuente", rs.getString("fuente"));
                if (hasColumn(rs, "fuente_url")) data.put("fuenteUrl", rs.getString("fuente_url"));
                if (hasColumn(rs, "status_http")) data.put("statusHttp", rs.getObject("status_http"));
                if (hasColumn(rs, "es_fallback")) {
                    boolean fallback = rs.getBoolean("es_fallback");
                    data.put("estado", fallback ? "FALLBACK" : "OK");
                    data.put("sourceType", fallback ? "FALLBACK" : "OFICIAL_API");
                    data.put("source", fallback ? "FALLBACK_INTERNO" : "BCRP_API");
                    data.put("confidence", DataConfidenceService.confidenceFor(fallback ? "FALLBACK" : "OFICIAL_API"));
                }
                if (hasColumn(rs, "fecha_actualizacion")) data.put("updatedAt", String.valueOf(rs.getTimestamp("fecha_actualizacion")));
                if (hasColumn(rs, "confianza")) data.put("confidence", rs.getBigDecimal("confianza"));
                if (hasColumn(rs, "fuente_dato")) data.put("sourceType", rs.getString("fuente_dato"));
                if (hasColumn(rs, "metodo_obtencion")) data.put("metodoObtencion", rs.getString("metodo_obtencion"));
                if (hasColumn(rs, "moneda_origen")) data.put("monedaOrigen", rs.getString("moneda_origen"));
                if (hasColumn(rs, "moneda_destino")) data.put("monedaDestino", rs.getString("moneda_destino"));
                if (hasColumn(rs, "promedio")) data.put("promedio", rs.getBigDecimal("promedio"));
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo leer trazabilidad de tipo de cambio: " + e.getMessage());
        }
        return data;
    }

    public Map<String, Object> obtenerTipoCambioDetalleRapido() {
        LocalDate hoy = LocalDate.now();
        Map<String, Object> data = new LinkedHashMap<>();

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM tipo_cambio_diario ORDER BY fecha DESC LIMIT 1"
             );
             ResultSet rs = ps.executeQuery()) {
            if (rs != null && rs.next()) {
                data.put("tipoCambio", rs.getBigDecimal("venta"));
                data.put("fecha", String.valueOf(rs.getDate("fecha")));
                data.put("fuente", hasColumn(rs, "fuente") ? rs.getString("fuente") : "Cache local");
                data.put("source", "BCRP_API");
                data.put("sourceType", "CACHE");
                data.put("confidence", DataConfidenceService.confidenceFor("CACHE"));
                data.put("estado", "CACHE");
                if (hasColumn(rs, "compra")) data.put("compra", rs.getBigDecimal("compra"));
                if (hasColumn(rs, "fecha_actualizacion")) data.put("updatedAt", String.valueOf(rs.getTimestamp("fecha_actualizacion")));
                return data;
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo leer tipo de cambio rapido desde cache: " + e.getMessage());
        }

        data.put("tipoCambio", new BigDecimal("3.75"));
        data.put("fecha", hoy.toString());
        data.put("fuente", "Fallback interno");
        data.put("source", "FALLBACK_INTERNO");
        data.put("sourceType", "FALLBACK");
        data.put("confidence", DataConfidenceService.confidenceFor("FALLBACK"));
        data.put("estado", "FALLBACK");
        data.put("updatedAt", Instant.now().toString());
        return data;
    }

    private void guardarTipoCambio(Connection con, LocalDate fecha, BigDecimal compra, BigDecimal venta) throws Exception {
        BigDecimal promedio = compra.add(venta).divide(new BigDecimal("2"), 4, java.math.RoundingMode.HALF_UP);
        String sqlV31 = "INSERT INTO tipo_cambio_diario (fecha, moneda_origen, moneda_destino, compra, venta, promedio, fuente, fuente_url, metodo_obtencion, status_http, es_fallback, raw_response_hash, fecha_actualizacion, fuente_dato, confianza) " +
                "VALUES (?, 'USD', 'PEN', ?, ?, ?, 'BCRP', ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?) " +
                "ON DUPLICATE KEY UPDATE compra = VALUES(compra), venta = VALUES(venta), promedio = VALUES(promedio), fuente_url = VALUES(fuente_url), metodo_obtencion = VALUES(metodo_obtencion), status_http = VALUES(status_http), es_fallback = VALUES(es_fallback), raw_response_hash = VALUES(raw_response_hash), fecha_actualizacion = CURRENT_TIMESTAMP, fuente_dato = VALUES(fuente_dato), confianza = VALUES(confianza)";
        try (PreparedStatement ps = con.prepareStatement(sqlV31)) {
            ps.setDate(1, Date.valueOf(fecha));
            ps.setBigDecimal(2, compra);
            ps.setBigDecimal(3, venta);
            ps.setBigDecimal(4, promedio);
            ps.setString(5, BCRP_URL);
            ps.setString(6, lastFallback.get() ? "FALLBACK" : "API_OFICIAL");
            if (lastStatusHttp.get() > 0) ps.setInt(7, lastStatusHttp.get()); else ps.setNull(7, java.sql.Types.INTEGER);
            ps.setBoolean(8, lastFallback.get());
            ps.setString(9, lastPayloadHash.get());
            ps.setString(10, lastFuenteDato.get());
            ps.setBigDecimal(11, BigDecimal.valueOf(DataConfidenceService.confidenceFor(lastFuenteDato.get())));
            ps.executeUpdate();
        } catch (Exception e) {
            String legacySql = "INSERT INTO tipo_cambio_diario (fecha, compra, venta, fuente) VALUES (?, ?, ?, 'BCRP') " +
                    "ON DUPLICATE KEY UPDATE compra = VALUES(compra), venta = VALUES(venta)";
            try (PreparedStatement ps = con.prepareStatement(legacySql)) {
                ps.setDate(1, Date.valueOf(fecha));
                ps.setBigDecimal(2, compra);
                ps.setBigDecimal(3, venta);
                ps.executeUpdate();
            }
        }
    }

    private boolean hasColumn(ResultSet rs, String name) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (name.equalsIgnoreCase(meta.getColumnLabel(i))) return true;
            }
        } catch (Exception e) { LoggerUtil.error("Error checking column metadata in ResultSet", e); }
        return false;
    }

    private String sanitizeJson(String value) {
        if (value == null) return "{}";
        String trimmed = value.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private BigDecimal[] parseLastBcrpValues(String payload) {
        if (payload == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"values\"\\s*:\\s*\\[\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?\\s*,\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?\\s*\\]")
                .matcher(payload);
        BigDecimal compra = null;
        BigDecimal venta = null;
        while (matcher.find()) {
            compra = new BigDecimal(matcher.group(1));
            venta = new BigDecimal(matcher.group(2));
        }
        return compra != null && venta != null ? new BigDecimal[]{compra, venta} : null;
    }
}


