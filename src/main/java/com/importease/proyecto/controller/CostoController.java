/*
 * NOTE: This controller provides server-side cost/tax calculation endpoints (/api/costo/calcular, /api/costo/comparar).
 * The main wizard flow (evaluacion.jsp + index.js) performs client-side tax calculation via CalculadoraTributaria
 * and does NOT call this API. This controller remains available for programmatic/legacy access.
 */
package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.HsCodeDAO;
import com.importease.proyecto.repository.UsuarioDAO;
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
public class CostoController extends HttpServlet {
    private HsCodeDAO hsDao = new HsCodeDAO();
    private UsuarioDAO usuarioDao = new UsuarioDAO();
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
                BigDecimal tipoCambio = new TipoCambioService().obtenerTipoCambio();

                CalculadoraTributaria.ResultadoTributario calc = CalculadoraTributaria.calcularImpuestos(
                    hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm, usado, regimen
                );
                RiskScoringService.ResultadoRiesgo riesgo = RiskScoringService.evaluarRiesgo(calc.getCif(), hs, usuario);

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
                out.print("{\"error\":\"ParÃ¡metros monetarios invÃ¡lidos\"}");
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

                // VerificaciÃ³n de AutorizaciÃ³n
                HttpSession session = req.getSession(false);
                Object uIdAttr = (session != null) ? session.getAttribute("usuarioId") : null;
                if (session == null || uIdAttr == null || (int) uIdAttr != usuarioId) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Acceso no autorizado. El ID de usuario no coincide con la sesiÃ³n.\"}");
                    return;
                }

                HsCode hs = hsDao.obtenerPorCodigo(hsCode);
                Usuario usuario = usuarioDao.buscarPorId(usuarioId);
                BigDecimal tipoCambio = new TipoCambioService().obtenerTipoCambio();

                // 1. Comparar Perfiles Fiscales (EstÃ¡ndar, Primera ImportaciÃ³n, Buen Contribuyente)
                ComparadorEscenariosService compService = new ComparadorEscenariosService();
                Map<String, CalculadoraTributaria.ResultadoTributario> perfiles = compService.compararPerfilesFiscales(
                    hs, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
                );

                // 2. Comparar TLC China (Con TLC vs Sin TLC)
                Map<String, CalculadoraTributaria.ResultadoTributario> tlcChina = compService.compararTlcChina(
                    hs, usuario, fob, flete, seguro, tipoCambio, incoterm
                );

                // 3. Comparar Despacho Anticipado vs Diferido (Anticipado vs Diferido + almacÃ©n extra $150 USD + riesgo)
                Map<String, Object> despacho = new HashMap<>();
                CalculadoraTributaria.ResultadoTributario anticipado = CalculadoraTributaria.calcularImpuestos(
                    hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
                );
                
                // Despacho diferido tiene un flete/costo logÃ­stico adicional en aduanas peruanas de $150 USD por almacenaje obligatorio
                BigDecimal fobDiferido = fob;
                BigDecimal fleteDiferido = flete.add(new BigDecimal("150.00")); // Sobrecargo por almacÃ©n temporal diferido
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
                out.print("{\"error\":\"ParÃ¡metros de comparaciÃ³n invÃ¡lidos\"}");
            }
        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null) return BigDecimal.ZERO;
        // Sanitizar eliminando espacios, comas, sÃ­mbolos de moneda
        String clean = val.replaceAll("[^0-9.\\-]", "");
        if (clean.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}

