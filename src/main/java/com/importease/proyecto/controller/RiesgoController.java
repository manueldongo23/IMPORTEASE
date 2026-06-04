/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.HsCodeDAO;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.RiskScoringService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet("/api/riesgo/*")
public class RiesgoController extends HttpServlet {
    private HsCodeDAO hsDao = new HsCodeDAO();
    private UsuarioDAO usuarioDao = new UsuarioDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/evaluar".equals(path)) {
            try {
                String hsCode = req.getParameter("hsCode");
                BigDecimal cif = new BigDecimal(req.getParameter("cif"));
                int usuarioId = Integer.parseInt(req.getParameter("usuarioId"));

                // Verificar que el usuarioId coincida con la sesiÃ³n
                HttpSession session = req.getSession(false);
                Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
                if (uIdAttr == null || (int) uIdAttr != usuarioId) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Acceso no autorizado\"}");
                    return;
                }

                HsCode hs = hsDao.obtenerPorCodigo(hsCode);
                Usuario usuario = usuarioDao.buscarPorId(usuarioId);
                RiskScoringService.ResultadoRiesgo riesgo = RiskScoringService.evaluarRiesgo(cif, hs, usuario);

                out.print(gson.toJson(riesgo));
            } catch (NumberFormatException | NullPointerException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"ParÃ¡metros invÃ¡lidos\"}");
            }
        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }
}
