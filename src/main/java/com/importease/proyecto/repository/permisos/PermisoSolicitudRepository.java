package com.importease.proyecto.repository.permisos;

import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.model.jpa.SolicitudPermisoEntity;
import com.importease.proyecto.repository.jpa.SolicitudPermisoJpaRepository;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermisoSolicitudRepository extends PermisoRepositorySupport {
    private final PermisoRespuestaRepository respuestaRepository;

    public PermisoSolicitudRepository() {
        this(new PermisoRespuestaRepository());
    }

    public PermisoSolicitudRepository(PermisoRespuestaRepository respuestaRepository) {
        this.respuestaRepository = respuestaRepository;
    }

    public int crearSolicitud(SolicitudPermiso sol) {
        SolicitudPermisoJpaRepository repo = getSolicitudRepo();
        if (repo != null) {
            try {
                SolicitudPermisoEntity saved = repo.save(toEntity(sol));
                if (saved.getId() != null) {
                    sol.setId(saved.getId());
                    return saved.getId();
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo crearSolicitud con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO solicitudes_permiso (operacion_id, usuario_id, codigo_entidad, tipo_permiso, estado, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sol.getOperacionId());
            ps.setInt(2, sol.getUsuarioId());
            ps.setString(3, sol.getCodigoEntidad());
            ps.setString(4, sol.getTipoPermiso());
            ps.setString(5, sol.getEstado());
            ps.setString(6, sol.getObservaciones());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    sol.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al crear solicitud de permiso", e);
        }
        return -1;
    }

    public boolean actualizarEstado(int solicitudId, String nuevoEstado) {
        SolicitudPermisoJpaRepository repo = getSolicitudRepo();
        if (repo != null) {
            try {
                return repo.actualizarEstado(solicitudId, nuevoEstado) > 0;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizarEstado con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "UPDATE solicitudes_permiso SET estado = ? WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, solicitudId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al actualizar estado de solicitud", e);
            return false;
        }
    }

    public boolean registrarSuce(int solicitudId, String suce, String resolucion) {
        SolicitudPermisoJpaRepository repo = getSolicitudRepo();
        if (repo != null) {
            try {
                Optional<SolicitudPermisoEntity> maybe = repo.findById(solicitudId);
                if (maybe.isPresent()) {
                    SolicitudPermisoEntity s = maybe.get();
                    String estado = estadoPorResolucion(resolucion);
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    s.setNumeroSuce(suce);
                    s.setNumeroDocumentoResolutivo(resolucion);
                    s.setEstado(estado);
                    if (s.getFechaEnvioVuce() == null) s.setFechaEnvioVuce(now);
                    if ("APROBADO".equals(estado) && s.getFechaAprobacion() == null) s.setFechaAprobacion(now);
                    repo.save(s);
                    return true;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo registrarSuce con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String estado = estadoPorResolucion(resolucion);
        String sql = "UPDATE solicitudes_permiso SET numero_suce = ?, numero_documento_resolutivo = ?, estado = ?, " +
                     "fecha_envio_vuce = CASE WHEN fecha_envio_vuce IS NULL THEN CURRENT_TIMESTAMP ELSE fecha_envio_vuce END, " +
                     "fecha_aprobacion = CASE WHEN ? = 'APROBADO' THEN CURRENT_TIMESTAMP ELSE fecha_aprobacion END " +
                     "WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, suce);
            ps.setString(2, resolucion);
            ps.setString(3, estado);
            ps.setString(4, estado);
            ps.setInt(5, solicitudId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar SUCE en solicitud", e);
            return false;
        }
    }

    public SolicitudPermiso obtenerSolicitud(int id) {
        SolicitudPermisoJpaRepository repo = getSolicitudRepo();
        if (repo != null) {
            try {
                Optional<SolicitudPermisoEntity> maybe = repo.findById(id);
                if (maybe.isPresent()) {
                    SolicitudPermiso sol = toModel(maybe.get());
                    sol.setDatos(respuestaRepository.obtenerDatosSolicitud(sol.getId()));
                    return sol;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerSolicitud con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM solicitudes_permiso WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SolicitudPermiso sol = mapearSolicitud(rs);
                sol.setDatos(respuestaRepository.obtenerDatosSolicitud(sol.getId()));
                return sol;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener solicitud por ID", e);
        }
        return null;
    }

    public List<SolicitudPermiso> listarSolicitudesPorUsuario(int usuarioId) {
        SolicitudPermisoJpaRepository repo = getSolicitudRepo();
        if (repo != null) {
            try {
                List<SolicitudPermiso> out = new ArrayList<>();
                for (SolicitudPermisoEntity entity : repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarSolicitudesPorUsuario con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<SolicitudPermiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitudes_permiso WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearSolicitud(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar solicitudes por usuario", e);
        }
        return lista;
    }

    public List<SolicitudPermiso> listarSolicitudesPorOperacion(int operacionId) {
        SolicitudPermisoJpaRepository repo = getSolicitudRepo();
        if (repo != null) {
            try {
                List<SolicitudPermiso> out = new ArrayList<>();
                for (SolicitudPermisoEntity entity : repo.findByOperacionIdOrderByFechaCreacionDesc(operacionId)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarSolicitudesPorOperacion con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<SolicitudPermiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitudes_permiso WHERE operacion_id = ? ORDER BY fecha_creacion DESC";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearSolicitud(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar solicitudes por operacion", e);
        }
        return lista;
    }

    private String estadoPorResolucion(String resolucion) {
        return (resolucion != null && !resolucion.trim().isEmpty()) ? "APROBADO" : "ENVIADO_A_VUCE";
    }
}
