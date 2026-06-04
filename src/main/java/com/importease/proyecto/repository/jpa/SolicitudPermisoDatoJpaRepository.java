package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.SolicitudPermisoDatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface SolicitudPermisoDatoJpaRepository extends JpaRepository<SolicitudPermisoDatoEntity, Integer> {
    List<SolicitudPermisoDatoEntity> findBySolicitudPermisoId(Integer solicitudPermisoId);

    @Transactional
    void deleteBySolicitudPermisoId(Integer solicitudPermisoId);
}
