package com.importease.proyecto.service;

import com.importease.proyecto.model.Usuario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para la validación local del RUC peruano.
 * Verifica el algoritmo del dígito verificador (módulo 11).
 */
class RucValidadorLocalTest {

    // RUCs reales conocidos para validación
    @Test
    void testRucValidoPersonaJuridica() {
        // SUNAT - RUC oficial
        assertTrue(RucValidadorLocal.esRucValido("20100130204"),
                "El RUC de SUNAT (20100130204) debería ser válido");
    }

    @Test
    void testRucValidoPersonaNatural() {
        // Formato 10 + DNI + dígito verificador
        // Usamos un RUC calculado correctamente
        String ruc = generarRucValido("10");
        if (ruc != null) {
            assertTrue(RucValidadorLocal.esRucValido(ruc),
                    "Un RUC generado correctamente debería pasar la validación");
        }
    }

    @Test
    void testRucNuloRetornaFalse() {
        assertFalse(RucValidadorLocal.esRucValido(null),
                "RUC nulo debería retornar false");
    }

    @Test
    void testRucVacioRetornaFalse() {
        assertFalse(RucValidadorLocal.esRucValido(""),
                "RUC vacío debería retornar false");
    }

    @Test
    void testRucMenor11DigitosRetornaFalse() {
        assertFalse(RucValidadorLocal.esRucValido("2010013020"),
                "RUC con menos de 11 dígitos debería retornar false");
    }

    @Test
    void testRucMayor11DigitosRetornaFalse() {
        assertFalse(RucValidadorLocal.esRucValido("201001302040"),
                "RUC con más de 11 dígitos debería retornar false");
    }

    @Test
    void testRucConLetrasRetornaFalse() {
        assertFalse(RucValidadorLocal.esRucValido("20ABC130204"),
                "RUC con letras debería retornar false");
    }

    @Test
    void testRucConPrefijoInvalido() {
        // Prefijo 30 no es válido
        assertFalse(RucValidadorLocal.esRucValido("30100130204"),
                "RUC con prefijo 30 debería retornar false");
    }

    @Test
    void testRucConDigitoVerificadorIncorrecto() {
        // 20100130205 tiene dígito verificador incorrecto (debería ser 4)
        assertFalse(RucValidadorLocal.esRucValido("20100130205"),
                "RUC con dígito verificador incorrecto debería retornar false");
    }

    @Test
    void testDeterminarPerfilJuridico() {
        assertEquals("IMPORTADOR_JURIDICO", RucValidadorLocal.determinarPerfil("20100130204"),
                "RUC que empieza con 20 debería ser IMPORTADOR_JURIDICO");
    }

    @Test
    void testDeterminarPerfilNatural() {
        assertEquals("IMPORTADOR_NATURAL", RucValidadorLocal.determinarPerfil("10123456789"),
                "RUC que empieza con 10 debería ser IMPORTADOR_NATURAL");
    }

    @Test
    void testDeterminarPerfilInstitucionPublica() {
        assertEquals("INSTITUCION_PUBLICA", RucValidadorLocal.determinarPerfil("15000000001"),
                "RUC que empieza con 15 debería ser INSTITUCION_PUBLICA");
    }

    @Test
    void testDeterminarPerfilNoDomiciliado() {
        assertEquals("NO_DOMICILIADO", RucValidadorLocal.determinarPerfil("17000000001"),
                "RUC que empieza con 17 debería ser NO_DOMICILIADO");
    }

    @Test
    void testDeterminarPerfilNulo() {
        assertEquals("OTROS", RucValidadorLocal.determinarPerfil(null),
                "RUC nulo debería retornar OTROS");
    }

    @Test
    void testValidarLocalmenteConRucValido() {
        Usuario u = RucValidadorLocal.validarLocalmente("20100130204");
        assertNotNull(u, "Validación local de un RUC válido no debería retornar null");
        assertEquals("20100130204", u.getRuc());
        assertTrue(u.isRucValidado());
        assertEquals("IMPORTADOR_JURIDICO", u.getPerfil());
        assertNotNull(u.getFuenteRuc());
        assertTrue(u.getFuenteRuc().contains("LOCAL"));
    }

    @Test
    void testValidarLocalmenteConRucInvalido() {
        Usuario u = RucValidadorLocal.validarLocalmente("20100130205");
        assertNull(u, "Validación local de un RUC inválido debería retornar null");
    }

    @Test
    void testValidarLocalmenteRetornaConfianza() {
        Usuario u = RucValidadorLocal.validarLocalmente("20100130204");
        assertNotNull(u);
        assertTrue(u.getRucConfianza() > 0.0,
                "La confianza debería ser mayor que 0");
        assertTrue(u.getRucConfianza() <= 1.0,
                "La confianza debería ser menor o igual a 1.0");
    }

    /**
     * Helper: genera un RUC válido con el prefijo indicado para testing.
     */
    private String generarRucValido(String prefijo) {
        int[] factores = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };
        String base = prefijo + "12345678"; // 10 dígitos
        if (base.length() != 10) return null;

        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(base.charAt(i)) * factores[i];
        }
        int residuo = suma % 11;
        int digito = 11 - residuo;
        if (digito == 10) digito = 0;
        if (digito == 11) digito = 1;

        return base + digito;
    }
}
