package com.importease.proyecto.service;

import com.importease.proyecto.model.Usuario;

/**
 * Adaptador para SunatRucServicio que usa el nuevo ServicioApiExterna.
 */
public class SunatRucServicio {

    private ServicioApiExterna externalApi = new ServicioApiExterna();

    public Usuario consultarRuc(String ruc) {
        String[] data = externalApi.consultarRuc(ruc);
        if (data == null) return null;
        boolean esSimulado = !externalApi.isTokenConfigured();

        Usuario u = new Usuario();
        u.setRuc(ruc);
        u.setRazonSocial(data[0]);
        // LÃ³gica de perfil basada en RUC
        if (ruc.startsWith("20")) {
            u.setPerfil("IMPORTADOR_JURIDICO");
        } else {
            u.setPerfil("IMPORTADOR_NATURAL");
        }
        u.setBuenContribuyente(data.length > 3 ? Boolean.parseBoolean(data[3]) : data[2].equalsIgnoreCase("HABIDO"));
        u.setRucValidado(true);
        u.setFuenteRuc(esSimulado ? "SIMULACION_LOCAL" : "TERCERO_API");
        u.setEstadoRuc(data.length > 1 ? data[1] : "VALIDADO");
        u.setCondicionRuc(data.length > 2 ? data[2] : "NO_INFORMADO");
        u.setRucConfianza(DataConfidenceServicio.confidenceFor(esSimulado ? "SYSTEM_RULE" : "TERCERO_API"));
        return u;
    }
}

