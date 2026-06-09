package com.importease.proyecto.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class VucePamServicio {

    private static final String PAM_URL_BUSQUEDA = "https://www.vuce.gob.pe/pam/busqueda.html";
    private static final String PAM_URL_DETALLE = "https://www.vuce.gob.pe/pam/detalle.html";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final Semaphore SEMAPHORE = new Semaphore(2);

    public Map<String, Object> consultarPAM(String partida, String descripcion) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("encontrado", false);
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                res.put("mensaje", "Servicio ocupado, intente nuevamente");
                return res;
            }

            sleepWithJitter();
            Document doc = Jsoup.connect(PAM_URL_BUSQUEDA)
                    .data("partida", partida != null ? partida : "")
                    .data("descripcion", descripcion != null ? descripcion : "")
                    .userAgent(UA).timeout(15000).post();

            Elements filas = doc.select("table tbody tr, .resultados tr");
            List<Map<String, String>> resultados = new ArrayList<>();

            for (Element fila : filas) {
                Elements celdas = fila.select("td");
                if (celdas.size() >= 4) {
                    Map<String, String> r = new LinkedHashMap<>();
                    r.put("partida", celdas.get(0).text().trim());
                    r.put("descripcion", celdas.get(1).text().trim());
                    r.put("impuestos", celdas.get(2).text().trim());
                    r.put("restricciones", celdas.get(3).text().trim());
                    r.put("requisitos", celdas.size() > 4 ? celdas.get(4).text().trim() : "");
                    resultados.add(r);
                }
            }

            if (!resultados.isEmpty()) {
                res.put("encontrado", true);
                res.put("resultados", resultados);
                res.put("total", resultados.size());
            } else {
                res.put("mensaje", "No se encontraron resultados en PAM");
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error consultando PAM VUCE: " + e.getMessage());
            res.put("mensaje", "Error de conexiÃ³n: " + e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return res;
    }

    public Map<String, Object> obtenerCondicionesAcceso(String partida) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("encontrado", false);
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) return res;

            sleepWithJitter();
            Document doc = Jsoup.connect(PAM_URL_DETALLE)
                    .data("partida", partida != null ? partida : "")
                    .userAgent(UA).timeout(15000).post();

            Map<String, String> condiciones = new LinkedHashMap<>();
            Elements tablas = doc.select("table");
            for (Element tabla : tablas) {
                Elements filas = tabla.select("tr");
                for (Element fila : filas) {
                    Elements celdas = fila.select("td, th");
                    if (celdas.size() >= 2) {
                        String clave = celdas.get(0).text().trim().toLowerCase();
                        String valor = celdas.get(1).text().trim();
                        if (clave.contains("impuesto") || clave.contains("tributo")) {
                            condiciones.put("impuestos", valor);
                        } else if (clave.contains("restriccion") || clave.contains("prohibicion")) {
                            condiciones.put("restricciones", valor);
                        } else if (clave.contains("requisito") || clave.contains("documentacion")) {
                            condiciones.put("requisitos", valor);
                        }
                    }
                }
            }

            if (!condiciones.isEmpty()) {
                res.put("encontrado", true);
                res.putAll(condiciones);
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error obteniendo condiciones PAM: " + e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return res;
    }

    private void sleepWithJitter() {
        try {
            Thread.sleep(1000 + (long)(Math.random() * 2000));
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); LoggerUtil.error("Thread interrupted in sleepWithJitter", e); }
    }
}


