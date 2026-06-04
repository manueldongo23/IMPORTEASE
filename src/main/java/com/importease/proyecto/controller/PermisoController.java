package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.controller.permisos.PermisoControllerSupport;
import com.importease.proyecto.controller.permisos.PermisoGetRequestHandler;
import com.importease.proyecto.controller.permisos.PermisoPostRequestHandler;
import com.importease.proyecto.service.OperacionAuthorizationService;
import com.importease.proyecto.service.permisos.PermisoAuditService;
import com.importease.proyecto.service.permisos.PermisoCommandService;
import com.importease.proyecto.service.permisos.PermisoQueryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/api/permisos/*", "/api/permiso/*"})
public class PermisoController extends HttpServlet {
    private final PermisoControllerSupport support = new PermisoControllerSupport(
            new PermisoQueryService(),
            new PermisoCommandService(),
            new PermisoAuditService(),
            new OperacionAuthorizationService(),
            new Gson()
    );
    private final PermisoGetRequestHandler getHandler = new PermisoGetRequestHandler(support);
    private final PermisoPostRequestHandler postHandler = new PermisoPostRequestHandler(support);

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
            com.importease.proyecto.service.LoggerUtil.error("Error en PermisoController GET", e);
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
            com.importease.proyecto.service.LoggerUtil.error("Error en PermisoController POST", e);
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
