package com.importease.proyecto.model;

import java.sql.Timestamp;

public class Usuario {
    private int id;
    private String ruc;
    private String razonSocial;
    private String email;
    private String passwordHash;
    private boolean buenContribuyente;
    private String perfil;
    private Timestamp fechaRegistro;
    private Timestamp ultimoAcceso;
    private boolean rucValidado;
    private String fuenteRuc;
    private Timestamp fechaValidacionRuc;
    private String estadoRuc;
    private String condicionRuc;
    private double rucConfianza;

    public Usuario() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isBuenContribuyente() { return buenContribuyente; }
    public void setBuenContribuyente(boolean buenContribuyente) { this.buenContribuyente = buenContribuyente; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public Timestamp getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(Timestamp ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    public boolean isRucValidado() { return rucValidado; }
    public void setRucValidado(boolean rucValidado) { this.rucValidado = rucValidado; }
    public String getFuenteRuc() { return fuenteRuc; }
    public void setFuenteRuc(String fuenteRuc) { this.fuenteRuc = fuenteRuc; }
    public Timestamp getFechaValidacionRuc() { return fechaValidacionRuc; }
    public void setFechaValidacionRuc(Timestamp fechaValidacionRuc) { this.fechaValidacionRuc = fechaValidacionRuc; }
    public String getEstadoRuc() { return estadoRuc; }
    public void setEstadoRuc(String estadoRuc) { this.estadoRuc = estadoRuc; }
    public String getCondicionRuc() { return condicionRuc; }
    public void setCondicionRuc(String condicionRuc) { this.condicionRuc = condicionRuc; }
    public double getRucConfianza() { return rucConfianza; }
    public void setRucConfianza(double rucConfianza) { this.rucConfianza = rucConfianza; }
}

