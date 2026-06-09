package com.importease.proyecto.controller;

/** @deprecated Replaced by evaluacion.jsp + index.js (6-step client-side wizard). Will be removed in v2.0. */

import com.importease.proyecto.repository.HsCodeRepositorio;
import com.importease.proyecto.repository.OperacionRepositorio;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.service.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;

@Deprecated
@WebServlet("/wizard")
public class WizardServlet extends HttpServlet {
    private HsCodeRepositorio hsDao = new HsCodeRepositorio();
    private OperacionRepositorio opDao = new OperacionRepositorio();
    private UsuarioRepositorio usuarioDao = new UsuarioRepositorio();

    private Integer getAuthenticatedUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return null;
        }
        return (Integer) session.getAttribute("usuarioId");
    }

    private boolean validateStepSequence(HttpServletRequest req, HttpServletResponse resp, String step) throws IOException {
        HttpSession session = req.getSession();
        int stepNum = 1;
        try {
            if (step != null) stepNum = Integer.parseInt(step);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Paso invalido");
            return false;
        }

        if (stepNum > 1 && session.getAttribute("productoDesc") == null) {
            resp.sendRedirect(req.getContextPath() + "/wizard?step=1");
            return false;
        }
        if (stepNum > 2 && session.getAttribute("hsCode") == null) {
            resp.sendRedirect(req.getContextPath() + "/wizard?step=2");
            return false;
        }
        if (stepNum > 3 && (session.getAttribute("paisOrigen") == null || session.getAttribute("incoterm") == null)) {
            resp.sendRedirect(req.getContextPath() + "/wizard?step=3");
            return false;
        }
        if (stepNum > 4 && (session.getAttribute("fob") == null || session.getAttribute("flete") == null || session.getAttribute("seguro") == null)) {
            resp.sendRedirect(req.getContextPath() + "/wizard?step=4");
            return false;
        }
        if (stepNum > 6 && (session.getAttribute("calculo") == null || session.getAttribute("riesgo") == null)) {
            resp.sendRedirect(req.getContextPath() + "/wizard?step=6");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("X-Deprecated-Endpoint", "Use evaluacion.jsp instead");
        if (getAuthenticatedUserId(req, resp) == null) return;

        String step = req.getParameter("step");
        if (!validateStepSequence(req, resp, step)) return;

        String jsp;
        if (step == null) step = "1";
        switch (step) {
            case "1": jsp = "/WEB-INF/wizard/step1.jsp"; break;
            case "2": jsp = "/WEB-INF/wizard/step2.jsp"; break;
            case "3": jsp = "/WEB-INF/wizard/step3.jsp"; break;
            case "4": jsp = "/WEB-INF/wizard/step4.jsp"; break;
            case "5": jsp = "/WEB-INF/wizard/step5.jsp"; break;
            case "6": jsp = "/WEB-INF/wizard/step6.jsp"; break;
            case "7": jsp = "/WEB-INF/wizard/step7.jsp"; break;
            default: jsp = "/WEB-INF/wizard/step1.jsp";
        }
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("X-Deprecated-Endpoint", "Use evaluacion.jsp instead");
        Integer authenticatedUserId = getAuthenticatedUserId(req, resp);
        if (authenticatedUserId == null) return;

        String step = req.getParameter("step");
        if (!validateStepSequence(req, resp, step)) return;

        HttpSession session = req.getSession();

        switch (step) {
            case "1":
                session.setAttribute("productoDesc", req.getParameter("productoDesc"));
                break;
            case "2":
                session.setAttribute("hsCode", req.getParameter("hsCode"));
                break;
            case "3":
                session.setAttribute("paisOrigen", req.getParameter("paisOrigen"));
                session.setAttribute("incoterm", req.getParameter("incoterm"));
                break;
            case "4":
                try {
                    session.setAttribute("fob", new BigDecimal(req.getParameter("fob")));
                    session.setAttribute("flete", new BigDecimal(req.getParameter("flete")));
                    session.setAttribute("seguro", new BigDecimal(req.getParameter("seguro")));
                } catch (NumberFormatException | NullPointerException e) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Valores monetarios invalidos");
                    return;
                }
                break;
            case "5":
                String hsCode = (String) session.getAttribute("hsCode");
                VuceValidadorServicio validator = new VuceValidadorServicio();
                session.setAttribute("vuceResultado", validator.validar(hsCode));
                break;
            case "6":
                String hsCodeCalc = (String) session.getAttribute("hsCode");
                BigDecimal fob = (BigDecimal) session.getAttribute("fob");
                BigDecimal flete = (BigDecimal) session.getAttribute("flete");
                BigDecimal seguro = (BigDecimal) session.getAttribute("seguro");
                String paisOrigen = (String) session.getAttribute("paisOrigen");
                int usuarioId = authenticatedUserId;

                com.importease.proyecto.model.HsCode hs = hsDao.obtenerPorCodigo(hsCodeCalc);
                com.importease.proyecto.model.Usuario usuario = usuarioDao.buscarPorId(usuarioId);
                BigDecimal tipoCambio = new TipoCambioServicio().obtenerTipoCambio();

                String incoterm = (String) session.getAttribute("incoterm");
                CalculadoraTributaria.ResultadoTributario calc = CalculadoraTributaria.calcularImpuestos(
                    hs, usuario, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
                );
                RiskScoringServicio.ResultadoRiesgo riesgo = RiskScoringServicio.evaluarRiesgo(calc.getCif(), hs, usuario);

                session.setAttribute("calculo", calc);
                session.setAttribute("riesgo", riesgo);
                session.setAttribute("canal", riesgo.getCanal());
                break;
            case "7":
                String productoDesc = (String) session.getAttribute("productoDesc");
                String hsCodeFinal = (String) session.getAttribute("hsCode");
                String paisOrigenFinal = (String) session.getAttribute("paisOrigen");
                String incotermFinal = (String) session.getAttribute("incoterm");
                BigDecimal fobFinal = (BigDecimal) session.getAttribute("fob");
                BigDecimal fleteFinal = (BigDecimal) session.getAttribute("flete");
                BigDecimal seguroFinal = (BigDecimal) session.getAttribute("seguro");
                CalculadoraTributaria.ResultadoTributario calcFinal = (CalculadoraTributaria.ResultadoTributario) session.getAttribute("calculo");
                RiskScoringServicio.ResultadoRiesgo riesgoFinal = (RiskScoringServicio.ResultadoRiesgo) session.getAttribute("riesgo");
                if (productoDesc == null || hsCodeFinal == null || paisOrigenFinal == null || incotermFinal == null
                        || fobFinal == null || fleteFinal == null || seguroFinal == null
                        || calcFinal == null || riesgoFinal == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Debes completar todos los pasos anteriores antes de finalizar.");
                    return;
                }
                BigDecimal tipoCambioFinal = new TipoCambioServicio().obtenerTipoCambio();
                int usuarioIdFinal = authenticatedUserId;

                com.importease.proyecto.model.Operacion op = new com.importease.proyecto.model.Operacion();
                op.setUsuarioId(usuarioIdFinal);
                op.setProductoDesc(productoDesc);
                op.setHsCode(hsCodeFinal);
                op.setPaisOrigen(paisOrigenFinal);
                op.setIncoterm(incotermFinal);
                op.setFob(fobFinal);
                op.setFlete(fleteFinal);
                op.setSeguro(seguroFinal);
                op.setTipoCambio(tipoCambioFinal);
                op.setAdValoremAplicado(calcFinal.getArancel());
                op.setIgvAplicado(calcFinal.getIgv());
                op.setPercepcionAplicada(calcFinal.getPercepcion());
                op.setTotalImpuestos(calcFinal.getTotalImpuestos());
                op.setCanalAsignado(riesgoFinal.getCanal());
                op.setEstado("REGISTRADA");

                boolean ok = opDao.guardar(op);
                if (ok) {
                    session.removeAttribute("productoDesc");
                    session.removeAttribute("hsCode");
                    session.removeAttribute("paisOrigen");
                    session.removeAttribute("incoterm");
                    session.removeAttribute("fob");
                    session.removeAttribute("flete");
                    session.removeAttribute("seguro");
                    session.removeAttribute("calculo");
                    session.removeAttribute("riesgo");
                    session.removeAttribute("canal");
                    resp.sendRedirect(req.getContextPath() + "/historial.jsp");
                    return;
                }
                break;
        }
        int nextStep = Integer.parseInt(step) + 1;
        resp.sendRedirect(req.getContextPath() + "/wizard?step=" + nextStep);
    }
}
