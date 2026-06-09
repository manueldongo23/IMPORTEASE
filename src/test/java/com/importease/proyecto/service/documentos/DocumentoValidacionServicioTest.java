package com.importease.proyecto.service.documentos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentoValidacionServicioTest {
    private final DocumentoValidacionServicio service = new DocumentoValidacionServicio();

    @Test
    public void normalizarTipoDocumentoReturnsUppercaseTrimmedValue() {
        assertEquals("FACTURA_COMERCIAL", service.normalizarTipoDocumento(" factura_comercial "));
    }

    @Test
    public void isTipoDocumentoPermitidoRejectsUnknownType() {
        assertTrue(service.isTipoDocumentoPermitido("BILL_OF_LADING"));
        assertFalse(service.isTipoDocumentoPermitido("CONTRATO_PRIVADO"));
    }

    @Test
    public void parsePositiveIntRejectsInvalidValues() {
        assertEquals(12, service.parsePositiveInt("12", -1));
        assertEquals(-1, service.parsePositiveInt("0", -1));
        assertEquals(-1, service.parsePositiveInt("abc", -1));
    }

    @Test
    public void getFileExtensionRejectsDoubleExtension() {
        assertEquals("pdf", service.getFileExtension("factura.pdf"));
        assertEquals("", service.getFileExtension("factura.pdf.exe"));
    }
}
