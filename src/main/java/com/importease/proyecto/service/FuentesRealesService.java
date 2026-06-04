package com.importease.proyecto.service;

import com.google.gson.Gson;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.repository.HsCodeDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FuentesRealesService {
    private final TipoCambioService tipoCambioService = new TipoCambioService();
    private final ObservatorioService observatorioService = new ObservatorioService();
    private final TrackingService trackingService = new TrackingService();
    private final FuenteEventoService fuenteEventoService = new FuenteEventoService();
    private final HsCodeDAO hsCodeDAO = new HsCodeDAO();
    private final Gson gson = new Gson();

    public Map<String, Object> estado() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("updatedAt", Instant.now().toString());
        data.put("principio", "Si una fuente falla, se muestra cache/fallback; si un dato es simulado, se etiqueta.");
        data.put("tipoCambio", tipoCambioService.obtenerTipoCambioDetalle());
        data.put("resumen", resumenEventos());
        data.put("fuentes", fuentesCatalogo());
        data.put("ultimosEventos", eventos(8));
        data.put("tracking", trackingResumen());
        return data;
    }

    public Map<String, Object> sincronizarTipoCambio() {
        Map<String, Object> detalle = tipoCambioService.obtenerTipoCambioDetalle();
        detalle.put("mensaje", "Tipo de cambio consultado con trazabilidad BCRP/cache/fallback.");
        return detalle;
    }

    public Map<String, Object> tipoCambio() {
        return tipoCambioService.obtenerTipoCambioDetalle();
    }

    public List<Map<String, Object>> eventos(int limite) {
        int max = Math.max(1, Math.min(limite, 50));
        String sql = "SELECT fuente, tipo_evento, COALESCE(tipo_fuente,'BD_LOCAL') tipo_fuente, COALESCE(entidad_afectada,tipo_evento) entidad_afectada, referencia, url, metodo_http, status_http, resultado, COALESCE(source_type,'BD_LOCAL') source_type, COALESCE(confianza,0) confianza, LEFT(COALESCE(mensaje_error,''), 180) mensaje_error, duracion_ms, fecha_evento FROM fuente_eventos ORDER BY fecha_evento DESC LIMIT ?";
        return query(sql, ps -> ps.setInt(1, max));
    }

    public Map<String, Object> arancel(String hs) {
        String code = normalizarHs(hs);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("hsCode", code);
        data.put("fuente", "SUNAT_ARANCEL");
        data.put("fuenteUrl", "https://www.sunat.gob.pe/orientacionaduanera/nomenclaturaarancelaria/tratamiento.html");
        data.put("metodoObtencion", "BD_LOCAL");
        data.put("updatedAt", Instant.now().toString());

        Map<String, Object> row = first("SELECT * FROM arancel_fuente_sunat WHERE hs_code = ? OR hs_code LIKE ? ORDER BY hs_code DESC LIMIT 1", ps -> {
            ps.setString(1, code);
            ps.setString(2, prefix(code) + "%");
        });
        if (!row.isEmpty()) {
            data.putAll(row);
            data.put("sourceType", value(row.get("sourceType"), "BD_LOCAL"));
            data.put("confidence", asDouble(row.get("confianza"), DataConfidenceService.confidenceFor("BD_LOCAL")));
            fuenteEventoService.registrarCache("SUNAT_ARANCEL", "ARANCEL_HS", code);
            return data;
        }

        HsCode local = hsCodeDAO.obtenerPorCodigo(code);
        if (local != null) {
            data.put("descripcion", local.getDescripcionEs());
            data.put("adValorem", local.getAdValorem());
            data.put("restriccionesTexto", local.isRequiereVuce() ? "Posible mercancia restringida: validar " + value(local.getEntidadVuce(), "VUCE") : "Sin restriccion detectada en matriz local.");
            data.put("regimenAsociado", "Importacion para consumo referencial");
            data.put("sourceType", "BD_LOCAL");
            data.put("confidence", DataConfidenceService.confidenceFor("BD_LOCAL"));
            fuenteEventoService.registrarCache("BD_LOCAL", "ARANCEL_HS", code);
            return data;
        }

        data.put("descripcion", "No encontrado en cache local. Validar en SUNAT/Aduanet.");
        data.put("sourceType", "PENDIENTE_VALIDACION");
        data.put("confidence", DataConfidenceService.confidenceFor("PENDIENTE_VALIDACION"));
        fuenteEventoService.registrarError("SUNAT_ARANCEL", "ARANCEL_HS", code, String.valueOf(data.get("fuenteUrl")), "GET", 0, "HS no encontrado en cache local", 0);
        return data;
    }

    public Map<String, Object> vuce(String hs) {
        String code = normalizarHs(hs);
        List<Map<String, Object>> rows = query("SELECT * FROM vuce_tramites_fuente WHERE hs_code = ? OR hs_code LIKE ? OR entidad IN (SELECT entidad_vuce FROM hs_codes WHERE codigo = ?) ORDER BY confianza DESC LIMIT 8", ps -> {
            ps.setString(1, code);
            ps.setString(2, prefix(code) + "%");
            ps.setString(3, code);
        });

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("hsCode", code);
        data.put("fuente", "VUCE_WEB");
        data.put("fuenteUrl", "https://www.vuce.gob.pe/");
        data.put("metodoObtencion", "BD_LOCAL");
        data.put("tramites", rows);
        data.put("sourceType", rows.isEmpty() ? "PENDIENTE_VALIDACION" : "BD_LOCAL");
        data.put("confidence", rows.isEmpty() ? DataConfidenceService.confidenceFor("PENDIENTE_VALIDACION") : 0.65);
        data.put("nota", rows.isEmpty()
                ? "No hay tramite VUCE cacheado para este HS; validar en VUCE antes de embarcar."
                : "Tramites referenciales basados en matriz local/VUCE. No equivalen a permiso emitido.");
        fuenteEventoService.registrarCache(rows.isEmpty() ? "VUCE_WEB" : "BD_LOCAL", "VUCE_HS", code);
        return data;
    }

    public Map<String, Object> comtrade(String hs) {
        Map<String, Object> data = observatorioService.analizar(hs);
        data.put("metodoObtencion", "API_OFICIAL/CACHE");
        data.put("fuenteUrl", "https://comtradeplus.un.org/");
        return data;
    }

    public Map<String, Object> pam(String hs) {
        Map<String, Object> data = new VucePamService().consultarPAM(hs, null);
        data.put("fuente", "VUCE_PAM");
        data.put("fuenteUrl", "https://www.vuce.gob.pe/pam/");
        data.put("metodoObtencion", "WEB_PUBLIC");
        fuenteEventoService.registrarCache("VUCE_PAM", "PAM_CONSULTA", hs != null ? hs : "GENERAL");
        return data;
    }

    public Map<String, Object> vuceProcedimientos() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fuente", "VUCE_PROCEDIMIENTOS");
        data.put("fuenteUrl", "https://www.vuce.gob.pe/mercancias-restricciones/procedimientos.html");
        data.put("metodoObtencion", "WEB_PUBLIC");
        List<Map<String, String>> procs = new VUCEValidatorService().listarProcedimientos();
        data.put("procedimientos", procs);
        data.put("total", procs.size());
        data.put("sourceType", "OFICIAL_WEB_PUBLIC");
        data.put("confidence", 0.7);
        fuenteEventoService.registrarCache("VUCE_PROCEDIMIENTOS", "PROCEDIMIENTOS", "ALL");
        return data;
    }

    public Map<String, Object> sunatDescargarCatalogo() {
        Map<String, Object> result = new SunatBulkLoaderService().descargarCatalogoEntidades();
        result.put("fuente", "SUNAT_BULK");
        result.put("fuenteUrl", "https://www.sunat.gob.pe/descarga/tablasgenerales/");
        result.put("metodoObtencion", "BULK_DOWNLOAD");
        fuenteEventoService.registrarCache("SUNAT_BULK", "CATALOGO_ENTIDADES", String.valueOf(result.get("entidades_insertadas")));
        return result;
    }

    public Map<String, Object> consultarTracking(Integer usuarioId, String sessionId, Map<String, Object> body, String ip, String userAgent) {
        Map<String, Object> registro = trackingService.registrar(usuarioId, sessionId, body, ip, userAgent);
        Object idObj = ((Map<?, ?>) registro.get("envio")).get("id");
        long id = parseLong(String.valueOf(idObj), -1);
        if (id > 0) {
            return trackingService.sincronizar(id, usuarioId, sessionId, ip, userAgent);
        }
        return registro;
    }

    private Map<String, Object> resumenEventos() {
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("okCache", count("SELECT COUNT(*) FROM fuente_eventos WHERE resultado IN ('OK','CACHE')"));
        resumen.put("errores", count("SELECT COUNT(*) FROM fuente_eventos WHERE resultado IN ('ERROR','TIMEOUT')"));
        resumen.put("fallbacks", count("SELECT COUNT(*) FROM fuente_eventos WHERE resultado = 'FALLBACK'"));
        resumen.put("simulados", count("SELECT COUNT(*) FROM fuente_eventos WHERE resultado = 'SIMULADO'"));
        resumen.put("pendientesCredenciales", count("SELECT COUNT(*) FROM fuente_eventos WHERE COALESCE(source_type,'') = 'PENDIENTE_CREDENCIALES'"));
        return resumen;
    }

    private List<Map<String, Object>> fuentesCatalogo() {
        List<Map<String, Object>> rows = query("SELECT codigo, nombre, tipo, url_base, requiere_credenciales, estado, prioridad FROM catalogo_fuentes ORDER BY prioridad, codigo LIMIT 20", ps -> {});
        if (!rows.isEmpty()) return rows;
        List<Map<String, Object>> fallback = new ArrayList<>();
        fallback.add(source("BCRP_API", "API oficial", "ACTIVA", "https://estadisticas.bcrp.gob.pe/estadisticas/series/api/"));
        fallback.add(source("SUNAT_ARANCEL", "Web oficial referencial", "PENDIENTE", "https://www.sunat.gob.pe/"));
        fallback.add(source("VUCE_WEB", "Web oficial referencial", "PENDIENTE", "https://www.vuce.gob.pe/"));
        fallback.add(source("UN_COMTRADE_API", "API oficial/cache", "PENDIENTE", "https://comtradeplus.un.org/"));
        return fallback;
    }

    private Map<String, Object> trackingResumen() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("proveedores", query("SELECT proveedor, nombre, api_url, developer_url, requiere_credenciales, estado FROM tracking_fuentes ORDER BY proveedor", ps -> {}));
        data.put("nota", "El tracking solo se marca real si responde la API. Sin credenciales queda PENDIENTE_CREDENCIALES.");
        return data;
    }

    private Map<String, Object> source(String codigo, String tipo, String estado, String url) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("codigo", codigo);
        row.put("tipo", tipo);
        row.put("estado", estado);
        row.put("url_base", url);
        return row;
    }

    private long count(String sql) {
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (Exception e) {
            LoggerUtil.error("Error al contar eventos en FuentesRealesService", e);
            return 0;
        }
    }

    private Map<String, Object> first(String sql, SqlBinder binder) {
        List<Map<String, Object>> rows = query(sql, binder);
        return rows.isEmpty() ? new LinkedHashMap<>() : rows.get(0);
    }

    private List<Map<String, Object>> query(String sql, SqlBinder binder) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        row.put(toCamel(meta.getColumnLabel(i)), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("FuentesRealesService query omitida: " + e.getMessage());
        }
        return rows;
    }

    private String normalizarHs(String hs) {
        String digits = hs == null ? "" : hs.replaceAll("[^0-9]", "");
        if (digits.length() >= 10) return digits.substring(0, 10);
        return digits;
    }

    private String prefix(String code) {
        if (code == null) return "";
        return code.length() >= 6 ? code.substring(0, 6) : code;
    }

    private String toCamel(String value) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : value.toCharArray()) {
            if (c == '_') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(c));
                upper = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private String value(Object value, String fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        return String.valueOf(value);
    }

    private double asDouble(Object value, double fallback) {
        if (value instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value); } catch (Exception e) { return fallback; }
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws Exception;
    }
}


