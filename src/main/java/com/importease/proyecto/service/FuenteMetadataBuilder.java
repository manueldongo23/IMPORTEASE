package com.importease.proyecto.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class FuenteMetadataBuilder {

    public static final String TYPE_OFFICIAL_API = "OFFICIAL_API";
    public static final String TYPE_OFFICIAL_PROCEDURE = "OFFICIAL_PROCEDURE";
    public static final String TYPE_MANUAL_USER_INPUT = "MANUAL_USER_INPUT";
    public static final String TYPE_CACHE_OFFICIAL = "CACHE_OFFICIAL";
    public static final String TYPE_SYSTEM_RULE = "SYSTEM_RULE";
    public static final String TYPE_SIMULATED = "SIMULATED";
    public static final String TYPE_REFERENTIAL = "REFERENTIAL";
    public static final String TYPE_BD_LOCAL = "BD_LOCAL";
    public static final String TYPE_UNKNOWN = "UNKNOWN";
    public static final String TYPE_OFFICIAL_BULK = "OFFICIAL_BULK";
    public static final String TYPE_OFFICIAL_WEB_PUBLIC = "OFFICIAL_WEB_PUBLIC";
    public static final String TYPE_OFFICIAL_WEB_AUTHENTICATED = "OFFICIAL_WEB_AUTHENTICATED";
    public static final String TYPE_LICENSED_COMMERCIAL = "LICENSED_COMMERCIAL";
    public static final String TYPE_USER_DOCUMENT = "USER_DOCUMENT";
    public static final String TYPE_TRAINING_MATERIAL = "TRAINING_MATERIAL";

    public static Map<String, Object> buildMetadata(String source, String sourceType, double confidence, String legalWarning) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("source", source != null ? source : "No identificada");
        meta.put("sourceType", sourceType != null ? sourceType : TYPE_UNKNOWN);
        meta.put("confidence", confidence);
        meta.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        boolean isOfficial = TYPE_OFFICIAL_API.equals(sourceType) || TYPE_OFFICIAL_PROCEDURE.equals(sourceType) || TYPE_CACHE_OFFICIAL.equals(sourceType) || TYPE_OFFICIAL_BULK.equals(sourceType) || TYPE_OFFICIAL_WEB_PUBLIC.equals(sourceType) || TYPE_OFFICIAL_WEB_AUTHENTICATED.equals(sourceType);
        boolean isSimulated = TYPE_SIMULATED.equals(sourceType);
        boolean isAuditable = isOfficial
                || TYPE_MANUAL_USER_INPUT.equals(sourceType)
                || TYPE_SYSTEM_RULE.equals(sourceType)
                || TYPE_REFERENTIAL.equals(sourceType)
                || TYPE_BD_LOCAL.equals(sourceType);
        
        meta.put("isOfficial", isOfficial);
        meta.put("isAuditable", isAuditable);
        meta.put("isSimulated", isSimulated);
        meta.put("legalWarning", legalWarning != null ? legalWarning : (isSimulated ? "Resultado simulado referencial" : ""));
        
        return meta;
    }

    public static Map<String, Object> addMetadata(Map<String, Object> map, String source, String sourceType, double confidence, String legalWarning) {
        map.putAll(buildMetadata(source, sourceType, confidence, legalWarning));
        return map;
    }
}


