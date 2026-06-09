package com.importease.proyecto.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Servicio de Panel/Tablero Ejecutivo (PanelServicio en español).
 * Calcula KPIs operativos reales del sistema para el panel principal.
 */
public class PanelServicio {
    private static final Map<String, String> ALLOWED_SUM_COLUMNS;
    private static final Map<String, String> ALLOWED_GROUP_COLUMNS;

    static {
        Map<String, String> sumColumns = new HashMap<>();
        sumColumns.put("valor_fob", "fob");
        sumColumns.put("valor_cif", "cif");
        sumColumns.put("total_impuestos", "total_impuestos");
        ALLOWED_SUM_COLUMNS = Collections.unmodifiableMap(sumColumns);

        Map<String, String> groupColumns = new HashMap<>();
        groupColumns.put("canal_asignado", "canal_asignado");
        ALLOWED_GROUP_COLUMNS = Collections.unmodifiableMap(groupColumns);
    }

    private static final long TASK_TIMEOUT_SECONDS = 2;
    private static final int THREAD_POOL_SIZE = 16;
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    private static class CacheEntry {
        final Map<String, Object> data;
        final long timestamp;
        CacheEntry(Map<String, Object> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final Map<Integer, CacheEntry> STATS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 5000; // 5 segundos de cache

    public static void invalidarCache(int usuarioId) {
        STATS_CACHE.remove(usuarioId);
    }

    public static void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Obtiene todas las estadísticas del panel para un usuario.
     * Las consultas independientes se ejecutan en paralelo para reducir la latencia.
     */
    public static Map<String, Object> obtenerEstadisticas(int usuarioId) {
        CacheEntry entry = STATS_CACHE.get(usuarioId);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_DURATION_MS) {
            return entry.data;
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        // 1. Operaciones por estado + totalOps (derivado)
        submitDbTask(futures, stats, "porEstado", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                Map<String, Integer> porEstado = contarPorEstado(con, usuarioId);
                synchronized (stats) {
                    stats.put("porEstado", porEstado);
                    stats.put("totalOps", porEstado.values().stream().mapToInt(Integer::intValue).sum());
                }
            } catch (Exception ignored) {}
        });

        // 2. Sumas combinadas en una sola consulta (FOB, CIF, Tributos)
        submitDbTask(futures, stats, "sumasCombinadas", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                Map<String, BigDecimal> sumas = sumarMultiplesCampos(con, usuarioId, "valor_fob", "valor_cif", "total_impuestos");
                synchronized (stats) {
                    stats.put("fobTotal", sumas.getOrDefault("valor_fob", BigDecimal.ZERO));
                    stats.put("cifTotal", sumas.getOrDefault("valor_cif", BigDecimal.ZERO));
                    stats.put("tributosTotal", sumas.getOrDefault("total_impuestos", BigDecimal.ZERO));
                }
            } catch (Exception ignored) {}
        });

        // 3. Distribución de canales
        submitDbTask(futures, stats, "porCanal", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                synchronized (stats) {
                    stats.put("porCanal", contarPorCampo(con, usuarioId, "canal_asignado"));
                }
            } catch (Exception ignored) {}
        });

        // 4. Top 5 subpartidas
        submitDbTask(futures, stats, "topHsCodes", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                synchronized (stats) {
                    stats.put("topHsCodes", topHsCodes(con, usuarioId, 5));
                }
            } catch (Exception ignored) {}
        });

        // 5. Operaciones restringidas
        submitDbTask(futures, stats, "restringidos", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                synchronized (stats) {
                    stats.put("restringidos", contarRestringidos(con, usuarioId));
                }
            } catch (Exception ignored) {}
        });

        // 6. Tipo de cambio del día (con fallback)
        futures.add(executor.submit(() -> {
            try {
                TipoCambioServicio tcService = new TipoCambioServicio();
                Map<String, Object> tcData = tcService.obtenerTipoCambioDetalleRapido();
                synchronized (stats) {
                    stats.put("tipoCambio", tcData);
                }
            } catch (Exception e) {
                Map<String, Object> tcFallback = new LinkedHashMap<>();
                tcFallback.put("tipoCambio", new BigDecimal("3.75"));
                tcFallback.put("fuente", "Fallback");
                synchronized (stats) {
                    stats.put("tipoCambio", tcFallback);
                }
                LoggerUtil.warn("Error en tarea tipoCambio: " + e.getMessage());
            }
        }));

        // 7. FOB mensual (últimos 6 meses)
        submitDbTask(futures, stats, "fobMensual", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                synchronized (stats) {
                    stats.put("fobMensual", fobMensual(con, usuarioId, 6));
                }
            } catch (Exception ignored) {}
        });

        // 8. Plazos activos
        submitDbTask(futures, stats, "plazosActivos", () -> {
            try (Connection con = ConexionDB.obtenerConexion()) {
                synchronized (stats) {
                    stats.put("plazosActivos", contarPlazosActivos(con, usuarioId));
                }
            } catch (Exception ignored) {}
        });

        // 9. Alertas críticas
        futures.add(executor.submit(() -> {
            try {
                List<Map<String, Object>> alertas = NotificacionServicio.obtenerNotificacionesCriticas(usuarioId);
                synchronized (stats) {
                    stats.put("alertasCriticas", alertas);
                }
            } catch (Exception e) {
                LoggerUtil.warn("Error en tarea alertasCriticas: " + e.getMessage());
            }
        }));

        awaitAll(futures);
        STATS_CACHE.put(usuarioId, new CacheEntry(stats));
        return stats;
    }

    private static void submitDbTask(List<Future<?>> futures, Map<String, Object> stats, String taskName, Runnable task) {
        futures.add(executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LoggerUtil.warn("Error en tarea " + taskName + ": " + e.getMessage());
            }
        }));
    }

    private static void awaitAll(List<Future<?>> futures) {
        for (Future<?> f : futures) {
            try {
                f.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                f.cancel(true);
                LoggerUtil.warn("Timeout en tarea del dashboard tras " + TASK_TIMEOUT_SECONDS + "s");
            } catch (Exception e) {
                LoggerUtil.warn("Error esperando tarea del dashboard: " + e.getMessage());
            }
        }
    }

    private static Map<String, Integer> contarPorEstado(Connection con, int usuarioId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT estado, COUNT(*) as total FROM operaciones WHERE usuario_id = ? GROUP BY estado ORDER BY total DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("estado"), rs.getInt("total"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error contando por estado: " + e.getMessage());
        }
        return map;
    }

    private static Map<String, Integer> contarPorCampo(Connection con, int usuarioId, String campo) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String columna = sanitizeColumn(campo, ALLOWED_GROUP_COLUMNS);
        if (columna == null) {
            LoggerUtil.warn("Columna de agrupacion no permitida en dashboard: " + campo);
            return map;
        }
        String sql = "SELECT COALESCE(" + columna + ", 'SIN CANAL') as valor, COUNT(*) as total FROM operaciones WHERE usuario_id = ? GROUP BY " + columna + " ORDER BY total DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("valor"), rs.getInt("total"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error contando por " + columna + ": " + e.getMessage());
        }
        return map;
    }

    private static List<Map<String, Object>> topHsCodes(Connection con, int usuarioId, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT i.hs_code, COUNT(*) as usos, h.descripcion_es " +
                     "FROM operaciones i LEFT JOIN hs_codes h ON i.hs_code = h.codigo " +
                     "WHERE i.usuario_id = ? AND i.hs_code IS NOT NULL " +
                     "GROUP BY i.hs_code, h.descripcion_es ORDER BY usos DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("hsCode", rs.getString("hs_code"));
                    item.put("descripcion", rs.getString("descripcion_es"));
                    item.put("usos", rs.getInt("usos"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error en top HS codes: " + e.getMessage());
        }
        return list;
    }

    private static int contarRestringidos(Connection con, int usuarioId) {
        String sql = "SELECT COUNT(*) FROM operaciones i " +
                     "JOIN hs_codes h ON i.hs_code = h.codigo " +
                     "WHERE i.usuario_id = ? AND h.requiere_vuce = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error contando restringidos: " + e.getMessage());
        }
        return 0;
    }

    private static List<Map<String, Object>> fobMensual(Connection con, int usuarioId, int meses) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(fecha_creacion, '%Y-%m') as mes, " +
                     "SUM(fob) as fob, SUM(total_impuestos) as tributos, COUNT(*) as ops " +
                     "FROM operaciones WHERE usuario_id = ? " +
                     "AND fecha_creacion >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                     "GROUP BY mes ORDER BY mes";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setInt(2, meses);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("mes", rs.getString("mes"));
                    item.put("fob", rs.getBigDecimal("fob"));
                    item.put("tributos", rs.getBigDecimal("tributos"));
                    item.put("operaciones", rs.getInt("ops"));
                    list.add(item);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error en FOB mensual: " + e.getMessage());
        }
        return list;
    }

    private static int contarPlazosActivos(Connection con, int usuarioId) {
        String sql = "SELECT COUNT(*) FROM operaciones WHERE usuario_id = ? AND estado NOT IN ('NACIONALIZADA', 'CANCELADA')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error contando plazos activos: " + e.getMessage());
        }
        return 0;
    }

    static String sanitizeColumn(String requested, Map<String, String> allowedColumns) {
        if (requested == null) {
            return null;
        }
        return allowedColumns.get(requested);
    }

    private static Map<String, BigDecimal> sumarMultiplesCampos(Connection con, int usuarioId, String... campos) {
        Map<String, BigDecimal> resultados = new LinkedHashMap<>();
        if (campos == null || campos.length == 0) {
            return resultados;
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        boolean first = true;
        for (String campo : campos) {
            String columna = sanitizeColumn(campo, ALLOWED_SUM_COLUMNS);
            if (columna == null) {
                LoggerUtil.warn("Columna no permitida en sumarMultiplesCampos: " + campo);
                resultados.put(campo, BigDecimal.ZERO);
                continue;
            }
            if (!first) {
                sql.append(", ");
            }
            first = false;
            sql.append("COALESCE(SUM(").append(columna).append("), 0) as ").append(columna);
        }
        if (first) {
            return resultados;
        }
        sql.append(" FROM operaciones WHERE usuario_id = ?");
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    for (String campo : campos) {
                        String columna = sanitizeColumn(campo, ALLOWED_SUM_COLUMNS);
                        if (columna != null) {
                            BigDecimal val = rs.getBigDecimal(columna);
                            resultados.put(campo, val != null ? val : BigDecimal.ZERO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error en sumarMultiplesCampos: " + e.getMessage());
        }
        for (String campo : campos) {
            resultados.putIfAbsent(campo, BigDecimal.ZERO);
        }
        return resultados;
    }
}
