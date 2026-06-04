package com.importease.proyecto.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Adaptador de APIs externas permitidas para ImportEase.
 * En esta versiÃ³n el alcance se limita a datos de soporte aduanero:
 * tipo de cambio referencial y consulta RUC. No incluye tracking, pagos ni marketplaces.
 */
public class ExternalApiService {

    private String getApiToken() {
        // 1) System property APIS_NET_PE_TOKEN
        String token = System.getProperty("APIS_NET_PE_TOKEN");
        // 2) Environment variable APIS_NET_PE_TOKEN
        if (token == null || token.isBlank()) {
            token = System.getenv("APIS_NET_PE_TOKEN");
        }
        // 3) System property PERU_API_TOKEN
        if (token == null || token.isBlank()) {
            token = System.getProperty("PERU_API_TOKEN");
        }
        // 4) Environment variable PERU_API_TOKEN
        if (token == null || token.isBlank()) {
            token = System.getenv("PERU_API_TOKEN");
        }
        // 5) config.properties: peru.api.token
        if (token == null || token.isBlank()) {
            token = loadTokenFromProperties();
        }
        if (token == null || token.isBlank() || token.contains("${")) {
            return null;
        }
        return token.trim();
    }

    private String loadTokenFromProperties() {
        // Try config.properties first
        try (java.io.InputStream is = ExternalApiService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                java.util.Properties props = new java.util.Properties();
                props.load(is);
                String val = props.getProperty("peru.api.token");
                if (val != null && !val.isBlank() && !val.contains("${")) {
                    return val.trim();
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo leer config.properties para token: " + e.getMessage());
        }
        // Fallback to config-local.properties (gitignored, for local dev)
        try (java.io.InputStream is = ExternalApiService.class.getClassLoader().getResourceAsStream("config-local.properties")) {
            if (is != null) {
                java.util.Properties props = new java.util.Properties();
                props.load(is);
                String val = props.getProperty("peru.api.token");
                if (val != null && !val.isBlank() && !val.contains("${")) {
                    return val.trim();
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo leer config-local.properties para token: " + e.getMessage());
        }
        return null;
    }

    /**
     * Indica si hay un token de API configurado para consultas externas.
     */
    public boolean isTokenConfigured() {
        String token = getApiToken();
        return token != null && !token.isEmpty();
    }

    /**
     * Consulta el tipo de cambio referencial desde el servicio oficial v2 de APIS.net.pe.
     * Si la llave no estÃ¡ configurada o falla, retorna un valor de contingencia para no bloquear el flujo acadÃ©mico.
     */
    public double obtenerTipoCambioSunat() {
        String token = getApiToken();
        if (token == null || token.isEmpty()) {
            LoggerUtil.warn("Falta configurar la variable de entorno APIS_NET_PE_TOKEN. Se usa fallback referencial 3.75.");
            return 3.75;
        }
        try {
            // Updated to v2 endpoint
            URL url = java.net.URI.create("https://api.apis.net.pe/v2/sunat/tipo-cambio").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    String response = sc.useDelimiter("\\A").hasNext() ? sc.next() : "";
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    return json.has("venta") ? json.get("venta").getAsDouble() : 3.75;
                }
            } else {
                LoggerUtil.warn("APIS.net.pe v2 retorno codigo HTTP " + conn.getResponseCode() + " en tipo de cambio.");
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener tipo de cambio externo v2. Se usa fallback referencial 3.75: " + e.getMessage());
        }
        return 3.75;
    }

    /**
     * Consulta RUC en tiempo real utilizando la versiÃ³n 2 (v2) de APIS.net.pe.
     * @return String[] { RazÃ³n Social, Estado, CondiciÃ³n } o null si falla la consulta.
     */
    public String[] consultarRuc(String ruc) {
        // Validar formato RUC antes de cualquier llamada externa
        if (ruc == null || !ruc.matches("^\\d{11}$")) {
            LoggerUtil.warn("RUC invalido (debe tener 11 digitos): " + ruc);
            return simularConsultaRuc(ruc);
        }
        String token = getApiToken();
        if (token == null || token.isEmpty()) {
            LoggerUtil.warn("Falta configurar token de API para consulta RUC. Se usara simulador local.");
            return simularConsultaRuc(ruc);
        }
        try {
            // Decolecta API endpoint
            URL url = java.net.URI.create("https://api.decolecta.com/v1/sunat/ruc?numero=" + java.net.URLEncoder.encode(ruc, "UTF-8")).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == 200) {
                try (Scanner sc = new Scanner(conn.getInputStream())) {
                    String response = sc.useDelimiter("\\A").hasNext() ? sc.next() : "";
                    JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                    
                    // Decolecta returns razon_social, estado, condicion, es_buen_contribuyente
                    String razonSocial = json.has("razon_social") ? json.get("razon_social").getAsString() : 
                                         (json.has("nombre") ? json.get("nombre").getAsString() : "");
                    String estado = json.has("estado") ? json.get("estado").getAsString() : "ACTIVO";
                    String condicion = json.has("condicion") ? json.get("condicion").getAsString() : "HABIDO";
                    String buenContribuyente = json.has("es_buen_contribuyente") ? String.valueOf(json.get("es_buen_contribuyente").getAsBoolean()) : condicion;
                    
                    return new String[] { razonSocial, estado, condicion, buenContribuyente };
                }
            } else {
                LoggerUtil.warn("APIS.net.pe v2 retorno codigo HTTP " + conn.getResponseCode() + " para RUC: " + ruc);
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo consultar RUC externo v2 para: " + ruc + " - " + e.getMessage());
        }
        return simularConsultaRuc(ruc);
    }
    /**
     * Simulador local para desarrollo: genera datos de RUC ficticios pero realistas
     * cuando no hay token de API configurado. Solo retorna datos si el RUC pasa la
     * validacion local (prefijo + digito verificador).
     */
    private String[] simularConsultaRuc(String ruc) {
        if (!RucValidadorLocal.esRucValido(ruc)) return null;
        String razonSocial;
        String prefijo = ruc.substring(0, 2);
        switch (prefijo) {
            case "20":
                razonSocial = "IMPORTADORA " + ruc.substring(2, 6) + " SOCIEDAD ANONIMA";
                break;
            case "10":
                razonSocial = "COMERCIO " + ruc.substring(2, 6) + " EIRL";
                break;
            case "15":
                razonSocial = "MUNICIPALIDAD DISTRITAL DE LA LIBERTAD";
                break;
            case "17":
                razonSocial = "EMPRESA EXTRANJERA NO DOMICILIADA";
                break;
            default:
                return null;
        }
        return new String[] { razonSocial, "ACTIVO", "HABIDO" };
    }
}

