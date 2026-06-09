package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

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

        String uri = req.getRequestURI();
        if (uri.contains("/api/permiso/")) {
            res.setHeader("X-Deprecated-Endpoint", "true");
            res.setHeader("X-New-Endpoint", req.getContextPath() + uri.substring(uri.indexOf("/api/permiso/")).replace("/api/permiso/", "/api/permisos/"));
        }
        String pathInfo = req.getPathInfo(); // MÃ¡s preciso que endsWith

        // Endpoints pÃºblicos que NO requieren autenticaciÃ³n (matching exacto)
        java.util.Set<String> publicPaths = java.util.Set.of(
            "/login", "/registro", "/captcha", "/validarRuc", "/recuperar", "/resetear"
        );
        if (pathInfo != null && publicPaths.contains(pathInfo)) {
            chain.doFilter(request, response);
            return;
        }
        // Fallback: URI matching para rutas sin pathInfo (ej: /captcha servlet)
        for (String pub : publicPaths) {
            String allowedApiSuffix = "/api/usuario" + pub;
            if (uri.endsWith(allowedApiSuffix) || uri.equals(req.getContextPath() + pub)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Endpoint publico limitado para registrar eventos anonimos. Evita abrir todo /api/tendencias/*.
        boolean publicTendencias = "POST".equalsIgnoreCase(req.getMethod())
                && uri.equals(req.getContextPath() + "/api/tendencias/registrar");
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

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}


