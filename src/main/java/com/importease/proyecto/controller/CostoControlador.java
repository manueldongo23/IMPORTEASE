package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.HsCodeRepositorio;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.service.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/costo/*")
public class CostoControlador extends HttpServlet {
    private HsCodeRepositorio hsDao = new HsCodeRepositorio();
    private UsuarioRepositorio usuarioDao = new UsuarioRepositorio();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/calcular".equals(path)) {
            try {
                String hsCode = req.getParameter("hsCode");
                BigDecimal fob = parseBigDecimal(req.getParameter("fob"));
                BigDecimal flete = parseBigDecimal(req.getParameter("flete"));
                BigDecimal seguro = parseBigDecimal(req.getParameter("seguro"));
                if (fob.compareTo(BigDecimal.ZERO) < 0 || flete.compareTo(BigDecimal.ZERO) < 0 || seguro.compareTo(BigDecimal.ZERO) < 0) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Los valores FOB, flete y seguro no pueden ser negativos.\"}");
                    return;
                }
                String paisOrigen = req.getParameter("paisOrigen");
                String incoterm = req.getParameter("incoterm");
                int usuarioId = Integer.parseInt(req.getParameter("usuarioId"));
                boolean usado = Boolean.parseBoolean(req.getParameter("usado"));
                String regimen = req.getParameter("regimen");

                // Verificación de Autorización
                HttpSession session = req.getSession(false);
                Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
                if (session == null || uIdAttr == null || (int) uIdAttr != usuarioId) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Acceso no autorizado. El ID de usuario no coincide con la sesión.\"}");
                    return;
                }

                HsCode hs = hsDao.obtenerPorCodigo(hsCode);
                Usuario usuario = usuarioDao.buscarPorId(usuarioId);
                BigDecimal tipoCambio = new TipoCambioServicio().obtenerTipoCambio();

                CalculadoraTributaria.ResultadoTributario calc = CalculadoraTributaria.calcularImpuestos(
                    hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, usado, regimen
                );
                RiskScoringServicio.ResultadoRiesgo riesgo = RiskScoringServicio.evaluarRiesgo(calc.getCif(), hs, usuario);

                Map<String, Object> response = new HashMap<>();
                response.put("cif", calc.getCif());
                response.put("arancel", calc.getArancel());
                response.put("isc", calc.getIsc());
                response.put("igv", calc.getIgv());
                response.put("percepcion", calc.getPercepcion());
                response.put("totalImpuestos", calc.getTotalImpuestos());
                response.put("totalNacionalizado", calc.getTotalNacionalizado());
                response.put("totalSoles", calc.getTotalSoles());
                response.put("canal", riesgo.getCanal());
                response.put("riesgo", riesgo.getScore());
                response.put("mensajeCanal", riesgo.getMensajeCanal());
                if (calc.getMensajeTlc() != null) response.put("mensajeTlc", calc.getMensajeTlc());
                if (calc.getMensajePercepcion() != null) response.put("mensajePercepcion", calc.getMensajePercepcion());

                out.print(gson.toJson(response));
            } catch (NumberFormatException | NullPointerException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Parámetros monetarios inválidos\"}");
            }
        } else if ("/comparar".equals(path)) {
            try {
                String hsCode = req.getParameter("hsCode");
                BigDecimal fob = parseBigDecimal(req.getParameter("fob"));
                BigDecimal flete = parseBigDecimal(req.getParameter("flete"));
                BigDecimal seguro = parseBigDecimal(req.getParameter("seguro"));
                String paisOrigen = req.getParameter("paisOrigen");
                String incoterm = req.getParameter("incoterm");
                int usuarioId = Integer.parseInt(req.getParameter("usuarioId"));

                // Verificación de Autorización
                HttpSession session = req.getSession(false);
                Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
                if (session == null || uIdAttr == null || (int) uIdAttr != usuarioId) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Acceso no autorizado. El ID de usuario no coincide con la sesión.\"}");
                    return;
                }

                HsCode hs = hsDao.obtenerPorCodigo(hsCode);
                Usuario usuario = usuarioDao.buscarPorId(usuarioId);
                BigDecimal tipoCambio = new TipoCambioServicio().obtenerTipoCambio();

                // 1. Comparar Perfiles Fiscales
                ComparadorEscenariosServicio compService = new ComparadorEscenariosServicio();
                Map<String, CalculadoraTributaria.ResultadoTributario> perfiles = compService.compararPerfilesFiscales(
                    hs, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
                );

                // 2. Comparar TLC China
                Map<String, CalculadoraTributaria.ResultadoTributario> tlcChina = compService.compararTlcChina(
                    hs, usuario, fob, flete, seguro, tipoCambio, incoterm
                );

                // 3. Comparar Despacho Anticipado vs Diferido
                Map<String, Object> despacho = new HashMap<>();
                CalculadoraTributaria.ResultadoTributario anticipado = CalculadoraTributaria.calcularImpuestos(
                    hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
                );
                
                BigDecimal fobDiferido = fob;
                BigDecimal fleteDiferido = flete.add(new BigDecimal("150.00"));
                CalculadoraTributaria.ResultadoTributario diferido = CalculadoraTributaria.calcularImpuestos(
                    hs, usuario, fobDiferido, fleteDiferido, seguro, paisOrigen, tipoCambio, incoterm
                );

                despacho.put("ANTICIPADO", anticipado);
                despacho.put("DIFERIDO", diferido);

                Map<String, Object> response = new HashMap<>();
                response.put("perfiles", perfiles);
                response.put("tlcChina", tlcChina);
                response.put("despacho", despacho);
                response.put("tipoCambio", tipoCambio);

                out.print(gson.toJson(response));
            } catch (NumberFormatException | NullPointerException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Parámetros de comparación inválidos\"}");
            }
        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null) return BigDecimal.ZERO;
        String clean = val.replaceAll("[^0-9.\\-]", "");
        if (clean.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
