package com.importease.proyecto.repository.documentos;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Repositorio de base de datos para la entidad de Documento.
 * Administra los metadatos de documentos asociados a las importaciones.
 */
public class DocumentoRepositorio {
    private static final Set<String> TIPOS_DOCUMENTO_PERMITIDOS = Set.of(
            "FACTURA_COMERCIAL",
            "BILL_OF_LADING",
            "CERTIFICADO_ORIGEN"
    );

    public List<Map<String, Object>> listarDocumentos(int importacionId) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT * FROM documentos_importacion WHERE importacion_id = ? AND soft_delete = FALSE ORDER BY id";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, importacionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapDocumentoListado(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar documentos en DB", e);
        }
        return lista;
    }

    public Map<String, Object> obtenerDocumentoPorId(int id) {
        String sql = "SELECT * FROM documentos_importacion WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDocumentoDetalle(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener documento por ID", e);
        }
        return null;
    }

    public boolean registrarDocumento(int importacionId, String tipoDoc, String rutaArchivo, String checksumHash) {
        if (!TIPOS_DOCUMENTO_PERMITIDOS.contains(tipoDoc)) {
            LoggerUtil.warn("Intento de registrar tipoDocumento no permitido: " + tipoDoc);
            return false;
        }

        String checkSql = "SELECT id FROM documentos_importacion WHERE importacion_id = ? AND tipo_documento = ?";
        try (Connection con = ConexionDB.obtenerConexion()) {
            Integer docId = buscarDocumentoExistente(con, checkSql, importacionId, tipoDoc);
            if (docId != null) {
                actualizarDocumentoExistente(con, docId, rutaArchivo, checksumHash);
            } else {
                insertarDocumento(con, importacionId, tipoDoc, rutaArchivo, checksumHash);
            }
            actualizarOperacionFichas(con, importacionId, tipoDoc, 1);
            return true;
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar documento en base de datos", e);
        }
        return false;
    }

    public boolean eliminarDocumento(int id, int importacionId, String tipoDoc) {
        String sql = "UPDATE documentos_importacion SET soft_delete = TRUE WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            actualizarOperacionFichas(con, importacionId, tipoDoc, 0);
            return true;
        } catch (SQLException e) {
            LoggerUtil.error("Error al eliminar documento en base de datos", e);
        }
        return false;
    }

    public void registrarEventoAuditoria(int usuarioId, String accion, String entidadId, int operacionId, String detalle, String ip) {
        String sql = "INSERT INTO auditoria_eventos (usuario_id, accion, modulo, entidad_afectada, entidad_id, detalle, ip_origen) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, "documentos_importacion");
            ps.setString(4, entidadId);
            ps.setInt(5, operacionId);
            ps.setString(6, detalle);
            ps.setString(7, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar auditoria en documentos", e);
        }
    }

    private Map<String, Object> mapDocumentoListado(ResultSet rs) throws SQLException {
        Map<String, Object> m = mapDocumentoDetalle(rs);
        m.put("es_obligatorio", rs.getBoolean("es_obligatorio"));
        m.put("fecha_subida", rs.getTimestamp("fecha_subida") != null ? rs.getTimestamp("fecha_subida").toString() : "");
        m.put("checksum_hash", rs.getString("checksum_hash") != null ? rs.getString("checksum_hash") : "");
        return m;
    }

    private Map<String, Object> mapDocumentoDetalle(ResultSet rs) throws SQLException {
        Map<String, Object> m = new HashMap<>();
        m.put("id", rs.getInt("id"));
        m.put("importacion_id", rs.getInt("importacion_id"));
        m.put("tipo_documento", rs.getString("tipo_documento"));
        m.put("ruta_archivo", rs.getString("ruta_archivo"));
        m.put("checksum_hash", rs.getString("checksum_hash"));
        return m;
    }

    private Integer buscarDocumentoExistente(Connection con, String checkSql, int importacionId, String tipoDoc) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, importacionId);
            ps.setString(2, tipoDoc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }

    private void actualizarDocumentoExistente(Connection con, int docId, String rutaArchivo, String checksumHash) throws SQLException {
        String updateSql = "UPDATE documentos_importacion SET ruta_archivo = ?, checksum_hash = ?, soft_delete = FALSE, fecha_subida = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(updateSql)) {
            ps.setString(1, rutaArchivo);
            ps.setString(2, checksumHash);
            ps.setInt(3, docId);
            ps.executeUpdate();
        }
    }

    private void insertarDocumento(Connection con, int importacionId, String tipoDoc, String rutaArchivo, String checksumHash) throws SQLException {
        String insertSql = "INSERT INTO documentos_importacion (importacion_id, tipo_documento, ruta_archivo, es_obligatorio, checksum_hash, soft_delete) VALUES (?, ?, ?, FALSE, ?, FALSE)";
        try (PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, importacionId);
            ps.setString(2, tipoDoc);
            ps.setString(3, rutaArchivo);
            ps.setString(4, checksumHash);
            ps.executeUpdate();
        }
    }

    private void actualizarOperacionFichas(Connection con, int importacionId, String tipoDoc, int valor) throws SQLException {
        String colName = columnaDocumento(normalizarTipoDocumento(tipoDoc));
        if (colName == null) {
            return;
        }

        String sql = "UPDATE operaciones SET " + colName + " = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, valor);
            ps.setInt(2, importacionId);
            ps.executeUpdate();
        }
    }

    private String columnaDocumento(String tipoDoc) {
        if ("FACTURA_COMERCIAL".equals(tipoDoc)) {
            return "documento_factura";
        }
        if ("BILL_OF_LADING".equals(tipoDoc)) {
            return "documento_bl";
        }
        if ("CERTIFICADO_ORIGEN".equals(tipoDoc)) {
            return "documento_certificado_origen";
        }
        return null;
    }

    private String normalizarTipoDocumento(String tipoDoc) {
        if (tipoDoc == null) {
            return "";
        }
        return tipoDoc.trim().toUpperCase(Locale.ROOT);
    }
}
