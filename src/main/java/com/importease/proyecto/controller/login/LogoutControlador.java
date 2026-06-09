package com.importease.proyecto.controller.login;

import com.importease.proyecto.util.JsonResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/logout")
public class LogoutControlador extends HttpServlet {
    private final JsonResponseWriter json;

    public LogoutControlador() {
        this(new JsonResponseWriter());
    }

    public LogoutControlador(JsonResponseWriter json) {
        this.json = json;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        javax.servlet.http.Cookie cookieJSession = new javax.servlet.http.Cookie("JSESSIONID", "");
        cookieJSession.setPath(req.getContextPath() + "/");
        cookieJSession.setMaxAge(0);
        cookieJSession.setHttpOnly(true);
        resp.addCookie(cookieJSession);

        javax.servlet.http.Cookie cookieCsrf = new javax.servlet.http.Cookie("csrf_token", "");
        cookieCsrf.setPath(req.getContextPath() + "/");
        cookieCsrf.setMaxAge(0);
        resp.addCookie(cookieCsrf);

        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        json.write(resp, 200, Map.of("success", true));
    }
}
