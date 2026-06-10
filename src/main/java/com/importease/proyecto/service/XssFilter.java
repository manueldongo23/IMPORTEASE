package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
        } finally {
            TipoCambioServicio.limpiarThreadLocal();
        }
    }

    @Override
    public void destroy() {
    }
}
