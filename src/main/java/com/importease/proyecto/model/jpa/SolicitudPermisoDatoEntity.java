package com.importease.proyecto.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "solicitud_permiso_datos")
public class SolicitudPermisoDatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "solicitud_permiso_id", nullable = false)
    private Integer solicitudPermisoId;

    @Column(name = "campo", nullable = false)
    private String campo;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "origen_dato", nullable = false)
    private String origenDato;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSolicitudPermisoId() {
        return solicitudPermisoId;
    }

    public void setSolicitudPermisoId(Integer solicitudPermisoId) {
        this.solicitudPermisoId = solicitudPermisoId;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getOrigenDato() {
        return origenDato;
    }

    public void setOrigenDato(String origenDato) {
        this.origenDato = origenDato;
    }
}
