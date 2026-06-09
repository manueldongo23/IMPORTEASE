package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.service.FuenteMetadataBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class DtaMapeador {
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

        // Centralized DTA status and calculations
        boolean dtaCalculado = imp.getValorFobBD().compareTo(BigDecimal.ZERO) > 0 && imp.getValorCifBD().compareTo(BigDecimal.ZERO) > 0;
        boolean dtaTieneMontos = imp.getTotalImpuestosBD().compareTo(BigDecimal.ZERO) > 0;
        boolean dtaDisponible = dtaCalculado;
        boolean dtaRequiereRevision = imp.getHsCode() == null || imp.getHsCode().isBlank();
        
        dta.put("dtaDisponible", dtaDisponible);
        dta.put("dtaCalculado", dtaCalculado);
        dta.put("dtaTieneMontos", dtaTieneMontos);
        dta.put("dtaRequiereRevision", dtaRequiereRevision);
        dta.put("dtaMensaje", dtaCalculado 
                ? "DTA calculada a partir de los valores FOB, Flete y Seguro de la operación."
                : "No se puede calcular el costo aproximado. Por favor, ingrese el valor FOB, flete y seguro en el asistente.");

        // Incoterm responsibility description
        String incoterm = imp.getIncoterm();
        String envioSeguroTexto = "Incoterm pendiente de definir. Revisar responsabilidades.";
        if (incoterm != null && !incoterm.isBlank()) {
            incoterm = incoterm.trim().toUpperCase();
            if ("CIF".equals(incoterm) || "CIP".equals(incoterm)) {
                envioSeguroTexto = "Vendedor asume (flete y seguro internacional incluidos en valor de transacción) - Incoterm " + incoterm;
            } else if ("FOB".equals(incoterm) || "FCA".equals(incoterm) || "EXW".equals(incoterm)) {
                envioSeguroTexto = "Comprador asume flete y seguro internacional por separado - Incoterm " + incoterm;
            } else {
                envioSeguroTexto = "Comprador asume responsabilidades generales - Incoterm " + incoterm;
            }
        }
        dta.put("incotermResponsabilidad", envioSeguroTexto);

        return dta;
    }
}
