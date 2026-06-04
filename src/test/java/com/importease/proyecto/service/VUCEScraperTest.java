package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class VUCEScraperTest {

    @Test
    public void testGetDatosDigesa() {
        VUCEScraper scraper = new VUCEScraper();
        Map<String, String> datos = scraper.getDatosDigesa();

        assertNotNull(datos);
        assertEquals("DIGESA", datos.get("nombre"));
        assertTrue(datos.containsKey("nombre_completo"));
        assertTrue(datos.containsKey("costo_tupa"));
        assertTrue(datos.containsKey("tiempo_dias"));
        assertTrue(datos.get("checklist_1").contains("SUCE"));
    }

    @Test
    public void testGetDatosMTC() {
        VUCEScraper scraper = new VUCEScraper();
        Map<String, String> datos = scraper.getDatosMTC();

        assertNotNull(datos);
        assertEquals("MTC", datos.get("nombre"));
        assertEquals("Gratuito (tramitado vÃ­a VUCE)", datos.get("costo_tupa"));
        assertTrue(datos.containsKey("checklist_1"));
    }

    @Test
    public void testGetDatosDigemid() {
        VUCEScraper scraper = new VUCEScraper();
        Map<String, String> datos = scraper.getDatosDigemid();

        assertNotNull(datos);
        assertEquals("DIGEMID", datos.get("nombre"));
        assertEquals("ALTA", datos.get("complejidad"));
    }

    @Test
    public void testGetDatosSenasa() {
        VUCEScraper scraper = new VUCEScraper();
        Map<String, String> datos = scraper.getDatosSenasa();

        assertNotNull(datos);
        assertEquals("SENASA", datos.get("nombre"));
        assertEquals("BAJA", datos.get("complejidad"));
    }

    @Test
    public void testGetDatosPorEntidad() {
        VUCEScraper scraper = new VUCEScraper();

        Map<String, String> digesa = scraper.getDatosPorEntidad("digesa");
        assertEquals("DIGESA", digesa.get("nombre"));

        Map<String, String> mtc = scraper.getDatosPorEntidad("MTC");
        assertEquals("MTC", mtc.get("nombre"));

        Map<String, String> invalida = scraper.getDatosPorEntidad("OTRA_ENTIDAD");
        assertEquals("OTRA_ENTIDAD", invalida.get("nombre"));
        assertEquals("Entidad reguladora gubernamental", invalida.get("nombre_completo"));
    }
}


