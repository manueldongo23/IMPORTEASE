package com.importease.proyecto.service;

import com.importease.proyecto.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class ContribuyenteServicio {
    private final SunatRucServicio sunatApi = new SunatRucServicio();

    /**
     * Valida un RUC utilizando múltiples estrategias en orden de prioridad:
     * 1. API externa (apis.net.pe) — máxima confianza
     * 2. Caché local en BD (tabla validaciones_ruc) — alta confianza
     * 3. Validación algorítmica local del dígito verificador — confianza media
     * 4. Si todo falla y el formato es válido, marca como pendiente
     */
    public Usuario validarRuc(String ruc) {
        // Validación básica de formato
        if (ruc == null || !ruc.matches("\\d{11}")) return null;

        // --- Estrategia 1: Caché en base de datos (tabla validaciones_ruc) ---
        try {
            Usuario cached = buscarEnCache(ruc);
            if (cached != null) {
                LoggerUtil.info("RUC " + ruc + " encontrado en caché local de validaciones.");
                new FuenteEventoServicio().registrarOk("RUC_CACHE_LOCAL", "RUC_VALIDACION", ruc, null, "CACHE", 200, cached.getRazonSocial(), 0);
                return cached;
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al buscar RUC en caché local: " + e.getMessage());
        }

        // --- Estrategia 2: API externa ---
        try {
            Usuario real = sunatApi.consultarRuc(ruc);
            if (real != null && real.getRazonSocial() != null && !real.getRazonSocial().isBlank()) {
                new FuenteEventoServicio().registrarOk("RUC_TERCERO", "RUC_VALIDACION", ruc, null, "GET", 200, real.getRazonSocial(), 0);
                // Guardar en caché de BD para futuras consultas
                guardarEnCache(ruc, real);
                return real;
            }
        } catch (Exception e) {
            LoggerUtil.warn("API externa para RUC no disponible: " + e.getMessage());
        }

        // --- Estrategia 3: Validación algorítmica local (dígito verificador peruano) ---
        Usuario local = RucValidadorLocal.validarLocalmente(ruc);
        if (local != null) {
            LoggerUtil.info("RUC " + ruc + " validado localmente por dígito verificador.");
            new FuenteEventoServicio().registrarFallback(
                    "RUC_VALIDACION_LOCAL",
                    "RUC_VALIDACION",
                    ruc,
                    "RUC validado por algoritmo local (dígito verificador). Razón social pendiente de confirmación con fuente externa."
            );
            return local;
        }

        // --- Estrategia 4: RUC con formato inválido (no pasó dígito verificador) ---
        LoggerUtil.warn("RUC " + ruc + " no pasó ninguna validación.");
        new FuenteEventoServicio().registrarFallback(
                "RUC_INVALIDO",
                "RUC_VALIDACION",
                ruc,
                "RUC no válido: no pasó validación de dígito verificador ni fuente externa."
        );
        return null;
    }

    /**
     * Busca un RUC previamente validado en la tabla validaciones_ruc.
     */
    private Usuario buscarEnCache(String ruc) {
        String sql = "SELECT ruc, razon_social, estado, condicion, fuente, confianza, resultado, fecha_validacion FROM validaciones_ruc WHERE ruc = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String resultado = rs.getString("resultado");
                    // Solo usar caché si el resultado fue exitoso
                    if (resultado != null && ("VALIDADO".equalsIgnoreCase(resultado) || "ACTIVO".equalsIgnoreCase(resultado))) {
                        // TTL check: re-validar si el caché tiene más de 24 horas
                        Timestamp fechaVal = rs.getTimestamp("fecha_validacion");
                        if (fechaVal != null) {
                            long diffMs = System.currentTimeMillis() - fechaVal.getTime();
                            if (diffMs > TimeUnit.HOURS.toMillis(24)) {
                                LoggerUtil.info("Caché de RUC " + ruc + " expirado (>24h), se re-validará.");
                                return null;
                            }
                        }
                        Usuario u = new Usuario();
                        u.setRuc(rs.getString("ruc"));
                        u.setRazonSocial(rs.getString("razon_social"));
                        u.setEstadoRuc(rs.getString("estado"));
                        u.setCondicionRuc(rs.getString("condicion"));
                        u.setFuenteRuc("CACHE_" + (rs.getString("fuente") != null ? rs.getString("fuente") : "BD_LOCAL"));
                        u.setRucValidado(true);
                        u.setRucConfianza(rs.getDouble("confianza"));
                        u.setBuenContribuyente("HABIDO".equalsIgnoreCase(rs.getString("condicion")));
                        u.setPerfil(RucValidadorLocal.determinarPerfil(ruc));
                        return u;
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warn("Error al consultar validaciones_ruc: " + e.getMessage());
        }
        return null;
    }

    /**
     * Guarda un resultado de validación exitosa en la tabla validaciones_ruc para caché.
     */
    private void guardarEnCache(String ruc, Usuario u) {
        String sql = "INSERT INTO validaciones_ruc (ruc, razon_social, estado, condicion, fuente, metodo_obtencion, status_http, confianza, resultado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE razon_social = VALUES(razon_social), estado = VALUES(estado), "
                   + "condicion = VALUES(condicion), fuente = VALUES(fuente), confianza = VALUES(confianza), "
                   + "resultado = VALUES(resultado), fecha_validacion = CURRENT_TIMESTAMP";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruc);
            ps.setString(2, u.getRazonSocial());
            ps.setString(3, u.getEstadoRuc());
            ps.setString(4, u.getCondicionRuc());
            ps.setString(5, u.getFuenteRuc());
            ps.setString(6, "API_V2");
            ps.setInt(7, 200);
            ps.setDouble(8, u.getRucConfianza());
            ps.setString(9, "VALIDADO");
            ps.executeUpdate();
        } catch (Exception e) {
            LoggerUtil.warn("No se pudo guardar RUC en caché: " + e.getMessage());
        }
    }
}
