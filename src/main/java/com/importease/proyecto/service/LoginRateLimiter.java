package com.importease.proyecto.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter para proteger el endpoint de login contra fuerza bruta.
 * Bloquea IPs que excedan MAX_ATTEMPTS intentos en WINDOW_MS milisegundos.
 */
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000; // 15 minutos

    private static final ConcurrentHashMap<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    /**
     * Registra un intento fallido de login para la IP dada.
     */
    public static void recordFailedAttempt(String ip) {
        attempts.compute(ip, (key, record) -> {
            long now = System.currentTimeMillis();
            if (record == null || now - record.windowStart > WINDOW_MS) {
                return new AttemptRecord(now, 1);
            }
            record.count.incrementAndGet();
            return record;
        });
    }

    /**
     * Verifica si la IP estÃ¡ bloqueada (excediÃ³ el mÃ¡ximo de intentos).
     */
    public static boolean isBlocked(String ip) {
        long now = System.currentTimeMillis();
        // Remueve de forma atÃ³mica si la ventana ya expirÃ³
        AttemptRecord record = attempts.computeIfPresent(ip, (key, r) -> {
            if (now - r.windowStart > WINDOW_MS) {
                return null;
            }
            return r;
        });
        if (record == null) return false;
        return record.count.get() >= MAX_ATTEMPTS;
    }

    /**
     * Limpia el registro de intentos tras un login exitoso.
     */
    public static void clearAttempts(String ip) {
        attempts.remove(ip);
    }

    /**
     * Devuelve los minutos restantes de bloqueo para una IP.
     */
    public static int getRemainingMinutes(String ip) {
        AttemptRecord record = attempts.get(ip);
        if (record == null) return 0;
        long elapsed = System.currentTimeMillis() - record.windowStart;
        long remaining = WINDOW_MS - elapsed;
        return (int) Math.max(1, remaining / (60 * 1000));
    }

    private static class AttemptRecord {
        final long windowStart;
        final AtomicInteger count;

        AttemptRecord(long windowStart, int initialCount) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(initialCount);
        }
    }
}


