package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalculadoraTributariaTest {

    @Test
    public void testCalcularImpuestosImportacionNormal() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);
        u.setPerfil("ESTANDAR");

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.80"), "FOB"
        );

        assertEquals(0, new BigDecimal("11150").compareTo(res.getCif()));
        assertEquals(0, new BigDecimal("669").compareTo(res.getArancel()));
        assertEquals(0, new BigDecimal("1891.04").compareTo(res.getIgv()));
        assertEquals(0, new BigDecimal("236.38").compareTo(res.getIpm()));
        assertEquals(0, new BigDecimal("488.12").compareTo(res.getPercepcion()));
    }

    @Test
    public void testCalcularImpuestosBuenContribuyente() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(true);

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.80"), "FOB"
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(res.getPercepcion()));
        assertTrue(res.getMensajePercepcion().contains("exento"));
    }

    @Test
    public void testCalcularImpuestosPrimeraImportacion() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);
        u.setPerfil("PRIMERA_IMPORTACION");

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.80"), "FOB"
        );

        assertEquals(0, new BigDecimal("1394.64").compareTo(res.getPercepcion()));
        assertTrue(res.getMensajePercepcion().contains("10%"));
    }

    @Test
    public void testCalcularImpuestosTlcChinaAplicado() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));
        hs.setTlcChina(true);

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "CN", new BigDecimal("3.80"), "FOB"
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(res.getArancel()));
        assertTrue(res.getMensajeTlc().contains("TLC Perú-China aplicado"));
    }

    @Test
    public void testCalcularImpuestosTlcChinaNoAplicado() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));
        hs.setTlcChina(false);

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "CN", new BigDecimal("3.80"), "FOB"
        );

        assertEquals(0, new BigDecimal("669").compareTo(res.getArancel()));
        assertTrue(res.getMensajeTlc() == null || res.getMensajeTlc().contains("No se encontró TLC"));
    }

    @Test
    public void testCalcularImpuestosIncotermCIF() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);

        BigDecimal fob = new BigDecimal("12150");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.80"), "CIF"
        );

        assertEquals(0, new BigDecimal("12150").compareTo(res.getCif()));
    }

    @Test
    public void testCalcularImpuestosMercanciaUsada() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);
        u.setPerfil("ESTANDAR");

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.80"), "FOB", true
        );

        assertEquals(0, new BigDecimal("1394.64").compareTo(res.getPercepcion()));
        assertTrue(res.getMensajePercepcion().contains("Mercancía Usada"));
    }

    @Test
    public void testCalcularImpuestosMercanciaUsadaBuenContribuyente() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(true);
        u.setPerfil("ESTANDAR");

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.80"), "FOB", true
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(res.getPercepcion()));
        assertTrue(res.getMensajePercepcion().contains("exento"));
    }

    @Test
    public void testCalcularImpuestosTlcChinaConAdvertencia() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));
        hs.setTlcChina(true);

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "CN", new BigDecimal("3.80"), "FOB"
        );

        assertTrue(res.getMensajeTlc().contains("arancelaria"));
    }

    @Test
    public void testBigDecimalExactnessAcrossTributos() {
        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("11"));
        hs.setIsc(new BigDecimal("0"));
        hs.setIgv(new BigDecimal("16"));

        Usuario u = new Usuario();
        u.setBuenContribuyente(false);
        u.setPerfil("ESTANDAR");

        BigDecimal fob = new BigDecimal("9999.99");
        BigDecimal flete = new BigDecimal("0.01");
        BigDecimal seguro = new BigDecimal("0.00");

        CalculadoraTributaria.ResultadoTributario res = CalculadoraTributaria.calcularImpuestos(
            hs, u, fob, flete, seguro, "USA", new BigDecimal("3.75"), "FOB"
        );

        assertEquals(0, fob.add(flete).add(seguro).compareTo(res.getCif()));
        assertFalse(res.getCif().toString().contains("999999"));
    }
}
