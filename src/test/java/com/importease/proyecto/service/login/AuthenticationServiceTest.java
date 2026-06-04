package com.importease.proyecto.service.login;

import com.google.gson.Gson;
import com.importease.proyecto.dto.login.LoginRequestDTO;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.usuario.UsuarioRepository;
import com.importease.proyecto.service.LoginRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthenticationServiceTest {
    private final PasswordHashService passwordHashService = new PasswordHashService();

    @Test
    public void authenticateReturnsSuccessForValidCredentialsAndCaptcha() {
        String ip = uniqueIp();
        Usuario usuario = usuario("manuel@importease.test", passwordHashService.hash("Secret123"));
        AuthenticationService service = serviceWith(usuario);
        MockHttpSession session = sessionWithCaptcha("1234");

        AuthenticationResult result = service.authenticate(
                request("manuel@importease.test", "Secret123", "1234"),
                ip,
                session
        );

        assertTrue(result.isSuccess());
        assertEquals(usuario, result.getUsuario());
        assertFalse(LoginRateLimiter.isBlocked(ip));
        assertNull(session.getAttribute("captcha_answer"));
    }

    @Test
    public void authenticateFailsAndConsumesCaptchaWhenCaptchaIsInvalid() {
        String ip = uniqueIp();
        Usuario usuario = usuario("manuel@importease.test", passwordHashService.hash("Secret123"));
        AuthenticationService service = serviceWith(usuario);
        MockHttpSession session = sessionWithCaptcha("1234");

        AuthenticationResult result = service.authenticate(
                request("manuel@importease.test", "Secret123", "9999"),
                ip,
                session
        );

        assertFalse(result.isSuccess());
        assertEquals("CAPTCHA incorrecto o expirado", result.getMessage());
        assertNull(session.getAttribute("captcha_answer"));
    }

    @Test
    public void authenticateFailsForInvalidPassword() {
        String ip = uniqueIp();
        Usuario usuario = usuario("manuel@importease.test", passwordHashService.hash("Secret123"));
        AuthenticationService service = serviceWith(usuario);
        MockHttpSession session = sessionWithCaptcha("1234");

        AuthenticationResult result = service.authenticate(
                request("manuel@importease.test", "Wrong123", "1234"),
                ip,
                session
        );

        assertFalse(result.isSuccess());
        assertEquals("Credenciales invalidas", result.getMessage());
    }

    @Test
    public void authenticateReturnsBlockedWhenIpExceededAttempts() {
        String ip = uniqueIp();
        for (int i = 0; i < 5; i++) {
            LoginRateLimiter.recordFailedAttempt(ip);
        }

        Usuario usuario = usuario("manuel@importease.test", passwordHashService.hash("Secret123"));
        AuthenticationService service = serviceWith(usuario);
        MockHttpSession session = sessionWithCaptcha("1234");

        AuthenticationResult result = service.authenticate(
                request("manuel@importease.test", "Secret123", "1234"),
                ip,
                session
        );

        assertFalse(result.isSuccess());
        assertEquals(429, result.getStatusCode());
        assertEquals("1234", session.getAttribute("captcha_answer"));
        LoginRateLimiter.clearAttempts(ip);
    }

    private AuthenticationService serviceWith(Usuario usuario) {
        return new AuthenticationService(
                new StaticUsuarioRepository(usuario),
                passwordHashService,
                new LoginAttemptService(),
                new CaptchaValidationService()
        );
    }

    private LoginRequestDTO request(String email, String password, String captcha) {
        String json = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"captcha\":\"%s\"}",
                email,
                password,
                captcha
        );
        return new Gson().fromJson(json, LoginRequestDTO.class);
    }

    private MockHttpSession sessionWithCaptcha(String captcha) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("captcha_answer", captcha);
        return session;
    }

    private Usuario usuario(String email, String passwordHash) {
        Usuario usuario = new Usuario();
        usuario.setId(7);
        usuario.setEmail(email);
        usuario.setRazonSocial("ImportEase Test SAC");
        usuario.setRuc("20123456789");
        usuario.setPasswordHash(passwordHash);
        return usuario;
    }

    private String uniqueIp() {
        return "10.10.10." + (System.nanoTime() % 200 + 1);
    }

    private static class StaticUsuarioRepository implements UsuarioRepository {
        private final Usuario usuario;

        StaticUsuarioRepository(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public Usuario findByEmail(String email) {
            if (usuario.getEmail().equals(email)) {
                return usuario;
            }
            return null;
        }
    }
}
