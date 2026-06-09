package com.importease.proyecto.repository;

import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositorio de base de datos para la entidad de Incoterm.
 */
public class IncotermRepositorio {
    public List<Map<String, Object>> listar() {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT codigo, nombre, modalidad, descripcion_resumida, " +
                "incluye_flete_internacional, incluye_seguro_internacional " +
                "FROM incoterms_2020 ORDER BY FIELD(codigo,'EXW','FCA','FOB','CFR','CIF','CPT','CIP','DAP','DDP'), codigo";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(row(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.warn("incoterms_2020 no disponible, usando catalogo fallback: " + e.getMessage());
        }
        return items.isEmpty() ? fallback() : items;
    }

    public Map<String, Object> obtener(String codigo) {
        String normalized = codigo == null ? "FOB" : codigo.trim().toUpperCase();
        return listar().stream()
                .filter(item -> normalized.equals(item.get("codigo")))
                .findFirst()
                .orElseGet(() -> listar().stream().filter(item -> "FOB".equals(item.get("codigo"))).findFirst().orElse(fallback().get(2)));
    }

    private Map<String, Object> row(ResultSet rs) throws SQLException {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("codigo", rs.getString("codigo"));
        item.put("nombre", rs.getString("nombre"));
        item.put("modalidad", rs.getString("modalidad"));
        item.put("descripcion", rs.getString("descripcion_resumida"));
        item.put("incluyeFlete", rs.getBoolean("incluye_flete_internacional"));
        item.put("incluyeSeguro", rs.getBoolean("incluye_seguro_internacional"));
        return item;
    }

    private List<Map<String, Object>> fallback() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item("EXW", "Ex Works", "MULTIMODAL", "Retiro en almacen del vendedor; el comprador asume casi todo.", false, false));
        items.add(item("FCA", "Free Carrier", "MULTIMODAL", "Entrega al transportista designado.", false, false));
        items.add(item("FOB", "Free On Board", "MARITIMO", "Entrega a bordo en puerto de embarque; comun para carga maritima.", false, false));
        items.add(item("CFR", "Cost and Freight", "MARITIMO", "Vendedor paga flete hasta destino; seguro no incluido.", true, false));
        items.add(item("CIF", "Cost, Insurance and Freight", "MARITIMO", "Vendedor paga costo, seguro y flete hasta destino.", true, true));
        items.add(item("CPT", "Carriage Paid To", "MULTIMODAL", "Vendedor paga transporte hasta destino pactado; seguro no incluido.", true, false));
        items.add(item("CIP", "Carriage and Insurance Paid To", "MULTIMODAL", "Vendedor paga transporte y seguro hasta destino pactado.", true, true));
        items.add(item("DAP", "Delivered At Place", "MULTIMODAL", "Entrega en lugar convenido sin despacho de importacion.", true, false));
        items.add(item("DDP", "Delivered Duty Paid", "MULTIMODAL", "Promete entrega con tributos pagados; requiere validar si aplica en Peru.", true, false));
        return items;
    }

    private Map<String, Object> item(String codigo, String nombre, String modalidad, String descripcion, boolean flete, boolean seguro) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("codigo", codigo);
        item.put("nombre", nombre);
        item.put("modalidad", modalidad);
        item.put("descripcion", descripcion);
        item.put("incluyeFlete", flete);
        item.put("incluyeSeguro", seguro);
        return item;
    }
}
