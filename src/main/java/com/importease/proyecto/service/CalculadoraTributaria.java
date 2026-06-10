package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ResourceBundle;
import java.text.MessageFormat;

public class CalculadoraTributaria {

    private static final ResourceBundle bundle;
    static {
        ResourceBundle temp = null;
        try {
            temp = ResourceBundle.getBundle("messages_es");
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo cargar ResourceBundle messages_es: " + e.getMessage());
        }
        bundle = temp;
    }

    private static final BigDecimal UMBRAL_EXENTO = new BigDecimal("200");
    private static final BigDecimal UMBRAL_IMPORTA_FACIL = new BigDecimal("2000");
    private static final BigDecimal TASA_FLAT_IMPORTA_FACIL = new BigDecimal("0.04");

    private static String getMessage(String key, String fallback) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (Exception e) {
                // key missing
            }
        }
        return fallback;
    }

    private static String getMessage(String key, String fallback, Object... args) {
        String msg = getMessage(key, fallback);
        try {
            return MessageFormat.format(msg, args);
        } catch (Exception e) {
            return msg;
        }
    }

    public static ResultadoTributario calcularImpuestos(HsCode hs, Usuario usuario,
            BigDecimal fob, BigDecimal flete, BigDecimal seguro, String paisOrigen, BigDecimal tipoCambio, String incoterm) {
        return calcularImpuestos(hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, false, null);
    }

    public static ResultadoTributario calcularImpuestos(HsCode hs, Usuario usuario,
            BigDecimal fob, BigDecimal flete, BigDecimal seguro, String paisOrigen, BigDecimal tipoCambio, String incoterm, boolean usado) {
        return calcularImpuestos(hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, usado, null);
    }

    public static ResultadoTributario calcularImpuestos(HsCode hs, Usuario usuario,
            BigDecimal fob, BigDecimal flete, BigDecimal seguro, String paisOrigen, BigDecimal tipoCambio, String incoterm, boolean usado, String tipoRegimen) {
        if ("IMPORTA_FACIL".equals(tipoRegimen)) {
            return calcularImportaFacil(hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, usado);
        }
        return calcularImpuestosGenerales(hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, usado);
    }

    private static ResultadoTributario calcularImpuestosGenerales(HsCode hs, Usuario usuario,
            BigDecimal fob, BigDecimal flete, BigDecimal seguro, String paisOrigen, BigDecimal tipoCambio, String incoterm, boolean usado) {
        ResultadoTributario res = new ResultadoTributario();

        BigDecimal cif;
        if ("CIF".equalsIgnoreCase(incoterm)) {
            cif = fob;
        } else {
            cif = fob.add(flete).add(seguro);
        }
        res.setCif(cif);

        BigDecimal tasaAdValorem = hs.getAdValorem();
        String origen = paisOrigen == null ? "" : paisOrigen.trim();

        if ("CN".equalsIgnoreCase(origen)) {
            if (hs.isTlcChina()) {
                // Excluir capÃ­tulos 50-63 (textiles) y 64 (calzado) del TLC
                String codigo = hs.getCodigo();
                if (codigo != null && codigo.length() >= 2) {
                    int capitulo = Integer.parseInt(codigo.substring(0, 2));
                    if (capitulo < 50 || capitulo > 64) {
                        tasaAdValorem = BigDecimal.ZERO;
                    }
                } else {
                    tasaAdValorem = BigDecimal.ZERO;
                }
                res.setMensajeTlc(getMessage("tlc.china.aplicado", "âœ… TLC PerÃº-China aplicado (legacy). Ad-Valorem 0%. âš ï¸ Sujeto a exclusiÃ³n arancelaria segÃºn subpartida especÃ­fica (ej. calzado y textiles)."));
            } else {
                res.setMensajeTlc(getMessage("tlc.china.no_aplicado", "No se encontrÃ³ TLC vigente con China. Se aplica arancel general."));
            }
        } else {
            try {
                java.util.Map<String, Object> tlcResult = TratadoLibreComercioServicio.verificarTlc(origen);
                boolean tlcVigente = Boolean.TRUE.equals(tlcResult.get("tlcVigente"));
                if (tlcVigente) {
                    BigDecimal arancelPref = (BigDecimal) tlcResult.get("arancelPreferencial");
                    if (arancelPref != null && arancelPref.compareTo(tasaAdValorem) < 0) {
                        tasaAdValorem = arancelPref;
                    }
                    res.setMensajeTlc(getMessage("tlc.aplicado", "âœ… {0} aplicado. Ad-Valorem: {1}%.", tlcResult.get("nombreTlc"), tasaAdValorem));
                } else {
                    res.setMensajeTlc(String.valueOf(tlcResult.get("mensaje")));
                }
            } catch (Exception e) {
                res.setMensajeTlc(getMessage("tlc.no_encontrado", "No se pudo verificar TLC con {0}. Se aplica arancel general como fallback.", origen));
            }
        }

        BigDecimal arancel = cif.multiply(tasaAdValorem).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        res.setArancel(arancel);

        BigDecimal isc = (cif.add(arancel)).multiply(hs.getIsc()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        res.setIsc(isc);

        BigDecimal baseIgv = cif.add(arancel).add(isc);
        // IGV e IPM fijos por ley: 16% IGV + 2% IPM = 18%
        BigDecimal tasaIgv = BigDecimal.valueOf(16);
        BigDecimal tasaIpm = BigDecimal.valueOf(2);

        // Si el producto tiene IGV = 0 (exonerado), ambos son 0
        BigDecimal rawIgv = hs.getIgv();
        if (rawIgv != null && rawIgv.compareTo(BigDecimal.ZERO) == 0) {
            tasaIgv = BigDecimal.ZERO;
            tasaIpm = BigDecimal.ZERO;
        }
        
        BigDecimal igv = baseIgv.multiply(tasaIgv).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal ipm = baseIgv.multiply(tasaIpm).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        res.setIgv(igv);
        res.setIpm(ipm);

        BigDecimal basePercepcion = baseIgv.add(igv).add(ipm);
        BigDecimal percepcion;
        if (usuario != null && usuario.isBuenContribuyente()) {
            percepcion = BigDecimal.ZERO;
            res.setMensajePercepcion(getMessage("percepcion.buen_contribuyente", "âœ… Buen Contribuyente: exento de percepciÃ³n."));
        } else if (usado) {
            percepcion = basePercepcion.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            res.setMensajePercepcion(getMessage("percepcion.usado", "âš ï¸ MercancÃ­a Usada: percepciÃ³n obligatoria 10%."));
        } else if (usuario != null && "PRIMERA_IMPORTACION".equals(usuario.getPerfil())) {
            percepcion = basePercepcion.multiply(BigDecimal.valueOf(10)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            res.setMensajePercepcion(getMessage("percepcion.primera_importacion", "âš ï¸ Primera importaciÃ³n: percepciÃ³n 10%."));
        } else {
            percepcion = basePercepcion.multiply(BigDecimal.valueOf(3.5)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            res.setMensajePercepcion(getMessage("percepcion.estandar", "ðŸ“Œ PercepciÃ³n estÃ¡ndar: 3.5%."));
        }
        res.setPercepcion(percepcion);

        BigDecimal totalImpuestos = arancel.add(isc).add(igv).add(ipm).add(percepcion);
        res.setTotalImpuestos(totalImpuestos);
        BigDecimal totalNacionalizado = cif.add(totalImpuestos);
        res.setTotalNacionalizado(totalNacionalizado);
        res.setTotalSoles(totalNacionalizado.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP));
        // QA-018: Clamp all BigDecimal results to >= 0
        clampNonNegative(res);
        return res;
    }

    private static void clampNonNegative(ResultadoTributario res) {
        if (res.getCif() != null && res.getCif().signum() < 0) res.setCif(BigDecimal.ZERO);
        if (res.getArancel() != null && res.getArancel().signum() < 0) res.setArancel(BigDecimal.ZERO);
        if (res.getIsc() != null && res.getIsc().signum() < 0) res.setIsc(BigDecimal.ZERO);
        if (res.getIgv() != null && res.getIgv().signum() < 0) res.setIgv(BigDecimal.ZERO);
        if (res.getIpm() != null && res.getIpm().signum() < 0) res.setIpm(BigDecimal.ZERO);
        if (res.getPercepcion() != null && res.getPercepcion().signum() < 0) res.setPercepcion(BigDecimal.ZERO);
        if (res.getTotalImpuestos() != null && res.getTotalImpuestos().signum() < 0) res.setTotalImpuestos(BigDecimal.ZERO);
        if (res.getTotalNacionalizado() != null && res.getTotalNacionalizado().signum() < 0) res.setTotalNacionalizado(BigDecimal.ZERO);
        if (res.getTotalSoles() != null && res.getTotalSoles().signum() < 0) res.setTotalSoles(BigDecimal.ZERO);
    }

    public static java.util.Map<String, Double> calcularTributos(double fob, double flete, double seguro, String tipo) {
        com.importease.proyecto.model.HsCode dummyHs = new com.importease.proyecto.model.HsCode();
        dummyHs.setAdValorem(java.math.BigDecimal.ZERO);
        dummyHs.setIsc(java.math.BigDecimal.ZERO);
        dummyHs.setTlcChina(false);
        com.importease.proyecto.model.Usuario dummyUser = null;

        java.math.BigDecimal fobBD = java.math.BigDecimal.valueOf(fob);
        java.math.BigDecimal fleteBD = java.math.BigDecimal.valueOf(flete);
        java.math.BigDecimal seguroBD = java.math.BigDecimal.valueOf(seguro);
        java.math.BigDecimal tipoCambio = java.math.BigDecimal.ONE;
        String paisOrigen = "PE";
        String incoterm = "CIF";

        String regimen = ("PERSONAL".equalsIgnoreCase(tipo)) ? "IMPORTA_FACIL" : null;
        ResultadoTributario res = calcularImpuestos(dummyHs, dummyUser, fobBD, fleteBD, seguroBD, paisOrigen, tipoCambio, incoterm, false, regimen);
        java.util.Map<String, Double> map = new java.util.HashMap<>();
        map.put("totalImpuestos", res.getTotalImpuestos() != null ? res.getTotalImpuestos().doubleValue() : 0.0);
        return map;
    }

    public static ResultadoTributario calcularImportaFacil(HsCode hs, Usuario usuario,
            BigDecimal fob, BigDecimal flete, BigDecimal seguro, String paisOrigen, BigDecimal tipoCambio, String incoterm, boolean usado) {
        ResultadoTributario res = new ResultadoTributario();

        BigDecimal cif;
        if ("CIF".equalsIgnoreCase(incoterm)) {
            cif = fob;
        } else {
            cif = fob.add(flete).add(seguro);
        }
        res.setCif(cif);

        if (fob.compareTo(UMBRAL_EXENTO) < 0) {
            res.setArancel(BigDecimal.ZERO);
            res.setIsc(BigDecimal.ZERO);
            res.setIgv(BigDecimal.ZERO);
            res.setIpm(BigDecimal.ZERO);
            res.setPercepcion(BigDecimal.ZERO);
            res.setTotalImpuestos(BigDecimal.ZERO);
            res.setTotalNacionalizado(cif);
            res.setTotalSoles(cif.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP));
            res.setMensajeTlc(getMessage("importa.facil.exento", "Categoria B Importa Facil: exento de tributos (FOB < $200)."));
        } else if (fob.compareTo(UMBRAL_IMPORTA_FACIL) <= 0) {
            BigDecimal adValorem = cif.multiply(TASA_FLAT_IMPORTA_FACIL).setScale(2, RoundingMode.HALF_UP);
            res.setArancel(adValorem);
            res.setIsc(BigDecimal.ZERO);

            BigDecimal baseIgv = cif.add(adValorem);
            BigDecimal igv = baseIgv.multiply(new BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ipm = baseIgv.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
            res.setIgv(igv);
            res.setIpm(ipm);
            res.setPercepcion(BigDecimal.ZERO);

            BigDecimal totalImpuestos = adValorem.add(igv).add(ipm);
            res.setTotalImpuestos(totalImpuestos);
            BigDecimal totalNacionalizado = cif.add(totalImpuestos);
            res.setTotalNacionalizado(totalNacionalizado);
            res.setTotalSoles(totalNacionalizado.multiply(tipoCambio).setScale(2, RoundingMode.HALF_UP));
            res.setMensajePercepcion(getMessage("importa.facil.percepcion", "Regimen Importa Facil (Categoria C): exento de percepcion."));
        } else {
            return calcularImpuestosGenerales(hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, usado);
        }

        clampNonNegative(res);
        return res;
    }

    public static ResultadoTributario calcularImportaFacil(BigDecimal valorFobUSD, BigDecimal tipoCambio, boolean aplicaTLC) {
        ResultadoTributario res = new ResultadoTributario();

        if (valorFobUSD.compareTo(UMBRAL_EXENTO) < 0) {
            res.setCif(valorFobUSD.multiply(tipoCambio));
            res.setArancel(BigDecimal.ZERO);
            res.setIsc(BigDecimal.ZERO);
            res.setIgv(BigDecimal.ZERO);
            res.setIpm(BigDecimal.ZERO);
            res.setPercepcion(BigDecimal.ZERO);
            res.setTotalImpuestos(BigDecimal.ZERO);
            res.setTotalNacionalizado(res.getCif());
            res.setTotalSoles(res.getTotalNacionalizado());
        } else if (valorFobUSD.compareTo(UMBRAL_IMPORTA_FACIL) <= 0) {
            BigDecimal cif = valorFobUSD.multiply(tipoCambio);
            res.setCif(cif);

            BigDecimal tasaAV = aplicaTLC ? BigDecimal.ZERO : TASA_FLAT_IMPORTA_FACIL;
            BigDecimal adValorem = cif.multiply(tasaAV).setScale(2, RoundingMode.HALF_UP);
            res.setArancel(adValorem);
            res.setIsc(BigDecimal.ZERO);

            BigDecimal baseIgv = cif.add(adValorem);
            BigDecimal igv = baseIgv.multiply(new BigDecimal("0.16")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal ipm = baseIgv.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
            res.setIgv(igv);
            res.setIpm(ipm);
            res.setPercepcion(BigDecimal.ZERO);

            BigDecimal totalImpuestos = adValorem.add(igv).add(ipm);
            res.setTotalImpuestos(totalImpuestos);
            res.setTotalNacionalizado(cif.add(totalImpuestos));
            res.setTotalSoles(res.getTotalNacionalizado());
        } else {
            res.setCif(valorFobUSD.multiply(tipoCambio));
            res.setArancel(BigDecimal.ZERO);
            res.setIsc(BigDecimal.ZERO);
            res.setIgv(BigDecimal.ZERO);
            res.setIpm(BigDecimal.ZERO);
            res.setPercepcion(BigDecimal.ZERO);
            res.setTotalImpuestos(BigDecimal.ZERO);
            res.setTotalNacionalizado(res.getCif());
            res.setTotalSoles(res.getTotalNacionalizado());
            res.setMensajeTlc(getMessage("importa.facil.excede", "FOB > $2000: usar regimen general (no Importa Facil)."));
        }

        clampNonNegative(res);
        return res;
    }

    public static class ResultadoTributario {
        private BigDecimal cif, arancel, isc, igv, ipm, percepcion, totalImpuestos, totalNacionalizado, totalSoles;
        private String mensajeTlc, mensajePercepcion;

        public BigDecimal getCif() { return cif; }
        public void setCif(BigDecimal cif) { this.cif = cif; }
        public BigDecimal getArancel() { return arancel; }
        public void setArancel(BigDecimal arancel) { this.arancel = arancel; }
        public BigDecimal getIsc() { return isc; }
        public void setIsc(BigDecimal isc) { this.isc = isc; }
        public BigDecimal getIgv() { return igv; }
        public void setIgv(BigDecimal igv) { this.igv = igv; }
        public BigDecimal getIpm() { return ipm; }
        public void setIpm(BigDecimal ipm) { this.ipm = ipm; }
        public BigDecimal getPercepcion() { return percepcion; }
        public void setPercepcion(BigDecimal percepcion) { this.percepcion = percepcion; }
        public BigDecimal getTotalImpuestos() { return totalImpuestos; }
        public void setTotalImpuestos(BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }
        public BigDecimal getTotalNacionalizado() { return totalNacionalizado; }
        public void setTotalNacionalizado(BigDecimal totalNacionalizado) { this.totalNacionalizado = totalNacionalizado; }
        public BigDecimal getTotalSoles() { return totalSoles; }
        public void setTotalSoles(BigDecimal totalSoles) { this.totalSoles = totalSoles; }
        public String getMensajeTlc() { return mensajeTlc; }
        public void setMensajeTlc(String mensajeTlc) { this.mensajeTlc = mensajeTlc; }
        public String getMensajePercepcion() { return mensajePercepcion; }
        public void setMensajePercepcion(String mensajePercepcion) { this.mensajePercepcion = mensajePercepcion; }
    }
}


