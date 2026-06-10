package com.importease.proyecto.model;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UsuarioDTO {
    private int id;
    private String ruc;
    private String razonSocial;
    private String email;
    private boolean buenContribuyente;
    private String perfil;
    private Timestamp fechaRegistro;
    private Timestamp ultimoAcceso;
    private boolean rucValidado;
    private String fuenteRuc;
    private String estadoRuc;
    private String condicionRuc;
    private double rucConfianza;
    private String nivelExperiencia;
    private String preferencias;

    public UsuarioDTO(Usuario u) {
        this.id = u.getId();
        this.ruc = u.getRuc();
        this.razonSocial = u.getRazonSocial();
        this.email = u.getEmail();
        this.buenContribuyente = u.isBuenContribuyente();
        this.perfil = u.getPerfil();
        this.fechaRegistro = u.getFechaRegistro();
        this.ultimoAcceso = u.getUltimoAcceso();
        this.rucValidado = u.isRucValidado();
        this.fuenteRuc = u.getFuenteRuc();
        this.estadoRuc = u.getEstadoRuc();
        this.condicionRuc = u.getCondicionRuc();
        this.rucConfianza = u.getRucConfianza();
        this.nivelExperiencia = u.getNivelExperiencia();
        this.preferencias = u.getPreferencias();
    }

    public int getId() { return id; }
    public String getRuc() { return ruc; }
    public String getRazonSocial() { return razonSocial; }
    public String getEmail() { return email; }
    public boolean isBuenContribuyente() { return buenContribuyente; }
    public String getPerfil() { return perfil; }
    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public Timestamp getUltimoAcceso() { return ultimoAcceso; }
    public boolean isRucValidado() { return rucValidado; }
    @JsonIgnore
    public String getFuenteRuc() { return fuenteRuc; }
    @JsonIgnore
    public String getEstadoRuc() { return estadoRuc; }
    @JsonIgnore
    public String getCondicionRuc() { return condicionRuc; }
    @JsonIgnore
    public double getRucConfianza() { return rucConfianza; }

    public String getNivelExperiencia() { return nivelExperiencia; }
    public String getPreferencias() { return preferencias; }
}
