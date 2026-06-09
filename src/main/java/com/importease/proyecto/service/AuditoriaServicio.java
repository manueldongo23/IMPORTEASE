package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.importease.proyecto.dto.AuditoriaDTO;
import com.importease.proyecto.dto.AuditoriaDTO.IncidenciaDTO;
import com.importease.proyecto.dto.AuditoriaDTO.OperacionAuditoriaDTO;
import com.importease.proyecto.dto.AuditoriaDTO.PartidaAuditoriaDTO;
import com.importease.proyecto.dto.AuditoriaDTO.PermisoAuditoriaDTO;

/**
 * Servicio Central de AuditorÃ­a y Control de Eventos.
 * Registra todas las actividades crÃ­ticas en la tabla auditoria_eventos.
 */
public class AuditoriaServicio {

    private static String sanitizeUserAgent(String ua) {
        if (ua == null) return "";
        String cleaned = ua.replaceAll("[\\r\\n\\t]+", " ").replaceAll("[^\\x20-\\x7E]", "");
        return cleaned.substring(0, Math.min(cleaned.length(), 500));
    }

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
            ps.setString(8, sanitizeUserAgent(userAgent));
            
            ps.executeUpdate();
            LoggerUtil.info("AUDIT LOG -> Usuario: " + usuarioId + " | AcciÃ³n: " + accion + " | Modulo: " + modulo + " | Detalle: " + detalle);
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar evento de auditorÃ­a en la base de datos", e);
        }
    }

    /**
     * Obtiene todos los datos agregados y listados requeridos por el panel de auditoría (auditoria.jsp).
     * Desacopla la lógica de base de datos de la vista.
     */
    public static AuditoriaDTO obtenerDatosAuditoria(int usuarioId, String userRuc) {
        AuditoriaDTO dto = new AuditoriaDTO();

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
            String sqlOps = "SELECT * FROM operaciones WHERE usuario_id = ? ORDER BY fecha_creacion DESC LIMIT 200";
            try (PreparedStatement ps = con.prepareStatement(sqlOps)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        OperacionAuditoriaDTO op = new OperacionAuditoriaDTO();
                        op.setId(rs.getInt("id"));
                        op.setHsCode(rs.getString("hs_code"));
                        op.setFob(rs.getDouble("fob"));
                        op.setCif(rs.getDouble("cif"));
                        op.setEstado(rs.getString("estado"));
                        op.setCanalAsignado(rs.getString("canal_asignado"));
                        op.setFechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toString() : "");
                        op.setTotalImpuestos(rs.getDouble("total_impuestos"));
                        op.setDocumentoFactura(rs.getBoolean("documento_factura"));
                        op.setDocumentoBl(rs.getBoolean("documento_bl"));
                        op.setProductoDesc(rs.getString("producto_desc"));
                        op.setPaisOrigen(rs.getString("pais_origen"));
                        
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
                        op.setEntidad(entidad);
                        
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
                        dto.getOperaciones().add(op);
                    }
                }
            }

            // 2. Obtener Permisos VUCE
            String sqlPermisos = "SELECT * FROM solicitudes_permiso WHERE usuario_id = ? ORDER BY fecha_creacion DESC LIMIT 200";
            try (PreparedStatement ps = con.prepareStatement(sqlPermisos)) {
                ps.setInt(1, usuarioId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        PermisoAuditoriaDTO perm = new PermisoAuditoriaDTO();
                        perm.setId(rs.getInt("id"));
                        perm.setOperacionId(rs.getInt("operacion_id"));
                        perm.setCodigoEntidad(rs.getString("codigo_entidad"));
                        perm.setTipoPermiso(rs.getString("tipo_permiso"));
                        perm.setEstado(rs.getString("estado"));
                        perm.setNumeroSuce(rs.getString("numero_suce"));
                        perm.setNumeroDocumentoResolutivo(rs.getString("numero_documento_resolutivo"));
                        perm.setFechaCreacion(rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toString() : "");
                        
                        totalPermisos++;
                        dto.getPermisos().add(perm);
                    }
                }
            }

            // 3. Obtener Top Partidas Buscadas (log_busquedas)
            String sqlTop = "SELECT resultado_hs_code, count(*) as qty FROM log_busquedas GROUP BY resultado_hs_code ORDER BY qty DESC LIMIT 5";
            try (PreparedStatement ps = con.prepareStatement(sqlTop);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PartidaAuditoriaDTO top = new PartidaAuditoriaDTO();
                    top.setCodigo(rs.getString("resultado_hs_code"));
                    top.setBusquedas(rs.getInt("qty"));
                    dto.getTopPartidas().add(top);
                }
            } catch (Exception e) {
                // Tabla vacía o sin registros
            }

        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener datos agregados de auditoría en base de datos", e);
        }

        // Simular logs
        dto.getLogsIncidencias().add(new IncidenciaDTO("Clasificación HS", "MTC homologación celulares Wi-Fi/Bluetooth validada para partida 8517.13.00.00", "AUTOMÁTICO"));
        dto.getLogsIncidencias().add(new IncidenciaDTO("Regla VUCE", "Alerta DIGESA activada para partida 2106.90.99.00 (Suplementos nutricionales)", "REGLA_ESTABLECIDA"));
        dto.getLogsIncidencias().add(new IncidenciaDTO("Control Fiscal", "Percepción del IGV SUNAT ajustada al 3.5% para RUC recurrente " + (userRuc != null ? userRuc : "No.RUC"), "SISTEMA"));
        dto.getLogsIncidencias().add(new IncidenciaDTO("Criptografía PDF", "DAM firmada digitalmente con clave SHA-256 en formato regulatorio", "SEGURIDAD"));

        dto.setTotalOperaciones(totalOperaciones);
        dto.setTotalFob(totalFob);
        dto.setTotalImpuestos(totalImpuestos);
        dto.setCanalVerde(canalVerde);
        dto.setCanalNaranja(canalNaranja);
        dto.setCanalRojo(canalRojo);
        dto.setTotalAlertas(totalAlertas);
        dto.setTotalPermisos(totalPermisos);

        return dto;
    }
}
