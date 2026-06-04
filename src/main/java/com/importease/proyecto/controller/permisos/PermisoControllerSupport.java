package com.importease.proyecto.controller.permisos;

import com.google.gson.Gson;
import com.importease.proyecto.service.OperacionAuthorizationService;
import com.importease.proyecto.service.permisos.PermisoAuditService;
import com.importease.proyecto.service.permisos.PermisoCommandService;
import com.importease.proyecto.service.permisos.PermisoQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class PermisoControllerSupport {
    private final PermisoQueryService queryService;
    private final PermisoCommandService commandService;
    private final PermisoAuditService auditService;
    private final OperacionAuthorizationService authorizationService;
    private final Gson gson;

    public PermisoControllerSupport(PermisoQueryService queryService, PermisoCommandService commandService, PermisoAuditService auditService, OperacionAuthorizationService authorizationService, Gson gson) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.auditService = auditService;
        this.authorizationService = authorizationService;
        this.gson = gson;
    }

    public PermisoQueryService queryService() { return queryService; }
    public PermisoCommandService commandService() { return commandService; }
    public PermisoAuditService auditService() { return auditService; }
    public OperacionAuthorizationService authorizationService() { return authorizationService; }
    public Gson gson() { return gson; }

    public void markDeprecatedAlias(HttpServletRequest req, HttpServletResponse resp) {
        if ("/api/permiso".equals(req.getServletPath())) {
            resp.setHeader("X-Deprecated-Endpoint", "true");
            resp.setHeader("X-New-Endpoint", req.getContextPath() + "/api/permisos" + (req.getPathInfo() != null ? req.getPathInfo() : ""));
            com.importease.proyecto.service.LoggerUtil.warn("Endpoint deprecated usado: " + req.getRequestURI() + ". Usar /api/permisos/*");
        }
    }

    public Integer resolveUsuarioId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object usuarioIdObj = session.getAttribute("usuarioId");
        if (usuarioIdObj instanceof Integer) return (Integer) usuarioIdObj;
        if (usuarioIdObj instanceof Number) return ((Number) usuarioIdObj).intValue();
        return null;
    }

    public String readBody(HttpServletRequest req) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    public int parseInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public String asString(Object value, String fallback) {
        if (value == null) return fallback;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? fallback : text;
    }
}
