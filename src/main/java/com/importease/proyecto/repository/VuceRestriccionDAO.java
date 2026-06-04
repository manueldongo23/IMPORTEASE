/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.importease.proyecto.repository;

import com.importease.proyecto.model.VuceRestriccion;
import com.importease.proyecto.service.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.importease.proyecto.repository.IVuceRestriccionDAO;
import com.importease.proyecto.service.LoggerUtil;


public class VuceRestriccionDAO implements IVuceRestriccionDAO{

    public VuceRestriccion obtenerPorEntidad(String entidad) {
        String sql = "SELECT * FROM vuce_restricciones WHERE entidad = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearVuce(rs);
        } catch (SQLException e) { LoggerUtil.error("Error al obtener restricciÃ³n VUCE por entidad", e); }
        return null;
    }

    public List<VuceRestriccion> listarTodas() {
        List<VuceRestriccion> lista = new ArrayList<>();
        String sql = "SELECT * FROM vuce_restricciones";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearVuce(rs));
        } catch (SQLException e) { LoggerUtil.error("Error al listar restricciones VUCE", e); }
        return lista;
    }

    private VuceRestriccion mapearVuce(ResultSet rs) throws SQLException {
        VuceRestriccion vr = new VuceRestriccion();
        vr.setId(rs.getInt("id"));
        vr.setEntidad(rs.getString("entidad"));
        vr.setTipoControl(rs.getString("tipo_control"));
        vr.setDescripcion(rs.getString("descripcion"));
        vr.setProductosEjemplo(rs.getString("productos_ejemplo"));
        vr.setRequiereRegistroSanitario(rs.getBoolean("requiere_registro_sanitario"));
        vr.setTiempoEstimadoDias(rs.getInt("tiempo_estimado_dias"));
        vr.setEnlaceTupa(rs.getString("enlace_tupa"));
        return vr;
    }
}

