package com.importease.proyecto.service;

import com.google.gson.Gson;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionDAO;
import com.importease.proyecto.service.aduanas.AduanasProcedureCatalog;
import com.importease.proyecto.service.aduanas.AduanasResponseBuilder;
import com.importease.proyecto.service.aduanas.AduanasDamService;
import com.importease.proyecto.service.aduanas.AduanasLegalService;
import com.importease.proyecto.service.aduanas.AduanasTimelineService;
import com.importease.proyecto.service.aduanas.DtaMapper;
import com.importease.proyecto.service.aduanas.ManifiestoCommandService;
import com.importease.proyecto.service.aduanas.OperacionAduaneraMapper;
import com.importease.proyecto.service.aduanas.PredamMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AduanasService {
    public static final String SOURCE = "DESPA_REFERENCIAL";
    public static final String SOURCE_TYPE = "BD_LOCAL";
    public static final double CONFIDENCE = 0.85;

    private final ImportacionDAO importacionDAO;
    private final EventoUsuarioService eventoUsuarioService;
    private final FuenteEventoService fuenteEventoService;
    private final Gson gson;
    private final AduanasResponseBuilder responseBuilder;
    private final AduanasProcedureCatalog procedureCatalog;
    private final DtaMapper dtaMapper;
    private final PredamMapper predamMapper;
    private final OperacionAduaneraMapper operacionMapper;
    private final AduanasTimelineService timelineService;
    private final AduanasDamService damService;
    private final ManifiestoCommandService manifiestoCommandService;
    private final AduanasLegalService legalService;

    public AduanasService() {
        this(new ImportacionDAO(), new EventoUsuarioService(), new FuenteEventoService());
    }

    AduanasService(ImportacionDAO importacionDAO, EventoUsuarioService eventoUsuarioService, FuenteEventoService fuenteEventoService) {
        this.importacionDAO = importacionDAO;
        this.eventoUsuarioService = eventoUsuarioService;
        this.fuenteEventoService = fuenteEventoService;
        this.gson = new Gson();
        this.responseBuilder = new AduanasResponseBuilder();
        this.procedureCatalog = new AduanasProcedureCatalog();
        this.dtaMapper = new DtaMapper();
        this.predamMapper = new PredamMapper();
        this.operacionMapper = new OperacionAduaneraMapper();
        this.timelineService = new AduanasTimelineService();
        this.damService = new AduanasDamService();
        this.manifiestoCommandService = new ManifiestoCommandService();
        this.legalService = new AduanasLegalService(procedureCatalog, responseBuilder);
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
                Map<String, Object> modalidad = evaluarModalidad(bodyWithOperacion(body, imp, regimen));
                long damId = ensureDam(con, imp, regimen, modalidad);
                ensureSerie(con, damId, imp);
                ensureDta(con, damId, imp);
                refreshTimeline(con, damId, imp, modalidad);
                refreshAlertas(con, imp, regimen, modalidad);
                ensureGarantiaIfNeeded(con, damId, imp, regimen);
                con.commit();

                eventoUsuarioService.registrar(usuarioId, sessionId, "EXPEDIENTE_GENERADO", "ADUANAS", "operacion", String.valueOf(operacionId), gson.toJson(regimen), ip, userAgent);
                fuenteEventoService.registrarSimulado("SIMULACION_ACADEMICA", "EXPEDIENTE_ADUANERO", String.valueOf(operacionId), "Expediente academico generado con datos de la operacion.");
                return buildExpediente(con, imp);
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
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("destino", "PERU");
        input.put("quedaEnPeru", true);
        input.put("fob", imp.getValorFob());
        input.put("hsCode", imp.getHsCode());
        input.put("restringida", looksRestricted(imp.getHsCode()));
        return input;
    }

    public Map<String, Object> evaluarRegimen(Map<String, Object> body) {
        String destino = upper(str(body.get("destino")));
        boolean quedaEnPeru = bool(body.get("quedaEnPeru")) || "PERU".equals(destino) || "CONSUMO".equals(destino);
        boolean soloTransita = bool(body.get("soloTransita")) || "TRANSITO".equals(destino);
        boolean temporal = bool(body.get("temporal")) || "TEMPORAL".equals(destino);
        boolean reimporta = bool(body.get("reimportacion")) || "REIMPORTACION".equals(destino);
        boolean transbordo = bool(body.get("transbordo")) || "TRANSBORDO".equals(destino);

        // QA-026: Flag contradiction detection
        if (quedaEnPeru && reimporta) {
            Map<String, Object> errorResult = FuenteMetadataBuilder.buildMetadata("Validacion de regimen", FuenteMetadataBuilder.TYPE_SYSTEM_RULE, 1.0, "Contradiccion de banderas");
            errorResult.put("error", true);
            errorResult.put("mensaje", "Contradiccion: la mercancia no puede quedarse en Peru y ser reimportacion al mismo tiempo.");
            return errorResult;
        }

        String codigo = "10";
        String nombre = "Importacion para el consumo";
        String motivo = quedaEnPeru ? "Se determinÃƒÂ³ rÃƒÂ©gimen 10 porque: la mercancÃƒÂ­a se queda en PerÃƒÂº." : "La mercancia ingresara al territorio aduanero para quedarse en Peru, con pago o garantia de tributos y formalidades.";
        String siguiente = "Validar modalidad de despacho, documentos sustentatorios y riesgos.";

        if (reimporta) {
            codigo = "36";
            nombre = "Reimportacion en el mismo estado";
            motivo = "La mercancia fue exportada antes desde Peru y retorna sin transformacion, reparacion ni elaboracion.";
            siguiente = "Verificar declaracion de exportacion precedente, plazo de 12 meses y canal rojo obligatorio.";
        } else if (transbordo) {
            codigo = "TRANSBORDO";
            nombre = "Transbordo";
            motivo = "La mercancia cambia de medio de transporte para salir del territorio bajo control aduanero.";
            siguiente = "Elegir modalidad de transbordo y controlar el plazo de 30 dias.";
        } else if (temporal) {
            codigo = "ADM_TEMP";
            nombre = "Admision temporal para reexportacion en el mismo estado";
            motivo = "La mercancia entrara temporalmente, con fin determinado y reexportacion prevista sin modificacion sustancial.";
            siguiente = "Calcular garantia, ubicar la mercancia y controlar el plazo del regimen.";
        } else if (soloTransita) {
            codigo = "80";
            nombre = "Transito aduanero";
            motivo = "La mercancia solo pasara por Peru o se trasladara bajo control de una aduana a otra.";
            siguiente = "Registrar manifiesto, ruta, garantia y aduana de destino o salida.";
        } else if (quedaEnPeru) {
            codigo = "10";
        }

        Map<String, Object> result = FuenteMetadataBuilder.buildMetadata("Reglas internas basadas en " + ("36".equals(codigo) ? "DESPA-PG.26" : ("ADM_TEMP".equals(codigo) ? "DESPA-PG.04" : ("TRANSBORDO".equals(codigo) ? "DESPA-PG.11" : ("80".equals(codigo) ? "DESPA-PG.08" : "DESPA-PG.01")))), FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, 0.95, "Regla parametrizada, requiere validaciÃƒÂ³n del agente.");
        result.put("regimenCodigo", codigo);
        result.put("regimenNombre", nombre);
        result.put("motivo", motivo);
        result.put("siguientePaso", siguiente);
        result.put("etiqueta", "Procedimiento Oficial");
        result.put("preguntasInterpretadas", List.of(
            item("La mercancia se quedara en Peru", quedaEnPeru),
            item("Solo transitara por Peru", soloTransita),
            item("Entrara temporalmente", temporal),
            item("Fue exportada antes desde Peru", reimporta),
            item("Cambiara de medio para salir", transbordo)
        ));
        return result;
    }

    public Map<String, Object> evaluarModalidad(Map<String, Object> body) {
        String regimen = str(body.get("regimenCodigo"));
        if (regimen == null || regimen.isBlank()) regimen = "10";
        BigDecimal fob = new BigDecimal(str(body.get("fob")) != null ? str(body.get("fob")) : "0");
        boolean restringida = bool(body.get("restringida"));
        boolean urgente = bool(body.get("urgente"));

        // QA-027: Verify urgency justifying document
        if (urgente) {
            String justDoc = str(body.get("documentoJustificacionUrgencia"));
            if (justDoc == null || justDoc.isBlank()) {
                LoggerUtil.warn("Operacion marcada como urgente sin documento de justificacion. Se recomienda ANTICIPADO.");
            }
        }

        String codigo = "ANTICIPADO";
        String nombre = "Despacho anticipado";
        String motivo = "Regla general referencial para importacion para el consumo.";
        List<String> excepciones = new ArrayList<>();

        if ("36".equals(regimen)) {
            codigo = "DIFERIDO_ROJO";
            nombre = "Diferido con reconocimiento fisico";
            motivo = "En reimportacion se simula canal rojo obligatorio para comprobar identidad de la mercancia.";
        } else if ("TRANSBORDO".equals(regimen)) {
            codigo = "M3_DEPOSITO";
            nombre = "Transbordo con ingreso a deposito temporal";
            motivo = "Modalidad didactica completa para controlar bultos, pesos, contenedores, precintos y regularizacion.";
        } else if ("ADM_TEMP".equals(regimen)) {
            codigo = "TEMPORAL";
            nombre = "Admision temporal";
            motivo = "Requiere finalidad, ubicacion, garantia y control de permanencia.";
        } else if ("80".equals(regimen)) {
            codigo = "TRANSITO_NACIONAL";
            nombre = "Transito aduanero nacional";
            motivo = "Traslado bajo control aduanero con garantia y ruta declarada.";
        } else {
            if (fob.compareTo(BigDecimal.ZERO) > 0 && fob.compareTo(new BigDecimal("2000")) <= 0) excepciones.add("FOB menor o igual a USD 2000");
            if (restringida) excepciones.add("Mercancia restringida requiere control sectorial");
            if (urgente) excepciones.add("Operacion marcada como urgente");
            if (!excepciones.isEmpty()) {
                codigo = urgente ? "URGENTE" : "DIFERIDO";
                nombre = urgente ? "Despacho urgente" : "Despacho diferido permitido";
                motivo = "Existe una excepcion referencial a la obligatoriedad de anticipado: " + String.join(", ", excepciones) + ".";
                if (urgente) {
                    motivo += " Se recomienda verificar documento de justificacion de urgencia.";
                }
            }
        }

        Map<String, Object> result = FuenteMetadataBuilder.buildMetadata("Regla de Modalidad DESPA-PG.01", FuenteMetadataBuilder.TYPE_OFFICIAL_PROCEDURE, 0.90, "RecomendaciÃƒÂ³n basada en reglas SUNAT.");
        result.put("modalidadCodigo", codigo);
        result.put("modalidadNombre", nombre);
        result.put("motivo", motivo);
        result.put("excepciones", excepciones);
        result.put("plazoTexto", plazoTexto(codigo));
        return result;
    }

    public Map<String, Object> registrarManifiesto(int usuarioId, Map<String, Object> body) {
        int operacionId = asInt(body.get("operacionId"), 0);
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = requireOwned(con, usuarioId, operacionId);
            long manifiestoId = manifiestoCommandService.registrarManifiesto(con, imp, body);
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
                PredamValidationService.validate(con, imp);
            } catch (PredamValidationException pve) {
                return pve.getValidationError();
            }

            Map<String, Object> serverInput = buildRegimenInputFromImportacion(imp);
            Map<String, Object> regimen = evaluarRegimen(serverInput);
            Map<String, Object> modalidad = evaluarModalidad(bodyWithOperacion(body, imp, regimen));
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
        boolean exportada = bool(body.get("exportadaDesdePeru"));
        boolean regularizada = bool(body.get("exportacionRegularizada"));
        boolean transformada = bool(body.get("transformada"));
        boolean beneficio = bool(body.get("beneficioExportacion"));
        int meses = asInt(body.get("mesesDesdeEmbarque"), 0);
        boolean enPlazo = meses > 0 && meses <= 12;
        boolean procede = exportada && regularizada && enPlazo && !transformada;

        // QA-028: Verify DECLARACION_EXPORTACION document exists
        if (procede) {
            int operacionId = asInt(body.get("operacionId"), 0);
            if (operacionId > 0) {
                String checkDoc = "SELECT 1 FROM documentos_importacion WHERE importacion_id = ? AND tipo_documento = 'DECLARACION_EXPORTACION' AND soft_delete = FALSE LIMIT 1";
                try (java.sql.Connection con = ConexionDB.obtenerConexion();
                     java.sql.PreparedStatement ps = con.prepareStatement(checkDoc)) {
                    ps.setInt(1, operacionId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            procede = false;
                        }
                    }
                } catch (Exception e) {
                    LoggerUtil.warn("Error al verificar DECLARACION_EXPORTACION: " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = baseResult();
        result.put("procede", procede);
        result.put("canalProbable", "ROJO");
        result.put("canalEsOficial", false);
        result.put("motivoCanal", "Canal rojo obligatorio en esta simulacion para comprobar que la mercancia es la misma.");
        result.put("documentoPrecedente", "Declaracion de exportacion definitiva regularizada");
        result.put("debeDevolverBeneficio", beneficio);
        result.put("diagnostico", procede ? "Procede reimportacion referencial." : "No procede hasta cerrar los requisitos faltantes.");
        result.put("faltantes", faltantesReimportacion(exportada, regularizada, enPlazo, transformada));
        return result;
    }

    public Map<String, Object> evaluarTransbordo(Map<String, Object> body) {
        String modalidad = str(body.get("modalidad"));
        if (modalidad == null || modalidad.isBlank()) modalidad = "M3_DEPOSITO";
        boolean pesoOk = !bool(body.get("diferenciaPesoMayor2"));
        boolean precintoOk = !bool(body.get("precintoViolado"));
        boolean solicitudesPendientes = bool(body.get("solicitudesPendientes"));

        // QA-029: Cross-check precinto status against contenedores table
        int operacionId = asInt(body.get("operacionId"), 0);
        if (operacionId > 0) {
            String precintoCheck = "SELECT c.estado_precinto FROM contenedores c INNER JOIN documentos_transporte dt ON c.documento_transporte_id = dt.id INNER JOIN manifiestos_carga m ON dt.manifiesto_id = m.id WHERE m.operacion_id = ? AND c.estado_precinto = 'VIOLADO' LIMIT 1";
            try (java.sql.Connection con = ConexionDB.obtenerConexion();
                 java.sql.PreparedStatement ps = con.prepareStatement(precintoCheck)) {
                ps.setInt(1, operacionId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        precintoOk = false;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.warn("Error al verificar estado precinto en contenedores: " + e.getMessage());
            }
        }

        boolean regularizable = pesoOk && precintoOk && !solicitudesPendientes;

        Map<String, Object> result = baseResult();
        result.put("modalidad", modalidad);
        result.put("plazoDias", 30);
        result.put("regularizable", regularizable);
        result.put("controla", List.of("DAM numerada", "documento de transporte", "pesos y bultos", "contenedores y precintos", "aduana de salida"));
        result.put("alertas", alertasTransbordo(pesoOk, precintoOk, solicitudesPendientes));
        result.put("mensaje", regularizable ? "Transbordo regularizable en la simulacion." : "Transbordo con observaciones para regularizar.");
        return result;
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
            LoggerUtil.warn("Error al cargar rÃƒÂ©gimen desde dam_cabecera: " + e.getMessage());
        }
        boolean restringida = isRestricted(con, imp.getHsCode());
        Map<String, Object> baseInput = Map.of("destino", destino, "fob", imp.getValorFob(), "restringida", restringida);
        Map<String, Object> regimen = evaluarRegimen(baseInput);
        Map<String, Object> modalidad = evaluarModalidad(bodyWithOperacion(baseInput, imp, regimen));
        Map<String, Object> expediente = baseResult();
        expediente.put("operacion", operacionMap(imp));
        expediente.put("regimen", regimen);
        expediente.put("modalidad", modalidad);
        // Determine TLC status for checklist
        boolean tlcAplicado = false;
        try {
            java.util.Map<String, Object> tlcResult = TlcService.verificarTlc(imp.getPaisOrigen());
            tlcAplicado = Boolean.TRUE.equals(tlcResult.get("tlcVigente"));
        } catch (Exception tlcEx) {
            LoggerUtil.warn("Error al verificar TLC para checklist: " + tlcEx.getMessage());
        }
        List<Map<String, Object>> documentos = checklist(str(regimen.get("regimenCodigo")), imp, restringida, tlcAplicado);
        List<Map<String, Object>> timelineQuery = queryTimeline(con, imp.getId());
        List<Map<String, Object>> timeline = (timelineQuery.isEmpty() || timelineQuery.size() < 12) ? buildTimeline(con, imp, str(modalidad.get("modalidadCodigo"))) : timelineQuery;
        List<Map<String, Object>> plazos = PlazoCriticoService.calcularPlazos(con, imp, str(regimen.get("regimenCodigo")), str(modalidad.get("modalidadCodigo")));
        List<Map<String, Object>> alertas = queryAlertas(con, imp.getId());
        Map<String, Object> predam = predamMap(imp, regimen, modalidad);

        expediente.put("documentos", documentos);
        expediente.put("timeline", timeline);
        expediente.put("plazos", plazos);
        expediente.put("alertas", alertas);
        expediente.put("baseLegal", baseLegal(str(regimen.get("regimenCodigo")), imp));
        expediente.put("dta", dtaMap(imp));
        expediente.put("predam", predam);
        expediente.put("panel", panelSalud(imp, regimen, modalidad, documentos, plazos, alertas, predam));
        return expediente;
    }

    private Importacion requireOwned(Connection con, int usuarioId, int operacionId) throws Exception {
        Importacion imp = importacionDAO.buscarPorId(con, operacionId);
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
        try (PreparedStatement del = con.prepareStatement("DELETE FROM alertas_regulatorias WHERE operacion_id = ? AND fuente = 'DESPA_REFERENCIAL'")) {
            del.setInt(1, imp.getId());
            del.executeUpdate();
        }
        String sql = "INSERT INTO alertas_regulatorias (operacion_id, hs_code, tipo_alerta, severidad, mensaje, base_legal, accion_recomendada, fuente, source_type, confidence) VALUES (?, ?, ?, ?, ?, ?, ?, 'DESPA_REFERENCIAL', 'ESTIMADO', 0.60)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map<String, Object> a : buildAlertas(imp, regimen, modalidad, looksRestricted(imp.getHsCode()))) {
                ps.setInt(1, imp.getId());
                ps.setString(2, imp.getHsCode());
                ps.setString(3, str(a.get("tipo")));
                ps.setString(4, str(a.get("severidad")));
                ps.setString(5, str(a.get("mensaje")));
                ps.setString(6, str(a.get("baseLegal")));
                ps.setString(7, str(a.get("accion")));
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

    private long insertManifiesto(Connection con, Importacion imp, Map<String, Object> body) throws Exception {
        return manifiestoCommandService.registrarManifiesto(con, imp, body);
    }

    private List<Map<String, Object>> queryTimeline(Connection con, int operacionId) throws Exception {
        return timelineService.queryTimeline(con, operacionId);
    }

    private List<Map<String, Object>> queryAlertas(Connection con, int operacionId) throws Exception {
        return timelineService.queryAlertas(con, operacionId);
    }

    private boolean isRestricted(Connection con, String hsCode) {
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

    private Map<String, Object> bodyWithOperacion(Map<String, Object> input, Importacion imp, Map<String, Object> regimen) {
        Map<String, Object> body = new LinkedHashMap<>(input);
        // QA-030: Remove unsafe metadata keys from body copy
        body.remove("source");
        body.remove("sourceType");
        body.remove("confidence");
        body.put("fob", imp.getValorFob());
        body.put("hsCode", imp.getHsCode());
        body.put("regimenCodigo", regimen.get("regimenCodigo"));
        body.put("restringida", looksRestricted(imp.getHsCode()));
        body.put("operacionId", imp.getId());
        return body;
    }

    private List<Map<String, Object>> buildTimeline(Connection con, Importacion imp, String modalidad) {
        LocalDateTime base = toLocal(imp.getFechaCreacion());
        List<Map<String, Object>> rows = new ArrayList<>();

        LocalDateTime fechaEmbarque = null;
        LocalDateTime fechaLlegada = null;
        LocalDateTime fechaTerminoDescarga = null;
        String nroManifiesto = "Manifiesto";
        String nroBL = "BL";

        String query = "SELECT m.numero_manifiesto, m.fecha_llegada, m.fecha_termino_descarga, d.numero_bl, d.fecha_embarque " +
                       "FROM manifiestos_carga m " +
                       "LEFT JOIN documentos_transporte d ON m.id = d.manifiesto_id " +
                       "WHERE m.operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String man = rs.getString("numero_manifiesto");
                    if (man != null) nroManifiesto = "Manifiesto NÃ‚Â° " + man;
                    String bl = rs.getString("numero_bl");
                    if (bl != null) nroBL = "BL NÃ‚Â° " + bl;
                    
                    Timestamp tsEmb = rs.getTimestamp("fecha_embarque");
                    if (tsEmb != null) fechaEmbarque = tsEmb.toLocalDateTime();
                    Timestamp tsLleg = rs.getTimestamp("fecha_llegada");
                    if (tsLleg != null) fechaLlegada = tsLleg.toLocalDateTime();
                    Timestamp tsDesc = rs.getTimestamp("fecha_termino_descarga");
                    if (tsDesc != null) fechaTerminoDescarga = tsDesc.toLocalDateTime();
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error consultando manifiesto en buildTimeline: " + e.getMessage());
        }

        LocalDateTime embDate = fechaEmbarque != null ? fechaEmbarque : base.minusDays(8);
        rows.add(event("EMBARQUE", "Embarque", embDate, "Transportista internacional", nroBL, "Inicia trazabilidad de transporte.", fechaEmbarque != null ? "Fecha de embarque verificada en BL." : "Fecha referencial si aun no se registro BL."));

        LocalDateTime arriboDate = fechaLlegada != null ? fechaLlegada : base.minusDays(1);
        rows.add(event("ARRIBO", "Arribo", arriboDate, "Transportista internacional", nroManifiesto, "Llegada del medio de transporte.", fechaLlegada != null ? "Fecha de arribo verificada en manifiesto." : "Dato manual/referencial."));

        rows.add(event("TRANSMISION_MANIFIESTO", "Manifiesto de carga", arriboDate.minusHours(12), "Transportista o agente de carga", nroManifiesto, "Permite vincular BL, bultos y deposito.", fechaLlegada != null ? "Transmision verificada." : "Pendiente de fuente oficial."));

        LocalDateTime descargaDate = fechaTerminoDescarga != null ? fechaTerminoDescarga : base.plusHours(4);
        rows.add(event("TERMINO_DESCARGA", "Termino de descarga", descargaDate, "Puerto/deposito", "IVA", "Inicia control de plazos para destinacion.", fechaTerminoDescarga != null ? "Termino de descarga verificado." : "Base para abandono legal referencial."));

        LocalDateTime almacenDate = fechaTerminoDescarga != null ? fechaTerminoDescarga.plusHours(4) : base.plusHours(8);
        rows.add(event("INGRESO_ALMACEN", "Ingreso a deposito temporal", almacenDate, "Deposito temporal", "IRM", "Confirma recepcion bajo control aduanero.", "Confirmado en almacen extraportuario."));

        LocalDateTime damDate = imp.getFechaNumeracion() != null ? imp.getFechaNumeracion().toLocalDateTime() : base.plusDays(1);
        String numDam = imp.getNumeroDam() != null ? imp.getNumeroDam() : predamNumber(imp);
        rows.add(event("NUMERACION_DAM", "Numeracion PRE-DAM referencial", damDate, "Declarante", numDam, "Simula numeracion para ordenar expediente.", imp.getNumeroDam() != null ? "DAM numerada oficialmente." : "No es DAM oficial SUNAT."));

        rows.add(event("NACIMIENTO_DTA", "Nacimiento DTA referencial", damDate, "ImportEase", "Liquidacion", "Explica cuando nace la deuda tributaria.", "Calculo estimado con datos de operacion."));
        rows.add(event("EXIGIBILIDAD_DTA", "Exigibilidad DTA referencial", damDate.plusDays(1), "ImportEase", "Liquidacion", "Controla fecha de pago o garantia.", "No reemplaza criterio SUNAT."));
        rows.add(event("PAGO_DTA", "Pago o garantia", damDate.plusDays(2), "Importador", "Constancia", "Permite continuar a levante si documentos estan completos.", "Pendiente de validacion."));
        rows.add(event("LEVANTE", "Levante referencial", damDate.plusDays(3), "Aduanas", "PRE-DAM", "Autoriza retiro si no hay observaciones.", "Simulado para demo."));
        rows.add(event("RETIRO_MERCANCIA", "Retiro de mercancia", damDate.plusDays(4), "Importador/deposito", "Salida almacen", "Cierre operativo del retiro.", "Simulado para demo."));
        
        if ("M3_DEPOSITO".equals(modalidad) || "TEMPORAL".equals(modalidad)) {
            rows.add(event("REGULARIZACION", "Regularizacion", damDate.plusDays(30), "Declarante", "Expediente", "Cierra regimen o control posterior.", "Controlar plazo segun regimen."));
        }
        return rows;
    }

    private List<Map<String, Object>> buildTimeline(Importacion imp, String modalidad) {
        LocalDateTime base = toLocal(imp.getFechaCreacion());
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(event("EMBARQUE", "Embarque", base.minusDays(8), "Transportista internacional", "BL", "Inicia trazabilidad de transporte.", "Fecha referencial si aun no se registro manifiesto."));
        rows.add(event("ARRIBO", "Arribo", base.minusDays(1), "Transportista internacional", "Manifiesto", "Llegada del medio de transporte.", "Dato manual/referencial."));
        rows.add(event("TRANSMISION_MANIFIESTO", "Manifiesto de carga", base.minusHours(12), "Transportista o agente de carga", "Manifiesto", "Permite vincular BL, bultos y deposito.", "Pendiente de fuente oficial."));
        rows.add(event("TERMINO_DESCARGA", "Termino de descarga", base.plusHours(4), "Puerto/deposito", "IVA", "Inicia control de plazos para destinacion.", "Base para abandono legal referencial."));
        rows.add(event("INGRESO_ALMACEN", "Ingreso a deposito temporal", base.plusHours(8), "Deposito temporal", "IRM", "Confirma recepcion bajo control aduanero.", "Registrar pesos/bultos si aplica."));
        rows.add(event("NUMERACION_DAM", "Numeracion PRE-DAM referencial", base.plusDays(1), "Declarante", predamNumber(imp), "Simula numeracion para ordenar expediente.", "No es DAM oficial SUNAT."));
        rows.add(event("NACIMIENTO_DTA", "Nacimiento DTA referencial", base.plusDays(1), "ImportEase", "Liquidacion", "Explica cuando nace la deuda tributaria.", "Calculo estimado con datos de operacion."));
        rows.add(event("EXIGIBILIDAD_DTA", "Exigibilidad DTA referencial", base.plusDays(2), "ImportEase", "Liquidacion", "Controla fecha de pago o garantia.", "No reemplaza criterio SUNAT."));
        rows.add(event("PAGO_DTA", "Pago o garantia", base.plusDays(3), "Importador", "Constancia", "Permite continuar a levante si documentos estan completos.", "Pendiente de validacion."));
        rows.add(event("LEVANTE", "Levante referencial", base.plusDays(4), "Aduanas", "PRE-DAM", "Autoriza retiro si no hay observaciones.", "Simulado para demo."));
        rows.add(event("RETIRO_MERCANCIA", "Retiro de mercancia", base.plusDays(5), "Importador/deposito", "Salida almacen", "Cierre operativo del retiro.", "Simulado para demo."));
        if ("M3_DEPOSITO".equals(modalidad) || "TEMPORAL".equals(modalidad)) {
            rows.add(event("REGULARIZACION", "Regularizacion", base.plusDays(30), "Declarante", "Expediente", "Cierra regimen o control posterior.", "Controlar plazo segun regimen."));
        }
        return rows;
    }

    private List<Map<String, Object>> buildAlertas(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad, boolean restringida) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (restringida) {
            list.add(alert("MERCANCIA_RESTRINGIDA", "ALTA", "El HS sugiere mercancia restringida o de control sectorial.", "VUCE/SUNAT referencial", "Revisar permisos antes de embarcar o nacionalizar."));
        }
        if (imp.getValorFobBD().compareTo(new BigDecimal("2000")) > 0) {
            list.add(alert("AGENTE_ADUANA_MANDATORIO", "ALTA", "El valor FOB supera los USD 2000. La Ley General de Aduanas exige obligatoriamente contratar un Agente de Aduanas licenciado.", "LGA Art. 21", "Contratar una agencia de aduanas autorizada por SUNAT para numerar el Despacho General."));
        }
        if (imp.getValorFobBD().compareTo(new BigDecimal("2000")) > 0 && "10".equals(regimen.get("regimenCodigo")) && "ANTICIPADO".equals(modalidad.get("modalidadCodigo"))) {
            list.add(alert("DESPACHO_ANTICIPADO", "MEDIA", "La modalidad anticipada queda como recomendada/general en esta simulacion.", "DESPA-PG.01 referencial", "Preparar documentos antes del arribo."));
        }
        if (!hasPredam(imp)) {
            list.add(alert("PREDAM_REFERENCIAL", "BAJA", "La PRE-DAM es referencial y debe validarse con agente de aduana.", "Simulacion academica", "No presentarla como DAM oficial."));
        }
        if ("36".equals(regimen.get("regimenCodigo"))) {
            list.add(alert("CANAL_ROJO_REIMPORTACION", "ALTA", "Reimportacion exige comprobar que la mercancia retorna en el mismo estado.", "DESPA-PG.26 referencial", "Preparar declaracion de exportacion precedente."));
        }
        if ("ADM_TEMP".equals(regimen.get("regimenCodigo")) || "80".equals(regimen.get("regimenCodigo"))) {
            list.add(alert("GARANTIA_REQUERIDA", "ALTA", "El regimen seleccionado requiere garantia referencial.", "Procedimiento DESPA referencial", "Calcular monto minimo y fecha de vencimiento."));
        }
        return list;
    }

    private List<Map<String, Object>> checklist(String regimen, Importacion imp, boolean restringida) {
        return checklist(regimen, imp, restringida, false);
    }

    private List<Map<String, Object>> checklist(String regimen, Importacion imp, boolean restringida, boolean tlcAplicado) {
        List<Map<String, Object>> docs = new ArrayList<>();
        docs.add(doc("FACTURA_COMERCIAL", "Factura comercial", "Debe incluir descripcion, moneda, valor unitario, Incoterm y forma de pago.", true, "BD_LOCAL"));
        docs.add(doc("DOCUMENTO_TRANSPORTE", "BL/AWB/documento de transporte", "Vincula manifiesto, consignatario, bultos y peso.", true, "BD_LOCAL"));
        docs.add(doc("SEGURO", "Seguro", "Obligatorio para sustentar valor si corresponde por Incoterm.", "CIF".equalsIgnoreCase(imp.getIncoterm()) || "CIP".equalsIgnoreCase(imp.getIncoterm()), "ESTIMADO"));
        // QA-014: CERTIFICADO_ORIGEN required if TLC applied
        boolean certOrigenRequerido = tlcAplicado;
        docs.add(doc("CERTIFICADO_ORIGEN", "Certificado de origen", "Necesario si deseas aplicar preferencia arancelaria.", certOrigenRequerido, "ESTIMADO"));
        if (restringida) docs.add(doc("PERMISO_SECTORIAL", "Permiso sectorial/VUCE", "Requerido por posible mercancia restringida.", true, "BD_LOCAL"));
        if ("ADM_TEMP".equals(regimen)) {
            docs.add(doc("GARANTIA", "Garantia", "Cubre tributos, recargos e intereses proyectados.", true, "ESTIMADO"));
            docs.add(doc("DJ_UBICACION_FINALIDAD", "Declaracion jurada de ubicacion y finalidad", "Explica uso temporal y ubicacion de la mercancia.", true, "BD_LOCAL"));
        }
        if ("36".equals(regimen)) docs.add(doc("DECLARACION_EXPORTACION", "Declaracion de exportacion precedente", "Debe estar regularizada y dentro de plazo.", true, "BD_LOCAL"));
        if ("TRANSBORDO".equals(regimen)) docs.add(doc("REGULARIZACION_TRANSBORDO", "Control de regularizacion", "Pesos, bultos, contenedores, precintos y aduana de salida.", true, "BD_LOCAL"));
        return docs;
    }

    private List<Map<String, Object>> baseLegal(String regimen, Importacion imp) {
        return legalService.buildBaseLegal(regimen, imp);
    }

    private Map<String, Object> dtaMap(Importacion imp) {
        return dtaMapper.toMap(imp);
    }

    private Map<String, Object> predamMap(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad) {
        return predamMapper.toMap(imp, regimen, modalidad);
    }

    private Map<String, Object> panelSalud(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad, List<Map<String, Object>> documentos, List<Map<String, Object>> plazos, List<Map<String, Object>> alertas, Map<String, Object> predam) {
        Map<String, Object> panel = new LinkedHashMap<>();
        
        int docsTotal = documentos.size();
        int docsCompletos = hasPredam(imp) ? docsTotal : Math.max(0, docsTotal - 3); // Simulado
        int completitud = hasPredam(imp) ? 100 : (imp.getValorFobBD().compareTo(BigDecimal.ZERO) > 0 && imp.getHsCode() != null ? 68 : 30);
        
        long plazosCriticos = plazos.stream().filter(p -> "CRITICAL".equals(p.get("status")) || "EXPIRED".equals(p.get("status"))).count();
        
        panel.put("completitud", completitud);
        panel.put("docsCompletos", docsCompletos);
        panel.put("docsTotal", docsTotal);
        panel.put("alertasCriticas", plazosCriticos);
        panel.put("fuentesManuales", 3); // Simulado para dashboard
        panel.put("fuentesSimuladas", 2); // Simulado para dashboard
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

    private Map<String, Object> event(String codigo, String nombre, LocalDateTime fecha, String responsable, String documento, String efecto, String observacion) {
        return responseBuilder.event(codigo, nombre, fecha, responsable, documento, efecto, observacion);
    }

    private Map<String, Object> alert(String tipo, String severidad, String mensaje, String baseLegal, String accion) {
        return responseBuilder.alert(tipo, severidad, mensaje, baseLegal, accion);
    }

    private Map<String, Object> doc(String codigo, String nombre, String descripcion, boolean requerido, String sourceType) {
        return responseBuilder.doc(codigo, nombre, descripcion, requerido, sourceType);
    }

    private Map<String, Object> legal(String modulo, String procedimiento, String validacion, String base, String estado, String accion) {
        return responseBuilder.legal(modulo, procedimiento, validacion, base, estado, accion);
    }

    private Map<String, Object> item(String label, boolean value) {
        return responseBuilder.item(label, value);
    }

    private Map<String, Object> baseResult() {
        return responseBuilder.baseResult();
    }

    private Map<String, Object> errorData(String code, String message) {
        return responseBuilder.errorData(code, message);
    }

    private List<String> faltantesReimportacion(boolean exportada, boolean regularizada, boolean enPlazo, boolean transformada) {
        List<String> faltantes = new ArrayList<>();
        if (!exportada) faltantes.add("Confirmar exportacion definitiva previa desde Peru.");
        if (!regularizada) faltantes.add("Regularizar declaracion de exportacion precedente.");
        if (!enPlazo) faltantes.add("Validar que no pasaron mas de 12 meses.");
        if (transformada) faltantes.add("No debe haber transformacion, reparacion ni elaboracion en el extranjero.");
        return faltantes;
    }

    private List<String> alertasTransbordo(boolean pesoOk, boolean precintoOk, boolean solicitudesPendientes) {
        List<String> alertas = new ArrayList<>();
        if (!pesoOk) alertas.add("Diferencia de peso mayor al 2%; requiere revision.");
        if (!precintoOk) alertas.add("Precinto violado o no conforme; requiere control.");
        if (solicitudesPendientes) alertas.add("Existen solicitudes pendientes que impiden regularizar.");
        if (alertas.isEmpty()) alertas.add("Sin alertas criticas para regularizacion referencial.");
        return alertas;
    }

    private String canalProbable(String regimen, Importacion imp) {
        if ("36".equals(regimen)) return "ROJO";
        if (looksRestricted(imp.getHsCode())) return "ROJO";
        if (imp.getValorCifBD().compareTo(new BigDecimal("10000")) > 0) return "NARANJA";
        return "VERDE";
    }

    private String predamNumber(Importacion imp) {
        return predamMapper.predamNumber(imp);
    }

    private String procedimiento(String regimen) {
        return procedureCatalog.procedimiento(regimen);
    }

    private String plazoTexto(String modalidad) {
        return procedureCatalog.plazoTexto(modalidad);
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

    private LocalDateTime toLocal(Timestamp ts) {
        if (ts == null) return LocalDateTime.now(ZoneId.of("America/Lima"));
        return ts.toLocalDateTime();
    }

    private Timestamp tsOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            String clean = value.trim();
            if (clean.length() == 10) clean += " 00:00:00";
            clean = clean.replace("T", " ");
            if (clean.length() == 16) clean += ":00";
            return Timestamp.valueOf(clean);
        } catch (Exception e) {
            LoggerUtil.warn("Error al parsear timestamp, valor invalido: " + value);
            throw new IllegalArgumentException("Valor de timestamp invalido: " + value, e);
        }
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim();
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String upper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private boolean bool(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        String s = String.valueOf(value).trim().toLowerCase();
        return "true".equals(s) || "si".equals(s) || "yes".equals(s) || "1".equals(s);
    }

    private int asInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }

    private double asDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }
}


