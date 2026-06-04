package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.OperacionDAO;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.ArancelService;
import com.importease.proyecto.service.AuditoriaService;
import com.importease.proyecto.service.RiskScoringService;
import com.importease.proyecto.util.CsrfUtil;

@WebServlet("/api/operacion/*")
public class OperacionController extends HttpServlet {
    private final OperacionDAO opDao = new OperacionDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/historial".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("usuarioId") == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"No autenticado\"}");
                return;
            }
            int usuarioId = (Integer) session.getAttribute("usuarioId");

            List<Operacion> lista = opDao.listarPorUsuario(usuarioId);
            out.print(gson.toJson(lista));
        } else if ("/numerar".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            resp.setHeader("Allow", "POST");
            out.print("{\"error\":\"Method Not Allowed. Use POST /api/operacion/numerar\"}");
        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!CsrfUtil.validateRequest(req, resp)) return;
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/guardar".equals(path)) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            Map<String, Object> data = gson.fromJson(sb.toString(), new TypeToken<Map<String, Object>>(){}.getType());
            Operacion op = new Operacion();

            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("usuarioId") == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\":\"No autenticado\"}");
                return;
            }
            int sessionUsuarioId = (Integer) session.getAttribute("usuarioId");

            op.setUsuarioId(sessionUsuarioId);
            op.setProductoDesc((String) data.get("productoDesc"));
            op.setHsCode((String) data.get("hsCode"));
            op.setPaisOrigen((String) data.get("paisOrigen"));
            op.setIncoterm((String) data.get("incoterm"));
            op.setFob(new BigDecimal(data.get("fob").toString()));
            op.setFlete(new BigDecimal(data.get("flete").toString()));
            op.setSeguro(new BigDecimal(data.get("seguro").toString()));
            op.setTipoCambio(new BigDecimal(data.getOrDefault("tipoCambio", "1").toString()));
            op.setAdValoremAplicado(new BigDecimal(data.getOrDefault("adValorem", "0").toString()));
            op.setIscAplicado(new BigDecimal(data.getOrDefault("isc", "0").toString()));
            op.setIgvAplicado(new BigDecimal(data.getOrDefault("igv", "0").toString()));
            op.setIpmAplicado(new BigDecimal(data.getOrDefault("ipm", "0").toString()));
            op.setPercepcionAplicada(new BigDecimal(data.getOrDefault("percepcion", "0").toString()));
            op.setTotalImpuestos(new BigDecimal(data.getOrDefault("totalImpuestos", "0").toString()));
            op.setCanalAsignado((String) data.getOrDefault("canal", "PENDIENTE"));
            op.setEstado("PENDIENTE");

            boolean ok = opDao.guardar(op);
            out.print(gson.toJson(Map.of("success", ok, "id", op.getId())));
        } else if ("/numerar".equals(path)) {
            numerarOperacion(req, resp, out);
        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    private void numerarOperacion(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws IOException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            out.print("{\"error\":\"ID invalido\"}");
            return;
        }

        Operacion op = opDao.obtenerPorId(id);
        if (op == null) {
            out.print(gson.toJson(Map.of("success", false, "error", "Operacion no encontrada")));
            return;
        }

        HttpSession session = req.getSession(false);
        Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
        if (session == null || uIdAttr == null || (int) uIdAttr != op.getUsuarioId()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\":\"Acceso no autorizado a esta operacion\"}");
            return;
        }

        Usuario u = new UsuarioDAO().buscarPorId(op.getUsuarioId());
        HsCode hs = new ArancelService().consultarArancelLocal(op.getHsCode());
        RiskScoringService.ResultadoRiesgo risk = RiskScoringService.evaluarRiesgo(
            op.getFob().add(op.getFlete()).add(op.getSeguro()),
            hs,
            u
        );

        int year = Year.now().getValue();
        String correlativo = String.format("%06d", new SecureRandom().nextInt(999999));
        String numeroDam = "PRE-DAM-118-" + year + "-10-" + correlativo;

        boolean ok = opDao.actualizarDam(id, numeroDam, "NUMERADA", risk.getCanal());
        if (ok) {
            AuditoriaService.registrar(
                op.getUsuarioId(),
                "NUMERAR_DAM",
                "operaciones",
                id,
                "Numeracion DAM generada: " + numeroDam + " | canal=" + risk.getCanal(),
                req.getRemoteAddr(),
                req.getHeader("User-Agent")
            );
        }

        out.print(gson.toJson(Map.of(
            "success", ok,
            "numeroDam", numeroDam,
            "canal", risk.getCanal(),
            "mensaje", risk.getMensajeCanal()
        )));
    }
}


