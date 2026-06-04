package com.importease.proyecto.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "reglas_restriccion")
public class ReglaRestriccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_entidad", nullable = false, length = 20)
    private String codigoEntidad;

    @Column(name = "capitulo_hs")
    private Integer capituloHs;

    @Column(name = "partida_hs")
    private String partidaHs;

    @Column(name = "palabra_clave")
    private String palabraClave;

    @Column(name = "tipo_permiso", nullable = false)
    private String tipoPermiso;

    @Column(name = "nivel_riesgo")
    private String nivelRiesgo;

    @Column(name = "mensaje_usuario")
    private String mensajeUsuario;

    @Column(name = "activo")
    private Boolean activo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoEntidad() {
        return codigoEntidad;
    }

    public void setCodigoEntidad(String codigoEntidad) {
        this.codigoEntidad = codigoEntidad;
    }

    public Integer getCapituloHs() {
        return capituloHs;
    }

    public void setCapituloHs(Integer capituloHs) {
        this.capituloHs = capituloHs;
    }

    public String getPartidaHs() {
        return partidaHs;
    }

    public void setPartidaHs(String partidaHs) {
        this.partidaHs = partidaHs;
    }

    public String getPalabraClave() {
        return palabraClave;
    }

    public void setPalabraClave(String palabraClave) {
        this.palabraClave = palabraClave;
    }

    public String getTipoPermiso() {
        return tipoPermiso;
    }

    public void setTipoPermiso(String tipoPermiso) {
        this.tipoPermiso = tipoPermiso;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
