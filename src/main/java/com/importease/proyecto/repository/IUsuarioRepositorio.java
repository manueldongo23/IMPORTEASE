package com.importease.proyecto.repository;

import com.importease.proyecto.model.Usuario;
import java.util.List;

public interface IUsuarioRepositorio {
    boolean crearUsuario(Usuario usuario);
    Usuario buscarPorRuc(String ruc);
    Usuario buscarPorId(int id);
    Usuario buscarPorEmail(String email);
    List<Usuario> listarUsuarios();
}

