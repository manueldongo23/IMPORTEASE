package com.importease.proyecto.controller.login;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/login")
public class LoginControlador extends HttpServlet {
    private final LoginManejadorPeticion loginManejadorPeticion = new LoginManejadorPeticion();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        loginManejadorPeticion.handle(request, response);
    }
}
