package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataConfidenceServiceTest {

    @Test
    public void reconoceTiposDeFuenteClaves() {
        assertEquals(0.96, DataConfidenceService.confidenceFor("OFFICIAL_PROCEDURE"));
        assertEquals(0.85, DataConfidenceService.confidenceFor("BD_LOCAL"));
        assertEquals(0.60, DataConfidenceService.confidenceFor("REFERENTIAL"));
        assertEquals(0.20, DataConfidenceService.confidenceFor("SIMULATED"));
        assertEquals(0.0, DataConfidenceService.confidenceFor("UNKNOWN"));
    }
}

