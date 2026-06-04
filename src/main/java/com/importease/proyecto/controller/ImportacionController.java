package com.importease.proyecto.controller;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionDAO;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.HtmlUtil;
import com.importease.proyecto.service.ImportacionService;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.AduanasService;
import com.importease.proyecto.service.importacion.CotizacionRateLimiter;
import com.importease.proyecto.service.importacion.ImportacionStateMachine;

@WebServlet("/api/importacion/*")
public class ImportacionController extends HttpServlet {

    private ImportacionService importacionService = new ImportacionService();
    private ImportacionDAO importacionDAO = new ImportacionDAO();
    private Gson gson = new Gson();
    private final CotizacionRateLimiter cotizacionRateLimiter = new CotizacionRateLimiter();
    private final ImportacionStateMachine stateMachine = new ImportacionStateMachine();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(401);
            resp.getWriter().print("{\"error\":\"No autorizado\"}");
            return;
        }
        int usuarioId = (Integer) session.getAttribute("usuarioId");

        try {
            if ("/listar".equals(path)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                try (Connection con = ConexionDB.obtenerConexion()) {
                    List<Importacion> importaciones = importacionDAO.listarPorUsuario(con, usuarioId);
                    resp.getWriter().print(gson.toJson(importaciones));
                }
            } else if ("/estados".equals(path)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().print(gson.toJson(stateMachine.estadosPublicos()));
            } else if ("/dam/descargar".equals(path)) {
                int id;
                try {
                    id = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setContentType("application/json");
                    resp.setCharacterEncoding("UTF-8");
                    resp.setStatus(400);
                    resp.getWriter().print("{\"error\":\"ID invÃƒÂ¡lido\"}");
                    return;
                }
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionDAO.buscarPorId(con, id);
                    if (imp != null && imp.getUsuarioId() == usuarioId) {
                        com.importease.proyecto.model.Usuario u = new com.importease.proyecto.repository.UsuarioDAO().buscarPorId(usuarioId);
                        String razonSocial = (u != null) ? u.getRazonSocial() : (String) session.getAttribute("usuarioNombre");
                        String ruc = (u != null) ? u.getRuc() : (String) session.getAttribute("usuarioRuc");
                        
                        byte[] pdf = new com.importease.proyecto.service.PdfService().generarPdfDam(imp, razonSocial, ruc);
                        resp.setContentType("application/pdf");
                        resp.setHeader("Content-Disposition", "attachment; filename=PRE_DAM_" + imp.getNumeroDam() + ".pdf");
                        resp.setContentLength(pdf.length);
                        resp.getOutputStream().write(pdf);
                    } else {
                        resp.setContentType("application/json");
                        resp.setCharacterEncoding("UTF-8");
                        resp.setStatus(403);
                        resp.getWriter().print("{\"error\":\"No autorizado para ver este documento\"}");
                    }
                }
            } else {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doGet de ImportacionController", e);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno al procesar la solicitud de importaciÃƒÂ³n.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.setStatus(401);
            out.print("{\"error\":\"No autorizado\"}");
            return;
        }
        int usuarioId = (Integer) session.getAttribute("usuarioId");

        try {
            if ("/cotizar".equals(path)) {
                // Rate Limiting por IP para evitar spam/DoS en cotizaciones (Error 7)
                String ip = req.getRemoteAddr();
                if (cotizacionRateLimiter.isLimited(ip)) {
                    resp.setStatus(429);
                    out.print("{\"error\":\"Demasiadas solicitudes. Por favor, espera un minuto para volver a cotizar.\"}");
                    return;
                }

                // Nuevo flujo para el Wizard
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                
                Map<String, String> data = gson.fromJson(sb.toString(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
                
                String hsCode = data.get("hsCode");
                java.math.BigDecimal fob = new java.math.BigDecimal(data.get("fob"));
                java.math.BigDecimal flete = new java.math.BigDecimal(data.get("flete"));
                java.math.BigDecimal seguro = new java.math.BigDecimal(data.get("seguro"));
                
                if (fob.compareTo(java.math.BigDecimal.ZERO) < 0 || flete.compareTo(java.math.BigDecimal.ZERO) < 0 || seguro.compareTo(java.math.BigDecimal.ZERO) < 0) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Los valores FOB, flete o seguro no pueden ser negativos para liquidar impuestos.\"}");
                    return;
                }
                
                String tipo = data.getOrDefault("tipoRuta", data.getOrDefault("tipo", "COMERCIAL"));
                String productoDesc = HtmlUtil.escapeHtml(data.get("productoDesc"));
                String paisOrigen = HtmlUtil.escapeHtml(data.get("paisOrigen"));
                String incoterm = data.getOrDefault("incoterm", "FOB");
                boolean usado = data.containsKey("usado") && Boolean.parseBoolean(data.get("usado"));

                Importacion nueva = importacionService.generarOperacion(usuarioId, hsCode, fob, flete, seguro, tipo, productoDesc, paisOrigen, incoterm, usado);
                if (nueva != null && nueva.getId() > 0) {
                    com.importease.proyecto.service.AuditoriaService.registrar(usuarioId, "CREAR_OPERACION", "operaciones", nueva.getId(),
                        "CreaciÃƒÂ³n de nueva operaciÃƒÂ³n de importaciÃƒÂ³n #" + nueva.getId() + " (" + tipo + ") - Producto: " + nueva.getProductoDesc() + " - HS: " + nueva.getHsCode() + " - CIF: $" + nueva.getValorCif(),
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                }
                out.print(gson.toJson(nueva));

            } else if ("/avanzar".equals(path)) {
                // Flujo para el Gestor LogÃƒÂ­stico (DOCS_PENDIENTES -> LISTA_DESPACHO)
                int importacionId;
                try {
                    importacionId = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ID invÃƒÂ¡lido\"}");
                    return;
                }
                
                // Security check (IDOR protection)
                String estadoActual;
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionDAO.buscarPorId(con, importacionId);
                    if (imp == null || imp.getUsuarioId() != usuarioId) {
                        resp.setStatus(403);
                        out.print("{\"error\":\"No autorizado para modificar este recurso\"}");
                        return;
                    }
                    estadoActual = imp.getEstado();
                }

                // Validar que solo se avance desde DOCS_PENDIENTES
                if (!"DOCS_PENDIENTES".equals(estadoActual)) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Solo se puede avanzar a Lista para Despacho desde Documentos Pendientes. Estado actual: " + estadoActual + "\"}");
                    return;
                }
                
                boolean success = importacionService.cambiarEstado(importacionId, com.importease.proyecto.model.EstadoImportacion.LISTA_DESPACHO.name());
                if (success) {
                    com.importease.proyecto.service.AuditoriaService.registrar(usuarioId, "CAMBIO_ESTADO", "operaciones", importacionId,
                        "OperaciÃƒÂ³n #" + importacionId + " avanzada a LISTA_DESPACHO (documentos verificados exitosamente).",
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                    // QA-054: Trigger DTA recalculation after successful state change
                    try (Connection con = ConexionDB.obtenerConexion()) {
                        Importacion imp = importacionDAO.buscarPorId(con, importacionId);
                        if (imp != null) {
                            new com.importease.proyecto.service.AduanasService().generarExpediente(usuarioId, req.getRequestedSessionId(),
                                Map.of("operacionId", String.valueOf(importacionId)), req.getRemoteAddr(), req.getHeader("User-Agent"));
                        }
                    } catch (Exception dtaEx) {
                        com.importease.proyecto.service.LoggerUtil.warn("Error al recalcular DTA tras avance: " + dtaEx.getMessage());
                    }
                    out.print("{\"success\":true}");
                } else {
                    resp.setStatus(400);
                    out.print("{\"success\":false, \"error\":\"Faltan documentos obligatorios o error en el servidor.\"}");
                }
            } else if ("/cambiarEstado".equals(path)) {
                // QA-050: TransiciÃƒÂ³n controlada por mÃƒÂ¡quina de estados
                int importacionId;
                try {
                    importacionId = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ID invÃƒÂ¡lido\"}");
                    return;
                }
                String nuevoEstado = req.getParameter("nuevoEstado");
                if (nuevoEstado == null || nuevoEstado.isBlank()) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"nuevoEstado es requerido\"}");
                    return;
                }
                
                // Security check (IDOR protection)
                String estadoActual;
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionDAO.buscarPorId(con, importacionId);
                    if (imp == null || imp.getUsuarioId() != usuarioId) {
                        resp.setStatus(403);
                        out.print("{\"error\":\"No autorizado para modificar este recurso\"}");
                        return;
                    }
                    estadoActual = imp.getEstado();
                }

                // Validar transiciÃƒÂ³n en mÃƒÂ¡quina de estados
                if (!stateMachine.permite(estadoActual, nuevoEstado)) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Transicion de estado no permitida. De " + estadoActual + " solo se permite: " + stateMachine.describirPermitidos(estadoActual) + "\"}");
                    return;
                }

                boolean success = importacionService.cambiarEstado(importacionId, nuevoEstado);
                
                if (success) {
                    com.importease.proyecto.service.AuditoriaService.registrar(usuarioId, "CAMBIO_ESTADO", "operaciones", importacionId,
                        "OperaciÃƒÂ³n #" + importacionId + " cambiÃƒÂ³ de estado a " + nuevoEstado,
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                    // QA-055: Ensure PDF download is updated after NACIONALIZADA
                    if ("NACIONALIZADA".equals(nuevoEstado)) {
                        try (Connection con = ConexionDB.obtenerConexion()) {
                            new com.importease.proyecto.service.AduanasService().generarExpediente(usuarioId, req.getRequestedSessionId(),
                                Map.of("operacionId", String.valueOf(importacionId)), req.getRemoteAddr(), req.getHeader("User-Agent"));
                        } catch (Exception pdfEx) {
                            com.importease.proyecto.service.LoggerUtil.warn("Error al actualizar expediente tras nacionalizacion: " + pdfEx.getMessage());
                        }
                    }
                    out.print("{\"success\":true}");
                }
                else { resp.setStatus(400); out.print("{\"error\":\"No se pudo actualizar el estado\"}"); }
            } else if ("/registrarDocumentoOperacion".equals(path)) {
                // Simula que el documento subido (Invoice/BL/Cert) fue cargado y validado
                int importacionId;
                try {
                    importacionId = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ID invÃƒÂ¡lido\"}");
                    return;
                }
                
                // Security check (IDOR protection)
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionDAO.buscarPorId(con, importacionId);
                    if (imp == null || imp.getUsuarioId() != usuarioId) {
                        resp.setStatus(403);
                        out.print("{\"error\":\"No autorizado para modificar este recurso\"}");
                        return;
                    }
                }

                String tipo = req.getParameter("tipo"); // FACTURA_COMERCIAL, BILL_OF_LADING, CERTIFICADO_ORIGEN
                if (tipo == null || (!"FACTURA_COMERCIAL".equals(tipo) && !"BILL_OF_LADING".equals(tipo) && !"CERTIFICADO_ORIGEN".equals(tipo))) {
                     resp.setStatus(400);
                     out.print("{\"error\":\"Tipo de documento no vÃƒÂ¡lido o no autorizado\"}");
                     return;
                }
                try (Connection con = ConexionDB.obtenerConexion()) {
                    boolean success = importacionDAO.marcarDocumentoSubido(con, importacionId, tipo);
                    boolean tieneArchivos = importacionDAO.tieneArchivoDocumento(con, importacionId, tipo);

                    if (success && !tieneArchivos) {
                        out.print("{\"success\":true,\"warning\":\"Documento marcado pero sin archivo adjunto\",\"tieneArchivos\":false}");
                    } else {
                        out.print("{\"success\":" + success + "}");
                    }
                }
            } else {
                resp.setStatus(404);
                out.print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doPost de ImportacionController", e);
            resp.setStatus(500);
            out.print("{\"error\":\"Error interno al procesar la cotizaciÃƒÂ³n o actualizaciÃƒÂ³n.\"}");
        }
    }
}


