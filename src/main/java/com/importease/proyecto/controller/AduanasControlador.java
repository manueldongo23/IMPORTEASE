package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.model.RespuestaEnvoltorio;
import com.importease.proyecto.service.AduanasServicio;
import com.importease.proyecto.service.CsrfUtil;
import com.importease.proyecto.service.DataConfidenceServicio;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/aduanas/*")
public class AduanasControlador extends HttpServlet {
    private final Gson gson = new Gson();
    private final AduanasServicio service = new AduanasServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        Integer usuarioId = getUsuarioId(req);
        if (usuarioId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            write(resp, RespuestaEnvoltorio.error("NO_SESSION", "Sesion requerida", AduanasServicio.SOURCE, false));
            return;
        }

        String path = req.getPathInfo();
        try {
            Object data;
            if ("/expediente".equals(path)) {
                int operacionId = getOperacionId(req);
                validarPropietario(usuarioId, operacionId);
                data = service.obtenerExpediente(usuarioId, operacionId);
            } else if ("/timeline".equals(path)) {
                int operacionId = getOperacionId(req);
                validarPropietario(usuarioId, operacionId);
                data = service.obtenerTimeline(usuarioId, operacionId);
            } else if ("/alertas".equals(path)) {
                int operacionId = getOperacionId(req);
                validarPropietario(usuarioId, operacionId);
                data = service.obtenerAlertas(usuarioId, operacionId);
            } else if ("/base-legal".equals(path)) {
                int operacionId = getOperacionId(req);
                validarPropietario(usuarioId, operacionId);
                data = service.obtenerBaseLegal(usuarioId, operacionId);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint aduanero no encontrado", AduanasServicio.SOURCE, false));
                return;
            }
            EnvelopeMetadata meta = resolveEnvelopeMetadata(data);
            write(resp, RespuestaEnvoltorio.ok(data, meta.source, meta.sourceType, meta.confidence));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            write(resp, RespuestaEnvoltorio.error("FORBIDDEN", "No autorizado para esta operacion", AduanasServicio.SOURCE, false));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(resp, RespuestaEnvoltorio.error("BAD_REQUEST", "Solicitud aduanera invalida", AduanasServicio.SOURCE, false));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            write(resp, RespuestaEnvoltorio.error("ADUANAS_ERROR", "No se pudo procesar la solicitud aduanera", AduanasServicio.SOURCE, false));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        Integer usuarioId = getUsuarioId(req);
        if (usuarioId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            write(resp, RespuestaEnvoltorio.error("NO_SESSION", "Sesion requerida", AduanasServicio.SOURCE, false));
            return;
        }

        String path = req.getPathInfo();
        Map<String, Object> body = readBody(req);
        try {
            Object data;
            if ("/generar-expediente".equals(path)) {
                int operacionId = asInt(body.get("operacionId"), 0);
                validarPropietario(usuarioId, operacionId);
                data = service.generarExpediente(usuarioId, getSessionId(req), body, req.getRemoteAddr(), req.getHeader("User-Agent"));
            } else if ("/evaluar-regimen".equals(path)) {
                data = service.evaluarRegimen(body);
            } else if ("/evaluar-modalidad".equals(path)) {
                data = service.evaluarModalidad(body);
            } else if ("/registrar-manifiesto".equals(path)) {
                int operacionId = asInt(body.get("operacionId"), 0);
                validarPropietario(usuarioId, operacionId);
                data = service.registrarManifiesto(usuarioId, body);
            } else if ("/generar-predam".equals(path)) {
                int operacionId = asInt(body.get("operacionId"), 0);
                validarPropietario(usuarioId, operacionId);
                data = service.generarPredam(usuarioId, body);
            } else if ("/evaluar-reimportacion".equals(path)) {
                data = service.evaluarReimportacion(body);
            } else if ("/evaluar-transbordo".equals(path)) {
                data = service.evaluarTransbordo(body);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint aduanero no encontrado", AduanasServicio.SOURCE, false));
                return;
            }
            attachCsrf(req, data);
            EnvelopeMetadata meta = resolveEnvelopeMetadata(data);
            write(resp, RespuestaEnvoltorio.ok(data, meta.source, meta.sourceType, meta.confidence));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            write(resp, RespuestaEnvoltorio.error("FORBIDDEN", "No autorizado para esta operacion", AduanasServicio.SOURCE, false));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(resp, RespuestaEnvoltorio.error("BAD_REQUEST", "Solicitud aduanera invalida", AduanasServicio.SOURCE, false));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            write(resp, RespuestaEnvoltorio.error("ADUANAS_ERROR", "No se pudo procesar la solicitud aduanera", AduanasServicio.SOURCE, false));
        }
    }

    @SuppressWarnings("unchecked")
    private void attachCsrf(HttpServletRequest req, Object data) {
        if (data instanceof Map<?, ?> raw) {
            ((Map<String, Object>) raw).put("csrfToken", CsrfUtil.getToken(req.getSession(false)));
        }
    }

    private int getOperacionId(HttpServletRequest req) {
        String raw = req.getParameter("operacionId");
        if (raw == null || raw.isBlank()) raw = req.getParameter("id");
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("operacionId requerido");
        return Integer.parseInt(raw);
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

    private EnvelopeMetadata resolveEnvelopeMetadata(Object data) {
        String source = AduanasServicio.SOURCE;
        String sourceType = AduanasServicio.SOURCE_TYPE;
        double confidence = DataConfidenceServicio.confidenceFor(sourceType);

        Map<?, ?> candidate = metadataCandidate(data);
        if (candidate != null) {
            Object sourceValue = candidate.get("source");
            Object sourceTypeValue = candidate.get("sourceType");
            Object confidenceValue = candidate.get("confidence");

            if (sourceValue != null && !String.valueOf(sourceValue).isBlank()) {
                source = String.valueOf(sourceValue);
            }
            if (sourceTypeValue != null && !String.valueOf(sourceTypeValue).isBlank()) {
                sourceType = String.valueOf(sourceTypeValue);
            }
            confidence = confidenceValue instanceof Number n
                    ? n.doubleValue()
                    : DataConfidenceServicio.confidenceFor(sourceType);
        }

        return new EnvelopeMetadata(source, sourceType, confidence);
    }

    private Map<?, ?> metadataCandidate(Object data) {
        if (data instanceof Map<?, ?> map) {
            return map;
        }
        if (data instanceof Collection<?> collection && !collection.isEmpty()) {
            Object first = collection.iterator().next();
            if (first instanceof Map<?, ?> map) {
                return map;
            }
        }
        return null;
    }

    private void write(HttpServletResponse resp, Object payload) throws IOException {
        resp.getWriter().print(gson.toJson(payload));
    }

    private static final class EnvelopeMetadata {
        private final String source;
        private final String sourceType;
        private final double confidence;

        private EnvelopeMetadata(String source, String sourceType, double confidence) {
            this.source = source;
            this.sourceType = sourceType;
            this.confidence = confidence;
        }
    }

    private void validarPropietario(int usuarioId, int operacionId) {
        if (operacionId <= 0) {
            throw new IllegalArgumentException("ID de operacion invalido");
        }
        if (!new com.importease.proyecto.service.OperacionAutorizacionServicio().isOperacionOwnedByUser(operacionId, usuarioId)) {
            throw new SecurityException("No autorizado para esta operacion");
        }
    }

    private int asInt(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return fallback; }
    }
}
