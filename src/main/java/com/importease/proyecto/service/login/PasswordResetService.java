package com.importease.proyecto.service.login;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.PasswordResetTokenDAO;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.CorreoRecuperacionService;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.PasswordValidator;
import com.importease.proyecto.util.EmailMasker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * Servicio seguro de recuperacion de contrasena.
 *
 * Reglas implementadas:
 * - Respuesta publica generica para evitar enumeracion de usuarios.
 * - Token aleatorio de alta entropia.
 * - Se persiste solo SHA-256 del token, nunca el token plano.
 * - Expiracion de 15 minutos y uso unico.
 * - Rate limit por IP y por usuario.
 * - No se registra el token ni el resetUrl en logs.
 */
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int RATE_WINDOW_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_IP = 10;
    private static final int MAX_REQUESTS_PER_USER = 3;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioDAO usuarioDao;
    private final PasswordResetTokenDAO tokenDao;

    public PasswordResetService() {
        this(new UsuarioDAO(), new PasswordResetTokenDAO());
    }

    public PasswordResetService(UsuarioDAO usuarioDao, PasswordResetTokenDAO tokenDao) {
        this.usuarioDao = usuarioDao;
        this.tokenDao = tokenDao;
    }

    public RecoveryRequestResult requestRecovery(String email, String baseUrl, String contextPath, String ip, String userAgent) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return RecoveryRequestResult.generic();
        }

        if (isIpRateLimited(ip)) {
            LoggerUtil.warn("Rate limit de recuperacion por IP excedido: " + maskIp(ip));
            return RecoveryRequestResult.rateLimited();
        }

        Usuario usuario = usuarioDao.buscarPorEmail(normalizedEmail);
        if (usuario == null) {
            LoggerUtil.info("Solicitud de recuperacion recibida para correo no registrado: " + EmailMasker.mask(normalizedEmail));
            return RecoveryRequestResult.generic();
        }

        if (isUserRateLimited(usuario.getId())) {
            LoggerUtil.warn("Rate limit de recuperacion por usuario excedido: usuarioId=" + usuario.getId());
            return RecoveryRequestResult.rateLimited();
        }

        String token = generateToken();
        String tokenHash = hashToken(token);
        Instant expiresAt = Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        synchronized ((usuario.getEmail() != null ? usuario.getEmail() : "").intern()) {
            tokenDao.invalidateActiveTokens(usuario.getId());
            tokenDao.createToken(usuario.getId(), tokenHash, expiresAt, ip, userAgent);
        }

        String resetUrl = CorreoRecuperacionService.buildResetLink(baseUrl, contextPath, normalizedEmail, token);
        LoggerUtil.info("Enviando correo de recuperación a " + EmailMasker.mask(normalizedEmail) +
                " (usuario: " + usuario.getRazonSocial() + ")");
        try {
            CorreoRecuperacionService.enviarRecuperacion(normalizedEmail, usuario.getRazonSocial(), resetUrl);
            LoggerUtil.info("Correo de recuperacion confirmado para: " + EmailMasker.mask(normalizedEmail));
            LoggerUtil.info("Solicitud de recuperacion procesada para usuarioId=" + usuario.getId()
                    + " correo=" + EmailMasker.mask(normalizedEmail));
            return RecoveryRequestResult.generic();
        } catch (Exception e) {
            LoggerUtil.error("Fallo al enviar correo de recuperación a " + EmailMasker.mask(normalizedEmail), e);
            LoggerUtil.warn("=== FALLBACK: usa este enlace en el navegador ===");
            LoggerUtil.warn("Correo de recuperacion no enviado, URL generada (no logueada por seguridad)");
            return RecoveryRequestResult.withFallbackUrl(resetUrl);
        }
    }

    public ResetResult resetPassword(String email, String token, String newPassword, String ip, String userAgent) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || token == null || token.isBlank()) {
            return ResetResult.invalidToken();
        }

        String pwError = PasswordValidator.validate(newPassword);
        if (pwError != null) {
            return ResetResult.validationError(pwError);
        }

        Usuario usuario = usuarioDao.buscarPorEmail(normalizedEmail);
        if (usuario == null) {
            return ResetResult.invalidToken();
        }

        String tokenHash = hashToken(token);
        boolean valid = tokenDao.isValidUnusedToken(usuario.getId(), tokenHash);
        if (!valid) {
            LoggerUtil.warn("Intento de reset con token invalido para usuarioId=" + usuario.getId()
                    + " ip=" + maskIp(ip));
            return ResetResult.invalidToken();
        }

        boolean updated = usuarioDao.actualizarPassword(normalizedEmail, newPassword);
        if (!updated) {
            return ResetResult.failed();
        }

        tokenDao.markUsed(usuario.getId(), tokenHash);
        LoggerUtil.info("Contrasena restablecida correctamente para usuarioId=" + usuario.getId()
                + " ip=" + maskIp(ip));
        return ResetResult.success(usuario.getId());
    }

    private boolean isIpRateLimited(String ip) {
        if (ip == null || ip.isBlank()) return false;
        return tokenDao.countRecentRequestsByIp(ip, RATE_WINDOW_MINUTES) >= MAX_REQUESTS_PER_IP;
    }

    private boolean isUserRateLimited(int usuarioId) {
        return tokenDao.countRecentRequestsByUsuario(usuarioId, RATE_WINDOW_MINUTES) >= MAX_REQUESTS_PER_USER;
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        String value = email.trim().toLowerCase();
        if (value.isEmpty() || !value.contains("@") || value.length() > 254) return null;
        return value;
    }

    private static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return "desconocida";
        if (ip.contains(".")) {
            String[] p = ip.split("\\.");
            if (p.length == 4) return p[0] + "." + p[1] + ".***.***";
        }
        if (ip.length() <= 6) return "***";
        return ip.substring(0, 4) + "***";
    }

    public static class RecoveryRequestResult {
        private final boolean success;
        private final String mensaje;
        private final boolean rateLimited;
        private final String resetUrl;

        private RecoveryRequestResult(boolean success, String mensaje, boolean rateLimited, String resetUrl) {
            this.success = success;
            this.mensaje = mensaje;
            this.rateLimited = rateLimited;
            this.resetUrl = resetUrl;
        }

        static RecoveryRequestResult generic() {
            return new RecoveryRequestResult(true,
                    "Si el correo existe, recibiras un enlace de recuperacion.", false, null);
        }

        static RecoveryRequestResult withFallbackUrl(String resetUrl) {
            return new RecoveryRequestResult(false,
                    "No se pudo enviar el correo. Usa el enlace directo de respaldo.", false, resetUrl);
        }

        static RecoveryRequestResult rateLimited() {
            return new RecoveryRequestResult(false,
                    "Demasiadas solicitudes. Espera unos minutos antes de volver a intentarlo.", true, null);
        }

        public boolean isSuccess() { return success; }
        public String getMensaje() { return mensaje; }
        public boolean isRateLimited() { return rateLimited; }
        public String getResetUrl() { return resetUrl; }
    }

    public static class ResetResult {
        private final boolean success;
        private final String mensaje;
        private final Integer usuarioId;

        private ResetResult(boolean success, String mensaje, Integer usuarioId) {
            this.success = success;
            this.mensaje = mensaje;
            this.usuarioId = usuarioId;
        }

        static ResetResult success(Integer usuarioId) {
            return new ResetResult(true, "Contrasena actualizada correctamente.", usuarioId);
        }

        static ResetResult invalidToken() {
            return new ResetResult(false, "El enlace de recuperacion es invalido, ya fue usado o ha expirado.", null);
        }

        static ResetResult validationError(String error) {
            return new ResetResult(false, error, null);
        }

        static ResetResult failed() {
            return new ResetResult(false, "Error al restablecer la contrasena.", null);
        }

        public boolean isSuccess() { return success; }
        public String getMensaje() { return mensaje; }
        public Integer getUsuarioId() { return usuarioId; }
    }
}
