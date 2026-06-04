package com.importease.proyecto.model;

import java.math.BigDecimal;
import java.util.Date;

public class HsCode {
    private int id;
    private String codigo;
    private String descripcionEs;
    private String descripcionEn;
    private int capitulo;
    private int partida;
    private int subpartida;
    private int nacional;
    private BigDecimal adValorem;
    private BigDecimal isc;
    private BigDecimal igv;
    private BigDecimal ipm;
    private boolean requiereVuce;
    private String entidadVuce;
    private boolean tlcChina;
    private boolean antidumping;
    private String restricciones;
    private String prohibiciones;
    private Date fechaActualizacion;

    public HsCode() {}

    public Date getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Date fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcionEs() { return descripcionEs; }
    public void setDescripcionEs(String descripcionEs) { this.descripcionEs = descripcionEs; }
    public String getDescripcionEn() { return descripcionEn; }
    public void setDescripcionEn(String descripcionEn) { this.descripcionEn = descripcionEn; }
    public int getCapitulo() { return capitulo; }
    public void setCapitulo(int capitulo) { this.capitulo = capitulo; }
    public int getPartida() { return partida; }
    public void setPartida(int partida) { this.partida = partida; }
    public int getSubpartida() { return subpartida; }
    public void setSubpartida(int subpartida) { this.subpartida = subpartida; }
    public int getNacional() { return nacional; }
    public void setNacional(int nacional) { this.nacional = nacional; }
    public BigDecimal getAdValorem() { return adValorem != null ? adValorem : BigDecimal.ZERO; }
    public void setAdValorem(BigDecimal adValorem) { this.adValorem = adValorem; }
    public BigDecimal getIsc() { return isc != null ? isc : BigDecimal.ZERO; }
    public void setIsc(BigDecimal isc) { this.isc = isc; }
    public BigDecimal getIgv() { return igv != null ? igv : BigDecimal.ZERO; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getIpm() { return ipm != null ? ipm : BigDecimal.ZERO; }
    public void setIpm(BigDecimal ipm) { this.ipm = ipm; }
    public boolean isAntidumping() { return antidumping; }
    public void setAntidumping(boolean antidumping) { this.antidumping = antidumping; }
    public String getRestricciones() { return restricciones; }
    public void setRestricciones(String restricciones) { this.restricciones = restricciones; }
    public String getProhibiciones() { return prohibiciones; }
    public void setProhibiciones(String prohibiciones) { this.prohibiciones = prohibiciones; }
    public boolean isRequiereVuce() { return requiereVuce; }
    public void setRequiereVuce(boolean requiereVuce) { this.requiereVuce = requiereVuce; }
    public String getEntidadVuce() { return entidadVuce; }
    public void setEntidadVuce(String entidadVuce) { this.entidadVuce = entidadVuce; }
    public boolean isTlcChina() { return tlcChina; }
    public void setTlcChina(boolean tlcChina) { this.tlcChina = tlcChina; }
}

