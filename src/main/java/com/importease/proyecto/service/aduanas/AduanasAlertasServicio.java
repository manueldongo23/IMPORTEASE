package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AduanasAlertasServicio {

    private final AduanasConstructorRespuesta responseBuilder = new AduanasConstructorRespuesta();

    public List<Map<String, Object>> buildAlertas(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad, boolean restringida) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (restringida) {
            list.add(alert("MERCANCIA_RESTRINGIDA", "ALTA", "El HS sugiere mercancia restringida o de control sectorial.", "VUCE/SUNAT referencial", "Revisar permisos antes de embarcar o nacionalizar.", "VUCE / SUNAT", "VUCE"));
        }
        if (imp.getValorFobBD().compareTo(new BigDecimal("2000")) > 0) {
            list.add(alert("AGENTE_ADUANA_MANDATORIO", "ALTA", "El valor FOB supera los USD 2000. La Ley General de Aduanas exige obligatoriamente contratar un Agente de Aduanas licenciado.", "LGA Art. 21", "Contratar una agencia de aduanas autorizada por SUNAT para numerar el Despacho General.", "SUNAT", "OFICIAL"));
        }
        if (imp.getValorFobBD().compareTo(new BigDecimal("2000")) > 0 && "10".equals(regimen.get("regimenCodigo")) && "ANTICIPADO".equals(modalidad.get("modalidadCodigo"))) {
            list.add(alert("DESPACHO_ANTICIPADO", "MEDIA", "La modalidad anticipada queda como recomendada/general en esta simulacion.", "DESPA-PG.01 referencial", "Preparar documentos antes del arribo.", "SUNAT", "SYSTEM_RULE"));
        }
        if (!hasPredam(imp)) {
            list.add(alert("PREDAM_REFERENCIAL", "BAJA", "La PRE-DAM es referencial y debe validarse con agente de aduana.", "Simulacion academica", "No presentarla como DAM oficial.", "ImportEase", "REFERENCIAL"));
        }
        if ("36".equals(regimen.get("regimenCodigo"))) {
            list.add(alert("CANAL_ROJO_REIMPORTACION", "ALTA", "Reimportacion exige comprobar que la mercancia retorna en el mismo estado.", "DESPA-PG.26 referencial", "Preparar declaracion de exportacion precedente.", "SUNAT", "OFICIAL"));
        }
        if ("ADM_TEMP".equals(regimen.get("regimenCodigo")) || "80".equals(regimen.get("regimenCodigo"))) {
            list.add(alert("GARANTIA_REQUERIDA", "ALTA", "El regimen seleccionado requiere garantia referencial.", "Procedimiento DESPA referencial", "Calcular monto minimo y fecha de vencimiento.", "SUNAT", "SYSTEM_RULE"));
        }
        return list;
    }

    public List<String> alertasTransbordo(boolean pesoOk, boolean precintoOk, boolean solicitudesPendientes) {
        List<String> alertas = new ArrayList<>();
        if (!pesoOk) alertas.add("Diferencia de peso mayor al 2%; requiere revision.");
        if (!precintoOk) alertas.add("Precinto violado o no conforme; requiere control.");
        if (solicitudesPendientes) alertas.add("Existen solicitudes pendientes que impiden regularizar.");
        if (alertas.isEmpty()) alertas.add("Sin alertas criticas para regularizacion referencial.");
        return alertas;
    }

    private Map<String, Object> alert(String tipo, String severidad, String mensaje, String baseLegal, String accion, String fuente, String sourceType) {
        Map<String, Object> map = responseBuilder.alert(tipo, severidad, mensaje, baseLegal, accion);
        map.put("source", fuente);
        map.put("sourceType", sourceType);
        return map;
    }

    private boolean hasPredam(Importacion imp) {
        return imp.getNumeroDam() != null && !imp.getNumeroDam().isBlank();
    }
}
