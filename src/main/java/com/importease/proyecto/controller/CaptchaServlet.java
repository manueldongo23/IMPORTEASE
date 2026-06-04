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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int width = 180;
        int height = 60;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 1. Antialiasing para bordes suaves
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 2. Fondo (Gradiente oscuro acorde al diseÃ±o)
        GradientPaint gradient = new GradientPaint(0, 0, new Color(15, 20, 30), width, height, new Color(5, 8, 15));
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);
        
        SecureRandom r = new SecureRandom();
        
        // 3. Ruido de fondo (CÃ­rculos pequeÃ±os)
        for (int i = 0; i < 40; i++) {
            g.setColor(new Color(0, 212, 193, 20 + r.nextInt(30)));
            int rValue = r.nextInt(4) + 1;
            g.fillOval(r.nextInt(width), r.nextInt(height), rValue, rValue);
        }

        // 4. LÃ­neas curvas de interferencia (Curvas de Bezier)
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

        // 5. Generar texto aleatorio
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        
        // 6. Dibujar texto con distorsiÃ³n y rotaciÃ³n individual
        String[] fontNames = {"Arial", "Verdana", "Monospaced"};
        for (int i = 0; i < 5; i++) {
            char c = alphabet.charAt(r.nextInt(alphabet.length()));
            sb.append(c);
            
            // SelecciÃ³n aleatoria de fuente y estilo
            Font font = new Font(fontNames[r.nextInt(fontNames.length)], Font.BOLD | (r.nextBoolean() ? Font.ITALIC : Font.PLAIN), 34 + r.nextInt(8));
            g.setFont(font);
            
            // Colores vibrantes de la paleta (Teal / Cyan)
            g.setColor(new Color(0, 180 + r.nextInt(75), 180 + r.nextInt(75)));
            
            // TransformaciÃ³n geomÃ©trica (RotaciÃ³n)
            java.awt.geom.AffineTransform original = g.getTransform();
            double angle = (r.nextDouble() - 0.5) * 0.8; // Ãngulo de rotaciÃ³n entre -0.4 y 0.4 radianes
            int x = 20 + (i * 30) + r.nextInt(10);
            int y = 40 + r.nextInt(10);
            
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(c), x, y);
            g.setTransform(original); // Restaurar estado
        }

        // 7. Guardar en sesiÃ³n
        HttpSession session = request.getSession();
        session.setAttribute(CAPTCHA_KEY, sb.toString());
        
        g.dispose();
        
        // 8. Enviar respuesta sin cachÃ©
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        ImageIO.write(image, "png", response.getOutputStream());
    }
}

