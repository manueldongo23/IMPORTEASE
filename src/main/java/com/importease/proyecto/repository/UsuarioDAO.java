package com.importease.proyecto.repository;

import com.importease.proyecto.config.SpringContextHolder;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.model.jpa.UsuarioEntity;
import com.importease.proyecto.repository.jpa.UsuarioJpaRepository;
import com.importease.proyecto.service.ConexionDB;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.login.PasswordHashService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO implements IUsuarioDAO {
    private final PasswordHashService passwordHashService = new PasswordHashService();

    public boolean crearUsuario(Usuario usuario) {
        if (guardarConJpa(usuario)) {
            return true;
        }

        String sql = "INSERT INTO usuarios (ruc, razon_social, email, password_hash, buen_contribuyente, perfil, ruc_validado, fuente_ruc, fecha_validacion_ruc, estado_ruc, condicion_ruc, ruc_confianza) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setUsuarioBase(ps, usuario);
            ps.setBoolean(7, usuario.isRucValidado());
            ps.setString(8, usuario.getFuenteRuc());
            ps.setTimestamp(9, usuario.getFechaValidacionRuc());
            ps.setString(10, usuario.getEstadoRuc());
            ps.setString(11, usuario.getCondicionRuc());
            ps.setBigDecimal(12, BigDecimal.valueOf(usuario.getRucConfianza()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.warn("Columnas RUC v3.1 no disponibles al crear usuario; usando insert legacy: " + e.getMessage());
            return crearUsuarioLegacy(usuario);
        }
    }

    private boolean crearUsuarioLegacy(Usuario usuario) {
        String sql = "INSERT INTO usuarios (ruc, razon_social, email, password_hash, buen_contribuyente, perfil) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setUsuarioBase(ps, usuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al crear usuario", e);
            return false;
        }
    }

    private boolean guardarConJpa(Usuario usuario) {
        UsuarioJpaRepository repo = getJpaRepo();
        if (repo == null) return false;
        try {
            UsuarioEntity entity = toEntity(usuario);
            entity.setPasswordHash(hashPassword(usuario.getPasswordHash()));
            UsuarioEntity saved = repo.save(entity);
            if (saved.getId() != null) usuario.setId(saved.getId());
            return true;
        } catch (Exception e) {
            LoggerUtil.warn("Fallo guardando usuario con JPA, se aplicara fallback JDBC: " + e.getMessage());
            return false;
        }
    }

    private void setUsuarioBase(PreparedStatement ps, Usuario usuario) throws SQLException {
        ps.setString(1, usuario.getRuc());
        ps.setString(2, usuario.getRazonSocial());
        ps.setString(3, usuario.getEmail());
        ps.setString(4, hashPassword(usuario.getPasswordHash()));
        ps.setBoolean(5, usuario.isBuenContribuyente());
        ps.setString(6, usuario.getPerfil());
    }

    private String hashPassword(String password) {
        return passwordHashService.hash(password);
    }

    public Usuario buscarPorRuc(String ruc) {
        UsuarioJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<UsuarioEntity> result = repo.findByRuc(ruc);
                if (result.isPresent()) return toModel(result.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarPorRuc con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM usuarios WHERE ruc = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ruc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearUsuario(rs);
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener usuario por RUC", e);
        }
        return null;
    }

    public Usuario buscarPorId(int id) {
        UsuarioJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<UsuarioEntity> result = repo.findById(id);
                if (result.isPresent()) return toModel(result.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarPorId con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearUsuario(rs);
        } catch (SQLException e) {
            LoggerUtil.error("Error al obtener usuario por ID", e);
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) {
        UsuarioJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<UsuarioEntity> result = repo.findByEmail(email);
                if (result.isPresent()) return toModel(result.get());
            } catch (Exception e) {
                LoggerUtil.warn("Fallo buscarPorEmail con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearUsuario(rs);
        } catch (SQLException e) {
            LoggerUtil.error("Error al buscar usuario por email", e);
        }
        return null;
    }

    public List<Usuario> listarUsuarios() {
        UsuarioJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                List<Usuario> usuarios = new ArrayList<>();
                for (UsuarioEntity entity : repo.findTop100ByOrderByFechaRegistroDesc()) {
                    usuarios.add(toModel(entity));
                }
                return usuarios;
            } catch (Exception e) {
                LoggerUtil.warn("Fallo listarUsuarios con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY fecha_registro DESC LIMIT 100";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearUsuario(rs));
        } catch (SQLException e) {
            LoggerUtil.error("Error al listar usuarios", e);
        }
        return lista;
    }

    public boolean actualizarPassword(String email, String newPassword) {
        UsuarioJpaRepository repo = getJpaRepo();
        if (repo != null) {
            try {
                Optional<UsuarioEntity> maybe = repo.findByEmail(email);
                if (maybe.isPresent()) {
                    UsuarioEntity entity = maybe.get();
                    entity.setPasswordHash(hashPassword(newPassword));
                    repo.save(entity);
                    return true;
                }
            } catch (Exception e) {
                LoggerUtil.warn("Fallo actualizarPassword con JPA, fallback JDBC: " + e.getMessage());
            }
        }

        String sql = "UPDATE usuarios SET password_hash = ? WHERE email = ?";
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashPassword(newPassword));
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.error("Error al actualizar password", e);
            return false;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setRuc(rs.getString("ruc"));
        u.setRazonSocial(rs.getString("razon_social"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setBuenContribuyente(rs.getBoolean("buen_contribuyente"));
        u.setPerfil(rs.getString("perfil"));
        u.setFechaRegistro(rs.getTimestamp("fecha_registro"));
        u.setUltimoAcceso(rs.getTimestamp("ultimo_acceso"));
        if (hasColumn(rs, "ruc_validado")) u.setRucValidado(rs.getBoolean("ruc_validado"));
        if (hasColumn(rs, "fuente_ruc")) u.setFuenteRuc(rs.getString("fuente_ruc"));
        if (hasColumn(rs, "fecha_validacion_ruc")) u.setFechaValidacionRuc(rs.getTimestamp("fecha_validacion_ruc"));
        if (hasColumn(rs, "estado_ruc")) u.setEstadoRuc(rs.getString("estado_ruc"));
        if (hasColumn(rs, "condicion_ruc")) u.setCondicionRuc(rs.getString("condicion_ruc"));
        if (hasColumn(rs, "ruc_confianza")) u.setRucConfianza(rs.getDouble("ruc_confianza"));
        return u;
    }

    private boolean hasColumn(ResultSet rs, String name) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (name.equalsIgnoreCase(meta.getColumnLabel(i))) return true;
            }
        } catch (SQLException ignored) {
        }
        return false;
    }

    private UsuarioJpaRepository getJpaRepo() {
        return SpringContextHolder.getBeanOrNull(UsuarioJpaRepository.class);
    }

    private UsuarioEntity toEntity(Usuario model) {
        UsuarioEntity entity = new UsuarioEntity();
        if (model.getId() > 0) entity.setId(model.getId());
        entity.setRuc(model.getRuc());
        entity.setRazonSocial(model.getRazonSocial());
        entity.setEmail(model.getEmail());
        entity.setPerfil(model.getPerfil());
        entity.setBuenContribuyente(model.isBuenContribuyente());
        entity.setFechaRegistro(model.getFechaRegistro());
        entity.setUltimoAcceso(model.getUltimoAcceso());
        entity.setRucValidado(model.isRucValidado());
        entity.setFuenteRuc(model.getFuenteRuc());
        entity.setFechaValidacionRuc(model.getFechaValidacionRuc());
        entity.setEstadoRuc(model.getEstadoRuc());
        entity.setCondicionRuc(model.getCondicionRuc());
        entity.setRucConfianza(BigDecimal.valueOf(model.getRucConfianza()));
        if (model.getPasswordHash() != null) {
            entity.setPasswordHash(model.getPasswordHash());
        }
        return entity;
    }

    private Usuario toModel(UsuarioEntity entity) {
        Usuario u = new Usuario();
        u.setId(entity.getId() == null ? 0 : entity.getId());
        u.setRuc(entity.getRuc());
        u.setRazonSocial(entity.getRazonSocial());
        u.setEmail(entity.getEmail());
        u.setPasswordHash(entity.getPasswordHash());
        u.setBuenContribuyente(Boolean.TRUE.equals(entity.getBuenContribuyente()));
        u.setPerfil(entity.getPerfil());
        u.setFechaRegistro(entity.getFechaRegistro());
        u.setUltimoAcceso(entity.getUltimoAcceso());
        u.setRucValidado(Boolean.TRUE.equals(entity.getRucValidado()));
        u.setFuenteRuc(entity.getFuenteRuc());
        u.setFechaValidacionRuc(entity.getFechaValidacionRuc());
        u.setEstadoRuc(entity.getEstadoRuc());
        u.setCondicionRuc(entity.getCondicionRuc());
        if (entity.getRucConfianza() != null) u.setRucConfianza(entity.getRucConfianza().doubleValue());
        return u;
    }
}
