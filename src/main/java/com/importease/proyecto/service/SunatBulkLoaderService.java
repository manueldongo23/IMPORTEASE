package com.importease.proyecto.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SunatBulkLoaderService {

    private static final String TABLAS_URL = "https://www.sunat.gob.pe/descarga/tablasgenerales/";
    private static final String ARANCEL_URL = "https://www.sunat.gob.pe/arancel/";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Semaphore SEMAPHORE = new Semaphore(1);

    public Map<String, Object> descargarCatalogoEntidades() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "error");
        result.put("tablas_descargadas", 0);
        result.put("entidades_insertadas", 0);

        boolean acquired = false;
        Path tempDir = null;
        try {
            acquired = SEMAPHORE.tryAcquire(30, TimeUnit.SECONDS);
            if (!acquired) {
                result.put("mensaje", "Timeout esperando turno");
                return result;
            }

            sleepWithJitter();
            Document doc = Jsoup.connect(TABLAS_URL).userAgent(UA).timeout(20000).get();
            List<String> zipLinks = new ArrayList<>();
            for (Element link : doc.select("a[href$=.zip]")) {
                String href = link.attr("href");
                if (href.contains("entidades") || href.contains("autorizante")) {
                    zipLinks.add(href.startsWith("http") ? href : TABLAS_URL + href);
                }
            }

            if (zipLinks.isEmpty()) {
                result.put("mensaje", "No se encontraron ZIP en " + TABLAS_URL);
                result.put("status", "warn");
                return result;
            }

            tempDir = Files.createTempDirectory("sunat_bulk_");
            int totalEntidades = 0;

            for (String zipUrl : zipLinks) {
                sleepWithJitter();
                Path zipPath = tempDir.resolve("tablas.zip");
                try (InputStream in = new URL(zipUrl).openStream()) {
                    Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
                }

                try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.getName().toLowerCase().contains("entidad")) {
                            String content = new String(zis.readAllBytes(), "UTF-8");
                            totalEntidades += procesarCSVEntidades(content);
                        }
                        zis.closeEntry();
                    }
                }
            }

            result.put("status", "ok");
            result.put("tablas_descargadas", zipLinks.size());
            result.put("entidades_insertadas", totalEntidades);
            result.put("mensaje", totalEntidades + " entidades autorizantes cargadas desde SUNAT");

        } catch (Exception e) {
            LoggerUtil.error("Error en descarga de catÃ¡logo SUNAT", e);
            result.put("mensaje", e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
            if (tempDir != null) {
                try { deleteDir(tempDir.toFile()); } catch (Exception e) { LoggerUtil.error("Error deleting temporary directory", e); }
            }
        }
        return result;
    }

    private int procesarCSVEntidades(String content) {
        int count = 0;
        String sql = "INSERT IGNORE INTO vuce_restricciones (entidad, tipo_control, descripcion) VALUES (?, 'SUNAT_BULK', ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isBlank()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 2) {
                    String codigo = parts[0].trim();
                    String nombre = parts.length > 1 ? parts[1].trim().replaceAll("\"", "") : "";
                    if (!codigo.isEmpty() && !nombre.isEmpty()) {
                        ps.setString(1, codigo);
                        ps.setString(2, nombre.length() > 500 ? nombre.substring(0, 500) : nombre);
                        try { ps.executeUpdate(); count++; } catch (Exception e) { LoggerUtil.error("Error inserting entity from bulk SUNAT CSV", e); }
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error insertando entidades desde bulk SUNAT", e);
        }
        return count;
    }

    public Map<String, Object> verificarActualizaciones() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "ok");
        res.put("ultima_consulta", new java.util.Date().toString());
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(10, TimeUnit.SECONDS);
            if (!acquired) {
                res.put("mensaje", "Timeout");
                return res;
            }
            sleepWithJitter();
            Document doc = Jsoup.connect(TABLAS_URL).userAgent(UA).timeout(15000).get();
            List<String> archivos = new ArrayList<>();
            for (Element link : doc.select("a[href$=.zip]")) {
                archivos.add(link.attr("href"));
            }
            res.put("archivos_disponibles", archivos.size());
            res.put("mensaje", archivos.size() + " archivos disponibles en SUNAT");
        } catch (Exception e) {
            res.put("status", "error");
            res.put("mensaje", e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return res;
    }

    private void sleepWithJitter() {
        try {
            Thread.sleep(2000 + (long)(Math.random() * 3000));
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); LoggerUtil.error("Thread interrupted in sleepWithJitter", e); }
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) {
            if (f.isDirectory()) deleteDir(f);
            else f.delete();
        }
        dir.delete();
    }
}


