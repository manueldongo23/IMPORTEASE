package com.importease.proyecto.model;

public class BlHijo {
    private int id;
    private int blMasterId;
    private String numeroBlHijo;
    private int importadorId;
    private double volumenCbm;
    private double pesoKg;

    public BlHijo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlMasterId() { return blMasterId; }
    public void setBlMasterId(int blMasterId) { this.blMasterId = blMasterId; }

    public String getNumeroBlHijo() { return numeroBlHijo; }
    public void setNumeroBlHijo(String numeroBlHijo) { this.numeroBlHijo = numeroBlHijo; }

    public int getImportadorId() { return importadorId; }
    public void setImportadorId(int importadorId) { this.importadorId = importadorId; }

    public double getVolumenCbm() { return volumenCbm; }
    public void setVolumenCbm(double volumenCbm) { this.volumenCbm = volumenCbm; }

    public double getPesoKg() { return pesoKg; }
    public void setPesoKg(double pesoKg) { this.pesoKg = pesoKg; }
}

