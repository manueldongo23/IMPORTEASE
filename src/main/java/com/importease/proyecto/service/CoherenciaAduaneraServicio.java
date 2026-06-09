package com.importease.proyecto.service;

import com.importease.proyecto.dto.IncidenciaCoherenciaDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CoherenciaAduaneraServicio {

    private final ImportacionRepositorio importacionRepositorio;

    public CoherenciaAduaneraServicio() {
        this.importacionRepositorio = new ImportacionRepositorio();
    }

    public List<IncidenciaCoherenciaDTO> verificarCoherencia(int expedienteId) {
        List<IncidenciaCoherenciaDTO> issues = new ArrayList<>();
        Importacion imp = null;

        try (Connection con = ConexionDB.obtenerConexion()) {
            imp = importacionRepositorio.buscarPorId(con, expedienteId);
            if (imp == null) {
                issues.add(new IncidenciaCoherenciaDTO("ERROR", "Expediente no encontrado", "id", "ALTA", "Verifica que el ID del expediente sea correcto"));
                return issues;
            }

            verificarManifiestoBL(con, expedienteId, issues);
            verificarReimportacion(imp, issues);
            verificarAdmisionTemporal(con, expedienteId, issues);
            verificarMercanciaRestringida(con, expedienteId, imp, issues);
            verificarFob(imp, issues);
            verificarDespachoFechas(con, imp, issues);
            verificarHsCode(imp, issues);

        } catch (Exception e) {
            LoggerUtil.error("Error en verificarCoherencia", e);
            issues.add(new IncidenciaCoherenciaDTO("ERROR", "Error interno al verificar coherencia: " + e.getMessage(), null, "ALTA", "Reintente la operaciÃƒÂ³n"));
        }

        if (issues.isEmpty()) {
            issues.add(new IncidenciaCoherenciaDTO("INFO", "Todos los controles de coherencia aduanera pasaron correctamente", null, "BAJA", "El expediente estÃƒÂ¡ listo para continuar"));
        }

        return issues;
    }

    private void verificarManifiestoBL(Connection con, int expedienteId, List<IncidenciaCoherenciaDTO> issues) {
        String sql = "SELECT m.id, d.numero_documento FROM manifiestos_carga m " +
                     "LEFT JOIN documentos_transporte d ON m.id = d.manifiesto_id " +
                     "WHERE m.operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    issues.add(new IncidenciaCoherenciaDTO("ERROR", "No existe manifiesto de carga registrado", "manifiestoId", "ALTA",
                        "Registra el manifiesto de carga en el paso de transporte"));
                } else {
                    String numDoc = rs.getString("numero_documento");
                    if (numDoc == null || numDoc.isBlank()) {
                        issues.add(new IncidenciaCoherenciaDTO("WARNING", "El manifiesto no tiene un BL/AWB asociado", "numeroDocumento", "MEDIA",
                            "Vincula un documento de transporte (BL/AWB) al manifiesto"));
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error verificando manifiesto/BL: " + e.getMessage());
        }
    }

    private void verificarReimportacion(Importacion imp, List<IncidenciaCoherenciaDTO> issues) {
        if (imp.getEstado() == null) return;

        boolean esReimportacion = imp.getEstado().toUpperCase().contains("REIMPORT")
            || "36".equals(imp.getEstado());

        if (esReimportacion) {
            issues.add(new IncidenciaCoherenciaDTO("WARNING", "RÃƒÂ©gimen de reimportaciÃƒÂ³n: se requiere DAM de exportaciÃƒÂ³n precedente",
                "regimen", "ALTA", "Verifica que exista una declaraciÃƒÂ³n de exportaciÃƒÂ³n previa regularizada dentro de 12 meses"));
        }
    }

    private void verificarAdmisionTemporal(Connection con, int expedienteId, List<IncidenciaCoherenciaDTO> issues) {
        try {
            String sql = "SELECT garantia_id FROM garantias_aduaneras WHERE operacion_id = ? LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, expedienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        issues.add(new IncidenciaCoherenciaDTO("INFO", "No se detectÃƒÂ³ garantÃƒÂ­a. Si el rÃƒÂ©gimen es AdmisiÃƒÂ³n Temporal, se requiere garantÃƒÂ­a",
                            "garantia", "MEDIA", "Registra la garantÃƒÂ­a correspondiente al rÃƒÂ©gimen"));
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error verificando admision temporal: " + e.getMessage());
        }
    }

    private void verificarMercanciaRestringida(Connection con, int expedienteId, Importacion imp, List<IncidenciaCoherenciaDTO> issues) {
        if (imp.getHsCode() != null && NormalizadorUtil.looksRestricted(imp.getHsCode())) {
            String sql = "SELECT COUNT(*) FROM solicitudes_permiso WHERE operacion_id = ? AND estado IN ('APROBADO', 'VIGENTE')";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, expedienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        issues.add(new IncidenciaCoherenciaDTO("ERROR",
                            "MercancÃƒÂ­a restringida sin permiso sectorial vigente",
                            "permisoVuce", "ALTA",
                            "Gestiona el permiso sectorial VUCE antes de continuar"));
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warn("Error verificando mercancia restringida: " + e.getMessage());
            }
        }
    }

    private void verificarFob(Importacion imp, List<IncidenciaCoherenciaDTO> issues) {
        if (imp.getValorFob() <= 0) {
            issues.add(new IncidenciaCoherenciaDTO("ERROR", "El valor FOB debe ser mayor a 0 USD",
                "fob", "ALTA", "Ingresa un valor FOB positivo"));
        } else if (imp.getValorFob() > 10_000_000) {
            issues.add(new IncidenciaCoherenciaDTO("WARNING", "El valor FOB excede los 10,000,000 USD, puede requerir validaciÃƒÂ³n adicional",
                "fob", "MEDIA", "Verifica que el valor FOB sea correcto"));
        }
    }

    private void verificarDespachoFechas(Connection con, Importacion imp, List<IncidenciaCoherenciaDTO> issues) {
        if (imp.getEstado() == null) return;

        boolean esAnticipado = "ANTICIPADO".equals(imp.getEstado()) || imp.getEstado().contains("ANTICIPADO");
        boolean esDiferido = "DIFERIDO".equals(imp.getEstado()) || imp.getEstado().contains("DIFERIDO");

        String sql = "SELECT fecha_llegada FROM manifiestos_carga WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.sql.Timestamp fechaLlegada = rs.getTimestamp("fecha_llegada");
                    if (fechaLlegada != null) {
                        java.time.LocalDateTime llegada = fechaLlegada.toLocalDateTime();
                        java.time.LocalDateTime now = java.time.LocalDateTime.now();

                        if (esAnticipado && !llegada.isAfter(now)) {
                            issues.add(new IncidenciaCoherenciaDTO("WARNING",
                                "Despacho anticipado pero la fecha de llegada ya pasÃƒÂ³",
                                "fechaLlegada", "MEDIA",
                                "Considera cambiar a despacho diferido si la mercancÃƒÂ­a ya arribÃƒÂ³"));
                        }

                        if (esDiferido && !llegada.isBefore(now)) {
                            issues.add(new IncidenciaCoherenciaDTO("WARNING",
                                "Despacho diferido pero la fecha de llegada estÃƒÂ¡ en el futuro",
                                "fechaLlegada", "MEDIA",
                                "Considera cambiar a despacho anticipado si la mercancÃƒÂ­a aÃƒÂºn no arriba"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error verificando fechas de despacho: " + e.getMessage());
        }
    }

    private void verificarHsCode(Importacion imp, List<IncidenciaCoherenciaDTO> issues) {
        if (imp.getHsCode() == null || imp.getHsCode().isBlank()) {
            issues.add(new IncidenciaCoherenciaDTO("ERROR", "HS Code no registrado",
                "hsCode", "ALTA", "Clasifica tu mercancÃƒÂ­a con un HS Code de 6 a 10 dÃƒÂ­gitos"));
        } else if (!imp.getHsCode().matches("\\d{6,10}")) {
            issues.add(new IncidenciaCoherenciaDTO("ERROR", "HS Code debe tener entre 6 y 10 dÃƒÂ­gitos numÃƒÂ©ricos",
                "hsCode", "ALTA", "Corrige el HS Code para que tenga entre 6 y 10 dÃƒÂ­gitos"));
        }
    }
}


