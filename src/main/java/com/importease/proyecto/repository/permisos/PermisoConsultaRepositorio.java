package com.importease.proyecto.repository.permisos;

import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.ReglaRestriccion;
import com.importease.proyecto.model.jpa.EntidadControlEntity;
import com.importease.proyecto.model.jpa.PreguntaPermisoEntity;
import com.importease.proyecto.model.jpa.ReglaRestriccionEntity;
import com.importease.proyecto.repository.jpa.EntidadControlJpaRepositorio;
import com.importease.proyecto.repository.jpa.PreguntaPermisoJpaRepositorio;
import com.importease.proyecto.repository.jpa.ReglaRestriccionJpaRepositorio;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermisoConsultaRepositorio extends PermisoRepositorioSoporte {

    public List<EntidadControl> listarEntidades() {
        EntidadControlJpaRepositorio repo = getEntidadRepo();
        if (repo != null) {
            try {
                List<EntidadControl> out = new ArrayList<>();
                for (EntidadControlEntity entity : repo.findByActivoTrueOrderByNombreEntidadAsc()) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarEntidades con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<EntidadControl> lista = new ArrayList<>();
        String sql = "SELECT * FROM entidades_control WHERE activo = TRUE";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearEntidad(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar entidades de control", e);
        }
        return lista;
    }

    public EntidadControl obtenerEntidad(String codigoEntidad) {
        EntidadControlJpaRepositorio repo = getEntidadRepo();
        if (repo != null) {
            try {
                Optional<EntidadControlEntity> maybe = repo.findByCodigoEntidad(codigoEntidad);
                if (maybe.isPresent()) return toModel(maybe.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerEntidad con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM entidades_control WHERE codigo_entidad = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoEntidad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearEntidad(rs);
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener entidad por codigo", e);
        }
        return null;
    }

    public List<ReglaRestriccion> buscarReglasPorHsCode(String hsCode) {
        List<ReglaRestriccion> lista = new ArrayList<>();
        if (hsCode == null || hsCode.length() < 4) return lista;
        int capituloHs = Integer.parseInt(hsCode.substring(0, 2));
        String partidaHs = hsCode.substring(0, 4);

        ReglaRestriccionJpaRepositorio repo = getReglaRepo();
        if (repo != null) {
            try {
                List<ReglaRestriccion> out = new ArrayList<>();
                for (ReglaRestriccionEntity entity : repo.buscarActivasPorCapituloOpartida(capituloHs, partidaHs)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarReglasPorHsCode con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM reglas_restriccion WHERE (capitulo_hs = ? OR partida_hs = ?) AND activo = TRUE";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, capituloHs);
            ps.setString(2, partidaHs);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearRegla(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar reglas por HS code", e);
        }
        return lista;
    }

    public List<ReglaRestriccion> buscarReglasPorPalabraClave(String descripcion) {
        List<ReglaRestriccion> lista = new ArrayList<>();
        if (descripcion == null || descripcion.trim().isEmpty()) return lista;

        ReglaRestriccionJpaRepositorio repo = getReglaRepo();
        if (repo != null) {
            try {
                List<ReglaRestriccion> out = new ArrayList<>();
                for (ReglaRestriccionEntity entity : repo.buscarActivasPorPalabraClave(descripcion.toUpperCase())) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarReglasPorPalabraClave con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM reglas_restriccion WHERE palabra_clave IS NOT NULL AND ? LIKE CONCAT('%', palabra_clave, '%') AND activo = TRUE";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, descripcion.toUpperCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearRegla(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar reglas por palabra clave", e);
        }
        return lista;
    }

    public List<PreguntaPermiso> obtenerPreguntasPorEntidad(String codigoEntidad) {
        PreguntaPermisoJpaRepositorio repo = getPreguntaRepo();
        if (repo != null) {
            try {
                List<PreguntaPermiso> out = new ArrayList<>();
                for (PreguntaPermisoEntity entity : repo.findByCodigoEntidadAndActivoTrueOrderByOrdenAsc(codigoEntidad)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerPreguntasPorEntidad con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<PreguntaPermiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM preguntas_permiso WHERE codigo_entidad = ? AND activo = TRUE ORDER BY orden";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoEntidad);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearPregunta(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener preguntas por entidad", e);
        }
        return lista;
    }
}
