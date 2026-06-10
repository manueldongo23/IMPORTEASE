package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Filtro global de autenticaciÃ³n.
 * Configurado en web.xml para controlar orden de ejecuciÃ³n.
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String rawUri = req.getRequestURI();
        String uri;
        try {
            uri = new URI(rawUri).normalize().getPath();
        } catch (URISyntaxException e) {
            uri = rawUri;
        }

        if (uri.contains("/api/permiso/")) {
            res.setHeader("X-Deprecated-Endpoint", "true");
            res.setHeader("X-New-Endpoint", req.getContextPath() + uri.substring(uri.indexOf("/api/permiso/")).replace("/api/permiso/", "/api/permisos/"));
        }
        String pathInfo = req.getPathInfo();

        java.util.Set<String> publicPaths = java.util.Set.of(
            "/login", "/logout", "/registro", "/captcha", "/validarRuc", "/recuperar", "/resetear",
            "/monitoreo/health", "/monitoreo/metrics"
        );
        if (pathInfo != null && publicPaths.contains(pathInfo)) {
            chain.doFilter(request, response);
            return;
        }
        String cp = req.getContextPath();
        boolean isPublic = false;
        for (String p : publicPaths) {
            if (uri.equals(cp + "/api" + p) || uri.equals(cp + p) || uri.equals(cp + "/api/usuario" + p)) {
                isPublic = true;
                break;
            }
        }
        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        boolean publicTendencias = "POST".equalsIgnoreCase(req.getMethod())
                && uri.equals(cp + "/api/tendencias/registrar");
        if (publicTendencias) {
            chain.doFilter(request, response);
            return;
        }

        // Validar sesiÃ³n activa
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().print("{\"error\":\"SesiÃ³n no vÃ¡lida. Inicie sesiÃ³n.\"}");
            return;
        }

        // Enforce Read-Only for Consultor role
        String perfil = (String) session.getAttribute("usuarioPerfil");
        String method = req.getMethod();
        if ("consultor".equalsIgnoreCase(perfil)) {
            boolean isWriteOperation = !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method) && !"OPTIONS".equalsIgnoreCase(method);
            if (isWriteOperation) {
                boolean isLogoutOrPrefs = uri.endsWith("/logout") || uri.endsWith("/preferencias");
                if (!isLogoutOrPrefs) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json");
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().print("{\"error\":\"Acceso denegado: El rol Consultor solo tiene permisos de lectura.\"}");
                    return;
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TipoCambioServicio.limpiarThreadLocal();
        }
    }

    @Override
    public void destroy() {}
}

