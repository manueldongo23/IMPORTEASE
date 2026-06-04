package com.importease.proyecto.service.documentos;

import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class DocumentoFileService {
    private final String uploadDirPath;

    public DocumentoFileService() {
        this(System.getProperty("user.home") + java.io.File.separator + "importease_uploads");
    }

    public DocumentoFileService(String uploadDirPath) {
        this.uploadDirPath = uploadDirPath;
    }

    public StoredDocument store(Part filePart, String submittedFileName) throws Exception {
        File dir = new File(uploadDirPath);
        if (!dir.exists()) dir.mkdirs();

        String secureFileName = UUID.randomUUID().toString() + "_" + cleanName(submittedFileName);
        File storeFile = new File(dir, secureFileName);
        filePart.write(storeFile.getAbsolutePath());

        String checksumHash = sha256(storeFile);
        String verifyHash = sha256(storeFile);
        if (!checksumHash.equals(verifyHash)) {
            storeFile.delete();
            throw new IOException("Error de integridad: el checksum del archivo no coincide tras la escritura");
        }
        return new StoredDocument("uploads/" + secureFileName, submittedFileName, checksumHash, storeFile);
    }

    public File resolveExistingFile(String relativePath) throws IOException {
        File file = new File(uploadDirPath, relativePath);
        File canonicalFile = file.getCanonicalFile();
        File canonicalUploadDir = new File(uploadDirPath).getCanonicalFile();
        if (!canonicalFile.getPath().startsWith(canonicalUploadDir.getPath())) {
            throw new SecurityException("Acceso denegado: ruta no valida");
        }
        if (!file.exists() || file.isDirectory()) {
            throw new IOException("El archivo fisico no existe en el servidor");
        }
        return file;
    }

    public boolean checksumMatches(File file, String expectedHash) throws NoSuchAlgorithmException, IOException {
        return expectedHash == null || expectedHash.isEmpty() || expectedHash.equals(sha256(file));
    }

    private String cleanName(String submittedFileName) {
        String cleanName = submittedFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (cleanName.length() <= 100) return cleanName;
        int extDot = cleanName.lastIndexOf('.');
        if (extDot > 0 && extDot < 100) return cleanName.substring(0, 100);
        if (extDot >= 100) return cleanName.substring(0, Math.min(extDot, 100)) + cleanName.substring(extDot);
        return cleanName.substring(0, 100);
    }

    private String sha256(File file) throws IOException, NoSuchAlgorithmException {
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
        for (byte b : hashBytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static class StoredDocument {
        private final String relativePath;
        private final String originalName;
        private final String checksumHash;
        private final File file;

        public StoredDocument(String relativePath, String originalName, String checksumHash, File file) {
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
