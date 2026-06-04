package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.service.PermisoService;

import java.util.List;
import java.util.Map;

public class PermisoQueryService {
    private final PermisoService permisoService;

    public PermisoQueryService() {
        this(new PermisoService());
    }

    public PermisoQueryService(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    public Map<String, Object> evaluarOperacion(int operacionId) {
        return permisoService.evaluarOperacion(operacionId);
    }

    public List<PreguntaPermiso> obtenerPreguntas(String codigoEntidad) {
        return permisoService.obtenerPreguntas(codigoEntidad);
    }

    public List<DocumentoPermiso> obtenerDocumentos(String codigoEntidad, String tipoPermiso) {
        return permisoService.obtenerDocumentos(codigoEntidad, tipoPermiso);
    }

    public List<SolicitudPermiso> listarSolicitudesPorOperacion(int operacionId) {
        return permisoService.listarSolicitudesPorOperacion(operacionId);
    }

    public List<SolicitudPermiso> listarSolicitudesPorUsuario(int usuarioId) {
        return permisoService.listarSolicitudesPorUsuario(usuarioId);
    }

    public SolicitudPermiso obtenerSolicitud(int id) {
        return permisoService.obtenerSolicitud(id);
    }

    public List<EntidadControl> listarEntidades() {
        return permisoService.listarEntidades();
    }

    public byte[] generarExpedientePDF(int solicitudId) {
        return permisoService.generarExpedientePDF(solicitudId);
    }
}
