package com.importease.proyecto.model;

import java.sql.Timestamp;

public class DocumentoImportacion {
    private int id;
    private int importacionId;
    private String tipoDocumento; // FACTURA_COMERCIAL, PACKING_LIST, BILL_OF_LADING, CERTIFICADO_ORIGEN, POLIZA_SEGURO, OTROS
    private String rutaArchivo;
    private Timestamp fechaSubida;
    private boolean esObligatorio;

    public DocumentoImportacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getImportacionId() { return importacionId; }
    public void setImportacionId(int importacionId) { this.importacionId = importacionId; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public Timestamp getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(Timestamp fechaSubida) { this.fechaSubida = fechaSubida; }

    public boolean isEsObligatorio() { return esObligatorio; }
    public void setEsObligatorio(boolean esObligatorio) { this.esObligatorio = esObligatorio; }
}

