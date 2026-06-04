package com.importease.proyecto.service.login;

import com.importease.proyecto.dto.login.LoginRequestDTO;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.usuario.UsuarioDaoRepository;
import com.importease.proyecto.repository.usuario.UsuarioRepository;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.util.EmailMasker;

import javax.servlet.http.HttpSession;

public class AuthenticationService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordHashService passwordHashService;
    private final LoginAttemptService attemptService;
    private final CaptchaValidationService captchaValidationService;

    public AuthenticationService() {
        this(
                new UsuarioDaoRepository(),
                new PasswordHashService(),
                new LoginAttemptService(),
                new CaptchaValidationService()
        );
    }

    public AuthenticationService(
            UsuarioRepository usuarioRepository,
            PasswordHashService passwordHashService,
            LoginAttemptService attemptService,
            CaptchaValidationService captchaValidationService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHashService = passwordHashService;
        this.attemptService = attemptService;
        this.captchaValidationService = captchaValidationService;
    }

    public AuthenticationResult authenticate(LoginRequestDTO request, String clientIp, HttpSession session) {
        if (attemptService.isBlocked(clientIp)) {
            return AuthenticationResult.blocked(attemptService.getRemainingMinutes(clientIp));
        }

        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            attemptService.recordFailedAttempt(clientIp);
            return AuthenticationResult.failed("Credenciales invalidas");
        }

        String email = request.getEmail().trim();
        if (!captchaValidationService.isValid(session, request.getCaptcha())) {
            attemptService.recordFailedAttempt(clientIp);
            return AuthenticationResult.failed("CAPTCHA incorrecto o expirado");
        }

        LoggerUtil.info("Login attempt: " + EmailMasker.mask(email));
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null || !passwordHashService.matches(request.getPassword(), usuario.getPasswordHash())) {
            attemptService.recordFailedAttempt(clientIp);
            LoggerUtil.info("Login failed: " + EmailMasker.mask(email));
            return AuthenticationResult.failed("Credenciales invalidas");
        }

        attemptService.clearAttempts(clientIp);
        LoggerUtil.info("Login success: " + EmailMasker.mask(email));
        return AuthenticationResult.success(usuario);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
