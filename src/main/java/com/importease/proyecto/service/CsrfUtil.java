package com.importease.proyecto.service;

import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpSession;

public class CsrfUtil {
    private static final SecureRandom random = new SecureRandom();

    public static String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static void setToken(HttpSession session) {
        if (session.getAttribute("csrf_token") == null) {
            session.setAttribute("csrf_token", generateToken());
        }
    }

    public static String getToken(HttpSession session) {
        return (String) session.getAttribute("csrf_token");
    }

    public static boolean isValid(HttpSession session, String token) {
        String sessionToken = getToken(session);
        if (sessionToken == null || token == null) return false;
        return java.security.MessageDigest.isEqual(
            sessionToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    public static void rotateToken(HttpSession session) {
        session.setAttribute("csrf_token", generateToken());
    }
}


