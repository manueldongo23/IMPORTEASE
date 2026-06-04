package com.importease.proyecto.repository;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.jpa.OperacionEntity;
import com.importease.proyecto.repository.jpa.OperacionJpaRepository;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

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

public class OperacionDAO implements IOperacionDAO {

    public boolean guardar(Operacion op) {
        OperacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                OperacionEntity saved = repo.save(toEntity(op));
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
        OperacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<OperacionEntity> entity = repo.findById(id);
                if (entity.isPresent()) return toModel(entity.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerPorId con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM operaciones WHERE id = ?";
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
        OperacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                List<Operacion> out = new ArrayList<>();
                for (OperacionEntity entity : repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarPorUsuario con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<Operacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM operaciones WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
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
        OperacionJpaRepository repo = getJpaRepo();
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
        OperacionJpaRepository repo = getJpaRepo();
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

    private OperacionJpaRepository getJpaRepo() {
        return SpringContextHolder.getBeanOrNull(OperacionJpaRepository.class);
    }

    private OperacionEntity toEntity(Operacion model) {
        OperacionEntity entity = new OperacionEntity();
        if (model.getId() > 0) entity.setId(model.getId());
        entity.setUsuarioId(model.getUsuarioId());
        entity.setProductoDesc(model.getProductoDesc());
        entity.setHsCode(model.getHsCode());
        entity.setPaisOrigen(model.getPaisOrigen());
        entity.setIncoterm(model.getIncoterm());
        entity.setFob(model.getFob());
        entity.setFlete(model.getFlete());
        entity.setSeguro(model.getSeguro());
        entity.setCif(model.getCif());
        entity.setTipoCambio(model.getTipoCambio());
        entity.setAdValoremAplicado(model.getAdValoremAplicado());
        entity.setIscAplicado(model.getIscAplicado());
        entity.setIgvAplicado(model.getIgvAplicado());
        entity.setIpmAplicado(model.getIpmAplicado());
        entity.setPercepcionAplicada(model.getPercepcionAplicada());
        entity.setTotalImpuestos(model.getTotalImpuestos());
        entity.setCanalAsignado(model.getCanalAsignado());
        entity.setEstado(model.getEstado());
        entity.setNumeroDam(model.getNumeroDam());
        entity.setFechaNumeracion(model.getFechaNumeracion());
        entity.setFechaCreacion(model.getFechaCreacion());
        return entity;
    }

    private Operacion toModel(OperacionEntity entity) {
        Operacion op = new Operacion();
        op.setId(entity.getId() == null ? 0 : entity.getId());
        op.setUsuarioId(entity.getUsuarioId() == null ? 0 : entity.getUsuarioId());
        op.setProductoDesc(entity.getProductoDesc());
        op.setHsCode(entity.getHsCode());
        op.setPaisOrigen(entity.getPaisOrigen());
        op.setIncoterm(entity.getIncoterm());
        op.setFob(entity.getFob());
        op.setFlete(entity.getFlete());
        op.setSeguro(entity.getSeguro());
        op.setCif(entity.getCif());
        op.setTipoCambio(entity.getTipoCambio());
        op.setAdValoremAplicado(entity.getAdValoremAplicado());
        op.setIscAplicado(entity.getIscAplicado());
        op.setIgvAplicado(entity.getIgvAplicado());
        op.setIpmAplicado(entity.getIpmAplicado());
        op.setPercepcionAplicada(entity.getPercepcionAplicada());
        op.setTotalImpuestos(entity.getTotalImpuestos());
        op.setCanalAsignado(entity.getCanalAsignado());
        op.setEstado(entity.getEstado());
        op.setNumeroDam(entity.getNumeroDam());
        op.setFechaNumeracion(entity.getFechaNumeracion());
        op.setFechaCreacion(entity.getFechaCreacion());
        return op;
    }
}
