package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.controller.permisos.PermisoControladorSoporte;
import com.importease.proyecto.controller.permisos.PermisoManejadorPeticionGet;
import com.importease.proyecto.controller.permisos.PermisoManejadorPeticionPost;
import com.importease.proyecto.service.OperacionAutorizacionServicio;
import com.importease.proyecto.service.permisos.PermisoAuditoriaServicio;
import com.importease.proyecto.service.permisos.PermisoComandoServicio;
import com.importease.proyecto.service.permisos.PermisoConsultaServicio;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/permisos/*", "/api/permiso/*"})
public class PermisoControlador extends HttpServlet {
    private final PermisoControladorSoporte support = new PermisoControladorSoporte(
            new PermisoConsultaServicio(),
            new PermisoComandoServicio(),
            new PermisoAuditoriaServicio(),
            new OperacionAutorizacionServicio(),
            new Gson()
    );
    private final PermisoManejadorPeticionGet getHandler = new PermisoManejadorPeticionGet(support);
    private final PermisoManejadorPeticionPost postHandler = new PermisoManejadorPeticionPost(support);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(req, resp);
        Integer usuarioId = support.resolveUsuarioId(req);
        if (usuarioId == null) {
            resp.setStatus(401);
            resp.getWriter().print("{\"error\":\"No autenticado\"}");
            return;
        }

        try {
            getHandler.handle(req, resp, usuarioId);
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en PermisoControlador GET", e);
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno del servidor\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        prepareResponse(req, resp);
        Integer usuarioId = support.resolveUsuarioId(req);
        if (usuarioId == null) {
            resp.setStatus(401);
            resp.getWriter().print("{\"error\":\"No autenticado\"}");
            return;
        }

        try {
            postHandler.handle(req, resp, usuarioId);
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en PermisoControlador POST", e);
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno del servidor\"}");
        }
    }

    private void prepareResponse(HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        support.markDeprecatedAlias(req, resp);
    }
}
