package com.importease.proyecto.controller.login;

import com.importease.proyecto.dto.login.LoginRequestDTO;
import com.importease.proyecto.dto.login.LoginResponseDTO;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.service.AuditoriaService;
import com.importease.proyecto.service.login.AuthenticationResult;
import com.importease.proyecto.service.login.AuthenticationService;
import com.importease.proyecto.service.login.SessionService;
import com.importease.proyecto.util.EmailMasker;
import com.importease.proyecto.util.JsonResponseWriter;
import com.importease.proyecto.util.RequestBodyParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginRequestHandler {
    private final RequestBodyParser bodyParser;
    private final JsonResponseWriter json;
    private final AuthenticationService authenticationService;
    private final SessionService sessionService;

    public LoginRequestHandler() {
        this(
                new RequestBodyParser(),
                new JsonResponseWriter(),
                new AuthenticationService(),
                new SessionService()
        );
    }

    public LoginRequestHandler(
            RequestBodyParser bodyParser,
            JsonResponseWriter json,
            AuthenticationService authenticationService,
            SessionService sessionService
    ) {
        this.bodyParser = bodyParser;
        this.json = json;
        this.authenticationService = authenticationService;
        this.sessionService = sessionService;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginRequestDTO loginRequest = bodyParser.read(request, LoginRequestDTO.class);
        AuthenticationResult result = authenticationService.authenticate(
                loginRequest,
                request.getRemoteAddr(),
                request.getSession()
        );

        if (!result.isSuccess()) {
            json.write(response, result.getStatusCode(), LoginResponseDTO.failed(result.getMessage()));
            return;
        }

        Usuario usuario = result.getUsuario();
        sessionService.startAuthenticatedSession(request, usuario);
        AuditoriaService.registrar(
                usuario.getId(),
                "INICIAR_SESION",
                "seguridad",
                null,
                "Inicio de sesion exitoso para el usuario: "
                        + EmailMasker.mask(usuario.getEmail())
                        + " ("
                        + usuario.getRazonSocial()
                        + ")",
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
        json.write(response, 200, LoginResponseDTO.success(usuario));
    }
}
