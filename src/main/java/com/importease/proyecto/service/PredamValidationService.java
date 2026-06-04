package com.importease.proyecto.service;

import com.importease.proyecto.model.Importacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.importease.proyecto.service.SunatDamScraperService;

public class PredamValidationService {

    public static void validate(Connection con, Importacion imp) throws PredamValidationException {
        List<String> missingFields = new ArrayList<>();

        checkImporterIdentity(con, imp, missingFields);
        checkCoreOperationFields(imp, missingFields);

        if (imp.getValorFob() < 1.0) {
            missingFields.add("Valor FOB invalido (Debe ser mayor o igual a 1 USD)");
        }
        // QA-006: FOB maximum validation
        if (imp.getValorFob() > 10_000_000) {
            missingFields.add("Valor FOB excede el maximo permitido (10,000,000 USD)");
        }
        if (imp.getHsCode() == null || !imp.getHsCode().trim().matches("\\d{6,10}")) {
            missingFields.add("HS Code (Subpartida arancelaria) invalido (Debe tener entre 6 y 10 digitos)");
        }
        if (imp.getBlMasterId() == null) {
            missingFields.add("Documento de transporte no vinculado a la operacion");
        }

        boolean hasManifiesto = checkManifiestoExists(con, imp.getId());
        if (!hasManifiesto) {
            missingFields.add("Manifiesto de carga no registrado");
        }

        boolean requiresPermit = NormalizadorUtil.looksRestricted(imp.getHsCode());
        checkMinimumDocuments(con, imp.getId(), requiresPermit, missingFields);

        // QA-004: ValidaciÃ³n CITES para Especies Protegidas (SERFOR) â€” expandida
        String descLower = imp.getProductoDesc() != null ? imp.getProductoDesc().toLowerCase(java.util.Locale.ROOT) : "";
        String code = imp.getHsCode() != null ? imp.getHsCode().trim() : "";
        boolean isWood4407 = code.startsWith("4407");
        boolean citesDescription = descLower.contains("caoba") || descLower.contains("cedro")
                || descLower.contains("swietenia") || descLower.contains("cedrela")
                || descLower.contains("mahogany") || descLower.contains("ipÃ©") || descLower.contains("ipe")
                || descLower.contains("palo rosa") || descLower.contains("rosewood")
                || descLower.contains("Ã©bano") || descLower.contains("ebony")
                || descLower.contains("palissandre") || descLower.contains("cites");
        if (isWood4407 || citesDescription) {
            missingFields.add("âš ï¸ ALERTA CITES (SERFOR): El producto forestal (HS 4407) contiene especies protegidas (Caoba/Swietenia, Cedro/Cedrela, IpÃ©, Palo Rosa, Ã‰bano). Se requiere presentar Certificado CITES de exportaciÃ³n oficial del paÃ­s de origen.");
        }

        // Error 21: Alerta cruzada MTC + DIGEMID para Equipos MÃ©dicos con Wi-Fi/Bluetooth
        if (code.startsWith("9018") || descLower.contains("medico") || descLower.contains("ecografo") || descLower.contains("clinico")) {
            boolean tieneInalambrico = descLower.contains("wifi") || descLower.contains("wi-fi") 
                    || descLower.contains("bluetooth") || descLower.contains("wireless") || descLower.contains("inalambrico");
            if (tieneInalambrico) {
                missingFields.add("âš ï¸ ALERTA CRUZADA (MTC + DIGEMID): Se ha detectado un dispositivo medico con antena Wi-Fi/Bluetooth. Requiere Homologacion de Equipos de Telecomunicaciones (MTC) ademas del Registro Sanitario (DIGEMID).");
            }
        }

        // QA-SEC-002: Verificar si los datos provienen de fuente simulada con baja confianza
        checkSimulatedDataWarning(con, imp);

        // QA-053: Verificar estado actual de la DAM en SUNAT si ya tiene nÃºmero asignado
        checkDamStatusAtSunat(imp, missingFields);

        if (!missingFields.isEmpty()) {
            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("success", false);
            errorMap.put("errorCode", "PREDAM_VALIDATION_FAILED");
            errorMap.put("title", "Rechazo de Control de Precondiciones (PRE-DAM)");
            errorMap.put("message", "El expediente aduanero no cumple con las precondiciones normativas minimas para iniciar el despacho.");
            errorMap.put("missingFields", missingFields);
            errorMap.put("severity", "BLOCKING");
            errorMap.put("sourceType", FuenteMetadataBuilder.TYPE_SYSTEM_RULE);
            errorMap.put("confidence", 1.0);

            ExpedienteAuditService.registrarValidacion(
                    con,
                    imp.getId(),
                    "PREDAM_MINIMOS",
                    "RECHAZADO",
                    "BLOCKING",
                    missingFields,
                    "Faltan requisitos normativos minimos para simular PRE-DAM."
            );
            throw new PredamValidationException(errorMap);
        } else {
            ExpedienteAuditService.registrarValidacion(
                    con,
                    imp.getId(),
                    "PREDAM_MINIMOS",
                    "APROBADO",
                    "INFO",
                    null,
                    "Validacion de precondiciones aprobada."
            );
        }
    }

    private static void checkImporterIdentity(Connection con, Importacion imp, List<String> missingFields) {
        if (imp.getUsuarioId() <= 0) {
            missingFields.add("Importador o consignatario no identificado");
            return;
        }

        String sql = "SELECT ruc, ruc_validado, estado_ruc, condicion_ruc FROM usuarios WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, imp.getUsuarioId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    missingFields.add("Importador no registrado");
                    return;
                }

                String ruc = rs.getString("ruc");
                boolean rucValidado = rs.getBoolean("ruc_validado");
                String estadoRuc = rs.getString("estado_ruc");
                String condicionRuc = rs.getString("condicion_ruc");

                if (ruc == null || ruc.isBlank()) {
                    missingFields.add("RUC del importador no registrado");
                }
                if (!isRucOperative(estadoRuc, condicionRuc, rucValidado)) {
                    missingFields.add("RUC activo y habido requerido para generar PRE-DAM");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar identidad del importador: " + e.getMessage());
            missingFields.add("Error interno al verificar identidad del importador (BD temporalmente no disponible)");
        }

        // QA-008: Verificar que exista al menos un documento_transporte vinculado al manifiesto
        String checkDocTransporte = "SELECT 1 FROM documentos_transporte dt INNER JOIN manifiestos_carga m ON dt.manifiesto_id = m.id WHERE m.operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(checkDocTransporte)) {
            ps.setInt(1, imp.getId());
            ResultSet rs = ps.executeQuery();
            if (rs == null) {
                LoggerUtil.warn("Error al verificar documento de transporte: ResultSet nulo");
                return;
            }
            try (rs) {
                if (!rs.next()) {
                    missingFields.add("No hay documento de transporte vinculado al manifiesto de carga");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar documento de transporte: " + e.getMessage());
        }
    }

    private static void checkCoreOperationFields(Importacion imp, List<String> missingFields) {
        if (imp.getProductoDesc() == null || imp.getProductoDesc().trim().isBlank()) {
            missingFields.add("Descripcion comercial del producto obligatoria");
        }
        if (imp.getPaisOrigen() == null || imp.getPaisOrigen().trim().isBlank()) {
            missingFields.add("Pais de origen o procedencia obligatorio");
        }
        if (imp.getIncoterm() == null || imp.getIncoterm().trim().isBlank()) {
            missingFields.add("Incoterm obligatorio");
        }
        if (imp.getValorCif() <= 0) {
            missingFields.add("Valor CIF invalido (Debe ser mayor a 0)");
        }
        if (imp.getTipoCambio() <= 0) {
            missingFields.add("Tipo de cambio invalido (Debe ser mayor a 0)");
        }
    }

    private static boolean checkManifiestoExists(Connection con, int operacionId) {
        String sql = "SELECT 1 FROM manifiestos_carga WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            ResultSet rs = ps.executeQuery();
            if (rs == null) {
                LoggerUtil.warn("Error al verificar manifiesto de carga: ResultSet nulo");
                return false;
            }
            try (rs) {
                return rs.next();
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar manifiesto de carga: " + e.getMessage());
            return false;
        }
    }

    private static void checkMinimumDocuments(Connection con, int operacionId, boolean requiresPermit, List<String> missingFields) {
        String sql = "SELECT documento_factura, documento_bl, permiso_vuce_obtenido FROM operaciones WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, operacionId);
            ResultSet rs = ps.executeQuery();
            if (rs == null) {
                LoggerUtil.warn("Error al verificar documentos minimos en base de datos: ResultSet nulo");
                return;
            }
            try (rs) {
                if (rs.next()) {
                    boolean hasFactura = rs.getBoolean("documento_factura");
                    boolean hasBL = rs.getBoolean("documento_bl");
                    boolean hasPermit = rs.getBoolean("permiso_vuce_obtenido");

                    if (!hasFactura) {
                        missingFields.add("Factura Comercial obligatoria (no cargada)");
                    }
                    if (!hasBL) {
                        missingFields.add("Documento de Transporte (BL/AWB) obligatorio (no cargado)");
                    }
                    if (requiresPermit && !hasPermit) {
                        missingFields.add("Permiso de Internamiento Sectorial (VUCE) obligatorio para mercaderia restringida");
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar documentos minimos en base de datos: " + e.getMessage());
        }

        // QA-009: Verificar que exista PERMISO_SECTORIAL en documentos_importacion si requiresPermit
        if (requiresPermit) {
            String checkPermisoDoc = "SELECT 1 FROM documentos_importacion WHERE importacion_id = ? AND tipo_documento = 'PERMISO_SECTORIAL' AND soft_delete = FALSE LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(checkPermisoDoc)) {
                ps.setInt(1, operacionId);
                ResultSet rs = ps.executeQuery();
                if (rs == null) {
                    LoggerUtil.warn("Error al verificar permiso sectorial en documentos_importacion: ResultSet nulo");
                    return;
                }
                try (rs) {
                    if (!rs.next()) {
                        missingFields.add("Documento PERMISO_SECTORIAL no encontrado en documentos_importacion para mercaderia restringida");
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warn("Error al verificar permiso sectorial en documentos_importacion: " + e.getMessage());
            }
        }
    }

    private static boolean isRucOperative(String estadoRuc, String condicionRuc, boolean rucValidado) {
        if (!rucValidado) return false;
        String estado = normalize(estadoRuc);
        String condicion = normalize(condicionRuc);
        if (estado.isBlank() || condicion.isBlank()) return false;
        return "ACTIVO".equals(estado) && !"NO_HABIDO".equals(condicion) && !"NO HABIDO".equals(condicion);
    }

    private static void checkSimulatedDataWarning(Connection con, Importacion imp) {
        String sql = "SELECT source_type, confidence FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, imp.getId());
            ResultSet rs = ps.executeQuery();
            if (rs == null) {
                LoggerUtil.warn("No se pudo verificar origen simulado de datos: ResultSet nulo");
                return;
            }
            try (rs) {
                if (rs.next()) {
                    String sourceType = rs.getString("source_type");
                    double confidence = rs.getDouble("confidence");
                    boolean isSimulated = "SIMULADO".equalsIgnoreCase(sourceType != null ? sourceType.trim() : "");
                    boolean lowConfidence = confidence < 0.30;
                    if (isSimulated || lowConfidence) {
                        String warningMsg = isSimulated
                            ? "Datos simulados detectados (sourceType=SIMULADO). Los resultados son referenciales."
                            : "Datos con baja confianza detectados (confidence=" + String.format("%.2f", confidence) + "). Los resultados son referenciales.";
                        List<String> warnings = new ArrayList<>();
                        warnings.add(warningMsg);
                        ExpedienteAuditService.registrarValidacion(
                            con,
                            imp.getId(),
                            "PREDAM_SIMULATED_DATA",
                            "APROBADO",
                            "WARNING",
                            warnings,
                            warningMsg
                        );
                        LoggerUtil.warn("Validacion PRE-DAM: " + warningMsg);
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo verificar origen simulado de datos: " + e.getMessage());
        }
    }

    public static Map<String, Object> validateFuentesAuditables(Connection con, int expedienteId) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        List<String> advertencias = new ArrayList<>();
        boolean tieneSimulado = false;

        String sqlFuentes = "SELECT source_type, fuente, COUNT(*) as total FROM ("
                + "SELECT source_type, fuente FROM manifiestos_carga WHERE operacion_id = ? "
                + "UNION ALL "
                + "SELECT source_type, fuente FROM dam_cabecera WHERE operacion_id = ? "
                + "UNION ALL "
                + "SELECT source_type, 'OPERACION' FROM operaciones WHERE id = ? "
                + ") AS fuentes GROUP BY source_type, fuente";
        try (PreparedStatement ps = con.prepareStatement(sqlFuentes)) {
            ps.setInt(1, expedienteId);
            ps.setInt(2, expedienteId);
            ps.setInt(3, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sourceType = rs.getString("source_type");
                    String fuente = rs.getString("fuente");
                    int total = rs.getInt("total");
                    if (sourceType != null && (sourceType.equals(FuenteMetadataBuilder.TYPE_SIMULATED)
                            || sourceType.equals(FuenteMetadataBuilder.TYPE_UNKNOWN))) {
                        tieneSimulado = true;
                        advertencias.add("Fuente '" + (fuente != null ? fuente : "N/A")
                                + "' con sourceType=" + sourceType
                                + " (" + total + " registro(s))");
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al verificar fuentes auditables: " + e.getMessage());
            advertencias.add("Error interno al verificar fuentes de datos");
        }

        if (tieneSimulado) {
            resultado.put("success", true);
            resultado.put("expedienteId", expedienteId);
            resultado.put("severidad", "WARNING");
            resultado.put("codigo", "FUENTES_SIMULADAS");
            resultado.put("mensaje", "El expediente contiene datos de origen SIMULADO o NO IDENTIFICADO. "
                    + "La PRE-DAM generada se basa en datos referenciales, no oficiales.");
            resultado.put("advertencias", advertencias);
            resultado.put("auditable", false);

            ExpedienteAuditService.registrarValidacion(
                    con,
                    expedienteId,
                    "FUENTES_SIMULADAS",
                    "ADVERTENCIA",
                    "WARNING",
                    advertencias,
                    "PRE-DAM basada en datos simulados - evidencia no auditable"
            );
        } else {
            resultado.put("success", true);
            resultado.put("expedienteId", expedienteId);
            resultado.put("severidad", "INFO");
            resultado.put("codigo", "FUENTES_AUDITABLES");
            resultado.put("mensaje", "Todas las fuentes del expediente son auditables u oficiales.");
            resultado.put("advertencias", advertencias);
            resultado.put("auditable", true);

            ExpedienteAuditService.registrarValidacion(
                    con,
                    expedienteId,
                    "FUENTES_AUDITABLES",
                    "APROBADO",
                    "INFO",
                    null,
                    "Todas las fuentes del expediente son auditables."
            );
        }

        resultado.putAll(FuenteMetadataBuilder.buildMetadata(
                "ExpedienteAuditService.validarFuentes",
                FuenteMetadataBuilder.TYPE_SYSTEM_RULE,
                tieneSimulado ? 0.4 : 0.95,
                tieneSimulado ? "Datos simulados - validez referencial" : "Fuentes auditables verificadas"
        ));

        return resultado;
    }

    private static void checkDamStatusAtSunat(Importacion imp, List<String> missingFields) {
        String damNumber = imp.getNumeroDam();
        if (damNumber == null || damNumber.isBlank()) return;

        try {
            SunatDamScraperService scraper = new SunatDamScraperService();
            Map<String, Object> result = scraper.consultarEstadoDam(damNumber);
            Object estado = result.get("estado");
            if (estado != null) {
                String estadoStr = estado.toString();
                if ("OBSERVADO".equals(estadoStr) || "ANULADO".equals(estadoStr) || "RECHAZADO".equals(estadoStr)) {
                    missingFields.add("La DAM " + damNumber + " se encuentra " + estadoStr + " en SUNAT. Se requiere atender las observaciones antes de generar PRE-DAM.");
                } else if ("ERROR_CONSULTA".equals(estadoStr) || "TIMEOUT".equals(estadoStr)) {
                    LoggerUtil.warn("No se pudo verificar DAM " + damNumber + " en SUNAT: " + result.get("error"));
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al consultar DAM " + damNumber + " en SUNAT: " + e.getMessage());
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}


