package com.importease.proyecto.config;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheControlFilter implements Filter {
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse resp = (HttpServletResponse) response;
        String path = request instanceof HttpServletRequest
                ? ((HttpServletRequest) request).getRequestURI()
                : "";

        if (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".svg") || path.endsWith(".png") || path.endsWith(".jpg")) {
            resp.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        } else if (path.endsWith(".jsp")) {
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);
        } else if (path.startsWith("/api/")) {
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        }

        chain.doFilter(request, response);
    }

    public void destroy() {}
}
