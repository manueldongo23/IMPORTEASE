package com.importease.proyecto.service.guidedflow;

import com.importease.proyecto.dto.IncidenciaCoherenciaDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.service.CoherenciaAduaneraServicio;
import com.importease.proyecto.service.FlujoGuiadoServicio;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.PredamValidacionException;
import com.importease.proyecto.service.PredamValidacionServicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class FlujoGuiadoReglaServicio {
    private final ImportacionRepositorio importacionRepositorio;

    public FlujoGuiadoReglaServicio() {
        this(new ImportacionRepositorio());
    }

    public FlujoGuiadoReglaServicio(ImportacionRepositorio importacionRepositorio) {
        this.importacionRepositorio = importacionRepositorio;
    }

    public String validateStep(Connection con, int paso, int expedienteId) {
        try {
            switch (paso) {
                case FlujoGuiadoServicio.PASO_INTENCION:
                    return null;
                case FlujoGuiadoServicio.PASO_DATOS_BASICOS:
                    return validarDatosBasicos(con, expedienteId);
                case FlujoGuiadoServicio.PASO_CLASIFICACION:
                    return validarClasificacion(con, expedienteId);
                case FlujoGuiadoServicio.PASO_TRANSPORTE:
                    return validarTransporte(con, expedienteId);
                case FlujoGuiadoServicio.PASO_DOCUMENTOS:
                    return validarDocumentos(con, expedienteId);
                case FlujoGuiadoServicio.PASO_COHERENCIA:
                    return validarCoherencia(expedienteId);
                case FlujoGuiadoServicio.PASO_DTA_PRE_DAM:
                    return validarPredam(con, expedienteId);
                case FlujoGuiadoServicio.PASO_REVISION_FINAL:
                    return null;
                default:
                    return "Paso desconocido";
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al validar paso " + paso, e);
            return "Error interno al validar el paso";
        }
    }

    private String validarDatosBasicos(Connection con, int expedienteId) throws SQLException {
        String sql = "SELECT producto_desc, pais_origen, incoterm, fob FROM operaciones WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "Expediente no encontrado";
                if (isBlank(rs.getString("producto_desc"))) return "La descripcion del producto es obligatoria";
                if (isBlank(rs.getString("pais_origen"))) return "El pais de origen es obligatorio";
                if (isBlank(rs.getString("incoterm"))) return "El Incoterm es obligatorio";
                if (rs.getDouble("fob") <= 0) return "El valor FOB debe ser mayor a 0";
            }
        }
        return null;
    }

    private String validarClasificacion(Connection con, int expedienteId) throws SQLException {
        String sql = "SELECT hs_code FROM operaciones WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "Expediente no encontrado";
                String hsCode = rs.getString("hs_code");
                if (hsCode == null || !hsCode.matches("\\d{6,10}")) {
                    return "El HS Code debe tener entre 6 y 10 digitos numericos";
                }
            }
        }
        return null;
    }

    private String validarTransporte(Connection con, int expedienteId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM manifiestos_carga WHERE operacion_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return null;
            }
        }
        return "Debe registrar al menos un manifiesto de carga";
    }

    private String validarDocumentos(Connection con, int expedienteId) throws SQLException {
        String sql = "SELECT tipo_documento, subido FROM documentos_importacion WHERE importacion_id = ? AND es_obligatorio = TRUE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    if (!rs.getBoolean("subido")) {
                        return "Documento obligatorio pendiente: " + rs.getString("tipo_documento");
                    }
                }
                if (!hasResults) return "No se han registrado documentos obligatorios";
            }
        }
        return null;
    }

    private String validarCoherencia(int expedienteId) {
        CoherenciaAduaneraServicio coherenciaService = new CoherenciaAduaneraServicio();
        List<IncidenciaCoherenciaDTO> issues = coherenciaService.verificarCoherencia(expedienteId);
        StringBuilder errorMsg = new StringBuilder();
        for (IncidenciaCoherenciaDTO issue : issues) {
            if ("ERROR".equals(issue.getTipo()) && "ALTA".equals(issue.getGravedad())) {
                if (errorMsg.length() > 0) errorMsg.append("; ");
                errorMsg.append(issue.getDescripcion());
            }
        }
        return errorMsg.length() > 0 ? errorMsg.toString() : null;
    }

    private String validarPredam(Connection con, int expedienteId) throws SQLException {
        Importacion imp = importacionRepositorio.buscarPorId(con, expedienteId);
        if (imp == null) return "Expediente no encontrado para validacion PRE-DAM";
        try {
            PredamValidacionServicio.validate(con, imp);
            return null;
        } catch (PredamValidacionException e) {
            return "Validacion PRE-DAM fallo";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
