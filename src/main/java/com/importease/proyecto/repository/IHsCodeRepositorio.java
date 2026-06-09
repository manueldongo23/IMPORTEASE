package com.importease.proyecto.repository;

import com.importease.proyecto.model.HsCode;
import java.util.List;

public interface IHsCodeRepositorio {
    HsCode buscarPorDescripcion(String descripcion);
    HsCode obtenerPorCodigo(String codigo);
    List<HsCode> listarTodos();
    List<HsCode> buscarSugerencias(String termino);
    boolean insertar(HsCode hs);
    boolean actualizar(HsCode hs);
}
