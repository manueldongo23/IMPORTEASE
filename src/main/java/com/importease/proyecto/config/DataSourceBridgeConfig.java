package com.importease.proyecto.config;

import com.importease.proyecto.service.ConexionDB;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class DataSourceBridgeConfig {

    private final DataSource dataSource;

    public DataSourceBridgeConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void init() {
        ConexionDB.setSpringManagedDataSource(dataSource);
        try {
            DbMigrationRunner.ejecutarMigraciones();
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error al ejecutar migraciones en DataSourceBridgeConfig init", e);
        }
    }
}
