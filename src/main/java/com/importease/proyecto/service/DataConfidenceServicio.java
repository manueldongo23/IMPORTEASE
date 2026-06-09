package com.importease.proyecto.service;

public class DataConfidenceServicio {
    public static double confidenceFor(String sourceType) {
        if (sourceType == null) return 0.0;
        return switch (sourceType) {
            case "OFICIAL_API" -> 0.98;
            case "OFFICIAL_PROCEDURE" -> 0.96;
            case "OFICIAL_WEB" -> 0.90;
            case "OFFICIAL_API_CACHE" -> 0.92;
            case "CACHE_OFFICIAL" -> 0.90;
            case "CACHE" -> 0.80;
            case "TERCERO_API" -> 0.82;
            case "BD_LOCAL" -> 0.85;
            case "SYSTEM_RULE" -> 0.78;
            case "REFERENTIAL" -> 0.60;
            case "ESTIMADO" -> 0.60;
            case "SIMULATED", "SIMULADO" -> 0.20;
            case "MANUAL_USER_INPUT", "MANUAL" -> 0.50;
            case "MANUAL_VERIFICADO" -> 0.65;
            case "PENDIENTE_CREDENCIALES" -> 0.15;
            case "PENDIENTE_VALIDACION" -> 0.10;
            case "FALLBACK" -> 0.35;
            case "UNKNOWN" -> 0.0;
            case "OFFICIAL_BULK" -> 0.97;
            case "OFFICIAL_WEB_PUBLIC" -> 0.88;
            case "OFFICIAL_WEB_AUTHENTICATED" -> 0.92;
            case "LICENSED_COMMERCIAL" -> 0.85;
            case "USER_DOCUMENT" -> 0.40;
            case "TRAINING_MATERIAL" -> 0.10;
            default -> 0.0;
        };
    }
}

