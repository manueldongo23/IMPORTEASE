package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            // Validar solo en mÃ©todos mutativos (POST, PUT, DELETE)
            String method = req.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
                
                // Excluir endpoints que operan sin sesiÃ³n activa (no tienen token CSRF)
                String uri = req.getRequestURI();
                String context = req.getContextPath();
                
                // Normalizar de forma canÃ³nica la URI para neutralizar Directory Traversal (../) y bypasses de red
                String normalizedUri = uri;
                try {
                    normalizedUri = new java.net.URI(uri).normalize().getPath();
                } catch (Exception e) { LoggerUtil.error("Error normalizing URI for CSRF check", e); }

                boolean isPublicApi = normalizedUri.equals(context + "/api/usuario/login")
                        || normalizedUri.equals(context + "/api/usuario/registro")
                        || normalizedUri.equals(context + "/api/usuario/recuperar")
                        || normalizedUri.equals(context + "/api/usuario/resetear")
                        || normalizedUri.equals(context + "/api/tendencias/registrar");
                if (isPublicApi) {
                    chain.doFilter(request, response);
                    return;
                }

                // 1. Origin Header Check & Referer Check (Defensa en Profundidad)
                String origin = req.getHeader("Origin");
                String referer = req.getHeader("Referer");
                String serverName = req.getServerName();

                if (origin != null && !origin.isEmpty()) {
                    try {
                        java.net.URI originUri = new java.net.URI(origin);
                        if (!originUri.getHost().equalsIgnoreCase(serverName)) {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().print("{\"error\":\"Violacion de origen CSRF (Origin header mismatch)\"}");
                            return;
                        }
                    } catch (Exception e) {
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.getWriter().print("{\"error\":\"Formato de origen invalido\"}");
                        return;
                    }
                } else if (referer != null && !referer.isEmpty()) {
                    try {
                        java.net.URI refererUri = new java.net.URI(referer);
                        if (!refererUri.getHost().equalsIgnoreCase(serverName)) {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.getWriter().print("{\"error\":\"Violacion de referencia CSRF (Referer header mismatch)\"}");
                            return;
                        }
                    } catch (Exception e) {
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.getWriter().print("{\"error\":\"Formato de referer invalido\"}");
                        return;
                    }
                }

                // 2. Token Check
                HttpSession session = req.getSession(false);
                String clientToken = req.getHeader("X-CSRF-TOKEN");
                if (clientToken == null || clientToken.isEmpty()) {
                    clientToken = req.getParameter("csrf_token");
                }
                if (clientToken == null || clientToken.isEmpty()) {
                    clientToken = req.getParameter("_csrf");
                }
                
                if (session == null || !CsrfUtil.isValid(session, clientToken)) {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.getWriter().print("{\"error\":\"Token CSRF invalido o ausente\"}");
                    return;
                }
            }

            chain.doFilter(request, response);
        } finally {
            TipoCambioService.limpiarThreadLocal();
        }
    }

    @Override
    public void destroy() {}
}

