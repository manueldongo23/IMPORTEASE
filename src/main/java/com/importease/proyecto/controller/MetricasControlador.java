package com.importease.proyecto.controller;

import com.google.gson.Gson;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet("/api/monitoreo/metrics")
public class MetricasControlador extends HttpServlet {
    private static final Gson gson = new Gson();
    private static final AtomicLong requestCount = new AtomicLong(0);
    private static final AtomicLong errorCount = new AtomicLong(0);
    private static final long startTime = System.currentTimeMillis();

    public static void incrementRequestCount() { requestCount.incrementAndGet(); }
    public static void incrementErrorCount() { errorCount.incrementAndGet(); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("uptime", System.currentTimeMillis() - startTime);
        metrics.put("requests", Map.of(
            "total", requestCount.get(),
            "errors", errorCount.get(),
            "errorRate", errorCount.get() > 0 ? String.format("%.2f%%", (double) errorCount.get() / Math.max(1, requestCount.get()) * 100) : "0%"
        ));

        Map<String, Object> jvmMap = new LinkedHashMap<>();
        jvmMap.put("availableProcessors", runtime.availableProcessors());
        jvmMap.put("freeMemory", runtime.freeMemory());
        jvmMap.put("totalMemory", runtime.totalMemory());
        jvmMap.put("maxMemory", runtime.maxMemory());
        jvmMap.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvmMap.put("heapUsed", memory.getHeapMemoryUsage().getUsed());
        jvmMap.put("heapMax", memory.getHeapMemoryUsage().getMax());
        jvmMap.put("nonHeapUsed", memory.getNonHeapMemoryUsage().getUsed());
        jvmMap.put("threadCount", threads.getThreadCount());
        jvmMap.put("daemonThreadCount", threads.getDaemonThreadCount());
        jvmMap.put("peakThreadCount", threads.getPeakThreadCount());
        metrics.put("jvm", jvmMap);

        metrics.put("os", Map.of(
            "name", os.getName(),
            "version", os.getVersion(),
            "arch", os.getArch(),
            "loadAverage", os.getSystemLoadAverage()
        ));

        resp.setStatus(200);
        resp.getWriter().print(gson.toJson(metrics));
    }
}
