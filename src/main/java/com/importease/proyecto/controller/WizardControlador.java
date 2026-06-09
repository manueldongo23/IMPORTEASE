package com.importease.proyecto.controller;

/** @deprecated Replaced by evaluacion.jsp + index.js. FlujoGuiadoServicio endpoints no longer called by UI. Will be removed in v2.0. */

import com.google.gson.Gson;
import com.importease.proyecto.dto.IncidenciaCoherenciaDTO;
import com.importease.proyecto.dto.PasoGuiadoDTO;
import com.importease.proyecto.dto.PanelSaludDTO;
import com.importease.proyecto.dto.SiguienteAccionDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.service.CoherenciaAduaneraServicio;
import com.importease.proyecto.service.FlujoGuiadoServicio;
import com.importease.proyecto.service.SaludPanelServicio;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.SiguienteAccionServicio;
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
public class WizardControlador extends HttpServlet {

    private final FlujoGuiadoServicio flujoGuiadoServicio = new FlujoGuiadoServicio();
    private final SiguienteAccionServicio siguienteAccionServicio = new SiguienteAccionServicio();
    private final SaludPanelServicio saludPanelServicio = new SaludPanelServicio();
    private final CoherenciaAduaneraServicio coherenciaService = new CoherenciaAduaneraServicio();
    private final ImportacionRepositorio importacionRepositorio = new ImportacionRepositorio();
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
            Importacion imp = importacionRepositorio.buscarPorId(con, expedienteId);
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
                PasoGuiadoDTO paso = flujoGuiadoServicio.obtenerPasoActual(expedienteId);
                resp.getWriter().print(gson.toJson(paso));
            } else if ("/siguienteAccion".equals(path)) {
                SiguienteAccionDTO accion = siguienteAccionServicio.calcularSiguienteAccion(expedienteId);
                resp.getWriter().print(gson.toJson(accion));
            } else if ("/salud".equals(path)) {
                PanelSaludDTO salud = saludPanelServicio.calcularSalud(expedienteId);
                resp.getWriter().print(gson.toJson(salud));
            } else if ("/coherencia".equals(path)) {
                List<IncidenciaCoherenciaDTO> issues = coherenciaService.verificarCoherencia(expedienteId);
                resp.getWriter().print(gson.toJson(issues));
            } else {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (SecurityException e) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para este expediente\"}");
        } catch (Exception e) {
            LoggerUtil.error("Error en doGet de WizardControlador", e);
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
                PasoGuiadoDTO paso = flujoGuiadoServicio.avanzarPaso(expedienteId, usuarioId);
                resp.getWriter().print(gson.toJson(paso));
            } else if ("/retroceder".equals(path)) {
                PasoGuiadoDTO paso = flujoGuiadoServicio.retrocederPaso(expedienteId, usuarioId);
                resp.getWriter().print(gson.toJson(paso));
            } else if ("/bloquear".equals(path)) {
                String motivo = req.getParameter("motivo");
                if (motivo == null || motivo.isBlank()) {
                    motivo = "Bloqueado por el usuario";
                }
                flujoGuiadoServicio.bloquearPaso(expedienteId, motivo);
                String escapedMotivo = motivo.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
                resp.getWriter().print("{\"success\":true, \"motivo\":\"" + escapedMotivo + "\"}");
            } else {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (SecurityException e) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para este expediente\"}");
        } catch (Exception e) {
            LoggerUtil.error("Error en doPost de WizardControlador", e);
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno del servidor\"}");
        }
    }
}
