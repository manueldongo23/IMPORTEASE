package com.importease.proyecto.service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidad para normalizacion y limpieza de descripciones comerciales crudas.
 */
public class NormalizadorUtil {

    private static final String[] RESTRICTED_PREFIXES = {"8517", "2106", "3004", "1209", "3303", "3304", "4407", "9018"};

    public static boolean looksRestricted(String hsCode) {
        if (hsCode == null) return false;
        for (String prefix : RESTRICTED_PREFIXES) {
            if (hsCode.startsWith(prefix)) return true;
        }
        return false;
    }

    public static String normalizar(String descripcion) {
        if (descripcion == null) return "";

        String texto = repararMojibake(descripcion)
            .toLowerCase(Locale.ROOT)
            .trim();

        texto = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");

        return texto
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\b(pcs|pc|units|unit|kg|gr|g|ml|l|cm|m|in|oz|lb|pack|pk|set|ctn|box|unidades|unidad|kilos|gramos|litros)\\b", " ")
            .replaceAll("\\b\\d+[a-z]*\\b", " ")
            .replaceAll("\\b[a-z]*\\d+[a-z]*\\b", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private static String repararMojibake(String texto) {
        return texto
            .replace("Â¡", "¡")
            .replace("Â¿", "¿")
            .replace("Ã¡", "á")
            .replace("Ã©", "é")
            .replace("Ã­", "í")
            .replace("Ã³", "ó")
            .replace("Ãº", "ú")
            .replace("Ã±", "ñ")
            .replace("Ã¼", "ü")
            .replace("Ã", "Á")
            .replace("Ã‰", "É")
            .replace("Ã", "Í")
            .replace("Ã“", "Ó")
            .replace("Ãš", "Ú")
            .replace("Ã‘", "Ñ")
            .replace("Ãœ", "Ü")
            .replace("ÃƒÂ¡", "á")
            .replace("ÃƒÂ©", "é")
            .replace("ÃƒÂ­", "í")
            .replace("ÃƒÂ³", "ó")
            .replace("ÃƒÂº", "ú")
            .replace("ÃƒÂ±", "ñ")
            .replace("ÃƒÂ¼", "ü")
            .replace("Ã‚Â¡", "¡")
            .replace("Ã‚Â¿", "¿");
    }
}
