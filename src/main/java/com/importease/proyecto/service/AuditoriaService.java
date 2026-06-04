package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio Central de AuditorÃ­a y Control de Eventos.
 * Registra todas las actividades crÃ­ticas en la tabla auditoria_eventos.
 */
public class AuditoriaService {

    public static void registrar(Integer usuarioId, String accion, String modulo, Integer entidadId, String detalle, String ip, String userAgent) {
        String sql = "INSERT INTO auditoria_eventos (usuario_id, accion, modulo, entidad_afectada, entidad_id, detalle, ip_origen, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexionSecundaria();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (usuarioId != null) ps.setInt(1, usuarioId);
            else ps.setNull(1, java.sql.Types.INTEGER);
            
            ps.setString(2, accion);
            ps.setString(3, modulo);
            ps.setString(4, "operaciones");
            
            if (entidadId != null) ps.setInt(5, entidadId);
            else ps.setNull(5, java.sql.Types.INTEGER);
            
            ps.setString(6, detalle != null ? detalle : "");
            ps.setString(7, ip != null ? ip : "");
            ps.setString(8, userAgent != null ? userAgent : "");
            
            ps.executeUpdate();
            LoggerUtil.info("AUDIT LOG -> Usuario: " + usuarioId + " | AcciÃ³n: " + accion + " | Modulo: " + modulo + " | Detalle: " + detalle);
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar evento de auditorÃ­a en la base de datos", e);
        }
    }

    /**
     * Obtiene todos los datos agregados y listados requeridos por el panel de auditorÃ­a (auditoria.jsp).
     * Desacopla la lÃ³gica de base de datos de la vista.
     */
    public static Map<String, Object> obtenerDatosAuditoria(int usuarioId) {
        Map<String, Object> datos = new HashMap<>();
        List<Map<String, Object>> operaciones = new ArrayList<>();
        List<Map<String, Object>> permisos = new ArrayList<>();
        List<Map<String, Object>> topPartidas = new ArrayList<>();

        int totalOperaciones = 0;
        double totalFob = 0.0;
        double totalImpuestos = 0.0;
        int canalVerde = 0;
        int canalNaranja = 0;
        int canalRojo = 0;
        int totalAlertas = 0;
        int totalPermisos = 0;

        try (Connection con = ConexionDB.obtenerConexionSecundaria()) {
            // 1. Obtener Operaciones
            String sqlOps = "SELECT * FROM operaciones WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
            try (PreparedStatement ps = con.prepareStatement(sqlOps)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> op = new HashMap<>();
                        op.put("id", rs.getInt("id"));
                        op.put("hs_code", rs.getString("hs_code"));
                        op.put("fob", rs.getDouble("fob"));
                        op.put("cif", rs.getDouble("cif"));
                        op.put("estado", rs.getString("estado"));
                        op.put("canal_asignado", rs.getString("canal_asignado"));
                        op.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                        op.put("total_impuestos", rs.getDouble("total_impuestos"));
                        op.put("documento_factura", rs.getBoolean("documento_factura"));
                        op.put("documento_bl", rs.getBoolean("documento_bl"));
                        
                        String hsc = rs.getString("hs_code");
                        String canal = rs.getString("canal_asignado");
                        
                        String entidad = "SUNAT (Libre)";
                        if (hsc != null) {
                            if (hsc.startsWith("8517") || hsc.startsWith("8525")) entidad = "MTC";
                            else if (hsc.startsWith("2106") || hsc.startsWith("1901")) entidad = "DIGESA";
                            else if (hsc.startsWith("3004") || hsc.startsWith("3304")) entidad = "DIGEMID";
                            else if (hsc.startsWith("0602") || hsc.startsWith("1001")) entidad = "SENASA";
                            else if (hsc.startsWith("4403") || hsc.startsWith("4407")) entidad = "SERFOR";
                            else if (hsc.startsWith("0302") || hsc.startsWith("1604")) entidad = "SANIPES";
                            else if (hsc.startsWith("9303") || hsc.startsWith("3602")) entidad = "SUCAMEC";
                        }
                        op.put("entidad", entidad);
                        
                        totalOperaciones++;
                        totalFob += rs.getDouble("fob");
                        totalImpuestos += rs.getDouble("total_impuestos");
                        
                        if ("VERDE".equalsIgnoreCase(canal)) canalVerde++;
                        else if ("NARANJA".equalsIgnoreCase(canal)) canalNaranja++;
                        else if ("ROJO".equalsIgnoreCase(canal)) canalRojo++;
                        
                        if ("PENDIENTE_DOCS".equalsIgnoreCase(rs.getString("estado")) || 
                            !rs.getBoolean("documento_factura") || !rs.getBoolean("documento_bl")) {
                            totalAlertas++;
                        }
                        operaciones.add(op);
                    }
                }
            }

            // 2. Obtener Permisos VUCE
            String sqlPermisos = "SELECT * FROM solicitudes_permiso WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
            try (PreparedStatement ps = con.prepareStatement(sqlPermisos)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> perm = new HashMap<>();
                        perm.put("id", rs.getInt("id"));
                        perm.put("operacion_id", rs.getInt("operacion_id"));
                        perm.put("codigo_entidad", rs.getString("codigo_entidad"));
                        perm.put("tipo_permiso", rs.getString("tipo_permiso"));
                        perm.put("estado", rs.getString("estado"));
                        perm.put("numero_suce", rs.getString("numero_suce"));
                        perm.put("numero_documento_resolutivo", rs.getString("numero_documento_resolutivo"));
                        perm.put("fecha_creacion", rs.getTimestamp("fecha_creacion"));
                        
                        totalPermisos++;
                        permisos.add(perm);
                    }
                }
            }

            // 3. Obtener Top Partidas Buscadas (log_busquedas)
            String sqlTop = "SELECT resultado_hs_code, count(*) as qty FROM log_busquedas GROUP BY resultado_hs_code ORDER BY qty DESC LIMIT 5";
            try (PreparedStatement ps = con.prepareStatement(sqlTop);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> top = new HashMap<>();
                    top.put("codigo", rs.getString("resultado_hs_code"));
                    top.put("busquedas", rs.getInt("qty"));
                    topPartidas.add(top);
                }
            } catch (Exception e) {
                // Tabla vacÃ­a o sin registros
            }

        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener datos agregados de auditorÃ­a en base de datos", e);
        }

        datos.put("operaciones", operaciones);
        datos.put("permisos", permisos);
        datos.put("topPartidas", topPartidas);
        datos.put("totalOperaciones", totalOperaciones);
        datos.put("totalFob", totalFob);
        datos.put("totalImpuestos", totalImpuestos);
        datos.put("canalVerde", canalVerde);
        datos.put("canalNaranja", canalNaranja);
        datos.put("canalRojo", canalRojo);
        datos.put("totalAlertas", totalAlertas);
        datos.put("totalPermisos", totalPermisos);

        return datos;
    }
}


