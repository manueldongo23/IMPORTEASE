package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheControlFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;
        String uri = ((javax.servlet.http.HttpServletRequest) request).getRequestURI();
        if (uri.contains("/api/")) {
            res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        } else {
            res.setHeader("Cache-Control", "public, max-age=2592000");
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TipoCambioServicio.limpiarThreadLocal();
        }
    }

    @Override
    public void destroy() {
    }
}
