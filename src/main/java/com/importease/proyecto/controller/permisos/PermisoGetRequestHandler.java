package com.importease.proyecto.controller.permisos;

import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.SolicitudPermiso;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PermisoGetRequestHandler {
    private final PermisoControllerSupport support;

    public PermisoGetRequestHandler(PermisoControllerSupport support) {
        this.support = support;
    }

    public void handle(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        String path = req.getPathInfo();
        if ("/evaluar".equals(path)) {
            handleEvaluar(req, resp, usuarioId);
        } else if ("/preguntas".equals(path)) {
            handlePreguntas(req, resp);
        } else if ("/listar".equals(path) || "/listarSolicitudes".equals(path)) {
            handleListar(req, resp, usuarioId);
        } else if ("/solicitud".equals(path) || "/detalle".equals(path)) {
            handleObtenerSolicitud(req, resp, usuarioId);
        } else if ("/entidades".equals(path)) {
            resp.getWriter().print(support.gson().toJson(support.queryService().listarEntidades()));
        } else if ("/documentos".equals(path)) {
            handleDocumentos(req, resp);
        } else if ("/pdf".equals(path) || "/descargarExpedientePdf".equals(path)) {
            handleDescargarPdf(req, resp, usuarioId);
        } else {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    private void handleEvaluar(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        int operacionId = support.parseInt(req.getParameter("operacionId"), -1);
        if (operacionId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"ID de operacion invalido\"}");
            return;
        }
        if (!support.authorizationService().isOperacionOwnedByUser(operacionId, usuarioId)) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para evaluar esta operacion\"}");
            return;
        }
        resp.getWriter().print(support.gson().toJson(support.queryService().evaluarOperacion(operacionId)));
    }

    private void handlePreguntas(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String entidad = req.getParameter("entidad");
        if (entidad == null || entidad.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Parametro entidad requerido\"}");
            return;
        }
        List<PreguntaPermiso> preguntas = support.queryService().obtenerPreguntas(entidad);
        resp.getWriter().print(support.gson().toJson(preguntas));
    }

    private void handleListar(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        String operacionIdStr = req.getParameter("operacionId");
        List<SolicitudPermiso> solicitudes;
        if (operacionIdStr != null && !operacionIdStr.isEmpty()) {
            int operacionId = support.parseInt(operacionIdStr, -1);
            if (operacionId <= 0) {
                resp.setStatus(400);
                resp.getWriter().print("{\"error\":\"ID de operacion invalido\"}");
                return;
            }
            if (!support.authorizationService().isOperacionOwnedByUser(operacionId, usuarioId)) {
                resp.setStatus(403);
                resp.getWriter().print("{\"error\":\"No autorizado para listar solicitudes de esta operacion\"}");
                return;
            }
            solicitudes = support.queryService().listarSolicitudesPorOperacion(operacionId);
        } else {
            solicitudes = support.queryService().listarSolicitudesPorUsuario(usuarioId);
        }
        resp.getWriter().print(support.gson().toJson(solicitudes));
    }

    private void handleObtenerSolicitud(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        int solicitudId = support.parseInt(req.getParameter("id"), -1);
        if (solicitudId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Parametro id invalido\"}");
            return;
        }

        SolicitudPermiso sol = support.queryService().obtenerSolicitud(solicitudId);
        if (sol == null) {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Solicitud no encontrada\"}");
            return;
        }
        if (sol.getUsuarioId() != usuarioId) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para ver esta solicitud\"}");
            return;
        }
        resp.getWriter().print(support.gson().toJson(sol));
    }

    private void handleDescargarPdf(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        String solicitudIdStr = req.getParameter("id");
        if (solicitudIdStr == null || solicitudIdStr.isEmpty()) solicitudIdStr = req.getParameter("solicitudId");
        if (solicitudIdStr == null || solicitudIdStr.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Parametro id requerido\"}");
            return;
        }

        int solId = support.parseInt(solicitudIdStr, -1);
        if (solId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Parametro id invalido\"}");
            return;
        }

        SolicitudPermiso sol = support.queryService().obtenerSolicitud(solId);
        if (sol == null) {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Solicitud no encontrada\"}");
            return;
        }
        if (sol.getUsuarioId() != usuarioId) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para descargar este expediente\"}");
            return;
        }

        byte[] pdf = support.queryService().generarExpedientePDF(solId);
        if (pdf == null || pdf.length == 0) {
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error al generar PDF\"}");
            return;
        }
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"expediente_permiso_" + solId + ".pdf\"");
        resp.setContentLength(pdf.length);
        try (OutputStream os = resp.getOutputStream()) {
            os.write(pdf);
            os.flush();
        }
    }

    private void handleDocumentos(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String entidad = req.getParameter("entidad");
        String tipoPermiso = req.getParameter("tipoPermiso");
        if (entidad == null || tipoPermiso == null) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Parametros entidad y tipoPermiso requeridos\"}");
            return;
        }
        resp.getWriter().print(support.gson().toJson(support.queryService().obtenerDocumentos(entidad, tipoPermiso)));
    }
}
