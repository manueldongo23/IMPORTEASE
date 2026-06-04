package com.importease.proyecto.dto;

import java.util.List;
import java.util.Map;

public class PredamResponseDTO {
    private boolean predamGenerada;
    private String mensaje;
    private List<String> camposFaltantes;
    private List<String> advertencias;
    private Map<String, Object> metadata;

    public PredamResponseDTO() {}

    public PredamResponseDTO(boolean predamGenerada, String mensaje, List<String> camposFaltantes, List<String> advertencias, Map<String, Object> metadata) {
        this.predamGenerada = predamGenerada;
        this.mensaje = mensaje;
        this.camposFaltantes = camposFaltantes;
        this.advertencias = advertencias;
        this.metadata = metadata;
    }

    public boolean isPredamGenerada() { return predamGenerada; }
    public void setPredamGenerada(boolean predamGenerada) { this.predamGenerada = predamGenerada; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public List<String> getCamposFaltantes() { return camposFaltantes; }
    public void setCamposFaltantes(List<String> camposFaltantes) { this.camposFaltantes = camposFaltantes; }
    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) { this.advertencias = advertencias; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}


