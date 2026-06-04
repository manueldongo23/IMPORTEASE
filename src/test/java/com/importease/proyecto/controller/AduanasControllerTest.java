package com.importease.proyecto.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AduanasControllerTest {

    @Test
    public void resuelveMetadataDesdeMapa() throws Exception {
        AduanasController controller = new AduanasController();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("source", "SIMULACION_ACADEMICA");
        data.put("sourceType", "SIMULATED");
        data.put("confidence", 0.20);

        Object meta = invokeResolve(controller, data);

        assertEquals("SIMULACION_ACADEMICA", readField(meta, "source"));
        assertEquals("SIMULATED", readField(meta, "sourceType"));
        assertEquals(0.20, (Double) readField(meta, "confidence"), 0.0001);
    }

    @Test
    public void resuelveMetadataDesdeListaConPrimerElemento() throws Exception {
        AduanasController controller = new AduanasController();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source", "DESPA_REFERENCIAL");
        row.put("sourceType", "REFERENTIAL");
        row.put("confidence", 0.60);
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);

        Object meta = invokeResolve(controller, rows);

        assertEquals("DESPA_REFERENCIAL", readField(meta, "source"));
        assertEquals("REFERENTIAL", readField(meta, "sourceType"));
        assertEquals(0.60, (Double) readField(meta, "confidence"), 0.0001);
    }

    private Object invokeResolve(AduanasController controller, Object data) throws Exception {
        Method method = AduanasController.class.getDeclaredMethod("resolveEnvelopeMetadata", Object.class);
        method.setAccessible(true);
        return method.invoke(controller, data);
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}


