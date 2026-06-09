package com.importease.proyecto.service;

/**
 * Validador de complejidad de contraseÃ±as.
 * Requisitos mÃ­nimos para entorno empresarial.
 */
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    /**
     * Valida que la contraseÃ±a cumpla requisitos mÃ­nimos de seguridad.
     * @return null si es vÃ¡lida, o un mensaje de error descriptivo.
     */
    public static String validate(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseÃ±a es obligatoria";
        }
        if (password.length() < MIN_LENGTH) {
            return "La contraseÃ±a debe tener al menos " + MIN_LENGTH + " caracteres";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseÃ±a debe contener al menos una letra mayÃºscula";
        }
        if (!password.matches(".*[a-z].*")) {
            return "La contraseÃ±a debe contener al menos una letra minÃºscula";
        }
        if (!password.matches(".*\\d.*")) {
            return "La contraseÃ±a debe contener al menos un nÃºmero";
        }
        return null; // VÃ¡lida
    }
}
