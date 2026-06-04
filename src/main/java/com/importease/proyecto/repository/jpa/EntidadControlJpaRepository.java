package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.EntidadControlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntidadControlJpaRepository extends JpaRepository<EntidadControlEntity, Integer> {
    List<EntidadControlEntity> findByActivoTrueOrderByNombreEntidadAsc();
    Optional<EntidadControlEntity> findByCodigoEntidad(String codigoEntidad);
}
