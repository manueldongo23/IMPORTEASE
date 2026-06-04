package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.TrackingEnvioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TrackingEnvioJpaRepository extends JpaRepository<TrackingEnvioEntity, Long> {
    List<TrackingEnvioEntity> findTop50ByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);
    Optional<TrackingEnvioEntity> findByIdAndUsuarioId(Long id, Integer usuarioId);

    @Transactional
    @Modifying
    @Query("UPDATE TrackingEnvioEntity t SET t.estadoActual = :estado, t.sourceType = :sourceType, t.confidence = :confidence, t.ultimaActualizacion = CURRENT_TIMESTAMP WHERE t.id = :id AND t.usuarioId = :usuarioId")
    int actualizarEstado(@Param("id") Long id,
                         @Param("usuarioId") Integer usuarioId,
                         @Param("estado") String estado,
                         @Param("sourceType") String sourceType,
                         @Param("confidence") BigDecimal confidence);
}
