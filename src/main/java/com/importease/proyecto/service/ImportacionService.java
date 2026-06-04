package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.math.BigDecimal;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionDAO;

public class ImportacionService {

    private ImportacionDAO importacionDAO;
    private TipoCambioService tipoCambioService;

    public ImportacionService() {
        this.importacionDAO = new ImportacionDAO();
        this.tipoCambioService = new TipoCambioService();
    }

    public java.util.List<Importacion> listarPorUsuario(int usuarioId) {
        try {
            return importacionDAO.listarPorUsuario(usuarioId);
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar importaciones por usuario", e);
            return new java.util.ArrayList<>();
        }
    }

    public Importacion generarOperacion(int usuarioId, String hsCode, BigDecimal fob, BigDecimal flete, BigDecimal seguro, String tipo) {
        return generarOperacion(usuarioId, hsCode, fob, flete, seguro, tipo, null, null);
    }

    public Importacion generarOperacion(int usuarioId, String hsCode, BigDecimal fob, BigDecimal flete, BigDecimal seguro, String tipo, String productoDesc, String paisOrigen) {
        return generarOperacion(usuarioId, hsCode, fob, flete, seguro, tipo, productoDesc, paisOrigen, "FOB");
    }

    public Importacion generarOperacion(int usuarioId, String hsCode, BigDecimal fob, BigDecimal flete, BigDecimal seguro, String tipo, String productoDesc, String paisOrigen, String incoterm) {
        return generarOperacion(usuarioId, hsCode, fob, flete, seguro, tipo, productoDesc, paisOrigen, incoterm, false);
    }

    public Importacion generarOperacion(int usuarioId, String hsCode, BigDecimal fob, BigDecimal flete, BigDecimal seguro, String tipo, String productoDesc, String paisOrigen, String incoterm, boolean usado) {
        Importacion imp = new Importacion();
        imp.setUsuarioId(usuarioId);
        imp.setHsCode(hsCode);
        imp.setValorFobBD(fob);
        imp.setFleteBD(flete);
        imp.setSeguroBD(seguro);
        imp.setUsado(usado);
        
        BigDecimal tc = tipoCambioService.obtenerTipoCambio();
        imp.setTipoCambioBD(tc);
        
        String cleanOrigen = (paisOrigen != null && !paisOrigen.trim().isEmpty()) ? paisOrigen : "CHINA";
        String cleanIncoterm = (incoterm != null && !incoterm.trim().isEmpty()) ? incoterm : "FOB";

        com.importease.proyecto.repository.UsuarioDAO usuarioDao = new com.importease.proyecto.repository.UsuarioDAO();
        com.importease.proyecto.repository.HsCodeDAO hsDao = new com.importease.proyecto.repository.HsCodeDAO();
        com.importease.proyecto.model.Usuario usuario = usuarioDao.buscarPorId(usuarioId);
        com.importease.proyecto.model.HsCode hs = hsDao.obtenerPorCodigo(hsCode);

        com.importease.proyecto.model.HsCode hsToUse = hs;
        if (hsToUse == null) {
            ArancelService arancelService = new ArancelService();
            hsToUse = arancelService.consultarArancel(hsCode);
        }

        if ("PERSONAL".equals(tipo)) {
            com.importease.proyecto.model.HsCode tempHs = new com.importease.proyecto.model.HsCode();
            tempHs.setCodigo(hsToUse.getCodigo());
            tempHs.setDescripcionEs(hsToUse.getDescripcionEs());
            tempHs.setIsc(java.math.BigDecimal.ZERO);
            tempHs.setTlcChina(hsToUse.isTlcChina());
            
            if (fob.compareTo(java.math.BigDecimal.valueOf(200)) < 0) {
                tempHs.setAdValorem(java.math.BigDecimal.ZERO);
                tempHs.setIgv(java.math.BigDecimal.ZERO);
                tempHs.setIsc(java.math.BigDecimal.ZERO);
                com.importease.proyecto.model.Usuario tempUser = new com.importease.proyecto.model.Usuario();
                if (usuario != null) {
                    tempUser.setId(usuario.getId());
                    tempUser.setRazonSocial(usuario.getRazonSocial());
                    tempUser.setRuc(usuario.getRuc());
                }
                tempUser.setBuenContribuyente(true);
                
                CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
                    tempHs, tempUser, fob, flete,
                    seguro, cleanOrigen, tc, cleanIncoterm
                );
                
                imp.setValorCifBD(res.getCif());
                imp.setMontoAdValoremBD(java.math.BigDecimal.ZERO);
                imp.setMontoIgbBD(java.math.BigDecimal.ZERO);
                imp.setMontoIpmBD(java.math.BigDecimal.ZERO);
                imp.setMontoPercepcionBD(java.math.BigDecimal.ZERO);
                imp.setTotalImpuestosBD(java.math.BigDecimal.ZERO);
            } else if (fob.compareTo(java.math.BigDecimal.valueOf(2000)) <= 0) {
                tempHs.setAdValorem(java.math.BigDecimal.valueOf(4));
                tempHs.setIgv(java.math.BigDecimal.valueOf(16));
                
                com.importease.proyecto.model.Usuario tempUser = new com.importease.proyecto.model.Usuario();
                if (usuario != null) {
                    tempUser.setId(usuario.getId());
                    tempUser.setRazonSocial(usuario.getRazonSocial());
                    tempUser.setRuc(usuario.getRuc());
                }
                tempUser.setBuenContribuyente(true);
                
                CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
                    tempHs, tempUser, fob, flete,
                    seguro, cleanOrigen, tc, cleanIncoterm
                );
                
                imp.setValorCifBD(res.getCif());
                imp.setMontoAdValoremBD(res.getArancel());
                imp.setMontoIgbBD(res.getIgv());
                imp.setMontoIpmBD(res.getIpm());
                imp.setMontoPercepcionBD(java.math.BigDecimal.ZERO);
                imp.setTotalImpuestosBD(res.getTotalImpuestos());
            } else {
                CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
                    hsToUse, usuario, fob, flete,
                    seguro, cleanOrigen, tc, cleanIncoterm, usado
                );
                
                imp.setValorCifBD(res.getCif());
                imp.setMontoAdValoremBD(res.getArancel());
                imp.setMontoIgbBD(res.getIgv());
                imp.setMontoIpmBD(res.getIpm());
                imp.setMontoPercepcionBD(res.getPercepcion());
                imp.setTotalImpuestosBD(res.getTotalImpuestos());
            }
        } else {
            CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
                hsToUse, usuario, fob, flete,
                seguro, cleanOrigen, tc, cleanIncoterm, usado
            );
            
            imp.setValorCifBD(res.getCif());
            imp.setMontoAdValoremBD(res.getArancel());
            imp.setMontoIgbBD(res.getIgv());
            imp.setMontoIpmBD(res.getIpm());
            imp.setMontoPercepcionBD(res.getPercepcion());
            imp.setTotalImpuestosBD(res.getTotalImpuestos());
        }
        
        imp.setEstado(com.importease.proyecto.model.EstadoImportacion.COTIZACION.name());
        imp.setProductoDesc(productoDesc != null && !productoDesc.trim().isEmpty() ? productoDesc : ("ImportaciÃ³n " + tipo + " - " + hsCode));
        imp.setPaisOrigen(cleanOrigen);
        imp.setIncoterm(cleanIncoterm);

        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                importacionDAO.insertar(con, imp);

                if (imp.getId() > 0) {
                    String dam = generarNumeroDam();

                    java.math.BigDecimal cifBD = imp.getValorCifBD();
                    RiskScoringService.ResultadoRiesgo riesgo = RiskScoringService.evaluarRiesgo(cifBD, hs, usuario);
                    String canal = riesgo.getCanal();

                    importacionDAO.actualizarDam(con, imp.getId(), dam, canal);

                    imp.setNumeroDam(dam);
                    imp.setCanalAsignado(canal);

                    registrarDocumentosObligatorios(con, imp.getId());

                    importacionDAO.registrarHistorialEstado(con, imp.getId(), null, com.importease.proyecto.model.EstadoImportacion.COTIZACION.name(), "Registro inicial de la cotizaciÃ³n de importaciÃ³n.", imp.getUsuarioId());
                }
                con.commit();
            } catch (SQLException e) {
                try { con.rollback(); } catch (SQLException ex) { /* fallback rollback */ }
                throw e;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al registrar operacion", e);
        }

        return imp;
    }

    private void registrarDocumentosObligatorios(Connection con, int impId) throws SQLException {
        String sql = "INSERT INTO documentos_importacion (importacion_id, tipo_documento, es_obligatorio) VALUES (?, ?, TRUE)";
        try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            String[] docs = {"FACTURA_COMERCIAL", "BILL_OF_LADING", "CERTIFICADO_ORIGEN"};
            for (String doc : docs) {
                ps.setInt(1, impId);
                ps.setString(2, doc);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public synchronized boolean cambiarEstado(int id, String nuevoEstado) {
        try (Connection con = ConexionDB.obtenerConexion()) {
            Importacion imp = importacionDAO.buscarPorId(con, id);
            if (imp == null) {
                return false;
            }
            String estadoAnterior = imp.getEstado();
            
            if (com.importease.proyecto.model.EstadoImportacion.NACIONALIZADA.name().equals(nuevoEstado)) {
                boolean tieneDocs = importacionDAO.validarDocumentosParaDespacho(con, id);
                if (!tieneDocs) return false;
                
                String dam = generarNumeroDam();
                
                String canal = "VERDE";
                com.importease.proyecto.repository.UsuarioDAO usuarioDao = new com.importease.proyecto.repository.UsuarioDAO();
                com.importease.proyecto.repository.HsCodeDAO hsDao = new com.importease.proyecto.repository.HsCodeDAO();
                com.importease.proyecto.model.Usuario usuario = usuarioDao.buscarPorId(imp.getUsuarioId());
                com.importease.proyecto.model.HsCode hs = hsDao.obtenerPorCodigo(imp.getHsCode());
                java.math.BigDecimal cifBD = imp.getValorCifBD();
                RiskScoringService.ResultadoRiesgo riesgo = RiskScoringService.evaluarRiesgo(cifBD, hs, usuario);
                canal = riesgo.getCanal();
                
                importacionDAO.actualizarDam(con, id, dam, canal);
            }

            boolean ok = importacionDAO.actualizarEstado(con, id, nuevoEstado);
            if (ok) {
                importacionDAO.registrarHistorialEstado(con, id, estadoAnterior, nuevoEstado, "Cambio de estado aduanero de la operaciÃ³n.", imp.getUsuarioId());
            }
            return ok;
        } catch (SQLException e) {
            LoggerUtil.error("Error al cambiar estado e insertar historial", e);
            return false;
        }
    }

    private String generarNumeroDam() {
        String anio = String.valueOf(java.time.Year.now().getValue());
        String num = String.format("%06d", new SecureRandom().nextInt(999999));
        return "PRE-DAM-118-" + anio + "-10-" + num;
    }
}


