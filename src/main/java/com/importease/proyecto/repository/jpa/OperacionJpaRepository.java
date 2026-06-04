package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.OperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OperacionJpaRepository extends JpaRepository<OperacionEntity, Integer> {
    List<OperacionEntity> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    @Query("SELECT COALESCE(SUM(o.totalImpuestos), 0), COALESCE(SUM(o.fob), 0), COUNT(o), " +
           "COALESCE(SUM(CASE WHEN o.canalAsignado = 'VERDE' THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN o.canalAsignado = 'NARANJA' THEN 1 ELSE 0 END), 0), " +
           "COALESCE(SUM(CASE WHEN o.canalAsignado = 'ROJO' THEN 1 ELSE 0 END), 0) " +
           "FROM OperacionEntity o WHERE o.usuarioId = :usuarioId")
    Object[] obtenerEstadisticas(@Param("usuarioId") Integer usuarioId);
}
