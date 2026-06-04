package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.PreguntaPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreguntaPermisoJpaRepository extends JpaRepository<PreguntaPermisoEntity, Integer> {
    List<PreguntaPermisoEntity> findByCodigoEntidadAndActivoTrueOrderByOrdenAsc(String codigoEntidad);
}
