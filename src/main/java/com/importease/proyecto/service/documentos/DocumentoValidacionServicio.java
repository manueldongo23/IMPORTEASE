package com.importease.proyecto.service.documentos;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

public class DocumentoValidacionServicio {
    private static final long MAX_FILE_SIZE = 1024L * 1024L * 5L;
    private static final Set<String> TIPOS_DOCUMENTO_PERMITIDOS = Set.of(
        "FACTURA_COMERCIAL",
        "BILL_OF_LADING",
        "CERTIFICADO_ORIGEN"
    );

    public String normalizarTipoDocumento(String tipoDoc) {
        if (tipoDoc == null) return "";
        return tipoDoc.trim().toUpperCase(Locale.ROOT);
    }

    public boolean isTipoDocumentoPermitido(String tipoDoc) {
        return TIPOS_DOCUMENTO_PERMITIDOS.contains(tipoDoc);
    }

    public int parsePositiveInt(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public String getSubmittedFileName(Part filePart) {
        String incomingFileName = filePart == null ? null : filePart.getSubmittedFileName();
        if (incomingFileName == null || incomingFileName.trim().isEmpty()) return "";
        return Paths.get(incomingFileName).getFileName().toString();
    }

    public String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) return "";
        String ext = fileName.substring(dotIndex + 1);
        String rest = fileName.substring(0, dotIndex);
        int secondDot = rest.lastIndexOf('.');
        if (secondDot != -1) {
            String secondExt = rest.substring(secondDot + 1);
            if ("pdf".equalsIgnoreCase(secondExt) || "jpg".equalsIgnoreCase(secondExt)
                    || "jpeg".equalsIgnoreCase(secondExt) || "png".equalsIgnoreCase(secondExt)) {
                return "";
            }
        }
        return ext;
    }

    public String validarArchivo(Part filePart) throws IOException {
        if (filePart == null || filePart.getSize() == 0) return "Archivo no proporcionado o vacio";
        if (filePart.getSize() > MAX_FILE_SIZE) return "El archivo supera el limite de 5 MB";

        String submittedFileName = getSubmittedFileName(filePart);
        if (submittedFileName.isEmpty()) return "Nombre de archivo invalido";

        String ext = getFileExtension(submittedFileName).toLowerCase(Locale.ROOT);
        if (!"pdf".equals(ext) && !"jpg".equals(ext) && !"jpeg".equals(ext) && !"png".equals(ext)) {
            return "Extension no permitida. Solo PDF, JPG, PNG";
        }

        String contentType = filePart.getContentType();
        if (!contentTypeMatchesExtension(contentType, ext)) {
            return "El tipo de contenido no coincide con la extension del archivo";
        }
        if (!magicBytesMatch(filePart, ext)) {
            return "Firma del archivo invalida (los Magic Bytes no coinciden con la extension)";
        }
        if (containsMaliciousPattern(filePart)) {
            return "El archivo contiene contenido potencialmente malicioso y ha sido rechazado";
        }
        return null;
    }

    private boolean contentTypeMatchesExtension(String contentType, String ext) {
        return ("pdf".equals(ext) && "application/pdf".equals(contentType))
            || (("jpg".equals(ext) || "jpeg".equals(ext)) && "image/jpeg".equals(contentType))
            || ("png".equals(ext) && "image/png".equals(contentType));
    }

    private boolean magicBytesMatch(Part filePart, String ext) throws IOException {
        try (InputStream is = filePart.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) return false;
            if ("pdf".equals(ext)) return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46;
            if ("png".equals(ext)) return header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
            if ("jpg".equals(ext) || "jpeg".equals(ext)) return header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
            return false;
        }
    }

    private boolean containsMaliciousPattern(Part filePart) throws IOException {
        try (InputStream scanIs = filePart.getInputStream()) {
            byte[] allBytes = scanIs.readAllBytes();
            String content = new String(allBytes, StandardCharsets.UTF_8);
            String upper = content.toUpperCase(Locale.ROOT);
            return upper.contains("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*")
                || upper.contains("<SCRIPT")
                || upper.contains("JAVASCRIPT:")
                || upper.contains("ONLOAD=")
                || upper.contains("ONERROR=")
                || upper.contains("ONMOUSEOVER=")
                || upper.contains("ONCLICK=")
                || upper.contains("EVAL(")
                || upper.contains("DOCUMENT.WRITE(")
                || upper.contains("<EMBED")
                || upper.contains("<OBJECT");
        }
    }
}
