package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;
import com.importease.proyecto.model.HsCode;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ArancelScraperTest {

    private Connection setupMockConnection(Document mockDoc) throws IOException {
        Connection mockCon = mock(Connection.class);
        when(mockCon.userAgent(anyString())).thenReturn(mockCon);
        when(mockCon.timeout(anyInt())).thenReturn(mockCon);
        when(mockCon.get()).thenReturn(mockDoc);
        return mockCon;
    }

    @Test
    public void testArancelScraperCelularesConMock() throws IOException {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            // Document para scrapearPartida
            Document mockDoc1 = mock(Document.class);
            Element mockDesc = mock(Element.class);
            when(mockDesc.text()).thenReturn("TelÃ©fonos inteligentes");
            when(mockDoc1.selectFirst(anyString())).thenReturn(mockDesc);
            
            // Simular filas de impuestos
            Elements rows = new Elements();
            Element row1 = mock(Element.class);
            when(row1.text()).thenReturn("Ad / Valorem 0%");
            rows.add(row1);
            when(mockDoc1.select("tr")).thenReturn(rows);

            // Document para detectarVUCE (que fallarÃ¡ para forzar el fallback de capÃ­tulo)
            Connection mockCon1 = setupMockConnection(mockDoc1);
            
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockCon1);

            ArancelScraper scraper = new ArancelScraper();
            HsCode hs = scraper.scrapearPartida("8517130000");

            assertNotNull(hs);
            assertEquals("8517130000", hs.getCodigo());
            // Al fallar la segunda llamada (VUCE), debe aplicar el fallback de capÃ­tulo
            assertTrue(hs.isRequiereVuce());
            assertEquals("MTC", hs.getEntidadVuce());
        }
    }

    @Test
    public void testArancelScraperCosmeticosConMock() throws IOException {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            Document mockDoc1 = mock(Document.class);
            Element mockDesc = mock(Element.class);
            when(mockDesc.text()).thenReturn("CosmÃ©ticos de belleza");
            when(mockDoc1.selectFirst(anyString())).thenReturn(mockDesc);
            
            Elements rows = new Elements();
            Element row1 = mock(Element.class);
            when(row1.text()).thenReturn("Ad-valorem 6%");
            rows.add(row1);
            when(mockDoc1.select("tr")).thenReturn(rows);

            Connection mockCon1 = setupMockConnection(mockDoc1);
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockCon1);

            ArancelScraper scraper = new ArancelScraper();
            HsCode hs = scraper.scrapearPartida("3304990000");

            assertNotNull(hs);
            assertEquals("3304990000", hs.getCodigo());
            assertTrue(hs.isRequiereVuce());
            assertEquals("DIGESA", hs.getEntidadVuce());
        }
    }

    @Test
    public void testArancelScraperLaptopsConMock() throws IOException {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            Document mockDoc1 = mock(Document.class);
            Element mockDesc = mock(Element.class);
            when(mockDesc.text()).thenReturn("Laptops portÃ¡tiles");
            when(mockDoc1.selectFirst(anyString())).thenReturn(mockDesc);
            
            Elements rows = new Elements();
            Element row1 = mock(Element.class);
            when(row1.text()).thenReturn("Ad-valorem 0%");
            rows.add(row1);
            when(mockDoc1.select("tr")).thenReturn(rows);

            Connection mockCon1 = setupMockConnection(mockDoc1);
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockCon1);

            ArancelScraper scraper = new ArancelScraper();
            HsCode hs = scraper.scrapearPartida("8471301000");

            assertNotNull(hs);
            assertEquals("8471301000", hs.getCodigo());
            assertFalse(hs.isRequiereVuce());
        }
    }

    @Test
    public void testBuscarPorDescripcionConMock() throws IOException {
        try (MockedStatic<Jsoup> mockedJsoup = Mockito.mockStatic(Jsoup.class)) {
            Document mockDoc = mock(Document.class);
            Elements rows = new Elements();
            Element row1 = mock(Element.class);
            Elements cells = new Elements();
            Element cell1 = mock(Element.class);
            when(cell1.text()).thenReturn("8517130000");
            Element cell2 = mock(Element.class);
            when(cell2.text()).thenReturn("Celulares inteligentes");
            cells.add(cell1);
            cells.add(cell2);
            when(row1.select("td")).thenReturn(cells);
            rows.add(row1);
            
            when(mockDoc.select(anyString())).thenReturn(rows);

            Connection mockCon = setupMockConnection(mockDoc);
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockCon);

            ArancelScraper scraper = new ArancelScraper();
            List<HsCode> resultados = scraper.buscarPorDescripcion("Celulares");
            
            assertNotNull(resultados);
            assertFalse(resultados.isEmpty());
            assertEquals("8517130000", resultados.get(0).getCodigo());
        }
    }
}


