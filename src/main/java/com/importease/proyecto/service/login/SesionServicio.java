package com.importease.proyecto.service.login;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.service.CsrfUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SesionServicio {
    public static void invalidarSesionesUsuario(Integer usuarioId) {
    }

    public void startAuthenticatedSession(HttpServletRequest request, Usuario usuario) {
        HttpSession currentSession = request.getSession(false);
        if (currentSession != null) {
            currentSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30 * 60);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getRazonSocial());
        session.setAttribute("usuarioRuc", usuario.getRuc());
        session.setAttribute("usuario", usuario.getEmail());
        session.setAttribute("usuarioPerfil", usuario.getPerfil());
        session.setAttribute("usuarioNivelExperiencia", usuario.getNivelExperiencia());
        session.setAttribute("usuarioPreferencias", usuario.getPreferencias());
        request.changeSessionId();
        CsrfUtil.setToken(session);
    }
}
