package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.service.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AduanasSimuladorLineaTiempo {

    private final AduanasConstructorRespuesta responseBuilder = new AduanasConstructorRespuesta();
    private final PredamMapeador predamMapeador = new PredamMapeador();

    public List<Map<String, Object>> buildTimeline(Connection con, Importacion imp, String modalidad) {
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

    public List<Map<String, Object>> buildTimeline(Importacion imp, String modalidad) {
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

    private Map<String, Object> event(String codigo, String nombre, LocalDateTime fecha, String responsable, String documento, String efecto, String observacion) {
        return responseBuilder.event(codigo, nombre, fecha, responsable, documento, efecto, observacion);
    }

    private String predamNumber(Importacion imp) {
        return predamMapeador.predamNumber(imp);
    }

    private LocalDateTime toLocal(Timestamp ts) {
        if (ts == null) return LocalDateTime.now(ZoneId.of("America/Lima"));
        return ts.toLocalDateTime();
    }
}
