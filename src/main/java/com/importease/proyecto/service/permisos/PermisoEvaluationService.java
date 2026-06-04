package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.ReglaRestriccion;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.repository.OperacionDAO;
import com.importease.proyecto.repository.PermisoDAO;
import com.importease.proyecto.service.LoggerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermisoEvaluationService {
    private final PermisoDAO permisoDAO;
    private final OperacionDAO operacionDAO;

    public PermisoEvaluationService() {
        this(new PermisoDAO(), new OperacionDAO());
    }

    public PermisoEvaluationService(PermisoDAO permisoDAO, OperacionDAO operacionDAO) {
        this.permisoDAO = permisoDAO;
        this.operacionDAO = operacionDAO;
    }

    public Map<String, Object> evaluarOperacion(int operacionId) {
        Map<String, Object> resultado = baseResultado();
        Operacion op = operacionDAO.obtenerPorId(operacionId);
        if (op == null) {
            LoggerUtil.warn("Operacion no encontrada para evaluacion de permisos: " + operacionId);
            return resultado;
        }

        List<ReglaRestriccion> reglasUnicas = buscarReglasUnicas(op.getHsCode(), op.getProductoDesc());
        boolean libre = reglasUnicas.isEmpty();
        List<SolicitudPermiso> solicitudesCreadas = new ArrayList<>();
        Set<String> entidadCodigos = new LinkedHashSet<>();
        List<EntidadControl> entidades = new ArrayList<>();
        String nivelRiesgoMax = "BAJO";

        if (!libre) {
            for (ReglaRestriccion regla : reglasUnicas) {
                SolicitudPermiso sol = crearSolicitud(op, operacionId, regla);
                int idGenerado = permisoDAO.crearSolicitud(sol);
                if (idGenerado > 0) solicitudesCreadas.add(sol);

                if (entidadCodigos.add(regla.getCodigoEntidad())) {
                    EntidadControl ent = permisoDAO.obtenerEntidad(regla.getCodigoEntidad());
                    if (ent != null) entidades.add(ent);
                }
                nivelRiesgoMax = riesgoMaximo(nivelRiesgoMax, regla.getNivelRiesgo());
            }
        }

        resultado.put("restricciones", reglasUnicas);
        resultado.put("solicitudes", solicitudesCreadas);
        resultado.put("entidades", entidades);
        resultado.put("nivelRiesgoMax", nivelRiesgoMax);
        resultado.put("libre", libre);
        return resultado;
    }

    private List<ReglaRestriccion> buscarReglasUnicas(String hsCode, String descripcion) {
        List<ReglaRestriccion> reglasPorHs = permisoDAO.buscarReglasPorHsCode(hsCode);
        List<ReglaRestriccion> reglasPorPalabra = permisoDAO.buscarReglasPorPalabraClave(descripcion);
        Map<Integer, ReglaRestriccion> reglasMap = new LinkedHashMap<>();
        for (ReglaRestriccion r : reglasPorHs) reglasMap.put(r.getId(), r);
        for (ReglaRestriccion r : reglasPorPalabra) reglasMap.put(r.getId(), r);
        return new ArrayList<>(reglasMap.values());
    }

    private SolicitudPermiso crearSolicitud(Operacion op, int operacionId, ReglaRestriccion regla) {
        SolicitudPermiso sol = new SolicitudPermiso();
        sol.setOperacionId(operacionId);
        sol.setUsuarioId(op.getUsuarioId());
        sol.setCodigoEntidad(regla.getCodigoEntidad());
        sol.setTipoPermiso(regla.getTipoPermiso());
        sol.setEstado("PERMISO_REQUERIDO");
        sol.setObservaciones(regla.getMensajeUsuario());
        return sol;
    }

    private String riesgoMaximo(String actual, String nuevo) {
        if ("ALTO".equals(nuevo)) return "ALTO";
        if ("MEDIO".equals(nuevo) && !"ALTO".equals(actual)) return "MEDIO";
        return actual;
    }

    private Map<String, Object> baseResultado() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("libre", true);
        resultado.put("restricciones", new ArrayList<>());
        resultado.put("solicitudes", new ArrayList<>());
        resultado.put("entidades", new ArrayList<>());
        resultado.put("nivelRiesgoMax", "BAJO");
        return resultado;
    }
}
