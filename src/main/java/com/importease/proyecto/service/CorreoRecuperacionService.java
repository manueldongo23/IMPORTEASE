package com.importease.proyecto.service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class CorreoRecuperacionService {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = CorreoRecuperacionService.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo cargar config.properties para SMTP: " + e.getMessage());
        }
        try (InputStream is = CorreoRecuperacionService.class.getClassLoader().getResourceAsStream("config-local.properties")) {
            if (is != null) {
                Properties local = new Properties();
                local.load(is);
                props.putAll(local);
                LoggerUtil.info("config-local.properties cargado como override SMTP");
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo cargar config-local.properties: " + e.getMessage());
        }
    }

    public static class ResultadoEnvio {
        private final boolean enviado;
        private final String mensaje;
        private final String proveedor;

        public ResultadoEnvio(boolean enviado, String mensaje, String proveedor) {
            this.enviado = enviado;
            this.mensaje = mensaje;
            this.proveedor = proveedor;
        }

        public boolean isEnviado() { return enviado; }
        public String getMensaje() { return mensaje; }
        public String getProveedor() { return proveedor; }
    }

    public static ResultadoEnvio enviarRecuperacion(String destinatarioEmail, String destinatarioNombre, String resetUrl) {
        String host = resolveConfigured("SMTP_HOST", "smtp.host");
        String username = resolveConfigured("SMTP_USERNAME", "smtp.username");
        String from = firstNonEmpty(resolveConfigured("SMTP_FROM", "smtp.from"), username);
        String password = resolveConfigured("SMTP_PASSWORD", "smtp.password");
        String port = firstNonEmpty(resolveConfigured("SMTP_PORT", "smtp.port"), "587");
        boolean starttls = parseBoolean(resolveConfigured("SMTP_STARTTLS", "smtp.starttls"), true);
        boolean ssl = parseBoolean(resolveConfigured("SMTP_SSL", "smtp.ssl"), false);

        if (isBlank(host) || isBlank(from) || isBlank(destinatarioEmail) || isBlank(resetUrl)) {
            throw new IllegalStateException("Configuracion SMTP incompleta. Defina SMTP_HOST, SMTP_USERNAME y SMTP_FROM (o reutilice la cuenta como remitente), ademas de la URL publica de restablecimiento.");
        }

        LoggerUtil.info("SMTP config: host=" + host + " port=" + port + " username=" + username + " auth=" + (password != null && !password.isEmpty()));

        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", host);
        mailProps.put("mail.smtp.port", port != null ? port : "587");
        boolean auth = !isBlank(username) && !isBlank(password);
        mailProps.put("mail.smtp.auth", String.valueOf(auth));
        mailProps.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        mailProps.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        mailProps.put("mail.smtp.ssl.trust", host);
        mailProps.put("mail.smtp.connectiontimeout", "10000");
        mailProps.put("mail.smtp.timeout", "10000");
        mailProps.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(mailProps, auth
                ? new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password != null ? password : "");
                    }
                }
                : null);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "ImportEase Soporte"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarioEmail, false));
            message.setSubject("ImportEase - Restablecer ContraseÃ±a", StandardCharsets.UTF_8.name());

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(buildPlainText(destinatarioNombre, resetUrl), StandardCharsets.UTF_8.name());

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(buildHtml(destinatarioNombre, resetUrl), "text/html; charset=UTF-8");

            MimeMultipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            LoggerUtil.info("Correo de recuperacion enviado a " + maskEmail(destinatarioEmail) + " via SMTP " + host + ":" + port);
            return new ResultadoEnvio(true, "Correo enviado correctamente.", "SMTP");
        } catch (Exception e) {
            LoggerUtil.error("Error al enviar correo de recuperacion a " + maskEmail(destinatarioEmail), e);
            throw new IllegalStateException("No se pudo enviar el correo de recuperacion.", e);
        }
    }

    public static String buildResetLink(String baseUrl, String contextPath, String email, String token) {
        String base = trimTrailingSlash(firstNonEmpty(baseUrl, ""));
        String ctx = normalizeContextPath(contextPath);
        if (!ctx.isEmpty() && base.endsWith(ctx)) {
            base = base.substring(0, base.length() - ctx.length());
            base = trimTrailingSlash(base);
        }
        return base + ctx + "/resetear-clave.jsp?email=" + url(email) + "&token=" + url(token);
    }

    public static String buildPlainText(String nombre, String resetUrl) {
        String safeName = isBlank(nombre) ? "usuario" : nombre;
        return "Hola, " + safeName + ".\n\n"
                + "Recibimos una solicitud para restablecer tu contrasena de ImportEase.\n"
                + "Abre el siguiente enlace para crear una nueva clave:\n"
                + resetUrl + "\n\n"
                + "Si no solicitaste este cambio, ignora este mensaje.";
    }

    public static String buildHtml(String nombre, String resetUrl) {
        String safeName = escapeHtml(isBlank(nombre) ? "usuario" : nombre);
        String safeUrl = escapeHtml(resetUrl);
        return "<!doctype html><html lang=\"es\"><body style=\"margin:0;background:#f6f7fb;font-family:Arial,sans-serif;color:#111827;\">"
                + "<div style=\"max-width:640px;margin:0 auto;padding:32px;\">"
                + "<div style=\"background:#ffffff;border:1px solid #e5e7eb;border-radius:24px;padding:32px;box-shadow:0 18px 50px rgba(15,23,42,.08);\">"
                + "<div style=\"display:flex;align-items:center;gap:12px;margin-bottom:24px;\">"
                + "<div style=\"width:40px;height:40px;border-radius:12px;background:#ef4444;color:#fff;font-weight:800;display:flex;align-items:center;justify-content:center;\">I</div>"
                + "<div><div style=\"font-size:16px;font-weight:800;line-height:1.2;\">ImportEase</div><div style=\"font-size:12px;color:#6b7280;\">Restablecimiento de contrasena</div></div>"
                + "</div>"
                + "<h1 style=\"font-size:24px;line-height:1.2;margin:0 0 16px 0;color:#111827;\">Hola, " + safeName + "</h1>"
                + "<p style=\"margin:0 0 16px 0;font-size:15px;line-height:1.6;color:#374151;\">Recibimos una solicitud para restablecer el acceso a tu cuenta. Haz clic en el boton para elegir una nueva contrasena.</p>"
                + "<div style=\"margin:28px 0;\">"
                + "<a href=\"" + safeUrl + "\" style=\"display:inline-block;background:#2563eb;color:#ffffff;text-decoration:none;font-weight:700;padding:14px 22px;border-radius:14px;\">Restablecer contrasena</a>"
                + "</div>"
                + "<p style=\"margin:0 0 10px 0;font-size:13px;line-height:1.6;color:#6b7280;\">O copia y pega este enlace en tu navegador:</p>"
                + "<p style=\"margin:0;word-break:break-all;font-size:13px;line-height:1.6;color:#111827;\">" + safeUrl + "</p>"
                + "<p style=\"margin:24px 0 0 0;font-size:12px;line-height:1.6;color:#6b7280;\">Este enlace expira en 15 minutos. Si no solicitaste el cambio, ignora este correo.</p>"
                + "</div></div></body></html>";
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        if (local.length() <= 2) return local.charAt(0) + "***@" + parts[1];
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + parts[1];
    }

    private static String url(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private static boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null) return defaultValue;
        String v = value.trim().toLowerCase();
        if (v.isEmpty()) return defaultValue;
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y") || v.equals("on");
    }

    private static String firstNonEmpty(String a, String b) {
        if (!isBlank(a)) return a.trim();
        if (!isBlank(b)) return b.trim();
        return null;
    }

    private static String resolveConfigured(String envName, String propName) {
        return sanitizeConfigured(firstNonEmpty(System.getenv(envName), props.getProperty(propName)));
    }

    private static String normalizeContextPath(String contextPath) {
        if (contextPath == null || contextPath.isBlank() || "/".equals(contextPath.trim())) return "";
        return contextPath.startsWith("/") ? contextPath.trim() : "/" + contextPath.trim();
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String sanitizeConfigured(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) return null;
        if (trimmed.contains("${")) return null;
        return trimmed;
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}


