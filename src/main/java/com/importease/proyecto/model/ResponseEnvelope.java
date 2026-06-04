package com.importease.proyecto.model;

import java.time.Instant;
import com.importease.proyecto.service.DataConfidenceService;
import com.importease.proyecto.service.FuenteMetadataBuilder;

public class ResponseEnvelope {
    private boolean success;
    private String source;
    private String sourceType;
    private double confidence;
    private String updatedAt;
    private Object data;
    private String errorCode;
    private String message;
    private boolean fallbackUsed;

    public static ResponseEnvelope ok(Object data, String source, String sourceType, double confidence) {
        ResponseEnvelope envelope = new ResponseEnvelope();
        envelope.success = true;
        envelope.data = data;
        envelope.source = source;
        envelope.sourceType = normalizeSourceType(sourceType);
        envelope.confidence = confidence > 0 ? confidence : DataConfidenceService.confidenceFor(envelope.sourceType);
        envelope.updatedAt = Instant.now().toString();
        return envelope;
    }

    public static ResponseEnvelope error(String errorCode, String message, String source, boolean fallbackUsed) {
        ResponseEnvelope envelope = new ResponseEnvelope();
        envelope.success = false;
        envelope.errorCode = errorCode;
        envelope.message = message;
        envelope.source = source;
        envelope.sourceType = fallbackUsed ? "FALLBACK" : "PENDIENTE_VALIDACION";
        envelope.confidence = fallbackUsed ? DataConfidenceService.confidenceFor("FALLBACK") : DataConfidenceService.confidenceFor("PENDIENTE_VALIDACION");
        envelope.fallbackUsed = fallbackUsed;
        envelope.updatedAt = Instant.now().toString();
        return envelope;
    }

    private static String normalizeSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) {
            return FuenteMetadataBuilder.TYPE_UNKNOWN;
        }
        return sourceType.trim();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}

