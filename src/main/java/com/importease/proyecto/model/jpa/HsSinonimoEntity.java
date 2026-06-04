package com.importease.proyecto.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "hs_sinonimos")
public class HsSinonimoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "termino_usuario", nullable = false, unique = true)
    private String terminoUsuario;

    @Column(name = "termino_tecnico", nullable = false)
    private String terminoTecnico;

    @Column(name = "codigo_hs_sugerido", nullable = false)
    private String codigoHsSugerido;

    @Column(name = "prioridad")
    private Integer prioridad;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTerminoUsuario() {
        return terminoUsuario;
    }

    public void setTerminoUsuario(String terminoUsuario) {
        this.terminoUsuario = terminoUsuario;
    }

    public String getTerminoTecnico() {
        return terminoTecnico;
    }

    public void setTerminoTecnico(String terminoTecnico) {
        this.terminoTecnico = terminoTecnico;
    }

    public String getCodigoHsSugerido() {
        return codigoHsSugerido;
    }

    public void setCodigoHsSugerido(String codigoHsSugerido) {
        this.codigoHsSugerido = codigoHsSugerido;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
