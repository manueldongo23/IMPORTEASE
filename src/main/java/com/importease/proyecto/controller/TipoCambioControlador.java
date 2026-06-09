package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.RespuestaEnvoltorio;
import com.importease.proyecto.service.DataConfidenceServicio;
import com.importease.proyecto.service.TipoCambioServicio;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/tipoCambio")
public class TipoCambioControlador extends HttpServlet {
    private final TipoCambioServicio tcService = new TipoCambioServicio();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"No autenticado\"}");
            return;
        }
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Map<String, Object> detalle = tcService.obtenerTipoCambioDetalle();
        String source = String.valueOf(detalle.getOrDefault("source", "BCRP_API"));
        String sourceType = String.valueOf(detalle.getOrDefault("sourceType", "OFICIAL_API"));
        double confidence = DataConfidenceServicio.confidenceFor(sourceType);
        Object explicitConfidence = detalle.get("confidence");
        if (explicitConfidence instanceof Number) {
            confidence = ((Number) explicitConfidence).doubleValue();
        }
        RespuestaEnvoltorio envelope = RespuestaEnvoltorio.ok(detalle, source, sourceType, confidence);
        out.print(gson.toJson(envelope));
    }
}
