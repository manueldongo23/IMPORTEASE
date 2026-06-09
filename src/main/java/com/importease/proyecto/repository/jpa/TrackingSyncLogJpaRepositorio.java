package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.TrackingSyncLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingSyncLogJpaRepositorio extends JpaRepository<TrackingSyncLogEntity, Long> {
}
