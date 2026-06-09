package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Filtro global que aÃ±ade headers de seguridad HTTP a TODAS las respuestas.
 */
public class SecurityHeadersFilter implements Filter {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest httpRequest = request instanceof HttpServletRequest
                ? (HttpServletRequest) request
                : null;
        String cspNonce = generateNonce();
        if (httpRequest != null) {
            httpRequest.setAttribute("csp_nonce", cspNonce);
        }

        // Prevenir Clickjacking
        res.setHeader("X-Frame-Options", "DENY");

        // Prevenir MIME sniffing
        res.setHeader("X-Content-Type-Options", "nosniff");

        // Deshabilitar protecciÃ³n XSS legacy (mitigaciÃ³n de comportamiento problemÃ¡tico de navegadores antiguos)
        res.setHeader("X-XSS-Protection", "0");

        // Cabeceras Anti-CachÃ© estrictas para datos aduaneros/financieros
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");

        // Referrer Policy
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions Policy (deshabilitar APIs innecesarias)
        res.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Content Security Policy
        // script-src governs inline event handlers (onclick, etc.) - 'unsafe-inline' needed since nonce can't be applied to inline handlers
        // script-src-elem governs <script> elements - nonce required, 'unsafe-inline' omitted (ignored when nonce present)
        res.setHeader("Content-Security-Policy",
            "default-src 'self'; " +
            "base-uri 'self'; " +
            "object-src 'none'; " +
            "form-action 'self'; " +
            "script-src 'self' 'unsafe-inline'; " +
            "script-src-elem 'self' 'nonce-" + cspNonce + "' https://cdn.jsdelivr.net https://unpkg.com; " +
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://unpkg.com; " +
            "style-src-elem 'self' 'unsafe-inline' https://fonts.googleapis.com https://unpkg.com; " +
            "font-src 'self' https://fonts.gstatic.com; " +
            "img-src 'self' data: https://www.vuce.gob.pe https://orientacion.sunat.gob.pe; " +
            "connect-src 'self' https://estadisticas.bcrp.gob.pe; " +
            "frame-ancestors 'none';");

        // HSTS solo cuando la solicitud llega por HTTPS. No se envia en localhost HTTP para no romper demo local.
        if (isHttps(httpRequest)) {
            res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }

        // Establecer codificaciÃ³n de caracteres global UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Interceptador para inyectar SameSite=Lax en cookies de sesiÃ³n
        javax.servlet.http.HttpServletResponseWrapper wrappedResponse = new javax.servlet.http.HttpServletResponseWrapper(res) {
            @Override
            public void addHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value != null) {
                    if (!value.toLowerCase().contains("samesite")) {
                        value += "; SameSite=Lax";
                    }
                    if (isHttps(httpRequest) && !value.toLowerCase().contains("secure")) {
                        value += "; Secure";
                    }
                }
                super.addHeader(name, value);
            }
            @Override
            public void setHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value != null) {
                    if (!value.toLowerCase().contains("samesite")) {
                        value += "; SameSite=Lax";
                    }
                    if (isHttps(httpRequest) && !value.toLowerCase().contains("secure")) {
                        value += "; Secure";
                    }
                }
                super.setHeader(name, value);
            }
        };

        chain.doFilter(request, wrappedResponse);
    }

    @Override
    public void destroy() {}


    private static boolean isHttps(HttpServletRequest request) {
        if (request == null) return false;
        if (request.isSecure()) return true;
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return forwardedProto != null && "https".equalsIgnoreCase(forwardedProto.trim());
    }

    private static String generateNonce() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
}


