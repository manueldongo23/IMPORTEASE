package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.service.PermisoService;

import java.util.Map;

public class PermisoCommandService {
    private final PermisoService permisoService;

    public PermisoCommandService() {
        this(new PermisoService());
    }

    public PermisoCommandService(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    public boolean guardarRespuestasCuestionario(int operacionId, Map<Integer, String> respuestas) {
        return permisoService.guardarRespuestasCuestionario(operacionId, respuestas);
    }

    public int crearSolicitudManual(SolicitudPermiso solicitud) {
        return permisoService.crearSolicitudManual(solicitud);
    }

    public boolean cambiarEstadoSolicitud(int solicitudId, String nuevoEstado) {
        return permisoService.cambiarEstadoSolicitud(solicitudId, nuevoEstado);
    }

    public Map<String, Object> autorrellenarExpediente(int solicitudId) {
        return permisoService.autorrellenarExpediente(solicitudId);
    }

    public Map<String, Object> vincularSuce(int solicitudId, String suce, String resolucion) {
        return permisoService.vincularSuce(solicitudId, suce, resolucion);
    }
}
