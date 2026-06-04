package com.importease.proyecto.service;

import org.slf4j.LoggerFactory;

public class LoggerUtil {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoggerUtil.class);

    public static void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }
}


