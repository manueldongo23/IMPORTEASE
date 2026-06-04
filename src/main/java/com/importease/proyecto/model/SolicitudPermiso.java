package com.importease.proyecto.model;

import java.sql.Timestamp;
import java.util.List;

public class SolicitudPermiso {
    private int id;
    private int operacionId;
    private int usuarioId;
    private String codigoEntidad;
    private String tipoPermiso;
    private String estado;
    private String numeroSuce;
    private String numeroDocumentoResolutivo;
    private Timestamp fechaCreacion;
    private Timestamp fechaEnvioVuce;
    private Timestamp fechaAprobacion;
    private String observaciones;
    private List<SolicitudPermisoDato> datos;

    public SolicitudPermiso() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOperacionId() { return operacionId; }
    public void setOperacionId(int operacionId) { this.operacionId = operacionId; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getCodigoEntidad() { return codigoEntidad; }
    public void setCodigoEntidad(String codigoEntidad) { this.codigoEntidad = codigoEntidad; }
    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNumeroSuce() { return numeroSuce; }
    public void setNumeroSuce(String numeroSuce) { this.numeroSuce = numeroSuce; }
    public String getNumeroDocumentoResolutivo() { return numeroDocumentoResolutivo; }
    public void setNumeroDocumentoResolutivo(String numeroDocumentoResolutivo) { this.numeroDocumentoResolutivo = numeroDocumentoResolutivo; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Timestamp getFechaEnvioVuce() { return fechaEnvioVuce; }
    public void setFechaEnvioVuce(Timestamp fechaEnvioVuce) { this.fechaEnvioVuce = fechaEnvioVuce; }
    public Timestamp getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(Timestamp fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public List<SolicitudPermisoDato> getDatos() { return datos; }
    public void setDatos(List<SolicitudPermisoDato> datos) { this.datos = datos; }
}


