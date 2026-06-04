package com.importease.proyecto.model;

public class EntidadControl {
    private int id;
    private String codigoEntidad;
    private String nombreEntidad;
    private String sector;
    private String descripcion;
    private String urlReferencia;
    private boolean activo;

    public EntidadControl() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigoEntidad() { return codigoEntidad; }
    public void setCodigoEntidad(String codigoEntidad) { this.codigoEntidad = codigoEntidad; }
    public String getNombreEntidad() { return nombreEntidad; }
    public void setNombreEntidad(String nombreEntidad) { this.nombreEntidad = nombreEntidad; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getUrlReferencia() { return urlReferencia; }
    public void setUrlReferencia(String urlReferencia) { this.urlReferencia = urlReferencia; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

