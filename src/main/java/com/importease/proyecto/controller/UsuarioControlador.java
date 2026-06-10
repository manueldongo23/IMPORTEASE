package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.controller.login.LoginManejadorPeticion;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.service.UsuarioServicio;
import com.importease.proyecto.service.ContribuyenteServicio;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.login.PasswordResetServicio;
import com.importease.proyecto.util.CsrfUtil;
import com.importease.proyecto.util.EmailMasker;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/usuario/*")
public class UsuarioControlador extends HttpServlet {
    private UsuarioServicio usuarioServicio = new UsuarioServicio();
    private ContribuyenteServicio contribuyenteServicio = new ContribuyenteServicio();
    private Gson gson = new Gson();
    private LoginManejadorPeticion loginManejadorPeticion = new LoginManejadorPeticion();
    private PasswordResetServicio passwordResetService = new PasswordResetServicio();

    private static String buildPublicBaseUrl(HttpServletRequest req) {
        String configured = firstNonEmpty(System.getenv("APP_PUBLIC_URL"), loadAppPublicUrl());
        if (configured != null) {
            return trimTrailingSlash(configured);
        }

        String host = req.getServerName();
        int port = req.getServerPort();
        String scheme = req.getScheme();
        boolean standardHttp = "http".equalsIgnoreCase(scheme) && port == 80;
        boolean standardHttps = "https".equalsIgnoreCase(scheme) && port == 443;
        String hostPort = standardHttp || standardHttps ? host : host + ":" + port;
        return scheme + "://" + hostPort;
    }

    private static String loadAppPublicUrl() {
        try (java.io.InputStream is = UsuarioControlador.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) return null;
            java.util.Properties p = new java.util.Properties();
            p.load(is);
            return sanitizeConfigured(p.getProperty("app.public.url"));
        } catch (Exception e) {
            return null;
        }
    }

    private static String sanitizeConfigured(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.contains("${")) return null;
        return trimmed;
    }

    private static String firstNonEmpty(String a, String b) {
        String sa = sanitizeConfigured(a);
        if (sa != null) return sa;
        return sanitizeConfigured(b);
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) return null;
        String out = value.trim();
        while (out.endsWith("/")) out = out.substring(0, out.length() - 1);
        return out;
    }

    private static String maskEmail(String email) {
        return EmailMasker.mask(email);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            if ("/perfil".equals(path)) {
                HttpSession session = req.getSession(false);
                Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
                if (session != null && uIdAttr != null) {
                    int id = (int) uIdAttr;
                    Usuario u = usuarioServicio.obtenerPorId(id);
                    if (u != null) {
                        out.print(gson.toJson(new com.importease.proyecto.model.UsuarioDTO(u)));
                    } else {
                        out.print("{\"error\":\"Usuario no encontrado\"}");
                    }
                } else {
                    out.print("{\"error\":\"No autenticado\"}");
                }
            }
            else if ("/validarRuc".equals(path)) {
                String ruc = req.getParameter("ruc");
                Usuario usuario = contribuyenteServicio.validarRuc(ruc);
                if (usuario != null) {
                    out.print(gson.toJson(usuario));
                } else {
                    out.print("{\"error\":\"RUC no encontrado\"}");
                }
            }
            else {
                resp.setStatus(404);
                out.print("{\"error\":\"Endpoint no encontrado\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doGet de UsuarioControlador", e);
            resp.setStatus(500);
            out.print("{\"error\":\"Error interno al procesar el perfil o RUC.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        boolean isPublic = "/login".equals(path) || "/registro".equals(path) || "/recuperar".equals(path) || "/resetear".equals(path);
        if (!isPublic) {
            if (!CsrfUtil.validateRequest(req, resp)) return;
        }

        PrintWriter out = null;
        try {
            // El login handler maneja su propia respuesta completa (status, content-type, writer)
            // No debemos abrir resp.getWriter() antes de delegarle
            if ("/login".equals(path)) {
                loginManejadorPeticion.handle(req, resp);
                return;
            }

            resp.setContentType("application/json");
            out = resp.getWriter();

            if ("/logout".equals(path)) {

                HttpSession session = req.getSession(false);
                if (session != null) session.invalidate();
                
                // Clear Session Cookies
                javax.servlet.http.Cookie cookieJSession = new javax.servlet.http.Cookie("JSESSIONID", "");
                cookieJSession.setPath(req.getContextPath() + "/");
                cookieJSession.setMaxAge(0);
                cookieJSession.setHttpOnly(true);
                resp.addCookie(cookieJSession);
                
                // Clear CSRF Cookie if any
                javax.servlet.http.Cookie cookieCsrf = new javax.servlet.http.Cookie("csrf_token", "");
                cookieCsrf.setPath(req.getContextPath() + "/");
                cookieCsrf.setMaxAge(0);
                resp.addCookie(cookieCsrf);

                // Instruct browser to not cache pages
                resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                resp.setHeader("Pragma", "no-cache");
                resp.setDateHeader("Expires", 0);

                out.print(gson.toJson(Map.of("success", true)));
            }
            else if ("/registro".equals(path)) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                Usuario newUser = gson.fromJson(sb.toString(), Usuario.class);
                if (newUser != null && newUser.getEmail() != null) {
                    newUser.setEmail(newUser.getEmail().trim().toLowerCase());
                }
                newUser.setPerfil("IMPORTADOR_ESTANDAR");
                newUser.setBuenContribuyente(false);
                newUser.setRucValidado(false);
                newUser.setRucConfianza(0.0);

                StringBuilder outMensaje = new StringBuilder();
                boolean ok = usuarioServicio.registrarUsuario(newUser, outMensaje);
                if (ok) {
                    Usuario u = usuarioServicio.obtenerPorEmail(newUser.getEmail());
                    Integer uId = (u != null) ? u.getId() : null;
                    com.importease.proyecto.service.AuditoriaServicio.registrar(uId, "REGISTRO_USUARIO", "seguridad", null,
                        "Registro de nuevo usuario: " + maskEmail(newUser.getEmail()) + " con RUC " + newUser.getRuc() + " (" + newUser.getRazonSocial() + ")",
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                    out.print(gson.toJson(Map.of("success", true)));
                } else {
                    out.print(gson.toJson(Map.of("success", false, "mensaje", outMensaje.toString())));
                }
            }
            else if ("/recuperar".equals(path)) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                Map<String, String> data = gson.fromJson(sb.toString(), new TypeToken<Map<String, String>>(){}.getType());
                String email = data != null ? data.get("email") : null;

                try {
                    PasswordResetServicio.RecoveryRequestResult result = passwordResetService.requestRecovery(
                            email,
                            buildPublicBaseUrl(req),
                            req.getContextPath(),
                            req.getRemoteAddr(),
                            req.getHeader("User-Agent")
                    );
                    if (result.isRateLimited()) {
                        resp.setStatus(429);
                    }
                    java.util.Map<String, Object> responseMap = new java.util.HashMap<>();
                    responseMap.put("success", result.isSuccess());
                    responseMap.put("mensaje", result.getMensaje());
                    out.print(gson.toJson(responseMap));
                } catch (Exception recoveryError) {
                    LoggerUtil.error("Fallo interno en solicitud de recuperacion", recoveryError);
                    out.print(gson.toJson(Map.of(
                            "success", true,
                            "mensaje", "Si el correo existe, recibiras un enlace de recuperacion."
                    )));
                }
            }
            else if ("/resetear".equals(path)) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                Map<String, String> data = gson.fromJson(sb.toString(), new TypeToken<Map<String, String>>(){}.getType());
                String email = data != null ? data.get("email") : null;
                String token = data != null ? data.get("token") : null;
                String newPassword = data != null ? data.get("password") : null;

                PasswordResetServicio.ResetResult result = passwordResetService.resetPassword(
                        email,
                        token,
                        newPassword,
                        req.getRemoteAddr(),
                        req.getHeader("User-Agent")
                );

                if (result.isSuccess() && result.getUsuarioId() != null) {
                    com.importease.proyecto.service.AuditoriaServicio.registrar(result.getUsuarioId(), "RESTABLECER_PASSWORD", "seguridad", null,
                            "Contrasena restablecida correctamente",
                            req.getRemoteAddr(), req.getHeader("User-Agent"));
                }

                out.print(gson.toJson(Map.of(
                        "success", result.isSuccess(),
                        "mensaje", result.getMensaje()
                )));
            }
            else if ("/preferencias".equals(path)) {
                HttpSession session = req.getSession(false);
                Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
                if (session != null && uIdAttr != null) {
                    int id = (int) uIdAttr;
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = req.getReader()) {
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                    }
                    Map<String, String> data = gson.fromJson(sb.toString(), new TypeToken<Map<String, String>>(){}.getType());
                    String nivel = data != null ? data.get("nivelExperiencia") : null;
                    String prefs = data != null ? data.get("preferencias") : null;

                    boolean ok = usuarioServicio.actualizarExperienciaYPreferencias(id, nivel, prefs);
                    if (ok) {
                        if (nivel != null) session.setAttribute("usuarioNivelExperiencia", nivel);
                        if (prefs != null) session.setAttribute("usuarioPreferencias", prefs);
                        out.print(gson.toJson(Map.of("success", true)));
                    } else {
                        out.print(gson.toJson(Map.of("success", false, "mensaje", "No se pudo actualizar las preferencias.")));
                    }
                } else {
                    resp.setStatus(401);
                    out.print("{\"error\":\"No autenticado\"}");
                }
            }
            else {
                resp.setStatus(404);
                out.print("{\"error\":\"Endpoint no encontrado\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doPost de UsuarioControlador", e);
            resp.setStatus(500);
            if (out == null) {
                try {
                    resp.setContentType("application/json");
                    out = resp.getWriter();
                } catch (IOException ignored) {}
            }
            if (out != null) {
                out.print(gson.toJson(Map.of("success", false, "mensaje", "Error interno del servidor al procesar la solicitud.")));
            }
        }
    }
}
