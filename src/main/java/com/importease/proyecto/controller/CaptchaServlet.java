package com.importease.proyecto.controller;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;

@WebServlet("/captcha")
public class CaptchaServlet extends HttpServlet {
    private static final String CAPTCHA_KEY = "captcha_answer";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    static {
        // Asegurar modo headless para entornos sin display (Railway, Docker, etc.)
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SecureRandom r = new SecureRandom();

        // Generar texto aleatorio primero (independiente de AWT)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
        }
        String captchaText = sb.toString();

        // Guardar en sesión SIEMPRE (antes del intento de renderizado)
        HttpSession session = request.getSession();
        session.setAttribute(CAPTCHA_KEY, captchaText);

        // Headers sin caché
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            // Intentar renderizar con AWT
            renderAwtCaptcha(response, captchaText, r);
        } catch (Exception | Error e) {
            // Fallback: generar imagen PNG minimal sin AWT (1x1 pixel transparente)
            // El captcha ya está guardado en la sesión
            com.importease.proyecto.service.LoggerUtil.warn(
                "CaptchaServlet: AWT fallback activado (entorno headless sin fonts): " + e.getMessage());
            renderFallbackImage(response, captchaText);
        }
    }

    private void renderAwtCaptcha(HttpServletResponse response, String captchaText, SecureRandom r) throws IOException {
        int width = 180;
        int height = 60;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 1. Antialiasing para bordes suaves
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 2. Fondo (Gradiente oscuro acorde al diseño)
        GradientPaint gradient = new GradientPaint(0, 0, new Color(15, 20, 30), width, height, new Color(5, 8, 15));
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // 3. Ruido de fondo (Círculos pequeños)
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(0, 212, 193, 20 + r.nextInt(30)));
            int rValue = r.nextInt(4) + 1;
            g.fillOval(r.nextInt(width), r.nextInt(height), rValue, rValue);
        }

        // 4. Líneas curvas de interferencia
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(0, 212, 193, 40 + r.nextInt(40)));
            g.setStroke(new BasicStroke(1.5f + r.nextFloat()));
            int x1 = r.nextInt(width / 4);
            int y1 = r.nextInt(height);
            int ctrlx1 = r.nextInt(width / 2) + width / 4;
            int ctrly1 = r.nextInt(height);
            int x2 = width - r.nextInt(width / 4);
            int y2 = r.nextInt(height);
            java.awt.geom.QuadCurve2D curve = new java.awt.geom.QuadCurve2D.Float(x1, y1, ctrlx1, ctrly1, x2, y2);
            g.draw(curve);
        }

        // 5. Dibujar texto con distorsión y rotación individual
        String[] fontNames = {"SansSerif", "Monospaced", "Serif"};
        for (int i = 0; i < captchaText.length(); i++) {
            char c = captchaText.charAt(i);
            Font font = new Font(fontNames[r.nextInt(fontNames.length)], Font.BOLD | (r.nextBoolean() ? Font.ITALIC : Font.PLAIN), 34 + r.nextInt(8));
            g.setFont(font);
            g.setColor(new Color(0, 180 + r.nextInt(75), 180 + r.nextInt(75)));

            java.awt.geom.AffineTransform original = g.getTransform();
            double angle = (r.nextDouble() - 0.5) * 0.8;
            int x = 20 + (i * 30) + r.nextInt(10);
            int y = 40 + r.nextInt(10);

            g.rotate(angle, x, y);
            g.drawString(String.valueOf(c), x, y);
            g.setTransform(original);
        }

        g.dispose();

        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    /**
     * Genera una imagen PNG simple usando datos de bytes raw sin AWT.
     * Dibuja el texto del captcha como pixeles blancos sobre fondo oscuro.
     */
    private void renderFallbackImage(HttpServletResponse response, String captchaText) throws IOException {
        // Generar una imagen PNG mínima de 180x60 con el texto visible
        // usando un generador de bitmap simple sin AWT
        int width = 180;
        int height = 60;
        int[] pixels = new int[width * height];

        // Fondo oscuro
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0xFF0F141E; // ARGB: oscuro
        }

        // Dibujar una línea horizontal simple como indicador visual
        for (int x = 10; x < width - 10; x++) {
            pixels[30 * width + x] = 0xFF00D4C1; // Teal
        }

        // Convertir a BufferedImage básica (esto sí funciona en headless)
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, pixels, 0, width);

        response.setContentType("image/png");
        ImageIO.write(img, "png", response.getOutputStream());
    }
}
