package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DashboardServiceTest {

    @Test
    public void sanitizeColumnPermiteSoloColumnasWhitelisted() {
        Map<String, String> whitelist = new HashMap<>();
        whitelist.put("valor_fob", "valor_fob");

        String result = DashboardService.sanitizeColumn("valor_fob", whitelist);
        assertEquals("valor_fob", result);
    }

    @Test
    public void sanitizeColumnBloqueaPayloadNoPermitido() {
        Map<String, String> whitelist = new HashMap<>();
        whitelist.put("valor_fob", "valor_fob");

        String result = DashboardService.sanitizeColumn("valor_fob) FROM importaciones; DROP TABLE usuarios; --", whitelist);
        assertNull(result);
    }
}


