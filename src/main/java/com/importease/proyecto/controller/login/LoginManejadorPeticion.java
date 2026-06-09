package com.importease.proyecto.controller.login;

import com.importease.proyecto.dto.login.SolicitudLoginDTO;
import com.importease.proyecto.dto.login.RespuestaLoginDTO;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.service.AuditoriaServicio;
import com.importease.proyecto.service.login.ResultadoAutenticacion;
import com.importease.proyecto.service.login.AutenticacionServicio;
import com.importease.proyecto.service.login.SesionServicio;
import com.importease.proyecto.util.EmailMasker;
import com.importease.proyecto.util.JsonResponseWriter;
import com.importease.proyecto.util.RequestBodyParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginManejadorPeticion {
    private final RequestBodyParser bodyParser;
    private final JsonResponseWriter json;
    private final AutenticacionServicio autenticacionServicio;
    private final SesionServicio sesionServicio;
    private static final ConcurrentHashMap<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000L;
    private static final ConcurrentHashMap<String, Long> blockedUntil = new ConcurrentHashMap<>();

    public LoginManejadorPeticion() {
        this(
                new RequestBodyParser(),
                new JsonResponseWriter(),
                new AutenticacionServicio(),
                new SesionServicio()
        );
    }

    public LoginManejadorPeticion(
            RequestBodyParser bodyParser,
            JsonResponseWriter json,
            AutenticacionServicio autenticacionServicio,
            SesionServicio sesionServicio
    ) {
        this.bodyParser = bodyParser;
        this.json = json;
        this.autenticacionServicio = autenticacionServicio;
        this.sesionServicio = sesionServicio;
    }

    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        Long blocked = blockedUntil.get(ip);
        if (blocked != null && now < blocked) {
            json.write(response, 429, RespuestaLoginDTO.failed("Demasiados intentos. Espera 60 segundos."));
            return;
        }
        if (blocked != null && now >= blocked) {
            blockedUntil.remove(ip);
            loginAttempts.remove(ip);
        }

        SolicitudLoginDTO loginRequest = bodyParser.read(request, SolicitudLoginDTO.class);
        ResultadoAutenticacion result = autenticacionServicio.authenticate(
                loginRequest,
                ip,
                request.getSession()
        );

        if (!result.isSuccess()) {
            AtomicInteger count = loginAttempts.computeIfAbsent(ip, k -> new AtomicInteger(0));
            int attempts = count.incrementAndGet();
            if (attempts >= MAX_ATTEMPTS) {
                blockedUntil.put(ip, now + WINDOW_MS);
            }
            json.write(response, result.getStatusCode(), RespuestaLoginDTO.failed(result.getMessage()));
            return;
        }

        loginAttempts.remove(ip);
        blockedUntil.remove(ip);

        Usuario usuario = result.getUsuario();
        sesionServicio.startAuthenticatedSession(request, usuario);
        AuditoriaServicio.registrar(
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
        json.write(response, 200, RespuestaLoginDTO.success(usuario));
    }
}
