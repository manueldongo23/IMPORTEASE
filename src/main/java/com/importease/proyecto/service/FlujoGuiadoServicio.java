package com.importease.proyecto.service;

import com.importease.proyecto.dto.PasoGuiadoDTO;
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.repository.guidedflow.FlujoGuiadoPasoRepositorio;
import com.importease.proyecto.service.guidedflow.FlujoGuiadoReglaServicio;
import com.importease.proyecto.service.guidedflow.FlujoGuiadoPasoCatalogo;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.AuditoriaServicio;
import com.importease.proyecto.service.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;

public class FlujoGuiadoServicio {

    public static final int PASO_INTENCION = 1;
    public static final int PASO_DATOS_BASICOS = 2;
    public static final int PASO_CLASIFICACION = 3;
    public static final int PASO_TRANSPORTE = 4;
    public static final int PASO_DOCUMENTOS = 5;
    public static final int PASO_COHERENCIA = 6;
    public static final int PASO_DTA_PRE_DAM = 7;
    public static final int PASO_REVISION_FINAL = 8;

    private static final int TOTAL_PASOS = 8;

    private final ImportacionRepositorio importacionRepositorio;
    private final FlujoGuiadoPasoRepositorio stepRepository;
    private final FlujoGuiadoPasoCatalogo stepCatalog;
    private final FlujoGuiadoReglaServicio ruleService;

    public FlujoGuiadoServicio() {
        this(new ImportacionRepositorio(), new FlujoGuiadoPasoRepositorio(), new FlujoGuiadoPasoCatalogo(), new FlujoGuiadoReglaServicio());
    }

    FlujoGuiadoServicio(ImportacionRepositorio importacionRepositorio) {
        this(importacionRepositorio, new FlujoGuiadoPasoRepositorio(), new FlujoGuiadoPasoCatalogo(), new FlujoGuiadoReglaServicio(importacionRepositorio));
    }

    FlujoGuiadoServicio(ImportacionRepositorio importacionRepositorio, FlujoGuiadoPasoRepositorio stepRepository) {
        this(importacionRepositorio, stepRepository, new FlujoGuiadoPasoCatalogo(), new FlujoGuiadoReglaServicio(importacionRepositorio));
    }

    FlujoGuiadoServicio(ImportacionRepositorio importacionRepositorio, FlujoGuiadoPasoRepositorio stepRepository, FlujoGuiadoPasoCatalogo stepCatalog, FlujoGuiadoReglaServicio ruleService) {
        this.importacionRepositorio = importacionRepositorio;
        this.stepRepository = stepRepository;
        this.stepCatalog = stepCatalog;
        this.ruleService = ruleService;
    }

    static String getPasoEstado(int expedienteId, int paso) {
        return new FlujoGuiadoPasoRepositorio().getPasoEstado(expedienteId, paso);
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
        int count = 0;
        for (int i = 1; i <= TOTAL_PASOS; i++) {
            if ("COMPLETO".equals(getStepEstado(con, expedienteId, i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * Obtiene el paso actual del flujo guiado, calculando estado, motivo de bloqueo
     * y porcentaje de avance.
     */
    public PasoGuiadoDTO obtenerPasoActual(int expedienteId) {
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
                return new PasoGuiadoDTO(TOTAL_PASOS, obtenerNombrePaso(TOTAL_PASOS),
                        obtenerDescripcionPaso(TOTAL_PASOS, true), "COMPLETO", null, BigDecimal.valueOf(100));
            }

            String validationError = ruleService.validateStep(con, currentStep, expedienteId);
            String estado = "PENDIENTE";
            String motivoBloqueo = null;
            int completedCount = countCompletedSteps(con, expedienteId);
            BigDecimal pct = BigDecimal.valueOf(completedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);

            if ("BLOQUEADO".equals(getStepEstado(con, expedienteId, currentStep))) {
                // re‑validar; if no error, unlock
                if (validationError == null) {
                    setStepEstado(con, expedienteId, currentStep, "PENDIENTE");
                } else {
                    motivoBloqueo = validationError;
                    estado = "BLOQUEADO";
                }
            } else if (validationError != null) {
                setStepEstado(con, expedienteId, currentStep, "OBSERVADO");
                estado = "OBSERVADO";
                motivoBloqueo = validationError;
            }

            return new PasoGuiadoDTO(currentStep, obtenerNombrePaso(currentStep),
                    obtenerDescripcionPaso(currentStep, true), estado, motivoBloqueo, pct);
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener paso actual", e);
        }
        return null;
    }

    /**
     * Avanza al siguiente paso del flujo guiado.
     */
    public PasoGuiadoDTO avanzarPaso(int expedienteId, int usuarioId) {
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
                return new PasoGuiadoDTO(TOTAL_PASOS, obtenerNombrePaso(TOTAL_PASOS),
                        obtenerDescripcionPaso(TOTAL_PASOS, true), "COMPLETO", null, BigDecimal.valueOf(100));
            }

            String validationError = ruleService.validateStep(con, currentStep, expedienteId);

            if (validationError != null) {
                setStepEstado(con, expedienteId, currentStep, "OBSERVADO");
                int completedCount = countCompletedSteps(con, expedienteId);
                BigDecimal pct = BigDecimal.valueOf(completedCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);
                AuditoriaServicio.registrar(usuarioId, "AVANZAR_PASO", "expedientes", expedienteId,
                        "Paso " + currentStep + " (" + obtenerNombrePaso(currentStep) + ") OBSERVADO: " + validationError,
                        null, null);
                return new PasoGuiadoDTO(currentStep, obtenerNombrePaso(currentStep),
                        obtenerDescripcionPaso(currentStep, true), "OBSERVADO", validationError, pct);
            }

            setStepEstado(con, expedienteId, currentStep, "COMPLETO");
            int nextStep = currentStep + 1;
            if (nextStep <= TOTAL_PASOS) {
                String nextEstado = getStepEstado(con, expedienteId, nextStep);
                if (nextEstado == null) {
                    setStepEstado(con, expedienteId, nextStep, "PENDIENTE");
                }
            }

            AuditoriaServicio.registrar(usuarioId, "AVANZAR_PASO", "expedientes", expedienteId,
                    "Avanzó al paso " + (currentStep + 1) + " (" + obtenerNombrePaso(currentStep + 1) + ")",
                    null, null);

            int completedCount = countCompletedSteps(con, expedienteId);
            BigDecimal pct = BigDecimal.valueOf(completedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(TOTAL_PASOS), 0, RoundingMode.HALF_UP);

            return new PasoGuiadoDTO(nextStep <= TOTAL_PASOS ? nextStep : TOTAL_PASOS,
                    obtenerNombrePaso(nextStep <= TOTAL_PASOS ? nextStep : TOTAL_PASOS),
                    obtenerDescripcionPaso(nextStep <= TOTAL_PASOS ? nextStep : TOTAL_PASOS, true),
                    nextStep <= TOTAL_PASOS ? "PENDIENTE" : "COMPLETO", null, pct);
        } catch (SQLException e) {
            LoggerUtil.error("Error al avanzar paso", e);
        }
        return obtenerPasoActual(expedienteId);
    }

    public PasoGuiadoDTO retrocederPaso(int expedienteId, int usuarioId) {
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
            return new PasoGuiadoDTO(prevStep, obtenerNombrePaso(prevStep),
                    obtenerDescripcionPaso(prevStep, true), "PENDIENTE", null, pct);
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
                if (est == null || "PENDIENTE".equals(est) || "BLOQUEADO".equals(est)) {
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

    /**
     * Desbloquea un paso previamente marcado como BLOQUEADO.
     */
    public void desbloquearPaso(int expedienteId, int paso) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            initStepsIfNeeded(con, expedienteId);
            setStepEstado(con, expedienteId, paso, "PENDIENTE");
        } catch (SQLException e) {
            LoggerUtil.error("Error al desbloquear paso", e);
        }
    }
}
