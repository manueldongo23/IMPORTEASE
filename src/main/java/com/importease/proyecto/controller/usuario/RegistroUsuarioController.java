package com.importease.proyecto.controller.usuario;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.AuditoriaService;
import com.importease.proyecto.service.PasswordValidator;
import com.importease.proyecto.util.EmailMasker;
import com.importease.proyecto.util.JsonResponseWriter;
import com.importease.proyecto.util.RequestBodyParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/registro")
public class RegistroUsuarioController extends HttpServlet {
    private final RequestBodyParser bodyParser;
    private final JsonResponseWriter json;
    private final UsuarioDAO usuarioDao;
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.AbstractMap.SimpleEntry<Integer, Long>> registroAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long RATE_WINDOW_MS = 15 * 60 * 1000;
    private static final int MAX_ATTEMPTS = 3;

    public RegistroUsuarioController() {
        this(new RequestBodyParser(), new JsonResponseWriter(), new UsuarioDAO());
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            registroAttempts.entrySet().removeIf(e -> now - e.getValue().getValue() > RATE_WINDOW_MS);
        }, 15, 15, java.util.concurrent.TimeUnit.MINUTES);
    }

    public RegistroUsuarioController(RequestBodyParser bodyParser, JsonResponseWriter json, UsuarioDAO usuarioDao) {
        this.bodyParser = bodyParser;
        this.json = json;
        this.usuarioDao = usuarioDao;
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

        Usuario newUser = bodyParser.read(req, Usuario.class);

        String pwError = PasswordValidator.validate(newUser.getPasswordHash());
        if (pwError != null) {
            json.write(resp, 200, Map.of("success", false, "mensaje", pwError));
            return;
        }

        if (usuarioDao.buscarPorEmail(newUser.getEmail()) != null) {
            json.write(resp, 200, Map.of("success", false, "mensaje", "Este email ya esta registrado"));
            return;
        }

        boolean ok = usuarioDao.crearUsuario(newUser);
        if (ok) {
            Usuario u = usuarioDao.buscarPorEmail(newUser.getEmail());
            Integer uId = (u != null) ? u.getId() : null;
            AuditoriaService.registrar(uId, "REGISTRO_USUARIO", "seguridad", null,
                "Registro de nuevo usuario: " + EmailMasker.mask(newUser.getEmail()) + " con RUC " + newUser.getRuc() + " (" + newUser.getRazonSocial() + ")",
                req.getRemoteAddr(), req.getHeader("User-Agent"));
            registroAttempts.remove(ip);
        }
        json.write(resp, 200, Map.of("success", ok));
    }
}
