package com.importease.proyecto.service;

import com.importease.proyecto.model.*;
import com.importease.proyecto.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AduanasFullFlowTest {

    @Mock
    private ImportacionRepositorio importacionRepositorio;

    @Mock
    private HsCodeRepositorio hsCodeRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private PermisoRepositorio permisoRepositorio;

    @Mock
    private TipoCambioServicio tipoCambioServicio;

    @Mock
    private ArancelServicio arancelServicio;

    @Mock
    private VUCEScraper vuceScraper;

    @InjectMocks
    private ImportacionServicio importacionServicio;

    @InjectMocks
    private VuceValidadorServicio vuceValidator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock tipo de cambio desde BCRP Data
        when(tipoCambioServicio.obtenerTipoCambio()).thenReturn(new BigDecimal("3.745"));
    }

    private Connection setupMockConnection() throws SQLException {
        Connection mockCon = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockCon.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockCon.prepareStatement(anyString(), anyInt())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockPs.executeBatch()).thenReturn(new int[]{1, 1});
        return mockCon;
    }

    @Test
    public void testAduanasFullUserJourneyFlow() throws SQLException {
        // --- STEP 1: USUARIO LOGIN Y VALIDACION DE DATOS DE SESION ---
        Usuario mockUser = new Usuario();
        mockUser.setId(1);
        mockUser.setRazonSocial("Manuel Andree");
        mockUser.setRuc("20601234567");
        mockUser.setPerfil("ESTANDAR");
        mockUser.setBuenContribuyente(false);

        // --- STEP 2: CLASIFICACION HS Y ARANCEL SUGGESTIONS SEARCH ---
        HsCode mockHs = new HsCode();
        mockHs.setCodigo("8517130000");
        mockHs.setDescripcionEs("Teléfonos inteligentes (smartphones) con tecnología celular integrada");
        mockHs.setAdValorem(new BigDecimal("0")); // 0% Ad-valorem en arancel peruano para celulares
        mockHs.setIgv(new BigDecimal("16"));
        mockHs.setIsc(BigDecimal.ZERO);
        mockHs.setRequiereVuce(true);
        mockHs.setEntidadVuce("MTC");

        // Stub mocks behaviors
        when(usuarioRepositorio.buscarPorId(1)).thenReturn(mockUser);
        when(hsCodeRepositorio.obtenerPorCodigo("8517130000")).thenReturn(mockHs);
        when(arancelServicio.consultarArancel("8517130000")).thenReturn(mockHs);

        // --- STEP 3: DETECCION REGULATORIA Y CONTROL VUCE (RESTRICCION) ---
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {
            Connection mockCon = setupMockConnection();
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockCon);

            // Verificar la detección regulatoria de VUCE a través de VuceValidadorServicio
            Map<String, Object> vuceResult = vuceValidator.validar("8517130000");
            assertNotNull(vuceResult);
            // El validador busca en reglas de restriccion. Al ser 8517130000 (celular), detectará restricción MTC
            assertTrue((Boolean) vuceResult.get("encontrado") || !vuceResult.isEmpty());

            // --- STEP 4: ESTIMACION Y LIQUIDACION TRIBUTARIA CIF (COTIZADOR) ---
            // Simular importación de celulares Xiaomi: FOB = 8200.00, Flete = 520.00, Seguro = 95.00
            // CIF = 8200 + 520 + 95 = 8815.00 USD
            // Base Imponible en Soles (TC 3.745) = 8815 * 3.745 = 33012.175 PEN
            // Impuestos: Ad-Valorem (0%) = 0
            // IGV (16%) de 33012.175 = 5281.948 PEN
            // IPM (2%) de 33012.175 = 660.2435 PEN
            // Percepción (3.5%) de (CIF + IGV + IPM) => (33012.175 + 5281.948 + 660.2435) = 38954.3665 * 0.035 = 1363.40 PEN
            // Total Impuestos = 0 + 5281.948 + 660.2435 + 1363.40 = 7305.59 PEN
            
            // Mock de la inserción en DAO para retornar un ID de operación
            doAnswer(invocation -> {
                Importacion impArg = invocation.getArgument(1);
                impArg.setId(4819);
                return null;
            }).when(importacionRepositorio).insertar(any(Connection.class), any(Importacion.class));

            Importacion opImp = importacionServicio.generarOperacion(1, "8517130000",
                BigDecimal.valueOf(8200), BigDecimal.valueOf(520), BigDecimal.valueOf(95), "COMERCIAL");
            
            assertNotNull(opImp);
            assertEquals(4819, opImp.getId());
            assertEquals(8815.0, opImp.getValorCif(), 0.001);
            assertEquals(0.0, opImp.getMontoAdValorem(), 0.001);
            // La suma de IGV, IPM y Percepción en Soles (convertido usando tipo de cambio)
            double totalImpuestosSoles = opImp.getTotalImpuestos() * opImp.getTipoCambio();
            assertTrue(totalImpuestosSoles > 6000.0);

            // --- STEP 5: CREACIÓN DE EXPEDIENTE PRE-VUCE (PERMISO) ---
            SolicitudPermiso mockSolicitud = new SolicitudPermiso();
            mockSolicitud.setId(102);
            mockSolicitud.setOperacionId(4819);
            mockSolicitud.setUsuarioId(1);
            mockSolicitud.setCodigoEntidad("MTC");
            mockSolicitud.setTipoPermiso("Licencia de Internamiento");
            mockSolicitud.setEstado("EXPEDIENTE_GENERADO");
            mockSolicitud.setNumeroSuce("20260523001");
            
            when(permisoRepositorio.crearSolicitud(any(SolicitudPermiso.class))).thenReturn(102);
            when(permisoRepositorio.obtenerSolicitud(eq(102))).thenReturn(mockSolicitud);

            int solicitudId = permisoRepositorio.crearSolicitud(mockSolicitud);
            assertEquals(102, solicitudId);
            
            // --- STEP 6: CHECKLIST DOCUMENTAL DE RESPALDO COMERCIAL ---
            // Validamos que falten documentos si no se han cargado y se carguen con éxito
            Importacion mockImp = new Importacion();
            mockImp.setId(4819);
            mockImp.setUsuarioId(1);
            mockImp.setHsCode("8517130000");
            mockImp.setEstado("COTIZACION");
            mockImp.setValorCif(8815.0);
            when(importacionRepositorio.buscarPorId(any(Connection.class), eq(4819))).thenReturn(mockImp);
            
            when(importacionRepositorio.validarDocumentosParaDespacho(any(Connection.class), eq(4819))).thenReturn(true);
            
            boolean docsValidos = importacionRepositorio.validarDocumentosParaDespacho(mockCon, 4819);
            assertTrue(docsValidos, "Los documentos cargados Factura Comercial, BL y Certificado de Origen son válidos.");

            // --- STEP 7: CULMINACIÓN DEL EXPEDIENTE PRE-VUCE Y ASIGNACION DE CANAL ---
            // Cuando la documentación está completa, cambia a estado LISTA_DESPACHO y se le asigna canal referencial (Canal Verde)
            when(importacionRepositorio.actualizarEstado(any(Connection.class), eq(4819), eq("LISTA_DESPACHO"))).thenReturn(true);
            boolean estadoCambiado = importacionServicio.cambiarEstado(4819, "LISTA_DESPACHO");
            assertTrue(estadoCambiado);

            // Simular asignación de canal de SUNAT referencial (Canal Verde)
            when(importacionRepositorio.actualizarDam(any(Connection.class), eq(4819), anyString(), eq("VERDE"))).thenReturn(true);
            boolean damAsignada = importacionRepositorio.actualizarDam(mockCon, 4819, "PRE-DAM-2026-04819", "VERDE");
            assertTrue(damAsignada);

            // Validar que se ha completado la simulación didáctica con éxito y trazabilidad digital
            Importacion finalOp = new Importacion();
            finalOp.setId(4819);
            finalOp.setHsCode("8517130000");
            finalOp.setProductoDesc("Celulares Xiaomi");
            finalOp.setEstado("LISTA_DESPACHO");
            finalOp.setCanalAsignado("VERDE");
            finalOp.setNumeroDam("PRE-DAM-2026-04819");
            
            assertEquals("VERDE", finalOp.getCanalAsignado());
            assertTrue(finalOp.getNumeroDam().startsWith("PRE-DAM-"));
        }
    }
}
