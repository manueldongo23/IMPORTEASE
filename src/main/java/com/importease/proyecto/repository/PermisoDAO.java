package com.importease.proyecto.repository;

import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.ReglaRestriccion;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.model.SolicitudPermisoDato;
import com.importease.proyecto.repository.permisos.PermisoConsultaRepository;
import com.importease.proyecto.repository.permisos.PermisoDocumentoRepository;
import com.importease.proyecto.repository.permisos.PermisoRespuestaRepository;
import com.importease.proyecto.repository.permisos.PermisoSolicitudRepository;

import java.util.List;
import java.util.Map;

public class PermisoDAO {
    private final PermisoConsultaRepository consultaRepository;
    private final PermisoDocumentoRepository documentoRepository;
    private final PermisoSolicitudRepository solicitudRepository;
    private final PermisoRespuestaRepository respuestaRepository;

    public PermisoDAO() {
        this.respuestaRepository = new PermisoRespuestaRepository();
        this.consultaRepository = new PermisoConsultaRepository();
        this.documentoRepository = new PermisoDocumentoRepository();
        this.solicitudRepository = new PermisoSolicitudRepository(respuestaRepository);
    }

    public List<EntidadControl> listarEntidades() {
        return consultaRepository.listarEntidades();
    }

    public EntidadControl obtenerEntidad(String codigoEntidad) {
        return consultaRepository.obtenerEntidad(codigoEntidad);
    }

    public List<ReglaRestriccion> buscarReglasPorHsCode(String hsCode) {
        return consultaRepository.buscarReglasPorHsCode(hsCode);
    }

    public List<ReglaRestriccion> buscarReglasPorPalabraClave(String descripcion) {
        return consultaRepository.buscarReglasPorPalabraClave(descripcion);
    }

    public List<PreguntaPermiso> obtenerPreguntasPorEntidad(String codigoEntidad) {
        return consultaRepository.obtenerPreguntasPorEntidad(codigoEntidad);
    }

    public List<DocumentoPermiso> obtenerDocumentosPorPermiso(String codigoEntidad, String tipoPermiso) {
        return documentoRepository.obtenerDocumentosPorPermiso(codigoEntidad, tipoPermiso);
    }

    public int crearSolicitud(SolicitudPermiso sol) {
        return solicitudRepository.crearSolicitud(sol);
    }

    public boolean actualizarEstado(int solicitudId, String nuevoEstado) {
        return solicitudRepository.actualizarEstado(solicitudId, nuevoEstado);
    }

    public boolean registrarSuce(int solicitudId, String suce, String resolucion) {
        return solicitudRepository.registrarSuce(solicitudId, suce, resolucion);
    }

    public SolicitudPermiso obtenerSolicitud(int id) {
        return solicitudRepository.obtenerSolicitud(id);
    }

    public List<SolicitudPermiso> listarSolicitudesPorUsuario(int usuarioId) {
        return solicitudRepository.listarSolicitudesPorUsuario(usuarioId);
    }

    public List<SolicitudPermiso> listarSolicitudesPorOperacion(int operacionId) {
        return solicitudRepository.listarSolicitudesPorOperacion(operacionId);
    }

    public boolean guardarDatosSolicitud(int solicitudId, List<SolicitudPermisoDato> datos) {
        return respuestaRepository.guardarDatosSolicitud(solicitudId, datos);
    }

    public boolean eliminarDatosSolicitud(int solicitudId) {
        return respuestaRepository.eliminarDatosSolicitud(solicitudId);
    }

    public boolean guardarRespuestasPermiso(int operacionId, Map<Integer, String> respuestas) {
        return respuestaRepository.guardarRespuestasPermiso(operacionId, respuestas);
    }
}
