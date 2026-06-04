package com.importease.proyecto.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "solicitudes_permiso")
public class SolicitudPermisoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "operacion_id", nullable = false)
    private Integer operacionId;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "codigo_entidad", nullable = false)
    private String codigoEntidad;

    @Column(name = "tipo_permiso", nullable = false)
    private String tipoPermiso;

    @Column(name = "estado")
    private String estado;

    @Column(name = "numero_suce")
    private String numeroSuce;

    @Column(name = "numero_documento_resolutivo")
    private String numeroDocumentoResolutivo;

    @Column(name = "fecha_creacion")
    private Timestamp fechaCreacion;

    @Column(name = "fecha_envio_vuce")
    private Timestamp fechaEnvioVuce;

    @Column(name = "fecha_aprobacion")
    private Timestamp fechaAprobacion;

    @Column(name = "observaciones")
    private String observaciones;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOperacionId() {
        return operacionId;
    }

    public void setOperacionId(Integer operacionId) {
        this.operacionId = operacionId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCodigoEntidad() {
        return codigoEntidad;
    }

    public void setCodigoEntidad(String codigoEntidad) {
        this.codigoEntidad = codigoEntidad;
    }

    public String getTipoPermiso() {
        return tipoPermiso;
    }

    public void setTipoPermiso(String tipoPermiso) {
        this.tipoPermiso = tipoPermiso;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNumeroSuce() {
        return numeroSuce;
    }

    public void setNumeroSuce(String numeroSuce) {
        this.numeroSuce = numeroSuce;
    }

    public String getNumeroDocumentoResolutivo() {
        return numeroDocumentoResolutivo;
    }

    public void setNumeroDocumentoResolutivo(String numeroDocumentoResolutivo) {
        this.numeroDocumentoResolutivo = numeroDocumentoResolutivo;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaEnvioVuce() {
        return fechaEnvioVuce;
    }

    public void setFechaEnvioVuce(Timestamp fechaEnvioVuce) {
        this.fechaEnvioVuce = fechaEnvioVuce;
    }

    public Timestamp getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(Timestamp fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
