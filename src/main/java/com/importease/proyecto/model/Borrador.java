package com.importease.proyecto.model;

import java.sql.Timestamp;

public class Borrador {
    private int id;
    private int usuarioId;
    private int pasoActual;
    private String jsonBorrador;
    private Timestamp fechaActualizacion;
    private String estado;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getPasoActual() {
        return pasoActual;
    }

    public void setPasoActual(int pasoActual) {
        this.pasoActual = pasoActual;
    }

    public String getJsonBorrador() {
        return jsonBorrador;
    }

    public void setJsonBorrador(String jsonBorrador) {
        this.jsonBorrador = jsonBorrador;
    }

    public Timestamp getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Timestamp fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
