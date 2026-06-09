package com.importease.proyecto.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class VUCEScraper {

    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT = 15000;

    private static final Semaphore SEMAPHORE = new Semaphore(3);
    private static final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
    private static final Map<String, List<Map<String, String>>> procedureCache = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, String>> TUPA_SEED = TupaDataSeed.TUPA_SEED;

    public Map<String, String> getDatosPorEntidad(String entidad) {
        String key = entidad.toUpperCase();
        if (cache.containsKey(key)) return cache.get(key);

        Map<String, String> datos = new LinkedHashMap<>(TUPA_SEED.getOrDefault(key, generarGenerico(key)));
        cache.put(key, datos);
        return datos;
    }

    public Map<String, String> getDatosDigesa()     { return getDatosPorEntidad("DIGESA"); }
    public Map<String, String> getDatosMTC()        { return getDatosPorEntidad("MTC"); }
    public Map<String, String> getDatosDigemid()    { return getDatosPorEntidad("DIGEMID"); }
    public Map<String, String> getDatosSenasa()     { return getDatosPorEntidad("SENASA"); }
    public Map<String, String> getDatosSucamec()    { return getDatosPorEntidad("SUCAMEC"); }
    public Map<String, String> getDatosSanipes()    { return getDatosPorEntidad("SANIPES"); }

    private Map<String, String> generarGenerico(String entidad) {
        Map<String, String> g = new LinkedHashMap<>();
        g.put("nombre", entidad);
        g.put("nombre_completo", "Entidad reguladora gubernamental");
        g.put("permiso", "Permiso de importaciÃ³n segÃºn TUPA");
        g.put("base_legal", "Verificar TUPA en VUCE");
        g.put("url_tramite", "https://www.vuce.gob.pe");
        g.put("url_entidad", "https://www.vuce.gob.pe");
        g.put("url_formulario", "https://www.vuce.gob.pe/paginas/manuales/Manual_Uso_VUCE_Mercancias_Restringidas.pdf");
        g.put("costo_tupa", "Consultar TUPA vigente");
        g.put("tiempo_dias", "Variable");
        g.put("complejidad", "MEDIA");
        return g;
    }

    // â”€â”€â”€ Crawl VUCE Lista de Procedimientos Incorporados â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public List<Map<String, String>> crawlProcedimientos() {
        if (!procedureCache.isEmpty()) {
            return procedureCache.get("__all__");
        }
        List<Map<String, String>> lista = new ArrayList<>();
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) return lista;

            sleepWithJitter();
            String url = "https://www.vuce.gob.pe/mercancias-restricciones/procedimientos.html";
            Document doc = Jsoup.connect(url).userAgent(UA).timeout(TIMEOUT).get();
            Elements filas = doc.select("table tbody tr, .resultados tr, .tabla-datos tr");
            if (filas.isEmpty()) filas = doc.select("tr");

            for (Element fila : filas) {
                Elements celdas = fila.select("td");
                if (celdas.size() >= 4) {
                    Map<String, String> p = new LinkedHashMap<>();
                    p.put("entidad", celdas.get(0).text().trim());
                    p.put("tupa", celdas.get(1).text().trim());
                    p.put("detalle", celdas.get(2).text().trim());
                    p.put("plazo", celdas.size() > 3 ? celdas.get(3).text().trim() : "");
                    p.put("costo", celdas.size() > 4 ? celdas.get(4).text().trim() : "");
                    lista.add(p);
                }
            }
            if (!lista.isEmpty()) procedureCache.put("__all__", lista);
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo obtener procedimientos VUCE: " + e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return lista;
    }

    // â”€â”€â”€ Crawl VUCE Lista de MercancÃ­as Restringidas â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public List<Map<String, String>> crawlMercanciasRestringidas(String partida) {
        List<Map<String, String>> lista = new ArrayList<>();
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) return lista;

            sleepWithJitter();
            String url = "https://www.vuce.gob.pe/mercancias-restricciones/consulta.html";
            Document doc = Jsoup.connect(url).data("partida", partida != null ? partida : "")
                    .userAgent(UA).timeout(TIMEOUT).post();
            Elements filas = doc.select("table tbody tr, .resultados tr");
            for (Element fila : filas) {
                Elements celdas = fila.select("td");
                if (celdas.size() >= 3) {
                    Map<String, String> r = new LinkedHashMap<>();
                    r.put("partida", celdas.get(0).text().trim());
                    r.put("descripcion", celdas.get(1).text().trim());
                    r.put("entidad", celdas.get(2).text().trim());
                    lista.add(r);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo consultar mercancÃ­as restringidas: " + e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return lista;
    }

    // â”€â”€â”€ Verificar Documento Resolutivo en VUCE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public Map<String, Object> verificarDocumentoResolutivo(String numero, String tipo) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("verificado", false);
        res.put("numero", numero);
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) return res;

            sleepWithJitter();
            String url = "https://www.vuce.gob.pe/mercancias-restricciones/consulta-dr.html";
            Document doc = Jsoup.connect(url)
                    .data("numero", numero != null ? numero : "")
                    .data("tipo", tipo != null ? tipo : "DR")
                    .userAgent(UA).timeout(TIMEOUT).post();
            String texto = doc.text().toUpperCase();
            res.put("verificado", texto.contains("DOCUMENTO RESOLUTIVO") || texto.contains("VÃLIDO") || texto.contains("ACTIVO"));
            res.put("detalle", doc.text().length() > 20 ? doc.text().substring(0, Math.min(doc.text().length(), 500)) : "");
        } catch (Exception e) {
            res.put("error", e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return res;
    }

    // â”€â”€â”€ Verificar SUCE en VUCE â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public Map<String, Object> verificarSUCE(String ruc, String numeroSuce) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("verificado", false);
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) return res;

            sleepWithJitter();
            String url = "https://www.vuce.gob.pe/mercancias-restricciones/consulta-suce.html";
            Document doc = Jsoup.connect(url)
                    .data("ruc", ruc != null ? ruc : "")
                    .data("suce", numeroSuce != null ? numeroSuce : "")
                    .userAgent(UA).timeout(TIMEOUT).post();
            String texto = doc.text().toUpperCase();
            res.put("verificado", texto.contains("SUCE") || texto.contains("TRÃMITE") || texto.contains("TRAMITE"));
            res.put("detalle", doc.text().length() > 20 ? doc.text().substring(0, Math.min(doc.text().length(), 500)) : "");
        } catch (Exception e) {
            res.put("error", e.getMessage());
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return res;
    }

    private void sleepWithJitter() {
        try {
            long delay = 1500 + (long)(Math.random() * 2000);
            Thread.sleep(delay);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); LoggerUtil.error("Thread interrupted in sleepWithJitter", e); }
    }
}


