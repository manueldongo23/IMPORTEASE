package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataConfidenceServicioTest {

    @Test
    public void reconoceTiposDeFuenteClaves() {
        assertEquals(0.96, DataConfidenceServicio.confidenceFor("OFFICIAL_PROCEDURE"));
        assertEquals(0.85, DataConfidenceServicio.confidenceFor("BD_LOCAL"));
        assertEquals(0.60, DataConfidenceServicio.confidenceFor("REFERENTIAL"));
        assertEquals(0.20, DataConfidenceServicio.confidenceFor("SIMULATED"));
        assertEquals(0.0, DataConfidenceServicio.confidenceFor("UNKNOWN"));
    }
}

