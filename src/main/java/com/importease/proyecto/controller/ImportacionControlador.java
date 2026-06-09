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
import com.importease.proyecto.repository.ImportacionRepositorio;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.HtmlUtil;
import com.importease.proyecto.service.ImportacionServicio;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.AduanasServicio;
import com.importease.proyecto.service.importacion.CotizacionRateLimiter;
import com.importease.proyecto.service.importacion.ImportacionStateMachine;

@WebServlet("/api/importacion/*")
public class ImportacionControlador extends HttpServlet {

    private final ImportacionServicio importacionServicio = new ImportacionServicio();
    private final ImportacionRepositorio importacionRepositorio = new ImportacionRepositorio();
    private final Gson gson = new Gson();
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
                    List<Importacion> importaciones = importacionRepositorio.listarPorUsuario(con, usuarioId);
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
                    resp.getWriter().print("{\"error\":\"ID invalido\"}");
                    return;
                }
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionRepositorio.buscarPorId(con, id);
                    if (imp != null && imp.getUsuarioId() == usuarioId) {
                        com.importease.proyecto.model.Usuario u = new UsuarioRepositorio().buscarPorId(usuarioId);
                        String razonSocial = (u != null) ? u.getRazonSocial() : (String) session.getAttribute("usuarioNombre");
                        String ruc = (u != null) ? u.getRuc() : (String) session.getAttribute("usuarioRuc");
                        
                        byte[] pdf = new com.importease.proyecto.service.PdfServicio().generarPdfDam(imp, razonSocial, ruc);
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
            } else if ("/obtenerBorrador".equals(path)) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                com.importease.proyecto.model.Borrador borrador = new com.importease.proyecto.repository.BorradorRepositorio().obtenerBorrador(usuarioId);
                if (borrador != null) {
                    resp.getWriter().print(gson.toJson(borrador));
                } else {
                    resp.setStatus(404);
                    resp.getWriter().print("{\"error\":\"No se encontró borrador\"}");
                }
            } else {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doGet de ImportacionControlador", e);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error interno al procesar la solicitud de importacion.\"}");
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
                String ip = req.getRemoteAddr();
                if (cotizacionRateLimiter.isLimited(ip)) {
                    resp.setStatus(429);
                    out.print("{\"error\":\"Demasiadas solicitudes. Por favor, espera un minuto para volver a cotizar.\"}");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }
                
                Map<String, String> data = gson.fromJson(sb.toString(), new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
                
                String hsCode = data.get("hsCode");
                String fobStr = data.get("fob");
                String fleteStr = data.get("flete");
                String seguroStr = data.get("seguro");
                if (fobStr == null || fleteStr == null || seguroStr == null) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Los valores FOB, flete y seguro son requeridos.\"}");
                    return;
                }
                java.math.BigDecimal fob = new java.math.BigDecimal(fobStr);
                java.math.BigDecimal flete = new java.math.BigDecimal(fleteStr);
                java.math.BigDecimal seguro = new java.math.BigDecimal(seguroStr);
                
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

                Importacion nueva = importacionServicio.generarOperacion(usuarioId, hsCode, fob, flete, seguro, tipo, productoDesc, paisOrigen, incoterm, usado);
                if (nueva != null && nueva.getId() > 0) {
                    com.importease.proyecto.service.AuditoriaServicio.registrar(usuarioId, "CREAR_OPERACION", "operaciones", nueva.getId(),
                        "Creacion de nueva operacion de importacion #" + nueva.getId() + " (" + tipo + ") - Producto: " + nueva.getProductoDesc() + " - HS: " + nueva.getHsCode() + " - CIF: $" + nueva.getValorCif(),
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                    // Clear the temporary draft once confirmed
                    new com.importease.proyecto.repository.BorradorRepositorio().eliminarBorrador(usuarioId);
                }
                out.print(gson.toJson(nueva));

            } else if ("/avanzar".equals(path)) {
                int importacionId;
                try {
                    importacionId = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ID invalido\"}");
                    return;
                }
                
                String estadoActual;
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionRepositorio.buscarPorId(con, importacionId);
                    if (imp == null || imp.getUsuarioId() != usuarioId) {
                        resp.setStatus(403);
                        out.print("{\"error\":\"No autorizado para modificar este recurso\"}");
                        return;
                    }
                    estadoActual = imp.getEstado();
                }

                if (!"DOCS_PENDIENTES".equals(estadoActual)) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Solo se puede avanzar a Lista para Despacho desde Documentos Pendientes. Estado actual: " + estadoActual + "\"}");
                    return;
                }
                
                boolean success = importacionServicio.cambiarEstado(importacionId, com.importease.proyecto.model.EstadoImportacion.LISTA_DESPACHO.name());
                if (success) {
                    com.importease.proyecto.service.AuditoriaServicio.registrar(usuarioId, "CAMBIO_ESTADO", "operaciones", importacionId,
                        "Operacion #" + importacionId + " avanzada a LISTA_DESPACHO (documentos verificados exitosamente).",
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                    try (Connection con = ConexionDB.obtenerConexion()) {
                        Importacion imp = importacionRepositorio.buscarPorId(con, importacionId);
                        if (imp != null) {
                            new com.importease.proyecto.service.AduanasServicio().generarExpediente(usuarioId, req.getRequestedSessionId(),
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
                int importacionId;
                try {
                    importacionId = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ID invalido\"}");
                    return;
                }
                String nuevoEstado = req.getParameter("nuevoEstado");
                if (nuevoEstado == null || nuevoEstado.isBlank()) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"nuevoEstado es requerido\"}");
                    return;
                }
                
                String estadoActual;
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionRepositorio.buscarPorId(con, importacionId);
                    if (imp == null || imp.getUsuarioId() != usuarioId) {
                        resp.setStatus(403);
                        out.print("{\"error\":\"No autorizado para modificar este recurso\"}");
                        return;
                    }
                    estadoActual = imp.getEstado();
                }

                if (!stateMachine.permite(estadoActual, nuevoEstado)) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Transicion de estado no permitida. De " + estadoActual + " solo se permite: " + stateMachine.describirPermitidos(estadoActual) + "\"}");
                    return;
                }

                boolean success = importacionServicio.cambiarEstado(importacionId, nuevoEstado);
                
                if (success) {
                    com.importease.proyecto.service.AuditoriaServicio.registrar(usuarioId, "CAMBIO_ESTADO", "operaciones", importacionId,
                        "Operacion #" + importacionId + " cambio de estado a " + nuevoEstado,
                        req.getRemoteAddr(), req.getHeader("User-Agent"));
                    if ("NACIONALIZADA".equals(nuevoEstado)) {
                        try (Connection con = ConexionDB.obtenerConexion()) {
                            new com.importease.proyecto.service.AduanasServicio().generarExpediente(usuarioId, req.getRequestedSessionId(),
                                Map.of("operacionId", String.valueOf(importacionId)), req.getRemoteAddr(), req.getHeader("User-Agent"));
                        } catch (Exception pdfEx) {
                            com.importease.proyecto.service.LoggerUtil.warn("Error al actualizar expediente tras nacionalizacion: " + pdfEx.getMessage());
                        }
                    }
                    out.print("{\"success\":true}");
                }
                else { resp.setStatus(400); out.print("{\"error\":\"No se pudo actualizar el estado\"}"); }
            } else if ("/registrarDocumentoOperacion".equals(path)) {
                int importacionId;
                try {
                    importacionId = Integer.parseInt(req.getParameter("id"));
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ID invalido\"}");
                    return;
                }
                
                try (Connection con = ConexionDB.obtenerConexion()) {
                    Importacion imp = importacionRepositorio.buscarPorId(con, importacionId);
                    if (imp == null || imp.getUsuarioId() != usuarioId) {
                        resp.setStatus(403);
                        out.print("{\"error\":\"No autorizado para modificar este recurso\"}");
                        return;
                    }
                }

                String tipo = req.getParameter("tipo");
                if (tipo == null || (!"FACTURA_COMERCIAL".equals(tipo) && !"BILL_OF_LADING".equals(tipo) && !"CERTIFICADO_ORIGEN".equals(tipo))) {
                     resp.setStatus(400);
                     out.print("{\"error\":\"Tipo de documento no valido o no autorizado\"}");
                     return;
                }
                try (Connection con = ConexionDB.obtenerConexion()) {
                    boolean success = importacionRepositorio.marcarDocumentoSubido(con, importacionId, tipo);
                    boolean tieneArchivos = importacionRepositorio.tieneArchivoDocumento(con, importacionId, tipo);

                    if (success && !tieneArchivos) {
                        out.print("{\"success\":true,\"warning\":\"Documento marcado pero sin archivo adjunto\",\"tieneArchivos\":false}");
                    } else {
                        out.print("{\"success\":" + success + "}");
                    }
                }
            } else if ("/guardarBorrador".equals(path)) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                
                Map<String, Object> body = gson.fromJson(sb.toString(), new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
                if (body == null || !body.containsKey("pasoActual") || !body.containsKey("jsonBorrador")) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Datos incompletos para guardar el borrador\"}");
                    return;
                }
                
                double pasoDouble = (Double) body.get("pasoActual");
                int pasoActual = (int) pasoDouble;
                String jsonBorrador = (String) body.get("jsonBorrador");
                
                boolean success = new com.importease.proyecto.repository.BorradorRepositorio().guardarBorrador(usuarioId, pasoActual, jsonBorrador);
                out.print("{\"success\":" + success + "}");
            } else {
                resp.setStatus(404);
                out.print("{\"error\":\"Ruta no encontrada\"}");
            }
        } catch (Exception e) {
            com.importease.proyecto.service.LoggerUtil.error("Error en doPost de ImportacionControlador", e);
            resp.setStatus(500);
            out.print("{\"error\":\"Error interno al procesar la cotizacion o actualizacion.\"}");
        }
    }
}
