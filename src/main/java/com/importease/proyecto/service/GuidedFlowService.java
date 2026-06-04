package com.importease.proyecto.service;

import com.importease.proyecto.dto.GuidedStepDTO;
import com.importease.proyecto.repository.ImportacionDAO;
import com.importease.proyecto.repository.guidedflow.GuidedFlowStepRepository;
import com.importease.proyecto.service.guidedflow.GuidedFlowRuleService;
import com.importease.proyecto.service.guidedflow.GuidedFlowStepCatalog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

public class GuidedFlowService {

    public static final int PASO_INTENCION = 1;
    public static final int PASO_DATOS_BASICOS = 2;
    public static final int PASO_CLASIFICACION = 3;
    public static final int PASO_TRANSPORTE = 4;
    public static final int PASO_DOCUMENTOS = 5;
    public static final int PASO_COHERENCIA = 6;
    public static final int PASO_DTA_PRE_DAM = 7;
    public static final int PASO_REVISION_FINAL = 8;

    private static final int TOTAL_PASOS = 8;

    private final ImportacionDAO importacionDAO;
    private final GuidedFlowStepRepository stepRepository;
    private final GuidedFlowStepCatalog stepCatalog;
    private final GuidedFlowRuleService ruleService;

    public GuidedFlowService() {
        this(new ImportacionDAO(), new GuidedFlowStepRepository(), new GuidedFlowStepCatalog(), new GuidedFlowRuleService());
    }

    GuidedFlowService(ImportacionDAO importacionDAO) {
        this(importacionDAO, new GuidedFlowStepRepository(), new GuidedFlowStepCatalog(), new GuidedFlowRuleService(importacionDAO));
    }

    GuidedFlowService(ImportacionDAO importacionDAO, GuidedFlowStepRepository stepRepository) {
        this(importacionDAO, stepRepository, new GuidedFlowStepCatalog(), new GuidedFlowRuleService(importacionDAO));
    }

    GuidedFlowService(ImportacionDAO importacionDAO, GuidedFlowStepRepository stepRepository, GuidedFlowStepCatalog stepCatalog, GuidedFlowRuleService ruleService) {
        this.importacionDAO = importacionDAO;
        this.stepRepository = stepRepository;
        this.stepCatalog = stepCatalog;
        this.ruleService = ruleService;
    }

    static String getPasoEstado(int expedienteId, int paso) {
        return new GuidedFlowStepRepository().getPasoEstado(expedienteId, paso);
    }

    private void initStepsIfNeeded(Connection con, int expedienteId) {
        stepRepository.initStepsIfNeeded(con, expedienteId, TOTAL_PASOS);
    }

    private String getStepEstado(Connection con, int expedienteId, int paso) {
        return stepRepository.getStepEstado(con, expedienteId, paso);
    }

    private String getMotivoBloqueo(Connection con, int expedienteId, int paso) {
        return stepRepository.getMotivoBloqueo(con, expedienteId, paso);
    }

    private void setStepEstado(Connection con, int expedienteId, int paso, String estado) {
        stepRepository.setStepEstado(con, expedienteId, paso, estado);
    }

    private void setStepEstado(Connection con, int expedienteId, int paso, String estado, String motivo) {
        stepRepository.setStepEstado(con, expedienteId, paso, estado, motivo);
    }

    private int countCompletedSteps(Connection con, int expedienteId) {
        return stepRepository.countCompletedSteps(con, expedienteId);
    }

    public GuidedStepDTO obtenerPasoActual(int expedienteId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            initStepsIfNeeded(con, expedienteId);

            int stepActual = 1;
            for (int i = 1; i <= TOTAL_PASOS; i++) {
                String est = getStepEstado(con, expedienteId, i);
                if (est == null || "PENDIENTE".equals(est)) {
                    stepActual = i;
                    break;
                }
                if ("BLOQUEADO".equals(est) || "OBSERVADO".equals(est)) {
                    stepActual = i;
                    break;
                }
                if ("COMPLETO".equals(est) && i == TOTAL_PASOS) {
                    stepActual = TOTAL_PASOS;
                }
            }

            String estado = getStepEstado(con, expedienteId, stepActual);
            if (estado == null) {
                estado = "PENDIENTE";
            }

            String motivoBloqueo = null;
            if ("BLOQUEADO".equals(estado)) {
                motivoBloqueo = getMotivoBloqueo(con, expedienteId, stepActual);
                if (motivoBloqueo == null) {
                    motivoBloqueo = "Paso bloqueado: revise las validaciones pendientes.";
                }
            }

            int completedCount = countCompletedSteps(con, expedienteId);
            BigDecimal pct = BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);

            return new GuidedStepDTO(
                stepActual,
                obtenerNombrePaso(stepActual),
                obtenerDescripcionPaso(stepActual, true),
                estado,
                motivoBloqueo,
                pct
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener paso actual", e);
        }

        return new GuidedStepDTO(1, obtenerNombrePaso(1), obtenerDescripcionPaso(1, true), "PENDIENTE", null, BigDecimal.ZERO);
    }

    public GuidedStepDTO avanzarPaso(int expedienteId, int usuarioId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            initStepsIfNeeded(con, expedienteId);

            int currentStep = 1;
            for (int i = 1; i <= TOTAL_PASOS; i++) {
                String est = getStepEstado(con, expedienteId, i);
                if (est == null || "PENDIENTE".equals(est) || "BLOQUEADO".equals(est) || "OBSERVADO".equals(est)) {
                    currentStep = i;
                    break;
                }
            }

            if (currentStep > TOTAL_PASOS) {
                int completedCount = countCompletedSteps(con, expedienteId);
                return new GuidedStepDTO(TOTAL_PASOS, obtenerNombrePaso(TOTAL_PASOS),
                    obtenerDescripcionPaso(TOTAL_PASOS, true), "COMPLETO", null,
                    BigDecimal.valueOf(100));
            }

            String validationError = ruleService.validateStep(con, currentStep, expedienteId);

            if (validationError != null) {
                setStepEstado(con, expedienteId, currentStep, "OBSERVADO");

                int completedCount = countCompletedSteps(con, expedienteId);
                BigDecimal pct = BigDecimal.valueOf(completedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);

                AuditoriaService.registrar(usuarioId, "AVANZAR_PASO", "expedientes", expedienteId,
                    "Paso " + currentStep + " (" + obtenerNombrePaso(currentStep) + ") OBSERVADO: " + validationError,
                    null, null);

                return new GuidedStepDTO(
                    currentStep,
                    obtenerNombrePaso(currentStep),
                    obtenerDescripcionPaso(currentStep, true),
                    "OBSERVADO",
                    validationError,
                    pct
                );
            }

            setStepEstado(con, expedienteId, currentStep, "COMPLETO");
            int nextStep = currentStep + 1;
            if (nextStep <= TOTAL_PASOS) {
                String nextEstado = getStepEstado(con, expedienteId, nextStep);
                if (nextEstado == null) {
                    setStepEstado(con, expedienteId, nextStep, "PENDIENTE");
                }
            }

            AuditoriaService.registrar(usuarioId, "AVANZAR_PASO", "expedientes", expedienteId,
                "AvanzÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â³ al paso " + (currentStep + 1) + " (" + obtenerNombrePaso(currentStep + 1) + ")",
                null, null);

            int completedCount = countCompletedSteps(con, expedienteId);
            BigDecimal pct = BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);

            return new GuidedStepDTO(
                nextStep <= TOTAL_PASOS ? nextStep : TOTAL_PASOS,
                obtenerNombrePaso(nextStep <= TOTAL_PASOS ? nextStep : TOTAL_PASOS),
                obtenerDescripcionPaso(nextStep <= TOTAL_PASOS ? nextStep : TOTAL_PASOS, true),
                nextStep <= TOTAL_PASOS ? "PENDIENTE" : "COMPLETO",
                null, pct
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error al avanzar paso", e);
        }

        return obtenerPasoActual(expedienteId);
    }

    public GuidedStepDTO retrocederPaso(int expedienteId, int usuarioId) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            initStepsIfNeeded(con, expedienteId);

            int currentStep = 1;
            for (int i = 1; i <= TOTAL_PASOS; i++) {
                String est = getStepEstado(con, expedienteId, i);
                if (est == null || "PENDIENTE".equals(est) || "BLOQUEADO".equals(est) || "OBSERVADO".equals(est)) {
                    currentStep = i;
                    break;
                }
                currentStep = i + 1;
            }
            if (currentStep > TOTAL_PASOS) {
                currentStep = TOTAL_PASOS;
            }

            if (currentStep <= 1) {
                return obtenerPasoActual(expedienteId);
            }

            int prevStep = currentStep - 1;
            setStepEstado(con, expedienteId, prevStep, "PENDIENTE");

            int completedCount = countCompletedSteps(con, expedienteId);
            BigDecimal pct = BigDecimal.valueOf(completedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);

            return new GuidedStepDTO(
                prevStep, obtenerNombrePaso(prevStep),
                obtenerDescripcionPaso(prevStep, true), "PENDIENTE", null, pct
            );
        } catch (SQLException e) {
            LoggerUtil.error("Error al retroceder paso", e);
        }

        return obtenerPasoActual(expedienteId);
    }

    public void bloquearPaso(int expedienteId, String motivo) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            initStepsIfNeeded(con, expedienteId);

            int currentStep = 1;
            for (int i = 1; i <= TOTAL_PASOS; i++) {
                String est = getStepEstado(con, expedienteId, i);
                if (est == null || "PENDIENTE".equals(est)) {
                    currentStep = i;
                    break;
                }
                if ("BLOQUEADO".equals(est)) {
                    currentStep = i;
                    break;
                }
            }

            setStepEstado(con, expedienteId, currentStep, "BLOQUEADO", motivo);
        } catch (SQLException e) {
            LoggerUtil.error("Error al bloquear paso", e);
        }
    }

    public String obtenerNombrePaso(int paso) {
        return stepCatalog.obtenerNombrePaso(paso);
    }

    public String obtenerDescripcionPaso(int paso, boolean modoPrincipiante) {
        return stepCatalog.obtenerDescripcionPaso(paso, modoPrincipiante);
    }

    public static boolean isPasoCompleto(int expedienteId, int paso) {
        return "COMPLETO".equals(getPasoEstado(expedienteId, paso));
    }
}




