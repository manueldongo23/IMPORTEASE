package com.importease.proyecto.service.permisos;

import com.importease.proyecto.service.AuditoriaService;

public class PermisoAuditService {
    public void registrarRespuestasGuardadas(int usuarioId, int operacionId, int totalRespuestas, String ip, String userAgent) {
        AuditoriaService.registrar(
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
