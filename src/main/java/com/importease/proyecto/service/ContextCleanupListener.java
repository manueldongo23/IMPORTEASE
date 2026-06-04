package com.importease.proyecto.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextCleanupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LoggerUtil.info("ContextCleanupListener inicializado correctamente.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LoggerUtil.info("Iniciando limpieza del contexto de la aplicaciÃ³n para evitar fugas de memoria...");
        
        // 1. Cerrar pool de HikariCP
        ConexionDB.cerrarPool();
        
        // 2. Apagar el hilo de limpieza de conexiones abandonadas de MySQL
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            LoggerUtil.info("AbandonedConnectionCleanupThread de MySQL cerrado exitosamente.");
        } catch (Exception e) {
            LoggerUtil.error("Error al cerrar AbandonedConnectionCleanupThread de MySQL", e);
        }
    }
}

