package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.service.PanelServicio;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/dashboard/stats")
public class PanelControlador extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("usuarioId") != null) {
            int uid = (int) session.getAttribute("usuarioId");
            try {
                Map<String, Object> stats = PanelServicio.obtenerEstadisticas(uid);
                out.print(gson.toJson(stats));
            } catch (Exception e) {
                com.importease.proyecto.service.LoggerUtil.error("Error al obtener estadísticas del panel", e);
                resp.setStatus(500);
                out.print("{\"error\":\"Error interno al cargar las estadísticas del panel.\"}");
            }
        } else {
            resp.setStatus(401);
            out.print("{\"error\":\"No autenticado\"}");
        }
    }
}
