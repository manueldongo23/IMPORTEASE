package com.importease.proyecto.repository;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.jpa.TrackingEnvioEntity;
import com.importease.proyecto.model.jpa.TrackingEventoEntity;
import com.importease.proyecto.model.jpa.TrackingSyncLogEntity;
import com.importease.proyecto.repository.jpa.TrackingEnvioJpaRepository;
import com.importease.proyecto.repository.jpa.TrackingEventoJpaRepository;
import com.importease.proyecto.repository.jpa.TrackingSyncLogJpaRepository;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TrackingDAO {

    public long insertarEnvio(Integer usuarioId, Map<String, Object> envio) {
        TrackingEnvioJpaRepository repo = getEnvioRepo();
        if (repo != null) {
            try {
                TrackingEnvioEntity entity = toEnvioEntity(usuarioId, envio);
                TrackingEnvioEntity saved = repo.save(entity);
                if (saved.getId() != null) return saved.getId();
            } catch (Exception e) {
                LoggerUtil.warn("Fallo insertarEnvio con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO tracking_envios " +
                "(usuario_id, operacion_id, proveedor, tracking_number, bl_number, container_number, eta, estado_actual, source, source_type, confidence) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (usuarioId != null) ps.setInt(1, usuarioId); else ps.setNull(1, Types.INTEGER);
            Integer operacionId = asInteger(envio.get("operacionId"));
            if (operacionId != null) ps.setInt(2, operacionId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, str(envio.get("proveedor")));
            ps.setString(4, str(envio.get("trackingNumber")));
            ps.setString(5, str(envio.get("blNumber")));
            ps.setString(6, str(envio.get("containerNumber")));
            LocalDate eta = asDate(envio.get("eta"));
            if (eta != null) ps.setDate(7, Date.valueOf(eta)); else ps.setNull(7, Types.DATE);
            ps.setString(8, str(envio.get("estadoActual")));
            ps.setString(9, str(envio.get("source")));
            ps.setString(10, str(envio.get("sourceType")));
            ps.setBigDecimal(11, BigDecimal.valueOf(asDouble(envio.get("confidence"), 0.65)));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_envios no disponible o error al insertar envio: " + e.getMessage());
        }
        return -1L;
    }

    public void insertarEvento(long trackingId, Map<String, Object> evento) {
        if (trackingId <= 0) return;

        TrackingEventoJpaRepository repo = getEventoRepo();
        if (repo != null) {
            try {
                TrackingEventoEntity entity = toEventoEntity(trackingId, evento);
                repo.save(entity);
                return;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo insertarEvento con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO tracking_eventos " +
                "(tracking_id, fecha_evento, ubicacion, estado, descripcion, fuente, source_type, confidence, raw_payload_json) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, trackingId);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, str(evento.get("ubicacion")));
            ps.setString(4, str(evento.get("estado")));
            ps.setString(5, str(evento.get("descripcion")));
            ps.setString(6, str(evento.get("fuente")));
            ps.setString(7, str(evento.get("sourceType")));
            ps.setBigDecimal(8, BigDecimal.valueOf(asDouble(evento.get("confidence"), 0.65)));
            Object raw = evento.get("rawPayloadJson");
            if (raw != null) ps.setString(9, String.valueOf(raw)); else ps.setNull(9, Types.VARCHAR);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_eventos no disponible o error al insertar evento: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> listar(Integer usuarioId) {
        TrackingEnvioJpaRepository repo = getEnvioRepo();
        if (repo != null) {
            try {
                Integer scopedUserId = usuarioId != null ? usuarioId : -1;
                List<Map<String, Object>> out = new ArrayList<>();
                for (TrackingEnvioEntity entity : repo.findTop50ByUsuarioIdOrderByFechaCreacionDesc(scopedUserId)) {
                    out.add(envioMap(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listar con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT * FROM tracking_envios WHERE usuario_id = ? ORDER BY fecha_creacion DESC LIMIT 50";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId != null ? usuarioId : -1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(envioMap(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_envios no disponible para listar: " + e.getMessage());
        }
        return lista;
    }

    public Map<String, Object> detalle(long id, Integer usuarioId) {
        TrackingEnvioJpaRepository repo = getEnvioRepo();
        if (repo != null) {
            try {
                Integer scopedUserId = usuarioId != null ? usuarioId : -1;
                Optional<TrackingEnvioEntity> maybe = repo.findByIdAndUsuarioId(id, scopedUserId);
                if (maybe.isPresent()) return envioMap(maybe.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo detalle con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM tracking_envios WHERE id = ? AND usuario_id = ? LIMIT 1";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setInt(2, usuarioId != null ? usuarioId : -1);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return envioMap(rs);
            }
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_envios no disponible para detalle: " + e.getMessage());
        }
        return null;
    }

    public List<Map<String, Object>> eventos(long trackingId, Integer usuarioId) {
        TrackingEventoJpaRepository repo = getEventoRepo();
        if (repo != null) {
            try {
                Integer scopedUserId = usuarioId != null ? usuarioId : -1;
                List<Map<String, Object>> out = new ArrayList<>();
                for (TrackingEventoEntity entity : repo.listarPorTrackingYUsuario(trackingId, scopedUserId)) {
                    out.add(eventoMap(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo eventos con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT e.* FROM tracking_eventos e " +
                "JOIN tracking_envios t ON t.id = e.tracking_id " +
                "WHERE e.tracking_id = ? AND t.usuario_id = ? ORDER BY e.fecha_evento ASC, e.id ASC";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, trackingId);
            ps.setInt(2, usuarioId != null ? usuarioId : -1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(eventoMap(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_eventos no disponible para listar: " + e.getMessage());
        }
        return lista;
    }

    public void actualizarEstado(long id, Integer usuarioId, String estado, String sourceType, double confidence) {
        if (id <= 0) return;

        TrackingEnvioJpaRepository repo = getEnvioRepo();
        if (repo != null) {
            try {
                Integer scopedUserId = usuarioId != null ? usuarioId : -1;
                repo.actualizarEstado(id, scopedUserId, estado, sourceType, BigDecimal.valueOf(confidence));
                return;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizarEstado con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "UPDATE tracking_envios SET estado_actual = ?, source_type = ?, confidence = ?, ultima_actualizacion = NOW() " +
                "WHERE id = ? AND usuario_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, sourceType);
            ps.setBigDecimal(3, BigDecimal.valueOf(confidence));
            ps.setLong(4, id);
            ps.setInt(5, usuarioId != null ? usuarioId : -1);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_envios no disponible para actualizar estado: " + e.getMessage());
        }
    }

    public void registrarSync(Long trackingId, String proveedor, String endpoint, Integer statusHttp, String resultado, String mensaje) {
        TrackingSyncLogJpaRepository repo = getSyncRepo();
        if (repo != null) {
            try {
                TrackingSyncLogEntity entity = new TrackingSyncLogEntity();
                if (trackingId != null && trackingId > 0) entity.setTrackingId(trackingId);
                entity.setProveedor(proveedor);
                entity.setEndpoint(endpoint);
                entity.setStatusHttp(statusHttp);
                entity.setResultado(resultado);
                entity.setMensaje(mensaje);
                repo.save(entity);
                return;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo registrarSync con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO tracking_sync_log (tracking_id, proveedor, endpoint, status_http, resultado, mensaje) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (trackingId != null && trackingId > 0) ps.setLong(1, trackingId); else ps.setNull(1, Types.BIGINT);
            ps.setString(2, proveedor);
            ps.setString(3, endpoint);
            if (statusHttp != null) ps.setInt(4, statusHttp); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, resultado);
            ps.setString(6, mensaje);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.warn("tracking_sync_log no disponible: " + e.getMessage());
        }
    }

    private Map<String, Object> envioMap(ResultSet rs) throws SQLException {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", rs.getLong("id"));
        item.put("usuarioId", rs.getObject("usuario_id"));
        item.put("operacionId", rs.getObject("operacion_id"));
        item.put("proveedor", rs.getString("proveedor"));
        item.put("trackingNumber", rs.getString("tracking_number"));
        item.put("blNumber", rs.getString("bl_number"));
        item.put("containerNumber", rs.getString("container_number"));
        item.put("eta", rs.getString("eta"));
        item.put("estadoActual", rs.getString("estado_actual"));
        item.put("source", rs.getString("source"));
        item.put("sourceType", rs.getString("source_type"));
        item.put("confidence", rs.getBigDecimal("confidence"));
        item.put("ultimaActualizacion", rs.getString("ultima_actualizacion"));
        item.put("fechaCreacion", rs.getString("fecha_creacion"));
        return item;
    }

    private Map<String, Object> envioMap(TrackingEnvioEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entity.getId());
        item.put("usuarioId", entity.getUsuarioId());
        item.put("operacionId", entity.getOperacionId());
        item.put("proveedor", entity.getProveedor());
        item.put("trackingNumber", entity.getTrackingNumber());
        item.put("blNumber", entity.getBlNumber());
        item.put("containerNumber", entity.getContainerNumber());
        item.put("eta", entity.getEta() == null ? null : entity.getEta().toString());
        item.put("estadoActual", entity.getEstadoActual());
        item.put("source", entity.getSource());
        item.put("sourceType", entity.getSourceType());
        item.put("confidence", entity.getConfidence());
        item.put("ultimaActualizacion", entity.getUltimaActualizacion() == null ? null : entity.getUltimaActualizacion().toString());
        item.put("fechaCreacion", entity.getFechaCreacion() == null ? null : entity.getFechaCreacion().toString());
        return item;
    }

    private Map<String, Object> eventoMap(ResultSet rs) throws SQLException {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", rs.getLong("id"));
        item.put("trackingId", rs.getLong("tracking_id"));
        item.put("fechaEvento", rs.getString("fecha_evento"));
        item.put("ubicacion", rs.getString("ubicacion"));
        item.put("estado", rs.getString("estado"));
        item.put("descripcion", rs.getString("descripcion"));
        item.put("fuente", rs.getString("fuente"));
        item.put("sourceType", rs.getString("source_type"));
        item.put("confidence", rs.getBigDecimal("confidence"));
        return item;
    }

    private Map<String, Object> eventoMap(TrackingEventoEntity entity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", entity.getId());
        item.put("trackingId", entity.getTrackingId());
        item.put("fechaEvento", entity.getFechaEvento() == null ? null : entity.getFechaEvento().toString());
        item.put("ubicacion", entity.getUbicacion());
        item.put("estado", entity.getEstado());
        item.put("descripcion", entity.getDescripcion());
        item.put("fuente", entity.getFuente());
        item.put("sourceType", entity.getSourceType());
        item.put("confidence", entity.getConfidence());
        return item;
    }

    private TrackingEnvioEntity toEnvioEntity(Integer usuarioId, Map<String, Object> envio) {
        TrackingEnvioEntity entity = new TrackingEnvioEntity();
        entity.setUsuarioId(usuarioId);
        entity.setOperacionId(asInteger(envio.get("operacionId")));
        entity.setProveedor(str(envio.get("proveedor")));
        entity.setTrackingNumber(str(envio.get("trackingNumber")));
        entity.setBlNumber(str(envio.get("blNumber")));
        entity.setContainerNumber(str(envio.get("containerNumber")));
        LocalDate eta = asDate(envio.get("eta"));
        if (eta != null) entity.setEta(Date.valueOf(eta));
        entity.setEstadoActual(str(envio.get("estadoActual")));
        entity.setSource(str(envio.get("source")));
        entity.setSourceType(str(envio.get("sourceType")));
        entity.setConfidence(BigDecimal.valueOf(asDouble(envio.get("confidence"), 0.65)));
        return entity;
    }

    private TrackingEventoEntity toEventoEntity(long trackingId, Map<String, Object> evento) {
        TrackingEventoEntity entity = new TrackingEventoEntity();
        entity.setTrackingId(trackingId);
        entity.setFechaEvento(new Timestamp(System.currentTimeMillis()));
        entity.setUbicacion(str(evento.get("ubicacion")));
        entity.setEstado(str(evento.get("estado")));
        entity.setDescripcion(str(evento.get("descripcion")));
        entity.setFuente(str(evento.get("fuente")));
        entity.setSourceType(str(evento.get("sourceType")));
        entity.setConfidence(BigDecimal.valueOf(asDouble(evento.get("confidence"), 0.65)));
        Object raw = evento.get("rawPayloadJson");
        if (raw != null) entity.setRawPayloadJson(String.valueOf(raw));
        return entity;
    }

    private TrackingEnvioJpaRepository getEnvioRepo() {
        return SpringContextHolder.getBeanOrNull(TrackingEnvioJpaRepository.class);
    }

    private TrackingEventoJpaRepository getEventoRepo() {
        return SpringContextHolder.getBeanOrNull(TrackingEventoJpaRepository.class);
    }

    private TrackingSyncLogJpaRepository getSyncRepo() {
        return SpringContextHolder.getBeanOrNull(TrackingSyncLogJpaRepository.class);
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private double asDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private LocalDate asDate(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
