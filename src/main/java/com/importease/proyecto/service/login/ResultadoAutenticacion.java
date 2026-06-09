package com.importease.proyecto.service.login;

import com.importease.proyecto.model.Usuario;

public class ResultadoAutenticacion {
    private final boolean success;
    private final int statusCode;
    private final String message;
    private final Usuario usuario;

    private ResultadoAutenticacion(boolean success, int statusCode, String message, Usuario usuario) {
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
        this.usuario = usuario;
    }

    public static ResultadoAutenticacion success(Usuario usuario) {
        return new ResultadoAutenticacion(true, 200, null, usuario);
    }

    public static ResultadoAutenticacion failed(String message) {
        return new ResultadoAutenticacion(false, 200, message, null);
    }

    public static ResultadoAutenticacion blocked(int remainingMinutes) {
        return new ResultadoAutenticacion(
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
