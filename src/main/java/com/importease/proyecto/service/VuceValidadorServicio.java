package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.repository.HsCodeRepositorio;
import com.importease.proyecto.repository.IHsCodeRepositorio;
import java.util.*;
import java.util.stream.Collectors;

public class VuceValidadorServicio {
    private final IHsCodeRepositorio hsDao;
    private final ArancelServicio arancelServicio;
    private final VUCEScraper vuceScraper;

    public VuceValidadorServicio() {
        this(new HsCodeRepositorio(), new ArancelServicio(), new VUCEScraper());
    }

    public VuceValidadorServicio(IHsCodeRepositorio hsDao, ArancelServicio arancelServicio, VUCEScraper vuceScraper) {
        this.hsDao = hsDao;
        this.arancelServicio = arancelServicio;
        this.vuceScraper = vuceScraper;
    }

    private static final String[] ENTIDADES_ORDEN = {"DIGESA", "SENASA", "MTC", "DIGEMID", "SUCAMEC", "SANIPES", "OSINERGMIN", "MINAGRI", "MINAM", "CULTURA", "PRODUCE"};

    public Map<String, Object> validar(String hsCode, String producto) {
        HsCode hs = null;

        if (hsCode != null && !hsCode.isBlank()) {
            hs = arancelServicio.consultarArancel(hsCode.replace(".", "").trim());
        }
        if (hs == null && producto != null && !producto.isBlank()) {
            List<HsCode> sugs = hsDao.buscarSugerencias(producto);
            if (!sugs.isEmpty()) hs = sugs.get(0);
        }

        return construirRespuesta(hs);
    }

    public Map<String, Object> validar(String hsCode) {
        return validar(hsCode, null);
    }

    public List<Map<String, String>> listarProcedimientos() {
        List<Map<String, String>> procs = vuceScraper.crawlProcedimientos();
        if (procs.isEmpty()) {
            return ENTIDADES_ORDEN.length > 0 ? Collections.singletonList(procedimientoDesdeTUPA("DIGESA")) : Collections.emptyList();
        }
        return procs;
    }

    public List<Map<String, String>> consultarMercanciasRestringidas(String partida) {
        return vuceScraper.crawlMercanciasRestringidas(partida);
    }

    public Map<String, Object> verificarDocumentoResolutivo(String numero, String tipo) {
        return vuceScraper.verificarDocumentoResolutivo(numero, tipo);
    }

    public Map<String, Object> verificarSUCE(String ruc, String numeroSuce) {
        return vuceScraper.verificarSUCE(ruc, numeroSuce);
    }

    private Map<String, String> procedimientoDesdeTUPA(String entidad) {
        Map<String, String> datos = vuceScraper.getDatosPorEntidad(entidad);
        Map<String, String> p = new LinkedHashMap<>();
        p.put("entidad", entidad);
        p.put("detalle", datos.getOrDefault("permiso", ""));
        p.put("plazo", datos.getOrDefault("tiempo_dias", ""));
        p.put("costo", datos.getOrDefault("costo_tupa", ""));
        return p;
    }

    private Map<String, Object> construirRespuesta(HsCode hs) {
        Map<String, Object> res = new HashMap<>();

        if (hs == null) {
            res.put("encontrado", false);
            res.put("requiere", false);
            res.put("mensaje", "Producto no encontrado. Ingresa el cÃ³digo HS de 10 dÃ­gitos o un nombre mÃ¡s especÃ­fico.");
            res.put("entidades", new ArrayList<>());
            return res;
        }

        res.put("encontrado", true);
        res.put("codigo", hs.getCodigo());
        res.put("descripcion", hs.getDescripcionEs());

        String entidadPrincipal = hs.getEntidadVuce();
        boolean requiere = hs.isRequiereVuce() && entidadPrincipal != null && !entidadPrincipal.isBlank();

        res.put("requiere", requiere);
        res.put("mensaje", requiere
            ? "MERCANCIA RESTRINGIDA: Requiere permiso de " + entidadPrincipal
            : "LIBRE IMPORTACION: No se detectaron restricciones VUCE.");

        List<Map<String, Object>> entidadesResult = new ArrayList<>();

        if (requiere) {
            Map<String, String> datosEntidad = vuceScraper.getDatosPorEntidad(entidadPrincipal);
            entidadesResult.add(buildEntidadMap(datosEntidad, "REQUERIDO", hs));

            agregarEntidadesOpcionales(hs, entidadPrincipal, entidadesResult);

            for (String key : ENTIDADES_ORDEN) {
                boolean yaEsta = entidadesResult.stream()
                    .anyMatch(e -> key.equals(e.get("nombre")));
                if (!yaEsta) {
                    Map<String, String> d = vuceScraper.getDatosPorEntidad(key);
                    entidadesResult.add(buildEntidadMap(d, "NO_APLICA", null));
                }
            }
        } else {
            for (String key : new String[]{"DIGESA", "SENASA", "MTC", "DIGEMID"}) {
                Map<String, String> d = vuceScraper.getDatosPorEntidad(key);
                entidadesResult.add(buildEntidadMap(d, "NO_APLICA", null));
            }
        }

        res.put("entidades", entidadesResult);
        return res;
    }

    private Map<String, Object> buildEntidadMap(Map<String, String> datos, String estado, HsCode hs) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("nombre",         datos.getOrDefault("nombre", ""));
        e.put("descripcion",    datos.getOrDefault("nombre_completo", ""));
        e.put("permiso",        datos.getOrDefault("permiso", ""));
        e.put("base_legal",     datos.getOrDefault("base_legal", ""));
        e.put("complejidad",    datos.getOrDefault("complejidad", "MEDIA"));
        e.put("tiempo",         datos.getOrDefault("tiempo_dias", "Variable"));
        e.put("costo",          datos.getOrDefault("costo_tupa", "Consultar TUPA"));
        e.put("url",            datos.getOrDefault("url_entidad", "https://www.vuce.gob.pe"));
        e.put("url_tramite",    datos.getOrDefault("url_tramite", "https://www.vuce.gob.pe"));
        e.put("formulario",     datos.getOrDefault("url_formulario", "https://www.vuce.gob.pe/paginas/manuales/Manual_Uso_VUCE_Mercancias_Restringidas.pdf"));
        e.put("estado",         estado);

        List<String> checklist = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String item = datos.get("checklist_" + i);
            if (item != null) checklist.add(item);
        }
        e.put("checklist", checklist);

        return e;
    }

    private void agregarEntidadesOpcionales(HsCode hs, String principal, List<Map<String, Object>> lista) {
        if (hs.getCodigo() == null || hs.getCodigo().length() < 2) return;
        try {
            int cap = Integer.parseInt(hs.getCodigo().substring(0, 2));

            if ("DIGESA".equals(principal) && cap >= 1 && cap <= 14) {
                Map<String, String> d = vuceScraper.getDatosPorEntidad("SENASA");
                lista.add(buildEntidadMap(d, "OPCIONAL", hs));
            }
            if ("DIGEMID".equals(principal) && cap == 33) {
                Map<String, String> d = vuceScraper.getDatosPorEntidad("DIGESA");
                lista.add(buildEntidadMap(d, "OPCIONAL", hs));
            }
        } catch (Exception e) { LoggerUtil.error("Error validating cross-entity VUCE requirements", e); }
    }
}
