package com.importease.proyecto.dto;

public class PermisoDTO {
    private int id;
    private String productoDesc;
    private String hsCode;
    private String entidad;
    private String permitName;
    private String stateLabel;
    private String stateClass;
    private String actionLabel;
    private String actionClass;
    private String fecha;
    private String rawState;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getProductoDesc() { return productoDesc; }
    public void setProductoDesc(String productoDesc) { this.productoDesc = productoDesc; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }
    public String getPermitName() { return permitName; }
    public void setPermitName(String permitName) { this.permitName = permitName; }
    public String getStateLabel() { return stateLabel; }
    public void setStateLabel(String stateLabel) { this.stateLabel = stateLabel; }
    public String getStateClass() { return stateClass; }
    public void setStateClass(String stateClass) { this.stateClass = stateClass; }
    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }
    public String getActionClass() { return actionClass; }
    public void setActionClass(String actionClass) { this.actionClass = actionClass; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getRawState() { return rawState; }
    public void setRawState(String rawState) { this.rawState = rawState; }
}
