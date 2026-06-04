package com.importease.proyecto.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    @Test
    public void testPasswordValida() {
        assertNull(PasswordValidator.validate("Segura123"));
        assertNull(PasswordValidator.validate("MypassWord99!"));
    }

    @Test
    public void testPasswordCorta() {
        String err = PasswordValidator.validate("Ab1");
        assertNotNull(err);
        assertTrue(err.contains("al menos 8 caracteres"));
    }

    @Test
    public void testPasswordFaltaMayuscula() {
        String err = PasswordValidator.validate("segura123");
        assertNotNull(err);
        assertTrue(err.contains("letra mayÃºscula"));
    }

    @Test
    public void testPasswordFaltaMinuscula() {
        String err = PasswordValidator.validate("SEGURA123");
        assertNotNull(err);
        assertTrue(err.contains("letra minÃºscula"));
    }

    @Test
    public void testPasswordFaltaNumero() {
        String err = PasswordValidator.validate("SeguraTxt");
        assertNotNull(err);
        assertTrue(err.contains("un nÃºmero"));
    }
}

