package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer> {
    Optional<UsuarioEntity> findByEmail(String email);
    Optional<UsuarioEntity> findByRuc(String ruc);
    List<UsuarioEntity> findTop100ByOrderByFechaRegistroDesc();
}
