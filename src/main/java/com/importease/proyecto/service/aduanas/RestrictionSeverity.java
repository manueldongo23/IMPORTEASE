package com.importease.proyecto.service.aduanas;

public enum RestrictionSeverity {
    INFO("informativa"),
    WARNING("advertencia"),
    PERMISSION_REQUIRED("requiere permiso"),
    CRITICAL("crítica"),
    REQUIRES_REVIEW("requiere revisión");

    private final String label;

    RestrictionSeverity(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static RestrictionSeverity fromString(String value) {
        if (value == null) return INFO;
        switch (value.toLowerCase()) {
            case "informativa":
                return INFO;
            case "advertencia":
                return WARNING;
            case "requiere permiso":
                return PERMISSION_REQUIRED;
            case "critica":
            case "crítica":
                return CRITICAL;
            case "requiere revision":
            case "requiere revisión":
                return REQUIRES_REVIEW;
            default:
                return INFO;
        }
    }
}
