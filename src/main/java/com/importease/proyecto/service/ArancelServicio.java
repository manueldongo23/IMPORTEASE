package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.repository.HsCodeRepositorio;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import com.importease.proyecto.service.LoggerUtil;

public class ArancelServicio {

    private com.importease.proyecto.repository.IHsCodeRepositorio hsDao = new com.importease.proyecto.repository.HsCodeRepositorio();
    private ArancelScraper scraper = new ArancelScraper();
    private com.importease.proyecto.repository.IOperacionRepositorio operacionDao = new com.importease.proyecto.repository.OperacionRepositorio();

    // In-memory cache: evita re-scrapear la misma partida en la misma sesiÃ³n
    private static final Map<String, HsCode> cache = new ConcurrentHashMap<>();

    public void registrarAuditoria(int usuarioId, HsCode hs, String tipo) {
        if (hs == null) return;
        try {
            com.importease.proyecto.model.Operacion op = new com.importease.proyecto.model.Operacion();
            op.setUsuarioId(usuarioId);
            op.setProductoDesc(hs.getDescripcionEs());
            op.setHsCode(hs.getCodigo());
            op.setEstado(tipo); // "CONSULTA" o "AUDITORIA"
            op.setFob(BigDecimal.ZERO);
            op.setFlete(BigDecimal.ZERO);
            op.setSeguro(BigDecimal.ZERO);
            op.setTipoCambio(new BigDecimal("3.75"));
            op.setAdValoremAplicado(hs.getAdValorem());
            op.setIscAplicado(hs.getIsc());
            op.setIgvAplicado(hs.getIgv() != null ? hs.getIgv() : new BigDecimal("16.00"));
            op.setPercepcionAplicada(BigDecimal.ZERO);
            op.setTotalImpuestos(BigDecimal.ZERO);
            op.setCanalAsignado("GRIS");
            op.setPaisOrigen("VAR");
            op.setIncoterm(tipo.equals("AUDITORIA") ? "VUCE" : "EXW");
            
            operacionDao.guardar(op);
            LoggerUtil.info("AuditorÃ­a registrada para usuario: " + usuarioId);
        } catch (Exception e) {
            LoggerUtil.error("Error al registrar auditorÃ­a", e);
        }
    }

    public HsCode consultarArancelLocal(String codigo) {
        return hsDao.obtenerPorCodigo(codigo);
    }

    public HsCode consultarArancelWebScraping(String codigo) {
        return scraper.scrapearPartida(codigo);
    }

    public HsCode consultarArancel(String codigo) {
        String key = codigo.replace(".", "").trim();

        // 1. Revisar cache en memoria (mÃ¡s rÃ¡pido que DB)
        if (cache.containsKey(key)) {
            LoggerUtil.info("Cache hit for HS: " + key);
            return cache.get(key);
        }

        // 2. Buscar en base de datos local
        HsCode hs = consultarArancelLocal(key);
        
        // Verificar si la informaciÃ³n es reciente (menos de 30 dÃ­as)
        boolean esReciente = false;
        if (hs != null && hs.getFechaActualizacion() != null) {
            long diff = System.currentTimeMillis() - hs.getFechaActualizacion().getTime();
            long dias = diff / (1000 * 60 * 60 * 24);
            if (dias < 30) esReciente = true;
            else LoggerUtil.info("Datos obsoletos para: " + key + " (" + dias + " dÃ­as)");
        }

        if (hs != null && esReciente) {
            cache.put(key, hs);
            return hs;
        }

        // 3. Scrapear de SUNAT (si no estÃ¡, o si es obsoleta)
        LoggerUtil.info("Scraping SUNAT for HS: " + key);
        HsCode hsNuevo = consultarArancelWebScraping(key);
        if (hsNuevo != null) {
            if (hs == null) {
                hsDao.insertar(hsNuevo);   // Nuevo registro
            } else {
                hsDao.actualizar(hsNuevo); // Actualizar existente (obsoleto)
            }
            cache.put(key, hsNuevo);
            return hsNuevo;
        }

        // Si el scraping falla pero tenemos datos viejos, los usamos como salvavidas
        if (hs != null) {
            LoggerUtil.info("Scraping fallÃ³, usando datos obsoletos como fallback: " + key);
            cache.put(key, hs);
            return hs;
        }

        // 4. Fallback por capÃ­tulo (siempre devuelve algo)
        LoggerUtil.info("Usando fallback para: " + key);
        hs = fallbackPorCapitulo(key);
        cache.put(key, hs);
        return hs;
    }

    private HsCode fallbackPorCapitulo(String codigo) {
        HsCode hs = new HsCode();
        hs.setCodigo(codigo);
        hs.setIgv(new BigDecimal("18"));
        hs.setIsc(BigDecimal.ZERO);

        try {
            int cap = Integer.parseInt(codigo.substring(0, 2));
            if (cap == 33) {
                hs.setDescripcionEs("[Aproximado] Perfumes, cosmÃ©ticos y preparaciones de tocador");
                hs.setAdValorem(new BigDecimal("17.00"));
                hs.setRequiereVuce(true); hs.setEntidadVuce("DIGESA");
            } else if (cap == 30) {
                hs.setDescripcionEs("[Aproximado] Productos farmacÃ©uticos");
                hs.setAdValorem(BigDecimal.ZERO);
                hs.setRequiereVuce(true); hs.setEntidadVuce("DIGEMID");
            } else if (cap == 84 || cap == 85) {
                hs.setDescripcionEs("[Aproximado] MÃ¡quinas y aparatos elÃ©ctricos / electrÃ³nicos");
                hs.setAdValorem(BigDecimal.ZERO);
                hs.setRequiereVuce(false);
            } else if (cap >= 1 && cap <= 24) {
                hs.setDescripcionEs("[Aproximado] Productos alimenticios o agropecuarios");
                hs.setAdValorem(new BigDecimal("6.00"));
                hs.setRequiereVuce(true); hs.setEntidadVuce("DIGESA");
            } else if (cap == 61 || cap == 62 || cap == 63) {
                hs.setDescripcionEs("[Aproximado] Prendas y complementos de vestir");
                hs.setAdValorem(new BigDecimal("11.00"));
                hs.setRequiereVuce(false);
            } else {
                hs.setDescripcionEs("[Aproximado] MercancÃ­a general (clasificaciÃ³n pendiente de SUNAT)");
                hs.setAdValorem(new BigDecimal("6.00"));
            }
        } catch (Exception e) {
            hs.setDescripcionEs("[Aproximado] MercancÃ­a sin clasificar");
            hs.setAdValorem(new BigDecimal("6.00"));
        }
        return hs;
    }
}

