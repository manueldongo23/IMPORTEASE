package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.service.PermisoServicio;

import java.util.List;
import java.util.Map;

public class PermisoConsultaServicio {
    private final PermisoServicio permisoServicio;

    public PermisoConsultaServicio() {
        this(new PermisoServicio());
    }

    public PermisoConsultaServicio(PermisoServicio permisoServicio) {
        this.permisoServicio = permisoServicio;
    }

    public Map<String, Object> evaluarOperacion(int operacionId) {
        return permisoServicio.evaluarOperacion(operacionId);
    }

    public List<PreguntaPermiso> obtenerPreguntas(String codigoEntidad) {
        return permisoServicio.obtenerPreguntas(codigoEntidad);
    }

    public List<DocumentoPermiso> obtenerDocumentos(String codigoEntidad, String tipoPermiso) {
        return permisoServicio.obtenerDocumentos(codigoEntidad, tipoPermiso);
    }

    public List<SolicitudPermiso> listarSolicitudesPorOperacion(int operacionId) {
        return permisoServicio.listarSolicitudesPorOperacion(operacionId);
    }

    public List<SolicitudPermiso> listarSolicitudesPorUsuario(int usuarioId) {
        return permisoServicio.listarSolicitudesPorUsuario(usuarioId);
    }

    public SolicitudPermiso obtenerSolicitud(int id) {
        return permisoServicio.obtenerSolicitud(id);
    }

    public List<EntidadControl> listarEntidades() {
        return permisoServicio.listarEntidades();
    }

    public byte[] generarExpedientePDF(int solicitudId) {
        return permisoServicio.generarExpedientePDF(solicitudId);
    }
}
