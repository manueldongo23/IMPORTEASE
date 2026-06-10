package com.importease.proyecto.service;

import org.slf4j.LoggerFactory;

public class LoggerUtil {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoggerUtil.class);

    public static org.slf4j.Logger getLogger(Class<?> clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    public static void error(String message, Throwable t) {
        logger.error(message, t);
    }

    public static void error(org.slf4j.Logger log, String msg, Throwable t) {
        if (log != null) log.error(msg, t); else logger.error(msg, t);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void info(org.slf4j.Logger log, String msg) {
        if (log != null) log.info(msg); else logger.info(msg);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(org.slf4j.Logger log, String msg) {
        if (log != null) log.warn(msg); else logger.warn(msg);
    }

    public static void error(org.slf4j.Logger log, String msg) {
        if (log != null) log.error(msg); else logger.error(msg);
    }
}


