package com.importease.proyecto.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.importease.proyecto.service.LoggerUtil;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            LoggerUtil.warn("No se pudo cargar config.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        String envKey = key.toUpperCase().replace(".", "_");
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        String value = properties.getProperty(key);
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String placeholderKey = value.substring(2, value.length() - 1);
            String placeholderValue = System.getenv(placeholderKey);
            return placeholderValue != null ? placeholderValue : "";
        }
        return value != null ? value : "";
    }
}


