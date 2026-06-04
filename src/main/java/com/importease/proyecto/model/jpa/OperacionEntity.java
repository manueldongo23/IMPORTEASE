package com.importease.proyecto.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "operaciones")
public class OperacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "producto_desc")
    private String productoDesc;

    @Column(name = "hs_code")
    private String hsCode;

    @Column(name = "pais_origen")
    private String paisOrigen;

    @Column(name = "incoterm")
    private String incoterm;

    @Column(name = "fob")
    private BigDecimal fob;

    @Column(name = "flete")
    private BigDecimal flete;

    @Column(name = "seguro")
    private BigDecimal seguro;

    @Column(name = "cif")
    private BigDecimal cif;

    @Column(name = "tipo_cambio")
    private BigDecimal tipoCambio;

    @Column(name = "ad_valorem_aplicado")
    private BigDecimal adValoremAplicado;

    @Column(name = "isc_aplicado")
    private BigDecimal iscAplicado;

    @Column(name = "igv_aplicado")
    private BigDecimal igvAplicado;

    @Column(name = "ipm_aplicado")
    private BigDecimal ipmAplicado;

    @Column(name = "percepcion_aplicada")
    private BigDecimal percepcionAplicada;

    @Column(name = "total_impuestos")
    private BigDecimal totalImpuestos;

    @Column(name = "canal_asignado")
    private String canalAsignado;

    @Column(name = "estado")
    private String estado;

    @Column(name = "numero_dam")
    private String numeroDam;

    @Column(name = "fecha_numeracion")
    private Timestamp fechaNumeracion;

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    @Column(name = "usado")
    private Boolean usado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getProductoDesc() {
        return productoDesc;
    }

    public void setProductoDesc(String productoDesc) {
        this.productoDesc = productoDesc;
    }

    public String getHsCode() {
        return hsCode;
    }

    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public BigDecimal getFob() {
        return fob;
    }

    public void setFob(BigDecimal fob) {
        this.fob = fob;
    }

    public BigDecimal getFlete() {
        return flete;
    }

    public void setFlete(BigDecimal flete) {
        this.flete = flete;
    }

    public BigDecimal getSeguro() {
        return seguro;
    }

    public void setSeguro(BigDecimal seguro) {
        this.seguro = seguro;
    }

    public BigDecimal getCif() {
        return cif;
    }

    public void setCif(BigDecimal cif) {
        this.cif = cif;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public BigDecimal getAdValoremAplicado() {
        return adValoremAplicado;
    }

    public void setAdValoremAplicado(BigDecimal adValoremAplicado) {
        this.adValoremAplicado = adValoremAplicado;
    }

    public BigDecimal getIscAplicado() {
        return iscAplicado;
    }

    public void setIscAplicado(BigDecimal iscAplicado) {
        this.iscAplicado = iscAplicado;
    }

    public BigDecimal getIgvAplicado() {
        return igvAplicado;
    }

    public void setIgvAplicado(BigDecimal igvAplicado) {
        this.igvAplicado = igvAplicado;
    }

    public BigDecimal getIpmAplicado() {
        return ipmAplicado;
    }

    public void setIpmAplicado(BigDecimal ipmAplicado) {
        this.ipmAplicado = ipmAplicado;
    }

    public BigDecimal getPercepcionAplicada() {
        return percepcionAplicada;
    }

    public void setPercepcionAplicada(BigDecimal percepcionAplicada) {
        this.percepcionAplicada = percepcionAplicada;
    }

    public BigDecimal getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(BigDecimal totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public String getCanalAsignado() {
        return canalAsignado;
    }

    public void setCanalAsignado(String canalAsignado) {
        this.canalAsignado = canalAsignado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNumeroDam() {
        return numeroDam;
    }

    public void setNumeroDam(String numeroDam) {
        this.numeroDam = numeroDam;
    }

    public Timestamp getFechaNumeracion() {
        return fechaNumeracion;
    }

    public void setFechaNumeracion(Timestamp fechaNumeracion) {
        this.fechaNumeracion = fechaNumeracion;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getUsado() {
        return usado;
    }

    public void setUsado(Boolean usado) {
        this.usado = usado;
    }
}
