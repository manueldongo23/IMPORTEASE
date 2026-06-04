package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AduanasLegalService {
    private final AduanasProcedureCatalog procedureCatalog;
    private final AduanasResponseBuilder responseBuilder;

    public AduanasLegalService() {
        this(new AduanasProcedureCatalog(), new AduanasResponseBuilder());
    }

    public AduanasLegalService(AduanasProcedureCatalog procedureCatalog, AduanasResponseBuilder responseBuilder) {
        this.procedureCatalog = procedureCatalog;
        this.responseBuilder = responseBuilder;
    }

    public List<Map<String, Object>> obtenerBaseLegal(Connection con, Importacion imp) {
        String destino = "PERU";
        String sqlDam = "SELECT regimen_codigo FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlDam)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) destino = traducirDestino(rs.getString("regimen_codigo"));
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al buscar DAM en base legal: " + e.getMessage());
        }
        return buildBaseLegal(resolveRegimenCodigo(destino), imp);
    }

    public List<Map<String, Object>> buildBaseLegal(String regimen, Importacion imp) {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(responseBuilder.legal("Regimen", procedureCatalog.procedimiento(regimen), "Seleccion de regimen", "El sistema traduce respuestas simples a regimen aduanero.", "Cumple", "Validar con agente si el caso real es complejo."));
        list.add(responseBuilder.legal("Modalidad", "DESPA-PG.01", "Modalidad de despacho", "Anticipado como regla general referencial y excepciones academicas.", "Referencial", "Revisar excepciones: FOB <= 2000, urgencia o mercancia restringida."));
        list.add(responseBuilder.legal("Documentos", procedureCatalog.procedimiento(regimen), "Documentos sustentatorios", "Factura, transporte, seguro, origen y permisos segun caso.", "Pendiente", "Completar checklist antes del levante."));
        list.add(responseBuilder.legal("Canal", procedureCatalog.procedimiento(regimen), "Riesgo estimado", "Canal probable, no oficial SUNAT.", "Estimado", "Usar como orientacion academica."));
        if (looksRestricted(imp.getHsCode())) {
            list.add(responseBuilder.legal("Restricciones", "VUCE/SUNAT referencial", "Mercancia restringida", "El HS activa control sectorial probable.", "Requiere accion", "Preparar permiso sectorial."));
        }
        return list;
    }

    private String traducirDestino(String cod) {
        if ("10".equals(cod)) return "CONSUMO";
        if ("36".equals(cod)) return "REIMPORTACION";
        if ("80".equals(cod)) return "TRANSITO";
        if ("ADM_TEMP".equals(cod)) return "TEMPORAL";
        if ("TRANSBORDO".equals(cod)) return "TRANSBORDO";
        return "PERU";
    }

    private String resolveRegimenCodigo(String destino) {
        if ("CONSUMO".equals(destino)) return "10";
        if ("REIMPORTACION".equals(destino)) return "36";
        if ("TRANSITO".equals(destino)) return "80";
        if ("TEMPORAL".equals(destino)) return "ADM_TEMP";
        if ("TRANSBORDO".equals(destino)) return "TRANSBORDO";
        return "10";
    }

    private boolean looksRestricted(String hsCode) {
        return hsCode != null && (hsCode.startsWith("02") || hsCode.startsWith("29") || hsCode.startsWith("30") || hsCode.startsWith("87"));
    }
}
