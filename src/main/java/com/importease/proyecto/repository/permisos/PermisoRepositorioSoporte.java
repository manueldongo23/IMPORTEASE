package com.importease.proyecto.repository.permisos;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.PreguntaPermiso;
import com.importease.proyecto.model.ReglaRestriccion;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.model.SolicitudPermisoDato;
import com.importease.proyecto.model.jpa.DocumentoPermisoEntity;
import com.importease.proyecto.model.jpa.EntidadControlEntity;
import com.importease.proyecto.model.jpa.PreguntaPermisoEntity;
import com.importease.proyecto.model.jpa.ReglaRestriccionEntity;
import com.importease.proyecto.model.jpa.SolicitudPermisoDatoEntity;
import com.importease.proyecto.model.jpa.SolicitudPermisoEntity;
import com.importease.proyecto.repository.jpa.DocumentoPermisoJpaRepositorio;
import com.importease.proyecto.repository.jpa.EntidadControlJpaRepositorio;
import com.importease.proyecto.repository.jpa.PreguntaPermisoJpaRepositorio;
import com.importease.proyecto.repository.jpa.ReglaRestriccionJpaRepositorio;
import com.importease.proyecto.repository.jpa.RespuestaPermisoOperacionJpaRepositorio;
import com.importease.proyecto.repository.jpa.SolicitudPermisoDatoJpaRepositorio;
import com.importease.proyecto.repository.jpa.SolicitudPermisoJpaRepositorio;

import java.sql.ResultSet;
import java.sql.SQLException;

abstract class PermisoRepositorioSoporte {

    protected SolicitudPermisoJpaRepositorio getSolicitudRepo() {
        return SpringContextHolder.getBeanOrNull(SolicitudPermisoJpaRepositorio.class);
    }

    protected EntidadControlJpaRepositorio getEntidadRepo() {
        return SpringContextHolder.getBeanOrNull(EntidadControlJpaRepositorio.class);
    }

    protected ReglaRestriccionJpaRepositorio getReglaRepo() {
        return SpringContextHolder.getBeanOrNull(ReglaRestriccionJpaRepositorio.class);
    }

    protected PreguntaPermisoJpaRepositorio getPreguntaRepo() {
        return SpringContextHolder.getBeanOrNull(PreguntaPermisoJpaRepositorio.class);
    }

    protected DocumentoPermisoJpaRepositorio getDocumentoRepo() {
        return SpringContextHolder.getBeanOrNull(DocumentoPermisoJpaRepositorio.class);
    }

    protected SolicitudPermisoDatoJpaRepositorio getDatoRepo() {
        return SpringContextHolder.getBeanOrNull(SolicitudPermisoDatoJpaRepositorio.class);
    }

    protected RespuestaPermisoOperacionJpaRepositorio getRespuestaRepo() {
        return SpringContextHolder.getBeanOrNull(RespuestaPermisoOperacionJpaRepositorio.class);
    }

    protected EntidadControl mapearEntidad(ResultSet rs) throws SQLException {
        EntidadControl e = new EntidadControl();
        e.setId(rs.getInt("id"));
        e.setCodigoEntidad(rs.getString("codigo_entidad"));
        e.setNombreEntidad(rs.getString("nombre_entidad"));
        e.setSector(rs.getString("sector"));
        e.setDescripcion(rs.getString("descripcion"));
        e.setUrlReferencia(rs.getString("url_referencia"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }

    protected ReglaRestriccion mapearRegla(ResultSet rs) throws SQLException {
        ReglaRestriccion r = new ReglaRestriccion();
        r.setId(rs.getInt("id"));
        r.setCodigoEntidad(rs.getString("codigo_entidad"));
        r.setCapituloHs(rs.getObject("capitulo_hs") != null ? rs.getInt("capitulo_hs") : null);
        r.setPartidaHs(rs.getString("partida_hs"));
        r.setPalabraClave(rs.getString("palabra_clave"));
        r.setTipoPermiso(rs.getString("tipo_permiso"));
        r.setNivelRiesgo(rs.getString("nivel_riesgo"));
        r.setMensajeUsuario(rs.getString("mensaje_usuario"));
        r.setActivo(rs.getBoolean("activo"));
        return r;
    }

    protected PreguntaPermiso mapearPregunta(ResultSet rs) throws SQLException {
        PreguntaPermiso p = new PreguntaPermiso();
        p.setId(rs.getInt("id"));
        p.setCodigoEntidad(rs.getString("codigo_entidad"));
        p.setPregunta(rs.getString("pregunta"));
        p.setTipoRespuesta(rs.getString("tipo_respuesta"));
        p.setObligatoria(rs.getBoolean("obligatoria"));
        p.setOrden(rs.getInt("orden"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }

    protected DocumentoPermiso mapearDocumento(ResultSet rs) throws SQLException {
        DocumentoPermiso d = new DocumentoPermiso();
        d.setId(rs.getInt("id"));
        d.setCodigoEntidad(rs.getString("codigo_entidad"));
        d.setTipoPermiso(rs.getString("tipo_permiso"));
        d.setNombreDocumento(rs.getString("nombre_documento"));
        d.setDescripcion(rs.getString("descripcion"));
        d.setObligatorio(rs.getBoolean("obligatorio"));
        d.setFormatoAceptado(rs.getString("formato_aceptado"));
        return d;
    }

    protected SolicitudPermiso mapearSolicitud(ResultSet rs) throws SQLException {
        SolicitudPermiso s = new SolicitudPermiso();
        s.setId(rs.getInt("id"));
        s.setOperacionId(rs.getInt("operacion_id"));
        s.setUsuarioId(rs.getInt("usuario_id"));
        s.setCodigoEntidad(rs.getString("codigo_entidad"));
        s.setTipoPermiso(rs.getString("tipo_permiso"));
        s.setEstado(rs.getString("estado"));
        s.setNumeroSuce(rs.getString("numero_suce"));
        s.setNumeroDocumentoResolutivo(rs.getString("numero_documento_resolutivo"));
        s.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        s.setFechaEnvioVuce(rs.getTimestamp("fecha_envio_vuce"));
        s.setFechaAprobacion(rs.getTimestamp("fecha_aprobacion"));
        s.setObservaciones(rs.getString("observaciones"));
        return s;
    }

    protected SolicitudPermisoEntity toEntity(SolicitudPermiso model) {
        SolicitudPermisoEntity entity = new SolicitudPermisoEntity();
        if (model.getId() > 0) entity.setId(model.getId());
        entity.setOperacionId(model.getOperacionId());
        entity.setUsuarioId(model.getUsuarioId());
        entity.setCodigoEntidad(model.getCodigoEntidad());
        entity.setTipoPermiso(model.getTipoPermiso());
        entity.setEstado(model.getEstado());
        entity.setNumeroSuce(model.getNumeroSuce());
        entity.setNumeroDocumentoResolutivo(model.getNumeroDocumentoResolutivo());
        entity.setFechaCreacion(model.getFechaCreacion());
        entity.setFechaEnvioVuce(model.getFechaEnvioVuce());
        entity.setFechaAprobacion(model.getFechaAprobacion());
        entity.setObservaciones(model.getObservaciones());
        return entity;
    }

    protected SolicitudPermiso toModel(SolicitudPermisoEntity entity) {
        SolicitudPermiso s = new SolicitudPermiso();
        s.setId(entity.getId() == null ? 0 : entity.getId());
        s.setOperacionId(entity.getOperacionId() == null ? 0 : entity.getOperacionId());
        s.setUsuarioId(entity.getUsuarioId() == null ? 0 : entity.getUsuarioId());
        s.setCodigoEntidad(entity.getCodigoEntidad());
        s.setTipoPermiso(entity.getTipoPermiso());
        s.setEstado(entity.getEstado());
        s.setNumeroSuce(entity.getNumeroSuce());
        s.setNumeroDocumentoResolutivo(entity.getNumeroDocumentoResolutivo());
        s.setFechaCreacion(entity.getFechaCreacion());
        s.setFechaEnvioVuce(entity.getFechaEnvioVuce());
        s.setFechaAprobacion(entity.getFechaAprobacion());
        s.setObservaciones(entity.getObservaciones());
        return s;
    }

    protected EntidadControl toModel(EntidadControlEntity entity) {
        EntidadControl model = new EntidadControl();
        model.setId(entity.getId() == null ? 0 : entity.getId());
        model.setCodigoEntidad(entity.getCodigoEntidad());
        model.setNombreEntidad(entity.getNombreEntidad());
        model.setSector(entity.getSector());
        model.setDescripcion(entity.getDescripcion());
        model.setUrlReferencia(entity.getUrlReferencia());
        model.setActivo(Boolean.TRUE.equals(entity.getActivo()));
        return model;
    }

    protected ReglaRestriccion toModel(ReglaRestriccionEntity entity) {
        ReglaRestriccion model = new ReglaRestriccion();
        model.setId(entity.getId() == null ? 0 : entity.getId());
        model.setCodigoEntidad(entity.getCodigoEntidad());
        model.setCapituloHs(entity.getCapituloHs());
        model.setPartidaHs(entity.getPartidaHs());
        model.setPalabraClave(entity.getPalabraClave());
        model.setTipoPermiso(entity.getTipoPermiso());
        model.setNivelRiesgo(entity.getNivelRiesgo());
        model.setMensajeUsuario(entity.getMensajeUsuario());
        model.setActivo(Boolean.TRUE.equals(entity.getActivo()));
        return model;
    }

    protected PreguntaPermiso toModel(PreguntaPermisoEntity entity) {
        PreguntaPermiso model = new PreguntaPermiso();
        model.setId(entity.getId() == null ? 0 : entity.getId());
        model.setCodigoEntidad(entity.getCodigoEntidad());
        model.setPregunta(entity.getPregunta());
        model.setTipoRespuesta(entity.getTipoRespuesta());
        model.setObligatoria(Boolean.TRUE.equals(entity.getObligatoria()));
        model.setOrden(entity.getOrden() == null ? 0 : entity.getOrden());
        model.setActivo(Boolean.TRUE.equals(entity.getActivo()));
        return model;
    }

    protected DocumentoPermiso toModel(DocumentoPermisoEntity entity) {
        DocumentoPermiso model = new DocumentoPermiso();
        model.setId(entity.getId() == null ? 0 : entity.getId());
        model.setCodigoEntidad(entity.getCodigoEntidad());
        model.setTipoPermiso(entity.getTipoPermiso());
        model.setNombreDocumento(entity.getNombreDocumento());
        model.setDescripcion(entity.getDescripcion());
        model.setObligatorio(Boolean.TRUE.equals(entity.getObligatorio()));
        model.setFormatoAceptado(entity.getFormatoAceptado());
        return model;
    }

    protected SolicitudPermisoDatoEntity toEntity(SolicitudPermisoDato model) {
        SolicitudPermisoDatoEntity entity = new SolicitudPermisoDatoEntity();
        if (model.getId() > 0) entity.setId(model.getId());
        if (model.getSolicitudPermisoId() > 0) entity.setSolicitudPermisoId(model.getSolicitudPermisoId());
        entity.setCampo(model.getCampo());
        entity.setValor(model.getValor());
        entity.setOrigenDato(model.getOrigenDato());
        return entity;
    }

    protected SolicitudPermisoDato toModel(SolicitudPermisoDatoEntity entity) {
        SolicitudPermisoDato model = new SolicitudPermisoDato();
        model.setId(entity.getId() == null ? 0 : entity.getId());
        model.setSolicitudPermisoId(entity.getSolicitudPermisoId() == null ? 0 : entity.getSolicitudPermisoId());
        model.setCampo(entity.getCampo());
        model.setValor(entity.getValor());
        model.setOrigenDato(entity.getOrigenDato());
        return model;
    }
}
