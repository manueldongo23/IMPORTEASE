package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.repository.HsCodeRepositorio;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/mentor/*")
public class MentorControlador extends HttpServlet {
    private HsCodeRepositorio hsDao = new HsCodeRepositorio();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\":\"No autenticado\"}");
            return;
        }
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if ("/sugerir".equals(path)) {
            String producto = req.getParameter("producto");
            Map<String, Object> respuesta = new HashMap<>();
            if (producto != null && producto.length() > 2) {
                HsCode hs = hsDao.buscarPorDescripcion(producto);
                if (hs != null) {
                    respuesta.put("mensaje", "El producto '" + com.importease.proyecto.service.HtmlUtil.escape(producto) + "' probablemente se clasifica en la partida " + hs.getCodigo() +
                                             " con un arancel del " + hs.getAdValorem() + "%.");
                    if (hs.isRequiereVuce()) {
                        respuesta.put("alerta", "Requiere permiso de " + hs.getEntidadVuce());
                    } else {
                        respuesta.put("alerta", "No requiere permisos especiales VUCE.");
                    }
                } else {
                    respuesta.put("mensaje", "No encontramos una coincidencia clara para '" + com.importease.proyecto.service.HtmlUtil.escape(producto) + "'.");
                    respuesta.put("alerta", "Prueba con una marca, uso, material o palabra mas especifica.");
                }
            } else {
                respuesta.put("mensaje", "Ingresa un producto para recibir sugerencias.");
            }
            out.print(gson.toJson(respuesta));
        } else if ("/ficha".equals(path)) {
            String clave = req.getParameter("clave");
            Map<String, String> ficha = com.importease.proyecto.service.MentorServicio.obtenerFichaAsesoria(clave);
            out.print(gson.toJson(ficha));
        } else {
            resp.setStatus(404);
            out.print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }
}
