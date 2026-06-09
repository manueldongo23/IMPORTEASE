package com.importease.proyecto.repository.permisos;

import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.jpa.DocumentoPermisoEntity;
import com.importease.proyecto.repository.jpa.DocumentoPermisoJpaRepositorio;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermisoDocumentoRepositorio extends PermisoRepositorioSoporte {

    public List<DocumentoPermiso> obtenerDocumentosPorPermiso(String codigoEntidad, String tipoPermiso) {
        DocumentoPermisoJpaRepositorio repo = getDocumentoRepo();
        if (repo != null) {
            try {
                List<DocumentoPermiso> out = new ArrayList<>();
                for (DocumentoPermisoEntity entity : repo.findByCodigoEntidadAndTipoPermiso(codigoEntidad, tipoPermiso)) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerDocumentosPorPermiso con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<DocumentoPermiso> lista = new ArrayList<>();
        String sql = "SELECT * FROM documentos_permiso WHERE codigo_entidad = ? AND tipo_permiso = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoEntidad);
            ps.setString(2, tipoPermiso);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearDocumento(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener documentos por permiso", e);
        }
        return lista;
    }
}
