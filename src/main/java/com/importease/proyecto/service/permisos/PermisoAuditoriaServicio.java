package com.importease.proyecto.service.permisos;

import com.importease.proyecto.service.AuditoriaServicio;

public class PermisoAuditoriaServicio {
    public void registrarRespuestasGuardadas(int usuarioId, int operacionId, int totalRespuestas, String ip, String userAgent) {
        AuditoriaServicio.registrar(
                usuarioId,
                "GUARDAR_RESPUESTAS_VUCE",
                "permisos",
                operacionId,
                "Se guardaron las respuestas regulatorias VUCE (" + totalRespuestas + " respuestas) para la operacion #" + operacionId,
                ip,
                userAgent
        );
    }
}
