package com.importease.proyecto.service.importacion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CotizacionRateLimiter {
    private static final int MAX_QUOTES_PER_MINUTE = 15;
    private static final long WINDOW_MS = 60_000L;

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> resetTimes = new ConcurrentHashMap<>();

    public boolean isLimited(String ip) {
        long now = System.currentTimeMillis();
        resetTimes.compute(ip, (key, resetTime) -> {
            if (resetTime == null || now > resetTime) {
                counters.put(ip, new AtomicInteger(0));
                return now + WINDOW_MS;
            }
            return resetTime;
        });

        return counters.get(ip).incrementAndGet() > MAX_QUOTES_PER_MINUTE;
    }
}
