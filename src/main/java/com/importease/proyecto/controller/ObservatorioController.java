package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.ResponseEnvelope;
import com.importease.proyecto.service.DataConfidenceService;
import com.importease.proyecto.service.ObservatorioService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/observatorio/*")
public class ObservatorioController extends HttpServlet {
    private final Gson gson = new Gson();
    private final ObservatorioService observatorioService = new ObservatorioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"No autenticado\"}");
            return;
        }
        setupJson(resp);
        String path = req.getPathInfo();
        String codigo = req.getParameter("codigo");
        if (codigo == null || codigo.replaceAll("[^0-9]", "").length() < 4) {
            resp.setStatus(400);
            write(resp, ResponseEnvelope.error("HS_REQUIRED", "Ingresa un codigo HS valido", "UN_COMTRADE_API", false));
            return;
        }

        try {
            Map<String, Object> data;
            if (path == null || "/hs".equals(path)) {
                data = observatorioService.analizar(codigo);
            } else if ("/top-origenes".equals(path)) {
                data = observatorioService.topOrigenes(codigo);
            } else if ("/tendencia".equals(path)) {
                data = observatorioService.tendencia(codigo);
            } else if ("/oportunidad".equals(path)) {
                data = observatorioService.oportunidad(codigo);
            } else {
                resp.setStatus(404);
                write(resp, ResponseEnvelope.error("ENDPOINT_NOT_FOUND", "Endpoint de observatorio no encontrado", "UN_COMTRADE_API", false));
                return;
            }
            String sourceType = String.valueOf(data.getOrDefault("sourceType", "CACHE"));
            write(resp, ResponseEnvelope.ok(data, "UN_COMTRADE_API", sourceType, DataConfidenceService.confidenceFor(sourceType)));
        } catch (Exception e) {
            resp.setStatus(500);
            write(resp, ResponseEnvelope.error("OBSERVATORIO_ERROR", "No se pudo consultar observatorio HS", "UN_COMTRADE_API", true));
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


