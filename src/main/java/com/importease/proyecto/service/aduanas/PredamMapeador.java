package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.service.FuenteMetadataBuilder;
import com.importease.proyecto.service.NormalizadorUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

public class PredamMapeador {
    public Map<String, Object> toMap(Importacion imp, Map<String, Object> regimen, Map<String, Object> modalidad) {
        Map<String, Object> map = FuenteMetadataBuilder.buildMetadata(
                "SIMULACION_ACADEMICA",
                FuenteMetadataBuilder.TYPE_SIMULATED,
                0.20,
                "PRE-DAM referencial generada. No es una DAM oficial SUNAT."
        );
        map.put("numeroDam", predamNumber(imp));
        map.put("regimen", regimen.get("regimenCodigo"));
        map.put("modalidad", modalidad.get("modalidadCodigo"));
        map.put("aduana", "118");
        map.put("canalProbable", canalProbable(String.valueOf(regimen.get("regimenCodigo")), imp));
        map.put("canalEsOficial", false);
        map.put("estado", "PRE-DAM referencial");
        return map;
    }

    public String predamNumber(Importacion imp) {
        if (imp.getNumeroDam() != null && !imp.getNumeroDam().isBlank()) {
            return imp.getNumeroDam();
        }
        int seed = Math.abs((imp.getId() * 7919 + new Random(imp.getId()).nextInt(999999)) % 999999);
        return "PRE-DAM-118-" + LocalDate.now().getYear() + "-10-" + String.format("%06d", seed);
    }

    public String canalProbable(String regimen, Importacion imp) {
        if ("36".equals(regimen)) return "ROJO";
        if (NormalizadorUtil.looksRestricted(imp.getHsCode())) return "ROJO";
        if (imp.getValorCifBD().compareTo(new BigDecimal("10000")) > 0) return "NARANJA";
        return "VERDE";
    }
}
