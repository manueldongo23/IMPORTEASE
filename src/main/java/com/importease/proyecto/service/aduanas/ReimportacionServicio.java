package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReimportacionServicio {

    private final AduanasConstructorRespuesta responseBuilder = new AduanasConstructorRespuesta();

    public Map<String, Object> evaluarReimportacion(Map<String, Object> body) {
        boolean exportada = bool(body.get("exportadaDesdePeru"));
        boolean regularizada = bool(body.get("exportacionRegularizada"));
        boolean transformada = bool(body.get("transformada"));
        boolean beneficio = bool(body.get("beneficioExportacion"));
        int meses = asInt(body.get("mesesDesdeEmbarque"), 0);
        boolean enPlazo = meses > 0 && meses <= 12;
        boolean procede = exportada && regularizada && enPlazo && !transformada;

        // QA-028: Verify DECLARACION_EXPORTACION document exists
        if (procede) {
            int operacionId = asInt(body.get("operacionId"), 0);
            if (operacionId > 0) {
                String checkDoc = "SELECT 1 FROM documentos_importacion WHERE importacion_id = ? AND tipo_documento = 'DECLARACION_EXPORTACION' AND soft_delete = FALSE LIMIT 1";
                try (Connection con = ConexionDB.obtenerConexion();
                     PreparedStatement ps = con.prepareStatement(checkDoc)) {
                    ps.setInt(1, operacionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            procede = false;
                        }
                    }
                } catch (Exception e) {
                    LoggerUtil.warn("Error al verificar DECLARACION_EXPORTACION: " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = responseBuilder.baseResult();
        result.put("procede", procede);
        result.put("canalProbable", "ROJO");
        result.put("canalEsOficial", false);
        result.put("motivoCanal", "Canal rojo obligatorio en esta simulacion para comprobar que la mercancia es la misma.");
        result.put("documentoPrecedente", "Declaracion de exportacion definitiva regularizada");
        result.put("debeDevolverBeneficio", beneficio);
        result.put("diagnostico", procede ? "Procede reimportacion referencial." : "No procede hasta cerrar los requisitos faltantes.");
        result.put("faltantes", faltantesReimportacion(exportada, regularizada, enPlazo, transformada));
        return result;
    }

    private List<String> faltantesReimportacion(boolean exportada, boolean regularizada, boolean enPlazo, boolean transformada) {
        List<String> faltantes = new ArrayList<>();
        if (!exportada) faltantes.add("Confirmar exportacion definitiva previa desde Peru.");
        if (!regularizada) faltantes.add("Regularizar declaracion de exportacion precedente.");
        if (!enPlazo) faltantes.add("Validar que no pasaron mas de 12 meses.");
        if (transformada) faltantes.add("No debe haber transformacion, reparacion ni elaboracion en el extranjero.");
        return faltantes;
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
