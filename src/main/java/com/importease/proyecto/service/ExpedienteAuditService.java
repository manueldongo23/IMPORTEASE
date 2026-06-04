package com.importease.proyecto.service;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.ByteBuffer;
import java.io.File;
import java.nio.file.Files;

public class ExpedienteAuditService {

    private static final Gson GSON = new Gson();
    private static KeyPair keyPair = null;
    private static final String DEFAULT_ZERO_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    static {
        keyPair = getOrCreateKeyPair();
    }

    private static synchronized KeyPair getOrCreateKeyPair() {
        File keyFile = new File("sql/rsa_keys.dat");
        if (keyFile.exists()) {
            try {
                byte[] data = Files.readAllBytes(keyFile.toPath());
                ByteBuffer buffer = ByteBuffer.wrap(data);
                int privLength = buffer.getInt();
                byte[] privBytes = new byte[privLength];
                buffer.get(privBytes);
                byte[] pubBytes = new byte[buffer.remaining()];
                buffer.get(pubBytes);

                KeyFactory kf = KeyFactory.getInstance("RSA");
                PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privBytes);
                X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubBytes);

                PrivateKey privateKey = kf.generatePrivate(privSpec);
                PublicKey publicKey = kf.generatePublic(pubSpec);
                return new KeyPair(publicKey, privateKey);
            } catch (Exception e) {
                LoggerUtil.error("Error al cargar llaves RSA persistidas, regenerando...", e);
            }
        }
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            
            File parent = keyFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            byte[] privBytes = kp.getPrivate().getEncoded();
            byte[] pubBytes = kp.getPublic().getEncoded();
            byte[] data = new byte[4 + privBytes.length + pubBytes.length];
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.putInt(privBytes.length);
            buffer.put(privBytes);
            buffer.put(pubBytes);
            Files.write(keyFile.toPath(), data);
            return kp;
        } catch (Exception e) {
            LoggerUtil.error("Error crÃ­tico al generar llaves RSA", e);
            return null;
        }
    }

    public static synchronized void registrarEventoAuditoria(Connection con, int expedienteId, int usuarioId, String tipoEvento, String descripcion, Object before, Object after, String sourceType, String ipAddress, String userAgent) {
        String beforeJson = before != null ? GSON.toJson(before) : "";
        String afterJson = after != null ? GSON.toJson(after) : "";
        String cleanDesc = descripcion != null ? descripcion : "";
        String cleanSource = sourceType != null ? sourceType : "";
        String cleanIp = ipAddress != null ? ipAddress : "";
        String cleanAgent = userAgent != null ? userAgent : "";

        String prevHash = DEFAULT_ZERO_HASH;
        String sqlPrev = "SELECT current_hash FROM expediente_eventos_auditoria WHERE expediente_id = ? ORDER BY id DESC LIMIT 1";
        
        try (PreparedStatement psPrev = con.prepareStatement(sqlPrev)) {
            psPrev.setInt(1, expedienteId);
            try (ResultSet rs = psPrev.executeQuery()) {
                if (rs.next()) {
                    String lastHash = rs.getString("current_hash");
                    if (lastHash != null && !lastHash.trim().isEmpty()) {
                        prevHash = lastHash;
                    }
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener el hash anterior de auditorÃ­a: " + e.getMessage(), e);
        }

        String dataToHash = expedienteId + "|" + usuarioId + "|" + tipoEvento + "|" + cleanDesc + "|" + beforeJson + "|" + afterJson + "|" + cleanSource + "|" + cleanIp + "|" + cleanAgent + "|" + prevHash;
        String currentHash = "";
        String firmaDigital = "";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            currentHash = sb.toString();

            if (keyPair != null) {
                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initSign(keyPair.getPrivate());
                sig.update(currentHash.getBytes(StandardCharsets.UTF_8));
                firmaDigital = Base64.getEncoder().encodeToString(sig.sign());
            }
        } catch (Exception e) {
            LoggerUtil.error("Error al calcular hash y firma para auditorÃ­a: " + e.getMessage(), e);
        }

        String sql = "INSERT INTO expediente_eventos_auditoria (expediente_id, usuario_id, tipo_evento, descripcion, before_json, after_json, source_type, ip_address, user_agent, prev_hash, current_hash, firma_digital) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            if (usuarioId > 0) {
                ps.setInt(2, usuarioId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, tipoEvento);
            ps.setString(4, cleanDesc);
            ps.setString(5, beforeJson.isEmpty() ? null : beforeJson);
            ps.setString(6, afterJson.isEmpty() ? null : afterJson);
            ps.setString(7, cleanSource);
            ps.setString(8, cleanIp);
            ps.setString(9, cleanAgent);
            ps.setString(10, prevHash);
            ps.setString(11, currentHash);
            ps.setString(12, firmaDigital.isEmpty() ? null : firmaDigital);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Error registrando evento de auditoria: " + e.getMessage(), e);
        }
    }

    public static boolean verificarCadenaAuditoria(Connection con, int expedienteId) {
        String sql = "SELECT * FROM expediente_eventos_auditoria WHERE expediente_id = ? ORDER BY id ASC";
        String expectedPrevHash = DEFAULT_ZERO_HASH;
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int usuarioId = rs.getInt("usuario_id");
                    String tipoEvento = rs.getString("tipo_evento");
                    String desc = rs.getString("descripcion");
                    String beforeJson = rs.getString("before_json");
                    String afterJson = rs.getString("after_json");
                    String sourceType = rs.getString("source_type");
                    String ip = rs.getString("ip_address");
                    String agent = rs.getString("user_agent");
                    String prevHash = rs.getString("prev_hash");
                    String currentHash = rs.getString("current_hash");
                    String firmaDigital = rs.getString("firma_digital");

                    String cleanDesc = desc != null ? desc : "";
                    String cleanBefore = beforeJson != null ? beforeJson : "";
                    String cleanAfter = afterJson != null ? afterJson : "";
                    String cleanSource = sourceType != null ? sourceType : "";
                    String cleanIp = ip != null ? ip : "";
                    String cleanAgent = agent != null ? agent : "";

                    if (prevHash == null || !prevHash.equals(expectedPrevHash)) {
                        LoggerUtil.error("ALTERACIÃ“N DETECTADA: Ruptura de hash chain para expediente " + expedienteId + ". Esperado: " + expectedPrevHash + ", Real: " + prevHash, new SecurityException("Hash Chain Rupture"));
                        return false;
                    }

                    String dataToHash = expedienteId + "|" + usuarioId + "|" + tipoEvento + "|" + cleanDesc + "|" + cleanBefore + "|" + cleanAfter + "|" + cleanSource + "|" + cleanIp + "|" + cleanAgent + "|" + prevHash;
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] hashBytes = md.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    for (byte b : hashBytes) {
                        sb.append(String.format("%02x", b));
                    }
                    String recomputedHash = sb.toString();

                    if (currentHash == null || !currentHash.equals(recomputedHash)) {
                        LoggerUtil.error("ALTERACIÃ“N DETECTADA: Datos manipulados para el evento ID " + rs.getLong("id") + " del expediente " + expedienteId + ". Hash guardado: " + currentHash + ", Recomputado: " + recomputedHash, new SecurityException("Data Manipulation Detected"));
                        return false;
                    }

                    if (keyPair != null && firmaDigital != null) {
                        Signature sig = Signature.getInstance("SHA256withRSA");
                        sig.initVerify(keyPair.getPublic());
                        sig.update(currentHash.getBytes(StandardCharsets.UTF_8));
                        boolean isValid = sig.verify(Base64.getDecoder().decode(firmaDigital));
                        if (!isValid) {
                            LoggerUtil.error("ALTERACIÃ“N DETECTADA: Firma digital invÃ¡lida para el evento ID " + rs.getLong("id") + " del expediente " + expedienteId + ".", new java.security.SignatureException("Invalid RSA Signature"));
                            return false;
                        }
                    }

                    expectedPrevHash = currentHash;
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Error al verificar la cadena de custodia: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public static void registrarValidacion(Connection con, int expedienteId, String codigoValidacion, String resultado, String severidad, List<String> camposFaltantes, String mensaje) {
        String sql = "INSERT INTO expediente_validaciones (expediente_id, codigo_validacion, resultado, severidad, campos_faltantes, mensaje) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expedienteId);
            ps.setString(2, codigoValidacion);
            ps.setString(3, resultado);
            ps.setString(4, severidad);
            ps.setString(5, camposFaltantes != null && !camposFaltantes.isEmpty() ? String.join(",", camposFaltantes) : null);
            ps.setString(6, mensaje);
            ps.executeUpdate();
        } catch (SQLException e) {
            LoggerUtil.error("Error registrando validacion de expediente: " + e.getMessage(), e);
        }
    }
}



