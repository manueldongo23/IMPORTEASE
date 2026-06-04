package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.TrackingEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrackingEventoJpaRepository extends JpaRepository<TrackingEventoEntity, Long> {

    @Query(value = "SELECT e.* FROM tracking_eventos e " +
                   "JOIN tracking_envios t ON t.id = e.tracking_id " +
                   "WHERE e.tracking_id = :trackingId AND t.usuario_id = :usuarioId " +
                   "ORDER BY e.fecha_evento ASC, e.id ASC", nativeQuery = true)
    List<TrackingEventoEntity> listarPorTrackingYUsuario(@Param("trackingId") Long trackingId,
                                                         @Param("usuarioId") Integer usuarioId);
}
