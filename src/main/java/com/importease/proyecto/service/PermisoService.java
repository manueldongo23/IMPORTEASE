package com.importease.proyecto.service;

import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.repository.PermisoDAO;
import com.importease.proyecto.service.permisos.PermisoEvaluationService;
import com.importease.proyecto.service.permisos.PermisoExpedienteService;
import com.importease.proyecto.service.permisos.PermisoPdfService;

import java.util.List;
import java.util.Map;

public class PermisoService {
    private final PermisoDAO permisoDAO;
    private final PermisoEvaluationService evaluationService;
    private final PermisoExpedienteService expedienteService;
    private final PermisoPdfService pdfService;

    public PermisoService() {
        this.permisoDAO = new PermisoDAO();
        this.evaluationService = new PermisoEvaluationService();
        this.expedienteService = new PermisoExpedienteService();
        this.pdfService = new PermisoPdfService();
    }

    public Map<String, Object> evaluarOperacion(int operacionId) {
        return evaluationService.evaluarOperacion(operacionId);
    }

    public Map<String, Object> autorrellenarExpediente(int solicitudId) {
        return expedienteService.autorrellenarExpediente(solicitudId);
    }

    public byte[] generarExpedientePDF(int solicitudId) {
        return pdfService.generarExpedientePDF(solicitudId);
    }

    public List<PreguntaPermiso> obtenerPreguntas(String codigoEntidad) {
        return permisoDAO.obtenerPreguntasPorEntidad(codigoEntidad);
    }

    public List<DocumentoPermiso> obtenerDocumentos(String codigoEntidad, String tipoPermiso) {
        return permisoDAO.obtenerDocumentosPorPermiso(codigoEntidad, tipoPermiso);
    }

    public List<SolicitudPermiso> listarSolicitudesPorOperacion(int operacionId) {
        return permisoDAO.listarSolicitudesPorOperacion(operacionId);
    }

    public List<SolicitudPermiso> listarSolicitudesPorUsuario(int usuarioId) {
        return permisoDAO.listarSolicitudesPorUsuario(usuarioId);
    }

    public SolicitudPermiso obtenerSolicitud(int id) {
        return permisoDAO.obtenerSolicitud(id);
    }

    public List<EntidadControl> listarEntidades() {
        return permisoDAO.listarEntidades();
    }

    public Map<String, Object> vincularSuce(int solicitudId, String suce, String resolucion) {
        boolean ok = permisoDAO.registrarSuce(solicitudId, suce, resolucion);
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
        return permisoDAO.crearSolicitud(sol);
    }

    public boolean cambiarEstadoSolicitud(int solicitudId, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) return false;
        return permisoDAO.actualizarEstado(solicitudId, nuevoEstado.trim().toUpperCase());
    }

    public boolean guardarRespuestasCuestionario(int operacionId, Map<Integer, String> respuestas) {
        return respuestas != null && !respuestas.isEmpty() && permisoDAO.guardarRespuestasPermiso(operacionId, respuestas);
    }
}
