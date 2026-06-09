package com.importease.proyecto.service;

import java.util.*;

public class MatrizRestriccionesServicio {

    private final List<EntidadRestriccion> restricciones = new ArrayList<>();

    public MatrizRestriccionesServicio() {
        cargarRestricciones();
    }

    private void cargarRestricciones() {
        restricciones.add(new EntidadRestriccion("01-02", "Animales vivos y productos del reino animal",
                "SENASA", "Certificado Zoosanitario de Importacion",
                "DS 016-2009-AG", "Vigente", "https://www.senasa.gob.pe"));

        restricciones.add(new EntidadRestriccion("03", "Pescados y crustaceos",
                "SANIPES / SENASA", "Certificado Sanitario / Certificado Zoosanitario",
                "DS 011-2017-PRODUCE, DS 016-2009-AG", "Vigente", "https://www.sanipes.gob.pe"));

        restricciones.add(new EntidadRestriccion("07-08", "Legumbres, hortalizas y frutas",
                "SENASA", "Certificado Fitosanitario de Importacion",
                "DS 016-2009-AG", "Vigente", "https://www.senasa.gob.pe"));

        restricciones.add(new EntidadRestriccion("28-38", "Productos quimicos y farmaceuticos",
                "DIGEMID / MINAM", "Registro Sanitario / Certificado de Libre Venta / MSDS",
                "DS 007-98-SA, Ley 28256", "Vigente", "https://www.digemid.minsa.gob.pe"));

        restricciones.add(new EntidadRestriccion("84-85", "Maquinas, aparatos y material electrico",
                "MTC", "Homologacion / Certificado de Conformidad / Registro de Equipos",
                "DS 008-2017-MTC", "Vigente", "https://www.mtc.gob.pe"));

        restricciones.add(new EntidadRestriccion("87", "Vehiculos automoviles",
                "MTC", "Certificado de Homologacion Vehicular / Registro Unico de Vehiculos",
                "DS 058-2003-MTC", "Vigente", "https://www.mtc.gob.pe"));

        restricciones.add(new EntidadRestriccion("93", "Armas y municiones",
                "SUCAMEC", "Licencia de Importacion / Certificado de Control",
                "Ley 30299, DS 010-2015-IN", "Vigente", "https://www.sucamec.gob.pe"));

        restricciones.add(new EntidadRestriccion("95", "Juguetes y articulos de recreo",
                "DIGESA", "Registro Sanitario / Certificado de Seguridad",
                "DS 010-2015-SA, Ley 28376", "Vigente", "https://www.digesa.minsa.gob.pe"));

        restricciones.add(new EntidadRestriccion("CITES", "Especies CITES (flora y fauna protegida)",
                "SERFOR / ATFFS", "Permiso CITES de exportacion / Certificado de origen legal",
                "Ley 29763, DS 019-2015-MINAGRI", "Vigente", "https://www.serfor.gob.pe"));

        restricciones.add(new EntidadRestriccion("97", "Obras de arte, antiguedades y colecciones",
                "Ministerio de Cultura", "Certificado de Inexportabilidad / Registro de Obras",
                "Ley 28296, DS 017-2003-ED", "Vigente", "https://www.cultura.gob.pe"));
    }

    public Optional<EntidadRestriccion> verificarRestriccion(String hsCode) {
        if (hsCode == null || hsCode.isBlank()) return Optional.empty();
        String code = hsCode.trim();

        for (EntidadRestriccion r : restricciones) {
            String rango = r.getRangoHs();
            if (rango.equals("CITES")) {
                if (esCITES(code)) return Optional.of(r);
                continue;
            }
            if (coincideRango(code, rango)) return Optional.of(r);
        }
        return Optional.empty();
    }

    public List<EntidadRestriccion> getTodasRestricciones() {
        return new ArrayList<>(restricciones);
    }

    private boolean coincideRango(String hsCode, String rango) {
        if (rango.contains("-")) {
            String[] partes = rango.split("-");
            int inicio = Integer.parseInt(partes[0].trim());
            int fin = Integer.parseInt(partes[1].trim());
            int prefijo = Integer.parseInt(hsCode.substring(0, Math.min(2, hsCode.length())));
            return prefijo >= inicio && prefijo <= fin;
        }
        return hsCode.startsWith(rango);
    }

    private boolean esCITES(String hsCode) {
        String code = hsCode.trim();
        return code.startsWith("4407") || code.startsWith("4403")
                || code.startsWith("0106") || code.startsWith("0301")
                || code.startsWith("0508") || code.startsWith("0510")
                || code.startsWith("0601") || code.startsWith("0602")
                || code.startsWith("1209") || code.startsWith("1211")
                || code.startsWith("4101") || code.startsWith("4103")
                || code.startsWith("4301") || code.startsWith("9705");
    }

    public static class EntidadRestriccion {
        private final String rangoHs;
        private final String descripcion;
        private final String entidad;
        private final String documentoRequerido;
        private final String baseLegal;
        private final String vigencia;
        private final String fuenteUrl;

        public EntidadRestriccion(String rangoHs, String descripcion, String entidad,
                                  String documentoRequerido, String baseLegal,
                                  String vigencia, String fuenteUrl) {
            this.rangoHs = rangoHs;
            this.descripcion = descripcion;
            this.entidad = entidad;
            this.documentoRequerido = documentoRequerido;
            this.baseLegal = baseLegal;
            this.vigencia = vigencia;
            this.fuenteUrl = fuenteUrl;
        }

        public String getRangoHs() { return rangoHs; }
        public String getDescripcion() { return descripcion; }
        public String getEntidad() { return entidad; }
        public String getDocumentoRequerido() { return documentoRequerido; }
        public String getBaseLegal() { return baseLegal; }
        public String getVigencia() { return vigencia; }
        public String getFuenteUrl() { return fuenteUrl; }
    }
}


