package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.repository.HsCodeRepositorio;
import com.importease.proyecto.repository.IHsCodeRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class VuceValidadorServicioTest {

    @Mock
    private ArancelServicio arancelServicio;

    @Mock
    private HsCodeRepositorio hsDao;

    @Mock
    private VUCEScraper vuceScraper;

    @InjectMocks
    private VuceValidadorServicio validator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(vuceScraper.getDatosPorEntidad(anyString())).thenReturn(new java.util.HashMap<>());
    }

    @Test
    public void testValidarProductoRestringido() {
        HsCode mockHs = new HsCode();
        mockHs.setCodigo("3304990000");
        mockHs.setDescripcionEs("CosmÃ©ticos");
        mockHs.setRequiereVuce(true);
        mockHs.setEntidadVuce("DIGESA");

        when(arancelServicio.consultarArancel(anyString())).thenReturn(mockHs);

        Map<String, Object> resultado = validator.validar("3304.99.00.00");

        assertTrue((Boolean) resultado.get("encontrado"));
        assertTrue((Boolean) resultado.get("requiere"));
        assertEquals("3304990000", resultado.get("codigo"));
        assertTrue(resultado.get("mensaje").toString().contains("DIGESA"));
    }
}


