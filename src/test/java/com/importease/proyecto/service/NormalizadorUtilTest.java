package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NormalizadorUtilTest {

    @Test
    public void testNormalizarTextoConRuidosComunes() {
        String original = "Celular Xiaomi Redmi Note 12 - 8GB / 128GB - 3 PCS!!";
        String limpio = NormalizadorUtil.normalizar(original);
        assertEquals("celular xiaomi redmi note", limpio);
    }

    @Test
    public void testNormalizarTextoConAcentosYSimbologia() {
        String original = "¡Lámparas de Neón importadas! 120KG / 50 UNITS - marca XYZ #2026";
        String limpio = NormalizadorUtil.normalizar(original);
        assertEquals("lamparas de neon importadas marca xyz", limpio);
    }

    @Test
    public void testNormalizarTextoVacioONulo() {
        assertEquals("", NormalizadorUtil.normalizar(null));
        assertEquals("", NormalizadorUtil.normalizar("   "));
    }
}
