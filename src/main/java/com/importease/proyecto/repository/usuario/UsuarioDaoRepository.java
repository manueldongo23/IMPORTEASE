package com.importease.proyecto.repository.usuario;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.UsuarioDAO;

public class UsuarioDaoRepository implements UsuarioRepository {
    private final UsuarioDAO usuarioDAO;

    public UsuarioDaoRepository() {
        this(new UsuarioDAO());
    }

    public UsuarioDaoRepository(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public Usuario findByEmail(String email) {
        return usuarioDAO.buscarPorEmail(email);
    }
}
