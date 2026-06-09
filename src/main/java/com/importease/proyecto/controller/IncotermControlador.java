package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.model.RespuestaEnvoltorio;
import com.importease.proyecto.service.CsrfUtil;
import com.importease.proyecto.service.DataConfidenceServicio;
import com.importease.proyecto.service.IncotermServicio;

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

@WebServlet("/api/incoterms/*")
public class IncotermControlador extends HttpServlet {
    private final Gson gson = new Gson();
    private final IncotermServicio service = new IncotermServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"No autenticado\"}");
            return;
        }
        Object data;
        if (path == null || "/listar".equals(path)) {
            data = service.listar();
        } else if ("/comparar".equals(path)) {
            data = service.comparar(req.getParameter("base"), req.getParameter("contra"));
        } else {
            resp.setStatus(404);
            write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint de incoterms no encontrado", IncotermServicio.SOURCE, false));
            return;
        }
        write(resp, RespuestaEnvoltorio.ok(data, IncotermServicio.SOURCE, "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!com.importease.proyecto.util.CsrfUtil.validateRequest(req, resp)) return;
        setupJson(resp);
        String path = req.getPathInfo();
        try {
            Map<String, Object> body = readBody(req);
            Object data;
            if ("/simular".equals(path)) {
                data = service.simular(body);
            } else if ("/guardar-decision".equals(path)) {
                data = service.guardarDecision(getUsuarioId(req), getSessionId(req), body, req.getRemoteAddr(), req.getHeader("User-Agent"));
            } else {
                resp.setStatus(404);
                write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint de incoterms no encontrado", IncotermServicio.SOURCE, false));
                return;
            }
            attachCsrf(req, data);
            write(resp, RespuestaEnvoltorio.ok(data, IncotermServicio.SOURCE, "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, RespuestaEnvoltorio.error("INCOTERM_ERROR", "No se pudo procesar Incoterms", IncotermServicio.SOURCE, false));
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

    private String getSessionId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : session.getId();
    }

    private void setupJson(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    @SuppressWarnings("unchecked")
    private void attachCsrf(HttpServletRequest req, Object data) {
        if (data instanceof Map<?, ?> raw) {
            ((Map<String, Object>) raw).put("csrfToken", CsrfUtil.getToken(req.getSession(false)));
        }
    }

    private void write(HttpServletResponse resp, Object payload) throws IOException {
        resp.getWriter().print(gson.toJson(payload));
    }
}
