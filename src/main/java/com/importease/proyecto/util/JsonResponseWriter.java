package com.importease.proyecto.util;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonResponseWriter {
    private final Gson gson;

    public JsonResponseWriter() {
        this(new Gson());
    }

    public JsonResponseWriter(Gson gson) {
        this.gson = gson;
    }

    public void write(HttpServletResponse response, int statusCode, Object body) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(gson.toJson(body));
    }
}
