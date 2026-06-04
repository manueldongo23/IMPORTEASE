package com.importease.proyecto.service.login;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHashService {
    public String hash(String password) {
        if (password == null) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean matches(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
