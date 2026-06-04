package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Tratados de Libre Comercio (TLC).
 * Prioriza el esquema base de la base local y cae a una variante expandida
 * solo si el esquema actual no coincide.
 */
public class TlcService {

    private static final Map<String, String> PAIS_A_CODIGO = Map.ofEntries(
        Map.entry("CHINA", "CN"), Map.entry("ESTADOS UNIDOS", "US"), Map.entry("USA", "US"),
        Map.entry("EEUU", "US"), Map.entry("JAPON", "JP"), Map.entry("JAPAN", "JP"),
        Map.entry("COREA", "KR"), Map.entry("COREA DEL SUR", "KR"), Map.entry("ALEMANIA", "DE"),
        Map.entry("FRANCIA", "FR"), Map.entry("ITALIA", "IT"), Map.entry("ESPAÃ‘A", "ES"),
        Map.entry("REINO UNIDO", "GB"), Map.entry("UK", "GB"), Map.entry("CANADA", "CA"),
        Map.entry("MEXICO", "MX"), Map.entry("CHILE", "CL"), Map.entry("COLOMBIA", "CO"),
        Map.entry("ECUADOR", "EC"), Map.entry("BOLIVIA", "BO"), Map.entry("BRASIL", "BR"),
        Map.entry("ARGENTINA", "AR"), Map.entry("URUGUAY", "UY"), Map.entry("PARAGUAY", "PY"),
        Map.entry("SINGAPUR", "SG"), Map.entry("AUSTRALIA", "AU"), Map.entry("NUEVA ZELANDA", "NZ"),
        Map.entry("VIETNAM", "VN"), Map.entry("TAILANDIA", "TH"), Map.entry("PANAMA", "PA"),
        Map.entry("COSTA RICA", "CR"), Map.entry("GUATEMALA", "GT"), Map.entry("HONG KONG", "HK"),
        Map.entry("CUBA", "CU"), Map.entry("SUIZA", "CH"), Map.entry("NORUEGA", "NO"),
        Map.entry("PERU", "PE"), Map.entry("TAIWAN", "TW"), Map.entry("INDIA", "IN"),
        Map.entry("INDONESIA", "ID"), Map.entry("MALASIA", "MY")
    );

    public static Map<String, Object> verificarTlc(String paisOrigen) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("paisOrigen", paisOrigen);

        String codigoIso = normalizarPais(paisOrigen);
        resultado.put("codigoIso", codigoIso);

        try (Connection con = ConexionDB.obtenerConexion()) {
            if (verificarTlcEsquemaBase(con, paisOrigen, codigoIso, resultado)) {
                return resultado;
            }
            if (verificarTlcEsquemaExpandido(con, paisOrigen, codigoIso, resultado)) {
                return resultado;
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al consultar TLC: " + e.getMessage());
        }

        resultado.put("tlcVigente", false);
        String fallbackMsg = "No se encontro TLC vigente con " + paisOrigen + ". Se aplica arancel general.";
        String msg = fallbackMsg;
        try {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("messages_es");
            if (bundle != null && bundle.containsKey("tlc.no_encontrado")) {
                msg = java.text.MessageFormat.format(bundle.getString("tlc.no_encontrado"), paisOrigen);
            }
        } catch (Exception e) {
            // fallback
        }
        resultado.put("mensaje", msg);
        resultado.put("sourceType", FuenteMetadataBuilder.TYPE_SYSTEM_RULE);
        resultado.put("confidence", DataConfidenceService.confidenceFor(FuenteMetadataBuilder.TYPE_SYSTEM_RULE));
        return resultado;
    }

    public static List<Map<String, Object>> listarTlcVigentes() {
        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection con = ConexionDB.obtenerConexion()) {
            if (!listarTlcEsquemaBase(con, lista)) {
                listarTlcEsquemaExpandido(con, lista);
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al listar TLCs: " + e.getMessage());
        }
        return lista;
    }

    public static String normalizarPais(String pais) {
        if (pais == null || pais.trim().isEmpty()) return "XX";
        String upper = pais.trim().toUpperCase();
        if (upper.length() == 2) return upper;
        return PAIS_A_CODIGO.getOrDefault(upper, upper.substring(0, Math.min(2, upper.length())));
    }

    private static boolean verificarTlcEsquemaBase(Connection con, String paisOrigen, String codigoIso, Map<String, Object> resultado) {
        String paisUpper = paisOrigen == null ? "" : paisOrigen.toUpperCase().trim();
        String sql = "SELECT pais_codigo, pais_nombre, acuerdo_nombre, reduccion_advalorem, requiere_certificado_origen, fuente_codigo FROM tlc_acuerdos " +
                "WHERE pais_codigo = ? OR UPPER(pais_nombre) = ? OR UPPER(pais_nombre) LIKE ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoIso);
            ps.setString(2, paisUpper);
            ps.setString(3, "%" + paisUpper + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = firstText(rs, "acuerdo_nombre", "pais_nombre");
                    resultado.put("tlcVigente", true);
                    resultado.put("nombreTlc", nombre);
                    resultado.put("tipo", "TLC");
                    resultado.put("fechaVigencia", null);
                    resultado.put("arancelPreferencial", rs.getBigDecimal("reduccion_advalorem"));
                    resultado.put("observaciones", rs.getBoolean("requiere_certificado_origen") ? "Requiere certificado de origen" : "No requiere certificado de origen");
                    resultado.put("urlOficial", null);
                    resultado.put("sourceType", FuenteMetadataBuilder.TYPE_BD_LOCAL);
                    resultado.put("confidence", DataConfidenceService.confidenceFor(FuenteMetadataBuilder.TYPE_BD_LOCAL));
                    resultado.put("mensaje", "TLC vigente: " + nombre + ". Puede aplicar arancel preferencial.");
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static boolean verificarTlcEsquemaExpandido(Connection con, String paisOrigen, String codigoIso, Map<String, Object> resultado) {
        String paisUpper = paisOrigen == null ? "" : paisOrigen.toUpperCase().trim();
        String sql = "SELECT * FROM tlc_acuerdos WHERE (codigo_pais = ? OR FIND_IN_SET(?, paises_miembros) > 0 OR UPPER(paises_miembros) LIKE CONCAT('%', ?, '%'))";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoIso);
            ps.setString(2, codigoIso);
            ps.setString(3, paisUpper);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombre = firstText(rs, "nombre", "acuerdo_nombre", "pais_nombre");
                    resultado.put("tlcVigente", true);
                    resultado.put("nombreTlc", nombre);
                    resultado.put("tipo", firstText(rs, "tipo", "fuente_codigo"));
                    resultado.put("fechaVigencia", firstText(rs, "fecha_vigencia"));
                    resultado.put("arancelPreferencial", firstDecimal(rs, "arancel_preferencial_general", "reduccion_advalorem"));
                    resultado.put("observaciones", firstText(rs, "observaciones"));
                    resultado.put("urlOficial", firstText(rs, "url_oficial"));
                    resultado.put("sourceType", FuenteMetadataBuilder.TYPE_BD_LOCAL);
                    resultado.put("confidence", DataConfidenceService.confidenceFor(FuenteMetadataBuilder.TYPE_BD_LOCAL));
                    resultado.put("mensaje", "TLC vigente: " + nombre + ". Puede aplicar arancel preferencial.");
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static boolean listarTlcEsquemaBase(Connection con, List<Map<String, Object>> lista) {
        String sql = "SELECT pais_codigo, pais_nombre, acuerdo_nombre, reduccion_advalorem, requiere_certificado_origen, fuente_codigo FROM tlc_acuerdos ORDER BY pais_nombre";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> tlc = new LinkedHashMap<>();
                tlc.put("codigoPais", rs.getString("pais_codigo"));
                tlc.put("nombre", firstText(rs, "acuerdo_nombre", "pais_nombre"));
                tlc.put("tipo", "TLC");
                tlc.put("fechaVigencia", null);
                tlc.put("arancelPreferencial", rs.getBigDecimal("reduccion_advalorem"));
                tlc.put("requiereCertificadoOrigen", rs.getBoolean("requiere_certificado_origen"));
                tlc.put("fuenteCodigo", rs.getString("fuente_codigo"));
                lista.add(tlc);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void listarTlcEsquemaExpandido(Connection con, List<Map<String, Object>> lista) throws Exception {
        String sql = "SELECT * FROM tlc_acuerdos ORDER BY 1";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> tlc = new LinkedHashMap<>();
                tlc.put("id", rs.getInt("id"));
                tlc.put("nombre", firstText(rs, "nombre", "acuerdo_nombre", "pais_nombre"));
                tlc.put("codigoPais", firstText(rs, "codigo_pais", "pais_codigo"));
                tlc.put("paisesMiembros", firstText(rs, "paises_miembros"));
                tlc.put("tipo", firstText(rs, "tipo", "fuente_codigo"));
                tlc.put("fechaVigencia", firstText(rs, "fecha_vigencia"));
                tlc.put("arancelPreferencial", firstDecimal(rs, "arancel_preferencial_general", "reduccion_advalorem"));
                tlc.put("observaciones", firstText(rs, "observaciones"));
                lista.add(tlc);
            }
        }
    }

    private static String firstText(ResultSet rs, String... columns) throws Exception {
        for (String column : columns) {
            try {
                String value = rs.getString(column);
                if (value != null && !value.isBlank()) return value;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static java.math.BigDecimal firstDecimal(ResultSet rs, String... columns) throws Exception {
        for (String column : columns) {
            try {
                java.math.BigDecimal value = rs.getBigDecimal(column);
                if (value != null) return value;
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}


