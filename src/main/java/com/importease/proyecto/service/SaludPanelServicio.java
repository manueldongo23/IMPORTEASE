package com.importease.proyecto.service;

import com.importease.proyecto.dto.PanelSaludDTO;
import com.importease.proyecto.dto.SiguienteAccionDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaludPanelServicio {

    private static final int TOTAL_STEPS = 8;

    private final ImportacionRepositorio importacionRepositorio;
    private final FlujoGuiadoServicio flujoGuiadoServicio;
    private final SiguienteAccionServicio siguienteAccionServicio;
    private final PlazoCriticoServicio plazoCriticoServicio;
    private final SunatDamScraperServicio sunatDamScraperService;

    public SaludPanelServicio() {
        this.importacionRepositorio = new ImportacionRepositorio();
        this.flujoGuiadoServicio = new FlujoGuiadoServicio();
        this.siguienteAccionServicio = new SiguienteAccionServicio();
        this.plazoCriticoServicio = new PlazoCriticoServicio();
        this.sunatDamScraperService = new SunatDamScraperServicio();
    }

    public PanelSaludDTO calcularSalud(int expedienteId) {
        Importacion imp = null;
        try {
            imp = importacionRepositorio.buscarPorId(expedienteId);
        } catch (Exception e) {
            LoggerUtil.error("Error al buscar importacion en SaludPanelServicio", e);
        }

        PanelSaludDTO panel = new PanelSaludDTO();
        SiguienteAccionDTO nextAction = siguienteAccionServicio.calcularSiguienteAccion(expedienteId);

        int completedSteps = 0;
        for (int i = 1; i <= TOTAL_STEPS; i++) {
            if (FlujoGuiadoServicio.isPasoCompleto(expedienteId, i)) {
                completedSteps++;
            }
        }
        BigDecimal completitud = BigDecimal.valueOf(completedSteps)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(TOTAL_STEPS), 0, RoundingMode.HALF_UP);
        panel.setPorcentajeCompletitud(completitud);

        panel.setRiesgoDocumental(calcularRiesgoDocumental(expedienteId));
        panel.setRiesgoNormativo(calcularRiesgoNormativo(imp));
        panel.setRiesgoPlazo(calcularRiesgoPlazo(imp));
        panel.setEstadoManifiesto(calcularEstadoManifiesto(expedienteId));
        panel.setEstadoPermisos(calcularEstadoPermisos(expedienteId, imp));
        panel.setEstadoPreDam(calcularEstadoPreDam(imp));
        panel.setDocumentosFaltantes(calcularDocumentosFaltantes(expedienteId, imp));
        panel.setSiguienteAccion(nextAction);

        consultarEstadoDamSunat(expedienteId, imp, panel);

        return panel;
    }

    private void consultarEstadoDamSunat(int expedienteId, Importacion imp, PanelSaludDTO panel) {
        if (imp == null) {
            panel.setEstadoDamSunat("NO_DISPONIBLE");
            panel.setEstadoDamOrigen("SIN_EXPEDIENTE");
            return;
        }
        String damNumber = imp.getNumeroDam();
        if (damNumber == null || damNumber.isBlank()) {
            panel.setEstadoDamSunat("SIN_DAM");
            panel.setEstadoDamOrigen("NO_APLICA");
            return;
        }
        try {
            java.util.Map<String, Object> result = sunatDamScraperService.consultarEstadoDam(damNumber);
            Object estado = result.get("estado");
            panel.setEstadoDamSunat(estado != null ? estado.toString() : "DESCONOCIDO");
            Object origen = result.get("origen");
            panel.setEstadoDamOrigen(origen != null ? origen.toString() : "SUNAT");
            Object fecha = result.get("fechaNumeracion");
            panel.setUltimaActualizacionDam(fecha != null ? fecha.toString() : "");
            LoggerUtil.info("DAM " + damNumber + " consultada en SUNAT: " + panel.getEstadoDamSunat());
        } catch (Exception e) {
            LoggerUtil.warn("Error consultando DAM en SUNAT para expediente " + expedienteId + ": " + e.getMessage());
            panel.setEstadoDamSunat("ERROR_CONSULTA");
            panel.setEstadoDamOrigen("SUNAT");
        }
    }

    private String calcularRiesgoDocumental(int expedienteId) {
        List<String> faltantes = calcularDocumentosFaltantes(expedienteId, null);
        if (faltantes.isEmpty()) return "BAJO";
        if (faltantes.size() <= 2) return "MEDIO";
        return "ALTO";
    }

    private String calcularRiesgoNormativo(Importacion imp) {
        if (imp == null) return "MEDIO";
        if (imp.getHsCode() != null && NormalizadorUtil.looksRestricted(imp.getHsCode())) {
            return "ALTO";
        }
        if (imp.getValorFob() > 100000) return "MEDIO";
        return "BAJO";
    }

    private String calcularRiesgoPlazo(Importacion imp) {
        if (imp == null) return "MEDIO";
        String regimenCode = null;
        String modalidadCode = null;
        try (Connection con = ConexionDB.obtenerConexion()) {
            String sql = "SELECT regimen_codigo, modalidad_codigo FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, imp.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        regimenCode = rs.getString("regimen_codigo");
                        modalidadCode = rs.getString("modalidad_codigo");
                    }
                }
            }
            if (regimenCode == null) return "BAJO";
            List<Map<String, Object>> plazos = PlazoCriticoServicio.calcularPlazos(con, imp, regimenCode, modalidadCode);
            boolean hasMedio = false;
            for (Map<String, Object> plazo : plazos) {
                String risk = (String) plazo.get("riskLevel");
                if ("CRITICO".equals(risk)) return "CRITICO";
                if ("ALTO".equals(risk)) return "ALTO";
                if ("MEDIO".equals(risk) || "INDETERMINADO".equals(risk)) hasMedio = true;
            }
            return hasMedio ? "MEDIO" : "BAJO";
        } catch (Exception e) {
            LoggerUtil.warn("Error al calcular riesgo de plazo: " + e.getMessage());
            return "MEDIO";
        }
    }

    private String calcularEstadoManifiesto(int expedienteId) {
        String sql = "SELECT COUNT(*) FROM manifiestos_carga WHERE operacion_id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "REGISTRADO";
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar manifiesto: " + e.getMessage());
        }
        return "PENDIENTE";
    }

    private String calcularEstadoPermisos(int expedienteId, Importacion imp) {
        if (imp == null || imp.getHsCode() == null) return "NO_REQUIERE";
        if (!NormalizadorUtil.looksRestricted(imp.getHsCode())) return "NO_REQUIERE";

        String sql = "SELECT COUNT(*) FROM solicitudes_permiso WHERE operacion_id = ? AND estado = 'APROBADO'";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "COMPLETO";
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar permisos: " + e.getMessage());
        }
        return "PENDIENTE";
    }

    private String calcularEstadoPreDam(Importacion imp) {
        if (imp == null) return "NO_REQUIERE";
        if (imp.getNumeroDam() != null && !imp.getNumeroDam().isBlank()) {
            return "HABILITADO";
        }
        try (Connection con = ConexionDB.obtenerConexion()) {
            PredamValidacionServicio.validate(con, imp);
            return "HABILITADO";
        } catch (PredamValidacionException e) {
            return "BLOQUEADO";
        } catch (Exception e) {
            LoggerUtil.warn("Error al validar PRE-DAM en SaludPanelServicio: " + e.getMessage());
            return "BLOQUEADO";
        }
    }

    private List<String> calcularDocumentosFaltantes(int expedienteId, Importacion imp) {
        List<String> faltantes = new ArrayList<>();
        String sql = "SELECT tipo_documento, subido FROM documentos_importacion WHERE importacion_id = ? AND es_obligatorio = TRUE";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasResults = false;
                while (rs.next()) {
                    hasResults = true;
                    boolean subido = rs.getBoolean("subido");
                    if (!subido) {
                        faltantes.add(rs.getString("tipo_documento"));
                    }
                }
                if (!hasResults) {
                    faltantes.add("FACTURA_COMERCIAL");
                    faltantes.add("BILL_OF_LADING");
                    faltantes.add("CERTIFICADO_ORIGEN");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar documentos: " + e.getMessage());
            faltantes.add("FACTURA_COMERCIAL");
            faltantes.add("BILL_OF_LADING");
        }
        return faltantes;
    }
}


