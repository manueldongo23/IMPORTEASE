/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.importease.proyecto.repository;

import com.importease.proyecto.model.HsCode;
import java.util.List;

public interface IHsCodeDAO {
    HsCode buscarPorDescripcion(String descripcion);
    HsCode obtenerPorCodigo(String codigo);
    List<HsCode> listarTodos();
    List<HsCode> buscarSugerencias(String termino);
    boolean insertar(HsCode hs);
    boolean actualizar(HsCode hs);
}

