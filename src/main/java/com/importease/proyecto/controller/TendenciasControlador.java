package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.model.RespuestaEnvoltorio;
import com.importease.proyecto.repository.BusquedaRepositorio;
import com.importease.proyecto.service.DataConfidenceServicio;
import com.importease.proyecto.service.EventoUsuarioServicio;
import com.importease.proyecto.service.FuenteEventoServicio;
import com.importease.proyecto.service.LoggerUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/tendencias/*")
public class TendenciasControlador extends HttpServlet {
    private final Gson gson = new Gson();
    private final BusquedaRepositorio busquedaRepositorio = new BusquedaRepositorio();
    private final EventoUsuarioServicio eventoUsuarioServicio = new EventoUsuarioServicio();
    private final FuenteEventoServicio fuenteEventoServicio = new FuenteEventoServicio();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        String path = req.getPathInfo();
        if (!"/registrar".equals(path)) {
            resp.setStatus(404);
            write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint de tendencias no encontrado", "BD_LOCAL", false));
            return;
        }

        try {
            Map<String, Object> body = gson.fromJson(readBody(req), new TypeToken<Map<String, Object>>(){}.getType());
            String termino = clean(asString(body, "termino"));
            String hsCode = clean(asString(body, "hsCode"));
            String tipo = clean(asString(body, "tipo"));

            if (termino == null || termino.isBlank()) {
                resp.setStatus(400);
                write(resp, RespuestaEnvoltorio.error("EMPTY_TERM", "No se puede registrar una busqueda vacia", "BD_LOCAL", false));
                return;
            }

            Integer usuarioId = getUsuarioId(req);
            String tipoSeguro = (tipo == null || tipo.isBlank()) ? "PRODUCTO" : tipo.toUpperCase();
            busquedaRepositorio.registrar(usuarioId, termino, hsCode, tipoSeguro);

            Map<String, Object> detalle = new LinkedHashMap<>();
            detalle.put("termino", termino);
            detalle.put("terminoNormalizado", normalize(termino));
            detalle.put("hsSugerido", hsCode);
            detalle.put("capituloHs", hsCode != null && hsCode.length() >= 2 ? hsCode.substring(0, 2) : null);
            detalle.put("moduloOrigen", clean(asString(body, "moduloOrigen")));
            detalle.put("paisOrigen", clean(asString(body, "paisOrigen")));
            detalle.put("valorFobEstimado", body != null ? body.get("valorFobEstimado") : null);

            eventoUsuarioServicio.registrar(
                    usuarioId,
                    req.getSession(false) != null ? req.getSession(false).getId() : null,
                    "BUSQUEDA_HS",
                    "buscador",
                    "hs_code",
                    hsCode,
                    gson.toJson(detalle),
                    req.getRemoteAddr(),
                    req.getHeader("User-Agent")
            );

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("registrado", true);
            data.put("terminoNormalizado", normalize(termino));
            data.put("hsCode", hsCode);
            write(resp, RespuestaEnvoltorio.ok(data, "BD_LOCAL", "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
        } catch (Exception e) {
            LoggerUtil.error("Error registrando tendencia", e);
            fuenteEventoServicio.registrarError("BD_LOCAL", "TENDENCIA_REGISTRAR", null, null, "POST", 500, e.getMessage(), 0);
            resp.setStatus(500);
            write(resp, RespuestaEnvoltorio.error("TRENDS_REGISTER_ERROR", "No se pudo registrar la tendencia", "BD_LOCAL", false));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        String path = req.getPathInfo();
        int dias = parseInt(req.getParameter("dias"), 30, 1, 365);
        int limit = parseInt(req.getParameter("limit"), 10, 1, 100);

        try {
            Object data;
            if ("/top-productos".equals(path) || "/top-busquedas".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                data = busquedaRepositorio.topProductosBuscados(dias, limit);
            } else if ("/top-hs".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                data = busquedaRepositorio.topHs(dias, limit);
            } else if ("/top-capitulos".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                data = busquedaRepositorio.topCategorias(dias, limit);
            } else if ("/conversion".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                data = busquedaRepositorio.conversionBasica(dias);
            } else if ("/funnel".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                data = busquedaRepositorio.eventosPorTipo(dias, limit);
            } else if ("/abandono".equals(path)) {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                data = busquedaRepositorio.eventosPorTipo(dias, limit);
            } else if ("/ultimas".equals(path)) {
                Integer uid = getUsuarioId(req);
                if (uid != null) {
                    java.util.List<java.util.Map<String, Object>> all = busquedaRepositorio.ultimasBusquedas(limit);
                    data = all.stream().filter(m -> uid.equals(m.get("usuarioId"))).collect(java.util.stream.Collectors.toList());
                } else {
                    data = java.util.Collections.emptyList();
                }
            } else {
                resp.setStatus(404);
                write(resp, RespuestaEnvoltorio.error("ENDPOINT_NOT_FOUND", "Endpoint de tendencias no encontrado", "BD_LOCAL", false));
                return;
            }
            write(resp, RespuestaEnvoltorio.ok(data, "BD_LOCAL", "BD_LOCAL", DataConfidenceServicio.confidenceFor("BD_LOCAL")));
        } catch (Exception e) {
            LoggerUtil.error("Error consultando tendencias", e);
            resp.setStatus(500);
            write(resp, RespuestaEnvoltorio.error("TRENDS_QUERY_ERROR", "No se pudieron consultar las tendencias", "BD_LOCAL", false));
        }
    }

    private Integer getUsuarioId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("usuarioId") : null;
        return value instanceof Integer ? (Integer) value : null;
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String asString(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) return null;
        return String.valueOf(body.get(key));
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.replaceAll("<[^>]*>", "").replaceAll("[\\r\\n\\t]+", " ").trim();
        return cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned;
    }

    private String normalize(String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }

    private int parseInt(String value, int fallback, int min, int max) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(min, Math.min(max, parsed));
        } catch (Exception e) {
            return fallback;
        }
    }

    private void setupJson(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    private void write(HttpServletResponse resp, Object payload) throws IOException {
        resp.getWriter().print(gson.toJson(payload));
    }
}
