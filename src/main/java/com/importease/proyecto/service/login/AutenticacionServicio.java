package com.importease.proyecto.service.login;

import com.importease.proyecto.dto.login.SolicitudLoginDTO;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.util.EmailMasker;

import javax.servlet.http.HttpSession;

/**
 * Servicio encargado de la autenticación de usuarios (en español).
 */
public class AutenticacionServicio {
    private final UsuarioRepositorio usuarioRepositorio;
    private final HashContrasenaServicio hashContrasenaServicio;
    private final LoginIntentoServicio attemptService;
    private final CaptchaValidacionServicio captchaValidacionServicio;

    public AutenticacionServicio() {
        this(
                new UsuarioRepositorio(),
                new HashContrasenaServicio(),
                new LoginIntentoServicio(),
                new CaptchaValidacionServicio()
        );
    }

    public AutenticacionServicio(
            UsuarioRepositorio usuarioRepositorio,
            HashContrasenaServicio hashContrasenaServicio,
            LoginIntentoServicio attemptService,
            CaptchaValidacionServicio captchaValidacionServicio
    ) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.hashContrasenaServicio = hashContrasenaServicio;
        this.attemptService = attemptService;
        this.captchaValidacionServicio = captchaValidacionServicio;
    }

    public ResultadoAutenticacion authenticate(SolicitudLoginDTO request, String clientIp, HttpSession session) {
        LoggerUtil.info("AutenticacionServicio.authenticate: Iniciando proceso para IP: " + clientIp);

        if (attemptService.isBlocked(clientIp)) {
            LoggerUtil.warn("AutenticacionServicio.authenticate: IP bloqueada temporalmente: " + clientIp);
            return ResultadoAutenticacion.blocked(attemptService.getRemainingMinutes(clientIp));
        }

        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            attemptService.recordFailedAttempt(clientIp);
            boolean reqNull = request == null;
            boolean emailBlank = !reqNull && isBlank(request.getEmail());
            boolean passBlank = !reqNull && isBlank(request.getPassword());
            LoggerUtil.warn("AutenticacionServicio.authenticate: Solicitud invalida. Request nulo: " + reqNull + ", Email vacio: " + emailBlank + ", Password vacio: " + passBlank);
            return ResultadoAutenticacion.failed("Credenciales invalidas");
        }

        String email = request.getEmail().trim().toLowerCase();
        
        // Diagnostico de CAPTCHA
        String expectedCaptcha = null;
        if (session == null) {
            LoggerUtil.warn("AutenticacionServicio.authenticate: La sesion HTTP es nula.");
        } else {
            expectedCaptcha = (String) session.getAttribute("captcha_answer");
            LoggerUtil.info("AutenticacionServicio.authenticate: Validando CAPTCHA. Sesion ID: " + session.getId() + ", Recibido: [" + request.getCaptcha() + "], Esperado en sesion: [" + expectedCaptcha + "]");
        }

        if (!captchaValidacionServicio.isValid(session, request.getCaptcha())) {
            attemptService.recordFailedAttempt(clientIp);
            LoggerUtil.warn("AutenticacionServicio.authenticate: Fallo de CAPTCHA para " + EmailMasker.mask(email) + ". Enviado: [" + request.getCaptcha() + "], Esperado: [" + expectedCaptcha + "]");
            return ResultadoAutenticacion.failed("CAPTCHA incorrecto o expirado");
        }

        LoggerUtil.info("AutenticacionServicio.authenticate: Intento de login: " + EmailMasker.mask(email));
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email);
        
        if (usuario == null) {
            attemptService.recordFailedAttempt(clientIp);
            LoggerUtil.warn("AutenticacionServicio.authenticate: Usuario NO encontrado en base de datos: " + EmailMasker.mask(email));
            return ResultadoAutenticacion.failed("Credenciales invalidas");
        }

        LoggerUtil.info("AutenticacionServicio.authenticate: Usuario encontrado. Email: " + EmailMasker.mask(usuario.getEmail()) + ", password_hash length: " + (usuario.getPasswordHash() != null ? usuario.getPasswordHash().length() : 0));

        boolean passwordMatches = hashContrasenaServicio.matches(request.getPassword(), usuario.getPasswordHash());
        LoggerUtil.info("AutenticacionServicio.authenticate: Comparando password. matches: " + passwordMatches);

        if (!passwordMatches) {
            attemptService.recordFailedAttempt(clientIp);
            LoggerUtil.warn("AutenticacionServicio.authenticate: Credenciales invalidas (password incorrecto) para: " + EmailMasker.mask(email));
            return ResultadoAutenticacion.failed("Credenciales invalidas");
        }

        attemptService.clearAttempts(clientIp);
        LoggerUtil.info("AutenticacionServicio.authenticate: Login exitoso para: " + EmailMasker.mask(email));
        return ResultadoAutenticacion.success(usuario);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
