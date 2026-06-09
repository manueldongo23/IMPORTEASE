package com.importease.proyecto.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class MentorServicio {

    public static Map<String, String> obtenerFichaAsesoria(String clave) {
        Map<String, String> ficha = new LinkedHashMap<>();

        if ("EXIGIBILIDAD_DTA".equalsIgnoreCase(clave) || "NACIMIENTO_DTA".equalsIgnoreCase(clave)) {
            ficha.put("titulo", "Nacimiento y exigibilidad de la deuda tributaria aduanera");
            ficha.put("articulo", "Articulos 140 y 150 de la Ley General de Aduanas (D.L. 1053)");
            ficha.put("explicacion", "La deuda tributaria aduanera nace con la numeracion de la Declaracion Aduanera de Mercancias (DAM). En despachos diferidos o anticipados sin garantia global, la obligacion tributaria vence y es exigible al dia siguiente calendario del nacimiento, de acuerdo con el articulo 150 de la LGA. A partir de esa fecha, corren intereses moratorios calculados por la SUNAT.");
            ficha.put("consejo", "Planifica el flujo de caja: numerar la DAM obliga a liquidar tributos en menos de 24 horas habiles para evitar intereses y gastos de almacenaje adicionales.");
        } else if ("BL_MASTER_HOUSE".equalsIgnoreCase(clave) || "DOCUMENTO_TRANSPORTE".equalsIgnoreCase(clave)) {
            ficha.put("titulo", "Diferencia entre documento de transporte master y house");
            ficha.put("articulo", "Articulo 60 del Reglamento de la Ley General de Aduanas");
            ficha.put("explicacion", "El Bill of Lading (BL) Master es emitido directamente por la linea naviera al agente de carga consolidada (consignatario Master). El BL House es emitido por el agente de carga al dueno real de la mercancia (importador). Ampara la porcion de carga consolidada individual para el tramite de despacho referencial.");
            ficha.put("consejo", "Asegurate de que el agente de aduanas transmita correctamente el endose en procuracion del BL House en el manifiesto para evitar rechazos en el canal de control.");
        } else if ("INCOTERMS_VALORACION".equalsIgnoreCase(clave) || "VALOR_CIF".equalsIgnoreCase(clave)) {
            ficha.put("titulo", "Incoterm FOB vs CIF y base imponible de valoracion");
            ficha.put("articulo", "Acuerdo de Valoracion de la OMC / Procedimiento DESPA-PE.01.10a");
            ficha.put("explicacion", "Las aduanas peruanas calculan los tributos de importacion sobre el valor CIF (FOB + Flete + Seguro). Si compras bajo Incoterm FOB, debes registrar el flete y el seguro formalmente para construir el valor CIF base. Si no cuentas con poliza de seguro, la SUNAT aplicara la tabla de seguro promedio segun la subpartida nacional.");
            ficha.put("consejo", "Cambiar de un Incoterm FOB a CIF obliga a registrar flete y seguro formalmente. Declarar seguros promedios infla artificialmente la base imponible e incrementa los impuestos finales.");
        } else if ("REIMPORTACION_PLAZO".equalsIgnoreCase(clave) || "REIMPORTACION".equalsIgnoreCase(clave)) {
            ficha.put("titulo", "Regimen de reimportacion en el mismo estado (Regimen 36)");
            ficha.put("articulo", "Articulo 51 de la LGA / Procedimiento DESPA-PG.26");
            ficha.put("explicacion", "Regimen que permite el ingreso al territorio aduanero de mercancias exportadas con caracter definitivo, con liberacion de derechos arancelarios y demas tributos aplicables a la importacion para el consumo, siempre que no hayan sufrido transformacion en el extranjero y se reimporten dentro del plazo de 12 meses contados desde el termino del embarque precedente.");
            ficha.put("consejo", "El canal de control asignado es obligatoriamente rojo. Debes presentar la DAM de exportacion original, regularizada, y probar documentalmente que la mercancia es identica a la que salio.");
        } else if ("ABANDONO_LEGAL".equalsIgnoreCase(clave) || "DESTINACION_DIFERIDA_15D".equalsIgnoreCase(clave)) {
            ficha.put("titulo", "Plazo de destinacion y alertas de abandono legal");
            ficha.put("articulo", "Articulos 178 y 180 de la Ley General de Aduanas");
            ficha.put("explicacion", "Para la modalidad de despacho diferido, la mercancia debe ser destinada ante la SUNAT dentro del plazo de 15 dias calendario contados a partir del dia siguiente del termino de la descarga. Vencido este plazo, la mercancia cae automaticamente en estado de abandono legal.");
            ficha.put("consejo", "El abandono legal permite a la SUNAT rematar, adjudicar o destruir la mercancia. Se puede solicitar prorroga de destinacion por 15 dias adicionales o realizar el rescate pagando las multas y tasas correspondientes.");
        } else {
            ficha.put("titulo", "Asesoria de cumplimiento tecnico aduanero");
            ficha.put("articulo", "Ley General de Aduanas del Peru (D.L. 1053)");
            ficha.put("explicacion", "ImportEase Aduanero te provee sustento legal en tiempo real referenciando los procedimientos oficiales de la SUNAT. Cada campo, tasa arancelaria, plazo de control y semaforo operativo esta enlazado a la normativa vigente.");
            ficha.put("consejo", "Haz clic en los iconos de informacion de cada panel para desplegar la ficha tecnica correspondiente.");
        }

        return ficha;
    }
}


