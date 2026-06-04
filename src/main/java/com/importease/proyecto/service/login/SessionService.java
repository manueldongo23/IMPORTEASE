package com.importease.proyecto.service.login;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.service.CsrfUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionService {
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
        request.changeSessionId();
        CsrfUtil.setToken(session);
    }
}
