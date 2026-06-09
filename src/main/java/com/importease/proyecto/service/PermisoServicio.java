package com.importease.proyecto.service;

import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.repository.PermisoRepositorio;
import com.importease.proyecto.service.permisos.PermisoEvaluacionServicio;
import com.importease.proyecto.service.permisos.PermisoExpedienteServicio;
import com.importease.proyecto.service.permisos.PermisoPdfServicio;

import java.util.List;
import java.util.Map;

public class PermisoServicio {
    private final PermisoRepositorio permisoRepositorio;
    private final PermisoEvaluacionServicio evaluationService;
    private final PermisoExpedienteServicio expedienteService;
    private final PermisoPdfServicio pdfServicio;

    public PermisoServicio() {
        this.permisoRepositorio = new PermisoRepositorio();
        this.evaluationService = new PermisoEvaluacionServicio();
        this.expedienteService = new PermisoExpedienteServicio();
        this.pdfServicio = new PermisoPdfServicio();
    }

    public Map<String, Object> evaluarOperacion(int operacionId) {
        return evaluationService.evaluarOperacion(operacionId);
    }

    public Map<String, Object> autorrellenarExpediente(int solicitudId) {
        return expedienteService.autorrellenarExpediente(solicitudId);
    }

    public byte[] generarExpedientePDF(int solicitudId) {
        return pdfServicio.generarExpedientePDF(solicitudId);
    }

    public List<PreguntaPermiso> obtenerPreguntas(String codigoEntidad) {
        return permisoRepositorio.obtenerPreguntasPorEntidad(codigoEntidad);
    }

    public List<DocumentoPermiso> obtenerDocumentos(String codigoEntidad, String tipoPermiso) {
        return permisoRepositorio.obtenerDocumentosPorPermiso(codigoEntidad, tipoPermiso);
    }

    public List<SolicitudPermiso> listarSolicitudesPorOperacion(int operacionId) {
        return permisoRepositorio.listarSolicitudesPorOperacion(operacionId);
    }

    public List<SolicitudPermiso> listarSolicitudesPorUsuario(int usuarioId) {
        return permisoRepositorio.listarSolicitudesPorUsuario(usuarioId);
    }

    public SolicitudPermiso obtenerSolicitud(int id) {
        return permisoRepositorio.obtenerSolicitud(id);
    }

    public List<EntidadControl> listarEntidades() {
        return permisoRepositorio.listarEntidades();
    }

    public Map<String, Object> vincularSuce(int solicitudId, String suce, String resolucion) {
        boolean ok = permisoRepositorio.registrarSuce(solicitudId, suce, resolucion);
        if (ok) {
            return Map.of(
                    "solicitudId", solicitudId,
                    "estado", resolucion != null && !resolucion.trim().isEmpty() ? "APROBADO" : "ENVIADO_A_VUCE",
                    "suce", suce
            );
        }
        return Map.of(
                "solicitudId", solicitudId,
                "estado", "ERROR",
                "suce", null
            );
    }

    public int crearSolicitudManual(SolicitudPermiso sol) {
        if (sol == null) return -1;
        if (sol.getEstado() == null || sol.getEstado().trim().isEmpty()) {
            sol.setEstado("PERMISO_REQUERIDO");
        }
        return permisoRepositorio.crearSolicitud(sol);
    }

    public boolean cambiarEstadoSolicitud(int solicitudId, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) return false;
        return permisoRepositorio.actualizarEstado(solicitudId, nuevoEstado.trim().toUpperCase());
    }

    public boolean guardarRespuestasCuestionario(int operacionId, Map<Integer, String> respuestas) {
        return respuestas != null && !respuestas.isEmpty() && permisoRepositorio.guardarRespuestasPermiso(operacionId, respuestas);
    }
}
