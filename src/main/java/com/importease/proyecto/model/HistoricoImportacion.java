package com.importease.proyecto.model;

public class HistoricoImportacion {
    private int id;
    private String hsCode;
    private int anio;
    private double volumenImportado;

    public HistoricoImportacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public double getVolumenImportado() { return volumenImportado; }
    public void setVolumenImportado(double volumenImportado) { this.volumenImportado = volumenImportado; }
}

