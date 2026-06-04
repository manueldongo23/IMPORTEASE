package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.UsuarioDAO;

@WebFilter(urlPatterns = {"*.jsp"})
public class SessionFilter implements Filter {

    private UsuarioDAO usuarioDao = new UsuarioDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // ExcepciÃ³n para login.jsp y ruta pÃºblica /login
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        if (uri.equals(contextPath + "/login.jsp") || uri.equals(contextPath + "/login")) {
            chain.doFilter(request, response);
            return;
        }
        // Paginas publicas necesarias para registro y recuperacion de contrasena.
        if (uri.equals(contextPath + "/registro.jsp") || uri.equals(contextPath + "/registro")
                || uri.equals(contextPath + "/recuperar.jsp")
                || uri.equals(contextPath + "/resetear-clave.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        boolean loggedIn = session != null && session.getAttribute("usuarioId") != null;
        
        try {
            if (loggedIn) {
                if (uri.contains("/admin/")) {
                    int usuarioId = (int) session.getAttribute("usuarioId");
                    Usuario u = usuarioDao.buscarPorId(usuarioId);
                    if (u == null || !"admin".equalsIgnoreCase(u.getPerfil())) {
                        res.sendRedirect(req.getContextPath() + "/dashboard.jsp");
                        return;
                    }
                }
                chain.doFilter(request, response);
            } else {
                res.sendRedirect(req.getContextPath() + "/login.jsp");
            }
        } finally {
            TipoCambioService.limpiarThreadLocal();
        }
    }

    @Override
    public void destroy() {}
}

