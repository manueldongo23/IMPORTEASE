package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.DocumentoPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoPermisoJpaRepositorio extends JpaRepository<DocumentoPermisoEntity, Integer> {
    List<DocumentoPermisoEntity> findByCodigoEntidadAndTipoPermiso(String codigoEntidad, String tipoPermiso);
}
