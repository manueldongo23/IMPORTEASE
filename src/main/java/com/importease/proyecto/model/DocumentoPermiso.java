package com.importease.proyecto.model;

public class DocumentoPermiso {
    private int id;
    private String codigoEntidad;
    private String tipoPermiso;
    private String nombreDocumento;
    private String descripcion;
    private boolean obligatorio;
    private String formatoAceptado;

    public DocumentoPermiso() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigoEntidad() { return codigoEntidad; }
    public void setCodigoEntidad(String codigoEntidad) { this.codigoEntidad = codigoEntidad; }
    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
    public String getNombreDocumento() { return nombreDocumento; }
    public void setNombreDocumento(String nombreDocumento) { this.nombreDocumento = nombreDocumento; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isObligatorio() { return obligatorio; }
    public void setObligatorio(boolean obligatorio) { this.obligatorio = obligatorio; }
    public String getFormatoAceptado() { return formatoAceptado; }
    public void setFormatoAceptado(String formatoAceptado) { this.formatoAceptado = formatoAceptado; }
}

