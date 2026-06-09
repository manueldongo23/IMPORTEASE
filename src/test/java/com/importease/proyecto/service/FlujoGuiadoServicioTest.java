package com.importease.proyecto.service;

import com.importease.proyecto.dto.PasoGuiadoDTO;
import com.importease.proyecto.dto.SiguienteAccionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class FlujoGuiadoServicioTest {

    private FlujoGuiadoServicio flujoGuiadoServicio;

    @BeforeEach
    public void setUp() throws Exception {
        javax.sql.DataSource mockDataSource = org.mockito.Mockito.mock(javax.sql.DataSource.class);
        java.sql.Connection mockConnection = org.mockito.Mockito.mock(java.sql.Connection.class);
        java.sql.PreparedStatement mockStatement = org.mockito.Mockito.mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet mockResultSet = org.mockito.Mockito.mock(java.sql.ResultSet.class);

        lenient().when(mockDataSource.getConnection()).thenReturn(mockConnection);
        lenient().when(mockConnection.prepareStatement(org.mockito.Mockito.anyString())).thenReturn(mockStatement);
        lenient().when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        lenient().when(mockResultSet.next()).thenReturn(false);

        ConexionDB.setSpringManagedDataSource(mockDataSource);

        flujoGuiadoServicio = new FlujoGuiadoServicio();
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        ConexionDB.setSpringManagedDataSource(null);
    }

    @Test
    public void testObtenerPasoActualReturnsStep1ForNewExpediente() {
        PasoGuiadoDTO paso = flujoGuiadoServicio.obtenerPasoActual(99999);
        assertNotNull(paso, "El paso no debe ser nulo");
        assertTrue(paso.getStep() >= 1, "Un expediente nuevo debe estar en el paso 1 o superior");
        assertTrue(paso.getPorcentajeGlobal().compareTo(BigDecimal.ZERO) >= 0, "El porcentaje no debe ser negativo");
    }

    @Test
    public void testObtenerNombrePasoValido() {
        assertEquals("IntenciÃƒÂ³n", flujoGuiadoServicio.obtenerNombrePaso(1));
        assertEquals("Datos BÃƒÂ¡sicos", flujoGuiadoServicio.obtenerNombrePaso(2));
        assertEquals("ClasificaciÃƒÂ³n (HS Code)", flujoGuiadoServicio.obtenerNombrePaso(3));
        assertEquals("Transporte y Manifiesto", flujoGuiadoServicio.obtenerNombrePaso(4));
        assertEquals("Checklist Documental", flujoGuiadoServicio.obtenerNombrePaso(5));
        assertEquals("ValidaciÃƒÂ³n de Coherencia", flujoGuiadoServicio.obtenerNombrePaso(6));
        assertEquals("CÃƒÂ¡lculo DTA y PRE-DAM", flujoGuiadoServicio.obtenerNombrePaso(7));
        assertEquals("RevisiÃƒÂ³n Final del Expediente", flujoGuiadoServicio.obtenerNombrePaso(8));
    }

    @Test
    public void testDescripcionPrincipianteVsExperto() {
        String principiante = flujoGuiadoServicio.obtenerDescripcionPaso(1, true);
        String experto = flujoGuiadoServicio.obtenerDescripcionPaso(1, false);
        assertNotNull(principiante);
        assertNotNull(experto);
        assertTrue(principiante.length() >= experto.length(),
            "La descripciÃƒÂ³n para principiantes debe ser mÃƒÂ¡s larga o igual que para expertos");
    }

    @Test
    public void testPasoCompletoInicialmenteFalso() {
        boolean completo = flujoGuiadoServicio.isPasoCompleto(99999, 1);
        assertFalse(completo, "Un expediente nuevo no debe tener ningÃƒÂºn paso completo inicialmente");
    }

    @Test
    public void testNextActionServiceReturnsCorrectActionPerStep() {
        SiguienteAccionServicio nas = new SiguienteAccionServicio();
        SiguienteAccionDTO accionInicial = nas.calcularSiguienteAccion(99999);
        assertNotNull(accionInicial, "La acciÃƒÂ³n no debe ser nula");
        assertTrue(accionInicial.getPaso() >= 1, "El paso de acciÃƒÂ³n debe ser al menos 1");
    }
}

