package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.service.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/api/vuce/*")
public class VuceController extends HttpServlet {
    private VUCEValidatorService validator = new VUCEValidatorService();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        switch (path) {
            case "/validar": {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                handleValidar(req, resp, out);
                break;
            }
            case "/procedimientos": {
                HttpSession session = req.getSession(false);
                if (session == null || session.getAttribute("usuario") == null) {
                    resp.setStatus(401);
                    resp.getWriter().write("{\"error\":\"No autenticado\"}");
                    return;
                }
                out.print(gson.toJson(validator.listarProcedimientos()));
                break;
            }
            case "/mercancias-restringidas":
                String partida = req.getParameter("partida");
                out.print(gson.toJson(validator.consultarMercanciasRestringidas(partida)));
                break;
            case "/verificar-dr":
                String num = req.getParameter("numero");
                String tipo = req.getParameter("tipo");
                out.print(gson.toJson(validator.verificarDocumentoResolutivo(num, tipo != null ? tipo : "DR")));
                break;
            case "/verificar-suce":
                String ruc = req.getParameter("ruc");
                String suce = req.getParameter("suce");
                out.print(gson.toJson(validator.verificarSUCE(ruc, suce)));
                break;
            default:
                resp.setStatus(404);
                out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    private void handleValidar(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException {
        String hsCode  = req.getParameter("hsCode");
        String producto = req.getParameter("producto");

        if (hsCode != null) {
            hsCode = hsCode.trim().replaceAll("[^\\d]", "");
            if (hsCode.length() > 12) hsCode = hsCode.substring(0, 12);
        }
        if (producto != null) {
            producto = producto.trim();
            if (producto.length() > 100) producto = producto.substring(0, 100);
            producto = com.importease.proyecto.service.HtmlUtil.escape(producto);
        }

        Map<String, Object> resultado = validator.validar(hsCode, producto);

        if (resultado != null && (boolean)resultado.getOrDefault("encontrado", false)) {
            Integer usuarioId = (Integer) req.getSession().getAttribute("usuarioId");
            if (usuarioId != null) {
                String finalCode = (String)resultado.get("codigo");
                com.importease.proyecto.model.HsCode hs = new com.importease.proyecto.service.ArancelService().consultarArancelLocal(finalCode);
                if (hs != null) {
                    new com.importease.proyecto.service.ArancelService().registrarAuditoria(usuarioId, hs, "AUDITORIA");
                }
            }
        }

        out.print(gson.toJson(resultado));
    }
}


