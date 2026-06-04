package com.importease.proyecto.service.importacion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportacionStateMachine {
    private static final Map<String, Set<String>> TRANSITIONS = new LinkedHashMap<>();
    private static final List<Map<String, Object>> PUBLIC_STATES;

    static {
        TRANSITIONS.put("COTIZACION", Set.of("TRANSITO", "DOCS_PENDIENTES"));
        TRANSITIONS.put("TRANSITO", Set.of("DOCS_PENDIENTES"));
        TRANSITIONS.put("DOCS_PENDIENTES", Set.of("LISTA_DESPACHO"));
        TRANSITIONS.put("LISTA_DESPACHO", Set.of("NACIONALIZADA"));
        TRANSITIONS.put("NACIONALIZADA", Set.of());

        PUBLIC_STATES = List.of(
                Map.of("estado", "COTIZACION", "siguientes", List.of("TRANSITO", "DOCS_PENDIENTES")),
                Map.of("estado", "TRANSITO", "siguientes", List.of("DOCS_PENDIENTES")),
                Map.of("estado", "DOCS_PENDIENTES", "siguientes", List.of("LISTA_DESPACHO")),
                Map.of("estado", "LISTA_DESPACHO", "siguientes", List.of("NACIONALIZADA")),
                Map.of("estado", "NACIONALIZADA", "siguientes", List.of())
        );
    }

    public List<Map<String, Object>> estadosPublicos() {
        return PUBLIC_STATES;
    }

    public boolean permite(String estadoActual, String nuevoEstado) {
        Set<String> allowed = TRANSITIONS.get(estadoActual);
        return allowed != null && allowed.contains(nuevoEstado);
    }

    public String describirPermitidos(String estadoActual) {
        Set<String> allowed = TRANSITIONS.get(estadoActual);
        return allowed != null ? String.join(", ", allowed) : "ninguna";
    }
}
