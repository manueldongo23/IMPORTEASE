package com.importease.proyecto.dto.login;

public class LoginRequestDTO {
    private String email;
    private String password;
    private String captcha;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getCaptcha() {
        return captcha;
    }
}
