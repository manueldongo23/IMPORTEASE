package com.importease.proyecto.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginRateLimiterTest {

    private final String IP_TEST = "192.168.1.100";

    @BeforeEach
    public void setUp() {
        // Limpiar IP de prueba antes de cada test para asegurar asilamiento
        LoginRateLimiter.clearAttempts(IP_TEST);
    }

    @Test
    public void testRateLimiterNoBloqueadoAlInicio() {
        assertFalse(LoginRateLimiter.isBlocked(IP_TEST));
        assertEquals(0, LoginRateLimiter.getRemainingMinutes(IP_TEST));
    }

    @Test
    public void testRateLimiterBloqueoPorIntentos() {
        // Registrar 5 intentos fallidos (lÃ­mite mÃ¡ximo configurado)
        for (int i = 0; i < 5; i++) {
            LoginRateLimiter.recordFailedAttempt(IP_TEST);
        }

        assertTrue(LoginRateLimiter.isBlocked(IP_TEST), "La IP debe estar bloqueada tras 5 intentos fallidos.");
        assertTrue(LoginRateLimiter.getRemainingMinutes(IP_TEST) > 0);
    }

    @Test
    public void testRateLimiterClearAttempts() {
        // Simular bloqueo previo
        for (int i = 0; i < 5; i++) {
            LoginRateLimiter.recordFailedAttempt(IP_TEST);
        }
        assertTrue(LoginRateLimiter.isBlocked(IP_TEST));

        // Limpiar registro tras login exitoso
        LoginRateLimiter.clearAttempts(IP_TEST);

        assertFalse(LoginRateLimiter.isBlocked(IP_TEST), "El bloqueo debe levantarse tras limpiar los intentos.");
        assertEquals(0, LoginRateLimiter.getRemainingMinutes(IP_TEST));
    }
}

