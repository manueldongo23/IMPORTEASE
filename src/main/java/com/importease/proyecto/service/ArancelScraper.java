package com.importease.proyecto.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.importease.proyecto.model.HsCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import com.importease.proyecto.service.LoggerUtil;

public class ArancelScraper {

    private static final String BASE_URL   = "https://www.aduanet.gob.pe/itarancel/arancelS01Alias";
    private static final String UA         = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    // MÃ¡ximo 2 peticiones simultÃ¡neas a SUNAT para evitar bloqueos por IP
    private static final Semaphore SEMAPHORE = new Semaphore(2);

    // â”€â”€â”€ Buscar por CÃ“DIGO de 10 dÃ­gitos â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public HsCode scrapearPartida(String codigo) {
        HsCode hs = new HsCode();
        String c = codigo.replace(".", "").trim();
        hs.setCodigo(c);
        
        boolean acquired = false;
        try {
            // Intentar adquirir permiso por 5 segundos
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                LoggerUtil.warn("Timeout esperando turno para scrapear: " + c);
                return null;
            }

            sleepWithJitter();
            Document doc = Jsoup.connect(BASE_URL + "?codigo=" + c)
                    .userAgent(UA).timeout(15000).get();

            // DescripciÃ³n
            Element desc = doc.selectFirst("td.denominacion");
            if (desc == null) desc = doc.selectFirst(".tablaDatos td:last-child");
            if (desc != null && !desc.text().isBlank())
                hs.setDescripcionEs(desc.text().trim());

            // Tributos
            for (Element row : doc.select("tr")) {
                String t = row.text();
                if (t.contains("Ad / Valorem") || t.contains("Ad-valorem")) {
                    hs.setAdValorem(parse(t));
                } else if (t.contains("I.G.V.") || t.contains("IGV")) {
                    hs.setIgv(parse(t));
                } else if (t.contains("I.S.C.") || t.contains("ISC")) {
                    hs.setIsc(parse(t));
                } else if (t.contains("I.P.M.") || t.contains("IPM")) {
                    hs.setIpm(parse(t));
                }
            }
            applyDefaults(hs);

            // Restricciones VUCE + texto de restricciones/prohibiciones
            String rest = detectarVUCE(hs, c);
            if (rest != null) {
                String upper = rest.toUpperCase();
                if (upper.contains("PROHIBID")) {
                    hs.setProhibiciones(rest);
                } else {
                    hs.setRestricciones(rest);
                }
                if (upper.contains("ANTIDUMPING") || upper.contains("DERECHO")) {
                    hs.setAntidumping(true);
                }
            }

            return hs;
        } catch (Exception e) {
            LoggerUtil.error("Error scrapeando cÃ³digo " + c, e);
            return null;
        } finally {
            if (acquired) SEMAPHORE.release();
        }
    }

    // â”€â”€â”€ Buscar por DESCRIPCIÃ“N en SUNAT â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    public List<HsCode> buscarPorDescripcion(String termino) {
        List<HsCode> lista = new ArrayList<>();
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) return lista;

            // SUNAT permite bÃºsqueda por descripciÃ³n con este endpoint
            sleepWithJitter();
            String url = BASE_URL + "?accion=buscarDescripcion&descripcion=" +
                         java.net.URLEncoder.encode(termino, "UTF-8");
            Document doc = Jsoup.connect(url).userAgent(UA).timeout(12000).get();

            // Tablas de resultados de SUNAT
            Elements filas = doc.select("table.tablaDatos tr, .resultados tr");
            for (Element fila : filas) {
                Elements celdas = fila.select("td");
                if (celdas.size() >= 2) {
                    String cod  = celdas.get(0).text().replace(".", "").trim();
                    String desc = celdas.get(1).text().trim();
                    if (cod.matches("\\d{10}") && !desc.isBlank()) {
                        HsCode hs = new HsCode();
                        hs.setCodigo(cod);
                        hs.setDescripcionEs(desc);
                        hs.setIgv(new BigDecimal("18"));
                        hs.setAdValorem(new BigDecimal("6"));
                        hs.setIsc(BigDecimal.ZERO);
                        lista.add(hs);
                    }
                }
                if (lista.size() >= 10) break;
            }
        } catch (Exception e) {
            LoggerUtil.error("Error en bÃºsqueda por descripciÃ³n: " + termino, e);
        } finally {
            if (acquired) SEMAPHORE.release();
        }
        return lista;
    }

    // â”€â”€â”€ Detectar entidad VUCE + extraer texto de restricciones â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private String detectarVUCE(HsCode hs, String codigoLimpio) {
        boolean acquired = false;
        try {
            acquired = SEMAPHORE.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                detectarVUCEPorCapitulo(hs, codigoLimpio);
                return null;
            }

            sleepWithJitter();
            String url = BASE_URL + "?accion=verListarRestricciones&socodigo=" + codigoLimpio;
            Document doc = Jsoup.connect(url).userAgent(UA).timeout(10000).get();
            String html = doc.text().toUpperCase();

            if      (html.contains("DIGESA"))                     setVuce(hs, "DIGESA");
            else if (html.contains("SENASA"))                     setVuce(hs, "SENASA");
            else if (html.contains("MTC") || html.contains("TELECOMUNICACIONES")) setVuce(hs, "MTC");
            else if (html.contains("DIGEMID"))                    setVuce(hs, "DIGEMID");
            else if (html.contains("PRODUCE") || html.contains("SANIPES")) setVuce(hs, "SANIPES");
            else if (html.contains("SUCAMEC"))                    setVuce(hs, "SUCAMEC");
            else if (html.contains("MINCETUR"))                   setVuce(hs, "MINCETUR");
            else if (html.contains("OSINERGMIN"))                 setVuce(hs, "OSINERGMIN");
            else if (html.contains("MINAGRI") || html.contains("AGRICULTURA")) setVuce(hs, "MINAGRI");
            else if (html.contains("MINAM") || html.contains("AMBIENTAL")) setVuce(hs, "MINAM");
            else if (html.contains("CULTURA"))                    setVuce(hs, "CULTURA");
            else if (html.contains("PRODUCE"))                    setVuce(hs, "PRODUCE");

            return html.length() > 10 ? html : null;
        } catch (Exception e) {
            detectarVUCEPorCapitulo(hs, codigoLimpio);
            return null;
        } finally {
            if (acquired) SEMAPHORE.release();
        }
    }

    private void detectarVUCEPorCapitulo(HsCode hs, String codigo) {
        if (codigo.length() < 2) return;
        try {
            int cap = Integer.parseInt(codigo.substring(0, 2));
            if (cap == 30)                       setVuce(hs, "DIGEMID");
            else if (cap == 33)                  setVuce(hs, "DIGESA");
            else if (cap >= 1  && cap <= 5)      setVuce(hs, "SENASA");
            else if (cap >= 6  && cap <= 14)     setVuce(hs, "SENASA");
            else if (cap >= 15 && cap <= 24)     setVuce(hs, "DIGESA");
            else if (cap == 93)                  setVuce(hs, "SUCAMEC");
            else if (cap == 85 || cap == 84) {
                String s = codigo.substring(0, 4);
                if (s.equals("8517") || s.equals("8525") || s.equals("8527")) {
                    setVuce(hs, "MTC");
                    if (s.equals("8517")) hs.setDescripcionEs("Equipos de telecomunicaciÃ³n (Celulares / Tablets)");
                } else if (s.equals("8471")) {
                    hs.setDescripcionEs("MÃ¡quinas automÃ¡ticas para tratamiento o procesamiento de datos (Laptops / CPUs)");
                    hs.setRequiereVuce(false); // Generalmente libre si no tiene radiofrecuencia de largo alcance
                }
            }
        } catch (Exception e) { LoggerUtil.error("Error assigning VUCE defaults for HS code", e); }
    }

    private void setVuce(HsCode hs, String entidad) {
        hs.setRequiereVuce(true);
        hs.setEntidadVuce(entidad);
    }

    private void applyDefaults(HsCode hs) {
        if (hs.getIgv()      == null || hs.getIgv().compareTo(BigDecimal.ZERO) == 0)
            hs.setIgv(new BigDecimal("18"));
        if (hs.getAdValorem() == null) hs.setAdValorem(BigDecimal.ZERO);
        if (hs.getIsc()       == null) hs.setIsc(BigDecimal.ZERO);
    }

    private BigDecimal parse(String text) {
        Matcher m = Pattern.compile("(\\d+(\\.\\d+)?)").matcher(text);
        return m.find() ? new BigDecimal(m.group(1)) : BigDecimal.ZERO;
    }

    private void sleepWithJitter() {
        try {
            long delay = 1000 + (long)(Math.random() * 1500);
            Thread.sleep(delay);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); LoggerUtil.error("Thread interrupted in sleepWithJitter", e); }
    }
}


