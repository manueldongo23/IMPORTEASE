/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.importease.proyecto.repository;

import com.importease.proyecto.model.Usuario;
import java.util.List;

public interface IUsuarioDAO {
    boolean crearUsuario(Usuario usuario);
    Usuario buscarPorRuc(String ruc);
    Usuario buscarPorId(int id);
    Usuario buscarPorEmail(String email);
    List<Usuario> listarUsuarios();
}


