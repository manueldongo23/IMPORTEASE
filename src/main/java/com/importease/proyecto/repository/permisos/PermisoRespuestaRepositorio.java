package com.importease.proyecto.repository.permisos;

import com.importease.proyecto.model.SolicitudPermisoDato;
import com.importease.proyecto.model.jpa.RespuestaPermisoOperacionEntity;
import com.importease.proyecto.model.jpa.SolicitudPermisoDatoEntity;
import com.importease.proyecto.repository.jpa.RespuestaPermisoOperacionJpaRepositorio;
import com.importease.proyecto.repository.jpa.SolicitudPermisoDatoJpaRepositorio;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermisoRespuestaRepositorio extends PermisoRepositorioSoporte {

    public boolean guardarDatosSolicitud(int solicitudId, List<SolicitudPermisoDato> datos) {
        SolicitudPermisoDatoJpaRepositorio repo = getDatoRepo();
        if (repo != null) {
            try {
                List<SolicitudPermisoDatoEntity> entities = new ArrayList<>();
                for (SolicitudPermisoDato dato : datos) {
                    SolicitudPermisoDatoEntity entity = toEntity(dato);
                    entity.setSolicitudPermisoId(solicitudId);
                    entities.add(entity);
                }
                repo.saveAll(entities);
                return true;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo guardarDatosSolicitud con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO solicitud_permiso_datos (solicitud_permiso_id, campo, valor, origen_dato) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (SolicitudPermisoDato dato : datos) {
                ps.setInt(1, solicitudId);
                ps.setString(2, dato.getCampo());
                ps.setString(3, dato.getValor());
                ps.setString(4, dato.getOrigenDato());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            LoggerUtil.error("Error al guardar datos de solicitud", e);
            return false;
        }
    }

    public boolean eliminarDatosSolicitud(int solicitudId) {
        SolicitudPermisoDatoJpaRepositorio repo = getDatoRepo();
        if (repo != null) {
            try {
                repo.deleteBySolicitudPermisoId(solicitudId);
                return true;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo eliminarDatosSolicitud con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "DELETE FROM solicitud_permiso_datos WHERE solicitud_permiso_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, solicitudId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LoggerUtil.error("Error al eliminar datos de solicitud", e);
            return false;
        }
    }

    public List<SolicitudPermisoDato> obtenerDatosSolicitud(int solicitudId) {
        SolicitudPermisoDatoJpaRepositorio repo = getDatoRepo();
        if (repo != null) {
            try {
                List<SolicitudPermisoDato> out = new ArrayList<>();
                for (SolicitudPermisoDatoEntity entity : repo.findBySolicitudPermisoId(solicitudId)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerDatosSolicitud con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<SolicitudPermisoDato> lista = new ArrayList<>();
        String sql = "SELECT * FROM solicitud_permiso_datos WHERE solicitud_permiso_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, solicitudId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SolicitudPermisoDato d = new SolicitudPermisoDato();
                d.setId(rs.getInt("id"));
                d.setSolicitudPermisoId(rs.getInt("solicitud_permiso_id"));
                d.setCampo(rs.getString("campo"));
                d.setValor(rs.getString("valor"));
                d.setOrigenDato(rs.getString("origen_dato"));
                lista.add(d);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener datos de solicitud", e);
        }
        return lista;
    }

    public boolean guardarRespuestasPermiso(int operacionId, Map<Integer, String> respuestas) {
        RespuestaPermisoOperacionJpaRepositorio repo = getRespuestaRepo();
        if (repo != null) {
            try {
                for (Map.Entry<Integer, String> entry : respuestas.entrySet()) {
                    Integer preguntaId = entry.getKey();
                    String respuesta = entry.getValue() != null ? entry.getValue().trim() : "";
                    repo.deleteByOperacionIdAndPreguntaId(operacionId, preguntaId);
                    RespuestaPermisoOperacionEntity entity = new RespuestaPermisoOperacionEntity();
                    entity.setOperacionId(operacionId);
                    entity.setPreguntaId(preguntaId);
                    entity.setRespuesta(respuesta);
                    repo.save(entity);
                }
                return true;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo guardarRespuestasPermiso con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String deleteSql = "DELETE FROM respuestas_permiso_operacion WHERE operacion_id = ? AND pregunta_id = ?";
        String insertSql = "INSERT INTO respuestas_permiso_operacion (operacion_id, pregunta_id, respuesta) VALUES (?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement delPs = con.prepareStatement(deleteSql);
                     PreparedStatement insPs = con.prepareStatement(insertSql)) {
                    for (Map.Entry<Integer, String> entry : respuestas.entrySet()) {
                        int preguntaId = entry.getKey();
                        String respuesta = entry.getValue();
                        delPs.setInt(1, operacionId);
                        delPs.setInt(2, preguntaId);
                        delPs.executeUpdate();
                        insPs.setInt(1, operacionId);
                        insPs.setInt(2, preguntaId);
                        insPs.setString(3, respuesta != null ? respuesta.trim() : "");
                        insPs.executeUpdate();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback();
                LoggerUtil.error("Error al guardar respuestas de permisos en transaccion", ex);
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al abrir conexion para guardar respuestas", e);
            return false;
        }
    }
}
