package com.importease.proyecto.controller;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/monitoreo/health")
public class MonitoreoControlador extends HttpServlet {
    private static final Gson gson = new Gson();
    private static final long START_TIME = System.currentTimeMillis();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("uptime", System.currentTimeMillis() - START_TIME);

        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heapUsed", runtime.totalMemory() - runtime.freeMemory());
        memoryInfo.put("heapMax", runtime.maxMemory());
        memoryInfo.put("heapCommitted", runtime.totalMemory());
        memoryInfo.put("nonHeapUsed", memory.getNonHeapMemoryUsage().getUsed());
        health.put("memory", memoryInfo);

        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("osName", os.getName());
        systemInfo.put("osVersion", os.getVersion());
        systemInfo.put("loadAverage", os.getSystemLoadAverage());
        health.put("system", systemInfo);

        resp.setStatus(200);
        resp.getWriter().print(gson.toJson(health));
    }
}
