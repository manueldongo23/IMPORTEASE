package com.importease.proyecto.service.guidedflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlujoGuiadoPasoCatalogoTest {
    private final FlujoGuiadoPasoCatalogo catalog = new FlujoGuiadoPasoCatalogo();

    @Test
    public void obtenerNombrePasoReturnsKnownStepName() {
        assertTrue(catalog.obtenerNombrePaso(3).contains("HS Code"));
    }

    @Test
    public void obtenerDescripcionPasoReturnsDifferentTextByMode() {
        String principiante = catalog.obtenerDescripcionPaso(1, true);
        String tecnica = catalog.obtenerDescripcionPaso(1, false);
        assertFalse(principiante.isBlank());
        assertFalse(tecnica.isBlank());
        assertTrue(principiante.length() >= tecnica.length());
    }
}
