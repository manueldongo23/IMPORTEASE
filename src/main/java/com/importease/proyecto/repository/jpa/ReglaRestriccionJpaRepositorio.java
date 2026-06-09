package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.ReglaRestriccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReglaRestriccionJpaRepositorio extends JpaRepository<ReglaRestriccionEntity, Integer> {

    @Query("SELECT r FROM ReglaRestriccionEntity r WHERE r.activo = true AND (r.capituloHs = :capitulo OR r.partidaHs = :partida)")
    List<ReglaRestriccionEntity> buscarActivasPorCapituloOpartida(@Param("capitulo") Integer capitulo, @Param("partida") String partida);

    @Query("SELECT r FROM ReglaRestriccionEntity r WHERE r.activo = true AND r.palabraClave IS NOT NULL AND UPPER(:descripcion) LIKE CONCAT('%', UPPER(r.palabraClave), '%')")
    List<ReglaRestriccionEntity> buscarActivasPorPalabraClave(@Param("descripcion") String descripcion);
}
