package com.importease.proyecto.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CsrfUtil {

    public static boolean isValidToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        String headerToken = request.getHeader("X-CSRF-TOKEN");
        String sessionToken = (String) session.getAttribute("csrf_token");
        if (sessionToken == null) {
            sessionToken = (String) session.getAttribute("csrfToken");
        }
        if (headerToken == null || sessionToken == null) return false;

        return MessageDigest.isEqual(
            sessionToken.getBytes(StandardCharsets.UTF_8),
            headerToken.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static boolean validateRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!isValidToken(request)) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
