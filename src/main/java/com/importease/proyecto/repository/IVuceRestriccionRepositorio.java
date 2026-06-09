package com.importease.proyecto.repository;

import com.importease.proyecto.model.VuceRestriccion;
import java.util.List;

public interface IVuceRestriccionRepositorio {
    VuceRestriccion obtenerPorEntidad(String entidad);
    List<VuceRestriccion> listarTodas();
}
