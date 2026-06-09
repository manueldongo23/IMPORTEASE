package com.importease.proyecto.mapper;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.jpa.OperacionEntity;
import java.math.BigDecimal;

/**
 * Mapper centralizado para transformar entre entidades de base de datos (OperacionEntity)
 * y los modelos de negocio (Importacion, Operacion).
 */
public class ImportacionMapper {

    public static OperacionEntity toEntity(Importacion imp) {
        if (imp == null) return null;
        OperacionEntity entity = new OperacionEntity();
        if (imp.getId() > 0) entity.setId(imp.getId());
        entity.setUsuarioId(imp.getUsuarioId());
        entity.setProductoDesc(imp.getProductoDesc());
        entity.setHsCode(imp.getHsCode());
        entity.setPaisOrigen(imp.getPaisOrigen());
        entity.setIncoterm(imp.getIncoterm() != null ? imp.getIncoterm() : "FOB");
        entity.setFob(bd(imp.getValorFobBD()));
        entity.setFlete(bd(imp.getFleteBD()));
        entity.setSeguro(bd(imp.getSeguroBD()));
        entity.setCif(bd(imp.getValorCifBD()));
        entity.setTipoCambio(bd(imp.getTipoCambioBD()));
        entity.setAdValoremAplicado(bd(imp.getMontoAdValoremBD()));
        entity.setIscAplicado(bd(imp.getMontoIscBD()));
        entity.setIgvAplicado(bd(imp.getMontoIgbBD()));
        entity.setIpmAplicado(bd(imp.getMontoIpmBD()));
        entity.setPercepcionAplicada(bd(imp.getMontoPercepcionBD()));
        entity.setTotalImpuestos(bd(imp.getTotalImpuestosBD()));
        entity.setEstado(imp.getEstado());
        entity.setCanalAsignado(imp.getCanalAsignado());
        entity.setNumeroDam(imp.getNumeroDam());
        entity.setFechaNumeracion(imp.getFechaNumeracion());
        entity.setFechaCreacion(imp.getFechaCreacion());
        entity.setUsado(imp.isUsado());
        return entity;
    }

    public static Importacion toModel(OperacionEntity entity) {
        if (entity == null) return null;
        Importacion imp = new Importacion();
        imp.setId(entity.getId() == null ? 0 : entity.getId());
        imp.setUsuarioId(entity.getUsuarioId() == null ? 0 : entity.getUsuarioId());
        imp.setHsCode(entity.getHsCode());
        imp.setProductoDesc(entity.getProductoDesc());
        imp.setPaisOrigen(entity.getPaisOrigen());
        imp.setIncoterm(entity.getIncoterm());
        imp.setValorFobBD(entity.getFob());
        imp.setFleteBD(entity.getFlete());
        imp.setSeguroBD(entity.getSeguro());
        imp.setValorCifBD(entity.getCif());
        imp.setTipoCambioBD(entity.getTipoCambio());
        imp.setMontoAdValoremBD(entity.getAdValoremAplicado());
        imp.setMontoIscBD(entity.getIscAplicado());
        imp.setMontoIgbBD(entity.getIgvAplicado());
        imp.setMontoIpmBD(entity.getIpmAplicado());
        imp.setMontoPercepcionBD(entity.getPercepcionAplicada());
        imp.setTotalImpuestosBD(entity.getTotalImpuestos());
        imp.setEstado(entity.getEstado());
        imp.setCanalAsignado(entity.getCanalAsignado());
        imp.setNumeroDam(entity.getNumeroDam());
        imp.setFechaNumeracion(entity.getFechaNumeracion());
        imp.setFechaCreacion(entity.getFechaCreacion());
        imp.setUsado(Boolean.TRUE.equals(entity.getUsado()));
        return imp;
    }

    public static OperacionEntity toEntity(Operacion model) {
        if (model == null) return null;
        OperacionEntity entity = new OperacionEntity();
        if (model.getId() > 0) entity.setId(model.getId());
        entity.setUsuarioId(model.getUsuarioId());
        entity.setProductoDesc(model.getProductoDesc());
        entity.setHsCode(model.getHsCode());
        entity.setPaisOrigen(model.getPaisOrigen());
        entity.setIncoterm(model.getIncoterm());
        entity.setFob(model.getFob());
        entity.setFlete(model.getFlete());
        entity.setSeguro(model.getSeguro());
        entity.setCif(model.getCif());
        entity.setTipoCambio(model.getTipoCambio());
        entity.setAdValoremAplicado(model.getAdValoremAplicado());
        entity.setIscAplicado(model.getIscAplicado());
        entity.setIgvAplicado(model.getIgvAplicado());
        entity.setIpmAplicado(model.getIpmAplicado());
        entity.setPercepcionAplicada(model.getPercepcionAplicada());
        entity.setTotalImpuestos(model.getTotalImpuestos());
        entity.setCanalAsignado(model.getCanalAsignado());
        entity.setEstado(model.getEstado());
        entity.setNumeroDam(model.getNumeroDam());
        entity.setFechaNumeracion(model.getFechaNumeracion());
        entity.setFechaCreacion(model.getFechaCreacion());
        return entity;
    }

    public static Operacion toModelOperacion(OperacionEntity entity) {
        if (entity == null) return null;
        Operacion op = new Operacion();
        op.setId(entity.getId() == null ? 0 : entity.getId());
        op.setUsuarioId(entity.getUsuarioId() == null ? 0 : entity.getUsuarioId());
        op.setProductoDesc(entity.getProductoDesc());
        op.setHsCode(entity.getHsCode());
        op.setPaisOrigen(entity.getPaisOrigen());
        op.setIncoterm(entity.getIncoterm());
        op.setFob(entity.getFob());
        op.setFlete(entity.getFlete());
        op.setSeguro(entity.getSeguro());
        op.setCif(entity.getCif());
        op.setTipoCambio(entity.getTipoCambio());
        op.setAdValoremAplicado(entity.getAdValoremAplicado());
        op.setIscAplicado(entity.getIscAplicado());
        op.setIgvAplicado(entity.getIgvAplicado());
        op.setIpmAplicado(entity.getIpmAplicado());
        op.setPercepcionAplicada(entity.getPercepcionAplicada());
        op.setTotalImpuestos(entity.getTotalImpuestos());
        op.setCanalAsignado(entity.getCanalAsignado());
        op.setEstado(entity.getEstado());
        op.setNumeroDam(entity.getNumeroDam());
        op.setFechaNumeracion(entity.getFechaNumeracion());
        op.setFechaCreacion(entity.getFechaCreacion());
        return op;
    }

    private static BigDecimal bd(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
