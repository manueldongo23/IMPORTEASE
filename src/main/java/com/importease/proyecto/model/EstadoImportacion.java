package com.importease.proyecto.model;

public enum EstadoImportacion {
    COTIZACION("CotizaciÃ³n"),
    TRANSITO("En TrÃ¡nsito"),
    DOCS_PENDIENTES("Documentos Pendientes"),
    LISTA_DESPACHO("Lista para Despacho"),
    NACIONALIZADA("Nacionalizada");

    private final String descripcion;

    EstadoImportacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

