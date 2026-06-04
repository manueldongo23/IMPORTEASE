package com.importease.proyecto.model;

public class PreguntaPermiso {
    private int id;
    private String codigoEntidad;
    private String pregunta;
    private String tipoRespuesta;
    private boolean obligatoria;
    private int orden;
    private boolean activo;

    public PreguntaPermiso() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigoEntidad() { return codigoEntidad; }
    public void setCodigoEntidad(String codigoEntidad) { this.codigoEntidad = codigoEntidad; }
    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }
    public String getTipoRespuesta() { return tipoRespuesta; }
    public void setTipoRespuesta(String tipoRespuesta) { this.tipoRespuesta = tipoRespuesta; }
    public boolean isObligatoria() { return obligatoria; }
    public void setObligatoria(boolean obligatoria) { this.obligatoria = obligatoria; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

