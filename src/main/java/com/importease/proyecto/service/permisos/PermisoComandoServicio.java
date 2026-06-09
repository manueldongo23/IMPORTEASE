package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.service.PermisoServicio;

import java.util.Map;

public class PermisoComandoServicio {
    private final PermisoServicio permisoServicio;

    public PermisoComandoServicio() {
        this(new PermisoServicio());
    }

    public PermisoComandoServicio(PermisoServicio permisoServicio) {
        this.permisoServicio = permisoServicio;
    }

    public boolean guardarRespuestasCuestionario(int operacionId, Map<Integer, String> respuestas) {
        return permisoServicio.guardarRespuestasCuestionario(operacionId, respuestas);
    }

    public int crearSolicitudManual(SolicitudPermiso solicitud) {
        return permisoServicio.crearSolicitudManual(solicitud);
    }

    public boolean cambiarEstadoSolicitud(int solicitudId, String nuevoEstado) {
        return permisoServicio.cambiarEstadoSolicitud(solicitudId, nuevoEstado);
    }

    public Map<String, Object> autorrellenarExpediente(int solicitudId) {
        return permisoServicio.autorrellenarExpediente(solicitudId);
    }

    public Map<String, Object> vincularSuce(int solicitudId, String suce, String resolucion) {
        return permisoServicio.vincularSuce(solicitudId, suce, resolucion);
    }
}
