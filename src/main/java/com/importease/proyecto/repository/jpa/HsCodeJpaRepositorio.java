package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.HsCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HsCodeJpaRepositorio extends JpaRepository<HsCodeEntity, Integer> {

    Optional<HsCodeEntity> findByCodigo(String codigo);

    List<HsCodeEntity> findAllByOrderByCodigoAsc();

    @Query(value = "SELECT * FROM hs_codes " +
                   "WHERE codigo = :codigoExact OR codigo LIKE :codigoLike OR LOWER(descripcion_es) LIKE :descLike " +
                   "ORDER BY CASE WHEN codigo = :codigoExact THEN 0 WHEN codigo LIKE :codigoLike THEN 1 ELSE 2 END, codigo " +
                   "LIMIT 1", nativeQuery = true)
    List<HsCodeEntity> buscarPrimeroPorDescripcion(@Param("codigoExact") String codigoExact,
                                                   @Param("codigoLike") String codigoLike,
                                                   @Param("descLike") String descLike);

    @Query(value = "SELECT * FROM hs_codes " +
                   "WHERE (codigo LIKE :codeLike OR LOWER(descripcion_es) LIKE :descLike) " +
                   "ORDER BY CASE " +
                   "WHEN codigo = :normalizedCode THEN 0 " +
                   "WHEN codigo LIKE :codeLike THEN 1 " +
                   "WHEN LOWER(descripcion_es) LIKE :descPrefixLike THEN 2 " +
                   "ELSE 3 END, codigo LIMIT 20", nativeQuery = true)
    List<HsCodeEntity> buscarSugerenciasConPrefijo(@Param("codeLike") String codeLike,
                                                   @Param("descLike") String descLike,
                                                   @Param("normalizedCode") String normalizedCode,
                                                   @Param("descPrefixLike") String descPrefixLike);

    @Query(value = "SELECT * FROM hs_codes " +
                   "WHERE LOWER(descripcion_es) LIKE :descLike " +
                   "ORDER BY CASE WHEN LOWER(descripcion_es) LIKE :descPrefixLike THEN 0 ELSE 1 END, codigo LIMIT 20", nativeQuery = true)
    List<HsCodeEntity> buscarSugerenciasPorDescripcion(@Param("descLike") String descLike,
                                                       @Param("descPrefixLike") String descPrefixLike);
}
