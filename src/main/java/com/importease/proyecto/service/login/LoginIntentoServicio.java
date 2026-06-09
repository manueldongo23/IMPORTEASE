package com.importease.proyecto.service.login;

import com.importease.proyecto.service.LoginRateLimiter;

public class LoginIntentoServicio {
    public boolean isBlocked(String clientIp) {
        return LoginRateLimiter.isBlocked(clientIp);
    }

    public void recordFailedAttempt(String clientIp) {
        LoginRateLimiter.recordFailedAttempt(clientIp);
    }

    public void clearAttempts(String clientIp) {
        LoginRateLimiter.clearAttempts(clientIp);
    }

    public int getRemainingMinutes(String clientIp) {
        return LoginRateLimiter.getRemainingMinutes(clientIp);
    }
}
