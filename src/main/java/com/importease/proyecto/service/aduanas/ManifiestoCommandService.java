package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.Importacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class ManifiestoCommandService {

    public long registrarManifiesto(Connection con, Importacion imp, Map<String, Object> body) throws Exception {
        String sql = "INSERT INTO manifiestos_carga (operacion_id, numero_manifiesto, tipo, via_transporte, fecha_llegada, fecha_termino_descarga, puerto_arribo, aduana_codigo, deposito_temporal, estado, fuente, source_type, confidence) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'REFERENCIAL', 'MANUAL_REFERENCIAL', 'MANUAL', 0.50)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, imp.getId());
            ps.setString(2, safeText(str(body.get("numeroManifiesto")), "MAN-" + imp.getId() + "-" + System.currentTimeMillis()));
            ps.setString(3, safeText(str(body.get("tipo")), "CARGA"));
            ps.setString(4, safeText(str(body.get("viaTransporte")), "MARITIMA"));
            ps.setTimestamp(5, tsOrNull(str(body.get("fechaLlegada"))));
            ps.setTimestamp(6, tsOrNull(str(body.get("fechaTerminoDescarga"))));
            ps.setString(7, safeText(str(body.get("puertoArribo")), "Callao"));
            ps.setString(8, safeText(str(body.get("aduanaCodigo")), "118"));
            ps.setString(9, safeText(str(body.get("depositoTemporal")), "Deposito temporal por confirmar"));
            ps.executeUpdate();
            long manifiestoId = 0;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) manifiestoId = rs.getLong(1);
            }
            insertDocumentoTransporte(con, manifiestoId, body);
            return manifiestoId;
        }
    }

    private void insertDocumentoTransporte(Connection con, long manifiestoId, Map<String, Object> body) throws Exception {
        String sql = "INSERT INTO documentos_transporte (manifiesto_id, tipo_documento, numero_documento, master_house, fecha_embarque, puerto_origen, puerto_destino, peso_bruto, bultos) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, manifiestoId);
            ps.setString(2, safeText(str(body.get("tipoDocumento")), "BL"));
            ps.setString(3, safeText(str(body.get("numeroDocumento")), "BL-REFERENCIAL"));
            ps.setString(4, safeText(str(body.get("masterHouse")), "HOUSE"));
            ps.setTimestamp(5, tsOrNull(str(body.get("fechaEmbarque"))));
            ps.setString(6, safeText(str(body.get("puertoOrigen")), "Por confirmar"));
            ps.setString(7, safeText(str(body.get("puertoDestino")), "Callao"));
            ps.setDouble(8, asDouble(body.get("pesoBruto"), 0));
            ps.setInt(9, asInt(body.get("bultos"), 1));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) insertContenedor(con, rs.getLong(1), body);
            }
        }
    }

    private void insertContenedor(Connection con, long docId, Map<String, Object> body) throws Exception {
        String numero = str(body.get("numeroContenedor"));
        if (numero == null || numero.isBlank()) return;
        String sql = "INSERT INTO contenedores (documento_transporte_id, numero_contenedor, tipo_contenedor, precinto_origen, estado_precinto, peso_manifestado, peso_recibido) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, docId);
            ps.setString(2, numero);
            ps.setString(3, safeText(str(body.get("tipoContenedor")), "20ST"));
            ps.setString(4, str(body.get("precintoOrigen")));
            ps.setString(5, safeText(str(body.get("estadoPrecinto")), "NO_VERIFICADO"));
            ps.setDouble(6, asDouble(body.get("pesoManifestado"), 0));
            ps.setDouble(7, asDouble(body.get("pesoRecibido"), 0));
            ps.executeUpdate();
        }
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private int asInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double asDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Timestamp tsOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Timestamp.valueOf(value.replace("T", " ") + (value.length() == 16 ? ":00" : ""));
        } catch (Exception e) {
            try {
                return Timestamp.valueOf(LocalDateTime.parse(value));
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
