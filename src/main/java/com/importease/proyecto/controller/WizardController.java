package com.importease.proyecto.controller;

/** @deprecated Replaced by evaluacion.jsp + index.js. GuidedFlowService endpoints no longer called by UI. Will be removed in v2.0. */

import com.google.gson.Gson;
import com.importease.proyecto.dto.CoherenciaIssueDTO;
import com.importease.proyecto.dto.GuidedStepDTO;
import com.importease.proyecto.dto.HealthPanelDTO;
import com.importease.proyecto.dto.NextActionDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionDAO;
import com.importease.proyecto.service.CoherenciaAduaneraService;
import com.importease.proyecto.service.GuidedFlowService;
import com.importease.proyecto.service.HealthPanelService;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.NextActionService;
import com.importease.proyecto.config.DatabaseUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@Deprecated
@WebServlet("/api/wizard/*")
public class WizardController extends HttpServlet {

    private final GuidedFlowService guidedFlowService = new GuidedFlowService();
    private final NextActionService nextActionService = new NextActionService();
    private final HealthPanelService healthPanelService = new HealthPanelService();
    private final CoherenciaAduaneraService coherenciaService = new CoherenciaAduaneraService();
    private final ImportacionDAO importacionDAO = new ImportacionDAO();
    private final Gson gson = new Gson();

    private Integer getAuthenticatedUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(401);
            resp.getWriter().print("{\"error\":\"No autorizado\"}");
            return null;
        }
        return (Integer) session.getAttribute("usuarioId");
    }

    private void requireOwned(Connection con, int usuarioId, int expedienteId) throws SecurityException {
        try {
            Importacion imp = importacionDAO.buscarPorId(con, expedienteId);
            if (imp == null || imp.getUsuarioId() != usuarioId) {
                throw new SecurityException("expediente no pertenece al usuario");
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("error verificando propiedad del expediente");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("X-Deprecated-Endpoint", "Use evaluacion.jsp + /api/importacion/*");
        Integer usuarioId = getAuthenticatedUserId(req, resp);
        if (usuarioId == null) return;

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        String expedienteParam = req.getParameter("expedienteId");

        int expedienteId;
        try {
            expedienteId = Integer.parseInt(expedienteParam);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"expedienteId invalido\"}");
            return;
        }

        try (Connection con = DatabaseUtil.getConnection()) {
            requireOwned(con, usuarioId, expedienteId);

            if ("/pasoActual".equals(path)) {
                GuidedStepDTO paso = guidedFlowService.obtenerPasoActual(expedienteId);
                resp.getWriter().print(gson.toJson(paso));
            } else if ("/siguienteAccion".equals(path)) {
                NextActionDTO accion = nextActionService.calcularSiguienteAccion(expedienteId);
                resp.getWriter().print(gson.toJson(accion));
            } else if ("/salud".equals(path)) {
                HealthPanelDTO salud = healthPanelService.calcularSalud(expedienteId);
                resp.getWriter().print(gson.toJson(salud));
            } else if ("/coherencia".equals(path)) {
                List<CoherenciaIssueDTO> issues = coherenciaService.verificarCoherencia(expedienteId);
                resp.getWriter().print(gson.toJson(issues));
            } else {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (SecurityException e) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para este expediente\"}");
        } catch (Exception e) {
            LoggerUtil.error("Error en doGet de WizardController", e);
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("X-Deprecated-Endpoint", "Use evaluacion.jsp + /api/importacion/*");
        Integer usuarioId = getAuthenticatedUserId(req, resp);
        if (usuarioId == null) return;

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        String expedienteParam = req.getParameter("expedienteId");

        int expedienteId;
        try {
            expedienteId = Integer.parseInt(expedienteParam);
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"expedienteId invalido\"}");
            return;
        }

        try (Connection con = DatabaseUtil.getConnection()) {
            requireOwned(con, usuarioId, expedienteId);

            if ("/avanzar".equals(path)) {
                GuidedStepDTO paso = guidedFlowService.avanzarPaso(expedienteId, usuarioId);
                resp.getWriter().print(gson.toJson(paso));
            } else if ("/retroceder".equals(path)) {
                GuidedStepDTO paso = guidedFlowService.retrocederPaso(expedienteId, usuarioId);
                resp.getWriter().print(gson.toJson(paso));
            } else if ("/bloquear".equals(path)) {
                String motivo = req.getParameter("motivo");
                if (motivo == null || motivo.isBlank()) {
                    motivo = "Bloqueado por el usuario";
                }
                guidedFlowService.bloquearPaso(expedienteId, motivo);
                resp.getWriter().print("{\"success\":true, \"motivo\":\"" + motivo + "\"}");
            } else {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (SecurityException e) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para este expediente\"}");
        } catch (Exception e) {
            LoggerUtil.error("Error en doPost de WizardController", e);
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno del servidor\"}");
        }
    }
}


