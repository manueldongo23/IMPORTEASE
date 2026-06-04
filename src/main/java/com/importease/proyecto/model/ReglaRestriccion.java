package com.importease.proyecto.model;

public class ReglaRestriccion {
    private int id;
    private String codigoEntidad;
    private Integer capituloHs;
    private String partidaHs;
    private String palabraClave;
    private String tipoPermiso;
    private String nivelRiesgo;
    private String mensajeUsuario;
    private boolean activo;

    public ReglaRestriccion() {}

    // Getters y Setters (todos)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCodigoEntidad() { return codigoEntidad; }
    public void setCodigoEntidad(String codigoEntidad) { this.codigoEntidad = codigoEntidad; }
    public Integer getCapituloHs() { return capituloHs; }
    public void setCapituloHs(Integer capituloHs) { this.capituloHs = capituloHs; }
    public String getPartidaHs() { return partidaHs; }
    public void setPartidaHs(String partidaHs) { this.partidaHs = partidaHs; }
    public String getPalabraClave() { return palabraClave; }
    public void setPalabraClave(String palabraClave) { this.palabraClave = palabraClave; }
    public String getTipoPermiso() { return tipoPermiso; }
    public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
    public String getMensajeUsuario() { return mensajeUsuario; }
    public void setMensajeUsuario(String mensajeUsuario) { this.mensajeUsuario = mensajeUsuario; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}

