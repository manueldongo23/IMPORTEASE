package com.importease.proyecto.dto;

public class CoherenciaIssueDTO {
    private String tipo;
    private String descripcion;
    private String campo;
    private String gravedad;
    private String sugerencia;

    public CoherenciaIssueDTO() {}

    public CoherenciaIssueDTO(String tipo, String descripcion, String campo, String gravedad, String sugerencia) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.campo = campo;
        this.gravedad = gravedad;
        this.sugerencia = sugerencia;
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }
    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }
    public String getSugerencia() { return sugerencia; }
    public void setSugerencia(String sugerencia) { this.sugerencia = sugerencia; }
}

