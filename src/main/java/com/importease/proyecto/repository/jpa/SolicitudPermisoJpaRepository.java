package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.SolicitudPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface SolicitudPermisoJpaRepository extends JpaRepository<SolicitudPermisoEntity, Integer> {
    List<SolicitudPermisoEntity> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    List<SolicitudPermisoEntity> findByOperacionIdOrderByFechaCreacionDesc(Integer operacionId);

    @Transactional
    @Modifying
    @Query("UPDATE SolicitudPermisoEntity s SET s.estado = :estado WHERE s.id = :id")
    int actualizarEstado(@Param("id") Integer id, @Param("estado") String estado);
}
