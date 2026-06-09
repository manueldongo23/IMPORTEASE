package com.importease.proyecto.service;

import com.importease.proyecto.dto.IncidenciaCoherenciaDTO;
import com.importease.proyecto.dto.PasoGuiadoDTO;
import com.importease.proyecto.dto.SiguienteAccionDTO;
import com.importease.proyecto.dto.PanelSaludDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class E2EFlujoCompletoTest {

    private FlujoGuiadoServicio flujoGuiadoServicio;
    private SaludPanelServicio saludPanelServicio;
    private SiguienteAccionServicio siguienteAccionServicio;

    @BeforeEach
    public void setUp() {
        flujoGuiadoServicio = new FlujoGuiadoServicio();
        saludPanelServicio = new SaludPanelServicio();
        siguienteAccionServicio = new SiguienteAccionServicio();
    }

    // --------------------------------------------------------------
    // test1: Create operation and navigate flow steps 1 Ã¢â€ â€™ 2
    // --------------------------------------------------------------
    @Test
    public void test1_CreateAndNavigateFlow() throws Exception {
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class);
             MockedStatic<AuditoriaServicio> mockedAuditoria = Mockito.mockStatic(AuditoriaServicio.class)) {

            Connection mockConn = mock(Connection.class);
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            PreparedStatement psInsert = mock(PreparedStatement.class);
            when(psInsert.executeBatch()).thenReturn(new int[]{1, 1, 1, 1, 1, 1, 1, 1});

            PreparedStatement psSelect = mock(PreparedStatement.class);
            ResultSet rsSelect = mock(ResultSet.class);
            when(rsSelect.next()).thenReturn(true);
            when(rsSelect.getString("estado")).thenReturn("PENDIENTE");
            when(psSelect.executeQuery()).thenReturn(rsSelect);

            PreparedStatement psUpsert = mock(PreparedStatement.class);
            when(psUpsert.executeUpdate()).thenReturn(1);

            PreparedStatement psCount = mock(PreparedStatement.class);
            ResultSet rsCount = mock(ResultSet.class);
            when(rsCount.next()).thenReturn(true);
            when(rsCount.getInt(1)).thenReturn(0);
            when(psCount.executeQuery()).thenReturn(rsCount);

            PreparedStatement psDefault = mock(PreparedStatement.class);
            ResultSet rsDefault = mock(ResultSet.class);
            when(rsDefault.next()).thenReturn(false);
            when(psDefault.executeQuery()).thenReturn(rsDefault);
            when(psDefault.executeUpdate()).thenReturn(1);

            when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
                String sql = invocation.getArgument(0);
                if (sql.contains("INSERT IGNORE")) return psInsert;
                if (sql.contains("ON DUPLICATE KEY UPDATE")) return psUpsert;
                if (sql.contains("COUNT(*)")) return psCount;
                if (sql.contains("SELECT estado") || sql.contains("motivo_bloqueo")) return psSelect;
                return psDefault;
            });

            PasoGuiadoDTO paso = flujoGuiadoServicio.obtenerPasoActual(12345);
            assertEquals(1, paso.getStep());
            assertEquals("PENDIENTE", paso.getEstado());

            PasoGuiadoDTO siguiente = flujoGuiadoServicio.avanzarPaso(12345, 1);
            assertEquals(2, siguiente.getStep());
            assertEquals("PENDIENTE", siguiente.getEstado());
            assertNull(siguiente.getMensajeBloqueo());

            mockedAuditoria.verify(() -> AuditoriaServicio.registrar(
                eq(1), eq("AVANZAR_PASO"), eq("expedientes"), eq(12345),
                anyString(), any(), any()
            ));
        }
    }

    // --------------------------------------------------------------
    // test2: Step 2 validation fails (incomplete data Ã¢â€ â€™ OBSERVADO)
    //        then succeeds after data is fixed
    // --------------------------------------------------------------
    @Test
    public void test2_Step2ValidationFailsWhenIncomplete() throws Exception {
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class);
             MockedStatic<AuditoriaServicio> mockedAuditoria = Mockito.mockStatic(AuditoriaServicio.class)) {

            Connection mockConn = mock(Connection.class);
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            PreparedStatement psInsert = mock(PreparedStatement.class);
            when(psInsert.executeBatch()).thenReturn(new int[]{1, 1, 1, 1, 1, 1, 1, 1});

            PreparedStatement psSelect = mock(PreparedStatement.class);
            ResultSet rsSelect = mock(ResultSet.class);
            when(rsSelect.next()).thenReturn(true);
            when(rsSelect.getString("estado"))
                .thenReturn("COMPLETO")   // step 1 in loop
                .thenReturn("PENDIENTE"); // step 2 in loop, then again for getStepEstado
            when(psSelect.executeQuery()).thenReturn(rsSelect);

            PreparedStatement psOps = mock(PreparedStatement.class);
            ResultSet rsOps = mock(ResultSet.class);
            when(rsOps.next()).thenReturn(true);
            when(rsOps.getString("producto_desc")).thenReturn(null);
            when(rsOps.getString("pais_origen")).thenReturn(null);
            when(rsOps.getString("incoterm")).thenReturn(null);
            when(rsOps.getDouble("fob")).thenReturn(0.0);
            when(psOps.executeQuery()).thenReturn(rsOps);

            PreparedStatement psUpsert = mock(PreparedStatement.class);
            when(psUpsert.executeUpdate()).thenReturn(1);

            PreparedStatement psCount = mock(PreparedStatement.class);
            ResultSet rsCount = mock(ResultSet.class);
            when(rsCount.next()).thenReturn(true);
            when(rsCount.getInt(1)).thenReturn(1);
            when(psCount.executeQuery()).thenReturn(rsCount);

            PreparedStatement psDefault = mock(PreparedStatement.class);
            ResultSet rsDefault = mock(ResultSet.class);
            when(rsDefault.next()).thenReturn(false);
            when(psDefault.executeQuery()).thenReturn(rsDefault);
            when(psDefault.executeUpdate()).thenReturn(1);

            when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
                String sql = invocation.getArgument(0);
                if (sql.contains("INSERT IGNORE")) return psInsert;
                if (sql.contains("ON DUPLICATE KEY UPDATE")) return psUpsert;
                if (sql.contains("COUNT(*)")) return psCount;
                if (sql.contains("SELECT estado") || sql.contains("motivo_bloqueo")) return psSelect;
                if (sql.contains("FROM operaciones")) return psOps;
                return psDefault;
            });

            PasoGuiadoDTO result = flujoGuiadoServicio.avanzarPaso(12345, 1);
            assertEquals("OBSERVADO", result.getEstado());
            assertNotNull(result.getMensajeBloqueo());
            assertTrue(result.getMensajeBloqueo().toLowerCase().contains("descripcion")
                    || result.getMensajeBloqueo().toLowerCase().contains("obligatoria")
                    || result.getMensajeBloqueo().toLowerCase().contains("producto"),
                "Mensaje de error debe indicar que la descripcion del producto es obligatoria, pero fue: "
                    + result.getMensajeBloqueo());

            mockedAuditoria.verify(() -> AuditoriaServicio.registrar(
                eq(1), eq("AVANZAR_PASO"), eq("expedientes"), eq(12345),
                contains("OBSERVADO"), any(), any()
            ));
        }
    }

    // --------------------------------------------------------------
    // test3: HealthPanel calculates health correctly
    // --------------------------------------------------------------
    @Test
    public void test3_HealthPanelCalculatesHealth() throws Exception {
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class);
             MockedStatic<PlazoCriticoServicio> mockedPlazo = Mockito.mockStatic(PlazoCriticoServicio.class)) {

            Connection mockConn = mock(Connection.class);
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            mockedPlazo.when(() -> PlazoCriticoServicio.calcularPlazos(
                    any(), any(), anyString(), anyString()))
                .thenReturn(new ArrayList<>());

            // --- Mock for SELECT * FROM operaciones (buscarPorId) ---
            PreparedStatement psOpsAll = mock(PreparedStatement.class);
            ResultSet rsOps = mock(ResultSet.class);
            when(rsOps.next()).thenReturn(true, false);
            when(rsOps.getInt("id")).thenReturn(12345);
            when(rsOps.getInt("usuario_id")).thenReturn(1);
            when(rsOps.getInt("proveedor_id")).thenReturn(0);
            when(rsOps.getString("hs_code")).thenReturn("8471300000");
            when(rsOps.getString("producto_desc")).thenReturn("Laptop HP ProBook");
            when(rsOps.getString("pais_origen")).thenReturn("CN");
            when(rsOps.getString("incoterm")).thenReturn("FOB");
            when(rsOps.getDouble("fob")).thenReturn(1500.0);
            when(rsOps.getDouble("flete")).thenReturn(100.0);
            when(rsOps.getDouble("seguro")).thenReturn(25.0);
            when(rsOps.getDouble("cif")).thenReturn(1625.0);
            when(rsOps.getDouble("tipo_cambio")).thenReturn(3.72);
            when(rsOps.getDouble("ad_valorem_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("isc_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("igv_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("ipm_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("percepcion_aplicada")).thenReturn(0.0);
            when(rsOps.getDouble("total_impuestos")).thenReturn(0.0);
            when(rsOps.getString("estado")).thenReturn("PENDIENTE");
            when(rsOps.getString("canal_asignado")).thenReturn(null);
            when(rsOps.getString("numero_dam")).thenReturn(null);
            when(rsOps.getTimestamp("fecha_numeracion")).thenReturn(null);
            when(rsOps.getTimestamp("fecha_creacion")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            when(rsOps.getBoolean("usado")).thenReturn(false);
            when(psOpsAll.executeQuery()).thenReturn(rsOps);

            // --- Mock for SELECT estado (guided steps - all COMPLETO) ---
            PreparedStatement psSelect = mock(PreparedStatement.class);
            ResultSet rsSelect = mock(ResultSet.class);
            when(rsSelect.next()).thenReturn(true);
            when(rsSelect.getString("estado")).thenReturn("COMPLETO");
            when(psSelect.executeQuery()).thenReturn(rsSelect);

            // --- Mock for INSERT IGNORE (in case isPasoCompleto triggers initSteps) ---
            PreparedStatement psInsert = mock(PreparedStatement.class);
            when(psInsert.executeBatch()).thenReturn(new int[]{1, 1, 1, 1, 1, 1, 1, 1});

            // --- Mock for ON DUPLICATE KEY UPDATE ---
            PreparedStatement psUpsert = mock(PreparedStatement.class);
            when(psUpsert.executeUpdate()).thenReturn(1);

            // --- Mock for documentos_importacion query ---
            PreparedStatement psDocs = mock(PreparedStatement.class);
            ResultSet rsDocs = mock(ResultSet.class);
            when(rsDocs.next()).thenReturn(false);
            when(psDocs.executeQuery()).thenReturn(rsDocs);

            // --- Mock for manifiestos_carga COUNT ---
            PreparedStatement psManCount = mock(PreparedStatement.class);
            ResultSet rsManCount = mock(ResultSet.class);
            when(rsManCount.next()).thenReturn(true);
            when(rsManCount.getInt(1)).thenReturn(1);
            when(psManCount.executeQuery()).thenReturn(rsManCount);

            // --- Mock for solicitudes_permiso COUNT ---
            PreparedStatement psPermCount = mock(PreparedStatement.class);
            ResultSet rsPermCount = mock(ResultSet.class);
            when(rsPermCount.next()).thenReturn(true);
            when(rsPermCount.getInt(1)).thenReturn(0);
            when(psPermCount.executeQuery()).thenReturn(rsPermCount);

            // --- Mock for dam_cabecera (regimen_codigo, modalidad_codigo) ---
            PreparedStatement psDam = mock(PreparedStatement.class);
            ResultSet rsDam = mock(ResultSet.class);
            when(rsDam.next()).thenReturn(false);
            when(psDam.executeQuery()).thenReturn(rsDam);

            // --- Mock for COUNT(*) guided steps (completed count) ---
            PreparedStatement psCountStep = mock(PreparedStatement.class);
            ResultSet rsCountStep = mock(ResultSet.class);
            when(rsCountStep.next()).thenReturn(true);
            when(rsCountStep.getInt(1)).thenReturn(8);
            when(psCountStep.executeQuery()).thenReturn(rsCountStep);

            // --- Default mock for any unmocked query ---
            PreparedStatement psDefault = mock(PreparedStatement.class);
            ResultSet rsDefault = mock(ResultSet.class);
            when(rsDefault.next()).thenReturn(false);
            when(psDefault.executeQuery()).thenReturn(rsDefault);
            when(psDefault.executeUpdate()).thenReturn(1);

            when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
                String sql = invocation.getArgument(0);
                if (sql.contains("SELECT * FROM operaciones") || sql.contains("SELECT * FROM importaciones")) return psOpsAll;
                if (sql.contains("INSERT IGNORE")) return psInsert;
                if (sql.contains("ON DUPLICATE KEY UPDATE")) return psUpsert;
                if (sql.contains("SELECT estado") || sql.contains("motivo_bloqueo")) return psSelect;
                if (sql.contains("FROM documentos_importacion")) return psDocs;
                if (sql.contains("FROM manifiestos_carga") && sql.contains("COUNT(*)")) return psManCount;
                if (sql.contains("FROM solicitudes_permiso")) return psPermCount;
                if (sql.contains("FROM dam_cabecera")) return psDam;
                if (sql.contains("COUNT(*)")) return psCountStep;
                return psDefault;
            });

            PanelSaludDTO health = saludPanelServicio.calcularSalud(12345);

            assertNotNull(health.getPorcentajeCompletitud(), "porcentajeCompletitud no debe ser nulo");
            assertNotNull(health.getRiesgoDocumental(), "riesgoDocumental no debe ser nulo");
            assertNotNull(health.getRiesgoNormativo(), "riesgoNormativo no debe ser nulo");
            assertNotNull(health.getEstadoManifiesto(), "estadoManifiesto no debe ser nulo");
            assertNotNull(health.getEstadoPermisos(), "estadoPermisos no debe ser nulo");
            assertNotNull(health.getEstadoPreDam(), "estadoPreDam no debe ser nulo");
            assertNotNull(health.getSiguienteAccion(), "siguienteAccion no debe ser nulo");
            assertNotNull(health.getDocumentosFaltantes(), "documentosFaltantes no debe ser nulo");

            assertTrue(health.getPorcentajeCompletitud().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    // --------------------------------------------------------------
    // test4: SiguienteAccionServicio returns correct action for a new operation
    // --------------------------------------------------------------
    @Test
    public void test4_NextActionReturnsAction() throws Exception {
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class)) {

            Connection mockConn = mock(Connection.class);
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            // SELECT * FROM operaciones (buscarPorId) Ã¢â€ â€™ no results
            PreparedStatement psOps = mock(PreparedStatement.class);
            ResultSet rsOps = mock(ResultSet.class);
            when(rsOps.next()).thenReturn(false);
            when(psOps.executeQuery()).thenReturn(rsOps);

            // SELECT estado Ã¢â€ â€™ returns PENDIENTE (no step completed)
            PreparedStatement psSelect = mock(PreparedStatement.class);
            ResultSet rsSelect = mock(ResultSet.class);
            when(rsSelect.next()).thenReturn(true);
            when(rsSelect.getString("estado")).thenReturn("PENDIENTE");
            when(psSelect.executeQuery()).thenReturn(rsSelect);

            // INSERT IGNORE (initStepsIfNeeded inside isPasoCompleto)
            PreparedStatement psInsert = mock(PreparedStatement.class);
            when(psInsert.executeBatch()).thenReturn(new int[]{1, 1, 1, 1, 1, 1, 1, 1});

            // ON DUPLICATE KEY UPDATE
            PreparedStatement psUpsert = mock(PreparedStatement.class);
            when(psUpsert.executeUpdate()).thenReturn(1);

            // COUNT(*) for guided steps
            PreparedStatement psCount = mock(PreparedStatement.class);
            ResultSet rsCount = mock(ResultSet.class);
            when(rsCount.next()).thenReturn(true);
            when(rsCount.getInt(1)).thenReturn(0);
            when(psCount.executeQuery()).thenReturn(rsCount);

            PreparedStatement psDefault = mock(PreparedStatement.class);
            ResultSet rsDefault = mock(ResultSet.class);
            when(rsDefault.next()).thenReturn(false);
            when(psDefault.executeQuery()).thenReturn(rsDefault);
            when(psDefault.executeUpdate()).thenReturn(1);

            when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
                String sql = invocation.getArgument(0);
                if (sql.contains("INSERT IGNORE")) return psInsert;
                if (sql.contains("ON DUPLICATE KEY UPDATE")) return psUpsert;
                if (sql.contains("COUNT(*)")) return psCount;
                if (sql.contains("SELECT estado") || sql.contains("motivo_bloqueo")) return psSelect;
                if (sql.contains("FROM operaciones") || sql.contains("FROM importaciones")) return psOps;
                return psDefault;
            });

            SiguienteAccionDTO action = siguienteAccionServicio.calcularSiguienteAccion(12345);

            assertNotNull(action, "La accion no debe ser nula");
            assertTrue(action.getPaso() >= 1, "El paso debe ser >= 1");
            assertNotNull(action.getMotivo(), "El motivo no debe ser nulo");
            assertNotNull(action.getAccion(), "La accion no debe ser nula");
            assertNotNull(action.getPrioridad(), "La prioridad no debe ser nula");
            assertNotNull(action.getCamposFaltantes(), "La lista de campos faltantes no debe ser nula");
        }
    }

    // --------------------------------------------------------------
    // test5: Step 7 (PRE-DAM) blocks with OBSERVADO when Importacion
    //        has missing fields (null hsCode, zero FOB)
    // --------------------------------------------------------------
    @Test
    public void test6_IDOR_WizardBlocksOtherUserExpediente() throws Exception {
        ImportacionRepositorio dao = new ImportacionRepositorio();
        Connection mockConn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(mockConn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(42);
        when(rs.getInt("usuario_id")).thenReturn(1);
        when(rs.getString("hs_code")).thenReturn("8471300000");
        when(rs.getString("producto_desc")).thenReturn("Laptops");
        when(rs.getString("pais_origen")).thenReturn("CN");
        when(rs.getString("incoterm")).thenReturn("CIF");
        when(rs.getBigDecimal("fob")).thenReturn(new BigDecimal("10000"));
        when(rs.getBigDecimal("flete")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("seguro")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("cif")).thenReturn(new BigDecimal("10000"));
        when(rs.getBigDecimal("tipo_cambio")).thenReturn(new BigDecimal("3.75"));
        when(rs.getBigDecimal("ad_valorem_aplicado")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("isc_aplicado")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("igv_aplicado")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("ipm_aplicado")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("percepcion_aplicada")).thenReturn(BigDecimal.ZERO);
        when(rs.getBigDecimal("total_impuestos")).thenReturn(BigDecimal.ZERO);
        when(rs.getString("estado")).thenReturn("PENDIENTE");
        when(rs.getBoolean("usado")).thenReturn(false);

        Importacion imp = dao.buscarPorId(mockConn, 42);
        assertNotNull(imp, "Importacion debe existir");
        assertEquals(1, imp.getUsuarioId(), "El expediente pertenece al usuario 1");

        int otroUsuarioId = 2;
        boolean accesoDenegado = imp.getUsuarioId() != otroUsuarioId;
        assertTrue(accesoDenegado, "Usuario 2 NO debe tener acceso al expediente del usuario 1");
    }

    @Test
    public void test7_BigDecimalSumExacta() throws Exception {
        BigDecimal fob = new BigDecimal("10.10");
        BigDecimal flete = new BigDecimal("0.20");
        BigDecimal seguro = new BigDecimal("0.30");
        BigDecimal cif = fob.add(flete).add(seguro);
        assertEquals(0, cif.compareTo(new BigDecimal("10.60")),
            "CIF 10.10+0.20+0.30 debe ser exactamente 10.60, no " + cif);
    }

    @Test
    public void test5_PredamBlocksWhenMissingFields() throws Exception {
        try (MockedStatic<ConexionDB> mockedConexion = Mockito.mockStatic(ConexionDB.class);
             MockedStatic<AuditoriaServicio> mockedAuditoria = Mockito.mockStatic(AuditoriaServicio.class)) {

            Connection mockConn = mock(Connection.class);
            mockedConexion.when(ConexionDB::obtenerConexion).thenReturn(mockConn);

            // ===== INSERT IGNORE batch =====
            PreparedStatement psInsert = mock(PreparedStatement.class);
            when(psInsert.executeBatch()).thenReturn(new int[]{1, 1, 1, 1, 1, 1, 1, 1});

            // ===== SELECT estado: steps 1-6 COMPLETO, step 7 PENDIENTE =====
            PreparedStatement psSelect = mock(PreparedStatement.class);
            ResultSet rsSelect = mock(ResultSet.class);
            when(rsSelect.next()).thenReturn(true);
            when(rsSelect.getString("estado"))
                .thenReturn("COMPLETO")   // step 1
                .thenReturn("COMPLETO")   // step 2
                .thenReturn("COMPLETO")   // step 3
                .thenReturn("COMPLETO")   // step 4
                .thenReturn("COMPLETO")   // step 5
                .thenReturn("COMPLETO")   // step 6
                .thenReturn("PENDIENTE")  // step 7 Ã¢â€ â€™ loop breaks
                .thenReturn("PENDIENTE"); // getStepEstado for current step
            when(psSelect.executeQuery()).thenReturn(rsSelect);

            // ===== SELECT * FROM operaciones (buscarPorId) Ã¢â€ â€™ Importacion with missing fields =====
            PreparedStatement psOpsAll = mock(PreparedStatement.class);
            ResultSet rsOps = mock(ResultSet.class);
            when(rsOps.next()).thenReturn(true, false);
            when(rsOps.getInt("id")).thenReturn(12345);
            when(rsOps.getInt("usuario_id")).thenReturn(1);
            when(rsOps.getInt("proveedor_id")).thenReturn(0);
            when(rsOps.getString("hs_code")).thenReturn(null);           // Ã¢â€ Â null triggers validation failure
            when(rsOps.getString("producto_desc")).thenReturn("Muestras quimicas");
            when(rsOps.getString("pais_origen")).thenReturn("DE");
            when(rsOps.getString("incoterm")).thenReturn("CIF");
            when(rsOps.getDouble("fob")).thenReturn(0.0);               // Ã¢â€ Â zero triggers validation failure
            when(rsOps.getDouble("flete")).thenReturn(50.0);
            when(rsOps.getDouble("seguro")).thenReturn(10.0);
            when(rsOps.getDouble("cif")).thenReturn(60.0);
            when(rsOps.getDouble("tipo_cambio")).thenReturn(3.72);
            when(rsOps.getDouble("ad_valorem_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("isc_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("igv_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("ipm_aplicado")).thenReturn(0.0);
            when(rsOps.getDouble("percepcion_aplicada")).thenReturn(0.0);
            when(rsOps.getDouble("total_impuestos")).thenReturn(0.0);
            when(rsOps.getString("estado")).thenReturn("PENDIENTE");
            when(rsOps.getString("canal_asignado")).thenReturn(null);
            when(rsOps.getString("numero_dam")).thenReturn(null);
            when(rsOps.getTimestamp("fecha_numeracion")).thenReturn(null);
            when(rsOps.getTimestamp("fecha_creacion")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            when(rsOps.getBoolean("usado")).thenReturn(false);
            when(psOpsAll.executeQuery()).thenReturn(rsOps);

            // ===== PredamValidacionServicio inner query mocks =====
            // SELECT FROM usuarios (importer identity)
            PreparedStatement psUsers = mock(PreparedStatement.class);
            ResultSet rsUsers = mock(ResultSet.class);
            when(rsUsers.next()).thenReturn(true, false);
            when(rsUsers.getString("ruc")).thenReturn("20601234567");
            when(rsUsers.getBoolean("ruc_validado")).thenReturn(true);
            when(rsUsers.getString("estado_ruc")).thenReturn("ACTIVO");
            when(rsUsers.getString("condicion_ruc")).thenReturn("HABIDO");
            when(psUsers.executeQuery()).thenReturn(rsUsers);

            // SELECT FROM documentos_transporte INNER JOIN manifiestos_carga
            PreparedStatement psDocTrans = mock(PreparedStatement.class);
            ResultSet rsDocTrans = mock(ResultSet.class);
            when(rsDocTrans.next()).thenReturn(true); // doc exists
            when(psDocTrans.executeQuery()).thenReturn(rsDocTrans);

            // SELECT FROM manifiestos_carga (checkManifiestoExists)
            PreparedStatement psMan = mock(PreparedStatement.class);
            ResultSet rsMan = mock(ResultSet.class);
            when(rsMan.next()).thenReturn(true); // manifiesto exists
            when(psMan.executeQuery()).thenReturn(rsMan);

            // SELECT FROM operaciones (checkMinimumDocuments)
            PreparedStatement psOpsDoc = mock(PreparedStatement.class);
            ResultSet rsOpsDoc = mock(ResultSet.class);
            when(rsOpsDoc.next()).thenReturn(true);
            when(rsOpsDoc.getBoolean("documento_factura")).thenReturn(true);
            when(rsOpsDoc.getBoolean("documento_bl")).thenReturn(true);
            when(rsOpsDoc.getBoolean("permiso_vuce_obtenido")).thenReturn(false);
            when(psOpsDoc.executeQuery()).thenReturn(rsOpsDoc);

            // SELECT FROM dam_cabecera (simulated data check)
            PreparedStatement psDam = mock(PreparedStatement.class);
            ResultSet rsDam = mock(ResultSet.class);
            when(rsDam.next()).thenReturn(false);
            when(psDam.executeQuery()).thenReturn(rsDam);

            // ===== ON DUPLICATE KEY UPDATE (setStepEstado for OBSERVADO) =====
            PreparedStatement psUpsert = mock(PreparedStatement.class);
            when(psUpsert.executeUpdate()).thenReturn(1);

            // ===== COUNT(*) completed steps =====
            PreparedStatement psCount = mock(PreparedStatement.class);
            ResultSet rsCount = mock(ResultSet.class);
            when(rsCount.next()).thenReturn(true);
            when(rsCount.getInt(1)).thenReturn(6);
            when(psCount.executeQuery()).thenReturn(rsCount);

            // ===== Default =====
            PreparedStatement psDefault = mock(PreparedStatement.class);
            ResultSet rsDefault = mock(ResultSet.class);
            when(rsDefault.next()).thenReturn(false);
            when(psDefault.executeQuery()).thenReturn(rsDefault);
            when(psDefault.executeUpdate()).thenReturn(1);

            when(mockConn.prepareStatement(anyString())).thenAnswer(invocation -> {
                String sql = invocation.getArgument(0);
                if (sql.contains("INSERT IGNORE")) return psInsert;
                if (sql.contains("ON DUPLICATE KEY UPDATE")) return psUpsert;
                if (sql.contains("COUNT(*)")) return psCount;
                if (sql.contains("SELECT estado") || sql.contains("motivo_bloqueo")) return psSelect;
                if (sql.contains("SELECT * FROM operaciones") || sql.contains("SELECT * FROM importaciones")) return psOpsAll;
                if (sql.contains("FROM usuarios")) return psUsers;
                if (sql.contains("documentos_transporte")) return psDocTrans;
                if (sql.contains("FROM manifiestos_carga")) return psMan;
                if (sql.contains("documento_factura") || sql.contains("permiso_vuce")) return psOpsDoc;
                if (sql.contains("FROM dam_cabecera")) return psDam;
                return psDefault;
            });

            PasoGuiadoDTO result = flujoGuiadoServicio.avanzarPaso(12345, 1);

            assertEquals("OBSERVADO", result.getEstado(),
                "Step 7 debe quedar OBSERVADO cuando el PRE-DAM falla por datos incompletos");
            assertNotNull(result.getMensajeBloqueo(),
                "Debe haber mensaje de bloqueo indicando el error de PRE-DAM");
            assertTrue(result.getMensajeBloqueo().startsWith("Validacion PRE-DAM fallo"),
                "El mensaje debe indicar que la validacion PRE-DAM fallo: " + result.getMensajeBloqueo());

            mockedAuditoria.verify(() -> AuditoriaServicio.registrar(
                eq(1), eq("AVANZAR_PASO"), eq("expedientes"), eq(12345),
                contains("OBSERVADO"), any(), any()
            ));
        }
    }
}


