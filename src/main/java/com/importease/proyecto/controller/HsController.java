package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.repository.HsCodeDAO;
import com.importease.proyecto.service.ArancelService;
import com.importease.proyecto.service.DataConfidenceService;
import com.importease.proyecto.service.HtmlUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/hs/*")
public class HsController extends HttpServlet {

    private final ArancelService arancelService = new ArancelService();
    private final HsCodeDAO hsDao = new HsCodeDAO();
    private final Gson gson = new Gson();

    private Integer resolveUsuarioId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }

        Object rawUsuarioId = session.getAttribute("usuarioId");
        if (rawUsuarioId instanceof Integer) {
            return (Integer) rawUsuarioId;
        }
        if (rawUsuarioId instanceof Number) {
            return ((Number) rawUsuarioId).intValue();
        }
        if (rawUsuarioId instanceof String) {
            String text = ((String) rawUsuarioId).trim();
            if (!text.isEmpty()) {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/buscar".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("usuario") == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"No autenticado\"}");
                return;
            }
            String producto = req.getParameter("producto");
            String codigo = req.getParameter("codigo");

            if (codigo != null) {
                codigo = codigo.trim().replaceAll("[^\\d]", "");
                if (codigo.length() > 12) {
                    codigo = codigo.substring(0, 12);
                }
            }
            if (producto != null) {
                producto = producto.trim();
                if (producto.length() > 100) {
                    producto = producto.substring(0, 100);
                }
            }

            HsCode hs = null;
            if (codigo != null && !codigo.isEmpty()) {
                hs = arancelService.consultarArancel(codigo);
            } else if (producto != null && !producto.isEmpty()) {
                hs = hsDao.buscarPorDescripcion(producto);
                if (hs == null) {
                    List<HsCode> sugs = hsDao.buscarSugerencias(producto);
                    if (!sugs.isEmpty()) {
                        hs = sugs.get(0);
                    }
                }
            }

            if (hs != null) {
                Integer usuarioId = resolveUsuarioId(req);
                if (usuarioId != null) {
                    try {
                        String productoAudit = producto != null ? HtmlUtil.escape(producto) : "";
                        arancelService.registrarAuditoria(usuarioId, hs, "CONSULTA");
                        com.importease.proyecto.service.AuditoriaService.registrar(
                                usuarioId,
                                "BUSQUEDA_HS",
                                "arancel",
                                null,
                                "Busqueda arancelaria exitosa para: "
                                        + (codigo != null ? codigo : "")
                                        + " "
                                        + productoAudit
                                        + " -> Codigo HS: "
                                        + hs.getCodigo(),
                                req.getRemoteAddr(),
                                req.getHeader("User-Agent")
                        );
                    } catch (RuntimeException ignored) {
                        // Auditoria no debe romper la respuesta principal del buscador.
                    }
                }

                Map<String, Object> response = new HashMap<>();
                response.put("hsCode", hs.getCodigo() != null ? hs.getCodigo() : "");
                response.put("descripcion", hs.getDescripcionEs() != null ? hs.getDescripcionEs() : "Sin descripcion");
                response.put("adValorem", hs.getAdValorem());
                response.put("isc", hs.getIsc());
                response.put("requiereVUCE", hs.isRequiereVuce());
                response.put("entidadVUCE", hs.getEntidadVuce() != null ? hs.getEntidadVuce() : "");
                response.put("source", "BD_LOCAL");
                response.put("sourceType", "BD_LOCAL");
                response.put("confidence", DataConfidenceService.confidenceFor("BD_LOCAL"));
                response.put("updatedAt", hs.getFechaActualizacion() != null ? hs.getFechaActualizacion().toString() : null);
                out.print(gson.toJson(response));
            } else {
                out.print("{\"error\":\"No encontrado\"}");
            }
            return;
        }

        if ("/listar".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("usuario") == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"No autenticado\"}");
                return;
            }
            List<HsCode> lista = hsDao.listarTodos();
            out.print(gson.toJson(lista));
            return;
        }

        if ("/sugerencias".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("usuario") == null) {
                resp.setStatus(401);
                resp.getWriter().write("{\"error\":\"No autenticado\"}");
                return;
            }
            String termino = req.getParameter("termino");
            if (termino != null) {
                termino = termino.trim();
                if (termino.length() > 100) {
                    termino = termino.substring(0, 100);
                }
            }

            List<HsCode> sugerencias = hsDao.buscarSugerencias(termino);

            Integer usuarioId = resolveUsuarioId(req);
            if (usuarioId != null && sugerencias != null && !sugerencias.isEmpty()) {
                try {
                    String terminoAudit = termino != null ? HtmlUtil.escape(termino) : "";
                    com.importease.proyecto.service.AuditoriaService.registrar(
                            usuarioId,
                            "SUGERENCIAS_HS",
                            "arancel",
                            null,
                            "Solicitud de sugerencias arancelarias para termino: '"
                                    + terminoAudit
                                    + "' ("
                                    + sugerencias.size()
                                    + " sugerencias encontradas)",
                            req.getRemoteAddr(),
                            req.getHeader("User-Agent")
                    );
                } catch (RuntimeException ignored) {
                    // Auditoria no debe romper la respuesta principal del buscador.
                }
            }

            out.print(gson.toJson(sugerencias));
            return;
        }

        resp.setStatus(404);
        out.print("{\"error\":\"Endpoint no encontrado\"}");
    }
}
