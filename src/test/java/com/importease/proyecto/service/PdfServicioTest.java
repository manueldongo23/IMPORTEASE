package com.importease.proyecto.service;

import com.importease.proyecto.model.Importacion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PdfServicioTest {

    @Test
    public void testGenerarPdfDamExitoso() {
        PdfServicio pdfServicio = new PdfServicio();

        Importacion imp = new Importacion();
        imp.setId(1001);
        imp.setNumeroDam("PRE-DAM-2026-01001");
        imp.setProductoDesc("Celulares inteligentes de alta gama");
        imp.setHsCode("8517130000");
        imp.setCanalAsignado("VERDE");
        imp.setMontoAdValorem(0.0);
        imp.setMontoIgb(150.0);
        imp.setMontoIpm(20.0);
        imp.setMontoPercepcion(0.0);
        imp.setTotalImpuestos(170.0);

        byte[] pdfBytes = pdfServicio.generarPdfDam(imp, "Manuel Dongo", "20601234567");

        assertNotNull(pdfBytes, "El PDF generado no debe ser nulo.");
        assertTrue(pdfBytes.length > 0, "El PDF generado debe tener contenido (tamaÃ±o mayor a 0).");

        // Validar el encabezado mÃ¡gico del formato PDF (%PDF)
        if (pdfBytes.length >= 4) {
            String header = new String(pdfBytes, 0, 4);
            assertEquals("%PDF", header, "El archivo generado debe iniciar con el identificador mÃ¡gico %PDF.");
        }
    }

    @Test
    public void testGenerarPdfDamConCamposNulos() {
        PdfServicio pdfServicio = new PdfServicio();

        Importacion imp = new Importacion();
        imp.setId(1002);
        imp.setNumeroDam(null); // Caso nulo
        imp.setProductoDesc("Laptops Lenovo ThinkPad");
        imp.setHsCode("8471301000");
        imp.setCanalAsignado(null); // Caso nulo
        imp.setMontoAdValorem(200.0);
        imp.setMontoIgb(320.0);
        imp.setMontoIpm(40.0);
        imp.setMontoPercepcion(120.0);
        imp.setTotalImpuestos(680.0);

        byte[] pdfBytes = pdfServicio.generarPdfDam(imp, null, null);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        if (pdfBytes.length >= 4) {
            String header = new String(pdfBytes, 0, 4);
            assertEquals("%PDF", header);
        }
    }
}

