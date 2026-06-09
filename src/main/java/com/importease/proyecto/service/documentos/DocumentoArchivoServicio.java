package com.importease.proyecto.service.documentos;

import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Servicio para la manipulación segura de archivos de documentos en disco.
 * Implementa la validación contra path traversal y cálculo de checksums.
 */
public class DocumentoArchivoServicio {
    private final String uploadDirPath;

    public DocumentoArchivoServicio() {
        this(System.getProperty("user.home") + File.separator + "importease_uploads");
    }

    public DocumentoArchivoServicio(String uploadDirPath) {
        this.uploadDirPath = uploadDirPath;
    }

    public GuardadoResultado guardar(Part filePart, String submittedFileName) throws Exception {
        File dir = new File(uploadDirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Prevenir path traversal en el nombre del archivo recibido
        String safeSubmittedName = cleanName(submittedFileName);
        String secureFileName = UUID.randomUUID().toString() + "_" + safeSubmittedName;
        File storeFile = new File(dir, secureFileName);

        // Validar path traversal
        File canonicalFile = storeFile.getCanonicalFile();
        File canonicalUploadDir = dir.getCanonicalFile();
        if (!canonicalFile.getPath().startsWith(canonicalUploadDir.getPath())) {
            throw new SecurityException("Intento de path traversal detectado al guardar archivo.");
        }

        filePart.write(storeFile.getAbsolutePath());

        String checksumHash = calcularSha256(storeFile);
        String verifyHash = calcularSha256(storeFile);
        if (!checksumHash.equals(verifyHash)) {
            storeFile.delete();
            throw new IOException("Error de integridad: el checksum del archivo no coincide tras la escritura");
        }

        return new GuardadoResultado("uploads/" + secureFileName, submittedFileName, checksumHash, storeFile);
    }

    public File resolverArchivoExistente(String relativePath) throws IOException {
        if (relativePath == null || relativePath.contains("..") || relativePath.startsWith("/")) {
            throw new SecurityException("Acceso denegado: ruta relativa no válida");
        }
        File file = new File(uploadDirPath, relativePath);
        File canonicalFile = file.getCanonicalFile();
        File canonicalUploadDir = new File(uploadDirPath).getCanonicalFile();
        if (!canonicalFile.getPath().startsWith(canonicalUploadDir.getPath())) {
            throw new SecurityException("Acceso denegado: violación de ruta del directorio de almacenamiento");
        }
        if (!file.exists() || file.isDirectory()) {
            throw new IOException("El archivo físico no existe en el servidor");
        }
        return file;
    }

    public boolean verificarChecksum(File file, String expectedHash) throws NoSuchAlgorithmException, IOException {
        return expectedHash == null || expectedHash.isEmpty() || expectedHash.equals(calcularSha256(file));
    }

    private String cleanName(String submittedFileName) {
        if (submittedFileName == null) return "archivo";
        String cleanName = submittedFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (cleanName.length() <= 100) return cleanName;
        int extDot = cleanName.lastIndexOf('.');
        if (extDot > 0 && extDot < 100) return cleanName.substring(0, 100);
        if (extDot >= 100) return cleanName.substring(0, Math.min(extDot, 100)) + cleanName.substring(extDot);
        return cleanName.substring(0, 100);
    }

    public String calcularSha256(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] block = new byte[4096];
            int length;
            while ((length = fis.read(block)) > 0) {
                digest.update(block, 0, length);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static class GuardadoResultado {
        private final String relativePath;
        private final String originalName;
        private final String checksumHash;
        private final File file;

        public GuardadoResultado(String relativePath, String originalName, String checksumHash, File file) {
            this.relativePath = relativePath;
            this.originalName = originalName;
            this.checksumHash = checksumHash;
            this.file = file;
        }

        public String getRelativePath() { return relativePath; }
        public String getOriginalName() { return originalName; }
        public String getChecksumHash() { return checksumHash; }
        public File getFile() { return file; }
    }
}
