package com.importease.proyecto.repository;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.model.jpa.OperacionEntity;
import com.importease.proyecto.repository.jpa.ImportacionJpaRepository;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImportacionDAO {

    public boolean validarDocumentosParaDespacho(int importacionId) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Object[] row = repo.obtenerEstadoDocumentos(importacionId);
                if (row != null && row.length >= 3) {
                    return asBoolean(row[0]) && asBoolean(row[1]) && asBoolean(row[2]);
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo validarDocumentosParaDespacho con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        try (Connection con = ConexionDB.obtenerConexion()) {
            return validarDocumentosParaDespacho(con, importacionId);
        }
    }

    public boolean validarDocumentosParaDespacho(Connection con, int importacionId) throws SQLException {
        if (con == null) {
            return validarDocumentosParaDespacho(importacionId);
        }
        String sql = "SELECT documento_factura, documento_bl, documento_certificado_origen FROM operaciones WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, importacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("documento_factura") &&
                        rs.getBoolean("documento_bl") &&
                        rs.getBoolean("documento_certificado_origen");
                }
            }
        }
        return false;
    }

    public void insertar(Importacion imp) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                OperacionEntity saved = repo.save(toEntity(imp));
                if (saved.getId() != null) imp.setId(saved.getId());
                return;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo insertar con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        try (Connection con = ConexionDB.obtenerConexion()) {
            insertar(con, imp);
        }
    }

    public void insertar(Connection con, Importacion imp) throws SQLException {
        if (con == null) {
            insertar(imp);
            return;
        }
        String sql = "INSERT INTO operaciones (usuario_id, producto_desc, hs_code, pais_origen, incoterm, " +
            "fob, flete, seguro, cif, tipo_cambio, ad_valorem_aplicado, isc_aplicado, " +
            "igv_aplicado, ipm_aplicado, percepcion_aplicada, total_impuestos, estado, usado) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, imp.getUsuarioId());
            ps.setString(2, imp.getProductoDesc());
            ps.setString(3, imp.getHsCode());
            ps.setString(4, imp.getPaisOrigen());
            ps.setString(5, imp.getIncoterm() != null ? imp.getIncoterm() : "FOB");
            ps.setBigDecimal(6, bd(imp.getValorFobBD()));
            ps.setBigDecimal(7, bd(imp.getFleteBD()));
            ps.setBigDecimal(8, bd(imp.getSeguroBD()));
            ps.setBigDecimal(9, bd(imp.getValorCifBD()));
            ps.setBigDecimal(10, bd(imp.getTipoCambioBD()));
            ps.setBigDecimal(11, bd(imp.getMontoAdValoremBD()));
            ps.setBigDecimal(12, bd(imp.getMontoIscBD()));
            ps.setBigDecimal(13, bd(imp.getMontoIgbBD()));
            ps.setBigDecimal(14, bd(imp.getMontoIpmBD()));
            ps.setBigDecimal(15, bd(imp.getMontoPercepcionBD()));
            ps.setBigDecimal(16, bd(imp.getTotalImpuestosBD()));
            ps.setString(17, imp.getEstado());
            ps.setBoolean(18, imp.isUsado());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    imp.setId(rs.getInt(1));
                }
            }
        }
    }

    public boolean actualizarEstado(int importacionId, String nuevoEstado) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                return repo.actualizarEstado(importacionId, nuevoEstado) > 0;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizarEstado con JPA, fallback JDBC: " + e.getMessage());
            }
        }
        try (Connection con = ConexionDB.obtenerConexion()) {
            return actualizarEstado(con, importacionId, nuevoEstado);
        }
    }

    public boolean actualizarEstado(Connection con, int importacionId, String nuevoEstado) throws SQLException {
        if (con == null) {
            return actualizarEstado(importacionId, nuevoEstado);
        }
        String sql = "UPDATE operaciones SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, importacionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean marcarDocumentoSubido(int id, String tipo) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                if ("FACTURA_COMERCIAL".equals(tipo)) return repo.marcarDocumentoFactura(id) > 0;
                if ("BILL_OF_LADING".equals(tipo)) return repo.marcarDocumentoBl(id) > 0;
                if ("CERTIFICADO_ORIGEN".equals(tipo)) return repo.marcarDocumentoCertificadoOrigen(id) > 0;
                return false;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo marcarDocumentoSubido con JPA, fallback JDBC: " + e.getMessage());
            }
        }
        try (Connection con = ConexionDB.obtenerConexion()) {
            return marcarDocumentoSubido(con, id, tipo);
        }
    }

    public boolean marcarDocumentoSubido(Connection con, int id, String tipo) throws SQLException {
        if (con == null) {
            return marcarDocumentoSubido(id, tipo);
        }
        String col = "";
        if ("FACTURA_COMERCIAL".equals(tipo)) col = "documento_factura";
        else if ("BILL_OF_LADING".equals(tipo)) col = "documento_bl";
        else if ("CERTIFICADO_ORIGEN".equals(tipo)) col = "documento_certificado_origen";

        if (col.isEmpty()) return false;

        String sql = "UPDATE operaciones SET " + col + " = TRUE WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean tieneArchivoDocumento(Connection con, int importacionId, String tipo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM documentos_importacion WHERE importacion_id = ? AND tipo_documento = ? AND soft_delete = FALSE AND ruta_archivo IS NOT NULL AND ruta_archivo <> ''";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, importacionId);
            ps.setString(2, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean actualizarDam(int id, String numeroDam, String canal) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                return repo.actualizarDam(id, numeroDam, canal) > 0;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizarDam con JPA, fallback JDBC: " + e.getMessage());
            }
        }
        try (Connection con = ConexionDB.obtenerConexion()) {
            return actualizarDam(con, id, numeroDam, canal);
        }
    }

    public boolean actualizarDam(Connection con, int id, String numeroDam, String canal) throws SQLException {
        if (con == null) {
            return actualizarDam(id, numeroDam, canal);
        }
        String sql = "UPDATE operaciones SET numero_dam = ?, canal_asignado = ?, fecha_numeracion = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroDam);
            ps.setString(2, canal);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Importacion> listarPorUsuario(int usuarioId) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                List<Importacion> lista = new ArrayList<>();
                for (OperacionEntity entity : repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)) {
                    lista.add(toModel(entity));
                }
                return lista;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarPorUsuario con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        try (Connection con = ConexionDB.obtenerConexion()) {
            return listarPorUsuario(con, usuarioId);
        }
    }

    public List<Importacion> listarPorUsuario(Connection con, int usuarioId) throws SQLException {
        if (con == null) {
            return listarPorUsuario(usuarioId);
        }
        List<Importacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM operaciones WHERE usuario_id = ? ORDER BY fecha_creacion DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearImportacion(rs));
                }
            }
        }
        return lista;
    }

    public Importacion buscarPorId(int id) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<OperacionEntity> entity = repo.findById(id);
                if (entity.isPresent()) return toModel(entity.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarPorId con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        try (Connection con = ConexionDB.obtenerConexion()) {
            return buscarPorId(con, id);
        }
    }

    public Importacion buscarPorId(Connection con, int id) throws SQLException {
        if (con == null) {
            return buscarPorId(id);
        }
        String sql = "SELECT * FROM operaciones WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearImportacion(rs);
                }
            }
        }
        return null;
    }

    public Map<String, Object> obtenerEstadisticas(int usuarioId) throws SQLException {
        ImportacionJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Map<String, Object> stats = new HashMap<>();
                stats.put("consultasHs", valueOrZero(repo.contarConsultasHs(usuarioId)));

                Object[] row = repo.obtenerEstadisticas(usuarioId);
                if (row != null && row.length >= 8) {
                    int totalOps = asInt(row[2]);
                    stats.put("totalImpuestos", asBigDecimal(row[0]));
                    stats.put("totalFob", asBigDecimal(row[1]));
                    stats.put("totalOps", totalOps);
                    stats.put("verde", asInt(row[3]));
                    stats.put("naranja", asInt(row[4]));
                    stats.put("rojo", asInt(row[5]));
                    stats.put("observadas", asInt(row[6]));
                    stats.put("restringidos", asInt(row[7]));
                } else {
                    stats.put("totalImpuestos", BigDecimal.ZERO);
                    stats.put("totalFob", BigDecimal.ZERO);
                    stats.put("totalOps", 0);
                    stats.put("verde", 0);
                    stats.put("naranja", 0);
                    stats.put("rojo", 0);
                    stats.put("observadas", 0);
                    stats.put("restringidos", 0);
                }

                String entidad = repo.obtenerEntidadMasFrecuente(usuarioId);
                stats.put("entidadMasFrecuente", (entidad == null || entidad.isBlank()) ? "Sin datos" : entidad);

                String pais = repo.obtenerPaisMasFrecuente(usuarioId);
                stats.put("paisMasFrecuente", (pais == null || pais.isBlank()) ? "Sin datos" : pais);

                BigDecimal ahorro = repo.obtenerTlcAhorro(usuarioId);
                stats.put("tlcAhorro", ahorro != null ? ahorro : BigDecimal.ZERO);
                return stats;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerEstadisticas con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        try (Connection con = ConexionDB.obtenerConexion()) {
            return obtenerEstadisticas(con, usuarioId);
        }
    }

    public Map<String, Object> obtenerEstadisticas(Connection con, int usuarioId) throws SQLException {
        if (con == null) {
            return obtenerEstadisticas(usuarioId);
        }
        Map<String, Object> stats = new HashMap<>();

        int consultasHs = 0;
        String sqlConsultas = "SELECT COUNT(*) FROM log_busquedas WHERE usuario_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlConsultas)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt(1);
                    if (cnt > 0) consultasHs = cnt;
                }
            }
        } catch (Exception e) {
            // fallback a cero
        }
        stats.put("consultasHs", consultasHs);

        String sql = "SELECT " +
            "SUM(total_impuestos) as total_impuestos, " +
            "SUM(fob) as total_fob, " +
            "COUNT(*) as total_ops, " +
            "SUM(CASE WHEN canal_asignado = 'VERDE' THEN 1 ELSE 0 END) as verde, " +
            "SUM(CASE WHEN canal_asignado = 'NARANJA' THEN 1 ELSE 0 END) as naranja, " +
            "SUM(CASE WHEN canal_asignado = 'ROJO' THEN 1 ELSE 0 END) as rojo, " +
            "SUM(CASE WHEN canal_asignado IN ('ROJO', 'NARANJA') THEN 1 ELSE 0 END) as observadas, " +
            "SUM(CASE WHEN entidad_vuce IS NOT NULL OR permiso_vuce_obtenido = TRUE THEN 1 ELSE 0 END) as restringidos " +
            "FROM operaciones WHERE usuario_id = ?";

        int totalOps = 0;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalOps = rs.getInt("total_ops");
                    stats.put("totalImpuestos", rs.getBigDecimal("total_impuestos") != null ? rs.getBigDecimal("total_impuestos") : BigDecimal.ZERO);
                    stats.put("totalFob", rs.getBigDecimal("total_fob") != null ? rs.getBigDecimal("total_fob") : BigDecimal.ZERO);
                    stats.put("totalOps", totalOps);
                    stats.put("verde", rs.getInt("verde"));
                    stats.put("naranja", rs.getInt("naranja"));
                    stats.put("rojo", rs.getInt("rojo"));
                    stats.put("observadas", rs.getInt("observadas"));
                    stats.put("restringidos", rs.getInt("restringidos"));
                }
            }
        }

        String entidadMasFrecuente = "Sin datos";
        String sqlEnt = "SELECT entidad_vuce, COUNT(*) as qty FROM operaciones WHERE usuario_id = ? AND entidad_vuce IS NOT NULL GROUP BY entidad_vuce ORDER BY qty DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlEnt)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    entidadMasFrecuente = rs.getString("entidad_vuce");
                }
            }
        } catch (Exception e) {
            // fallback
        }
        stats.put("entidadMasFrecuente", entidadMasFrecuente);

        String paisMasFrecuente = "Sin datos";
        String sqlPais = "SELECT pais_origen, COUNT(*) as qty FROM operaciones WHERE usuario_id = ? AND pais_origen IS NOT NULL GROUP BY pais_origen ORDER BY qty DESC LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlPais)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    paisMasFrecuente = rs.getString("pais_origen");
                }
            }
        } catch (Exception e) {
            // fallback
        }
        stats.put("paisMasFrecuente", paisMasFrecuente);

        BigDecimal tlcAhorro = BigDecimal.ZERO;
        if (totalOps > 0) {
            String sqlTlc = "SELECT SUM(cif * 0.06) FROM operaciones WHERE usuario_id = ? AND (incoterm = 'CIF' OR incoterm IS NOT NULL) AND canal_asignado = 'VERDE'";
            try (PreparedStatement ps = con.prepareStatement(sqlTlc)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBigDecimal(1) != null) {
                        tlcAhorro = rs.getBigDecimal(1);
                    }
                }
            } catch (Exception e) {
                // fallback
            }
        }
        stats.put("tlcAhorro", tlcAhorro);

        return stats;
    }

    private Importacion mapearImportacion(ResultSet rs) throws SQLException {
        Importacion imp = new Importacion();
        imp.setId(rs.getInt("id"));
        imp.setUsuarioId(rs.getInt("usuario_id"));
        imp.setHsCode(rs.getString("hs_code"));
        imp.setProductoDesc(rs.getString("producto_desc"));
        imp.setPaisOrigen(rs.getString("pais_origen"));
        imp.setIncoterm(rs.getString("incoterm"));
        imp.setValorFobBD(rs.getBigDecimal("fob"));
        imp.setFleteBD(rs.getBigDecimal("flete"));
        imp.setSeguroBD(rs.getBigDecimal("seguro"));
        imp.setValorCifBD(rs.getBigDecimal("cif"));
        imp.setTipoCambioBD(rs.getBigDecimal("tipo_cambio"));
        imp.setMontoAdValoremBD(rs.getBigDecimal("ad_valorem_aplicado"));
        imp.setMontoIscBD(rs.getBigDecimal("isc_aplicado"));
        imp.setMontoIgbBD(rs.getBigDecimal("igv_aplicado"));
        imp.setMontoIpmBD(rs.getBigDecimal("ipm_aplicado"));
        imp.setMontoPercepcionBD(rs.getBigDecimal("percepcion_aplicada"));
        imp.setTotalImpuestosBD(rs.getBigDecimal("total_impuestos"));
        imp.setEstado(rs.getString("estado"));
        imp.setCanalAsignado(rs.getString("canal_asignado"));
        imp.setNumeroDam(rs.getString("numero_dam"));
        imp.setFechaNumeracion(rs.getTimestamp("fecha_numeracion"));
        imp.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        imp.setUsado(rs.getBoolean("usado"));
        return imp;
    }

    public void registrarHistorialEstado(int operacionId, String estadoAnterior, String estadoNuevo, String observacion, Integer usuarioId) throws SQLException {
        try (Connection con = ConexionDB.obtenerConexion()) {
            registrarHistorialEstado(con, operacionId, estadoAnterior, estadoNuevo, observacion, usuarioId);
        }
    }

    public void registrarHistorialEstado(Connection con, int operacionId, String estadoAnterior, String estadoNuevo, String observacion, Integer usuarioId) throws SQLException {
        if (con == null) {
            registrarHistorialEstado(operacionId, estadoAnterior, estadoNuevo, observacion, usuarioId);
            return;
        }
        String sql = "INSERT INTO historial_estado_operacion (operacion_id, estado_anterior, estado_nuevo, observacion, usuario_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            if (estadoAnterior != null) ps.setString(2, estadoAnterior);
            else ps.setNull(2, java.sql.Types.VARCHAR);
            ps.setString(3, estadoNuevo);
            if (observacion != null) ps.setString(4, observacion);
            else ps.setNull(4, java.sql.Types.VARCHAR);
            if (usuarioId != null) ps.setInt(5, usuarioId);
            else ps.setNull(5, java.sql.Types.INTEGER);
            ps.executeUpdate();
        }
    }

    private ImportacionJpaRepository getJpaRepo() {
        return SpringContextHolder.getBeanOrNull(ImportacionJpaRepository.class);
    }

    private OperacionEntity toEntity(Importacion imp) {
        OperacionEntity entity = new OperacionEntity();
        if (imp.getId() > 0) entity.setId(imp.getId());
        entity.setUsuarioId(imp.getUsuarioId());
        entity.setProductoDesc(imp.getProductoDesc());
        entity.setHsCode(imp.getHsCode());
        entity.setPaisOrigen(imp.getPaisOrigen());
        entity.setIncoterm(imp.getIncoterm() != null ? imp.getIncoterm() : "FOB");
        entity.setFob(bd(imp.getValorFobBD()));
        entity.setFlete(bd(imp.getFleteBD()));
        entity.setSeguro(bd(imp.getSeguroBD()));
        entity.setCif(bd(imp.getValorCifBD()));
        entity.setTipoCambio(bd(imp.getTipoCambioBD()));
        entity.setAdValoremAplicado(bd(imp.getMontoAdValoremBD()));
        entity.setIscAplicado(bd(imp.getMontoIscBD()));
        entity.setIgvAplicado(bd(imp.getMontoIgbBD()));
        entity.setIpmAplicado(bd(imp.getMontoIpmBD()));
        entity.setPercepcionAplicada(bd(imp.getMontoPercepcionBD()));
        entity.setTotalImpuestos(bd(imp.getTotalImpuestosBD()));
        entity.setEstado(imp.getEstado());
        entity.setCanalAsignado(imp.getCanalAsignado());
        entity.setNumeroDam(imp.getNumeroDam());
        entity.setFechaNumeracion(imp.getFechaNumeracion());
        entity.setFechaCreacion(imp.getFechaCreacion());
        entity.setUsado(imp.isUsado());
        return entity;
    }

    private Importacion toModel(OperacionEntity entity) {
        Importacion imp = new Importacion();
        imp.setId(entity.getId() == null ? 0 : entity.getId());
        imp.setUsuarioId(entity.getUsuarioId() == null ? 0 : entity.getUsuarioId());
        imp.setHsCode(entity.getHsCode());
        imp.setProductoDesc(entity.getProductoDesc());
        imp.setPaisOrigen(entity.getPaisOrigen());
        imp.setIncoterm(entity.getIncoterm());
        imp.setValorFobBD(entity.getFob());
        imp.setFleteBD(entity.getFlete());
        imp.setSeguroBD(entity.getSeguro());
        imp.setValorCifBD(entity.getCif());
        imp.setTipoCambioBD(entity.getTipoCambio());
        imp.setMontoAdValoremBD(entity.getAdValoremAplicado());
        imp.setMontoIscBD(entity.getIscAplicado());
        imp.setMontoIgbBD(entity.getIgvAplicado());
        imp.setMontoIpmBD(entity.getIpmAplicado());
        imp.setMontoPercepcionBD(entity.getPercepcionAplicada());
        imp.setTotalImpuestosBD(entity.getTotalImpuestos());
        imp.setEstado(entity.getEstado());
        imp.setCanalAsignado(entity.getCanalAsignado());
        imp.setNumeroDam(entity.getNumeroDam());
        imp.setFechaNumeracion(entity.getFechaNumeracion());
        imp.setFechaCreacion(entity.getFechaCreacion());
        imp.setUsado(Boolean.TRUE.equals(entity.getUsado()));
        return imp;
    }

    private BigDecimal bd(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private boolean asBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        return "true".equalsIgnoreCase(String.valueOf(value)) || "1".equals(String.valueOf(value));
    }

    private int asInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal asBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
