package com.importease.proyecto.config;

import com.importease.proyecto.service.LoggerUtil;
import java.util.concurrent.atomic.AtomicLong;

public class CircuitBreaker {
    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final String name;
    private final int thresholdSize; // Cantidad de peticiones del sliding window
    private final double failureRateThreshold; // Tasa de error para abrir circuito (ej. 0.40)
    private final long cooldownPeriodMs; // Tiempo para reintentar (ej. 60000ms)

    private State state = State.CLOSED;
    private final boolean[] window;
    private int cursor = 0;
    private int totalRequests = 0;
    private final AtomicLong lastStateTransitionTime = new AtomicLong(System.currentTimeMillis());

    public CircuitBreaker(String name, int thresholdSize, double failureRateThreshold, long cooldownPeriodMs) {
        this.name = name;
        this.thresholdSize = thresholdSize;
        this.failureRateThreshold = failureRateThreshold;
        this.cooldownPeriodMs = cooldownPeriodMs;
        this.window = new boolean[thresholdSize];
    }

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        if (state == State.OPEN) {
            if (now - lastStateTransitionTime.get() > cooldownPeriodMs) {
                transitionTo(State.HALF_OPEN);
                return true;
            }
            return false;
        }
        return true;
    }

    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            transitionTo(State.CLOSED);
            resetWindow();
        } else if (state == State.CLOSED) {
            recordResult(true);
        }
    }

    public synchronized void recordFailure() {
        if (state == State.HALF_OPEN) {
            transitionTo(State.OPEN);
        } else if (state == State.CLOSED) {
            recordResult(false);
            checkFailureRate();
        }
    }

    public State getState() {
        return state;
    }

    private void recordResult(boolean success) {
        window[cursor] = success;
        cursor = (cursor + 1) % thresholdSize;
        if (totalRequests < thresholdSize) {
            totalRequests++;
        }
    }

    private void checkFailureRate() {
        if (totalRequests < thresholdSize) return;

        int failures = 0;
        for (boolean success : window) {
            if (!success) failures++;
        }

        double rate = (double) failures / thresholdSize;
        if (rate >= failureRateThreshold) {
            transitionTo(State.OPEN);
        }
    }

    private void transitionTo(State newState) {
        LoggerUtil.warn("CircuitBreaker [" + name + "] transition from " + state + " to " + newState);
        this.state = newState;
        this.lastStateTransitionTime.set(System.currentTimeMillis());
    }

    private void resetWindow() {
        this.cursor = 0;
        this.totalRequests = 0;
        java.util.Arrays.fill(window, false);
    }
}


