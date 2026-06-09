package com.importease.proyecto.controller.usuario;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.service.UsuarioServicio;
import com.importease.proyecto.service.AuditoriaServicio;
import com.importease.proyecto.util.EmailMasker;
import com.importease.proyecto.util.JsonResponseWriter;
import com.importease.proyecto.util.RequestBodyParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/api/registro")
public class RegistroUsuarioControlador extends HttpServlet {
    private final RequestBodyParser bodyParser;
    private final JsonResponseWriter json;
    private final UsuarioServicio usuarioServicio;
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.AbstractMap.SimpleEntry<Integer, Long>> registroAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long RATE_WINDOW_MS = 15 * 60 * 1000;
    private static final int MAX_ATTEMPTS = 3;

    public RegistroUsuarioControlador() {
        this(new RequestBodyParser(), new JsonResponseWriter(), new UsuarioServicio());
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            registroAttempts.entrySet().removeIf(e -> now - e.getValue().getValue() > RATE_WINDOW_MS);
        }, 15, 15, java.util.concurrent.TimeUnit.MINUTES);
    }

    public RegistroUsuarioControlador(RequestBodyParser bodyParser, JsonResponseWriter json, UsuarioServicio usuarioServicio) {
        this.bodyParser = bodyParser;
        this.json = json;
        this.usuarioServicio = usuarioServicio;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ip = req.getRemoteAddr();
        java.util.AbstractMap.SimpleEntry<Integer, Long> record = registroAttempts.get(ip);
        if (record != null) {
            if (System.currentTimeMillis() - record.getValue() > RATE_WINDOW_MS) {
                registroAttempts.remove(ip);
            } else if (record.getKey() >= MAX_ATTEMPTS) {
                json.write(resp, 429, java.util.Map.of("error", "Demasiados intentos. Intenta en 15 minutos."));
                return;
            }
        }
        registroAttempts.merge(ip, new java.util.AbstractMap.SimpleEntry<>(1, System.currentTimeMillis()), (oldVal, newVal) -> {
            if (System.currentTimeMillis() - oldVal.getValue() > RATE_WINDOW_MS) {
                return new java.util.AbstractMap.SimpleEntry<>(1, System.currentTimeMillis());
            }
            return new java.util.AbstractMap.SimpleEntry<>(oldVal.getKey() + 1, oldVal.getValue());
        });

        StringBuilder rawBody = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) rawBody.append(line);
        }
        JsonObject jsonObj = JsonParser.parseString(rawBody.toString()).getAsJsonObject();
        String email = jsonObj.has("email") ? jsonObj.get("email").getAsString() : null;
        String password = jsonObj.has("passwordHash") ? jsonObj.get("passwordHash").getAsString() : null;
        String ruc = jsonObj.has("ruc") ? jsonObj.get("ruc").getAsString() : null;
        String razonSocial = jsonObj.has("razonSocial") ? jsonObj.get("razonSocial").getAsString() : null;

        Usuario newUser = new Usuario();
        newUser.setEmail(email);
        newUser.setPasswordHash(password);
        newUser.setRuc(ruc);
        newUser.setRazonSocial(razonSocial);

        StringBuilder outMensaje = new StringBuilder();
        boolean ok = usuarioServicio.registrarUsuario(newUser, outMensaje);
        if (ok) {
            Usuario u = usuarioServicio.obtenerPorEmail(newUser.getEmail());
            Integer uId = (u != null) ? u.getId() : null;
            AuditoriaServicio.registrar(uId, "REGISTRO_USUARIO", "seguridad", null,
                "Registro de nuevo usuario: " + EmailMasker.mask(newUser.getEmail()) + " con RUC " + newUser.getRuc() + " (" + newUser.getRazonSocial() + ")",
                req.getRemoteAddr(), req.getHeader("User-Agent"));
            registroAttempts.remove(ip);
            json.write(resp, 200, Map.of("success", true));
        } else {
            json.write(resp, 200, Map.of("success", false, "mensaje", outMensaje.toString()));
        }
    }
}
