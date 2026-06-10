package com.importease.proyecto.repository;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.jpa.HsCodeEntity;
import com.importease.proyecto.repository.jpa.HsCodeJpaRepositorio;
import com.importease.proyecto.repository.jpa.HsSinonimoJpaRepositorio;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de base de datos para la entidad de Partidas Arancelarias (HsCode).
 */
public class HsCodeRepositorio implements IHsCodeRepositorio {

    private static final java.util.Map<String, String> DICCIONARIO_SINONIMOS = new java.util.HashMap<>();
    static {
        String celularCode = "8517130000";
        DICCIONARIO_SINONIMOS.put("celular", celularCode);
        DICCIONARIO_SINONIMOS.put("celulares", celularCode);
        DICCIONARIO_SINONIMOS.put("smartphone", celularCode);
        DICCIONARIO_SINONIMOS.put("smartphones", celularCode);
        DICCIONARIO_SINONIMOS.put("iphone", celularCode);
        DICCIONARIO_SINONIMOS.put("samsung", celularCode);
        DICCIONARIO_SINONIMOS.put("motorola", celularCode);
        DICCIONARIO_SINONIMOS.put("xiaomi", celularCode);
        DICCIONARIO_SINONIMOS.put("huawei", celularCode);
        DICCIONARIO_SINONIMOS.put("telefono movil", celularCode);
        DICCIONARIO_SINONIMOS.put("telefono", celularCode);
        DICCIONARIO_SINONIMOS.put("telefonos", celularCode);
        DICCIONARIO_SINONIMOS.put("movil", celularCode);
        DICCIONARIO_SINONIMOS.put("moviles", celularCode);

        String laptopCode = "8471300000";
        DICCIONARIO_SINONIMOS.put("laptop", laptopCode);
        DICCIONARIO_SINONIMOS.put("laptops", laptopCode);
        DICCIONARIO_SINONIMOS.put("notebook", laptopCode);
        DICCIONARIO_SINONIMOS.put("notebooks", laptopCode);
        DICCIONARIO_SINONIMOS.put("computadora", laptopCode);
        DICCIONARIO_SINONIMOS.put("computadoras", laptopCode);
        DICCIONARIO_SINONIMOS.put("computador", laptopCode);
        DICCIONARIO_SINONIMOS.put("ordenador", laptopCode);
        DICCIONARIO_SINONIMOS.put("tablet", laptopCode);
        DICCIONARIO_SINONIMOS.put("tablets", laptopCode);
        DICCIONARIO_SINONIMOS.put("tableta", laptopCode);
        DICCIONARIO_SINONIMOS.put("tabletas", laptopCode);

        String perfumeCode = "3303000000";
        DICCIONARIO_SINONIMOS.put("perfume", perfumeCode);
        DICCIONARIO_SINONIMOS.put("perfumes", perfumeCode);
        DICCIONARIO_SINONIMOS.put("fragancia", perfumeCode);
        DICCIONARIO_SINONIMOS.put("fragancias", perfumeCode);
        DICCIONARIO_SINONIMOS.put("colonia", perfumeCode);
        DICCIONARIO_SINONIMOS.put("colonias", perfumeCode);

        String semillaCode = "1209919000";
        DICCIONARIO_SINONIMOS.put("semilla", semillaCode);
        DICCIONARIO_SINONIMOS.put("semillas", semillaCode);

        String maderaCode = "4407910000";
        DICCIONARIO_SINONIMOS.put("madera", maderaCode);
        DICCIONARIO_SINONIMOS.put("maderas", maderaCode);
        DICCIONARIO_SINONIMOS.put("roble", maderaCode);
        DICCIONARIO_SINONIMOS.put("pino", maderaCode);
        DICCIONARIO_SINONIMOS.put("tablero", maderaCode);
        DICCIONARIO_SINONIMOS.put("tableros", maderaCode);

        String audifonosCode = "8518300000";
        DICCIONARIO_SINONIMOS.put("audifono", audifonosCode);
        DICCIONARIO_SINONIMOS.put("audifonos", audifonosCode);
        DICCIONARIO_SINONIMOS.put("auricular", audifonosCode);
        DICCIONARIO_SINONIMOS.put("auriculares", audifonosCode);
    }

    private String buscarCodigoPorSinonimo(String termino) {
        if (termino == null || termino.isBlank()) return null;
        String t = com.importease.proyecto.service.NormalizadorUtil.normalizar(termino);
        if (t.isEmpty()) {
            t = termino.toLowerCase().trim();
        }

        // 1. Direct dictionary match
        String localMatch = DICCIONARIO_SINONIMOS.get(t);
        if (localMatch != null) return localMatch;

        // 2. Scan individual tokens and singularize
        String[] tokens = t.split("\\s+");
        for (String token : tokens) {
            String match = DICCIONARIO_SINONIMOS.get(token);
            if (match != null) return match;

            if (token.endsWith("es") && token.length() > 4) {
                match = DICCIONARIO_SINONIMOS.get(token.substring(0, token.length() - 2));
                if (match != null) return match;
            }
            if (token.endsWith("s") && token.length() > 3) {
                match = DICCIONARIO_SINONIMOS.get(token.substring(0, token.length() - 1));
                if (match != null) return match;
            }
        }

        // 3. Substring check in dictionary
        for (java.util.Map.Entry<String, String> entry : DICCIONARIO_SINONIMOS.entrySet()) {
            if (t.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 4. Fallback to DB query on hs_sinonimos table
        HsSinonimoJpaRepositorio repo = getSinonimoRepo();
        if (repo != null) {
            try {
                Optional<String> maybe = repo.buscarCodigoSugeridoActivo(t);
                if (maybe.isPresent()) {
                    return maybe.get();
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarCodigoPorSinonimo con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT codigo_hs_sugerido FROM hs_sinonimos WHERE termino_usuario = ? AND activo = TRUE LIMIT 1";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("codigo_hs_sugerido");
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar codigo por sinonimo en DB", e);
        }
        return null;
    }

    public HsCode buscarPorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) return null;
        String termino = descripcion.trim().toLowerCase();

        // 1. Priorizar sinonimo comercial si existe.
        String codigoSinonimo = buscarCodigoPorSinonimo(termino);
        if (codigoSinonimo != null) {
            HsCode hs = obtenerPorCodigo(codigoSinonimo);
            if (hs != null) return hs;
        }

        String codigoNormalizado = termino.replace(".", "");
        HsCodeJpaRepositorio repo = getHsCodeRepo();
        if (repo != null) {
            try {
                List<HsCodeEntity> rows = repo.buscarPrimeroPorDescripcion(
                    codigoNormalizado,
                    codigoNormalizado + "%",
                    termino + "*"
                );
                if (!rows.isEmpty()) {
                    HsCode hs = toModel(rows.get(0));
                    preasignarVucePorCapitulo(hs);
                    return hs;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarPorDescripcion con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id FROM hs_codes " +
                     "WHERE codigo = ? OR codigo LIKE ? OR MATCH(descripcion_es) AGAINST(? IN BOOLEAN MODE) " +
                     "ORDER BY CASE WHEN codigo = ? THEN 0 WHEN codigo LIKE ? THEN 1 ELSE 2 END, codigo LIMIT 1";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigoNormalizado);
            ps.setString(2, codigoNormalizado + "%");
            ps.setString(3, termino + "*");
            ps.setString(4, codigoNormalizado);
            ps.setString(5, codigoNormalizado + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HsCode hs = mapearHsCode(rs);
                preasignarVucePorCapitulo(hs);
                return hs;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar HS Code por descripcion", e);
        }
        return null;
    }

    public HsCode obtenerPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        String key = codigo.replace(".", "").trim();

        HsCodeJpaRepositorio repo = getHsCodeRepo();
        if (repo != null) {
            try {
                Optional<HsCodeEntity> maybe = repo.findByCodigo(key);
                if (maybe.isPresent()) {
                    HsCode hs = toModel(maybe.get());
                    preasignarVucePorCapitulo(hs);
                    return hs;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo obtenerPorCodigo con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id FROM hs_codes WHERE codigo = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                HsCode hs = mapearHsCode(rs);
                preasignarVucePorCapitulo(hs);
                return hs;
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener HS Code por codigo", e);
        }
        return null;
    }

    public List<HsCode> listarTodos() {
        HsCodeJpaRepositorio repo = getHsCodeRepo();
        if (repo != null) {
            try {
                List<HsCode> out = new ArrayList<>();
                for (HsCodeEntity entity : repo.findAllByOrderByCodigoAsc()) {
                    out.add(toModel(entity));
                }
                return out;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarTodos con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<HsCode> lista = new ArrayList<>();
        String sql = "SELECT codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id FROM hs_codes ORDER BY codigo";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearHsCode(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar todos los HS Codes", e);
        }
        return lista;
    }

    public List<HsCode> buscarSugerencias(String termino) {
        List<HsCode> lista = new ArrayList<>();
        if (termino == null || termino.isBlank()) return lista;

        String t = com.importease.proyecto.service.NormalizadorUtil.normalizar(termino);
        if (t.isEmpty()) {
            t = termino.toLowerCase().trim();
        }

        String codigoSinonimo = buscarCodigoPorSinonimo(t);
        if (codigoSinonimo != null) {
            HsCode hs = obtenerPorCodigo(codigoSinonimo);
            if (hs != null) {
                preasignarVucePorCapitulo(hs);
                addUniqueByCodigo(lista, hs);
            }
        }

        String normalizedCode = t.replaceAll("[^\\d]", "");
        boolean hasNumericPrefix = !normalizedCode.isEmpty();
        String codeLike = normalizedCode + "%";
        String matchTerm = t + "*";

        HsCodeJpaRepositorio repo = getHsCodeRepo();
        if (repo != null) {
            try {
                List<HsCodeEntity> rows = hasNumericPrefix
                    ? repo.buscarSugerenciasConPrefijo(codeLike, matchTerm, normalizedCode)
                    : repo.buscarSugerenciasPorDescripcion(matchTerm);

                for (HsCodeEntity row : rows) {
                    HsCode hs = toModel(row);
                    preasignarVucePorCapitulo(hs);
                    addUniqueByCodigo(lista, hs);
                }
                return lista;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarSugerencias con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String cols = "codigo, descripcion_es, descripcion_en, capitulo, partida, subpartida, nacional, ad_valorem, isc, igv, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, tlc_china, fecha_actualizacion, id";
        String sql = hasNumericPrefix
            ? "SELECT " + cols + " FROM hs_codes " +
               "WHERE (codigo LIKE ? OR MATCH(descripcion_es) AGAINST(? IN BOOLEAN MODE)) " +
               "ORDER BY CASE " +
               "WHEN codigo = ? THEN 0 " +
               "WHEN codigo LIKE ? THEN 1 " +
               "WHEN MATCH(descripcion_es) AGAINST(? IN BOOLEAN MODE) THEN 2 " +
               "ELSE 3 END, codigo LIMIT 20"
            : "SELECT " + cols + " FROM hs_codes " +
               "WHERE MATCH(descripcion_es) AGAINST(? IN BOOLEAN MODE) " +
               "ORDER BY CASE " +
               "WHEN MATCH(descripcion_es) AGAINST(? IN BOOLEAN MODE) THEN 0 " +
               "ELSE 1 END, codigo LIMIT 20";

        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            // matchTerm already declared above, reuse it
            if (hasNumericPrefix) {
                ps.setString(1, codeLike);
                ps.setString(2, matchTerm);
                ps.setString(3, normalizedCode);
                ps.setString(4, codeLike);
                ps.setString(5, matchTerm);
            } else {
                ps.setString(1, matchTerm);
                ps.setString(2, matchTerm);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HsCode hs = mapearHsCode(rs);
                    preasignarVucePorCapitulo(hs);
                    addUniqueByCodigo(lista, hs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar sugerencias de HS Code en la base arancelaria", e);
        }

        return lista;
    }

    private void preasignarVucePorCapitulo(HsCode hs) {
        if (hs.getEntidadVuce() != null && !hs.getEntidadVuce().isBlank()) return;

        String cod = hs.getCodigo();
        if (cod == null || cod.length() < 2) return;

        try {
            int cap = Integer.parseInt(cod.substring(0, 2));
            if (cap == 30) {
                hs.setRequiereVuce(true);
                hs.setEntidadVuce("DIGEMID");
            } else if (cap == 33) {
                hs.setRequiereVuce(true);
                hs.setEntidadVuce("DIGESA");
            } else if (cap >= 1 && cap <= 14) {
                hs.setRequiereVuce(true);
                hs.setEntidadVuce("SENASA");
            } else if (cap >= 15 && cap <= 24) {
                hs.setRequiereVuce(true);
                hs.setEntidadVuce("DIGESA");
            } else if (cap == 93) {
                hs.setRequiereVuce(true);
                hs.setEntidadVuce("SUCAMEC");
            } else if (cod.startsWith("8517") || cod.startsWith("8525") || cod.startsWith("8527")) {
                hs.setRequiereVuce(true);
                hs.setEntidadVuce("MTC");
            }
        } catch (Exception ignored) {
        }
    }

    public boolean insertar(HsCode hs) {
        HsCodeJpaRepositorio repo = getHsCodeRepo();
        if (repo != null) {
            try {
                HsCodeEntity saved = repo.save(toEntity(hs));
                if (saved.getId() != null) hs.setId(saved.getId());
                return true;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo insertar HS Code con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO hs_codes (codigo, descripcion_es, ad_valorem, igv, isc, ipm, requiere_vuce, entidad_vuce, antidumping, restricciones, prohibiciones, fecha_actualizacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hs.getCodigo());
            ps.setString(2, hs.getDescripcionEs());
            ps.setBigDecimal(3, hs.getAdValorem());
            ps.setBigDecimal(4, hs.getIgv());
            ps.setBigDecimal(5, hs.getIsc());
            ps.setBigDecimal(6, hs.getIpm());
            ps.setBoolean(7, hs.isRequiereVuce());
            ps.setString(8, hs.getEntidadVuce());
            ps.setBoolean(9, hs.isAntidumping());
            ps.setString(10, hs.getRestricciones());
            ps.setString(11, hs.getProhibiciones());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al insertar HS Code", e);
        }
        return false;
    }

    public boolean actualizar(HsCode hs) {
        HsCodeJpaRepositorio repo = getHsCodeRepo();
        if (repo != null) {
            try {
                Optional<HsCodeEntity> maybe = repo.findByCodigo(hs.getCodigo());
                if (maybe.isPresent()) {
                    HsCodeEntity entity = maybe.get();
                    mergeIntoEntity(entity, hs);
                    repo.save(entity);
                    return true;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizar HS Code con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "UPDATE hs_codes SET descripcion_es=?, ad_valorem=?, igv=?, isc=?, ipm=?, requiere_vuce=?, entidad_vuce=?, antidumping=?, restricciones=?, prohibiciones=?, fecha_actualizacion=CURRENT_TIMESTAMP WHERE codigo=?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hs.getDescripcionEs());
            ps.setBigDecimal(2, hs.getAdValorem());
            ps.setBigDecimal(3, hs.getIgv());
            ps.setBigDecimal(4, hs.getIsc());
            ps.setBigDecimal(5, hs.getIpm());
            ps.setBoolean(6, hs.isRequiereVuce());
            ps.setString(7, hs.getEntidadVuce());
            ps.setBoolean(8, hs.isAntidumping());
            ps.setString(9, hs.getRestricciones());
            ps.setString(10, hs.getProhibiciones());
            ps.setString(11, hs.getCodigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al actualizar HS Code", e);
        }
        return false;
    }

    private void addUniqueByCodigo(List<HsCode> list, HsCode candidate) {
        if (candidate == null || candidate.getCodigo() == null) return;
        for (HsCode item : list) {
            if (candidate.getCodigo().equals(item.getCodigo())) {
                return;
            }
        }
        list.add(candidate);
    }

    private HsCode mapearHsCode(ResultSet rs) throws SQLException {
        HsCode hs = new HsCode();
        hs.setId(rs.getInt("id"));
        hs.setCodigo(rs.getString("codigo"));
        hs.setDescripcionEs(rs.getString("descripcion_es"));
        hs.setDescripcionEn(rs.getString("descripcion_en"));
        hs.setCapitulo(rs.getInt("capitulo"));
        hs.setPartida(rs.getInt("partida"));
        hs.setSubpartida(rs.getInt("subpartida"));
        hs.setNacional(rs.getInt("nacional"));
        hs.setAdValorem(rs.getBigDecimal("ad_valorem"));
        hs.setIsc(rs.getBigDecimal("isc"));
        hs.setIgv(rs.getBigDecimal("igv"));
        try {
            hs.setIpm(rs.getBigDecimal("ipm"));
        } catch (SQLException e) {
            hs.setIpm(BigDecimal.ZERO);
        }
        hs.setRequiereVuce(rs.getBoolean("requiere_vuce"));
        hs.setEntidadVuce(rs.getString("entidad_vuce"));
        try {
            hs.setAntidumping(rs.getBoolean("antidumping"));
        } catch (SQLException e) {
            hs.setAntidumping(false);
        }
        try {
            hs.setRestricciones(rs.getString("restricciones"));
        } catch (SQLException e) {
            hs.setRestricciones(null);
        }
        try {
            hs.setProhibiciones(rs.getString("prohibiciones"));
        } catch (SQLException e) {
            hs.setProhibiciones(null);
        }
        try {
            hs.setTlcChina(rs.getBoolean("tlc_china"));
        } catch (SQLException e) {
            hs.setTlcChina(false);
        }
        hs.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
        return hs;
    }

    private HsCodeJpaRepositorio getHsCodeRepo() {
        return SpringContextHolder.getBeanOrNull(HsCodeJpaRepositorio.class);
    }

    private HsSinonimoJpaRepositorio getSinonimoRepo() {
        return SpringContextHolder.getBeanOrNull(HsSinonimoJpaRepositorio.class);
    }

    private HsCodeEntity toEntity(HsCode model) {
        HsCodeEntity entity = new HsCodeEntity();
        mergeIntoEntity(entity, model);
        return entity;
    }

    private void mergeIntoEntity(HsCodeEntity entity, HsCode model) {
        if (model.getId() > 0) entity.setId(model.getId());
        entity.setCodigo(model.getCodigo());
        entity.setDescripcionEs(model.getDescripcionEs());
        entity.setDescripcionEn(model.getDescripcionEn());
        entity.setCapitulo(model.getCapitulo());
        entity.setPartida(model.getPartida());
        entity.setSubpartida(model.getSubpartida());
        entity.setNacional(model.getNacional());
        entity.setAdValorem(model.getAdValorem());
        entity.setIsc(model.getIsc());
        entity.setIgv(model.getIgv());
        entity.setIpm(model.getIpm());
        entity.setRequiereVuce(model.isRequiereVuce());
        entity.setEntidadVuce(model.getEntidadVuce());
        entity.setTlcChina(model.isTlcChina());
        entity.setAntidumping(model.isAntidumping());
        entity.setRestricciones(model.getRestricciones());
        entity.setProhibiciones(model.getProhibiciones());
        if (model.getFechaActualizacion() != null) {
            entity.setFechaActualizacion(new java.sql.Timestamp(model.getFechaActualizacion().getTime()));
        }
    }

    private HsCode toModel(HsCodeEntity entity) {
        HsCode hs = new HsCode();
        hs.setId(entity.getId() == null ? 0 : entity.getId());
        hs.setCodigo(entity.getCodigo());
        hs.setDescripcionEs(entity.getDescripcionEs());
        hs.setDescripcionEn(entity.getDescripcionEn());
        hs.setCapitulo(entity.getCapitulo() == null ? 0 : entity.getCapitulo());
        hs.setPartida(entity.getPartida() == null ? 0 : entity.getPartida());
        hs.setSubpartida(entity.getSubpartida() == null ? 0 : entity.getSubpartida());
        hs.setNacional(entity.getNacional() == null ? 0 : entity.getNacional());
        hs.setAdValorem(entity.getAdValorem());
        hs.setIsc(entity.getIsc());
        hs.setIgv(entity.getIgv());
        hs.setIpm(entity.getIpm());
        hs.setRequiereVuce(Boolean.TRUE.equals(entity.getRequiereVuce()));
        hs.setEntidadVuce(entity.getEntidadVuce());
        hs.setTlcChina(Boolean.TRUE.equals(entity.getTlcChina()));
        hs.setAntidumping(Boolean.TRUE.equals(entity.getAntidumping()));
        hs.setRestricciones(entity.getRestricciones());
        hs.setProhibiciones(entity.getProhibiciones());
        hs.setFechaActualizacion(entity.getFechaActualizacion() == null ? null : new Date(entity.getFechaActualizacion().getTime()));
        return hs;
    }
}
