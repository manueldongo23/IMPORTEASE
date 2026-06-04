package com.importease.proyecto.service;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogoNormativo {

    private final List<ReglaNormativa> reglas = new ArrayList<>();
    private String fuente;
    private String version;

    public CatalogoNormativo() {
        this.fuente = "Ley General de Aduanas - Decreto Supremo";
        this.version = "1.0";
        cargarReglasIniciales();
    }

    public CatalogoNormativo(String fuente, String version) {
        this.fuente = fuente;
        this.version = version;
        cargarReglasIniciales();
    }

    private void cargarReglasIniciales() {
        reglas.add(new ReglaNormativa("IMP_CONSUMO_DESTINACION", "Plazo destinacion aduanera importacion consumo", TipoRegla.BLOCKING,
                "LGA art 155", "DESPA-PG.01", version, true,
                "La mercancia importada debe destinarse dentro de 15 dias calendario desde el termino de la descarga."));

        reglas.add(new ReglaNormativa("IMP_CONSUMO_ABANDONO", "Abandono legal por vencimiento de plazo", TipoRegla.BLOCKING,
                "LGA art 158", "DESPA-PG.01", version, true,
                "Vencido el plazo de destinacion sin regularizar, la mercancia entra en abandono legal y puede ser rematada."));

        reglas.add(new ReglaNormativa("REIMPORTACION_PLAZO", "Plazo maximo para reimportacion", TipoRegla.BLOCKING,
                "LGA art 108", "DESPA-PG.26", version, true,
                "La reimportacion en el mismo estado debe realizarse dentro de 36 meses desde la exportacion precedente."));

        reglas.add(new ReglaNormativa("ADMISION_TEMPORAL_PLAZO", "Plazo maximo admision temporal", TipoRegla.BLOCKING,
                "LGA art 112", "DESPA-PG.04", version, true,
                "La mercancia bajo admision temporal puede permanecer hasta 540 dias prorrogables."));

        reglas.add(new ReglaNormativa("ADMISION_TEMPORAL_GARANTIA", "Garantia obligatoria admision temporal", TipoRegla.WARNING,
                "LGA art 113", "DESPA-PG.04", version, true,
                "Se debe constituir garantia por tributos suspendidos ante la SAF."));

        reglas.add(new ReglaNormativa("TRANSITO_RUTA", "Ruta autorizada para transito aduanero", TipoRegla.BLOCKING,
                "LGA art 161", "DESPA-PG.08", version, true,
                "El transito debe realizarse por la ruta autorizada por SUNAT sin desviaciones."));

        reglas.add(new ReglaNormativa("TRANSITO_PLAZO", "Plazo general para transito aduanero", TipoRegla.BLOCKING,
                "LGA art 161", "DESPA-PG.08", version, true,
                "El transito aduanero debe completarse en un plazo maximo de 30 dias calendario desde el levante."));

        reglas.add(new ReglaNormativa("TRANSITO_PRECINTOS", "Precintos obligatorios en transito", TipoRegla.BLOCKING,
                "LGA art 162", "DESPA-PG.08", version, true,
                "Los precintos aduaneros son obligatorios para garantizar la integridad de la carga en transito."));

        reglas.add(new ReglaNormativa("TRANSBORDO_MODALIDAD", "Modalidades de transbordo autorizadas", TipoRegla.WARNING,
                "LGA art 165", "DESPA-PG.11", version, true,
                "El transbordo puede ser directo (buque a buque) o indirecto (con deposito temporal)."));

        reglas.add(new ReglaNormativa("TRANSBORDO_PLAZO", "Plazo para reembarque en transbordo", TipoRegla.BLOCKING,
                "LGA art 165", "DESPA-PG.11", version, true,
                "El reembarque debe realizarse dentro de 30 dias calendario desde la descarga."));

        reglas.add(new ReglaNormativa("MERCANCIA_RESTRINGIDA", "Permiso sectorial obligatorio", TipoRegla.BLOCKING,
                "LGA art 63, DS 007-98-SA", "VUCE", version, true,
                "Las mercancias restringidas requieren permiso sectorial previo (VUCE) antes del despacho."));

        reglas.add(new ReglaNormativa("PREDAM_MANIFIESTO", "PRE-DAM bloqueada sin manifiesto", TipoRegla.BLOCKING,
                "LGA art 40, DESPA-PG.01", "DESPA-PG.01", version, true,
                "No se puede generar PRE-DAM sin manifiesto de carga registrado para la operacion."));

        reglas.add(new ReglaNormativa("PREDAM_FOB", "PRE-DAM bloqueada sin valor FOB", TipoRegla.BLOCKING,
                "LGA art 30, DESPA-PG.01", "DESPA-PG.01", version, true,
                "El valor FOB es obligatorio y debe ser mayor o igual a 1 USD para generar PRE-DAM."));

        reglas.add(new ReglaNormativa("PREDAM_HS", "PRE-DAM bloqueada sin codigo HS", TipoRegla.BLOCKING,
                "LGA art 30, DESPA-PG.01 anexo 8", "DESPA-PG.01", version, true,
                "El codigo de subpartida nacional (HS) es obligatorio para la clasificacion arancelaria."));

        reglas.add(new ReglaNormativa("PREDAM_TRANSPORTE", "PRE-DAM bloqueada sin documento de transporte", TipoRegla.BLOCKING,
                "LGA art 40", "DESPA-PG.01", version, true,
                "Debe existir un documento de transporte (BL/AWB) vinculado al manifiesto."));

        reglas.add(new ReglaNormativa("DTA_TIPO_CAMBIO", "DTA requiere tipo de cambio", TipoRegla.BLOCKING,
                "LGA art 30, Ley 27681", "DESPA-PG.01", version, true,
                "El tipo de cambio es obligatorio para la conversion de valores en la DTA."));
    }

    public List<ReglaNormativa> getReglasByTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) return new ArrayList<>(reglas);
        return reglas.stream()
                .filter(r -> r.getTipo().name().equalsIgnoreCase(tipo.trim()))
                .collect(Collectors.toList());
    }

    public Optional<ReglaNormativa> getRegla(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return reglas.stream()
                .filter(r -> r.getCodigo().equalsIgnoreCase(codigo.trim()))
                .findFirst();
    }

    public List<ReglaNormativa> getTodasLasReglas() {
        return new ArrayList<>(reglas);
    }

    public String getFuente() { return fuente; }
    public String getVersion() { return version; }

    public enum TipoRegla {
        BLOCKING, WARNING, INFO
    }

    public static class ReglaNormativa {
        private final String codigo;
        private final String nombre;
        private final TipoRegla tipo;
        private final String baseLegal;
        private final String fuente;
        private final String version;
        private final boolean vigente;
        private final String descripcion;

        public ReglaNormativa(String codigo, String nombre, TipoRegla tipo,
                              String baseLegal, String fuente, String version,
                              boolean vigente, String descripcion) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.tipo = tipo;
            this.baseLegal = baseLegal;
            this.fuente = fuente;
            this.version = version;
            this.vigente = vigente;
            this.descripcion = descripcion;
        }

        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public TipoRegla getTipo() { return tipo; }
        public String getBaseLegal() { return baseLegal; }
        public String getFuente() { return fuente; }
        public String getVersion() { return version; }
        public boolean isVigente() { return vigente; }
        public String getDescripcion() { return descripcion; }

        @Override
        public String toString() {
            return "ReglaNormativa{" +
                    "codigo='" + codigo + '\'' +
                    ", nombre='" + nombre + '\'' +
                    ", tipo=" + tipo +
                    ", baseLegal='" + baseLegal + '\'' +
                    ", vigente=" + vigente +
                    '}';
        }
    }
}


