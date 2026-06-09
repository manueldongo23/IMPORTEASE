package com.importease.proyecto.validator;

import com.importease.proyecto.service.PasswordValidator;
import com.importease.proyecto.service.RucValidadorLocal;

/**
 * Validador dedicado a la entidad de Usuario.
 * Centraliza las validaciones de campos de entrada para el registro e inicio de sesión.
 */
public class UsuarioValidador {

    /**
     * Valida si un correo electrónico tiene formato correcto.
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }

    /**
     * Valida la contraseña del usuario usando PasswordValidator.
     */
    public static String validarPassword(String password) {
        return PasswordValidator.validate(password);
    }

    /**
     * Valida el RUC peruano del usuario usando RucValidadorLocal.
     */
    public static boolean esRucValido(String ruc) {
        return RucValidadorLocal.esRucValido(ruc);
    }
}
