package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import com.importease.proyecto.model.ResponseEnvelope;
import com.importease.proyecto.service.DataConfidenceService;
import com.importease.proyecto.service.FuentesRealesService;
import com.importease.proyecto.service.SunatBulkLoaderService;
import com.importease.proyecto.service.VeritradeIngestor;
import com.importease.proyecto.service.VUCEValidatorService;
import com.importease.proyecto.service.VucePamService;

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

@WebServlet("/api/fuentes/*")
public class FuentesController extends HttpServlet {
    private final Gson gson = new Gson();
    private final FuentesRealesService fuentesService = new FuentesRealesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        if (!isLoggedIn(req)) {
            resp.setStatus(401);
            write(resp, ResponseEnvelope.error("SESSION_REQUIRED", "Inicia sesion para consultar fuentes.", "BD_LOCAL", false));
            return;
        }

        String path = req.getPathInfo();
        try {
            if (path == null || "/estado".equals(path)) {
                writeOk(resp, fuentesService.estado(), "BD_LOCAL", "BD_LOCAL");
                return;
            }
            if ("/eventos".equals(path)) {
                int limite = parseInt(req.getParameter("limite"), 20);
                writeOk(resp, fuentesService.eventos(limite), "BD_LOCAL", "BD_LOCAL");
                return;
            }
            if ("/tipo-cambio".equals(path)) {
                Map<String, Object> data = fuentesService.tipoCambio();
                writeOk(resp, data, str(data.get("source"), "BCRP_API"), str(data.get("sourceType"), "OFICIAL_API"));
                return;
            }
            if ("/arancel".equals(path)) {
                Map<String, Object> data = fuentesService.arancel(req.getParameter("hs"));
                writeOk(resp, data, "SUNAT_ARANCEL", str(data.get("sourceType"), "BD_LOCAL"));
                return;
            }
            if ("/vuce".equals(path)) {
                Map<String, Object> data = fuentesService.vuce(req.getParameter("hs"));
                writeOk(resp, data, "VUCE_WEB", str(data.get("sourceType"), "BD_LOCAL"));
                return;
            }
            if ("/comtrade".equals(path)) {
                Map<String, Object> data = fuentesService.comtrade(req.getParameter("hs"));
                writeOk(resp, data, "UN_COMTRADE_API", str(data.get("sourceType"), "CACHE"));
                return;
            }
            if ("/pam".equals(path)) {
                Map<String, Object> data = fuentesService.pam(req.getParameter("hs"));
                writeOk(resp, data, "VUCE_PAM", str(data.get("sourceType"), "OFICIAL_WEB_PUBLIC"));
                return;
            }
            if ("/procedimientos".equals(path)) {
                Map<String, Object> data = fuentesService.vuceProcedimientos();
                writeOk(resp, data, "VUCE_PROCEDIMIENTOS", "OFICIAL_WEB_PUBLIC");
                return;
            }
            resp.setStatus(404);
            write(resp, ResponseEnvelope.error("ENDPOINT_NOT_FOUND", "Endpoint de fuentes no encontrado.", "BD_LOCAL", false));
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, ResponseEnvelope.error("FUENTES_QUERY_ERROR", "No se pudo consultar fuentes reales.", "BD_LOCAL", false));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupJson(resp);
        if (!isLoggedIn(req)) {
            resp.setStatus(401);
            write(resp, ResponseEnvelope.error("SESSION_REQUIRED", "Inicia sesion para consultar fuentes.", "BD_LOCAL", false));
            return;
        }

        String path = req.getPathInfo();
        Integer usuarioId = getUsuarioId(req);
        String sessionId = req.getSession(false) != null ? req.getSession(false).getId() : null;
        try {
            if ("/sincronizar-tipo-cambio".equals(path)) {
                Map<String, Object> data = fuentesService.sincronizarTipoCambio();
                writeOk(resp, data, str(data.get("source"), "BCRP_API"), str(data.get("sourceType"), "OFICIAL_API"));
                return;
            }
            if ("/tracking/consultar".equals(path)) {
                Map<String, Object> body = readBody(req);
                Map<String, Object> data = fuentesService.consultarTracking(usuarioId, sessionId, body, req.getRemoteAddr(), req.getHeader("User-Agent"));
                writeOk(resp, data, "TRACKING_API", str(data.get("sourceType"), "PENDIENTE_CREDENCIALES"));
                return;
            }
            if ("/veritrade/importar".equals(path)) {
                String formato = req.getParameter("formato");
                java.io.InputStream fileStream = req.getPart("archivo").getInputStream();
                String nombre = req.getPart("archivo").getSubmittedFileName();
                Map<String, Object> result;
                if ("csv".equalsIgnoreCase(formato)) {
                    result = new VeritradeIngestor().importarCSV(fileStream, nombre, usuarioId);
                } else {
                    result = new VeritradeIngestor().importarXLSX(fileStream, nombre, usuarioId);
                }
                writeOk(resp, result, "VERITRADE", "LICENSED_COMMERCIAL");
                return;
            }
            if ("/sunat/descargar-catalogo".equals(path)) {
                Map<String, Object> data = fuentesService.sunatDescargarCatalogo();
                writeOk(resp, data, "SUNAT_BULK", "OFICIAL_BULK");
                return;
            }
            resp.setStatus(404);
            write(resp, ResponseEnvelope.error("ENDPOINT_NOT_FOUND", "Endpoint de fuentes no encontrado.", "BD_LOCAL", false));
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);
            write(resp, ResponseEnvelope.error("FUENTES_BAD_REQUEST", e.getMessage(), "BD_LOCAL", false));
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, ResponseEnvelope.error("FUENTES_MUTATION_ERROR", "No se pudo procesar la fuente solicitada.", "BD_LOCAL", false));
        }
    }

    private void writeOk(HttpServletResponse resp, Object data, String source, String sourceType) throws IOException {
        write(resp, ResponseEnvelope.ok(data, source, sourceType, DataConfidenceService.confidenceFor(sourceType)));
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

    private boolean isLoggedIn(HttpServletRequest req) {
        return req.getSession(false) != null && req.getSession(false).getAttribute("usuarioId") != null;
    }

    private Integer getUsuarioId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object value = session != null ? session.getAttribute("usuarioId") : null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try { return value == null ? null : Integer.parseInt(String.valueOf(value)); } catch (Exception e) { return null; }
    }

    private int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception e) { return fallback; }
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


