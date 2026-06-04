package com.importease.proyecto.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

public class ConexionDB {
    private static final Properties props = new Properties();
    private static volatile DataSource springManagedDataSource;
    private static volatile HikariDataSource dataSource;
    private static volatile HikariDataSource dataSourceSecundario;

    static {
        cargarConfiguracion();
    }

    private static void cargarConfiguracion() {
        try (InputStream is = ConexionDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new IllegalStateException("db.properties no encontrado en classpath.");
            }
            props.load(is);
        } catch (Exception e) {
            throw new IllegalStateException("Error critico cargando db.properties.", e);
        }
    }

    private static void inicializarPoolSiNecesario() {
        if (dataSource != null) return;
        synchronized (ConexionDB.class) {
            if (dataSource != null) return;
            inicializarPool();
        }
    }

    private static void inicializarPoolSecundarioSiNecesario() {
        if (dataSourceSecundario != null) return;
        synchronized (ConexionDB.class) {
            if (dataSourceSecundario != null) return;
            inicializarPoolSecundario();
        }
    }

    private static void inicializarPoolSecundario() {
        String url = firstNonEmpty(System.getenv("DB_URL"), props.getProperty("db.url"));
        if (url == null) return;

        String user = resolveCredential("DB_USER", "db.user", "root");
        String password = resolveCredential("DB_PASSWORD", "db.password", "");

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            config.setMaximumPoolSize(3);
            config.setMinimumIdle(1);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(4000);
            config.setLeakDetectionThreshold(15000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "100");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSourceSecundario = new HikariDataSource(config);
            LoggerUtil.info("Pool secundario de conexiones (para scrapers/auditoria) inicializado correctamente.");
        } catch (Exception e) {
            LoggerUtil.error("Fallo al inicializar pool de conexion secundario", e);
            dataSourceSecundario = null;
        }
    }

    private static void asegurarBaseDeDatosExiste(String url, String user, String password) {
        if (url == null) return;
        try {
            int dbNameIndex = url.indexOf("/importease_db");
            if (dbNameIndex != -1) {
                String baseUrl = url.substring(0, dbNameIndex + 1) + "?useSSL=false&serverTimezone=America/Lima";
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = java.sql.DriverManager.getConnection(baseUrl, user, password);
                     java.sql.Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS importease_db");
                    LoggerUtil.info("Base de datos 'importease_db' verificada/creada con éxito.");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Intento de asegurar existencia de BD: " + e.getMessage());
        }
    }

    private static void inicializarPool() {
        String url = firstNonEmpty(System.getenv("DB_URL"), props.getProperty("db.url"));
        if (url == null) {
            throw new IllegalStateException("db.url no configurado. No se puede iniciar ConexionDB.");
        }

        String user = resolveCredential("DB_USER", "db.user", "importease_app");
        String dbPassword = resolveCredential("DB_PASSWORD", "db.password", "importease_dev");

        // Intentar asegurar que existe la BD con las credenciales configuradas
        asegurarBaseDeDatosExiste(url, user, dbPassword);

        // 1. Intentar con credenciales configuradas (importease_app / importease_dev o env vars)
        if (intentarCrearDataSource(url, user, dbPassword, "localhost (" + user + ")")) {
            return;
        }

        throw new IllegalStateException("No se pudo conectar a MySQL. Configure spring.datasource.username y spring.datasource.password en application.properties.");
    }

    private static String resolveCredential(String envName, String propName, String fallback) {
        String envValue = sanitize(System.getenv(envName));
        if (envValue != null) return envValue;

        String propValue = sanitize(props.getProperty(propName));
        if (propValue != null) return propValue;

        return fallback;
    }

    private static String resolvePlaceholder(String value) {
        if (value == null) return null;
        value = value.trim();
        if (value.startsWith("${") && value.endsWith("}")) {
            String content = value.substring(2, value.length() - 1);
            int colonIndex = content.indexOf(':');
            if (colonIndex != -1) {
                String envName = content.substring(0, colonIndex);
                String defaultValue = content.substring(colonIndex + 1);
                String envValue = System.getenv(envName);
                if (envValue != null && !envValue.trim().isEmpty()) {
                    return envValue.trim();
                }
                return defaultValue.trim();
            } else {
                String envValue = System.getenv(content);
                if (envValue != null && !envValue.trim().isEmpty()) {
                    return envValue.trim();
                }
                return null;
            }
        }
        return value;
    }

    private static String sanitize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.contains("${")) {
            String resolved = resolvePlaceholder(trimmed);
            return sanitize(resolved);
        }
        return trimmed;
    }

    private static String firstNonEmpty(String a, String b) {
        String sa = sanitize(a);
        if (sa != null) return sa;
        return sanitize(b);
    }

    private static boolean intentarCrearDataSource(String url, String user, String password, String descripcion) {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(4000);
            config.setLeakDetectionThreshold(15000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            try (Connection conn = dataSource.getConnection()) {
                LoggerUtil.info("DB conectada correctamente usando pool: " + descripcion);
                return true;
            }
        } catch (Exception e) {
            LoggerUtil.error("Fallo al inicializar pool de conexion " + descripcion, e);
            if (dataSource != null) {
                try {
                    dataSource.close();
                } catch (Exception ignored) {
                    // no-op
                }
                dataSource = null;
            }
            return false;
        }
    }

    public static Connection obtenerConexion() throws SQLException {
        if (springManagedDataSource != null) {
            return springManagedDataSource.getConnection();
        }
        inicializarPoolSiNecesario();
        if (dataSource == null) {
            throw new SQLException("ConexionDB no inicializado.");
        }
        return dataSource.getConnection();
    }

    public static Connection obtenerConexionSecundaria() throws SQLException {
        if (springManagedDataSource != null) {
            return springManagedDataSource.getConnection();
        }
        inicializarPoolSecundarioSiNecesario();
        if (dataSourceSecundario == null) {
            return obtenerConexion();
        }
        return dataSourceSecundario.getConnection();
    }

    public static void cerrarPool() {
        springManagedDataSource = null;
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            LoggerUtil.info("Pool de conexiones de base de datos cerrado correctamente.");
        }
        if (dataSourceSecundario != null) {
            dataSourceSecundario.close();
            dataSourceSecundario = null;
            LoggerUtil.info("Pool secundario de conexiones cerrado correctamente.");
        }
    }

    public static void setSpringManagedDataSource(DataSource dataSource) {
        springManagedDataSource = dataSource;
        if (dataSource != null) {
            LoggerUtil.info("ConexionDB conectado a DataSource administrado por Spring Boot.");
        }
    }
}


