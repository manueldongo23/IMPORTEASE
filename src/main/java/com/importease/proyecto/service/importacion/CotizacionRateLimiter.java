package com.importease.proyecto.service.importacion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CotizacionRateLimiter {
    private static final int MAX_QUOTES_PER_MINUTE = 15;
    private static final long WINDOW_MS = 60_000L;

    private static class WindowState {
        final AtomicInteger counter;
        final long resetTime;
        WindowState(AtomicInteger counter, long resetTime) {
            this.counter = counter;
            this.resetTime = resetTime;
        }
    }

    private final ConcurrentHashMap<String, WindowState> windows = new ConcurrentHashMap<>();

    public boolean isLimited(String ip) {
        long now = System.currentTimeMillis();
        WindowState state = windows.compute(ip, (key, existing) -> {
            if (existing == null || now > existing.resetTime) {
                return new WindowState(new AtomicInteger(0), now + WINDOW_MS);
            }
            return existing;
        });
        return state.counter.incrementAndGet() > MAX_QUOTES_PER_MINUTE;
    }
}
