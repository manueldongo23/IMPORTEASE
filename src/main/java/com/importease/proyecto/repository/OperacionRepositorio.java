package com.importease.proyecto.repository;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.jpa.OperacionEntity;
import com.importease.proyecto.repository.jpa.OperacionJpaRepositorio;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.mapper.ImportacionMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio de base de datos para la entidad de Operacion.
 * Reorganizado bajo arquitectura limpia. Utiliza ImportacionMapper para conversión de datos.
 */
public class OperacionRepositorio implements IOperacionRepositorio {

    public boolean guardar(Operacion op) {
        OperacionJpaRepositorio repo = getJpaRepo();
        if (repo != null) {
            try {
                OperacionEntity saved = repo.save(ImportacionMapper.toEntity(op));
                if (saved.getId() != null) op.setId(saved.getId());
                return true;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo guardar operacion con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO operaciones (usuario_id, producto_desc, hs_code, pais_origen, incoterm, fob, flete, seguro, tipo_cambio, ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos, canal_asignado, estado, numero_dam) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, op.getUsuarioId());
            ps.setString(2, op.getProductoDesc());
            ps.setString(3, op.getHsCode());
            ps.setString(4, op.getPaisOrigen());
            ps.setString(5, op.getIncoterm());
            ps.setBigDecimal(6, op.getFob());
            ps.setBigDecimal(7, op.getFlete());
            ps.setBigDecimal(8, op.getSeguro());
            ps.setBigDecimal(9, op.getTipoCambio());
            ps.setBigDecimal(10, op.getAdValoremAplicado());
            ps.setBigDecimal(11, op.getIscAplicado());
            ps.setBigDecimal(12, op.getIgvAplicado());
            ps.setBigDecimal(13, op.getIpmAplicado());
            ps.setBigDecimal(14, op.getPercepcionAplicada());
            ps.setBigDecimal(15, op.getTotalImpuestos());
            ps.setString(16, op.getCanalAsignado());
            ps.setString(17, op.getEstado());
            ps.setString(18, op.getNumeroDam());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) op.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al guardar operacion", e);
        }
        return false;
    }

    public Operacion obtenerPorId(int id) {
        OperacionJpaRepositorio repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<OperacionEntity> entity = repo.findById(id);
                if (entity.isPresent()) return ImportacionMapper.toModelOperacion(entity.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerPorId con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT id, usuario_id, producto_desc, hs_code, pais_origen, incoterm, fob, flete, seguro, cif, tipo_cambio, ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos, canal_asignado, estado, numero_dam, fecha_numeracion, fecha_creacion FROM operaciones WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearOperacion(rs);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener operacion por ID", e);
        }
        return null;
    }

    public List<Operacion> listarPorUsuario(int usuarioId) {
        OperacionJpaRepositorio repo = getJpaRepo();
        if (repo != null) {
            try {
                List<Operacion> out = new ArrayList<>();
                for (OperacionEntity entity : repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)) {
                    out.add(ImportacionMapper.toModelOperacion(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarPorUsuario con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<Operacion> lista = new ArrayList<>();
        String sql = "SELECT id, usuario_id, producto_desc, hs_code, pais_origen, incoterm, fob, flete, seguro, cif, tipo_cambio, ad_valorem_aplicado, isc_aplicado, igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos, canal_asignado, estado, numero_dam, fecha_numeracion, fecha_creacion FROM operaciones WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearOperacion(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar operaciones por usuario", e);
        }
        return lista;
    }

    public boolean actualizarDam(int id, String numeroDam, String estado, String canal) {
        OperacionJpaRepositorio repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<OperacionEntity> entity = repo.findById(id);
                if (entity.isPresent()) {
                    OperacionEntity op = entity.get();
                    op.setNumeroDam(numeroDam);
                    op.setEstado(estado);
                    op.setCanalAsignado(canal);
                    op.setFechaNumeracion(new Timestamp(System.currentTimeMillis()));
                    repo.save(op);
                    return true;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizarDam con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "UPDATE operaciones SET numero_dam = ?, estado = ?, canal_asignado = ?, fecha_numeracion = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroDam);
            ps.setString(2, estado);
            ps.setString(3, canal);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al actualizar DAM", e);
            return false;
        }
    }

    public Map<String, Object> obtenerEstadisticas(int usuarioId) {
        OperacionJpaRepositorio repo = getJpaRepo();
        if (repo != null) {
            try {
                Object[] row = repo.obtenerEstadisticas(usuarioId);
                if (row != null && row.length >= 6) {
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("totalImpuestos", row[0] instanceof BigDecimal ? row[0] : BigDecimal.ZERO);
                    stats.put("totalFob", row[1] instanceof BigDecimal ? row[1] : BigDecimal.ZERO);
                    stats.put("totalOps", row[2] instanceof Long ? ((Long) row[2]).intValue() : 0);
                    stats.put("verde", row[3] instanceof Long ? ((Long) row[3]).intValue() : 0);
                    stats.put("naranja", row[4] instanceof Long ? ((Long) row[4]).intValue() : 0);
                    stats.put("rojo", row[5] instanceof Long ? ((Long) row[5]).intValue() : 0);
                    return stats;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerEstadisticas con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
            "SUM(total_impuestos) as total_impuestos, " +
            "SUM(fob) as total_fob, " +
            "COUNT(*) as total_ops, " +
            "SUM(CASE WHEN canal_asignado = 'VERDE' THEN 1 ELSE 0 END) as verde, " +
            "SUM(CASE WHEN canal_asignado = 'NARANJA' THEN 1 ELSE 0 END) as naranja, " +
            "SUM(CASE WHEN canal_asignado = 'ROJO' THEN 1 ELSE 0 END) as rojo " +
            "FROM operaciones WHERE usuario_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalImpuestos", rs.getBigDecimal("total_impuestos") != null ? rs.getBigDecimal("total_impuestos") : BigDecimal.ZERO);
                    stats.put("totalFob", rs.getBigDecimal("total_fob") != null ? rs.getBigDecimal("total_fob") : BigDecimal.ZERO);
                    stats.put("totalOps", rs.getInt("total_ops"));
                    stats.put("verde", rs.getInt("verde"));
                    stats.put("naranja", rs.getInt("naranja"));
                    stats.put("rojo", rs.getInt("rojo"));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener estadisticas", e);
        }
        return stats;
    }

    private Operacion mapearOperacion(ResultSet rs) throws SQLException {
        Operacion op = new Operacion();
        op.setId(rs.getInt("id"));
        op.setUsuarioId(rs.getInt("usuario_id"));
        op.setProductoDesc(rs.getString("producto_desc"));
        op.setHsCode(rs.getString("hs_code"));
        op.setPaisOrigen(rs.getString("pais_origen"));
        op.setIncoterm(rs.getString("incoterm"));
        op.setFob(rs.getBigDecimal("fob"));
        op.setFlete(rs.getBigDecimal("flete"));
        op.setSeguro(rs.getBigDecimal("seguro"));
        op.setCif(rs.getBigDecimal("cif"));
        op.setTipoCambio(rs.getBigDecimal("tipo_cambio"));
        op.setAdValoremAplicado(rs.getBigDecimal("ad_valorem_aplicado"));
        op.setIscAplicado(rs.getBigDecimal("isc_aplicado"));
        op.setIgvAplicado(rs.getBigDecimal("igv_aplicado"));
        op.setIpmAplicado(rs.getBigDecimal("ipm_aplicado"));
        op.setPercepcionAplicada(rs.getBigDecimal("percepcion_aplicada"));
        op.setTotalImpuestos(rs.getBigDecimal("total_impuestos"));
        op.setCanalAsignado(rs.getString("canal_asignado"));
        op.setEstado(rs.getString("estado"));
        op.setNumeroDam(rs.getString("numero_dam"));
        op.setFechaNumeracion(rs.getTimestamp("fecha_numeracion"));
        op.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        return op;
    }

    private OperacionJpaRepositorio getJpaRepo() {
        return SpringContextHolder.getBeanOrNull(OperacionJpaRepositorio.class);
    }
}
