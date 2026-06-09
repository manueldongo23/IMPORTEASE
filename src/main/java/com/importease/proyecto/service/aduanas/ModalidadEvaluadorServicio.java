package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.service.FuenteMetadataBuilder;
import com.importease.proyecto.service.LoggerUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModalidadEvaluadorServicio {

    private final AduanasCatalogoProcedimientos procedureCatalog = new AduanasCatalogoProcedimientos();

    public Map<String, Object> evaluarModalidad(Map<String, Object> body) {
        String regimen = str(body.get("regimenCodigo"));
        if (regimen == null || regimen.isBlank()) regimen = "10";
        BigDecimal fob = new BigDecimal(str(body.get("fob")) != null ? str(body.get("fob")) : "0");
        boolean restringida = bool(body.get("restringida"));
        boolean urgente = bool(body.get("urgente"));

        // QA-027: Verify urgency justifying document
        if (urgente) {
            String justDoc = str(body.get("documentoJustificacionUrgencia"));
            if (justDoc == null || justDoc.isBlank()) {
                LoggerUtil.warn("Operacion marcada como urgente sin documento de justificacion. Se recomienda ANTICIPADO.");
            }
        }

        String codigo = "ANTICIPADO";
        String nombre = "Despacho anticipado";
        String motivo = "Regla general referencial para importacion para el consumo.";
        List<String> excepciones = new ArrayList<>();

        if ("36".equals(regimen)) {
            codigo = "DIFERIDO_ROJO";
            nombre = "Diferido con reconocimiento fisico";
            motivo = "En reimportacion se simula canal rojo obligatorio para comprobar identidad de la mercancia.";
        } else if ("TRANSBORDO".equals(regimen)) {
            codigo = "M3_DEPOSITO";
            nombre = "Transbordo con ingreso a deposito temporal";
            motivo = "Modalidad didactica completa para controlar bultos, pesos, contenedores, precintos y regularizacion.";
        } else if ("ADM_TEMP".equals(regimen)) {
            codigo = "TEMPORAL";
            nombre = "Admision temporal";
            motivo = "Requiere finalidad, ubicacion, garantia y control de permanencia.";
        } else if ("80".equals(regimen)) {
            codigo = "TRANSITO_NACIONAL";
            nombre = "Transito aduanero nacional";
            motivo = "Traslado bajo control aduanero con garantia y ruta declarada.";
        } else {
            if (fob.compareTo(BigDecimal.ZERO) > 0 && fob.compareTo(new BigDecimal("2000")) <= 0) excepciones.add("FOB menor o igual a USD 2000");
            if (restringida) excepciones.add("Mercancia restringida requiere control sectorial");
            if (urgente) excepciones.add("Operacion marcada como urgente");
            if (!excepciones.isEmpty()) {
                codigo = urgente ? "URGENTE" : "DIFERIDO";
                nombre = urgente ? "Despacho urgente" : "Despacho diferido permitido";
                motivo = "Existe una excepcion referencial a la obligatoriedad de anticipado: " + String.join(", ", excepciones) + ".";
                if (urgente) {
                    motivo += " Se recomienda verificar documento de justificacion de urgencia.";
                }
            }
        }

        Map<String, Object> result = FuenteMetadataBuilder.buildMetadata("Regla de Modalidad DESPA-PG.01", FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, 0.90, "RecomendaciÃƒÂ³n basada en reglas SUNAT.");
        result.put("modalidadCodigo", codigo);
        result.put("modalidadNombre", nombre);
        result.put("motivo", motivo);
        result.put("excepciones", excepciones);
        result.put("plazoTexto", procedureCatalog.plazoTexto(codigo));
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
}
