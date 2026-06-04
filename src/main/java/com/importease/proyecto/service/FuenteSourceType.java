package com.importease.proyecto.service;

public enum FuenteSourceType {
    OFFICIAL_API("OFFICIAL_API", true, false, 0.95),
    OFFICIAL_PROCEDURE("OFFICIAL_PROCEDURE", true, false, 0.90),
    CACHE_OFFICIAL("CACHE_OFFICIAL", true, false, 0.85),
    SYSTEM_RULE("SYSTEM_RULE", true, false, 1.0),
    MANUAL_USER_INPUT("MANUAL_USER_INPUT", false, false, 0.50),
    BD_LOCAL("BD_LOCAL", false, false, 0.85),
    REFERENTIAL("REFERENTIAL", false, false, 0.60),
    SIMULATED("SIMULATED", false, true, 0.20),
    UNKNOWN("UNKNOWN", false, true, 0.10);

    private final String code;
    private final boolean official;
    private final boolean simulated;
    private final double defaultConfidence;

    FuenteSourceType(String code, boolean official, boolean simulated, double defaultConfidence) {
        this.code = code;
        this.official = official;
        this.simulated = simulated;
        this.defaultConfidence = defaultConfidence;
    }

    public String getCode() { return code; }
    public boolean isOfficial() { return official; }
    public boolean isSimulated() { return simulated; }
    public boolean isAuditable() { return official || this == SYSTEM_RULE || this == MANUAL_USER_INPUT || this == BD_LOCAL || this == REFERENTIAL; }
    public double getDefaultConfidence() { return defaultConfidence; }

    public static FuenteSourceType fromCode(String code) {
        if (code == null) return UNKNOWN;
        String upper = code.trim().toUpperCase();
        for (FuenteSourceType t : values()) {
            if (t.code.equals(upper) || t.name().equals(upper)) return t;
        }
        return UNKNOWN;
    }

    public static FuenteSourceType fromLegacy(String legacy) {
        if (legacy == null) return UNKNOWN;
        switch (legacy.trim().toUpperCase()) {
            case "SIMULADO": return SIMULATED;
            case "ESTIMADO": return REFERENTIAL;
            case "MANUAL_REFERENCIAL": return MANUAL_USER_INPUT;
            case "BD_LOCAL": return BD_LOCAL;
            case "PENDIENTE_VALIDACION": return MANUAL_USER_INPUT;
            case "REGISTRADO": return OFFICIAL_PROCEDURE;
            case "OFICIAL_API": return OFFICIAL_API;
            case "CACHE": return CACHE_OFFICIAL;
            default: return fromCode(legacy);
        }
    }
}

