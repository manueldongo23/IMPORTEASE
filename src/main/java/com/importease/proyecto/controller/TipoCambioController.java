/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.ResponseEnvelope;
import com.importease.proyecto.service.DataConfidenceService;
import com.importease.proyecto.service.TipoCambioService;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/tipoCambio")
public class TipoCambioController extends HttpServlet {
    private final TipoCambioService tcService = new TipoCambioService();
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
        double confidence = DataConfidenceService.confidenceFor(sourceType);
        Object explicitConfidence = detalle.get("confidence");
        if (explicitConfidence instanceof Number) {
            confidence = ((Number) explicitConfidence).doubleValue();
        }
        ResponseEnvelope envelope = ResponseEnvelope.ok(detalle, source, sourceType, confidence);
        out.print(gson.toJson(envelope));
    }
}


