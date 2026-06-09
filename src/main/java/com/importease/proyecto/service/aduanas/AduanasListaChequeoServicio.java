package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AduanasListaChequeoServicio {

    private final AduanasConstructorRespuesta responseBuilder = new AduanasConstructorRespuesta();

    public List<Map<String, Object>> checklist(String regimen, Importacion imp, boolean restringida) {
        return checklist(regimen, imp, restringida, false);
    }

    public List<Map<String, Object>> checklist(String regimen, Importacion imp, boolean restringida, boolean tlcAplicado) {
        List<Map<String, Object>> docs = new ArrayList<>();
        docs.add(doc("FACTURA_COMERCIAL", "Factura comercial", "Debe incluir descripcion, moneda, valor unitario, Incoterm y forma de pago.", true, "BD_LOCAL"));
        docs.add(doc("DOCUMENTO_TRANSPORTE", "BL/AWB/documento de transporte", "Vincula manifiesto, consignatario, bultos y peso.", true, "BD_LOCAL"));
        docs.add(doc("SEGURO", "Seguro", "Obligatorio para sustentar valor si corresponde por Incoterm.", "CIF".equalsIgnoreCase(imp.getIncoterm()) || "CIP".equalsIgnoreCase(imp.getIncoterm()), "ESTIMADO"));
        
        // QA-014: CERTIFICADO_ORIGEN required if TLC applied
        boolean certOrigenRequerido = tlcAplicado;
        docs.add(doc("CERTIFICADO_ORIGEN", "Certificado de origen", "Necesario si deseas aplicar preferencia arancelaria.", certOrigenRequerido, "ESTIMADO"));
        
        if (restringida) docs.add(doc("PERMISO_SECTORIAL", "Permiso sectorial/VUCE", "Requerido por posible mercancia restringida.", true, "BD_LOCAL"));
        if ("ADM_TEMP".equals(regimen)) {
            docs.add(doc("GARANTIA", "Garantia", "Cubre tributos, recargos e intereses proyectados.", true, "ESTIMADO"));
            docs.add(doc("DJ_UBICACION_FINALIDAD", "Declaracion jurada de ubicacion y finalidad", "Explica uso temporal y ubicacion de la mercancia.", true, "BD_LOCAL"));
        }
        if ("36".equals(regimen)) docs.add(doc("DECLARACION_EXPORTACION", "Declaracion de exportacion precedente", "Debe estar regularizada y dentro de plazo.", true, "BD_LOCAL"));
        if ("TRANSBORDO".equals(regimen)) docs.add(doc("REGULARIZACION_TRANSBORDO", "Control de regularizacion", "Pesos, bultos, contenedores, precintos y aduana de salida.", true, "BD_LOCAL"));
        return docs;
    }

    private Map<String, Object> doc(String codigo, String nombre, String descripcion, boolean requerido, String sourceType) {
        return responseBuilder.doc(codigo, nombre, descripcion, requerido, sourceType);
    }
}
