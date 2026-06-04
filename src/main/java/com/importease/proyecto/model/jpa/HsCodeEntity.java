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
@Table(name = "hs_codes")
public class HsCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "descripcion_es", nullable = false)
    private String descripcionEs;

    @Column(name = "descripcion_en")
    private String descripcionEn;

    @Column(name = "capitulo")
    private Integer capitulo;

    @Column(name = "partida")
    private Integer partida;

    @Column(name = "subpartida")
    private Integer subpartida;

    @Column(name = "nacional")
    private Integer nacional;

    @Column(name = "ad_valorem")
    private BigDecimal adValorem;

    @Column(name = "isc")
    private BigDecimal isc;

    @Column(name = "igv")
    private BigDecimal igv;

    @Column(name = "ipm")
    private BigDecimal ipm;

    @Column(name = "requiere_vuce")
    private Boolean requiereVuce;

    @Column(name = "entidad_vuce")
    private String entidadVuce;

    @Column(name = "tlc_china")
    private Boolean tlcChina;

    @Column(name = "antidumping")
    private Boolean antidumping;

    @Column(name = "restricciones")
    private String restricciones;

    @Column(name = "prohibiciones")
    private String prohibiciones;

    @Column(name = "fecha_actualizacion")
    private Timestamp fechaActualizacion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcionEs() {
        return descripcionEs;
    }

    public void setDescripcionEs(String descripcionEs) {
        this.descripcionEs = descripcionEs;
    }

    public String getDescripcionEn() {
        return descripcionEn;
    }

    public void setDescripcionEn(String descripcionEn) {
        this.descripcionEn = descripcionEn;
    }

    public Integer getCapitulo() {
        return capitulo;
    }

    public void setCapitulo(Integer capitulo) {
        this.capitulo = capitulo;
    }

    public Integer getPartida() {
        return partida;
    }

    public void setPartida(Integer partida) {
        this.partida = partida;
    }

    public Integer getSubpartida() {
        return subpartida;
    }

    public void setSubpartida(Integer subpartida) {
        this.subpartida = subpartida;
    }

    public Integer getNacional() {
        return nacional;
    }

    public void setNacional(Integer nacional) {
        this.nacional = nacional;
    }

    public BigDecimal getAdValorem() {
        return adValorem;
    }

    public void setAdValorem(BigDecimal adValorem) {
        this.adValorem = adValorem;
    }

    public BigDecimal getIsc() {
        return isc;
    }

    public void setIsc(BigDecimal isc) {
        this.isc = isc;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getIpm() {
        return ipm;
    }

    public void setIpm(BigDecimal ipm) {
        this.ipm = ipm;
    }

    public Boolean getRequiereVuce() {
        return requiereVuce;
    }

    public void setRequiereVuce(Boolean requiereVuce) {
        this.requiereVuce = requiereVuce;
    }

    public String getEntidadVuce() {
        return entidadVuce;
    }

    public void setEntidadVuce(String entidadVuce) {
        this.entidadVuce = entidadVuce;
    }

    public Boolean getTlcChina() {
        return tlcChina;
    }

    public void setTlcChina(Boolean tlcChina) {
        this.tlcChina = tlcChina;
    }

    public Boolean getAntidumping() {
        return antidumping;
    }

    public void setAntidumping(Boolean antidumping) {
        this.antidumping = antidumping;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(String restricciones) {
        this.restricciones = restricciones;
    }

    public String getProhibiciones() {
        return prohibiciones;
    }

    public void setProhibiciones(String prohibiciones) {
        this.prohibiciones = prohibiciones;
    }

    public Timestamp getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Timestamp fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
