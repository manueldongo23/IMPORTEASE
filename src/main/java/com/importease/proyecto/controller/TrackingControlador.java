package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.model.RespuestaEnvoltorio;
import com.importease.proyecto.service.DataConfidenceServicio;
import com.importease.proyecto.service.TrackingServicio;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/tracking/*")
public class TrackingControlador extends HttpServlet {
    private final Gson gson = new Gson();
    private final TrackingServicio trackingServicio = new TrackingServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        String path = req.getPathInfo();
        Integer usuarioId = getUsuarioId(req);
        try {
            Object data;
            if ("/listar".equals(path)) {
                data = trackingServicio.listar(usuarioId);
                write(resp, RespuestaEnvoltorio.ok(data, "BD_LOCAL", "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
                return;
            }
            if ("/detalle".equals(path)) {
                long id = parseLong(req.getParameter("id"), -1);
                data = trackingServicio.detalle(id, usuarioId);
                if (data == null) {
                    resp.setStatus(404);
                    write(resp, RespuestaEnvoltorio.error("TRACKING_NOT_FOUND", "Tracking no encontrado", "BD_LOCAL", false));
                    return;
                }
                write(resp, RespuestaEnvoltorio.ok(data, "BD_LOCAL", "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
                return;
            }
            if ("/eventos".equals(path)) {
                long id = parseLong(req.getParameter("id"), -1);
                data = trackingServicio.eventos(id, usuarioId);
                write(resp, RespuestaEnvoltorio.ok(data, "BD_LOCAL", "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
                return;
            }
            resp.setStatus(404);
            write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint de tracking no encontrado", "BD_LOCAL", false));
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, RespuestaEnvoltorio.error("TRACKING_QUERY_ERROR", "No se pudo consultar tracking", "BD_LOCAL", false));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        String path = req.getPathInfo();
        Integer usuarioId = getUsuarioId(req);
        String sessionId = req.getSession(false) != null ? req.getSession(false).getId() : null;
        try {
            if ("/registrar".equals(path)) {
                Map<String, Object> body = readBody(req);
                Map<String, Object> data = trackingServicio.registrar(usuarioId, sessionId, body, req.getRemoteAddr(), req.getHeader("User-Agent"));
                String sourceType = str(data.get("sourceType"), "MANUAL_VERIFICADO");
                write(resp, RespuestaEnvoltorio.ok(data, "USUARIO", sourceType, DataConfidenceServicio.confidenceFor(sourceType)));
                return;
            }
            if ("/sincronizar".equals(path)) {
                long id = parseLong(req.getParameter("id"), -1);
                if (id <= 0) {
                    Map<String, Object> body = readBody(req);
                    id = parseLong(str(body.get("id"), "-1"), -1);
                }
                Map<String, Object> data = trackingServicio.sincronizar(id, usuarioId, sessionId, req.getRemoteAddr(), req.getHeader("User-Agent"));
                String sourceType = str(data.get("sourceType"), "PENDIENTE_CREDENCIALES");
                write(resp, RespuestaEnvoltorio.ok(data, "TRACKING_API", sourceType, DataConfidenceServicio.confidenceFor(sourceType)));
                return;
            }
            resp.setStatus(404);
            write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint de tracking no encontrado", "BD_LOCAL", false));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            write(resp, RespuestaEnvoltorio.error("TRACKING_BAD_REQUEST", e.getMessage(), "BD_LOCAL", false));
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, RespuestaEnvoltorio.error("TRACKING_MUTATION_ERROR", "No se pudo procesar tracking", "BD_LOCAL", false));
        }
    }

    private Map<String, Object> readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        if (sb.isEmpty()) return new LinkedHashMap<>();
        Map<String, Object> body = gson.fromJson(sb.toString(), new TypeToken<Map<String, Object>>(){}.getType());
        return body == null ? new LinkedHashMap<>() : body;
    }

    private Integer getUsuarioId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("usuarioId") : null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try { return value == null ? null : Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return null; }
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value); } catch (Exception e) { return fallback; }
    }

    private String str(Object value, String fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        return String.valueOf(value);
    }

    private void setupJson(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    private void write(HttpServletResponse resp, Object payload) throws IOException {
        resp.getWriter().print(gson.toJson(payload));
    }
}
