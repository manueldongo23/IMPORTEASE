package com.importease.proyecto.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Operacion {
    private int id;
    private int usuarioId;
    private String productoDesc;
    private String hsCode;
    private String paisOrigen;
    private String incoterm;
    private BigDecimal fob;
    private BigDecimal flete;
    private BigDecimal seguro;
    private BigDecimal cif;
    private BigDecimal tipoCambio;
    private BigDecimal adValoremAplicado;
    private BigDecimal iscAplicado;
    private BigDecimal igvAplicado;
    private BigDecimal ipmAplicado;
    private BigDecimal percepcionAplicada;
    private BigDecimal totalImpuestos;
    private String canalAsignado;
    private String estado;
    private String numeroDam;
    private Timestamp fechaNumeracion;
    private Timestamp fechaCreacion;

    public Operacion() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getProductoDesc() { return productoDesc; }
    public void setProductoDesc(String productoDesc) { this.productoDesc = productoDesc; }
    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
    public String getIncoterm() { return incoterm; }
    public void setIncoterm(String incoterm) { this.incoterm = incoterm; }
    public BigDecimal getFob() { return fob; }
    public void setFob(BigDecimal fob) { this.fob = fob; }
    public BigDecimal getFlete() { return flete; }
    public void setFlete(BigDecimal flete) { this.flete = flete; }
    public BigDecimal getSeguro() { return seguro; }
    public void setSeguro(BigDecimal seguro) { this.seguro = seguro; }
    public BigDecimal getCif() { return cif; }
    public void setCif(BigDecimal cif) { this.cif = cif; }
    public BigDecimal getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }
    public BigDecimal getAdValoremAplicado() { return adValoremAplicado; }
    public void setAdValoremAplicado(BigDecimal adValoremAplicado) { this.adValoremAplicado = adValoremAplicado; }
    public BigDecimal getIscAplicado() { return iscAplicado; }
    public void setIscAplicado(BigDecimal iscAplicado) { this.iscAplicado = iscAplicado; }
    public BigDecimal getIgvAplicado() { return igvAplicado; }
    public void setIgvAplicado(BigDecimal igvAplicado) { this.igvAplicado = igvAplicado; }
    public BigDecimal getIpmAplicado() { return ipmAplicado; }
    public void setIpmAplicado(BigDecimal ipmAplicado) { this.ipmAplicado = ipmAplicado; }
    public BigDecimal getPercepcionAplicada() { return percepcionAplicada; }
    public void setPercepcionAplicada(BigDecimal percepcionAplicada) { this.percepcionAplicada = percepcionAplicada; }
    public BigDecimal getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }
    public String getCanalAsignado() { return canalAsignado; }
    public void setCanalAsignado(String canalAsignado) { this.canalAsignado = canalAsignado; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNumeroDam() { return numeroDam; }
    public void setNumeroDam(String numeroDam) { this.numeroDam = numeroDam; }
    public Timestamp getFechaNumeracion() { return fechaNumeracion; }
    public void setFechaNumeracion(Timestamp fechaNumeracion) { this.fechaNumeracion = fechaNumeracion; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
