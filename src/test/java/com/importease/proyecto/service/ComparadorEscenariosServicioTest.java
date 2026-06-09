package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ComparadorEscenariosServicioTest {

    @Test
    public void testCompararPerfilesFiscales() {
        ComparadorEscenariosServicio service = new ComparadorEscenariosServicio();

        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        Map<String, CalculadoraTributaria.ResultadoTributario> comparacion = service.compararPerfilesFiscales(
            hs, fob, flete, seguro, "USA", new BigDecimal("3.80"), "FOB"
        );

        assertNotNull(comparacion);
        assertEquals(3, comparacion.size());
        assertTrue(comparacion.containsKey("ESTANDAR"));
        assertTrue(comparacion.containsKey("PRIMERA_IMPORTACION"));
        assertTrue(comparacion.containsKey("BUEN_CONTRIBUYENTE"));

        // Buen contribuyente debe tener percepciÃ³n igual a cero
        assertEquals(0, BigDecimal.ZERO.compareTo(comparacion.get("BUEN_CONTRIBUYENTE").getPercepcion()));

        // Primera importaciÃ³n debe tener una percepciÃ³n del 10% (mayor que estÃ¡ndar 3.5%)
        BigDecimal percEstandar = comparacion.get("ESTANDAR").getPercepcion();
        BigDecimal percPrimera = comparacion.get("PRIMERA_IMPORTACION").getPercepcion();
        assertTrue(percPrimera.compareTo(percEstandar) > 0);
    }

    @Test
    public void testCompararTlcChina() {
        ComparadorEscenariosServicio service = new ComparadorEscenariosServicio();

        HsCode hs = new HsCode();
        hs.setAdValorem(new BigDecimal("6")); // Tasa estÃ¡ndar
        hs.setIsc(BigDecimal.ZERO);
        hs.setIgv(new BigDecimal("16"));

        BigDecimal fob = new BigDecimal("10000");
        BigDecimal flete = new BigDecimal("1000");
        BigDecimal seguro = new BigDecimal("150");

        Map<String, CalculadoraTributaria.ResultadoTributario> comparacion = service.compararTlcChina(
            hs, null, fob, flete, seguro, new BigDecimal("3.80"), "FOB"
        );

        assertNotNull(comparacion);
        assertEquals(2, comparacion.size());
        assertTrue(comparacion.containsKey("CON_TLC_CHINA"));
        assertTrue(comparacion.containsKey("SIN_TLC_CHINA"));

        // El escenario CON TLC debe tener arancel igual a cero
        assertEquals(0, BigDecimal.ZERO.compareTo(comparacion.get("CON_TLC_CHINA").getArancel()));

        // El escenario SIN TLC debe tener arancel mayor a cero (aplicÃ³ el 6% de Ad-Valorem)
        assertTrue(comparacion.get("SIN_TLC_CHINA").getArancel().compareTo(BigDecimal.ZERO) > 0);
    }
}


