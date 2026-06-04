package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;

import java.util.LinkedHashMap;
import java.util.Map;

public class OperacionAduaneraMapper {
    public Map<String, Object> toMap(Importacion imp) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", imp.getId());
        map.put("productoDesc", imp.getProductoDesc());
        map.put("hsCode", imp.getHsCode());
        map.put("paisOrigen", imp.getPaisOrigen());
        map.put("incoterm", imp.getIncoterm());
        map.put("fob", imp.getValorFob());
        map.put("flete", imp.getFlete());
        map.put("seguro", imp.getSeguro());
        map.put("cif", imp.getValorCif());
        map.put("estado", imp.getEstado());
        map.put("numeroDam", imp.getNumeroDam());
        map.put("canalAsignado", imp.getCanalAsignado());
        return map;
    }
}
