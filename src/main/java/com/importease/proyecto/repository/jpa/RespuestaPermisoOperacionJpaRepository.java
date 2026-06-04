package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.RespuestaPermisoOperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface RespuestaPermisoOperacionJpaRepository extends JpaRepository<RespuestaPermisoOperacionEntity, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM RespuestaPermisoOperacionEntity r WHERE r.operacionId = :operacionId AND r.preguntaId = :preguntaId")
    int deleteByOperacionIdAndPreguntaId(@Param("operacionId") Integer operacionId, @Param("preguntaId") Integer preguntaId);
}
