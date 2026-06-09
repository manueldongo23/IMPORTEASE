package com.importease.proyecto.repository.jpa;

import com.importease.proyecto.model.jpa.OperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface ImportacionJpaRepositorio extends JpaRepository<OperacionEntity, Integer> {

    List<OperacionEntity> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    @Query(value = "SELECT documento_factura, documento_bl, documento_certificado_origen FROM operaciones WHERE id = :id", nativeQuery = true)
    Object[] obtenerEstadoDocumentos(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE operaciones SET documento_factura = TRUE WHERE id = :id", nativeQuery = true)
    int marcarDocumentoFactura(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE operaciones SET documento_bl = TRUE WHERE id = :id", nativeQuery = true)
    int marcarDocumentoBl(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE operaciones SET documento_certificado_origen = TRUE WHERE id = :id", nativeQuery = true)
    int marcarDocumentoCertificadoOrigen(@Param("id") Integer id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE operaciones SET estado = :estado WHERE id = :id", nativeQuery = true)
    int actualizarEstado(@Param("id") Integer id, @Param("estado") String estado);

    @Transactional
    @Modifying
    @Query(value = "UPDATE operaciones SET numero_dam = :numeroDam, canal_asignado = :canal, fecha_numeracion = CURRENT_TIMESTAMP WHERE id = :id", nativeQuery = true)
    int actualizarDam(@Param("id") Integer id, @Param("numeroDam") String numeroDam, @Param("canal") String canal);

    @Query(value = "SELECT " +
        "COALESCE(SUM(total_impuestos), 0), " +
        "COALESCE(SUM(fob), 0), " +
        "COUNT(*), " +
        "COALESCE(SUM(CASE WHEN canal_asignado = 'VERDE' THEN 1 ELSE 0 END), 0), " +
        "COALESCE(SUM(CASE WHEN canal_asignado = 'NARANJA' THEN 1 ELSE 0 END), 0), " +
        "COALESCE(SUM(CASE WHEN canal_asignado = 'ROJO' THEN 1 ELSE 0 END), 0), " +
        "COALESCE(SUM(CASE WHEN canal_asignado IN ('ROJO', 'NARANJA') THEN 1 ELSE 0 END), 0), " +
        "COALESCE(SUM(CASE WHEN entidad_vuce IS NOT NULL OR permiso_vuce_obtenido = TRUE THEN 1 ELSE 0 END), 0) " +
        "FROM operaciones WHERE usuario_id = :usuarioId", nativeQuery = true)
    Object[] obtenerEstadisticas(@Param("usuarioId") Integer usuarioId);

    @Query(value = "SELECT COUNT(*) FROM log_busquedas WHERE usuario_id = :usuarioId", nativeQuery = true)
    Integer contarConsultasHs(@Param("usuarioId") Integer usuarioId);

    @Query(value = "SELECT entidad_vuce FROM operaciones WHERE usuario_id = :usuarioId AND entidad_vuce IS NOT NULL GROUP BY entidad_vuce ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String obtenerEntidadMasFrecuente(@Param("usuarioId") Integer usuarioId);

    @Query(value = "SELECT pais_origen FROM operaciones WHERE usuario_id = :usuarioId AND pais_origen IS NOT NULL GROUP BY pais_origen ORDER BY COUNT(*) DESC LIMIT 1", nativeQuery = true)
    String obtenerPaisMasFrecuente(@Param("usuarioId") Integer usuarioId);

    @Query(value = "SELECT COALESCE(SUM(cif * 0.06), 0) FROM operaciones WHERE usuario_id = :usuarioId AND (incoterm = 'CIF' OR incoterm IS NOT NULL) AND canal_asignado = 'VERDE'", nativeQuery = true)
    BigDecimal obtenerTlcAhorro(@Param("usuarioId") Integer usuarioId);
}
