package com.importease.proyecto.model;

import java.sql.Timestamp;

public class PartidaArancelaria {
    private String hsCode;
    private String descripcion;
    private double adValoremPct;
    private double iscPct;
    private double igvPct;
    private double ipmPct;
    private double seguroPct;
    private String tendenciaActual; // CRECIENTE, ESTANCADO, DECADENCIA
    private Timestamp ultimaActualizacionTendencia;

    public PartidaArancelaria() {}

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getAdValoremPct() { return adValoremPct; }
    public void setAdValoremPct(double adValoremPct) { this.adValoremPct = adValoremPct; }

    public double getIscPct() { return iscPct; }
    public void setIscPct(double iscPct) { this.iscPct = iscPct; }

    public double getIgvPct() { return igvPct; }
    public void setIgvPct(double igvPct) { this.igvPct = igvPct; }

    public double getIpmPct() { return ipmPct; }
    public void setIpmPct(double ipmPct) { this.ipmPct = ipmPct; }

    public double getSeguroPct() { return seguroPct; }
    public void setSeguroPct(double seguroPct) { this.seguroPct = seguroPct; }

    public String getTendenciaActual() { return tendenciaActual; }
    public void setTendenciaActual(String tendenciaActual) { this.tendenciaActual = tendenciaActual; }

    public Timestamp getUltimaActualizacionTendencia() { return ultimaActualizacionTendencia; }
    public void setUltimaActualizacionTendencia(Timestamp ultimaActualizacionTendencia) { this.ultimaActualizacionTendencia = ultimaActualizacionTendencia; }
}

