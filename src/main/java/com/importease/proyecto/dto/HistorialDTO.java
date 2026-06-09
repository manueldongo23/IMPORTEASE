package com.importease.proyecto.dto;

public class HistorialDTO {
    private int id;
    private String productoDesc;
    private String hsCode;
    private String entidad;
    private String stateLabel;
    private String stateClass;
    private String docFraction;
    private String nextAction;
    private boolean listable;
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
    public String getStateLabel() { return stateLabel; }
    public void setStateLabel(String stateLabel) { this.stateLabel = stateLabel; }
    public String getStateClass() { return stateClass; }
    public void setStateClass(String stateClass) { this.stateClass = stateClass; }
    public String getDocFraction() { return docFraction; }
    public void setDocFraction(String docFraction) { this.docFraction = docFraction; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public boolean isListable() { return listable; }
    public void setListable(boolean listable) { this.listable = listable; }
    public String getRawState() { return rawState; }
    public void setRawState(String rawState) { this.rawState = rawState; }
}
