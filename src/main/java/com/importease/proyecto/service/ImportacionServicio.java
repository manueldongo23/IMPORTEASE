package com.importease.proyecto.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.security.SecureRandom;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.EstadoImportacion;
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.repository.HsCodeRepositorio;
import com.importease.proyecto.dto.HistorialDTO;
import com.importease.proyecto.dto.PermisoDTO;

/**
 * Servicio encargado de gestionar el flujo de importaciones y cálculo de impuestos.
 */
public class ImportacionServicio {

    private final ImportacionRepositorio importacionRepositorio;
    private final TipoCambioServicio tipoCambioServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final HsCodeRepositorio hsCodeRepositorio;

    public ImportacionServicio() {
        this(new ImportacionRepositorio(), new TipoCambioServicio(), new UsuarioRepositorio(), new HsCodeRepositorio());
    }

    public ImportacionServicio(
            ImportacionRepositorio importacionRepositorio,
            TipoCambioServicio tipoCambioServicio,
            UsuarioRepositorio usuarioRepositorio,
            HsCodeRepositorio hsCodeRepositorio
    ) {
        this.importacionRepositorio = importacionRepositorio;
        this.tipoCambioServicio = tipoCambioServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.hsCodeRepositorio = hsCodeRepositorio;
    }

    public List<Importacion> listarPorUsuario(int usuarioId) {
        try {
            return importacionRepositorio.listarPorUsuario(usuarioId);
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar importaciones por usuario", e);
            return new ArrayList<>();
        }
    }

    public List<HistorialDTO> obtenerHistorial(int usuarioId) {
        List<Importacion> lista = listarPorUsuario(usuarioId);
        List<HistorialDTO> historial = new ArrayList<>();
        if (lista != null) {
            for (Importacion imp : lista) {
                HistorialDTO dto = new HistorialDTO();
                dto.setId(imp.getId());
                dto.setProductoDesc(imp.getProductoDesc() != null ? imp.getProductoDesc() : "Mercancía General");
                String code = imp.getHsCode();
                dto.setHsCode(code != null ? code : "Sin Partida");
                
                String entity = "--";
                if (code != null) {
                    if (code.startsWith("8517")) entity = "MTC";
                    else if (code.startsWith("2106") || code.startsWith("1901")) entity = "DIGESA";
                    else if (code.startsWith("3004") || code.startsWith("3304") || code.startsWith("9018")) entity = "DIGEMID";
                    else if (code.startsWith("0602") || code.startsWith("1209")) entity = "SENASA";
                    else if (code.startsWith("4407")) entity = "SERFOR";
                }
                dto.setEntidad(entity);

                String rawState = imp.getEstado();
                if (rawState == null) rawState = "BORRADOR";
                dto.setRawState(rawState);
                
                String stateLabel = "Borrador";
                String stateClass = "text-[var(--text-secondary)] bg-[var(--surface-2)] border-[var(--border)]";
                String docFraction = "0/6";
                String nextAction = "Completar información";
                boolean isListable = false;
                
                if (rawState.equals("BORRADOR") || rawState.equals("COTIZACION")) {
                    stateLabel = "Borrador";
                    stateClass = "text-[var(--text-secondary)] bg-[var(--surface-2)] border-[var(--border)]";
                    docFraction = "0/6";
                    nextAction = "Completar información";
                } else if (!entity.equals("--") && !rawState.equals("LISTA_DESPACHO") && !rawState.equals("NACIONALIZADA")) {
                    stateLabel = "Requiere permiso";
                    stateClass = "text-[var(--warning)] bg-[var(--warning-soft)] border-[var(--warning)]";
                    docFraction = "2/6";
                    nextAction = "Preparar expediente";
                } else if (rawState.equals("PENDIENTE_DOCS")) {
                    stateLabel = "En revisión";
                    stateClass = "text-[var(--accent)] bg-[var(--accent-soft)] border-[var(--accent)]";
                    docFraction = "1/6";
                    nextAction = "Confirmar composición";
                } else {
                    stateLabel = "Lista";
                    stateClass = "text-[var(--success)] bg-[var(--success-soft)] border-[var(--success)]";
                    docFraction = "6/6";
                    nextAction = "Descargar reporte";
                    isListable = true;
                }
                
                dto.setStateLabel(stateLabel);
                dto.setStateClass(stateClass);
                dto.setDocFraction(docFraction);
                dto.setNextAction(nextAction);
                dto.setListable(isListable);
                
                historial.add(dto);
            }
        }
        return historial;
    }

    public List<PermisoDTO> obtenerPermisos(int usuarioId) {
        List<Importacion> lista = listarPorUsuario(usuarioId);
        List<PermisoDTO> permisos = new ArrayList<>();
        if (lista != null) {
            for (Importacion imp : lista) {
                String code = imp.getHsCode();
                String entity = "--";
                String permitName = "Licencia Especial";
                if (code != null) {
                    if (code.startsWith("8517")) { entity = "MTC"; permitName = "Homologación"; }
                    else if (code.startsWith("2106") || code.startsWith("1901")) { entity = "DIGESA"; permitName = "Registro sanitario"; }
                    else if (code.startsWith("3004") || code.startsWith("3304") || code.startsWith("9018")) { entity = "DIGEMID"; permitName = "Registro sanitario"; }
                    else if (code.startsWith("0602") || code.startsWith("1209")) { entity = "SENASA"; permitName = "Permiso fitosanitario"; }
                    else if (code.startsWith("4407")) { entity = "SERFOR"; permitName = "Permiso forestal"; }
                }
                
                if (entity.equals("--")) continue;

                PermisoDTO dto = new PermisoDTO();
                dto.setId(imp.getId());
                dto.setProductoDesc(imp.getProductoDesc() != null ? imp.getProductoDesc() : "Restringida");
                dto.setHsCode(code);
                dto.setEntidad(entity);
                dto.setPermitName(permitName);
                
                String rawState = imp.getEstado();
                if (rawState == null) rawState = "BORRADOR";
                dto.setRawState(rawState);
                
                String stateLabel = "Pendiente";
                String stateClass = "text-orange-600 bg-orange-50 border-orange-200";
                String actionLabel = "Iniciar trámite";
                String actionClass = "soft-button text-[10px]";
                
                if (rawState.equals("BORRADOR") || rawState.equals("COTIZACION")) {
                    stateLabel = "En preparación";
                    stateClass = "text-gray-500 bg-gray-50 border-gray-200";
                    actionLabel = "Editar";
                    actionClass = "soft-button text-[10px]";
                } else if (rawState.equals("PENDIENTE_DOCS")) {
                    stateLabel = "En revisión";
                    stateClass = "text-blue-600 bg-blue-50 border-blue-200";
                    actionLabel = "Continuar";
                    actionClass = "soft-button text-[10px]";
                } else if (rawState.equals("LISTA_DESPACHO") || rawState.equals("NACIONALIZADA")) {
                    stateLabel = "Aprobado";
                    stateClass = "text-emerald-600 bg-emerald-50 border-emerald-200";
                    actionLabel = "Descargar";
                    actionClass = "primary-button text-[10px]";
                }
                
                dto.setStateLabel(stateLabel);
                dto.setStateClass(stateClass);
                dto.setActionLabel(actionLabel);
                dto.setActionClass(actionClass);
                
                String fecha = imp.getFechaCreacion() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(imp.getFechaCreacion()) : "--";
                dto.setFecha(fecha);
                
                permisos.add(dto);
            }
        }
        return permisos;
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
        
        BigDecimal tc = tipoCambioServicio.obtenerTipoCambio();
        imp.setTipoCambioBD(tc);
        
        String cleanOrigen = (paisOrigen != null && !paisOrigen.trim().isEmpty()) ? paisOrigen : "CHINA";
        String cleanIncoterm = (incoterm != null && !incoterm.trim().isEmpty()) ? incoterm : "FOB";

        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId);
        HsCode hs = hsCodeRepositorio.obtenerPorCodigo(hsCode);

        HsCode hsToUse = hs;
        if (hsToUse == null) {
            ArancelServicio arancelServicio = new ArancelServicio();
            hsToUse = arancelServicio.consultarArancel(hsCode);
        }

        if ("PERSONAL".equals(tipo)) {
            HsCode tempHs = new HsCode();
            tempHs.setCodigo(hsToUse.getCodigo());
            tempHs.setDescripcionEs(hsToUse.getDescripcionEs());
            tempHs.setIsc(BigDecimal.ZERO);
            tempHs.setTlcChina(hsToUse.isTlcChina());
            
            if (fob.compareTo(BigDecimal.valueOf(200)) < 0) {
                tempHs.setAdValorem(BigDecimal.ZERO);
                tempHs.setIgv(BigDecimal.ZERO);
                tempHs.setIsc(BigDecimal.ZERO);
                Usuario tempUser = new Usuario();
                if (usuario != null) {
                    tempUser.setId(usuario.getId());
                    tempUser.setRazonSocial(usuario.getRazonSocial());
                    tempUser.setRuc(usuario.getRuc());
                    tempUser.setBuenContribuyente(usuario.isBuenContribuyente());
                }
                
                CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
                    tempHs, tempUser, fob, flete,
                    seguro, cleanOrigen, tc, cleanIncoterm
                );
                
                imp.setValorCifBD(res.getCif());
                imp.setMontoAdValoremBD(BigDecimal.ZERO);
                imp.setMontoIgbBD(BigDecimal.ZERO);
                imp.setMontoIpmBD(BigDecimal.ZERO);
                imp.setMontoPercepcionBD(BigDecimal.ZERO);
                imp.setTotalImpuestosBD(BigDecimal.ZERO);
            } else if (fob.compareTo(BigDecimal.valueOf(2000)) <= 0) {
                tempHs.setAdValorem(BigDecimal.valueOf(4));
                tempHs.setIgv(BigDecimal.valueOf(16));
                
                Usuario tempUser = new Usuario();
                if (usuario != null) {
                    tempUser.setId(usuario.getId());
                    tempUser.setRazonSocial(usuario.getRazonSocial());
                    tempUser.setRuc(usuario.getRuc());
                    tempUser.setBuenContribuyente(usuario.isBuenContribuyente());
                }
                
                CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
                    tempHs, tempUser, fob, flete,
                    seguro, cleanOrigen, tc, cleanIncoterm
                );
                
                imp.setValorCifBD(res.getCif());
                imp.setMontoAdValoremBD(res.getArancel());
                imp.setMontoIgbBD(res.getIgv());
                imp.setMontoIpmBD(res.getIpm());
                imp.setMontoPercepcionBD(BigDecimal.ZERO);
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
        
        imp.setEstado(EstadoImportacion.COTIZACION.name());
        imp.setProductoDesc(productoDesc != null && !productoDesc.trim().isEmpty() ? productoDesc : ("Importación " + tipo + " - " + hsCode));
        imp.setPaisOrigen(cleanOrigen);
        imp.setIncoterm(cleanIncoterm);

        try (Connection con = ConexionDB.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                importacionRepositorio.insertar(con, imp);

                if (imp.getId() > 0) {
                    String dam = generarNumeroDam();

                    BigDecimal cifBD = imp.getValorCifBD();
                    RiskScoringServicio.ResultadoRiesgo riesgo = RiskScoringServicio.evaluarRiesgo(cifBD, hs, usuario);
                    String canal = riesgo.getCanal();

                    importacionRepositorio.actualizarDam(con, imp.getId(), dam, canal);

                    imp.setNumeroDam(dam);
                    imp.setCanalAsignado(canal);

                    registrarDocumentosObligatorios(con, imp.getId());

                    importacionRepositorio.registrarHistorialEstado(con, imp.getId(), null, EstadoImportacion.COTIZACION.name(), "Registro inicial de la cotización de importación.", imp.getUsuarioId());
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
            Importacion imp = importacionRepositorio.buscarPorId(con, id);
            if (imp == null) {
                return false;
            }
            String estadoAnterior = imp.getEstado();
            
            if (EstadoImportacion.NACIONALIZADA.name().equals(nuevoEstado)) {
                boolean tieneDocs = importacionRepositorio.validarDocumentosParaDespacho(con, id);
                if (!tieneDocs) return false;
                
                String dam = generarNumeroDam();
                
                String canal = "VERDE";
                Usuario usuario = usuarioRepositorio.buscarPorId(imp.getUsuarioId());
                HsCode hs = hsCodeRepositorio.obtenerPorCodigo(imp.getHsCode());
                BigDecimal cifBD = imp.getValorCifBD();
                RiskScoringServicio.ResultadoRiesgo riesgo = RiskScoringServicio.evaluarRiesgo(cifBD, hs, usuario);
                canal = riesgo.getCanal();
                
                importacionRepositorio.actualizarDam(con, id, dam, canal);
            }

            boolean ok = importacionRepositorio.actualizarEstado(con, id, nuevoEstado);
            if (ok) {
                importacionRepositorio.registrarHistorialEstado(con, id, estadoAnterior, nuevoEstado, "Cambio de estado aduanero de la operación.", imp.getUsuarioId());
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
