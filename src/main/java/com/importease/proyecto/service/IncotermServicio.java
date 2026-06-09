package com.importease.proyecto.service;

import com.google.gson.Gson;
import com.importease.proyecto.repository.IncotermRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IncotermServicio {
    public static final String SOURCE = "ICC_2020_REFERENCIAL";
    private final IncotermRepositorio dao = new IncotermRepositorio();
    private final EventoUsuarioServicio eventoUsuarioServicio = new EventoUsuarioServicio();
    private final Gson gson = new Gson();

    public List<Map<String, Object>> listar() {
        return dao.listar();
    }

    public Map<String, Object> comparar(String base, String contra) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("base", simular(base, BigDecimal.valueOf(5000), BigDecimal.valueOf(450), BigDecimal.valueOf(80), "COMERCIAL", false));
        data.put("contra", simular(contra, BigDecimal.valueOf(5000), BigDecimal.valueOf(450), BigDecimal.valueOf(80), "COMERCIAL", false));
        data.put("mensaje", "Comparacion referencial: valida siempre la factura, transporte y seguro reales.");
        return data;
    }

    public Map<String, Object> simular(Map<String, Object> body) {
        String incoterm = str(body.get("incoterm"), "FOB");
        BigDecimal fob = money(body.get("fob"), BigDecimal.valueOf(5000));
        BigDecimal flete = money(body.get("flete"), BigDecimal.valueOf(450));
        BigDecimal seguro = money(body.get("seguro"), BigDecimal.valueOf(80));
        String tipo = str(body.get("tipoImportacion"), "COMERCIAL");
        boolean restringido = bool(body.get("productoRestringido"));
        if ("NO_SE".equalsIgnoreCase(incoterm) || "UNKNOWN".equalsIgnoreCase(incoterm)) {
            incoterm = recomendar(tipo, restringido, fob).get("incoterm").toString();
        }
        return simular(incoterm, fob, flete, seguro, tipo, restringido);
    }

    public Map<String, Object> guardarDecision(Integer usuarioId, String sessionId, Map<String, Object> body, String ip, String userAgent) {
        Map<String, Object> simulation = simular(body);
        eventoUsuarioServicio.registrar(
                usuarioId,
                sessionId,
                "INCOTERM_DECIDIDO",
                "incoterms",
                "incoterm",
                String.valueOf(simulation.get("incoterm")),
                gson.toJson(simulation),
                ip,
                userAgent
        );
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("guardado", true);
        data.put("decision", simulation);
        return data;
    }

    private Map<String, Object> simular(String incotermInput, BigDecimal fob, BigDecimal flete, BigDecimal seguro, String tipo, boolean restringido) {
        String incoterm = normalize(incotermInput);
        Map<String, Object> info = dao.obtener(incoterm);
        boolean incluyeFlete = Boolean.TRUE.equals(info.get("incluyeFlete"));
        boolean incluyeSeguro = Boolean.TRUE.equals(info.get("incluyeSeguro"));

        BigDecimal baseCif = fob;
        if (!incluyeFlete) baseCif = baseCif.add(flete);
        if (!incluyeSeguro) baseCif = baseCif.add(seguro);
        baseCif = baseCif.setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> riesgo = riesgo(incoterm, tipo, restringido);
        Map<String, Object> recomendacion = recomendar(tipo, restringido, fob);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("incoterm", incoterm);
        data.put("nombre", info.get("nombre"));
        data.put("modalidad", info.get("modalidad"));
        data.put("descripcion", info.get("descripcion"));
        data.put("fob", fob);
        data.put("flete", flete);
        data.put("seguro", seguro);
        data.put("valorCifAduana", baseCif);
        data.put("incluyeFlete", incluyeFlete);
        data.put("incluyeSeguro", incluyeSeguro);
        data.put("quienPagaFlete", incluyeFlete ? "Proveedor/vendedor" : "Importador/comprador");
        data.put("quienPagaSeguro", incluyeSeguro ? "Proveedor/vendedor" : "Importador/comprador");
        data.put("riesgo", riesgo);
        data.put("recomendacion", recomendacion);
        data.put("checklist", checklist(incoterm, incluyeFlete, incluyeSeguro));
        data.put("lectura", lectura(incoterm, incluyeFlete, incluyeSeguro, baseCif));
        data.put("source", SOURCE);
        data.put("sourceType", "BD_LOCAL");
        data.put("confidence", DataConfidenceServicio.confidenceFor("BD_LOCAL"));
        return data;
    }

    private Map<String, Object> riesgo(String incoterm, String tipo, boolean restringido) {
        String nivel = "VERDE";
        String mensaje = "Incoterm razonable si la factura y costos coinciden.";
        if ("EXW".equals(incoterm) || "DAP".equals(incoterm) || "CFR".equals(incoterm) || "CPT".equals(incoterm)) {
            nivel = "AMARILLO";
            mensaje = "Revisa costos no incluidos para evitar sorpresas en aduanas.";
        }
        if ("DDP".equals(incoterm)) {
            nivel = "ROJO";
            mensaje = "DDP puede ser confuso en Peru: valida despacho, tributos y responsable real con agente.";
        }
        if (restringido && ("DAP".equals(incoterm) || "DDP".equals(incoterm))) {
            nivel = "ROJO";
            mensaje = "Producto restringido con entrega en destino: valida permisos antes de cerrar compra.";
        }
        if ("PERSONAL".equalsIgnoreCase(tipo) && ("EXW".equals(incoterm) || "DDP".equals(incoterm))) {
            nivel = "ROJO";
            mensaje = "Para primera compra o uso personal, este Incoterm suele traer demasiada carga operativa.";
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nivel", nivel);
        data.put("mensaje", mensaje);
        return data;
    }

    private Map<String, Object> recomendar(String tipo, boolean restringido, BigDecimal fob) {
        String incoterm = "FOB";
        String motivo = "FOB te deja controlar flete, seguro y evidencia de costos para aduanas.";
        if ("PERSONAL".equalsIgnoreCase(tipo) || fob.compareTo(BigDecimal.valueOf(2000)) <= 0) {
            incoterm = "CIF";
            motivo = "CIF es mas simple para comparar si el proveedor ya incluye flete y seguro.";
        }
        if (restringido) {
            incoterm = "FOB";
            motivo = "Con mercancia restringida conviene controlar embarque y permisos antes de que la carga avance.";
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("incoterm", incoterm);
        data.put("motivo", motivo);
        return data;
    }

    private List<String> checklist(String incoterm, boolean incluyeFlete, boolean incluyeSeguro) {
        java.util.ArrayList<String> docs = new java.util.ArrayList<>();
        docs.add("Factura comercial con Incoterm " + incoterm + " visible.");
        docs.add("Documento de transporte BL/AWB o guia courier.");
        if (!incluyeFlete) docs.add("Cotizacion o factura de flete internacional.");
        if (!incluyeSeguro) docs.add("Poliza o constancia de seguro, o sustento del seguro referencial.");
        if ("DDP".equals(incoterm)) docs.add("Validacion escrita de quien asume tributos y despacho en Peru.");
        return docs;
    }

    private String lectura(String incoterm, boolean incluyeFlete, boolean incluyeSeguro, BigDecimal cif) {
        if ("DDP".equals(incoterm)) {
            return "DDP promete mucho, pero para Peru debes validar quien declara, quien paga tributos y que documentos recibiras.";
        }
        if (incluyeFlete && incluyeSeguro) {
            return "Con " + incoterm + " el valor del proveedor ya deberia incluir flete y seguro. Para el costeo se toma como base CIF referencial: USD " + cif + ".";
        }
        if (incluyeFlete) {
            return "Con " + incoterm + " el flete viene dentro del precio, pero el seguro debe sustentarse o estimarse aparte.";
        }
        return "Con " + incoterm + " tu importacion necesita sumar producto, flete y seguro para estimar la base CIF.";
    }

    private String normalize(String value) {
        String incoterm = value == null ? "FOB" : value.trim().toUpperCase(Locale.ROOT);
        return switch (incoterm) {
            case "EXW", "FCA", "FOB", "CFR", "CIF", "CPT", "CIP", "DAP", "DDP" -> incoterm;
            default -> "FOB";
        };
    }

    private BigDecimal money(Object value, BigDecimal fallback) {
        if (value == null) return fallback;
        try {
            BigDecimal parsed = new BigDecimal(String.valueOf(value));
            return parsed.compareTo(BigDecimal.ZERO) < 0 ? fallback : parsed;
        } catch (Exception e) {
            return fallback;
        }
    }

    private boolean bool(Object value) {
        if (value instanceof Boolean b) return b;
        return value != null && "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String str(Object value, String fallback) {
        if (value == null || String.valueOf(value).isBlank()) return fallback;
        return String.valueOf(value);
    }
}
