package com.importease.proyecto.dto;

import java.math.BigDecimal;
import java.util.List;

public class PanelSaludDTO {
    private BigDecimal porcentajeCompletitud;
    private String riesgoDocumental;
    private String riesgoNormativo;
    private String riesgoPlazo;
    private String estadoManifiesto;
    private String estadoPermisos;
    private String estadoPreDam;
    private List<String> documentosFaltantes;
    private SiguienteAccionDTO siguienteAccion;
    private String estadoDamSunat;
    private String estadoDamOrigen;
    private String ultimaActualizacionDam;

    public PanelSaludDTO() {}

    public BigDecimal getPorcentajeCompletitud() { return porcentajeCompletitud; }
    public void setPorcentajeCompletitud(BigDecimal porcentajeCompletitud) { this.porcentajeCompletitud = porcentajeCompletitud; }
    public String getRiesgoDocumental() { return riesgoDocumental; }
    public void setRiesgoDocumental(String riesgoDocumental) { this.riesgoDocumental = riesgoDocumental; }
    public String getRiesgoNormativo() { return riesgoNormativo; }
    public void setRiesgoNormativo(String riesgoNormativo) { this.riesgoNormativo = riesgoNormativo; }
    public String getRiesgoPlazo() { return riesgoPlazo; }
    public void setRiesgoPlazo(String riesgoPlazo) { this.riesgoPlazo = riesgoPlazo; }
    public String getEstadoManifiesto() { return estadoManifiesto; }
    public void setEstadoManifiesto(String estadoManifiesto) { this.estadoManifiesto = estadoManifiesto; }
    public String getEstadoPermisos() { return estadoPermisos; }
    public void setEstadoPermisos(String estadoPermisos) { this.estadoPermisos = estadoPermisos; }
    public String getEstadoPreDam() { return estadoPreDam; }
    public void setEstadoPreDam(String estadoPreDam) { this.estadoPreDam = estadoPreDam; }
    public List<String> getDocumentosFaltantes() { return documentosFaltantes; }
    public void setDocumentosFaltantes(List<String> documentosFaltantes) { this.documentosFaltantes = documentosFaltantes; }
    public SiguienteAccionDTO getSiguienteAccion() { return siguienteAccion; }
    public void setSiguienteAccion(SiguienteAccionDTO siguienteAccion) { this.siguienteAccion = siguienteAccion; }
    public String getEstadoDamSunat() { return estadoDamSunat; }
    public void setEstadoDamSunat(String estadoDamSunat) { this.estadoDamSunat = estadoDamSunat; }
    public String getEstadoDamOrigen() { return estadoDamOrigen; }
    public void setEstadoDamOrigen(String estadoDamOrigen) { this.estadoDamOrigen = estadoDamOrigen; }
    public String getUltimaActualizacionDam() { return ultimaActualizacionDam; }
    public void setUltimaActualizacionDam(String ultimaActualizacionDam) { this.ultimaActualizacionDam = ultimaActualizacionDam; }
}


