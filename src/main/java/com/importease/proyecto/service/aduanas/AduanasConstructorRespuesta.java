package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.service.DataConfidenceServicio;
import com.importease.proyecto.service.FuenteMetadataBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class AduanasConstructorRespuesta {
    public static final String SOURCE = "DESPA_REFERENCIAL";
    public static final String SOURCE_TYPE = "BD_LOCAL";
    public static final double CONFIDENCE = 0.85;

    public Map<String, Object> baseResult() {
        return FuenteMetadataBuilder.buildMetadata(SOURCE, SOURCE_TYPE, CONFIDENCE, "Referencial academico");
    }

    public Map<String, Object> errorData(String code, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("errorCode", code);
        map.put("message", message);
        return map;
    }

    public Map<String, Object> item(String label, boolean value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    public Map<String, Object> event(String codigo, String nombre, LocalDateTime fecha, String responsable, String documento, String efecto, String observacion) {
        Map<String, Object> map = baseResult();
        map.put("codigo", codigo);
        map.put("nombre", nombre);
        map.put("fecha", fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        map.put("fechaIso", fecha.withSecond(0).withNano(0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        map.put("responsable", responsable);
        map.put("documento", documento);
        map.put("efectoLegal", efecto);
        map.put("observacion", observacion);
        return map;
    }

    public Map<String, Object> alert(String tipo, String severidad, String mensaje, String baseLegal, String accion) {
        Map<String, Object> map = baseResult();
        map.put("tipo", tipo);
        map.put("severidad", severidad);
        map.put("mensaje", mensaje);
        map.put("baseLegal", baseLegal);
        map.put("accion", accion);
        return map;
    }

    public Map<String, Object> doc(String codigo, String nombre, String descripcion, boolean requerido, String sourceType) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("codigo", codigo);
        map.put("nombre", nombre);
        map.put("descripcion", descripcion);
        map.put("requerido", requerido);
        map.put("estado", requerido ? "Pendiente" : "Segun corresponda");
        map.put("source", SOURCE);
        map.put("sourceType", sourceType);
        map.put("confidence", DataConfidenceServicio.confidenceFor(sourceType));
        return map;
    }

    public Map<String, Object> legal(String modulo, String procedimiento, String validacion, String base, String estado, String accion) {
        Map<String, Object> map = baseResult();
        map.put("modulo", modulo);
        map.put("procedimiento", procedimiento);
        map.put("validacion", validacion);
        map.put("baseLegal", base);
        map.put("estado", estado);
        map.put("accion", accion);
        return map;
    }
}
