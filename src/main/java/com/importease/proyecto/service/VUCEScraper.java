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

    // TUPA data seed (actualizado 2024-2026 de fuentes oficiales)
    private static final Map<String, Map<String, String>> TUPA_SEED = new LinkedHashMap<>();

    static {
        // DIGESA
        Map<String, String> d = new LinkedHashMap<>();
        d.put("nombre", "DIGESA");
        d.put("nombre_completo", "DirecciÃ³n General de Salud Ambiental e Inocuidad Alimentaria");
        d.put("permiso", "Registro Sanitario de Alimentos y Bebidas");
        d.put("base_legal", "D.S. NÂ° 007-98-SA, Ley NÂ° 26842");
        d.put("url_tramite", "https://www.digesa.minsa.gob.pe/expedientes/login.aspx");
        d.put("url_entidad", "https://www.digesa.minsa.gob.pe/");
        d.put("url_formulario", "https://www.vuce.gob.pe/paginas/manuales/Manual_Uso_VUCE_Mercancias_Restringidas.pdf");
        d.put("complejidad", "MEDIA");
        d.put("costo_tupa", "S/ 390.00");
        d.put("tiempo_dias", "7 dÃ­as hÃ¡biles");
        d.put("checklist_1", "Formulario Ãºnico SUCE (solicitud electrÃ³nica en VUCE)");
        d.put("checklist_2", "AnÃ¡lisis fÃ­sico-quÃ­mico y microbiolÃ³gico de laboratorio acreditado INACAL");
        d.put("checklist_3", "Lista de ingredientes y aditivos con cÃ³digo SIN");
        d.put("checklist_4", "Condiciones de conservaciÃ³n y almacenamiento del producto");
        d.put("checklist_5", "Vida Ãºtil (shelf life) en condiciones normales");
        d.put("checklist_6", "Certificado de Libre ComercializaciÃ³n del paÃ­s de origen (solo importados)");
        d.put("checklist_7", "DeclaraciÃ³n Jurada de etiquetado conforme al Art. 117 D.S. 007-98-SA");
        TUPA_SEED.put("DIGESA", d);

        // MTC
        d = new LinkedHashMap<>();
        d.put("nombre", "MTC");
        d.put("nombre_completo", "Ministerio de Transportes y Comunicaciones");
        d.put("permiso", "Certificado de HomologaciÃ³n de Equipos de Telecomunicaciones");
        d.put("base_legal", "Ley NÂ° 28737, D.S. NÂ° 020-2007-MTC");
        d.put("url_tramite", "https://portal.mtc.gob.pe/comunicaciones/homologacion/index.html");
        d.put("url_entidad", "https://portal.mtc.gob.pe/");
        d.put("url_formulario", "https://portal.mtc.gob.pe/comunicaciones/homologacion/homologacion_general.html");
        d.put("complejidad", "MEDIA");
        d.put("costo_tupa", "Gratuito (tramitado vÃ­a VUCE)");
        d.put("tiempo_dias", "15 dÃ­as hÃ¡biles");
        d.put("checklist_1", "DeclaraciÃ³n de conformidad del fabricante (DoC)");
        d.put("checklist_2", "Informe de pruebas de laboratorio acreditado (IEC/ETSI/FCC)");
        d.put("checklist_3", "Manual tÃ©cnico del equipo en espaÃ±ol");
        d.put("checklist_4", "Ficha tÃ©cnica con especificaciones de frecuencias");
        TUPA_SEED.put("MTC", d);

        // DIGEMID
        d = new LinkedHashMap<>();
        d.put("nombre", "DIGEMID");
        d.put("nombre_completo", "DirecciÃ³n General de Medicamentos, Insumos y Drogas");
        d.put("permiso", "Registro Sanitario / NotificaciÃ³n Sanitaria Obligatoria (NSO)");
        d.put("base_legal", "Ley NÂ° 29459, D.S. NÂ° 016-2011-SA");
        d.put("url_tramite", "https://www.digemid.minsa.gob.pe/main.asp?Seccion=470");
        d.put("url_entidad", "https://www.digemid.minsa.gob.pe/");
        d.put("url_formulario", "https://www.vuce.gob.pe/paginas/manuales/Manual_Uso_VUCE_Mercancias_Restringidas.pdf");
        d.put("complejidad", "ALTA");
        d.put("costo_tupa", "S/ 602.00 (Registro) / S/ 151.00 (NSO)");
        d.put("tiempo_dias", "90 dÃ­as calendario (Registro) / 30 dÃ­as (NSO)");
        d.put("checklist_1", "Certificado de Buenas PrÃ¡cticas de Manufactura (BPM) del fabricante");
        d.put("checklist_2", "Certificado de AnÃ¡lisis del producto por lote");
        d.put("checklist_3", "Estudios de estabilidad acelerada del producto");
        d.put("checklist_4", "Registro de marca ante INDECOPI");
        d.put("checklist_5", "Certificado de origen del paÃ­s fabricante");
        TUPA_SEED.put("DIGEMID", d);

        // SENASA
        d = new LinkedHashMap<>();
        d.put("nombre", "SENASA");
        d.put("nombre_completo", "Servicio Nacional de Sanidad Agraria");
        d.put("permiso", "Permiso Fitosanitario / Zoosanitario de ImportaciÃ³n");
        d.put("base_legal", "Ley NÂ° 27322, D.Leg NÂ° 1059");
        d.put("url_tramite", "https://servicios.senasa.gob.pe/consultaPublica/");
        d.put("url_entidad", "https://www.gob.pe/senasa");
        d.put("url_formulario", "https://www.senasa.gob.pe/senasa/descargaarchivo/2016/06/solicitud-permiso-importacion.pdf");
        d.put("complejidad", "BAJA");
        d.put("costo_tupa", "S/ 45.30 (Permiso Fitosanitario)");
        d.put("tiempo_dias", "5 dÃ­as hÃ¡biles");
        d.put("checklist_1", "Certificado Fitosanitario del paÃ­s de origen (original)");
        d.put("checklist_2", "DeclaraciÃ³n de especie y variedad del producto");
        d.put("checklist_3", "Lista de empaque (packing list) detallada");
        TUPA_SEED.put("SENASA", d);

        // SUCAMEC
        d = new LinkedHashMap<>();
        d.put("nombre", "SUCAMEC");
        d.put("nombre_completo", "Superintendencia Nacional de Control de Servicios de Seguridad, Armas, Municiones y Explosivos de Uso Civil");
        d.put("permiso", "AutorizaciÃ³n de ImportaciÃ³n de Armas, Municiones y Explosivos");
        d.put("base_legal", "D.Leg NÂ° 1127, Ley NÂ° 30299");
        d.put("url_tramite", "https://www.sucamec.gob.pe/web/index.php/tramites");
        d.put("url_entidad", "https://www.sucamec.gob.pe/");
        d.put("url_formulario", "https://www.vuce.gob.pe/paginas/manuales/Manual_Uso_VUCE_Mercancias_Restringidas.pdf");
        d.put("complejidad", "ALTA");
        d.put("costo_tupa", "S/ 340.00+");
        d.put("tiempo_dias", "30-60 dÃ­as");
        d.put("checklist_1", "AutorizaciÃ³n especial del Ministerio del Interior");
        d.put("checklist_2", "Certificado de usuario final");
        d.put("checklist_3", "DocumentaciÃ³n de empresa de seguridad habilitada por SUCAMEC");
        TUPA_SEED.put("SUCAMEC", d);

        // SANIPES
        d = new LinkedHashMap<>();
        d.put("nombre", "SANIPES");
        d.put("nombre_completo", "Organismo Nacional de Sanidad Pesquera");
        d.put("permiso", "Permiso de ImportaciÃ³n de Recursos Pesqueros");
        d.put("base_legal", "Ley NÂ° 30063, D.S. NÂ° 012-2013-PRODUCE");
        d.put("url_tramite", "https://www.sanipes.gob.pe/procedimientos/index.php");
        d.put("url_entidad", "https://www.sanipes.gob.pe/");
        d.put("url_formulario", "https://www.vuce.gob.pe/paginas/manuales/Manual_Uso_VUCE_Mercancias_Restringidas.pdf");
        d.put("complejidad", "MEDIA");
        d.put("costo_tupa", "S/ 120.00 aprox.");
        d.put("tiempo_dias", "7-15 dÃ­as hÃ¡biles");
        d.put("checklist_1", "Certificado sanitario del paÃ­s de origen (original)");
        d.put("checklist_2", "AnÃ¡lisis microbiolÃ³gico del producto (lab. acreditado)");
        d.put("checklist_3", "DeclaraciÃ³n de especie pesquera");
        TUPA_SEED.put("SANIPES", d);

        // OSINERGMIN
        d = new LinkedHashMap<>();
        d.put("nombre", "OSINERGMIN");
        d.put("nombre_completo", "Organismo Supervisor de la InversiÃ³n en EnergÃ­a y MinerÃ­a");
        d.put("permiso", "AutorizaciÃ³n de ImportaciÃ³n de Hidrocarburos y Sustancias Peligrosas");
        d.put("base_legal", "Ley NÂ° 28976, Ley del OSINERGMIN");
        d.put("url_entidad", "https://www.osinergmin.gob.pe/");
        d.put("complejidad", "ALTA");
        d.put("costo_tupa", "Consultar TUPA vigente");
        d.put("tiempo_dias", "15-30 dÃ­as hÃ¡biles");
        d.put("checklist_1", "Ficha tÃ©cnica del producto");
        d.put("checklist_2", "Hoja de seguridad MSDS");
        d.put("checklist_3", "Certificado de anÃ¡lisis del lote");
        TUPA_SEED.put("OSINERGMIN", d);

        // MINAGRI
        d = new LinkedHashMap<>();
        d.put("nombre", "MINAGRI");
        d.put("nombre_completo", "Ministerio de Agricultura y Riego");
        d.put("permiso", "AutorizaciÃ³n de ImportaciÃ³n de Productos Agropecuarios");
        d.put("url_entidad", "https://www.gob.pe/minagri");
        d.put("complejidad", "MEDIA");
        TUPA_SEED.put("MINAGRI", d);

        // MINAM
        d = new LinkedHashMap<>();
        d.put("nombre", "MINAM");
        d.put("nombre_completo", "Ministerio del Ambiente");
        d.put("permiso", "AutorizaciÃ³n de ImportaciÃ³n de Sustancias Agotadoras de la Capa de Ozono (SAO)");
        d.put("base_legal", "Ley NÂ° 28611, Ley General del Ambiente");
        d.put("url_entidad", "https://www.gob.pe/minam");
        d.put("complejidad", "ALTA");
        d.put("checklist_1", "DeclaraciÃ³n jurada de uso");
        d.put("checklist_2", "Ficha tÃ©cnica del producto");
        TUPA_SEED.put("MINAM", d);

        // CULTURA
        d = new LinkedHashMap<>();
        d.put("nombre", "CULTURA");
        d.put("nombre_completo", "Ministerio de Cultura");
        d.put("permiso", "AutorizaciÃ³n de ImportaciÃ³n de Bienes Culturales");
        d.put("url_entidad", "https://www.gob.pe/cultura");
        d.put("complejidad", "MEDIA");
        TUPA_SEED.put("CULTURA", d);

        // PRODUCE
        d = new LinkedHashMap<>();
        d.put("nombre", "PRODUCE");
        d.put("nombre_completo", "Ministerio de la ProducciÃ³n");
        d.put("permiso", "AutorizaciÃ³n de ImportaciÃ³n de Productos Pesqueros Industriales");
        d.put("url_entidad", "https://www.gob.pe/produce");
        d.put("complejidad", "MEDIA");
        TUPA_SEED.put("PRODUCE", d);
    }

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


