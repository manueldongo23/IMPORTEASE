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
        if (attemptService.isBlocked(clientIp)) {
            return ResultadoAutenticacion.blocked(attemptService.getRemainingMinutes(clientIp));
        }

        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            attemptService.recordFailedAttempt(clientIp);
            return ResultadoAutenticacion.failed("Credenciales invalidas");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (!captchaValidacionServicio.isValid(session, request.getCaptcha())) {
            attemptService.recordFailedAttempt(clientIp);
            return ResultadoAutenticacion.failed("CAPTCHA incorrecto o expirado");
        }

        LoggerUtil.info("Login attempt: " + EmailMasker.mask(email));
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email);
        if (usuario == null || !hashContrasenaServicio.matches(request.getPassword(), usuario.getPasswordHash())) {
            attemptService.recordFailedAttempt(clientIp);
            LoggerUtil.info("Login failed: " + EmailMasker.mask(email));
            return ResultadoAutenticacion.failed("Credenciales invalidas");
        }

        attemptService.clearAttempts(clientIp);
        LoggerUtil.info("Login success: " + EmailMasker.mask(email));
        return ResultadoAutenticacion.success(usuario);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
