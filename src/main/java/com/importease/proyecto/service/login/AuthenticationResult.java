package com.importease.proyecto.service.login;

import com.importease.proyecto.model.Usuario;

public class AuthenticationResult {
    private final boolean success;
    private final int statusCode;
    private final String message;
    private final Usuario usuario;

    private AuthenticationResult(boolean success, int statusCode, String message, Usuario usuario) {
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
        this.usuario = usuario;
    }

    public static AuthenticationResult success(Usuario usuario) {
        return new AuthenticationResult(true, 200, null, usuario);
    }

    public static AuthenticationResult failed(String message) {
        return new AuthenticationResult(false, 200, message, null);
    }

    public static AuthenticationResult blocked(int remainingMinutes) {
        return new AuthenticationResult(
                false,
                429,
                "Demasiados intentos fallidos. Intente en " + remainingMinutes + " minutos.",
                null
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
