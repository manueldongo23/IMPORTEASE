package com.importease.proyecto.service;

import com.importease.proyecto.model.Importacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlazoCriticoServicio {

    public static List<Map<String, Object>> calcularPlazos(Connection con, Importacion imp, String regimenCodigo, String modalidadCodigo) {
        List<Map<String, Object>> plazos = new ArrayList<>();
        LocalDateTime now = ZonedDateTime.now(ZoneId.of("America/Lima")).toLocalDateTime();
        LocalDateTime base = imp.getFechaCreacion() != null ? imp.getFechaCreacion().toLocalDateTime() : now;

        // 1. Intentar cargar fechas reales de manifiesto y BL
        LocalDateTime fechaEmbarque = null;
        LocalDateTime fechaLlegada = null;
        LocalDateTime fechaTerminoDescarga = null;
        LocalDateTime fechaNumeracion = null;
        String viaTransporte = "MARITIMA";

        String query = "SELECT m.via_transporte, m.fecha_llegada, m.fecha_termino_descarga, d.fecha_embarque " +
                       "FROM manifiestos_carga m " +
                       "LEFT JOIN documentos_transporte d ON m.id = d.manifiesto_id " +
                       "WHERE m.operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String via = rs.getString("via_transporte");
                    if (via != null) viaTransporte = via.toUpperCase();
                    Timestamp tsEmb = rs.getTimestamp("fecha_embarque");
                    if (tsEmb != null) fechaEmbarque = tsEmb.toLocalDateTime();
                    Timestamp tsLleg = rs.getTimestamp("fecha_llegada");
                    if (tsLleg != null) fechaLlegada = tsLleg.toLocalDateTime();
                    Timestamp tsDesc = rs.getTimestamp("fecha_termino_descarga");
                    if (tsDesc != null) fechaTerminoDescarga = tsDesc.toLocalDateTime();
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error consultando manifiesto en PlazoCriticoServicio: " + e.getMessage());
        }

        String queryDam = "SELECT fecha_numeracion FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(queryDam)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp tsNum = rs.getTimestamp("fecha_numeracion");
                    if (tsNum != null) fechaNumeracion = tsNum.toLocalDateTime();
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error consultando DAM en PlazoCriticoServicio: " + e.getMessage());
        }

        // Cargar reglas de plazo desde BD si existen
        Map<String, Object> reglaPlazo = cargarReglaPlazo(con, regimenCodigo);

        if ("10".equals(regimenCodigo)) {
            int plazoDias = reglaPlazo.containsKey("plazo_dias") ? ((Number) reglaPlazo.get("plazo_dias")).intValue() : 15;
            String fuente = reglaPlazo.containsKey("fuente_referencia") ? (String) reglaPlazo.get("fuente_referencia") : "DESPA-PG.01 / Ley General de Aduanas";
            String mensaje = reglaPlazo.containsKey("mensaje") ? (String) reglaPlazo.get("mensaje") : "La mercancÃ­a debe ser destinada dentro de " + plazoDias + " dÃ­as calendario desde el tÃ©rmino de la descarga para evitar abandono legal.";
            if (fechaTerminoDescarga != null) {
                LocalDateTime limiteDestinacion = fechaTerminoDescarga.plusDays(plazoDias);
                plazos.add(buildPlazo("DESTINACION_DIFERIDA_" + plazoDias + "D", "Plazo para destinaciÃ³n aduanera", regimenCodigo, "TÃ‰RMINO_DESCARGA REAL", fechaTerminoDescarga, limiteDestinacion, fuente, FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, mensaje));
            } else {
                plazos.add(buildPlazoPendiente("DESTINACION_DIFERIDA_" + plazoDias + "D", "Plazo para destinaciÃ³n aduanera", regimenCodigo, "TÃ‰RMINO_DESCARGA REAL", "Falta registrar fecha de tÃ©rmino de descarga real en el manifiesto."));
            }
        } else if ("TRANSBORDO".equals(regimenCodigo)) {
            int plazoDias = reglaPlazo.containsKey("plazo_dias") ? ((Number) reglaPlazo.get("plazo_dias")).intValue() : 30;
            String fuente = reglaPlazo.containsKey("fuente_referencia") ? (String) reglaPlazo.get("fuente_referencia") : "DESPA-PG.11";
            String mensaje = reglaPlazo.containsKey("mensaje") ? (String) reglaPlazo.get("mensaje") : "El transbordo debe completarse y regularizarse en un plazo mÃ¡ximo de " + plazoDias + " dÃ­as calendario desde la numeraciÃ³n.";
            if (fechaNumeracion != null) {
                LocalDateTime limiteTransbordo = fechaNumeracion.plusDays(plazoDias);
                plazos.add(buildPlazo("TRANSBORDO_REGULARIZACION_" + plazoDias + "D", "RegularizaciÃ³n de transbordo", regimenCodigo, "NUMERACIÃ“N DAM REAL", fechaNumeracion, limiteTransbordo, fuente, FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, mensaje));
            } else {
                plazos.add(buildPlazoPendiente("TRANSBORDO_REGULARIZACION_" + plazoDias + "D", "RegularizaciÃ³n de transbordo", regimenCodigo, "NUMERACIÃ“N DAM REAL", "Falta numerar la PRE-DAM/DAM para iniciar el control de regularizaciÃ³n."));
            }
        } else if ("36".equals(regimenCodigo)) {
            int plazoMeses = reglaPlazo.containsKey("plazo_meses") ? ((Number) reglaPlazo.get("plazo_meses")).intValue() : 12;
            String fuente = reglaPlazo.containsKey("fuente_referencia") ? (String) reglaPlazo.get("fuente_referencia") : "DESPA-PG.26";
            String mensaje = reglaPlazo.containsKey("mensaje") ? (String) reglaPlazo.get("mensaje") : "La reimportaciÃ³n sin pago de tributos debe realizarse dentro de los " + plazoMeses + " meses contados desde el tÃ©rmino del embarque precedente.";
            if (fechaEmbarque != null) {
                LocalDateTime limiteReimportacion = fechaEmbarque.plusMonths(plazoMeses);
                plazos.add(buildPlazo("REIMPORTACION_" + plazoMeses + "M", "Plazo mÃ¡ximo para reimportar", regimenCodigo, "EMBARQUE PRECEDENTE REAL", fechaEmbarque, limiteReimportacion, fuente, FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, mensaje));
            } else {
                plazos.add(buildPlazoPendiente("REIMPORTACION_" + plazoMeses + "M", "Plazo mÃ¡ximo para reimportar", regimenCodigo, "EMBARQUE PRECEDENTE REAL", "Falta ingresar la fecha del embarque precedente en la pestaÃ±a Especiales para calcular el plazo."));
            }
        } else if ("ADM_TEMP".equals(regimenCodigo)) {
            int plazoDias = reglaPlazo.containsKey("plazo_dias") ? ((Number) reglaPlazo.get("plazo_dias")).intValue() : 540;
            String fuente = reglaPlazo.containsKey("fuente_referencia") ? (String) reglaPlazo.get("fuente_referencia") : "DESPA-PG.04";
            String mensaje = reglaPlazo.containsKey("mensaje") ? (String) reglaPlazo.get("mensaje") : "La mercancÃ­a bajo admisiÃ³n temporal debe ser reexportada o nacionalizada dentro de los " + plazoDias + " dÃ­as desde el levante.";
            LocalDateTime levante = fechaNumeracion != null ? fechaNumeracion.plusDays(3) : base.plusDays(4);
            LocalDateTime limiteReexportacion = levante.plusDays(plazoDias);
            plazos.add(buildPlazo("ADM_TEMP_REEXPORTACION_" + plazoDias + "D", "Plazo para reexportaciÃ³n o nacionalizaciÃ³n", regimenCodigo, "FECHA_LEVANTE", levante, limiteReexportacion, fuente, FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, mensaje));
        } else if ("80".equals(regimenCodigo)) {
            String fuente = reglaPlazo.containsKey("fuente_referencia") ? (String) reglaPlazo.get("fuente_referencia") : "DESPA-PG.08";
            if ("TERRESTRE".equals(viaTransporte)) {
                int plazoDias = reglaPlazo.containsKey("plazo_dias") ? ((Number) reglaPlazo.get("plazo_dias")).intValue() : 10;
                String mensaje = reglaPlazo.containsKey("mensaje") ? (String) reglaPlazo.get("mensaje") : "El trÃ¡nsito terrestre debe completarse en la aduana de destino dentro de los " + plazoDias + " dÃ­as autorizados por ruta.";
                LocalDateTime levante = fechaNumeracion != null ? fechaNumeracion.plusDays(2) : base.plusDays(2);
                LocalDateTime limiteTransito = levante.plusDays(plazoDias);
                plazos.add(buildPlazo("TRANSITO_TERRESTRE_" + plazoDias + "D", "Plazo autorizado trÃ¡nsito terrestre", regimenCodigo, "FECHA_LEVANTE", levante, limiteTransito, fuente + " (Terrestre)", FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, mensaje));
            } else {
                int plazoDias = reglaPlazo.containsKey("plazo_dias_alterno") ? ((Number) reglaPlazo.get("plazo_dias_alterno")).intValue() : 30;
                String mensaje = reglaPlazo.containsKey("mensaje") ? (String) reglaPlazo.get("mensaje") : "El trÃ¡nsito aduanero general marÃ­timo o aÃ©reo no puede exceder " + plazoDias + " dÃ­as calendario desde el levante.";
                LocalDateTime levante = fechaNumeracion != null ? fechaNumeracion.plusDays(2) : base.plusDays(2);
                LocalDateTime limiteTransito = levante.plusDays(plazoDias);
                plazos.add(buildPlazo("TRANSITO_AEREO_MAR_" + plazoDias + "D", "Plazo autorizado trÃ¡nsito general", regimenCodigo, "FECHA_LEVANTE", levante, limiteTransito, fuente, FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, mensaje));
            }
        }

        return plazos;
    }

    private static Map<String, Object> buildPlazo(String codigo, String label, String regimen, String baseEvent, LocalDateTime baseDate, LocalDateTime deadline, String source, String sourceType, String message) {
        Map<String, Object> plazo = new LinkedHashMap<>();
        plazo.put("code", codigo);
        plazo.put("label", label);
        plazo.put("regimen", regimen);
        plazo.put("baseEvent", baseEvent);
        plazo.put("baseDate", baseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        plazo.put("deadline", deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        plazo.put("isRegistered", true);
        
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(ZonedDateTime.now(ZoneId.of("America/Lima")).toLocalDateTime(), deadline);
        plazo.put("daysRemaining", daysRemaining);

        // QA-025: Proportional severity windows
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(baseDate, deadline);
        long criticalWindow = totalDays > 30 ? (long) Math.ceil(totalDays * 0.10) : 5;
        long warningWindow = totalDays > 30 ? (long) Math.ceil(totalDays * 0.30) : 15;
        
        String status = "OK";
        String riskLevel = "BAJO";
        if (daysRemaining < 0) {
            status = "EXPIRED";
            riskLevel = "CRITICO";
        } else if (daysRemaining <= criticalWindow) {
            status = "CRITICAL";
            riskLevel = "ALTO";
        } else if (daysRemaining <= warningWindow) {
            status = "WARNING";
            riskLevel = "MEDIO";
        }
        
        plazo.put("status", status);
        plazo.put("riskLevel", riskLevel);
        plazo.put("message", message);
        plazo.putAll(FuenteMetadataBuilder.buildMetadata(source, sourceType, 1.0, "Plazo de control operativo real"));
        
        return plazo;
    }

    private static Map<String, Object> buildPlazoPendiente(String codigo, String label, String regimen, String baseEvent, String message) {
        Map<String, Object> plazo = new LinkedHashMap<>();
        plazo.put("code", codigo);
        plazo.put("label", label);
        plazo.put("regimen", regimen);
        plazo.put("baseEvent", baseEvent);
        plazo.put("isRegistered", false);
        plazo.put("status", "PENDIENTE_DATO_REAL");
        plazo.put("riskLevel", "INDETERMINADO");
        plazo.put("message", message);
        plazo.putAll(FuenteMetadataBuilder.buildMetadata("Procedimiento SUNAT", FuenteMetadataBuilder.TYPE_SYSTEM_RULE, 1.0, "Falta registrar datos reales en el manifiesto para calcular plazos."));
        return plazo;
    }

    private static Map<String, Object> cargarReglaPlazo(Connection con, String regimenCodigo) {
        Map<String, Object> regla = new java.util.HashMap<>();
        String sql = "SELECT plazo_dias, norma_fuente, evento_base, modalidad, via " +
                     "FROM reglas_plazo WHERE regimen = ? AND vigente = TRUE LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, regimenCodigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    regla.put("plazo_dias", rs.getInt("plazo_dias"));
                    String fuente = rs.getString("norma_fuente");
                    if (fuente != null) regla.put("fuente_referencia", fuente);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error cargando regla_plazo para regimen " + regimenCodigo + ": " + e.getMessage());
        }
        return regla;
    }
}


