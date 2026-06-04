package com.importease.proyecto.service;

import com.importease.proyecto.model.Importacion;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PredamValidationServiceTest {

    @Test
    public void perfumesConHs3303ExigenPermisoSectorial() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement psUsers = mock(PreparedStatement.class);
        PreparedStatement psManifest = mock(PreparedStatement.class);
        PreparedStatement psOps = mock(PreparedStatement.class);
        PreparedStatement psAudit = mock(PreparedStatement.class);
        ResultSet rsUsers = mock(ResultSet.class);
        ResultSet rsManifest = mock(ResultSet.class);
        ResultSet rsOps = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("FROM usuarios")) {
                return psUsers;
            }
            if (sql.contains("FROM manifiestos_carga")) {
                return psManifest;
            }
            if (sql.contains("FROM operaciones")) {
                return psOps;
            }
            return psAudit;
        });

        when(psUsers.executeQuery()).thenReturn(rsUsers);
        when(psManifest.executeQuery()).thenReturn(rsManifest);
        when(psOps.executeQuery()).thenReturn(rsOps);
        when(psAudit.executeUpdate()).thenReturn(1);

        when(rsUsers.next()).thenReturn(true, false);
        when(rsUsers.getString("ruc")).thenReturn("20601234567");
        when(rsUsers.getBoolean("ruc_validado")).thenReturn(true);
        when(rsUsers.getString("estado_ruc")).thenReturn("ACTIVO");
        when(rsUsers.getString("condicion_ruc")).thenReturn("HABIDO");

        when(rsManifest.next()).thenReturn(true);

        when(rsOps.next()).thenReturn(true);
        when(rsOps.getBoolean("documento_factura")).thenReturn(true);
        when(rsOps.getBoolean("documento_bl")).thenReturn(true);
        when(rsOps.getBoolean("permiso_vuce_obtenido")).thenReturn(false);

        Importacion imp = new Importacion();
        imp.setId(99);
        imp.setUsuarioId(7);
        imp.setHsCode("3303000000");
        imp.setValorFob(100.0);
        imp.setValorCif(112.0);
        imp.setTipoCambio(3.72);
        imp.setProductoDesc("Perfumes y aguas de tocador");
        imp.setPaisOrigen("FR");
        imp.setIncoterm("FOB");
        imp.setBlMasterId(1);

        PredamValidationException ex = assertThrows(PredamValidationException.class, () -> PredamValidationService.validate(con, imp));
        Map<String, Object> error = ex.getValidationError();
        @SuppressWarnings("unchecked")
        List<String> missing = (List<String>) error.get("missingFields");

        assertTrue(missing.stream().anyMatch(s -> s.toLowerCase().contains("permiso")));
        assertTrue(missing.stream().noneMatch(s -> s.toLowerCase().contains("manifiesto")));
        assertTrue(missing.stream().noneMatch(s -> s.toLowerCase().contains("ruc activo y habido")));
    }

    @Test
    public void equipoMedicoInalambricoExigeAlertaCruzadaMtcDigemid() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement psUsers = mock(PreparedStatement.class);
        PreparedStatement psManifest = mock(PreparedStatement.class);
        PreparedStatement psOps = mock(PreparedStatement.class);
        PreparedStatement psAudit = mock(PreparedStatement.class);
        ResultSet rsUsers = mock(ResultSet.class);
        ResultSet rsManifest = mock(ResultSet.class);
        ResultSet rsOps = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0, String.class);
            if (sql.contains("FROM usuarios")) {
                return psUsers;
            }
            if (sql.contains("FROM manifiestos_carga")) {
                return psManifest;
            }
            if (sql.contains("FROM operaciones")) {
                return psOps;
            }
            return psAudit;
        });

        when(psUsers.executeQuery()).thenReturn(rsUsers);
        when(psManifest.executeQuery()).thenReturn(rsManifest);
        when(psOps.executeQuery()).thenReturn(rsOps);
        when(psAudit.executeUpdate()).thenReturn(1);

        when(rsUsers.next()).thenReturn(true, false);
        when(rsUsers.getString("ruc")).thenReturn("20601234567");
        when(rsUsers.getBoolean("ruc_validado")).thenReturn(true);
        when(rsUsers.getString("estado_ruc")).thenReturn("ACTIVO");
        when(rsUsers.getString("condicion_ruc")).thenReturn("HABIDO");

        when(rsManifest.next()).thenReturn(true);

        when(rsOps.next()).thenReturn(true);
        when(rsOps.getBoolean("documento_factura")).thenReturn(true);
        when(rsOps.getBoolean("documento_bl")).thenReturn(true);
        when(rsOps.getBoolean("permiso_vuce_obtenido")).thenReturn(false);

        Importacion imp = new Importacion();
        imp.setId(101);
        imp.setUsuarioId(7);
        imp.setHsCode("9018120000"); // EcÃ³grafo
        imp.setValorFob(5000.0);
        imp.setValorCif(5250.0);
        imp.setTipoCambio(3.72);
        imp.setProductoDesc("EcÃ³grafo mÃ©dico con conexiÃ³n Wi-Fi y Bluetooth");
        imp.setPaisOrigen("US");
        imp.setIncoterm("FOB");
        imp.setBlMasterId(2);

        PredamValidationException ex = assertThrows(PredamValidationException.class, () -> PredamValidationService.validate(con, imp));
        Map<String, Object> error = ex.getValidationError();
        @SuppressWarnings("unchecked")
        List<String> missing = (List<String>) error.get("missingFields");

        assertTrue(missing.stream().anyMatch(s -> s.contains("ALERTA CRUZADA (MTC + DIGEMID)")));
    }
}


