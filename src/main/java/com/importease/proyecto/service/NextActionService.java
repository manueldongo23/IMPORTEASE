package com.importease.proyecto.service;

import com.importease.proyecto.dto.NextActionDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionDAO;

import java.util.ArrayList;
import java.util.List;

public class NextActionService {

    private final ImportacionDAO importacionDAO;

    public NextActionService() {
        this.importacionDAO = new ImportacionDAO();
    }

    NextActionService(ImportacionDAO importacionDAO) {
        this.importacionDAO = importacionDAO;
    }

    public NextActionDTO calcularSiguienteAccion(int expedienteId) {
        Importacion imp = null;
        try {
            imp = importacionDAO.buscarPorId(expedienteId);
        } catch (Exception e) {
            LoggerUtil.error("Error al buscar importacion en NextActionService", e);
        }

        boolean paso1Ok = isPasoCompleto(expedienteId, 1);
        boolean paso2Ok = isPasoCompleto(expedienteId, 2);
        boolean paso3Ok = isPasoCompleto(expedienteId, 3);
        boolean paso4Ok = isPasoCompleto(expedienteId, 4);
        boolean paso5Ok = isPasoCompleto(expedienteId, 5);
        boolean paso6Ok = isPasoCompleto(expedienteId, 6);
        boolean paso7Ok = isPasoCompleto(expedienteId, 7);

        if (!paso1Ok) {
            return new NextActionDTO(
                "Define quÃƒÂ© deseas hacer con tu mercancÃƒÂ­a",
                "AÃƒÂºn no has definido el rÃƒÂ©gimen aduanero de la operaciÃƒÂ³n",
                1, "HIGH", null,
                List.of("regimen", "tipoOperacion")
            );
        }

        if (!paso2Ok) {
            List<String> campos = new ArrayList<>();
            if (imp == null || imp.getProductoDesc() == null || imp.getProductoDesc().isBlank()) campos.add("productoDesc");
            if (imp == null || imp.getPaisOrigen() == null || imp.getPaisOrigen().isBlank()) campos.add("paisOrigen");
            if (imp == null || imp.getIncoterm() == null || imp.getIncoterm().isBlank()) campos.add("incoterm");
            if (imp == null || imp.getValorFob() <= 0) campos.add("fob");
            return new NextActionDTO(
                "Registra FOB, paÃƒÂ­s de origen y descripciÃƒÂ³n",
                "Completa los datos bÃƒÂ¡sicos de la operaciÃƒÂ³n",
                2, "HIGH", null, campos
            );
        }

        if (!paso3Ok) {
            List<String> campos = new ArrayList<>();
            if (imp == null || imp.getHsCode() == null || !imp.getHsCode().matches("\\d{6,10}")) campos.add("hsCode");
            return new NextActionDTO(
                "Clasifica tu mercancÃƒÂ­a con el HS Code",
                "Se necesita el cÃƒÂ³digo arancelario para continuar",
                3, "HIGH", null, campos
            );
        }

        if (!paso4Ok) {
            return new NextActionDTO(
                "Registra transporte y manifiesto de carga",
                "Vincula el BL/AWB y las fechas de embarque/llegada",
                4, "HIGH", null,
                List.of("manifiesto", "fechaLlegada", "documentoTransporte")
            );
        }

        if (!paso5Ok) {
            return new NextActionDTO(
                "Completa el checklist documental",
                "Verifica que todos los documentos obligatorios estÃƒÂ©n cargados",
                5, "HIGH", null,
                List.of("facturaComercial", "billOfLading", "certificadoOrigen")
            );
        }

        if (!paso6Ok) {
            String bloqueo = verificarBloqueoCoherencia(imp);
            return new NextActionDTO(
                "Ejecuta validaciÃƒÂ³n de coherencia aduanera",
                "Verifica consistencia entre todos los datos del expediente",
                6, "MEDIUM", bloqueo,
                List.of()
            );
        }

        if (!paso7Ok) {
            return new NextActionDTO(
                "Genera el cÃƒÂ¡lculo DTA y PRE-DAM referencial",
                "Calcula la deuda tributaria estimada y genera el PRE-DAM",
                7, "MEDIUM", null,
                List.of("tipoCambio")
            );
        }

        return new NextActionDTO(
            "Revisa y confirma el expediente completo",
            "Todos los pasos estÃƒÂ¡n completos, realiza la revisiÃƒÂ³n final",
            8, "LOW", null,
            List.of()
        );
    }

    private static boolean isPasoCompleto(int expedienteId, int paso) {
        String estado = GuidedFlowService.getPasoEstado(expedienteId, paso);
        return "COMPLETO".equals(estado);
    }

    private String verificarBloqueoCoherencia(Importacion imp) {
        if (imp == null) return "OperaciÃƒÂ³n no encontrada";
        if (imp.getHsCode() != null && NormalizadorUtil.looksRestricted(imp.getHsCode())) {
            return "MercancÃƒÂ­a restringida detectada: se requiere permiso sectorial";
        }
        return null;
    }
}


