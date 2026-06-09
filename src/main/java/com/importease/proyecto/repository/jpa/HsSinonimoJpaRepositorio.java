package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.HsSinonimoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HsSinonimoJpaRepositorio extends JpaRepository<HsSinonimoEntity, Integer> {

    @Query(value = "SELECT codigo_hs_sugerido FROM hs_sinonimos WHERE termino_usuario = :termino AND activo = TRUE ORDER BY prioridad ASC LIMIT 1",
           nativeQuery = true)
    Optional<String> buscarCodigoSugeridoActivo(@Param("termino") String termino);
}
