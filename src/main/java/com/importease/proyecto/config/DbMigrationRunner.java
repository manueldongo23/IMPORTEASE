package com.importease.proyecto.config;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

@Component
public class DbMigrationRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        LoggerUtil.info("====== INICIANDO MIGRACIONES AUTOMÁTICAS DE BASE DE DATOS ======");
        try (Connection con = ConexionDB.obtenerConexion();
             Statement stmt = con.createStatement()) {

            // 1. Columna prev_hash
            try {
                stmt.executeUpdate("ALTER TABLE expediente_eventos_auditoria ADD COLUMN prev_hash VARCHAR(64) NULL");
                LoggerUtil.info("[OK] Columna 'prev_hash' agregada a 'expediente_eventos_auditoria'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'prev_hash' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 2. Columna current_hash
            try {
                stmt.executeUpdate("ALTER TABLE expediente_eventos_auditoria ADD COLUMN current_hash VARCHAR(64) NULL");
                LoggerUtil.info("[OK] Columna 'current_hash' agregada a 'expediente_eventos_auditoria'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'current_hash' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 3. Columna firma_digital
            try {
                stmt.executeUpdate("ALTER TABLE expediente_eventos_auditoria ADD COLUMN firma_digital VARCHAR(512) NULL");
                LoggerUtil.info("[OK] Columna 'firma_digital' agregada a 'expediente_eventos_auditoria'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'firma_digital' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 4. Columna checksum_hash
            try {
                stmt.executeUpdate("ALTER TABLE documentos_importacion ADD COLUMN checksum_hash VARCHAR(64) NULL");
                LoggerUtil.info("[OK] Columna 'checksum_hash' agregada a 'documentos_importacion'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'checksum_hash' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 5. Columna soft_delete
            try {
                stmt.executeUpdate("ALTER TABLE documentos_importacion ADD COLUMN soft_delete BOOLEAN NOT NULL DEFAULT FALSE");
                LoggerUtil.info("[OK] Columna 'soft_delete' agregada a 'documentos_importacion'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'soft_delete' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 6. Columna usado
            try {
                stmt.executeUpdate("ALTER TABLE operaciones ADD COLUMN usado BOOLEAN DEFAULT FALSE");
                LoggerUtil.info("[OK] Columna 'usado' agregada a 'operaciones'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'usado' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 7. Columna nivel_experiencia
            try {
                stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN nivel_experiencia VARCHAR(50) DEFAULT 'NUNCA'");
                LoggerUtil.info("[OK] Columna 'nivel_experiencia' agregada a 'usuarios'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'nivel_experiencia' ya existe o no se pudo agregar: " + e.getMessage());
            }

            // 8. Columna preferencias
            try {
                stmt.executeUpdate("ALTER TABLE usuarios ADD COLUMN preferencias VARCHAR(255) DEFAULT '{\"ocultarConsejos\":false}'");
                LoggerUtil.info("[OK] Columna 'preferencias' agregada a 'usuarios'.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna 'preferencias' ya existe o no se pudo agregar: " + e.getMessage());
            }

            LoggerUtil.info("====== MIGRACIONES DE BASE DE DATOS FINALIZADAS CON ÉXITO ======");
        } catch (Exception e) {
            LoggerUtil.error("[ERROR] Fallo al ejecutar las migraciones automáticas de base de datos", e);
        }
    }
}
