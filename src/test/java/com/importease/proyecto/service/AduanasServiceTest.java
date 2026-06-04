package com.importease.proyecto.service;

import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionDAO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AduanasServiceTest {

    private final AduanasService service = new AduanasService();

    @Test
    public void selectorRegimenCubreCincoEscenarios() {
        assertEquals("10", service.evaluarRegimen(Map.of("destino", "PERU")).get("regimenCodigo"));
        assertEquals("80", service.evaluarRegimen(Map.of("destino", "TRANSITO")).get("regimenCodigo"));
        assertEquals("ADM_TEMP", service.evaluarRegimen(Map.of("destino", "TEMPORAL")).get("regimenCodigo"));
        assertEquals("36", service.evaluarRegimen(Map.of("destino", "REIMPORTACION")).get("regimenCodigo"));
        assertEquals("TRANSBORDO", service.evaluarRegimen(Map.of("destino", "TRANSBORDO")).get("regimenCodigo"));
    }

    @Test
    public void modalidadImportacionMarcaExcepciones() {
        Map<String, Object> result = service.evaluarModalidad(Map.of(
                "regimenCodigo", "10",
                "fob", 1500,
                "restringida", false
        ));

        assertEquals("DIFERIDO", result.get("modalidadCodigo"));
        assertTrue(String.valueOf(result.get("motivo")).contains("FOB menor"));
    }

    @Test
    public void reimportacionExigePlazoYMismoEstado() {
        Map<String, Object> ok = service.evaluarReimportacion(Map.of(
                "exportadaDesdePeru", true,
                "exportacionRegularizada", true,
                "mesesDesdeEmbarque", 8,
                "transformada", false,
                "beneficioExportacion", false
        ));
        assertEquals(true, ok.get("procede"));
        assertEquals("ROJO", ok.get("canalProbable"));

        Map<String, Object> bad = service.evaluarReimportacion(Map.of(
                "exportadaDesdePeru", true,
                "exportacionRegularizada", true,
                "mesesDesdeEmbarque", 14,
                "transformada", false
        ));
        assertEquals(false, bad.get("procede"));
    }

    @Test
    public void transbordoDetectaObservacionesDeRegularizacion() {
        Map<String, Object> result = service.evaluarTransbordo(Map.of(
                "modalidad", "M3_DEPOSITO",
                "diferenciaPesoMayor2", true,
                "precintoViolado", false,
                "solicitudesPendientes", false
        ));

        assertEquals(false, result.get("regularizable"));
        assertTrue(String.valueOf(result.get("alertas")).contains("peso"));
    }

    @Test
    public void dtaMapUsaBigDecimalYPreservaPrecision() throws Exception {
        Importacion imp = new Importacion();
        imp.setValorCifBD(new BigDecimal("100.25"));
        imp.setTipoCambioBD(new BigDecimal("3.725"));
        imp.setMontoAdValoremBD(new BigDecimal("6.015"));
        imp.setMontoIscBD(BigDecimal.ZERO);
        imp.setMontoIgbBD(new BigDecimal("8.10"));
        imp.setMontoIpmBD(new BigDecimal("0.40"));
        imp.setMontoPercepcionBD(new BigDecimal("1.20"));
        imp.setTotalImpuestosBD(new BigDecimal("15.71"));

        Method method = AduanasService.class.getDeclaredMethod("dtaMap", Importacion.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> dta = (Map<String, Object>) method.invoke(service, imp);

        assertInstanceOf(BigDecimal.class, dta.get("baseCifUsd"));
        assertInstanceOf(BigDecimal.class, dta.get("tipoCambio"));
        assertInstanceOf(BigDecimal.class, dta.get("baseCifPen"));
        assertEquals(new BigDecimal("373.43"), dta.get("baseCifPen"));
    }

    @Test
    public void buildAlertasGeneraAgenteAduanaMandatorioAlSuperarFob2000() throws Exception {
        Importacion imp = new Importacion();
        imp.setValorFob(2500.00);
        imp.setHsCode("8517130000");

        Map<String, Object> regimen = Map.of("regimenCodigo", "10");
        Map<String, Object> modalidad = Map.of("modalidadCodigo", "ANTICIPADO");

        Method method = AduanasService.class.getDeclaredMethod("buildAlertas", Importacion.class, Map.class, Map.class, boolean.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alertas = (List<Map<String, Object>>) method.invoke(service, imp, regimen, modalidad, false);

        boolean found = false;
        for (Map<String, Object> alerta : alertas) {
            if ("AGENTE_ADUANA_MANDATORIO".equals(alerta.get("tipo"))) {
                found = true;
                assertEquals("ALTA", alerta.get("severidad"));
                assertTrue(String.valueOf(alerta.get("mensaje")).contains("FOB supera los USD 2000"));
                assertTrue(String.valueOf(alerta.get("baseLegal")).contains("LGA Art. 21"));
            }
        }
        assertTrue(found, "Debe generar la alerta de Agente de Aduanas Mandatorio.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void obtenerTimelineRejectsOtherUserOperation() throws java.sql.SQLException {
        try (var mockedDB = mockStatic(ConexionDB.class)) {
            Connection mockConn = mock(Connection.class);
            mockedDB.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            ImportacionDAO mockDAO = mock(ImportacionDAO.class);
            Importacion imp = new Importacion();
            imp.setId(999);
            imp.setUsuarioId(2);

            doReturn(imp).when(mockDAO).buscarPorId(mockConn, 999);

            AduanasService svc = new AduanasService(mockDAO, mock(EventoUsuarioService.class), mock(FuenteEventoService.class));

            List<Map<String, Object>> result = svc.obtenerTimeline(1, 999);
            assertTrue(result.isEmpty(), "Timeline debe retornar vacÃ­o para operaciÃ³n de otro usuario");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void obtenerAlertasRejectsOtherUserOperation() throws java.sql.SQLException {
        try (var mockedDB = mockStatic(ConexionDB.class)) {
            Connection mockConn = mock(Connection.class);
            mockedDB.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            ImportacionDAO mockDAO = mock(ImportacionDAO.class);
            Importacion imp = new Importacion();
            imp.setId(888);
            imp.setUsuarioId(2);

            doReturn(imp).when(mockDAO).buscarPorId(mockConn, 888);

            AduanasService svc = new AduanasService(mockDAO, mock(EventoUsuarioService.class), mock(FuenteEventoService.class));

            List<Map<String, Object>> result = svc.obtenerAlertas(1, 888);
            assertTrue(result.isEmpty(), "Alertas debe retornar vacÃ­o para operaciÃ³n de otro usuario");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void obtenerBaseLegalRejectsOtherUserOperation() throws java.sql.SQLException {
        try (var mockedDB = mockStatic(ConexionDB.class)) {
            Connection mockConn = mock(Connection.class);
            mockedDB.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            ImportacionDAO mockDAO = mock(ImportacionDAO.class);
            Importacion imp = new Importacion();
            imp.setId(777);
            imp.setUsuarioId(3);

            doReturn(imp).when(mockDAO).buscarPorId(mockConn, 777);

            AduanasService svc = new AduanasService(mockDAO, mock(EventoUsuarioService.class), mock(FuenteEventoService.class));

            List<Map<String, Object>> result = svc.obtenerBaseLegal(1, 777);
            assertTrue(result.isEmpty(), "Base Legal debe retornar vacÃ­o para operaciÃ³n de otro usuario");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void obtenerExpedienteRejectsOtherUserOperation() throws java.sql.SQLException {
        try (var mockedDB = mockStatic(ConexionDB.class)) {
            Connection mockConn = mock(Connection.class);
            mockedDB.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            ImportacionDAO mockDAO = mock(ImportacionDAO.class);
            Importacion imp = new Importacion();
            imp.setId(666);
            imp.setUsuarioId(4);

            doReturn(imp).when(mockDAO).buscarPorId(mockConn, 666);

            AduanasService svc = new AduanasService(mockDAO, mock(EventoUsuarioService.class), mock(FuenteEventoService.class));

            Map<String, Object> result = svc.obtenerExpediente(1, 666);
            assertTrue(result.containsKey("errorCode"), "Expediente debe retornar error para operaciÃ³n de otro usuario");
        }
    }
}


