package com.importease.proyecto.service;

import com.google.gson.Gson;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.service.aduanas.AduanasCatalogoProcedimientos;
import com.importease.proyecto.service.aduanas.AduanasConstructorRespuesta;
import com.importease.proyecto.service.aduanas.AduanasDamServicio;
import com.importease.proyecto.service.aduanas.AduanasLegalServicio;
import com.importease.proyecto.service.aduanas.AduanasLineaTiempoServicio;
import com.importease.proyecto.service.aduanas.DtaMapeador;
import com.importease.proyecto.service.aduanas.ManifiestoComandoServicio;
import com.importease.proyecto.service.aduanas.OperacionAduaneraMapeador;
import com.importease.proyecto.service.aduanas.PredamMapeador;

// Extracted services
import com.importease.proyecto.service.aduanas.RegimenEvaluadorServicio;
import com.importease.proyecto.service.aduanas.RestrictionSeverity;
import com.importease.proyecto.service.aduanas.ModalidadEvaluadorServicio;
import com.importease.proyecto.service.aduanas.AduanasSimuladorLineaTiempo;
import com.importease.proyecto.service.aduanas.AduanasListaChequeoServicio;
import com.importease.proyecto.service.aduanas.AduanasAlertasServicio;
import com.importease.proyecto.service.aduanas.ReimportacionServicio;
import com.importease.proyecto.service.aduanas.TransbordoServicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AduanasServicio {
    public static final String SOURCE = "DESPA_REFERENCIAL";
    public static final String SOURCE_TYPE = "BD_LOCAL";
    public static final double CONFIDENCE = 0.85;

    private final ImportacionRepositorio importacionRepositorio;
    private final EventoUsuarioServicio eventoUsuarioServicio;
    private final FuenteEventoServicio fuenteEventoServicio;
    private final Gson gson;
    private final AduanasConstructorRespuesta responseBuilder;
    private final AduanasCatalogoProcedimientos procedureCatalog;
    private final DtaMapeador dtaMapeador;
    private final PredamMapeador predamMapeador;
    private final OperacionAduaneraMapeador operacionMapper;
    private final AduanasLineaTiempoServicio timelineService;
    private final AduanasDamServicio damService;
    private final ManifiestoComandoServicio manifiestoComandoServicio;
    private final AduanasLegalServicio legalService;

    // Extracted logic services
    private final RegimenEvaluadorServicio regimenEvaluadorServicio;
    private final ModalidadEvaluadorServicio modalidadEvaluadorServicio;
    private final AduanasSimuladorLineaTiempo timelineSimulator;
    private final AduanasListaChequeoServicio checklistService;
    private final AduanasAlertasServicio alertsService;
    private final ReimportacionServicio reimportacionServicio;
    private final TransbordoServicio transbordoServicio;

    public AduanasServicio() {
        this(new ImportacionRepositorio(), new EventoUsuarioServicio(), new FuenteEventoServicio());
    }

    AduanasServicio(ImportacionRepositorio importacionRepositorio, EventoUsuarioServicio eventoUsuarioServicio, FuenteEventoServicio fuenteEventoServicio) {
        this.importacionRepositorio = importacionRepositorio;
        this.eventoUsuarioServicio = eventoUsuarioServicio;
        this.fuenteEventoServicio = fuenteEventoServicio;
        this.gson = new Gson();
        this.responseBuilder = new AduanasConstructorRespuesta();
        this.procedureCatalog = new AduanasCatalogoProcedimientos();
        this.dtaMapeador = new DtaMapeador();
        this.predamMapeador = new PredamMapeador();
        this.operacionMapper = new OperacionAduaneraMapeador();
        this.timelineService = new AduanasLineaTiempoServicio();
        this.damService = new AduanasDamServicio();
        this.manifiestoComandoServicio = new ManifiestoComandoServicio();
        this.legalService = new AduanasLegalServicio(procedureCatalog, responseBuilder);

        this.regimenEvaluadorServicio = new RegimenEvaluadorServicio();
        this.modalidadEvaluadorServicio = new ModalidadEvaluadorServicio();
        this.timelineSimulator = new AduanasSimuladorLineaTiempo();
        this.checklistService = new AduanasListaChequeoServicio();
        this.alertsService = new AduanasAlertasServicio();
        this.reimportacionServicio = new ReimportacionServicio();
        this.transbordoServicio = new TransbordoServicio();
    }

    public Map<String, Object> obtenerExpediente(int usuarioId, int operacionId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = requireOwned(con, usuarioId, operacionId);
            Map<String, Object> expediente = buildExpediente(con, imp);
            return expediente;
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener expediente aduanero: " + e.getMessage());
            return errorData("EXPEDIENTE_NO_DISPONIBLE", "No se pudo cargar el expediente aduanero.");
        }
    }

    public Map<String, Object> generarExpediente(int usuarioId, String sessionId, Map<String, Object> body, String ip, String userAgent) {
        int operacionId = asInt(body.get("operacionId"), 0);
        if (operacionId <= 0) return errorData("OPERACION_INVALIDA", "Operacion invalida.");

        try (Connection con = ConexionDB.obtenerConexion()) {
            try {
                con.setAutoCommit(false);
                Importacion imp = requireOwned(con, usuarioId, operacionId);
                Map<String, Object> regimen = evaluarRegimen(body);
                Map<String, Object> modalidad = evaluarModalidad(bodyWithOperacion(con, body, imp, regimen));
                long damId = ensureDam(con, imp, regimen, modalidad);
                ensureSerie(con, damId, imp);
                ensureDta(con, damId, imp);
                refreshTimeline(con, damId, imp, modalidad);
                refreshAlertas(con, imp, regimen, modalidad);
                ensureGarantiaIfNeeded(con, damId, imp, regimen);
                con.commit();

                eventoUsuarioServicio.registrar(usuarioId, sessionId, "EXPEDIENTE_GENERADO", "ADUANAS", "operacion", String.valueOf(operacionId), gson.toJson(regimen), ip, userAgent);
                fuenteEventoServicio.registrarSimulado("SIMULACION_ACADEMICA", "EXPEDIENTE_ADUANERO", String.valueOf(operacionId), "Expediente academico generado con datos de la operacion.");
                return buildExpediente(con, imp);
            } catch (SecurityException e) {
                try {
                    con.rollback();
                } catch (Exception rollbackEx) {
                    LoggerUtil.error("Fallo al ejecutar rollback de conexion", rollbackEx);
                }
                throw e;
            } catch (Exception innerEx) {
                try {
                    con.rollback();
                } catch (Exception rollbackEx) {
                    LoggerUtil.error("Fallo al ejecutar rollback de conexion", rollbackEx);
                }
                throw innerEx;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (Exception autoCommitEx) {
                    LoggerUtil.error("Fallo al restaurar autoCommit de conexion", autoCommitEx);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Error al generar expediente aduanero", e);
            return errorData("EXPEDIENTE_ERROR", "No se pudo generar el expediente.");
        }
    }

    private Map<String, Object> buildRegimenInputFromImportacion(Importacion imp) {
        return regimenEvaluadorServicio.buildRegimenInputFromImportacion(imp);
    }

    public Map<String, Object> evaluarRegimen(Map<String, Object> body) {
        return regimenEvaluadorServicio.evaluarRegimen(body);
    }

    public Map<String, Object> evaluarModalidad(Map<String, Object> body) {
        return modalidadEvaluadorServicio.evaluarModalidad(body);
    }

    public Map<String, Object> registrarManifiesto(int usuarioId, Map<String, Object> body) {
        int operacionId = asInt(body.get("operacionId"), 0);
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = requireOwned(con, usuarioId, operacionId);
            long manifiestoId = manifiestoComandoServicio.registrarManifiesto(con, imp, body);
            Map<String, Object> result = baseResult();
            result.put("manifiestoId", manifiestoId);
            result.put("mensaje", "Manifiesto, BL y deposito temporal registrados como datos manuales/referenciales.");
            return result;
        } catch (Exception e) {
            LoggerUtil.error("Error al registrar manifiesto", e);
            return errorData("MANIFIESTO_ERROR", "No se pudo registrar manifiesto.");
        }
    }

    public Map<String, Object> generarPredam(int usuarioId, Map<String, Object> body) {
        int operacionId = asInt(body.get("operacionId"), 0);
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = requireOwned(con, usuarioId, operacionId);
            
            try {
                PredamValidacionServicio.validate(con, imp);
            } catch (PredamValidacionException pve) {
                return pve.getValidationError();
            }

            Map<String, Object> serverInput = buildRegimenInputFromImportacion(imp);
            Map<String, Object> regimen = evaluarRegimen(serverInput);
            Map<String, Object> modalidad = evaluarModalidad(bodyWithOperacion(con, body, imp, regimen));
            long damId = ensureDam(con, imp, regimen, modalidad);
            ensureSerie(con, damId, imp);
            Map<String, Object> result = FuenteMetadataBuilder.buildMetadata("SIMULACION_ACADEMICA", FuenteMetadataBuilder.TYPE_SIMULATED, 0.20, "PRE-DAM referencial generada. No es una DAM oficial SUNAT.");
            result.put("damId", damId);
            result.put("numeroDam", predamNumber(imp));
            result.put("mensaje", "PRE-DAM referencial generada. No es una DAM oficial SUNAT.");
            return result;
        } catch (Exception e) {
            LoggerUtil.error("Error al generar PRE-DAM", e);
            return errorData("PREDAM_ERROR", "No se pudo generar PRE-DAM referencial.");
        }
    }

    public List<Map<String, Object>> obtenerTimeline(int usuarioId, int operacionId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = requireOwned(con, usuarioId, operacionId);
            List<Map<String, Object>> rows = queryTimeline(con, operacionId);
            return rows.isEmpty() ? buildTimeline(imp, "ANTICIPADO") : rows;
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener timeline: " + e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> obtenerAlertas(int usuarioId, int operacionId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            requireOwned(con, usuarioId, operacionId);
            return queryAlertas(con, operacionId);
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener alertas: " + e.getMessage());
            return List.of();
        }
    }

    public List<Map<String, Object>> obtenerBaseLegal(int usuarioId, int operacionId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = requireOwned(con, usuarioId, operacionId);
            String destino = "PERU";
            String sqlDam = "SELECT regimen_codigo FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sqlDam)) {
                ps.setInt(1, imp.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String cod = rs.getString("regimen_codigo");
                        if ("10".equals(cod)) destino = "CONSUMO";
                        else if ("36".equals(cod)) destino = "REIMPORTACION";
                        else if ("80".equals(cod)) destino = "TRANSITO";
                        else if ("ADM_TEMP".equals(cod)) destino = "TEMPORAL";
                        else if ("TRANSBORDO".equals(cod)) destino = "TRANSBORDO";
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warn("Error al buscar DAM en base legal: " + e.getMessage());
            }
            Map<String, Object> regimen = evaluarRegimen(Map.of("destino", destino));
            return legalService.obtenerBaseLegal(con, imp);
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener base legal: " + e.getMessage());
            return List.of();
        }
    }

    public Map<String, Object> evaluarReimportacion(Map<String, Object> body) {
        return reimportacionServicio.evaluarReimportacion(body);
    }

    public Map<String, Object> evaluarTransbordo(Map<String, Object> body) {
        return transbordoServicio.evaluarTransbordo(body);
    }

    private Map<String, Object> buildExpediente(Connection con, Importacion imp) throws Exception {
        String destino = "PERU";
        String sqlDam = "SELECT regimen_codigo FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlDam)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String cod = rs.getString("regimen_codigo");
                    if ("10".equals(cod)) destino = "CONSUMO";
                    else if ("36".equals(cod)) destino = "REIMPORTACION";
                    else if ("80".equals(cod)) destino = "TRANSITO";
                    else if ("ADM_TEMP".equals(cod)) destino = "TEMPORAL";
                    else if ("TRANSBORDO".equals(cod)) destino = "TRANSBORDO";
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al cargar régimen desde dam_cabecera: " + e.getMessage());
        }
        boolean restringida = isRestricted(con, imp.getHsCode());
        Map<String, Object> baseInput = Map.of("destino", destino, "fob", imp.getValorFob(), "restringida", restringida);
        Map<String, Object> regimen = evaluarRegimen(baseInput);
        Map<String, Object> modalidad = evaluarModalidad(bodyWithOperacion(con, baseInput, imp, regimen));
        Map<String, Object> expediente = baseResult();
        expediente.put("operacion", operacionMap(imp));
        expediente.put("regimen", regimen);
        expediente.put("modalidad", modalidad);
        boolean tlcAplicado = false;
        try {
            java.util.Map<String, Object> tlcResult = TratadoLibreComercioServicio.verificarTlc(imp.getPaisOrigen());
            tlcAplicado = Boolean.TRUE.equals(tlcResult.get("tlcVigente"));
        } catch (Exception tlcEx) {
            LoggerUtil.warn("Error al verificar TLC para checklist: " + tlcEx.getMessage());
        }
        List<Map<String, Object>> documentos = checklist(str(regimen.get("regimenCodigo")), imp, restringida, tlcAplicado);
        List<Map<String, Object>> timelineQuery = queryTimeline(con, imp.getId());
        List<Map<String, Object>> timeline = (timelineQuery.isEmpty() || timelineQuery.size() < 12) ? buildTimeline(con, imp, str(modalidad.get("modalidadCodigo"))) : timelineQuery;
        List<Map<String, Object>> plazos = PlazoCriticoServicio.calcularPlazos(con, imp, str(regimen.get("regimenCodigo")), str(modalidad.get("modalidadCodigo")));
        List<Map<String, Object>> alertas = queryAlertas(con, imp.getId());
        Map<String, Object> predam = predamMap(imp, regimen, modalidad);

        expediente.put("documentos", documentos);
        expediente.put("timeline", timeline);
        expediente.put("plazos", plazos);
        expediente.put("alertas", alertas);
        expediente.put("baseLegal", baseLegal(str(regimen.get("regimenCodigo")), imp));
        expediente.put("dta", dtaMap(imp));
        expediente.put("predam", predam);

        Map<String, Object> restDetalles = obtenerDetallesRestriccion(con, imp.getHsCode());
        expediente.put("restringida", restDetalles.get("restringida"));
        expediente.put("restriccionesTexto", restDetalles.get("restriccionesTexto"));
        expediente.put("entidadesControl", restDetalles.get("entidadesControl"));
        expediente.put("fuenteRestriccion", restDetalles.get("fuenteRestriccion"));
        expediente.put("severidadRestriccion", restDetalles.get("severidadRestriccion"));
        expediente.put("requierePermiso", restDetalles.get("requierePermiso"));
        expediente.put("permisosDetectados", restDetalles.get("permisosDetectados"));
        expediente.put("restriccionAccionRecomendada", restDetalles.get("accionRecomendada"));

        expediente.put("panel", panelSalud(imp, regimen, modalidad, documentos, plazos, alertas, predam));
        return expediente;
    }

    private Importacion requireOwned(Connection con, int usuarioId, int operacionId) throws Exception {
        Importacion imp = importacionRepositorio.buscarPorId(con, operacionId);
        if (imp == null) throw new IllegalArgumentException("Operacion no encontrada.");
        if (imp.getUsuarioId() != usuarioId) throw new SecurityException("Operacion no pertenece al usuario.");
        return imp;
    }

    private long ensureDam(Connection con, Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad) throws Exception {
        return damService.ensureDam(
                con,
                imp,
                predamNumber(imp),
                str(regimen.get("regimenCodigo")),
                str(modalidad.get("modalidadCodigo")),
                canalProbable(str(regimen.get("regimenCodigo")), imp)
        );
    }

    private void ensureSerie(Connection con, long damId, Importacion imp) throws Exception {
        damService.ensureSerie(con, damId, imp, estimateAdValoremPct(imp), looksRestricted(imp.getHsCode()));
    }

    private void ensureDta(Connection con, long damId, Importacion imp) throws Exception {
        damService.ensureDta(con, damId, imp);
    }

    private void refreshTimeline(Connection con, long damId, Importacion imp, Map<String, Object> modalidad) throws Exception {
        try (PreparedStatement del = con.prepareStatement("DELETE FROM eventos_aduaneros WHERE operacion_id = ? AND fuente = 'SIMULACION_ACADEMICA'")) {
            del.setInt(1, imp.getId());
            del.executeUpdate();
        }
        String sql = "INSERT INTO eventos_aduaneros (operacion_id, dam_id, evento_codigo, evento_nombre, fecha_evento, responsable_tipo, documento_asociado, efecto_legal, fuente, observacion, source_type, confidence) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SIMULACION_ACADEMICA', ?, 'SIMULADO', 0.20)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map<String, Object> ev : buildTimeline(imp, str(modalidad.get("modalidadCodigo")))) {
                ps.setInt(1, imp.getId());
                ps.setLong(2, damId);
                ps.setString(3, str(ev.get("codigo")));
                ps.setString(4, str(ev.get("nombre")));
                ps.setTimestamp(5, Timestamp.valueOf(str(ev.get("fechaIso"))));
                ps.setString(6, str(ev.get("responsable")));
                ps.setString(7, str(ev.get("documento")));
                ps.setString(8, str(ev.get("efectoLegal")));
                ps.setString(9, str(ev.get("observacion")));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void refreshAlertas(Connection con, Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad) throws Exception {
        try (PreparedStatement del = con.prepareStatement("DELETE FROM alertas_regulatorias WHERE operacion_id = ?")) {
            del.setInt(1, imp.getId());
            del.executeUpdate();
        }
        String sql = "INSERT INTO alertas_regulatorias (operacion_id, hs_code, tipo_alerta, severidad, mensaje, base_legal, accion_recomendada, fuente, source_type, confidence) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map<String, Object> a : buildAlertas(imp, regimen, modalidad, isRestricted(con, imp.getHsCode()))) {
                ps.setInt(1, imp.getId());
                ps.setString(2, imp.getHsCode());
                ps.setString(3, str(a.get("tipo")));
                ps.setString(4, str(a.get("severidad")));
                ps.setString(5, str(a.get("mensaje")));
                ps.setString(6, str(a.get("baseLegal")));
                ps.setString(7, str(a.get("accion")));
                ps.setString(8, str(a.get("source") != null ? a.get("source") : "DESPA_REFERENCIAL"));
                ps.setString(9, str(a.get("sourceType") != null ? a.get("sourceType") : "ESTIMADO"));
                ps.setDouble(10, a.get("confidence") instanceof Number n ? n.doubleValue() : 0.60);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void ensureGarantiaIfNeeded(Connection con, long damId, Importacion imp, Map<String, Object> regimen) throws Exception {
        String codigo = str(regimen.get("regimenCodigo"));
        if (!"80".equals(codigo) && !"ADM_TEMP".equals(codigo)) return;
        String sql = "INSERT INTO garantias_aduaneras (operacion_id, dam_id, regimen_codigo, monto_minimo, fecha_vencimiento, estado, source_type, confidence) VALUES (?, ?, ?, ?, DATE_ADD(CURRENT_DATE, INTERVAL ? DAY), 'PENDIENTE', 'ESTIMADO', 0.60)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, imp.getId());
            ps.setLong(2, damId);
            ps.setString(3, codigo);
            BigDecimal garantiaBase = "80".equals(codigo)
                    ? max(imp.getValorFobBD(), imp.getValorCifBD())
                    : max(imp.getTotalImpuestosBD(), imp.getValorCifBD().multiply(new BigDecimal("0.25")));
            ps.setBigDecimal(4, garantiaBase.setScale(2, RoundingMode.HALF_UP));
            ps.setInt(5, "ADM_TEMP".equals(codigo) ? 540 : 30);
            ps.executeUpdate();
        }
    }

    private List<Map<String, Object>> queryTimeline(Connection con, int operacionId) throws Exception {
        return timelineService.queryTimeline(con, operacionId);
    }

    private List<Map<String, Object>> queryAlertas(Connection con, int operacionId) throws Exception {
        return timelineService.queryAlertas(con, operacionId);
    }

    public boolean isRestricted(Connection con, String hsCode) {
        if (looksRestricted(hsCode)) return true;
        if (hsCode == null || hsCode.length() < 2) return false;
        String sql = "SELECT 1 FROM matriz_restricciones_hs WHERE vigente = TRUE AND (hs_code = ? OR capitulo = ?) LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hsCode);
            ps.setInt(2, Integer.parseInt(hsCode.substring(0, 2)));
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> obtenerDetallesRestriccion(Connection con, String hsCode) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("restringida", false);
        details.put("restriccionesTexto", "Sin restricciones detectadas.");
        details.put("entidadesControl", "Ninguna");
        details.put("fuenteRestriccion", "Referencial");
        details.put("severidadRestriccion", RestrictionSeverity.INFO.getLabel());
        details.put("requierePermiso", false);
        details.put("permisosDetectados", "Ninguno");
        details.put("accionRecomendada", "Revisar normatividad general antes de embarcar.");

        if (hsCode == null || hsCode.isBlank()) {
            details.put("severidadRestriccion", RestrictionSeverity.REQUIRES_REVIEW.getLabel());
            details.put("restriccionesTexto", "Código arancelario no provisto. Se requiere revisión.");
            return details;
        }

        String cleanHs = hsCode.replaceAll("[^0-9]", "");
        if (cleanHs.length() < 2) {
            details.put("severidadRestriccion", RestrictionSeverity.REQUIRES_REVIEW.getLabel());
            details.put("restriccionesTexto", "Código arancelario insuficiente. Se requiere revisión.");
            return details;
        }

        // Priority 1: Exact hs_code match in matriz_restricciones_hs
        String sqlExact = "SELECT entidad_codigo, tipo_control, documento_control, es_prohibida, es_restringida, base_legal, source_type " +
                          "FROM matriz_restricciones_hs WHERE vigente = TRUE AND hs_code = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlExact)) {
            ps.setString(1, cleanHs);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    populateRestriccion(details, rs, "matriz_restricciones_hs (Partida exacta)");
                    return details;
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al buscar restricción exacta: " + e.getMessage());
        }

        // Priority 2: Chapter match in matriz_restricciones_hs
        String sqlChapter = "SELECT entidad_codigo, tipo_control, documento_control, es_prohibida, es_restringida, base_legal, source_type " +
                            "FROM matriz_restricciones_hs WHERE vigente = TRUE AND capitulo = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlChapter)) {
            ps.setInt(1, Integer.parseInt(cleanHs.substring(0, 2)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    populateRestriccion(details, rs, "matriz_restricciones_hs (Capítulo)");
                    return details;
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al buscar restricción por capítulo: " + e.getMessage());
        }

        // Priority 3: Catalog match in hs_codes table
        String sqlCatalog = "SELECT requiere_vuce, entidad_vuce FROM hs_codes WHERE codigo = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlCatalog)) {
            ps.setString(1, cleanHs);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean reqVuce = rs.getBoolean("requiere_vuce");
                    if (reqVuce) {
                        details.put("restringida", true);
                        details.put("requierePermiso", true);
                        String entidad = rs.getString("entidad_vuce");
                        details.put("entidadesControl", entidad != null && !entidad.isBlank() ? entidad : "VUCE");
                        details.put("fuenteRestriccion", "Base arancelaria interna");
                        details.put("severidadRestriccion", RestrictionSeverity.PERMISSION_REQUIRED.getLabel());
                        details.put("accionRecomendada", "Revisar trámite VUCE con la entidad controladora antes de continuar.");
                        details.put("restriccionesTexto", "Este producto requiere revisión de una entidad competente porque el catálogo arancelario registra restricciones.");
                        return details;
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al buscar en catálogo hs_codes: " + e.getMessage());
        }

        // Priority 4: Static fallback check (looksRestricted)
        if (looksRestricted(cleanHs)) {
            details.put("restringida", true);
            details.put("requierePermiso", true);
            details.put("fuenteRestriccion", "Base arancelaria interna (Fallback)");
            details.put("severidadRestriccion", RestrictionSeverity.REQUIRES_REVIEW.getLabel());
            details.put("accionRecomendada", "Verificar requisitos en VUCE/SUNAT antes de proceder.");
            
            String entidad = "VUCE";
            String doc = "Permiso/Registro Sanitario/Certificado";
            if (cleanHs.startsWith("8517")) {
                entidad = "MTC";
                doc = "Certificado de Homologación";
            } else if (cleanHs.startsWith("2106") || cleanHs.startsWith("1209")) {
                entidad = "DIGESA / SENASA";
                doc = "Registro Sanitario / Permiso Fito/Zoosanitario";
            } else if (cleanHs.startsWith("3004") || cleanHs.startsWith("3303") || cleanHs.startsWith("3304")) {
                entidad = "DIGEMID";
                doc = "Registro Sanitario / Notificación Sanitaria Obligatoria";
            } else if (cleanHs.startsWith("4407")) {
                entidad = "SERFOR";
                doc = "Certificado Fitosanitario / Autorización de Importación";
            } else if (cleanHs.startsWith("9018")) {
                entidad = "DIGEMID / IPEN";
                doc = "Registro Sanitario / Autorización de Equipos Médicos";
            }
            details.put("entidadesControl", entidad);
            details.put("permisosDetectados", doc);
            details.put("restriccionesTexto", "Este producto puede requerir revisión de " + entidad + " porque el código arancelario registra restricciones asociadas.");
            return details;
        }

        return details;
    }

    private void populateRestriccion(Map<String, Object> details, ResultSet rs, String fuenteDetalle) throws Exception {
        boolean prohibida = rs.getBoolean("es_prohibida");
        boolean restringida = rs.getBoolean("es_restringida") || prohibida;
        
        details.put("restringida", restringida);
        details.put("requierePermiso", restringida);
        
        String entidad = rs.getString("entidad_codigo");
        if (entidad != null && !entidad.isBlank()) {
            details.put("entidadesControl", entidad);
        }
        
        String doc = rs.getString("documento_control");
        if (doc != null && !doc.isBlank()) {
            details.put("permisosDetectados", doc);
        }
        
        String fuente = rs.getString("source_type");
        details.put("fuenteRestriccion", fuente != null ? fuente : fuenteDetalle);
        details.put("severidadRestriccion", prohibida ? RestrictionSeverity.CRITICAL.getLabel() : RestrictionSeverity.PERMISSION_REQUIRED.getLabel());
        details.put("accionRecomendada", prohibida ? "Operación prohibida de importación. No continuar." : "Revisar permisos con la entidad controladora antes de continuar.");
        
        String desc = "Este producto puede requerir revisión de una entidad competente porque el código arancelario registra restricciones asociadas.";
        if (entidad != null && !entidad.isBlank()) {
            desc += " Entidad reguladora: " + entidad + ".";
        }
        details.put("restriccionesTexto", desc);
    }

    private Map<String, Object> bodyWithOperacion(Connection con, Map<String, Object> input, Importacion imp, Map<String, Object> regimen) {
        Map<String, Object> body = new LinkedHashMap<>(input);
        body.remove("source");
        body.remove("sourceType");
        body.remove("confidence");
        body.put("fob", imp.getValorFob());
        body.put("hsCode", imp.getHsCode());
        body.put("regimenCodigo", regimen.get("regimenCodigo"));
        body.put("restringida", isRestricted(con, imp.getHsCode()));
        body.put("operacionId", imp.getId());
        return body;
    }

    private List<Map<String, Object>> buildTimeline(Connection con, Importacion imp, String modalidad) {
        return timelineSimulator.buildTimeline(con, imp, modalidad);
    }

    private List<Map<String, Object>> buildTimeline(Importacion imp, String modalidad) {
        return timelineSimulator.buildTimeline(imp, modalidad);
    }

    private List<Map<String, Object>> buildAlertas(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad, boolean restringida) {
        return alertsService.buildAlertas(imp, regimen, modalidad, restringida);
    }

    private List<Map<String, Object>> checklist(String regimen, Importacion imp, boolean restringida) {
        return checklistService.checklist(regimen, imp, restringida);
    }

    private List<Map<String, Object>> checklist(String regimen, Importacion imp, boolean restringida, boolean tlcAplicado) {
        return checklistService.checklist(regimen, imp, restringida, tlcAplicado);
    }

    private List<Map<String, Object>> baseLegal(String regimen, Importacion imp) {
        return legalService.buildBaseLegal(regimen, imp);
    }

    private Map<String, Object> dtaMap(Importacion imp) {
        return dtaMapeador.toMap(imp);
    }

    private Map<String, Object> predamMap(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad) {
        return predamMapeador.toMap(imp, regimen, modalidad);
    }

    private Map<String, Object> panelSalud(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad, List<Map<String, Object>> documentos, List<Map<String, Object>> plazos, List<Map<String, Object>> alertas, Map<String, Object> predam) {
        Map<String, Object> panel = new LinkedHashMap<>();
        
        int docsTotal = documentos.size();
        int docsCompletos = hasPredam(imp) ? docsTotal : Math.max(0, docsTotal - 3);
        int completitud = hasPredam(imp) ? 100 : (imp.getValorFobBD().compareTo(BigDecimal.ZERO) > 0 && imp.getHsCode() != null ? 68 : 30);
        
        long plazosCriticos = plazos.stream().filter(p -> "CRITICAL".equals(p.get("status")) || "EXPIRED".equals(p.get("status"))).count();
        
        panel.put("completitud", completitud);
        panel.put("docsCompletos", docsCompletos);
        panel.put("docsTotal", docsTotal);
        panel.put("alertasCriticas", plazosCriticos);
        panel.put("fuentesManuales", 3);
        panel.put("fuentesSimuladas", 2);
        panel.put("estadoPredam", hasPredam(imp) ? "Generada referencialmente" : "Pendiente o bloqueada");
        
        String accion = "Completar manifiesto y valor FOB";
        if (hasPredam(imp)) accion = "Evaluar canales y revisar deudas referenciales";
        else if (imp.getValorFob() > 0 && imp.getHsCode() != null) accion = "Generar PRE-DAM";
        
        panel.put("siguientePaso", accion);
        return panel;
    }

    private Map<String, Object> operacionMap(Importacion imp) {
        return operacionMapper.toMap(imp);
    }

    private Map<String, Object> baseResult() {
        return responseBuilder.baseResult();
    }

    private Map<String, Object> errorData(String code, String message) {
        return responseBuilder.errorData(code, message);
    }

    private String canalProbable(String regimen, Importacion imp) {
        if ("36".equals(regimen)) return "ROJO";
        if (looksRestricted(imp.getHsCode())) return "ROJO";
        if (imp.getValorCifBD().compareTo(new BigDecimal("10000")) > 0) return "NARANJA";
        return "VERDE";
    }

    private String predamNumber(Importacion imp) {
        return predamMapeador.predamNumber(imp);
    }

    private static boolean looksRestricted(String hsCode) {
        return NormalizadorUtil.looksRestricted(hsCode);
    }

    private boolean hasPredam(Importacion imp) {
        return imp.getNumeroDam() != null && !imp.getNumeroDam().isBlank();
    }

    private BigDecimal estimateAdValoremPct(Importacion imp) {
        BigDecimal cif = imp.getValorCifBD();
        if (cif == null || cif.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal adValorem = imp.getMontoAdValoremBD();
        if (adValorem == null) {
            adValorem = BigDecimal.ZERO;
        }
        BigDecimal pct = adValorem.divide(cif, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        if (pct.compareTo(BigDecimal.ZERO) < 0) {
            pct = BigDecimal.ZERO;
        }
        if (pct.compareTo(BigDecimal.valueOf(30)) > 0) {
            pct = BigDecimal.valueOf(30);
        }
        return pct.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal max(BigDecimal a, BigDecimal b) {
        BigDecimal left = a == null ? BigDecimal.ZERO : a;
        BigDecimal right = b == null ? BigDecimal.ZERO : b;
        return left.compareTo(right) >= 0 ? left : right;
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }
}
