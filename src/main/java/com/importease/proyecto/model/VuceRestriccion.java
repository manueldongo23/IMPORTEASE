package com.importease.proyecto.model;

public class VuceRestriccion {
    private int id;
    private String entidad;
    private String tipoControl;
    private String descripcion;
    private String productosEjemplo;
    private boolean requiereRegistroSanitario;
    private int tiempoEstimadoDias;
    private String enlaceTupa;

    public VuceRestriccion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getTipoControl() { return tipoControl; }
    public void setTipoControl(String tipoControl) { this.tipoControl = tipoControl; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getProductosEjemplo() { return productosEjemplo; }
    public void setProductosEjemplo(String productosEjemplo) { this.productosEjemplo = productosEjemplo; }
    public boolean isRequiereRegistroSanitario() { return requiereRegistroSanitario; }
    public void setRequiereRegistroSanitario(boolean requiereRegistroSanitario) { this.requiereRegistroSanitario = requiereRegistroSanitario; }
    public int getTiempoEstimadoDias() { return tiempoEstimadoDias; }
    public void setTiempoEstimadoDias(int tiempoEstimadoDias) { this.tiempoEstimadoDias = tiempoEstimadoDias; }
    public String getEnlaceTupa() { return enlaceTupa; }
    public void setEnlaceTupa(String enlaceTupa) { this.enlaceTupa = enlaceTupa; }
}
