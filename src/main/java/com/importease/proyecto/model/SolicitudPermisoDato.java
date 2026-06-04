package com.importease.proyecto.model;

public class SolicitudPermisoDato {
    private int id;
    private int solicitudPermisoId;
    private String campo;
    private String valor;
    private String origenDato;

    public SolicitudPermisoDato() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSolicitudPermisoId() { return solicitudPermisoId; }
    public void setSolicitudPermisoId(int solicitudPermisoId) { this.solicitudPermisoId = solicitudPermisoId; }
    public String getCampo() { return campo; }
    public void setCampo(String campo) { this.campo = campo; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getOrigenDato() { return origenDato; }
    public void setOrigenDato(String origenDato) { this.origenDato = origenDato; }
}

