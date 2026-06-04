package com.importease.proyecto.repository.usuario;

import com.importease.proyecto.model.Usuario;

public interface UsuarioRepository {
    Usuario findByEmail(String email);
}
