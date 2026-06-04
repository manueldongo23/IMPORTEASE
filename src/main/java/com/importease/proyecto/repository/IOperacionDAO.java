/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.importease.proyecto.repository;

import com.importease.proyecto.model.Operacion;
import java.util.List;

public interface IOperacionDAO {
    boolean guardar(Operacion operacion);
    Operacion obtenerPorId(int id);
    List<Operacion> listarPorUsuario(int usuarioId);
    boolean actualizarDam(int id, String numeroDam, String estado, String canal);
}


