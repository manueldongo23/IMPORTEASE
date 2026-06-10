package com.importease.proyecto.config;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bootstrap de datos demo para entornos vacios.
 * Carga el script SQL del repo solo cuando la base no tiene registros utiles.
 */
@Component
@Order(2)
public class DatabaseBootstrapRunner implements CommandLineRunner {

    private static final String SQL_FILE = "sql/importease_railway.sql";

    @Override
    public void run(String... args) {
        if (!debeSembrarDatosDemo()) {
            LoggerUtil.info("Bootstrap demo omitido: la base ya tiene datos operativos.");
            return;
        }

        Path script = localizarScriptSql();
        if (script == null) {
            LoggerUtil.warn("No se encontro " + SQL_FILE + " para cargar datos demo.");
            return;
        }

        LoggerUtil.info("====== CARGANDO DATOS DEMO DE IMPORTEASE ======");
        try (Connection con = ConexionDB.obtenerConexion()) {
            ejecutarScriptSql(con, script);
            LoggerUtil.info("====== DATOS DEMO CARGADOS CON EXITO ======");
        } catch (Exception e) {
            LoggerUtil.error("Fallo al cargar los datos demo de ImportEase", e);
        }
    }

    private boolean debeSembrarDatosDemo() {
        try (Connection con = ConexionDB.obtenerConexion()) {
            boolean usuariosExiste = tablaExiste(con, "usuarios");
            boolean operacionesExiste = tablaExiste(con, "operaciones");
            if (!usuariosExiste || !operacionesExiste) {
                return true;
            }

            long usuarios = contarFilas(con, "usuarios");
            long operaciones = contarFilas(con, "operaciones");
            return usuarios == 0 || operaciones == 0;
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo validar el contenido de la base; se intentara bootstrap demo: " + e.getMessage());
            return true;
        }
    }

    private boolean tablaExiste(Connection con, String tableName) throws Exception {
        try (ResultSet rs = con.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private long contarFilas(Connection con, String tableName) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private Path localizarScriptSql() {
        Path directo = Path.of(SQL_FILE);
        if (Files.exists(directo)) {
            return directo;
        }

        InputStream resource = DatabaseBootstrapRunner.class.getClassLoader().getResourceAsStream(SQL_FILE);
        if (resource == null) {
            return null;
        }

        try {
            Path temp = Files.createTempFile("importease-railway-", ".sql");
            temp.toFile().deleteOnExit();
            Files.copy(resource, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return temp;
        } catch (IOException e) {
            LoggerUtil.warn("No se pudo materializar el script SQL embebido: " + e.getMessage());
            return null;
        } finally {
            try {
                resource.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void ejecutarScriptSql(Connection con, Path scriptPath) throws IOException, java.sql.SQLException {
        try (BufferedReader reader = Files.newBufferedReader(scriptPath, StandardCharsets.UTF_8);
             Statement stmt = con.createStatement()) {

            String delimiter = ";";
            StringBuilder current = new StringBuilder();
            boolean inBlockComment = false;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (inBlockComment) {
                    if (trimmed.contains("*/")) {
                        inBlockComment = false;
                    }
                    continue;
                }

                if (trimmed.startsWith("/*")) {
                    if (!trimmed.contains("*/")) {
                        inBlockComment = true;
                    }
                    continue;
                }

                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("#")) {
                    continue;
                }

                if (trimmed.toUpperCase(Locale.ROOT).startsWith("DELIMITER ")) {
                    ejecutarSiHayContenido(stmt, current, delimiter);
                    delimiter = trimmed.substring("DELIMITER ".length()).trim();
                    continue;
                }

                current.append(line).append('\n');
                if (terminaConDelimitador(trimmed, delimiter)) {
                    ejecutarSiHayContenido(stmt, current, delimiter);
                }
            }

            ejecutarSiHayContenido(stmt, current, delimiter);
        }
    }

    private boolean terminaConDelimitador(String trimmed, String delimiter) {
        return trimmed.endsWith(delimiter);
    }

    private void ejecutarSiHayContenido(Statement stmt, StringBuilder current, String delimiter) throws java.sql.SQLException {
        String sql = current.toString().trim();
        if (sql.isEmpty()) {
            current.setLength(0);
            return;
        }

        String statement = quitarDelimitadorFinal(sql, delimiter);
        if (statement.isBlank()) {
            current.setLength(0);
            return;
        }

        try {
            stmt.execute(statement);
        } catch (java.sql.SQLException e) {
            if (esErrorIgnorable(statement, e)) {
                LoggerUtil.info("Bootstrap demo: se ignoro una sentencia no critica: " + resumen(statement) + " -> " + e.getMessage());
            } else {
                throw e;
            }
        } finally {
            current.setLength(0);
        }
    }

    private String quitarDelimitadorFinal(String sql, String delimiter) {
        String trimmed = sql.trim();
        if (delimiter != null && !delimiter.isBlank() && trimmed.endsWith(delimiter)) {
            return trimmed.substring(0, trimmed.length() - delimiter.length()).trim();
        }
        if (";".equals(delimiter) && trimmed.endsWith(";")) {
            return trimmed.substring(0, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private boolean esErrorIgnorable(String statement, java.sql.SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
        String sql = statement.toLowerCase(Locale.ROOT);

        if (message.contains("duplicate") || message.contains("already exists")) {
            return true;
        }
        if (sql.startsWith("drop ") && (message.contains("doesn't exist") || message.contains("can't drop"))) {
            return true;
        }
        return false;
    }

    private String resumen(String statement) {
        String compact = statement.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 140) {
            return compact;
        }
        return compact.substring(0, 140) + "...";
    }
}
