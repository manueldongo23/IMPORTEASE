package com.importease.proyecto.service;

import com.google.gson.Gson;
import com.importease.proyecto.repository.TrackingRepositorio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrackingServicio {
    private static final Map<String, String> ENDPOINTS = Map.of(
            "DHL", "https://api-eu.dhl.com/track/shipments",
            "FEDEX", "https://apis.fedex.com/track/v1/trackingnumbers",
            "UPS", "https://onlinetools.ups.com/api/track/v1/details",
            "MAERSK", "https://api.maersk.com/track-and-trace",
            "OTRO", "MANUAL"
    );

    private final TrackingRepositorio dao = new TrackingRepositorio();
    private final FuenteEventoServicio fuenteEventoServicio = new FuenteEventoServicio();
    private final EventoUsuarioServicio eventoUsuarioServicio = new EventoUsuarioServicio();
    private final Gson gson = new Gson();

    public Map<String, Object> registrar(Integer usuarioId, String sessionId, Map<String, Object> body, String ip, String userAgent) {
        String proveedor = normalizarProveedor(str(body.get("proveedor")));
        String trackingNumber = clean(str(body.get("trackingNumber")), 120);
        String blNumber = clean(str(body.get("blNumber")), 120);
        String containerNumber = clean(str(body.get("containerNumber")), 120);

        if (isBlank(trackingNumber) && isBlank(blNumber) && isBlank(containerNumber)) {
            throw new IllegalArgumentException("Ingresa tracking number, BL o contenedor.");
        }

        int operacionId = 0;
        Object opIdObj = body.get("operacionId");
        if (opIdObj instanceof Number n) operacionId = n.intValue();
        else if (opIdObj != null) {
            try { operacionId = Integer.parseInt(String.valueOf(opIdObj)); } catch (Exception e) {}
        }
        if (operacionId > 0) {
            if (!new com.importease.proyecto.service.OperacionAutorizacionServicio().isOperacionOwnedByUser(operacionId, usuarioId)) {
                throw new SecurityException("No autorizado para esta operacion");
            }
        }

        double confidence = DataConfidenceServicio.confidenceFor("MANUAL_VERIFICADO");
        Map<String, Object> envio = new LinkedHashMap<>();
        envio.put("operacionId", body.get("operacionId"));
        envio.put("proveedor", proveedor);
        envio.put("trackingNumber", trackingNumber);
        envio.put("blNumber", blNumber);
        envio.put("containerNumber", containerNumber);
        envio.put("eta", clean(str(body.get("eta")), 20));
        envio.put("estadoActual", "REGISTRADO");
        envio.put("source", "USUARIO");
        envio.put("sourceType", "MANUAL_VERIFICADO");
        envio.put("confidence", confidence);

        long id = dao.insertarEnvio(usuarioId, envio);
        if (id <= 0) {
            id = System.currentTimeMillis() * -1;
            envio.put("persistido", false);
            envio.put("mensajePersistencia", "Migracion tracking pendiente: se muestra como registro temporal.");
        } else {
            envio.put("persistido", true);
        }
        envio.put("id", id);
        envio.put("usuarioId", usuarioId);
        envio.put("ultimaActualizacion", LocalDate.now().toString());

        Map<String, Object> evento = evento("Registrado", "Operacion registrada para seguimiento trazable.", "ImportEase", "MANUAL_VERIFICADO", confidence);
        dao.insertarEvento(id, evento);

        eventoUsuarioServicio.registrar(usuarioId, sessionId, "TRACKING_REGISTRADO", "tracking", "tracking_envio", String.valueOf(id),
                gson.toJson(envio), ip, userAgent);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("envio", envio);
        data.put("eventos", List.of(evento));
        data.put("credencialesDisponibles", credencialesDisponibles(proveedor));
        data.put("apiEndpoint", ENDPOINTS.getOrDefault(proveedor, "MANUAL"));
        data.put("sourceType", "MANUAL_VERIFICADO");
        return data;
    }

    public List<Map<String, Object>> listar(Integer usuarioId) {
        return dao.listar(usuarioId);
    }

    public Map<String, Object> detalle(long id, Integer usuarioId) {
        Map<String, Object> envio = dao.detalle(id, usuarioId);
        if (envio == null) return null;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("envio", envio);
        data.put("eventos", eventos(id, usuarioId));
        data.put("credencialesDisponibles", credencialesDisponibles(str(envio.get("proveedor"))));
        data.put("apiEndpoint", ENDPOINTS.getOrDefault(str(envio.get("proveedor")), "MANUAL"));
        return data;
    }

    public List<Map<String, Object>> eventos(long id, Integer usuarioId) {
        List<Map<String, Object>> eventos = dao.eventos(id, usuarioId);
        if (!eventos.isEmpty()) return eventos;
        List<Map<String, Object>> fallback = new ArrayList<>();
        fallback.add(evento("Sin eventos externos", "Aun no hay eventos sincronizados desde API o registro manual.", "ImportEase", "PENDIENTE_VALIDACION", DataConfidenceServicio.confidenceFor("PENDIENTE_VALIDACION")));
        return fallback;
    }

    public Map<String, Object> sincronizar(long id, Integer usuarioId, String sessionId, String ip, String userAgent) {
        Map<String, Object> envio = dao.detalle(id, usuarioId);
        if (envio == null) {
            throw new IllegalArgumentException("Tracking no encontrado o no pertenece al usuario.");
        }

        String proveedor = normalizarProveedor(str(envio.get("proveedor")));
        String endpoint = ENDPOINTS.getOrDefault(proveedor, "MANUAL");
        String referencia = primeraNoVacia(str(envio.get("trackingNumber")), str(envio.get("blNumber")), str(envio.get("containerNumber")));

        if (!credencialesDisponibles(proveedor)) {
            double confidence = DataConfidenceServicio.confidenceFor("PENDIENTE_CREDENCIALES");
            Map<String, Object> evento = evento("API pendiente de credenciales",
                    "El adaptador de " + proveedor + " esta listo, pero falta configurar token/API key para consultar la fuente oficial.",
                    proveedor + "_API", "PENDIENTE_CREDENCIALES", confidence);
            dao.insertarEvento(id, evento);
            dao.actualizarEstado(id, usuarioId, "PENDIENTE_CREDENCIALES", "PENDIENTE_CREDENCIALES", confidence);
            dao.registrarSync(id, proveedor, endpoint, null, "PENDIENTE_CREDENCIALES", "Faltan credenciales de " + proveedor);
            fuenteEventoServicio.registrarError(proveedor + "_API", "TRACKING_SYNC", referencia, endpoint, "GET", 0, "Faltan credenciales", 0);
            eventoUsuarioServicio.registrar(usuarioId, sessionId, "TRACKING_SYNC_CREDENCIALES_PENDIENTES", "tracking", "tracking_envio", String.valueOf(id),
                    gson.toJson(evento), ip, userAgent);

            Map<String, Object> data = new LinkedHashMap<>();
            envio.put("estadoActual", "PENDIENTE_CREDENCIALES");
            envio.put("sourceType", "PENDIENTE_CREDENCIALES");
            envio.put("confidence", confidence);
            data.put("envio", envio);
            data.put("eventos", dao.eventos(id, usuarioId));
            data.put("apiEndpoint", endpoint);
            data.put("credencialesDisponibles", false);
            data.put("sourceType", "PENDIENTE_CREDENCIALES");
            return data;
        }

        double confidence = DataConfidenceServicio.confidenceFor("PENDIENTE_VALIDACION");
        Map<String, Object> evento = evento("Adaptador configurado",
                "Hay credenciales configuradas para " + proveedor + ". La consulta productiva debe validarse con el contrato/API oficial antes de marcar OFICIAL_API.",
                proveedor + "_API", "PENDIENTE_VALIDACION", confidence);
        dao.insertarEvento(id, evento);
        dao.actualizarEstado(id, usuarioId, "ADAPTADOR_CONFIGURADO", "PENDIENTE_VALIDACION", confidence);
        dao.registrarSync(id, proveedor, endpoint, null, "CACHE", "Credenciales detectadas; adaptador pendiente de homologacion final.");

        Map<String, Object> data = new LinkedHashMap<>();
        envio.put("estadoActual", "ADAPTADOR_CONFIGURADO");
        envio.put("sourceType", "PENDIENTE_VALIDACION");
        envio.put("confidence", confidence);
        data.put("envio", envio);
        data.put("eventos", dao.eventos(id, usuarioId));
        data.put("apiEndpoint", endpoint);
        data.put("credencialesDisponibles", true);
        data.put("sourceType", "PENDIENTE_VALIDACION");
        return data;
    }

    private Map<String, Object> evento(String estado, String descripcion, String fuente, String sourceType, double confidence) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("fechaEvento", java.time.LocalDateTime.now().toString());
        evento.put("ubicacion", "Por confirmar");
        evento.put("estado", estado);
        evento.put("descripcion", descripcion);
        evento.put("fuente", fuente);
        evento.put("sourceType", sourceType);
        evento.put("confidence", confidence);
        return evento;
    }

    private boolean credencialesDisponibles(String proveedor) {
        return switch (normalizarProveedor(proveedor)) {
            case "DHL" -> hasEnv("DHL_API_KEY");
            case "FEDEX" -> hasEnv("FEDEX_CLIENT_ID") && hasEnv("FEDEX_CLIENT_SECRET");
            case "UPS" -> hasEnv("UPS_CLIENT_ID") && hasEnv("UPS_CLIENT_SECRET");
            case "MAERSK" -> hasEnv("MAERSK_CONSUMER_KEY");
            default -> true;
        };
    }

    private boolean hasEnv(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) value = System.getenv(key);
        return !isBlank(value);
    }

    private String normalizarProveedor(String proveedor) {
        String value = isBlank(proveedor) ? "OTRO" : proveedor.trim().toUpperCase(Locale.ROOT);
        if (value.contains("DHL")) return "DHL";
        if (value.contains("FEDEX")) return "FEDEX";
        if (value.contains("UPS")) return "UPS";
        if (value.contains("MAERSK")) return "MAERSK";
        return "OTRO";
    }

    private String clean(String value, int max) {
        if (value == null) return null;
        String cleaned = value.replaceAll("<[^>]*>", "").replaceAll("[\\r\\n\\t]+", " ").trim();
        return cleaned.length() > max ? cleaned.substring(0, max) : cleaned;
    }

    private String primeraNoVacia(String... values) {
        for (String value : values) if (!isBlank(value)) return value;
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
