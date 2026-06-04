package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class RiskScoringServiceTest {

    @Test
    public void testEvaluarRiesgoVerde() {
        HsCode hs = new HsCode();
        hs.setAdValorem(BigDecimal.ZERO);
        hs.setRequiereVuce(false);

        Usuario u = new Usuario();
        u.setBuenContribuyente(true);

        BigDecimal cif = new BigDecimal("1000"); // score += 5
        // hs advalorem 0 => score += 0
        // requiereVuce false => score += 0
        // buenContribuyente true => score -= 10
        // total score = -5 clamped to 0

        RiskScoringService.ResultadoRiesgo res = RiskScoringService.evaluarRiesgo(cif, hs, u);
        assertEquals(0, res.getScore());
        assertEquals("VERDE", res.getCanal());
        assertTrue(res.getMensajeCanal().contains("Verde"));
    }

    @Test
    public void testEvaluarRiesgoNaranja() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6")); // score += 15
        hs.setRequiereVuce(true); // score += 20

        Usuario u = new Usuario();
        u.setBuenContribuyente(true); // score -= 10

        BigDecimal cif = new BigDecimal("6000"); // score += 20
        // total score = 20 + 15 + 20 - 10 = 45

        RiskScoringService.ResultadoRiesgo res = RiskScoringService.evaluarRiesgo(cif, hs, u);
        assertEquals(45, res.getScore());
        assertEquals("NARANJA", res.getCanal());
        assertTrue(res.getMensajeCanal().contains("Naranja"));
    }

    @Test
    public void testEvaluarRiesgoRojo() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("12")); // score += 25
        hs.setRequiereVuce(true); // score += 20

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);
        u.setPerfil("PRIMERA_IMPORTACION"); // score += 15

        BigDecimal cif = new BigDecimal("25000"); // score += 40
        // total score = 40 + 25 + 20 + 15 = 100

        RiskScoringService.ResultadoRiesgo res = RiskScoringService.evaluarRiesgo(cif, hs, u);
        assertEquals(100, res.getScore());
        assertEquals("ROJO", res.getCanal());
        assertTrue(res.getMensajeCanal().contains("Rojo"));
    }
}

