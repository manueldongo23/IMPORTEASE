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

    @Query(value = "SELECT codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id FROM hs_codes " +
                   "WHERE codigo = :codigoExact OR codigo LIKE :codigoLike OR MATCH(descripcion_es) AGAINST(:matchTerm IN BOOLEAN MODE) " +
                   "ORDER BY CASE WHEN codigo = :codigoExact THEN 0 WHEN codigo LIKE :codigoLike THEN 1 ELSE 2 END, codigo " +
                   "LIMIT 1", nativeQuery = true)
    List<HsCodeEntity> buscarPrimeroPorDescripcion(@Param("codigoExact") String codigoExact,
                                                   @Param("codigoLike") String codigoLike,
                                                   @Param("matchTerm") String matchTerm);

    @Query(value = "SELECT codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id FROM hs_codes " +
                   "WHERE (codigo LIKE :codeLike OR MATCH(descripcion_es) AGAINST(:matchTerm IN BOOLEAN MODE)) " +
                   "ORDER BY CASE " +
                   "WHEN codigo = :normalizedCode THEN 0 " +
                   "WHEN codigo LIKE :codeLike THEN 1 " +
                   "WHEN MATCH(descripcion_es) AGAINST(:matchTerm IN BOOLEAN MODE) THEN 2 " +
                   "ELSE 3 END, codigo LIMIT 20", nativeQuery = true)
    List<HsCodeEntity> buscarSugerenciasConPrefijo(@Param("codeLike") String codeLike,
                                                   @Param("matchTerm") String matchTerm,
                                                   @Param("normalizedCode") String normalizedCode);

    @Query(value = "SELECT codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id FROM hs_codes " +
                   "WHERE MATCH(descripcion_es) AGAINST(:matchTerm IN BOOLEAN MODE) " +
                   "ORDER BY codigo LIMIT 20", nativeQuery = true)
    List<HsCodeEntity> buscarSugerenciasPorDescripcion(@Param("matchTerm") String matchTerm);
}
