package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public class TransbordoServicio {

    private final AduanasConstructorRespuesta responseBuilder = new AduanasConstructorRespuesta();
    private final AduanasAlertasServicio alertsService = new AduanasAlertasServicio();

    public Map<String, Object> evaluarTransbordo(Map<String, Object> body) {
        String modalidad = str(body.get("modalidad"));
        if (modalidad == null || modalidad.isBlank()) modalidad = "M3_DEPOSITO";
        boolean pesoOk = !bool(body.get("diferenciaPesoMayor2"));
        boolean precintoOk = !bool(body.get("precintoViolado"));
        boolean solicitudesPendientes = bool(body.get("solicitudesPendientes"));

        // QA-029: Cross-check precinto status against contenedores table
        int operacionId = asInt(body.get("operacionId"), 0);
        if (operacionId > 0) {
            String precintoCheck = "SELECT c.estado_precinto FROM contenedores c INNER JOIN documentos_transporte dt ON c.documento_transporte_id = dt.id INNER JOIN manifiestos_carga m ON dt.manifiesto_id = m.id WHERE m.operacion_id = ? AND c.estado_precinto = 'VIOLADO' LIMIT 1";
            try (Connection con = ConexionDB.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(precintoCheck)) {
                ps.setInt(1, operacionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        precintoOk = false;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warn("Error al verificar estado precinto en contenedores: " + e.getMessage());
            }
        }

        boolean regularizable = pesoOk && precintoOk && !solicitudesPendientes;

        Map<String, Object> result = responseBuilder.baseResult();
        result.put("modalidad", modalidad);
        result.put("plazoDias", 30);
        result.put("regularizable", regularizable);
        result.put("controla", List.of("DAM numerada", "documento de transporte", "pesos y bultos", "contenedores y precintos", "aduana de salida"));
        result.put("alertas", alertsService.alertasTransbordo(pesoOk, precintoOk, solicitudesPendientes));
        result.put("mensaje", regularizable ? "Transbordo regularizable en la simulacion." : "Transbordo con observaciones para regularizar.");
        return result;
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean bool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        String s = String.valueOf(value).trim().toLowerCase();
        return "true".equals(s) || "si".equals(s) || "yes".equals(s) || "1".equals(s);
    }

    private int asInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }
}
