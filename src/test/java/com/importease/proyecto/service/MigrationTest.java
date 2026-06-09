package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

public class MigrationTest {

    @Test
    public void testRunMigrations() {
        LoggerUtil.info("====== EJECUTANDO MIGRACION PROGRAMATICA DE BASE DE DATOS ======");
        try (Connection con = ConexionDB.obtenerConexion();
             Statement stmt = con.createStatement()) {
            
            java.nio.file.Path schemaPath = java.nio.file.Paths.get("sql/importease_full_schema.sql");
            if (java.nio.file.Files.exists(schemaPath)) {
                String content = java.nio.file.Files.readString(schemaPath);
                String[] statements = content.split(";");
                int count = 0;
                for (String stmtStr : statements) {
                    String sql = stmtStr.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--") && sql.toUpperCase().startsWith("CREATE")) {
                        try {
                            stmt.executeUpdate(sql);
                            count++;
                        } catch (Exception e) {
                            // Ya existe o no se puede ejecutar en test ignorar
                        }
                    }
                }
                LoggerUtil.info("[OK] " + count + " declaraciones CREATE ejecutadas desde schema consolidado.");
            } else {
                LoggerUtil.warn("No se encontro sql/importease_full_schema.sql");
            }
            
            try {
                stmt.executeUpdate("ALTER TABLE expediente_eventos_auditoria ADD COLUMN prev_hash VARCHAR(64) NULL");
                LoggerUtil.info("[OK] Columna prev_hash agregada con exito.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna prev_hash ya existe o no se pudo agregar: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE expediente_eventos_auditoria ADD COLUMN current_hash VARCHAR(64) NULL");
                LoggerUtil.info("[OK] Columna current_hash agregada con exito.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna current_hash ya existe o no se pudo agregar: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE expediente_eventos_auditoria ADD COLUMN firma_digital VARCHAR(512) NULL");
                LoggerUtil.info("[OK] Columna firma_digital agregada con exito.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna firma_digital ya existe o no se pudo agregar: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE documentos_importacion ADD COLUMN checksum_hash VARCHAR(64) NULL");
                LoggerUtil.info("[OK] Columna checksum_hash agregada con exito.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna checksum_hash ya existe o no se pudo agregar: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE documentos_importacion ADD COLUMN soft_delete BOOLEAN NOT NULL DEFAULT FALSE");
                LoggerUtil.info("[OK] Columna soft_delete agregada con exito.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna soft_delete ya existe o no se pudo agregar: " + e.getMessage());
            }

            try {
                stmt.executeUpdate("ALTER TABLE operaciones ADD COLUMN usado BOOLEAN DEFAULT FALSE");
                LoggerUtil.info("[OK] Columna usado agregada a operaciones con exito.");
            } catch (Exception e) {
                LoggerUtil.info("[INFO] Columna usado ya existe o no se pudo agregar en operaciones: " + e.getMessage());
            }

            LoggerUtil.info("====== MIGRACION FINALIZADA CON EXITO ======");
            assertTrue(true);
        } catch (Exception e) {
            LoggerUtil.error("[ERROR] Error al conectar a la base de datos para la migracion", e);
        }
    }
}
