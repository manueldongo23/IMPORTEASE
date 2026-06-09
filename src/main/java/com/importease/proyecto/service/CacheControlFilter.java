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
        // 30 days cache for static resources
        res.setHeader("Cache-Control", "public, max-age=2592000");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
