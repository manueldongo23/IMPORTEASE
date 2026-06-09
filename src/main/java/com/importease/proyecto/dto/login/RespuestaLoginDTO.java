package com.importease.proyecto.dto.login;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.model.UsuarioDTO;

public class RespuestaLoginDTO {
    private final boolean success;
    private final String mensaje;
    private final UsuarioDTO usuario;

    private RespuestaLoginDTO(boolean success, String mensaje, UsuarioDTO usuario) {
        this.success = success;
        this.mensaje = mensaje;
        this.usuario = usuario;
    }

    public static RespuestaLoginDTO success(Usuario usuario) {
        return new RespuestaLoginDTO(true, null, new UsuarioDTO(usuario));
    }

    public static RespuestaLoginDTO failed(String mensaje) {
        return new RespuestaLoginDTO(false, mensaje, null);
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
