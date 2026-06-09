package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.service.FuenteMetadataBuilder;
import com.importease.proyecto.service.NormalizadorUtil;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegimenEvaluadorServicio {

    public Map<String, Object> buildRegimenInputFromImportacion(Importacion imp) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("destino", "PERU");
        input.put("quedaEnPeru", true);
        input.put("fob", imp.getValorFob());
        input.put("hsCode", imp.getHsCode());
        input.put("restringida", looksRestricted(imp.getHsCode()));
        return input;
    }

    public Map<String, Object> evaluarRegimen(Map<String, Object> body) {
        String destino = upper(str(body.get("destino")));
        boolean quedaEnPeru = bool(body.get("quedaEnPeru")) || "PERU".equals(destino) || "CONSUMO".equals(destino);
        boolean soloTransita = bool(body.get("soloTransita")) || "TRANSITO".equals(destino);
        boolean temporal = bool(body.get("temporal")) || "TEMPORAL".equals(destino);
        boolean reimporta = bool(body.get("reimportacion")) || "REIMPORTACION".equals(destino);
        boolean transbordo = bool(body.get("transbordo")) || "TRANSBORDO".equals(destino);

        // QA-026: Flag contradiction detection
        if (quedaEnPeru && reimporta) {
            Map<String, Object> errorResult = FuenteMetadataBuilder.buildMetadata("Validacion de regimen", FuenteMetadataBuilder.TYPE_SYSTEM_RULE, 1.0, "Contradiccion de banderas");
            errorResult.put("error", true);
            errorResult.put("mensaje", "Contradiccion: la mercancia no puede quedarse en Peru y ser reimportacion al mismo tiempo.");
            return errorResult;
        }

        String codigo = "10";
        String nombre = "Importacion para el consumo";
        String motivo = quedaEnPeru ? "Se determinÃƒÂ³ rÃƒÂ©gimen 10 porque: la mercancÃƒÂ­a se queda en PerÃƒÂº." : "La mercancia ingresara al territorio aduanero para quedarse en Peru, con pago o garantia de tributos y formalidades.";
        String siguiente = "Validar modalidad de despacho, documentos sustentatorios y riesgos.";

        if (reimporta) {
            codigo = "36";
            nombre = "Reimportacion en el mismo estado";
            motivo = "La mercancia fue exportada antes desde Peru y retorna sin transformacion, reparacion ni elaboracion.";
            siguiente = "Verificar declaracion de exportacion precedente, plazo de 12 meses y canal rojo obligatorio.";
        } else if (transbordo) {
            codigo = "TRANSBORDO";
            nombre = "Transbordo";
            motivo = "La mercancia cambia de medio de transporte para salir del territorio bajo control aduanero.";
            siguiente = "Elegir modalidad de transbordo y controlar el plazo de 30 dias.";
        } else if (temporal) {
            codigo = "ADM_TEMP";
            nombre = "Admision temporal para reexportacion en el mismo estado";
            motivo = "La mercancia entrara temporalmente, con fin determinado y reexportacion prevista sin modificacion sustancial.";
            siguiente = "Calcular garantia, ubicar la mercancia y controlar el plazo del regimen.";
        } else if (soloTransita) {
            codigo = "80";
            nombre = "Transito aduanero";
            motivo = "La mercancia solo pasara por Peru o se trasladara bajo control de una aduana a otra.";
            siguiente = "Registrar manifiesto, ruta, garantia y aduana de destino o salida.";
        } else if (quedaEnPeru) {
            codigo = "10";
        }

        Map<String, Object> result = FuenteMetadataBuilder.buildMetadata("Reglas internas basadas en " + ("36".equals(codigo) ? "DESPA-PG.26" : ("ADM_TEMP".equals(codigo) ? "DESPA-PG.04" : ("TRANSBORDO".equals(codigo) ? "DESPA-PG.11" : ("80".equals(codigo) ? "DESPA-PG.08" : "DESPA-PG.01")))), FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, 0.95, "Regla parametrizada, requiere validaciÃƒÂ³n del agente.");
        result.put("regimenCodigo", codigo);
        result.put("regimenNombre", nombre);
        result.put("motivo", motivo);
        result.put("siguientePaso", siguiente);
        result.put("etiqueta", "Procedimiento Oficial");
        result.put("preguntasInterpretadas", List.of(
            item("La mercancia se quedara en Peru", quedaEnPeru),
            item("Solo transitara por Peru", soloTransita),
            item("Entrara temporalmente", temporal),
            item("Fue exportada antes desde Peru", reimporta),
            item("Cambiara de medio para salir", transbordo)
        ));
        return result;
    }

    private static Map<String, Object> item(String texto, boolean valor) {
        return Map.of("pregunta", texto, "respuesta", valor);
    }

    private static boolean looksRestricted(String hsCode) {
        return NormalizadorUtil.looksRestricted(hsCode);
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private boolean bool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        String s = String.valueOf(value).trim().toLowerCase();
        return "true".equals(s) || "si".equals(s) || "yes".equals(s) || "1".equals(s);
    }
}
