package com.importease.proyecto.util;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class RequestBodyParser {
    private final Gson gson;

    public RequestBodyParser() {
        this(new Gson());
    }

    public RequestBodyParser(Gson gson) {
        this.gson = gson;
    }

    public <T> T read(HttpServletRequest request, Class<T> type) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }
        return gson.fromJson(body.toString(), type);
    }
}
