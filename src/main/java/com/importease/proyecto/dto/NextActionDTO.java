package com.importease.proyecto.dto;

import java.util.List;

public class NextActionDTO {
    private String accion;
    private String motivo;
    private int paso;
    private String prioridad;
    private String bloqueo;
    private List<String> camposFaltantes;

    public NextActionDTO() {}

    public NextActionDTO(String accion, String motivo, int paso, String prioridad, String bloqueo, List<String> camposFaltantes) {
        this.accion = accion;
        this.motivo = motivo;
        this.paso = paso;
        this.prioridad = prioridad;
        this.bloqueo = bloqueo;
        this.camposFaltantes = camposFaltantes;
    }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public int getPaso() { return paso; }
    public void setPaso(int paso) { this.paso = paso; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public String getBloqueo() { return bloqueo; }
    public void setBloqueo(String bloqueo) { this.bloqueo = bloqueo; }
    public List<String> getCamposFaltantes() { return camposFaltantes; }
    public void setCamposFaltantes(List<String> camposFaltantes) { this.camposFaltantes = camposFaltantes; }
}


