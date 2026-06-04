package com.importease.proyecto.model;

import java.sql.Date;

public class BlMaster {
    private int id;
    private String numeroBl;
    private String naviera;
    private String puertoOrigen;
    private String puertoDestino;
    private Date fechaEmbarque;
    private String estado; // EN_TRANSITO, ARRIBADO, DESCARGADO

    public BlMaster() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroBl() { return numeroBl; }
    public void setNumeroBl(String numeroBl) { this.numeroBl = numeroBl; }

    public String getNaviera() { return naviera; }
    public void setNaviera(String naviera) { this.naviera = naviera; }

    public String getPuertoOrigen() { return puertoOrigen; }
    public void setPuertoOrigen(String puertoOrigen) { this.puertoOrigen = puertoOrigen; }

    public String getPuertoDestino() { return puertoDestino; }
    public void setPuertoDestino(String puertoDestino) { this.puertoDestino = puertoDestino; }

    public Date getFechaEmbarque() { return fechaEmbarque; }
    public void setFechaEmbarque(Date fechaEmbarque) { this.fechaEmbarque = fechaEmbarque; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

