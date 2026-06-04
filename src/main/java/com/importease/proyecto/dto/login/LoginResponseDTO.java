package com.importease.proyecto.dto.login;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.model.UsuarioDTO;

public class LoginResponseDTO {
    private final boolean success;
    private final String mensaje;
    private final UsuarioDTO usuario;

    private LoginResponseDTO(boolean success, String mensaje, UsuarioDTO usuario) {
        this.success = success;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public static LoginResponseDTO success(Usuario usuario) {
        return new LoginResponseDTO(true, null, new UsuarioDTO(usuario));
    }

    public static LoginResponseDTO failed(String mensaje) {
        return new LoginResponseDTO(false, mensaje, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMensaje() {
        return mensaje;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }
}
