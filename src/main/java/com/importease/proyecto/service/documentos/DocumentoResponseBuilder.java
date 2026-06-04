package com.importease.proyecto.service.documentos;

import java.util.HashMap;
import java.util.Map;

public class DocumentoResponseBuilder {

    public Map<String, Object> uploadSuccess(String relativePath, String originalName, String checksumHash) {
        Map<String, Object> successMap = new HashMap<>();
        successMap.put("mensaje", "Archivo cargado exitosamente");
        successMap.put("ruta", relativePath);
        successMap.put("nombre", originalName);
        successMap.put("checksum_hash", checksumHash);
        return successMap;
    }
}
