package com.importease.proyecto.service;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SunatDamScraperServicio {

    private static final String DAM_CONSULT_URL = "https://www.sunat.gob.pe/ol-ti-itdamconsul/damex/consultaIntegra";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Semaphore SEMAPHORE = new Semaphore(2);
    private static final Gson GSON = new Gson();

    /**
     * Consulta el estado de una DAM en SUNAT.
     * @param damNumber NÃºmero de DAM completo (ej. "118-2025-10-123456")
     * @return Mapa con estado, detalle, fechaNumeracion, aduana, observaciones
     */
    public Map<String, Object> consultarEstadoDam(String damNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("damNumber", damNumber);
        result.put("success", false);
        result.put("origen", "SUNAT");

        if (damNumber == null || damNumber.trim().isEmpty()) {
            result.put("error", "NÃºmero de DAM no proporcionado");
            result.put("estado", "NO_DISPONIBLE");
            return result;
        }

        String[] partes = damNumber.trim().split("-");
        String aduana = partes.length > 0 ? partes[0] : "";
        String ejercicio = partes.length > 1 ? partes[1] : "";
        String regimen = partes.length > 2 ? partes[2] : "";
        String numero = partes.length > 3 ? partes[3] : partes.length > 2 ? partes[2] : "";

        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                LoggerUtil.warn("Timeout esperando turno para consultar DAM: " + damNumber);
                result.put("estado", "TIMEOUT");
                result.put("error", "El servicio SUNAT estÃ¡ congestionado. Intente nuevamente.");
                return result;
            }

            sleepWithJitter();
            Document doc = Jsoup.connect(DAM_CONSULT_URL)
                    .data("accion", "consulta")
                    .data("num_dam", numero)
                    .data("num_aduana", aduana)
                    .data("ejercicio", ejercicio)
                    .data("regimen", regimen)
                    .userAgent(UA)
                    .timeout(15000)
                    .post();

            String html = doc.text();
            result.put("success", true);
            result.put("estado", extraerEstado(html));
            result.put("fechaNumeracion", extraerCampo(html, "Fecha de Numeraci\u00f3n"));
            result.put("aduanafisica", extraerCampo(html, "Aduana"));
            result.put("regimen", extraerCampo(html, "R\u00e9gimen"));
            result.put("detalle", extraerDetalle(html));
            result.put("observaciones", extraerObservaciones(doc));
            result.putAll(FuenteMetadataBuilder.buildMetadata(
                    "SunatDamScraperServicio.consultarEstadoDam",
                    FuenteMetadataBuilder.TYPE_OFFICIAL_API,
                    0.85,
                    "Consulta SUNAT en vivo"
            ));

        } catch (Exception e) {
            LoggerUtil.error("Error consultando DAM " + damNumber + " en SUNAT", e);
            result.put("estado", "ERROR_CONSULTA");
            result.put("error", "No se pudo consultar el estado de la DAM en SUNAT: " + e.getMessage());
            if (e instanceof org.jsoup.HttpStatusException) {
                int statusCode = ((org.jsoup.HttpStatusException) e).getStatusCode();
                result.put("httpStatus", statusCode);
                if (statusCode == 404) {
                    result.put("estado", "NO_ENCONTRADO");
                    result.put("error", "La DAM no fue encontrada en SUNAT");
                }
            }
        } finally {
            if (acquired) SEMAPHORE.release();
        }

        return result;
    }

    private String extraerEstado(String html) {
        if (html == null) return "DESCONOCIDO";
        String upper = html.toUpperCase(java.util.Locale.ROOT);
        if (upper.contains("LEVANTADO"))           return "LEVANTADO";
        if (upper.contains("NUMERADO"))             return "NUMERADO";
        if (upper.contains("EN TRÃMITE") || upper.contains("EN TRAMITE")) return "EN_TRAMITE";
        if (upper.contains("OBSERVADO"))            return "OBSERVADO";
        if (upper.contains("RECTIFICADO"))          return "RECTIFICADO";
        if (upper.contains("ANULADO"))              return "ANULADO";
        if (upper.contains("LIQUIDADO"))            return "LIQUIDADO";
        if (upper.contains("ABANDONO"))             return "ABANDONO";
        if (upper.contains("EMBARCADO") || upper.contains("EMBARQUE")) return "EMBARCADO";
        if (upper.contains("ARCHIVADO"))            return "ARCHIVADO";
        if (upper.contains("RECHAZADO"))            return "RECHAZADO";
        return "EN_PROCESO";
    }

    private String extraerCampo(String html, String campo) {
        if (html == null || campo == null) return "";
        Pattern p = Pattern.compile(
            Pattern.quote(campo) + "[\\s:]*([^\\n\\r]{1,200})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher m = p.matcher(html);
        return m.find() ? m.group(1).trim() : "";
    }

    private String extraerDetalle(String html) {
        if (html == null) return "";
        String estado = extraerEstado(html);
        switch (estado) {
            case "LEVANTADO":   return "DeclaraciÃ³n levantada - mercancÃ­a liberada";
            case "NUMERADO":    return "DeclaraciÃ³n numerada y en proceso de revisiÃ³n";
            case "EN_TRAMITE":  return "DeclaraciÃ³n en trÃ¡mite administrativo";
            case "OBSERVADO":   return "DeclaraciÃ³n observada - requiere subsanaciÃ³n";
            case "RECTIFICADO": return "DeclaraciÃ³n rectificada por el importador o SUNAT";
            case "ANULADO":     return "DeclaraciÃ³n anulada";
            case "LIQUIDADO":   return "DeclaraciÃ³n liquidada - tributos pagados";
            case "ABANDONO":    return "DeclaraciÃ³n en abandono legal";
            case "EMBARCADO":   return "MercancÃ­a embarcada con autorizaciÃ³n";
            case "ARCHIVADO":   return "Expediente archivado";
            case "RECHAZADO":   return "DeclaraciÃ³n rechazada";
            default:            return "DeclaraciÃ³n en proceso de evaluaciÃ³n";
        }
    }

    private java.util.List<String> extraerObservaciones(Document doc) {
        java.util.List<String> obs = new java.util.ArrayList<>();
        try {
            for (Element row : doc.select("table.tablaDatos tr, .resultados tr, .observaciones tr")) {
                String text = row.text().trim();
                if (text.toLowerCase(java.util.Locale.ROOT).contains("observaci")
                        || text.toLowerCase(java.util.Locale.ROOT).contains("reparo")
                        || text.toLowerCase(java.util.Locale.ROOT).contains("subsanaci")) {
                    for (Element td : row.select("td")) {
                        String t = td.text().trim();
                        if (t.length() > 5) obs.add(t);
                    }
                }
            }
        } catch (Exception e) { LoggerUtil.error("Error extracting observations from DAM page", e); }
        return obs;
    }

    private void sleepWithJitter() {
        try {
            long delay = 1000 + (long)(Math.random() * 1500);
            Thread.sleep(delay);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); LoggerUtil.error("Thread interrupted in sleepWithJitter", e); }
    }
}

