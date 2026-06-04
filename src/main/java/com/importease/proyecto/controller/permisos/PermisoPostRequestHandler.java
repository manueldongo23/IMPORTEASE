package com.importease.proyecto.controller.permisos;

import com.google.gson.reflect.TypeToken;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.repository.UsuarioDAO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PermisoPostRequestHandler {
    private final PermisoControllerSupport support;

    public PermisoPostRequestHandler(PermisoControllerSupport support) {
        this.support = support;
    }

    public void handle(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        String path = req.getPathInfo();
        if ("/evaluar".equals(path)) {
            new PermisoGetRequestHandler(support).handle(req, resp, usuarioId);
        } else if ("/autorrellenar".equals(path)) {
            handleAutorrellenar(req, resp, usuarioId);
        } else if ("/registrar-suce".equals(path)) {
            handleRegistrarSuce(req, resp, usuarioId);
        } else if ("/guardar-respuestas".equals(path)) {
            handleGuardarRespuestas(req, resp, usuarioId);
        } else if ("/crearSolicitud".equals(path) || "/crear-solicitud".equals(path)) {
            handleCrearSolicitud(req, resp, usuarioId);
        } else if ("/cambiar-estado".equals(path)) {
            handleCambiarEstado(req, resp, usuarioId);
        } else {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Endpoint no encontrado\"}");
        }
    }

    @SuppressWarnings("unchecked")
    private void handleGuardarRespuestas(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        Map<String, Object> data = support.gson().fromJson(support.readBody(req), new TypeToken<Map<String, Object>>() {}.getType());
        if (data == null || !data.containsKey("operacionId") || !data.containsKey("respuestas")) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Datos incompletos\"}");
            return;
        }

        int operacionId = support.parseInt(data.get("operacionId"), -1);
        if (operacionId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"operacionId invalido\"}");
            return;
        }
        if (!support.authorizationService().isOperacionOwnedByUser(operacionId, usuarioId)) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para modificar esta operacion\"}");
            return;
        }
        if (!(data.get("respuestas") instanceof Map)) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Formato de respuestas invalido\"}");
            return;
        }

        Map<String, Object> rawRespuestas = (Map<String, Object>) data.get("respuestas");
        Map<Integer, String> respuestas = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawRespuestas.entrySet()) {
            try {
                respuestas.put(Integer.parseInt(entry.getKey()), entry.getValue() != null ? String.valueOf(entry.getValue()) : "");
            } catch (NumberFormatException ignored) {
            }
        }

        boolean ok = support.commandService().guardarRespuestasCuestionario(operacionId, respuestas);
        if (ok) {
            support.auditService().registrarRespuestasGuardadas(usuarioId, operacionId, respuestas.size(), req.getRemoteAddr(), req.getHeader("User-Agent"));
            resp.getWriter().print("{\"success\":true, \"mensaje\":\"Respuestas guardadas correctamente en base de datos para auditoria.\"}");
        } else {
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"Error al guardar respuestas en la base de datos\"}");
        }
    }

    private void handleCrearSolicitud(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        Map<String, Object> data = support.gson().fromJson(support.readBody(req), new TypeToken<Map<String, Object>>() {}.getType());
        int operacionId = support.parseInt(data != null ? data.get("operacionId") : null, -1);
        if (operacionId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Campo operacionId requerido\"}");
            return;
        }
        if (!support.authorizationService().isOperacionOwnedByUser(operacionId, usuarioId)) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para crear permisos en esta operacion\"}");
            return;
        }

        SolicitudPermiso sol = new SolicitudPermiso();
        sol.setOperacionId(operacionId);
        sol.setUsuarioId(usuarioId);
        sol.setCodigoEntidad(support.asString(data != null ? data.get("codigoEntidad") : null, "SUNAT"));
        sol.setTipoPermiso(support.asString(data != null ? data.get("tipoPermiso") : null, "Validacion regulatoria referencial"));
        sol.setEstado(support.asString(data != null ? data.get("estado") : null, "PERMISO_REQUERIDO"));
        String observaciones = support.asString(data != null ? data.get("observaciones") : null, "Solicitud creada desde flujo guiado");
        sol.setObservaciones(support.asString(data != null ? data.get("obs") : null, observaciones));

        String suce = support.asString(data != null ? data.get("suce") : null, null);
        String resolucion = support.asString(data != null ? data.get("resolucion") : null, null);

        int id = support.commandService().crearSolicitudManual(sol);
        if (id > 0) {
            if (suce != null || resolucion != null) support.commandService().vincularSuce(id, suce, resolucion);
            resp.getWriter().print(support.gson().toJson(Map.of("success", true, "id", id, "estado", sol.getEstado())));
        } else {
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"No se pudo crear la solicitud de permiso\"}");
        }
    }

    private void handleCambiarEstado(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        Map<String, Object> data = support.gson().fromJson(support.readBody(req), new TypeToken<Map<String, Object>>() {}.getType());
        int solicitudId = support.parseInt(data != null ? data.get("solicitudId") : null, -1);
        String nuevoEstado = support.asString(data != null ? data.get("estado") : null, null);
        if (solicitudId <= 0 || nuevoEstado == null || nuevoEstado.isBlank()) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Campos solicitudId y estado requeridos\"}");
            return;
        }

        SolicitudPermiso sol = support.queryService().obtenerSolicitud(solicitudId);
        if (sol == null) {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Solicitud no encontrada\"}");
            return;
        }
        if (sol.getUsuarioId() != usuarioId) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para modificar esta solicitud\"}");
            return;
        }
        if (requiereAdmin(nuevoEstado)) {
            com.importease.proyecto.model.Usuario u = new UsuarioDAO().buscarPorId(usuarioId);
            if (u == null || !"admin".equalsIgnoreCase(u.getPerfil())) {
                resp.setStatus(403);
                resp.getWriter().print("{\"error\":\"No autorizado: Solo un administrador puede certificar o aprobar solicitudes de permisos.\"}");
                return;
            }
        }

        boolean ok = support.commandService().cambiarEstadoSolicitud(solicitudId, nuevoEstado);
        resp.getWriter().print(ok ? "{\"success\":true}" : "{\"success\":false,\"error\":\"No se pudo cambiar el estado\"}");
    }

    private void handleAutorrellenar(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        int solId = support.parseInt(req.getParameter("solicitudId"), -1);
        if (solId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Parametro solicitudId invalido\"}");
            return;
        }
        SolicitudPermiso sol = support.queryService().obtenerSolicitud(solId);
        if (sol == null) {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Solicitud no encontrada\"}");
            return;
        }
        if (sol.getUsuarioId() != usuarioId) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para modificar esta solicitud\"}");
            return;
        }
        resp.getWriter().print(support.gson().toJson(support.commandService().autorrellenarExpediente(solId)));
    }

    private void handleRegistrarSuce(HttpServletRequest req, HttpServletResponse resp, int usuarioId) throws IOException {
        Map<String, Object> data = support.gson().fromJson(support.readBody(req), new TypeToken<Map<String, Object>>() {}.getType());
        String suce = support.asString(data != null ? data.get("suce") : null, null);
        String resolucion = support.asString(data != null ? data.get("resolucion") : null, null);
        int solId = support.parseInt(data != null ? data.get("solicitudId") : null, -1);

        if (solId <= 0) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Campo solicitudId requerido\"}");
            return;
        }

        SolicitudPermiso sol = support.queryService().obtenerSolicitud(solId);
        if (sol == null) {
            resp.setStatus(404);
            resp.getWriter().print("{\"error\":\"Solicitud no encontrada\"}");
            return;
        }
        if (sol.getUsuarioId() != usuarioId) {
            resp.setStatus(403);
            resp.getWriter().print("{\"error\":\"No autorizado para modificar esta solicitud\"}");
            return;
        }

        resp.getWriter().print(support.gson().toJson(support.commandService().vincularSuce(solId, suce, resolucion)));
    }

    private boolean requiereAdmin(String nuevoEstado) {
        return "APROBADO".equalsIgnoreCase(nuevoEstado)
                || "LISTA_DESPACHO".equalsIgnoreCase(nuevoEstado)
                || "NACIONALIZADA".equalsIgnoreCase(nuevoEstado);
    }
}
