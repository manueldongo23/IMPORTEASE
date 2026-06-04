package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio premium de alertas y notificaciones logÃ­sticas crÃ­ticas en tiempo real.
 * Analiza expedientes en curso para prevenir multas, demoras y el Abandono Legal ante SUNAT.
 */
public class NotificacionService {

    /**
     * Genera alertas proactivas basadas en la base de datos para el importador.
     */
    public static List<Map<String, Object>> obtenerNotificacionesCriticas(int usuarioId) {
        List<Map<String, Object>> alertas = new ArrayList<>();

        String sql = "SELECT i.id, i.producto_desc, i.hs_code, i.estado, i.fecha_creacion, " +
                     "i.documento_factura, i.documento_bl, i.documento_certificado_origen, " +
                     "h.requiere_permiso, h.entidad_vuce, COUNT(sp.id) AS permisos_registrados " +
                     "FROM importaciones i " +
                     "LEFT JOIN hs_codes h ON i.hs_code = h.codigo " +
                     "LEFT JOIN solicitudes_permiso sp ON sp.operacion_id = i.id " +
                     "WHERE i.usuario_id = ? AND i.estado NOT IN ('NACIONALIZADA', 'CANCELADA') " +
                     "GROUP BY i.id, i.producto_desc, i.hs_code, i.estado, i.fecha_creacion, " +
                     "i.documento_factura, i.documento_bl, i.documento_certificado_origen, " +
                     "h.requiere_permiso, h.entidad_vuce";

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String desc = rs.getString("producto_desc");
                    String state = rs.getString("estado");
                    java.sql.Timestamp created = rs.getTimestamp("fecha_creacion");
                    boolean hasInvoice = rs.getBoolean("documento_factura");
                    boolean hasBl = rs.getBoolean("documento_bl");
                    boolean reqVuce = rs.getBoolean("requiere_permiso");
                    String entidad = rs.getString("entidad_vuce");

                    // 1. Alerta: Abandono Legal
                    if (created != null) {
                        LocalDateTime base = created.toLocalDateTime();
                        long diasTranscurridos = ChronoUnit.DAYS.between(base, LocalDateTime.now());
                        long diasRestantes = 15 - diasTranscurridos;

                        if (diasRestantes <= 3) {
                            alertas.add(buildAlerta(
                                "CRITICAL", 
                                "Â¡Abandono Legal Inminente! - Op #" + id, 
                                "Faltan " + (diasRestantes < 0 ? 0 : diasRestantes) + " dÃ­as para que la mercancÃ­a caiga en abandono legal. Destine la DAM diferida de inmediato.", 
                                id
                            ));
                        } else if (diasRestantes <= 7) {
                            alertas.add(buildAlerta(
                                "WARNING", 
                                "Plazo de DestinaciÃ³n CrÃ­tico - Op #" + id, 
                                "Han transcurrido " + diasTranscurridos + " dÃ­as. Le quedan " + diasRestantes + " dÃ­as para destinar su mercancÃ­a ante SUNAT.", 
                                id
                            ));
                        }
                    }

                    // 2. Alerta: Permisos VUCE Pendientes
                    if (reqVuce && (entidad != null && !entidad.equalsIgnoreCase("SUNAT"))) {
                        boolean tienePermiso = rs.getInt("permisos_registrados") > 0;
                        if (!tienePermiso) {
                            alertas.add(buildAlerta(
                                "CRITICAL",
                                "TrÃ¡mite VUCE Requerido (" + entidad + ") - Op #" + id,
                                "Su mercancÃ­a (" + desc + ") es restringida y requiere permiso de " + entidad + ". Inicie el trÃ¡mite antes del embarque.",
                                id
                            ));
                        }
                    }

                    // 3. Alerta: DocumentaciÃ³n faltante en el expediente
                    if (!hasInvoice || !hasBl) {
                        List<String> faltantes = new ArrayList<>();
                        if (!hasInvoice) faltantes.add("Factura Comercial");
                        if (!hasBl) faltantes.add("Documento de Transporte (BL/AWB)");

                        alertas.add(buildAlerta(
                            "WARNING",
                            "Expediente Incompleto - Op #" + id,
                            "Faltan documentos obligatorios en la carpeta base: " + String.join(", ", faltantes) + ".",
                            id
                        ));
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Error al calcular notificaciones aduaneras: " + e.getMessage(), e);
        }

        // Si no hay alertas crÃ­ticas, agregamos una notificaciÃ³n de salud excelente del dashboard
        if (alertas.isEmpty()) {
            alertas.add(buildAlerta(
                "INFO",
                "Expediente en Buen Estado",
                "No se registran alertas de abandono legal ni bloqueos aduaneros en sus operaciones vigentes.",
                0
            ));
        }

        return alertas;
    }

    private static Map<String, Object> buildAlerta(String tipo, String titulo, String mensaje, int operacionId) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("tipo", tipo); // INFO, WARNING, CRITICAL
        alert.put("titulo", titulo);
        alert.put("mensaje", mensaje);
        alert.put("operacionId", operacionId);
        alert.put("fecha", LocalDateTime.now().toString());
        return alert;
    }
}


