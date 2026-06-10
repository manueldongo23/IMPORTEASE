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
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ruc", nullable = false, unique = true, length = 20)
    private String ruc;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "buen_contribuyente")
    private Boolean buenContribuyente;

    @Column(name = "perfil")
    private String perfil;

    @Column(name = "fecha_registro")
    private Timestamp fechaRegistro;

    @Column(name = "ultimo_acceso")
    private Timestamp ultimoAcceso;

    @Column(name = "ruc_validado")
    private Boolean rucValidado;

    @Column(name = "fuente_ruc")
    private String fuenteRuc;

    @Column(name = "fecha_validacion_ruc")
    private Timestamp fechaValidacionRuc;

    @Column(name = "estado_ruc")
    private String estadoRuc;

    @Column(name = "condicion_ruc")
    private String condicionRuc;

    @Column(name = "ruc_confianza")
    private BigDecimal rucConfianza;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getBuenContribuyente() {
        return buenContribuyente;
    }

    public void setBuenContribuyente(Boolean buenContribuyente) {
        this.buenContribuyente = buenContribuyente;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Timestamp getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(Timestamp ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Boolean getRucValidado() {
        return rucValidado;
    }

    public void setRucValidado(Boolean rucValidado) {
        this.rucValidado = rucValidado;
    }

    public String getFuenteRuc() {
        return fuenteRuc;
    }

    public void setFuenteRuc(String fuenteRuc) {
        this.fuenteRuc = fuenteRuc;
    }

    public Timestamp getFechaValidacionRuc() {
        return fechaValidacionRuc;
    }

    public void setFechaValidacionRuc(Timestamp fechaValidacionRuc) {
        this.fechaValidacionRuc = fechaValidacionRuc;
    }

    public String getEstadoRuc() {
        return estadoRuc;
    }

    public void setEstadoRuc(String estadoRuc) {
        this.estadoRuc = estadoRuc;
    }

    public String getCondicionRuc() {
        return condicionRuc;
    }

    public void setCondicionRuc(String condicionRuc) {
        this.condicionRuc = condicionRuc;
    }

    public BigDecimal getRucConfianza() {
        return rucConfianza;
    }

    public void setRucConfianza(BigDecimal rucConfianza) {
        this.rucConfianza = rucConfianza;
    }

    @Column(name = "nivel_experiencia")
    private String nivelExperiencia;

    @Column(name = "preferencias")
    private String preferencias;

    public String getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(String nivelExperiencia) {
        this.nivelExperiencia = nivelExperiencia;
    }

    public String getPreferencias() {
        return preferencias;
    }

    public void setPreferencias(String preferencias) {
        this.preferencias = preferencias;
    }
}
