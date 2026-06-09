package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.HsCodeRepositorio;
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.repository.UsuarioRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ImportacionServicioTest {

    @Mock
    private ImportacionRepositorio importacionRepositorio;

    @Mock
    private TipoCambioServicio tipoCambioServicio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private HsCodeRepositorio hsCodeRepositorio;

    @InjectMocks
    private ImportacionServicio importacionServicio;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock tipo de cambio to return a fixed exchange rate
        when(tipoCambioServicio.obtenerTipoCambio()).thenReturn(new BigDecimal("3.80"));
    }

    private Connection setupMockConnection() throws SQLException {
        Connection mockCon = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockCon.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockCon.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
        when(mockPs.executeBatch()).thenReturn(new int[]{1, 1, 1});
        return mockCon;
    }

    @Test
    public void testGenerarOperacionPersonalBajo200() throws SQLException {
        Usuario u = new Usuario();
        u.setId(1);
        u.setBuenContribuyente(false);
        u.setPerfil("ESTANDAR");

        HsCode hs = new HsCode();
        hs.setCodigo("8517130000");
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setRequiereVuce(false);

        when(usuarioRepositorio.buscarPorId(1)).thenReturn(u);
        when(hsCodeRepositorio.obtenerPorCodigo("8517130000")).thenReturn(hs);

        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {
            Connection mockCon = setupMockConnection();
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockCon);

            Importacion imp = importacionServicio.generarOperacion(1, "8517130000",
                BigDecimal.valueOf(150), BigDecimal.valueOf(30), BigDecimal.valueOf(5), "PERSONAL");

            assertNotNull(imp);
            assertEquals(0.0, imp.getMontoAdValorem(), 0.001);
            assertEquals(0.0, imp.getMontoIgb(), 0.001);
            assertEquals(0.0, imp.getMontoIpm(), 0.001);
            assertEquals(0.0, imp.getMontoPercepcion(), 0.001);
            assertEquals(0.0, imp.getTotalImpuestos(), 0.001);
        }
    }

    @Test
    public void testGenerarOperacionPersonalBajo2000() throws SQLException {
        Usuario u = new Usuario();
        u.setId(1);
        u.setBuenContribuyente(false);
        u.setPerfil("ESTANDAR");

        HsCode hs = new HsCode();
        hs.setCodigo("8517130000");
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setRequiereVuce(false);

        when(usuarioRepositorio.buscarPorId(1)).thenReturn(u);
        when(hsCodeRepositorio.obtenerPorCodigo("8517130000")).thenReturn(hs);

        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {
            Connection mockCon = setupMockConnection();
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockCon);

            // Execute: FOB = 1000 (Category C, 4% Flat tariff + 16% IGV + 2% IPM)
            // CIF = 1000 + 100 + 20 = 1120
            // AdValorem (4%) = 44.80
            // Base IGV/IPM = 1120 + 44.80 = 1164.80
            // IGV (16%) = 186.368
            // IPM (2%) = 23.296
            Importacion imp = importacionServicio.generarOperacion(1, "8517130000",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100), BigDecimal.valueOf(20), "PERSONAL");

            assertNotNull(imp);
            assertEquals(44.80, imp.getMontoAdValorem(), 0.01);
            assertEquals(186.368, imp.getMontoIgb(), 0.01);
            assertEquals(23.296, imp.getMontoIpm(), 0.01);
            assertEquals(0.0, imp.getMontoPercepcion(), 0.01); // Courier Personal limits exemption
        }
    }

    @Test
    public void testGenerarOperacionComercialTasaEstandar() throws SQLException {
        Usuario u = new Usuario();
        u.setId(1);
        u.setBuenContribuyente(false);
        u.setPerfil("ESTANDAR");

        HsCode hs = new HsCode();
        hs.setCodigo("8517130000");
        hs.setAdValorem(new BigDecimal("6"));
        hs.setIgv(new BigDecimal("16"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setRequiereVuce(false);

        when(usuarioRepositorio.buscarPorId(1)).thenReturn(u);
        when(hsCodeRepositorio.obtenerPorCodigo("8517130000")).thenReturn(hs);

        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {
            Connection mockCon = setupMockConnection();
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockCon);

            // Execute: COMERCIAL import, FOB = 5000
            // CIF = 5000 + 300 + 50 = 5350
            // AdValorem (6%) = 321.0
            // Base IGV/IPM = 5350 + 321 = 5671
            // IGV (16%) = 907.36
            // IPM (2%) = 113.42
            // Percepción estándar (3.5%) de (Base IGV + IGV + IPM) => (5671 + 907.36 + 113.42) = 6691.78 * 0.035 = 234.21
            Importacion imp = importacionServicio.generarOperacion(1, "8517130000",
                BigDecimal.valueOf(5000), BigDecimal.valueOf(300), BigDecimal.valueOf(50), "COMERCIAL");

            assertNotNull(imp);
            assertEquals(321.0, imp.getMontoAdValorem(), 0.01);
            assertEquals(907.36, imp.getMontoIgb(), 0.01);
            assertEquals(113.42, imp.getMontoIpm(), 0.01);
            assertEquals(234.21, imp.getMontoPercepcion(), 0.01);
        }
    }

    @Test
    public void testGenerarOperacionPreDamPrefix() throws SQLException {
        Usuario u = new Usuario();
        u.setId(1);
        u.setBuenContribuyente(true); // Exento percepcion

        HsCode hs = new HsCode();
        hs.setCodigo("8517130000");
        hs.setAdValorem(new BigDecimal("0"));
        hs.setIsc(BigDecimal.ZERO);
        hs.setRequiereVuce(false);

        when(usuarioRepositorio.buscarPorId(1)).thenReturn(u);
        when(hsCodeRepositorio.obtenerPorCodigo("8517130000")).thenReturn(hs);

        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {
            Connection mockCon = setupMockConnection();
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockCon);

            // Mock DAO insertion to assign an ID so DAM generation triggers
            doAnswer(invocation -> {
                Importacion impArg = invocation.getArgument(1);
                impArg.setId(99);
                return null;
            }).when(importacionRepositorio).insertar(any(Connection.class), any(Importacion.class));

            Importacion imp = importacionServicio.generarOperacion(1, "8517130000",
                BigDecimal.valueOf(100), BigDecimal.valueOf(10), BigDecimal.valueOf(2), "COMERCIAL");

            assertNotNull(imp);
            assertNotNull(imp.getNumeroDam());
            assertTrue(imp.getNumeroDam().startsWith("PRE-DAM-"), "El número DAM debe iniciar con PRE-DAM- para ser considerado referencial/simulado.");
        }
    }

    @Test
    public void testCambiarEstadoNacionalizadaSinDocumentos() throws SQLException {
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {
            Connection mockCon = mock(Connection.class);
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockCon);

            Importacion imp = new Importacion();
            imp.setId(123);
            imp.setUsuarioId(1);
            imp.setEstado("COTIZACION");
            when(importacionRepositorio.buscarPorId(any(Connection.class), eq(123))).thenReturn(imp);

            // Mock that the documents validation fails
            when(importacionRepositorio.validarDocumentosParaDespacho(any(Connection.class), eq(123))).thenReturn(false);

            boolean success = importacionServicio.cambiarEstado(123, "NACIONALIZADA");

            assertFalse(success, "No se debe permitir la nacionalización si faltan documentos validados.");
            verify(importacionRepositorio, never()).actualizarEstado(any(Connection.class), eq(123), anyString());
        }
    }
}
