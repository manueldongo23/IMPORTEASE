package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CorreoRecuperacionServicioTest {

    @Test
    public void buildResetLinkEncodesEmailAndToken() {
        String url = CorreoRecuperacionServicio.buildResetLink(
                "http://localhost:8084/",
                "/importease",
                "md2023076842@virtual.upt.pe",
                "abc-123"
        );

        assertTrue(url.startsWith("http://localhost:8084/importease/resetear-clave.jsp"));
        assertTrue(url.contains("email=md2023076842%40virtual.upt.pe"));
        assertTrue(url.contains("token=abc-123"));
    }

    @Test
    public void buildHtmlContainsCallToActionAndLink() {
        String html = CorreoRecuperacionServicio.buildHtml("Manuel", "http://localhost/resetear-clave.jsp?email=a%40b.com&token=123");

        assertTrue(html.contains("Restablecer contrasena"));
        assertTrue(html.contains("http://localhost/resetear-clave.jsp?email=a%40b.com&amp;token=123"));
        assertTrue(html.contains("Manuel"));
    }
}

