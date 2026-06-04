/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.controller.login.LoginRequestHandler;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.ContribuyenteService;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.PasswordValidator;
import com.importease.proyecto.service.login.PasswordResetService;
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
public class UsuarioController extends HttpServlet {
    private UsuarioDAO usuarioDao = new UsuarioDAO();
    private ContribuyenteService contribuyenteService = new ContribuyenteService();
    private Gson gson = new Gson();
    private LoginRequestHandler loginRequestHandler = new LoginRequestHandler();
    private PasswordResetService passwordResetService = new PasswordResetService();
    private static String buildPublicBaseUrl(HttpServletRequest req) {
        String configured = firstNonEmpty(System.getenv("APP_PUBLIC_URL"), loadAppPublicUrl());
        if (configured != null) {
            return trimTrailingSlash(configured);
        }

        String forwardedProto = req.getHeader("X-Forwarded-Proto");
        String scheme = (forwardedProto != null && !forwardedProto.isBlank()) ? forwardedProto : req.getScheme();
        String host = req.getHeader("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = req.getServerName();
            int port = req.getServerPort();
            boolean standardHttp = "http".equalsIgnoreCase(scheme) && port == 80;
            boolean standardHttps = "https".equalsIgnoreCase(scheme) && port == 443;
            if (!standardHttp && !standardHttps) {
                host = host + ":" + port;
            }
        }
        return scheme + "://" + host;
    }

    private static String loadAppPublicUrl() {
        try (java.io.InputStream is = UsuarioController.class.getClassLoader().getResourceAsStream("config.properties")) {
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

    /** Enmascarar email para logs: u***o@example.com */
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
                    Usuario u = usuarioDao.buscarPorId(id);
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
                Usuario usuario = contribuyenteService.validarRuc(ruc);
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
            com.importease.proyecto.service.LoggerUtil.error("Error en doGet de UsuarioController", e);
            resp.setStatus(500);
            out.print("{\"error\":\"Error interno al procesar el perfil o RUC.\"}");
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        boolean isPublic = "/login".equals(path) || "/registro".equals(path) || "/recuperar".equals(path) || "/resetear".equals(path);
        if (!isPublic) {
            if (!CsrfUtil.validateRequest(req, resp)) return;
        }
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            if ("/login".equals(path)) {
                loginRequestHandler.handle(req, resp);
            }
            else if ("/logout".equals(path)) {
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
                newUser.setBuenContribuyente(false);
                newUser.setPerfil("IMPORTADOR_ESTANDAR");
                newUser.setRucValidado(false);
                newUser.setFuenteRuc(null);
                newUser.setEstadoRuc(null);
                newUser.setCondicionRuc(null);
                newUser.setRucConfianza(0.0);

                // Validar complejidad de contraseÃƒÂ±a
                String pwError = PasswordValidator.validate(newUser.getPasswordHash());
                if (pwError != null) {
                    out.print(gson.toJson(Map.of("success", false, "mensaje", pwError)));
                    return;
                }

                // Verificar email duplicado
                if (usuarioDao.buscarPorEmail(newUser.getEmail()) != null) {
                    out.print(gson.toJson(Map.of("success", false, "mensaje", "Este email ya estÃƒÂ¡ registrado")));
                    return;
                }

                boolean ok = usuarioDao.crearUsuario(newUser);
                if (ok) {
                    Usuario u = usuarioDao.buscarPorEmail(newUser.getEmail());
                    Integer uId = (u != null) ? u.getId() : null;
                    com.importease.proyecto.service.AuditoriaService.registrar(uId, "REGISTRO_USUARIO", "seguridad", null,
                        "Registro de nuevo usuario: " + maskEmail(newUser.getEmail()) + " con RUC " + newUser.getRuc() + " (" + newUser.getRazonSocial() + ")",
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                }
                out.print(gson.toJson(Map.of("success", ok)));
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
                    PasswordResetService.RecoveryRequestResult result = passwordResetService.requestRecovery(
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
                    if (result.getResetUrl() != null) {
                        responseMap.put("resetUrl", result.getResetUrl());
                    }
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

                PasswordResetService.ResetResult result = passwordResetService.resetPassword(
                        email,
                        token,
                        newPassword,
                        req.getRemoteAddr(),
                        req.getHeader("User-Agent")
                );

                if (result.isSuccess() && result.getUsuarioId() != null) {
                    com.importease.proyecto.service.AuditoriaService.registrar(result.getUsuarioId(), "RESTABLECER_PASSWORD", "seguridad", null,
                            "Contrasena restablecida correctamente",
                            req.getRemoteAddr(), req.getHeader("User-Agent"));
                }

                out.print(gson.toJson(Map.of(
                        "success", result.isSuccess(),
                        "mensaje", result.getMensaje()
                )));
            }
            else {
                resp.setStatus(404);
                out.print("{\"error\":\"Endpoint no encontrado\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doPost de UsuarioController", e);
            resp.setStatus(500);
            out.print(gson.toJson(Map.of("success", false, "mensaje", "Error interno del servidor al procesar la solicitud.")));
        }
    }
}

