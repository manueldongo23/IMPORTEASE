package com.importease.proyecto.dto;

import java.math.BigDecimal;

public class PasoGuiadoDTO {
    private int step;
    private String nombre;
    private String descripcion;
    private String estado;
    private String mensajeBloqueo;
    private BigDecimal porcentajeGlobal;

    public PasoGuiadoDTO() {}

    public PasoGuiadoDTO(int step, String nombre, String descripcion, String estado, String mensajeBloqueo, BigDecimal porcentajeGlobal) {
        this.step = step;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.mensajeBloqueo = mensajeBloqueo;
        this.porcentajeGlobal = porcentajeGlobal;
    }

    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMensajeBloqueo() { return mensajeBloqueo; }
    public void setMensajeBloqueo(String mensajeBloqueo) { this.mensajeBloqueo = mensajeBloqueo; }
    public BigDecimal getPorcentajeGlobal() { return porcentajeGlobal; }
    public void setPorcentajeGlobal(BigDecimal porcentajeGlobal) { this.porcentajeGlobal = porcentajeGlobal; }
}

