package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.service.FuenteMetadataBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class DtaMapper {
    public Map<String, Object> toMap(Importacion imp) {
        Map<String, Object> dta = FuenteMetadataBuilder.buildMetadata(
                "Motor DTA Interno",
                FuenteMetadataBuilder.TYPE_SYSTEM_RULE,
                0.80,
                "DTA estimada con los datos del sistema; no reemplaza liquidacion oficial."
        );
        BigDecimal baseCifPen = imp.getValorCifBD().multiply(imp.getTipoCambioBD()).setScale(2, RoundingMode.HALF_UP);
        dta.put("baseCifUsd", imp.getValorCifBD());
        dta.put("tipoCambio", imp.getTipoCambioBD());
        dta.put("baseCifPen", baseCifPen);
        dta.put("adValorem", imp.getMontoAdValoremBD());
        dta.put("isc", imp.getMontoIscBD());
        dta.put("igv", imp.getMontoIgbBD());
        dta.put("ipm", imp.getMontoIpmBD());
        dta.put("percepcion", imp.getMontoPercepcionBD());
        dta.put("total", imp.getTotalImpuestosBD());
        dta.put("estado", "Referencial");
        dta.put("mensaje", "DTA estimada con los datos del sistema; no reemplaza liquidacion oficial.");
        return dta;
    }
}
