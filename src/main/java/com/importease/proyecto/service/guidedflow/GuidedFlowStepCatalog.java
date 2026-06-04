package com.importease.proyecto.service.guidedflow;

public class GuidedFlowStepCatalog {

    public String obtenerNombrePaso(int paso) {
        switch (paso) {
            case 1: return "IntenciÃƒÂ³n";
            case 2: return "Datos BÃƒÂ¡sicos";
            case 3: return "ClasificaciÃƒÂ³n (HS Code)";
            case 4: return "Transporte y Manifiesto";
            case 5: return "Checklist Documental";
            case 6: return "ValidaciÃƒÂ³n de Coherencia";
            case 7: return "CÃƒÂ¡lculo DTA y PRE-DAM";
            case 8: return "RevisiÃƒÂ³n Final del Expediente";
            default: return "Paso desconocido";
        }
    }

    public String obtenerDescripcionPaso(int paso, boolean modoPrincipiante) {
        if (modoPrincipiante) {
            return obtenerDescripcionPrincipiante(paso);
        }
        return obtenerDescripcionTecnica(paso);
    }

    private String obtenerDescripcionPrincipiante(int paso) {
        switch (paso) {
            case 1: return "Ã‚Â¿QuÃƒÂ© deseas hacer con tu mercancÃƒÂa? Define el rÃƒÂ©gimen aduanero (importaciÃƒÂ³n, reimportaciÃƒÂ³n, trÃƒÂ¡nsito, etc.)";
            case 2: return "Registra los datos bÃƒÂ¡sicos de la operaciÃƒÂ³n: FOB, paÃƒÂs de origen, incoterm y descripciÃƒÂ³n del producto";
            case 3: return "Clasifica tu mercancÃƒÂa usando el HS Code (Sistema Armonizado). Ingresa entre 6 y 10 dÃƒÂgitos";
            case 4: return "Registra transporte y manifiesto: documentos de transporte, fechas de embarque y llegada";
            case 5: return "Checklist documental: verifica que tengas Factura, BL, Seguro y Certificado de Origen";
            case 6: return "ValidaciÃƒÂ³n de coherencia aduanera: el sistema verifica que todos los datos sean consistentes";
            case 7: return "CÃƒÂ¡lculo DTA y PRE-DAM referencial: genera la deuda tributaria estimada y el PRE-DAM";
            case 8: return "RevisiÃƒÂ³n final del expediente: confirma todos los datos antes de enviar";
            default: return "";
        }
    }

    private String obtenerDescripcionTecnica(int paso) {
        switch (paso) {
            case 1: return "Define el rÃƒÂ©gimen aduanero de tu operaciÃƒÂ³n";
            case 2: return "FOB, paÃƒÂs de origen, incoterm y descripciÃƒÂ³n";
            case 3: return "Clasifica tu mercancÃƒÂa (HS Code 6-10 dÃƒÂgitos)";
            case 4: return "Registra transporte, BL/AWB y fechas";
            case 5: return "Verifica documentos obligatorios";
            case 6: return "Consistencia de datos aduaneros";
            case 7: return "DTA referencial y PRE-DAM";
            case 8: return "ConfirmaciÃƒÂ³n final del expediente";
            default: return "";
        }
    }
}
