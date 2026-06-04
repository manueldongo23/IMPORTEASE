package com.importease.proyecto.repository;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import java.sql.*;
import java.util.*;

/**
 * DAO para el registro y consulta de bÃºsquedas de usuarios.
 * Alimenta el mÃ³dulo de Inteligencia de Mercado (tendencias internas).
 */
public class BusquedaDAO {

    /**
     * Registra una bÃºsqueda realizada por un usuario.
     */
    public void registrar(Integer usuarioId, String termino, String hsCode, String tipo) {
        String sql = "INSERT INTO log_busquedas (usuario_id, termino, resultado_hs_code, tipo) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usuarioId != null) ps.setInt(1, usuarioId);
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, termino);
            ps.setString(3, hsCode);
            ps.setString(4, tipo != null ? tipo : "PRODUCTO");
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar bÃºsqueda", e);
        }
    }

    /**
     * Top N productos mÃ¡s buscados en los Ãºltimos X dÃ­as.
     */
    public List<Map<String, Object>> topProductosBuscados(int dias, int limit) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT termino, COUNT(*) as veces, COUNT(DISTINCT usuario_id) as usuarios_unicos " +
                     "FROM log_busquedas " +
                     "WHERE fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY termino ORDER BY veces DESC LIMIT ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("termino", rs.getString("termino"));
                item.put("veces", rs.getInt("veces"));
                item.put("usuariosUnicos", rs.getInt("usuarios_unicos"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.error("Error top buscados", e); }
        return lista;
    }

    /**
     * EvoluciÃ³n diaria de bÃºsquedas en los Ãºltimos X dÃ­as.
     */
    public List<Map<String, Object>> evolucionDiaria(int dias) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT DATE(fecha_busqueda) as dia, COUNT(*) as total " +
                     "FROM log_busquedas " +
                     "WHERE fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY dia ORDER BY dia ASC";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("dia", rs.getString("dia"));
                item.put("total", rs.getInt("total"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.error("Error evoluciÃ³n diaria", e); }
        return lista;
    }

    /**
     * EvoluciÃ³n diaria para tÃ©rminos especÃ­ficos (comparativa).
     */
    public Map<String, List<Map<String, Object>>> evolucionPorTerminos(List<String> terminos, int dias) {
        Map<String, List<Map<String, Object>>> resultados = new LinkedHashMap<>();
        if (terminos == null || terminos.isEmpty()) return resultados;

        String placeholders = String.join(",", Collections.nCopies(terminos.size(), "?"));
        String sql = "SELECT LOWER(termino) as term_lower, DATE(fecha_busqueda) as dia, COUNT(*) as total " +
                     "FROM log_busquedas " +
                     "WHERE fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "AND LOWER(termino) IN (" + placeholders + ") " +
                     "GROUP BY term_lower, dia ORDER BY dia ASC";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            int idx = 2;
            for (String t : terminos) {
                String termLower = t.toLowerCase();
                ps.setString(idx++, termLower);
                resultados.put(termLower, new ArrayList<>());
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("dia", rs.getString("dia"));
                item.put("total", rs.getInt("total"));
                String tLower = rs.getString("term_lower");
                if(resultados.containsKey(tLower)) {
                    resultados.get(tLower).add(item);
                }
            }
        } catch (SQLException e) { LoggerUtil.error("Error evolucion multiples", e); }
        return resultados;
    }

    /**
     * CategorÃ­as HS mÃ¡s consultadas (por capÃ­tulo arancelario).
     */
    public List<Map<String, Object>> topCategorias(int dias, int limit) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT LEFT(resultado_hs_code, 2) as capitulo, COUNT(*) as veces " +
                     "FROM log_busquedas " +
                     "WHERE resultado_hs_code IS NOT NULL AND fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY capitulo ORDER BY veces DESC LIMIT ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("capitulo", rs.getString("capitulo"));
                item.put("veces", rs.getInt("veces"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.error("Error top categorÃ­as", e); }
        return lista;
    }

    public List<Map<String, Object>> topHs(int dias, int limit) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT resultado_hs_code as hs_code, COUNT(*) as veces, COUNT(DISTINCT usuario_id) as usuarios_unicos " +
                     "FROM log_busquedas " +
                     "WHERE resultado_hs_code IS NOT NULL AND resultado_hs_code <> '' " +
                     "AND fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY resultado_hs_code ORDER BY veces DESC LIMIT ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("hsCode", rs.getString("hs_code"));
                item.put("veces", rs.getInt("veces"));
                item.put("usuariosUnicos", rs.getInt("usuarios_unicos"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.error("Error top HS", e); }
        return lista;
    }

    public List<Map<String, Object>> ultimasBusquedas(int limit) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT usuario_id, termino, resultado_hs_code, tipo, fecha_busqueda " +
                     "FROM log_busquedas ORDER BY fecha_busqueda DESC LIMIT ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("usuarioId", rs.getObject("usuario_id"));
                item.put("termino", rs.getString("termino"));
                item.put("hsCode", rs.getString("resultado_hs_code"));
                item.put("tipo", rs.getString("tipo"));
                item.put("fecha", rs.getString("fecha_busqueda"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.error("Error ultimas busquedas", e); }
        return lista;
    }

    public Map<String, Object> conversionBasica(int dias) {
        Map<String, Object> stats = new LinkedHashMap<>();
        String sql = "SELECT " +
                     "(SELECT COUNT(*) FROM log_busquedas WHERE fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY)) as busquedas, " +
                     "(SELECT COUNT(*) FROM operaciones WHERE fecha_creacion >= DATE_SUB(NOW(), INTERVAL ? DAY)) as operaciones";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, dias);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int busquedas = rs.getInt("busquedas");
                int operaciones = rs.getInt("operaciones");
                stats.put("busquedas", busquedas);
                stats.put("operaciones", operaciones);
                stats.put("conversion", busquedas == 0 ? 0.0 : (operaciones * 100.0 / busquedas));
            }
        } catch (SQLException e) { LoggerUtil.error("Error conversion basica", e); }
        return stats;
    }

    public List<Map<String, Object>> eventosPorTipo(int dias, int limit) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT evento, modulo, COUNT(*) as total " +
                     "FROM eventos_usuario WHERE fecha_evento >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY evento, modulo ORDER BY total DESC LIMIT ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("evento", rs.getString("evento"));
                item.put("modulo", rs.getString("modulo"));
                item.put("total", rs.getInt("total"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.warn("eventos_usuario aun no disponible para tendencias: " + e.getMessage()); }
        return lista;
    }

    /**
     * Top productos importados (operaciones reales, no solo bÃºsquedas).
     */
    public List<Map<String, Object>> topProductosImportados(int dias, int limit) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT producto_desc, COUNT(*) as ops, SUM(total_impuestos) as tributos, " +
                     "SUM(fob) as fob_total " +
                     "FROM operaciones " +
                     "WHERE estado != 'AUDITORIA' AND estado != 'CONSULTA' " +
                     "AND fecha_creacion >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                     "GROUP BY producto_desc ORDER BY ops DESC LIMIT ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("producto", rs.getString("producto_desc"));
                item.put("operaciones", rs.getInt("ops"));
                item.put("tributos", rs.getBigDecimal("tributos"));
                item.put("fobTotal", rs.getBigDecimal("fob_total"));
                lista.add(item);
            }
        } catch (SQLException e) { LoggerUtil.error("Error top importados", e); }
        return lista;
    }

    /**
     * Total de bÃºsquedas realizadas (estadÃ­stica general).
     */
    public Map<String, Object> estadisticasGenerales(int dias) {
        Map<String, Object> stats = new LinkedHashMap<>();
        String sql = "SELECT COUNT(*) as total, COUNT(DISTINCT usuario_id) as usuarios, " +
                     "COUNT(DISTINCT termino) as terminos_unicos " +
                     "FROM log_busquedas WHERE fecha_busqueda >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dias);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("totalBusquedas", rs.getInt("total"));
                stats.put("usuariosActivos", rs.getInt("usuarios"));
                stats.put("terminosUnicos", rs.getInt("terminos_unicos"));
            }
        } catch (SQLException e) { LoggerUtil.error("Error estadÃ­sticas", e); }
        return stats;
    }
}


