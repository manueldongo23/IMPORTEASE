package com.importease.proyecto.config;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

@Component
public class ServletContextBridge implements ServletContextAware {

    private final DataSource dataSource;

    public ServletContextBridge(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        try {
            ConexionDB.setSpringManagedDataSource(dataSource);
            LoggerUtil.info("ServletContextBridge ha configurado ConexionDB con el DataSource de Spring en el contexto Servlet.");
        } catch (Exception e) {
            LoggerUtil.error("Fallo al establecer DataSource en ServletContextBridge", e);
        }
    }
}
