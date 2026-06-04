package com.importease.proyecto.service.aduanas;

public class AduanasProcedureCatalog {
    public String procedimiento(String regimen) {
        if ("36".equals(regimen)) return "DESPA-PG.26";
        if ("TRANSBORDO".equals(regimen)) return "DESPA-PG.11";
        if ("ADM_TEMP".equals(regimen)) return "DESPA-PG.04";
        if ("80".equals(regimen)) return "DESPA-PG.08";
        return "DESPA-PG.01";
    }

    public String plazoTexto(String modalidad) {
        if ("DIFERIDO_ROJO".equals(modalidad)) return "12 meses desde el termino del embarque precedente.";
        if ("M3_DEPOSITO".equals(modalidad) || "M2_TIERRA".equals(modalidad) || "M1_DIRECTO".equals(modalidad)) return "30 dias calendario desde numeracion.";
        if ("TEMPORAL".equals(modalidad)) return "Hasta 18 meses referenciales desde levante.";
        if ("DIFERIDO".equals(modalidad)) return "15 dias calendario desde termino de descarga.";
        return "Preparar antes del arribo como regla general.";
    }
}
