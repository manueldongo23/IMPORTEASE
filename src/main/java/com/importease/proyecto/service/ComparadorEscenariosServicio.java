package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio premium de simulaciÃ³n de escenarios "What-If" para comercio exterior.
 * Permite a los importadores comparar side-by-side los impactos tributarios segÃºn perfiles fiscales y acuerdos comerciales.
 */
public class ComparadorEscenariosServicio {

    /**
     * Compara los resultados tributarios aplicando los tres perfiles de percepciÃ³n de SUNAT:
     * - EstÃ¡ndar (3.5% sobre la base de percepciÃ³n)
     * - Primera ImportaciÃ³n (10% sobre la base de percepciÃ³n)
     * - Buen Contribuyente (0% - Exento)
     *
     * @return Mapa asociativo con la liquidaciÃ³n detallada para cada uno de los 3 escenarios.
     */
    public Map<String, CalculadoraTributaria.ResultadoTributario> compararPerfilesFiscales(
            HsCode hs, BigDecimal fob, BigDecimal flete, BigDecimal seguro, String paisOrigen, BigDecimal tipoCambio, String incoterm) {
        
        Map<String, CalculadoraTributaria.ResultadoTributario> comparacion = new HashMap<>();

        // Escenario 1: EstÃ¡ndar
        Usuario userEstandar = new Usuario();
        userEstandar.setPerfil("ESTANDAR");
        userEstandar.setBuenContribuyente(false);
        comparacion.put("ESTANDAR", CalculadoraTributaria.calcularImpuestos(
            hs, userEstandar, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
        ));

        // Escenario 2: Primera ImportaciÃ³n
        Usuario userPrimera = new Usuario();
        userPrimera.setPerfil("PRIMERA_IMPORTACION");
        userPrimera.setBuenContribuyente(false);
        comparacion.put("PRIMERA_IMPORTACION", CalculadoraTributaria.calcularImpuestos(
            hs, userPrimera, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
        ));

        // Escenario 3: Buen Contribuyente
        Usuario userBuen = new Usuario();
        userBuen.setPerfil("ESTANDAR");
        userBuen.setBuenContribuyente(true);
        comparacion.put("BUEN_CONTRIBUYENTE", CalculadoraTributaria.calcularImpuestos(
            hs, userBuen, fob, flete, seguro, paisOrigen, tipoCambio, incoterm
        ));

        return comparacion;
    }

    /**
     * Compara el impacto de importar desde China aplicando el beneficio arancelario del TLC (Ad-Valorem 0%)
     * versus el rÃ©gimen general sin TLC (Ad-Valorem estÃ¡ndar del arancel peruano).
     *
     * @return Mapa asociativo con la liquidaciÃ³n comparativa (TLC vs No TLC).
     */
    public Map<String, CalculadoraTributaria.ResultadoTributario> compararTlcChina(
            HsCode hs, Usuario usuario, BigDecimal fob, BigDecimal flete, BigDecimal seguro, BigDecimal tipoCambio, String incoterm) {
        
        Map<String, CalculadoraTributaria.ResultadoTributario> comparacion = new HashMap<>();

        // Escenario A: Con TLC PerÃº-China (Fuerza Ad-Valorem a 0%)
        HsCode hsConTlc = new HsCode();
        hsConTlc.setAdValorem(hs.getAdValorem());
        hsConTlc.setIsc(hs.getIsc());
        hsConTlc.setTlcChina(true); // Tratado activo en subpartida
        
        comparacion.put("CON_TLC_CHINA", CalculadoraTributaria.calcularImpuestos(
            hsConTlc, usuario, fob, flete, seguro, "CN", tipoCambio, incoterm
        ));

        // Escenario B: RÃ©gimen General Sin TLC (Se mantiene la tasa arancelaria regular)
        HsCode hsSinTlc = new HsCode();
        hsSinTlc.setAdValorem(hs.getAdValorem());
        hsSinTlc.setIsc(hs.getIsc());
        hsSinTlc.setTlcChina(false); // Tratado inactivo
        
        comparacion.put("SIN_TLC_CHINA", CalculadoraTributaria.calcularImpuestos(
            hsSinTlc, usuario, fob, flete, seguro, "CN", tipoCambio, incoterm
        ));

        return comparacion;
    }
}


