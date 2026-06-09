package com.importease.proyecto.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class TupaDataSeed {
    public static final Map<String, Map<String, String>> TUPA_SEED = new LinkedHashMap<>();

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
}
