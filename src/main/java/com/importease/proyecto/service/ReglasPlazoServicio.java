package com.importease.proyecto.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ReglasPlazoServicio {

    private final List<ReglaPlazo> reglas = new ArrayList<>();

    public ReglasPlazoServicio() {
        cargarPlazos();
    }

    private void cargarPlazos() {
        reglas.add(new ReglaPlazo("DIFERIDO_10", "Importacion diferida - plazo destinacion",
                "TERMINO_DESCARGA", 15, "10", "DIFERIDO", "MARITIMA",
                "DESPA-PG.01 / LGA art 150", "1.0", true));

        reglas.add(new ReglaPlazo("ANTICIPADO_10", "Importacion anticipada - plazo antes de llegada",
                "FECHA_LLEGADA", 30, "10", "ANTICIPADO", "AEREA",
                "DESPA-PG.01 / LGA art 150", "1.0", true));

        reglas.add(new ReglaPlazo("REIMPORTACION_23", "Reimportacion - plazo maximo desde exportacion",
                "FECHA_EMBARQUE", 1095, "23", null, null,
                "DESPA-PG.26 / LGA art 108", "1.0", true));

        reglas.add(new ReglaPlazo("ADMISION_TEMPORAL_21", "Admision temporal - plazo maximo",
                "FECHA_LEVANTE", 540, "21", null, null,
                "DESPA-PG.04 / LGA art 112", "1.0", true));

        reglas.add(new ReglaPlazo("TRANSITO_50", "Transito aduanero - plazo general",
                "FECHA_LEVANTE", 30, "50", null, null,
                "DESPA-PG.08 / LGA art 161", "1.0", true));

        reglas.add(new ReglaPlazo("TRANSBORDO_60", "Transbordo - plazo para reembarque",
                "FECHA_NUMERACION", 30, "60", null, null,
                "DESPA-PG.11 / LGA art 165", "1.0", true));

        reglas.add(new ReglaPlazo("ABANDONO_DIFERIDO", "Abandono legal - plazo desde termino descarga",
                "TERMINO_DESCARGA", 30, "10", "DIFERIDO", "MARITIMA",
                "DESPA-PG.01 / LGA art 155", "1.0", true));

        reglas.add(new ReglaPlazo("ABANDONO_ANTICIPADO", "Abandono legal - plazo desde llegada",
                "FECHA_LLEGADA", 15, "10", "ANTICIPADO", "AEREA",
                "DESPA-PG.01 / LGA art 155", "1.0", true));
    }

    public Map<String, Object> calcularPlazo(String codigoRegla, LocalDateTime fechaBase) {
        Map<String, Object> resultado = new LinkedHashMap<>();

        Optional<ReglaPlazo> opt = getRegla(codigoRegla);
        if (opt.isEmpty()) {
            resultado.put("success", false);
            resultado.put("error", "Regla de plazo no encontrada: " + codigoRegla);
            return resultado;
        }

        ReglaPlazo regla = opt.get();
        if (fechaBase == null) {
            resultado.put("success", false);
            resultado.put("error", "Fecha base es nula");
            return resultado;
        }

        LocalDateTime now = ZonedDateTime.now(ZoneId.of("America/Lima")).toLocalDateTime();
        LocalDateTime fechaLimite = fechaBase.plusDays(regla.getPlazoDias());
        long diasRestantes = ChronoUnit.DAYS.between(now, fechaLimite);

        resultado.put("success", true);
        resultado.put("codigoRegla", regla.getCodigo());
        resultado.put("label", regla.getLabel());
        resultado.put("regimen", regla.getRegimen());
        resultado.put("modalidad", regla.getModalidad());
        resultado.put("via", regla.getVia());
        resultado.put("eventoBase", regla.getEventoBase());
        resultado.put("plazoDias", regla.getPlazoDias());
        resultado.put("fechaBase", fechaBase.format(DateTimeFormatter.ISO_LOCAL_DATE));
        resultado.put("fechaLimite", fechaLimite.format(DateTimeFormatter.ISO_LOCAL_DATE));
        resultado.put("diasRestantes", diasRestantes);
        resultado.put("normaFuente", regla.getNormaFuente());

        String estado = "OK";
        String riesgo = "BAJO";
        if (diasRestantes < 0) {
            estado = "VENCIDO";
            riesgo = "CRITICO";
        } else if (diasRestantes <= 5) {
            estado = "CRITICO";
            riesgo = "ALTO";
        } else if (diasRestantes <= 15) {
            estado = "PROXIMO";
            riesgo = "MEDIO";
        }
        resultado.put("estado", estado);
        resultado.put("riesgo", riesgo);

        resultado.putAll(FuenteMetadataBuilder.buildMetadata(
                regla.getNormaFuente(),
                FuenteMetadataBuilder.TYPE_SYSTEM_RULE,
                1.0,
                "Plazo calculado segun regla normativa " + regla.getCodigo()
        ));

        return resultado;
    }

    public Optional<ReglaPlazo> getRegla(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return reglas.stream()
                .filter(r -> r.getCodigo().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    public List<ReglaPlazo> getReglasPorRegimen(String regimen) {
        if (regimen == null) return Collections.emptyList();
        return reglas.stream()
                .filter(r -> regimen.equals(r.getRegimen()))
                .toList();
    }

    public List<ReglaPlazo> getTodasLasReglas() {
        return new ArrayList<>(reglas);
    }

    public static class ReglaPlazo {
        private final String codigo;
        private final String label;
        private final String eventoBase;
        private final int plazoDias;
        private final String regimen;
        private final String modalidad;
        private final String via;
        private final String normaFuente;
        private final String version;
        private final boolean vigente;

        public ReglaPlazo(String codigo, String label, String eventoBase, int plazoDias,
                          String regimen, String modalidad, String via,
                          String normaFuente, String version, boolean vigente) {
            this.codigo = codigo;
            this.label = label;
            this.eventoBase = eventoBase;
            this.plazoDias = plazoDias;
            this.regimen = regimen;
            this.modalidad = modalidad;
            this.via = via;
            this.normaFuente = normaFuente;
            this.version = version;
            this.vigente = vigente;
        }

        public String getCodigo() { return codigo; }
        public String getLabel() { return label; }
        public String getEventoBase() { return eventoBase; }
        public int getPlazoDias() { return plazoDias; }
        public String getRegimen() { return regimen; }
        public String getModalidad() { return modalidad; }
        public String getVia() { return via; }
        public String getNormaFuente() { return normaFuente; }
        public String getVersion() { return version; }
        public boolean isVigente() { return vigente; }
    }
}


