package com.importease.proyecto.service;

import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.service.login.HashContrasenaServicio;
import com.importease.proyecto.validator.UsuarioValidador;
import java.sql.Timestamp;
import java.util.List;

/**
 * Servicio encargado de la lógica de negocio de los usuarios.
 * Orquesta la validación, enriquecimiento de datos de SUNAT, hashing de contraseñas y persistencia.
 */
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ContribuyenteServicio contribuyenteServicio;
    private final HashContrasenaServicio hashContrasenaServicio;

    public UsuarioServicio() {
        this(new UsuarioRepositorio(), new ContribuyenteServicio(), new HashContrasenaServicio());
    }

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, ContribuyenteServicio contribuyenteServicio, HashContrasenaServicio hashContrasenaServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.contribuyenteServicio = contribuyenteServicio;
        this.hashContrasenaServicio = hashContrasenaServicio;
    }

    public boolean registrarUsuario(Usuario newUser, StringBuilder outMensaje) {
        if (newUser == null) {
            outMensaje.append("Datos de usuario nulos.");
            return false;
        }
        if (newUser.getEmail() != null) {
            newUser.setEmail(newUser.getEmail().trim().toLowerCase());
        }
        // 1. Validar campos
        if (!UsuarioValidador.esEmailValido(newUser.getEmail())) {
            outMensaje.append("Formato de correo electrónico inválido.");
            return false;
        }
        if (!UsuarioValidador.esRucValido(newUser.getRuc())) {
            outMensaje.append("Número de RUC inválido.");
            return false;
        }
        String pwError = UsuarioValidador.validarPassword(newUser.getPasswordHash());
        if (pwError != null) {
            outMensaje.append(pwError);
            return false;
        }

        // 2. Verificar duplicado
        if (usuarioRepositorio.buscarPorEmail(newUser.getEmail()) != null) {
            outMensaje.append("Este email ya está registrado.");
            return false;
        }
        if (usuarioRepositorio.buscarPorRuc(newUser.getRuc()) != null) {
            outMensaje.append("Este RUC ya está registrado.");
            return false;
        }

        // 3. Enriquecer datos de SUNAT
        Usuario validatedRuc = contribuyenteServicio.validarRuc(newUser.getRuc());
        if (validatedRuc != null) {
            newUser.setRucValidado(validatedRuc.isRucValidado());
            newUser.setFuenteRuc(validatedRuc.getFuenteRuc());
            newUser.setEstadoRuc(validatedRuc.getEstadoRuc());
            newUser.setCondicionRuc(validatedRuc.getCondicionRuc());
            newUser.setRucConfianza(validatedRuc.getRucConfianza());
            newUser.setFechaValidacionRuc(new Timestamp(System.currentTimeMillis()));
            newUser.setPerfil(validatedRuc.getPerfil() != null ? validatedRuc.getPerfil() : "IMPORTADOR_ESTANDAR");
            if (validatedRuc.isBuenContribuyente()) {
                newUser.setBuenContribuyente(true);
            }
        } else {
            newUser.setRucValidado(false);
            newUser.setRucConfianza(0.0);
            newUser.setPerfil("IMPORTADOR_ESTANDAR");
        }

        // 4. Hashing de contraseña (centralizado en la capa de Servicio)
        String rawPassword = newUser.getPasswordHash();
        String hashedPassword = hashContrasenaServicio.hash(rawPassword);
        newUser.setPasswordHash(hashedPassword);

        // 5. Persistir
        return usuarioRepositorio.crearUsuario(newUser);
    }

    public boolean actualizarPassword(String email, String newPassword, StringBuilder outMensaje) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : null;
        String pwError = UsuarioValidador.validarPassword(newPassword);
        if (pwError != null) {
            outMensaje.append(pwError);
            return false;
        }
        String hashedPassword = hashContrasenaServicio.hash(newPassword);
        return usuarioRepositorio.actualizarPassword(normalizedEmail, hashedPassword);
    }

    public Usuario obtenerPorId(int id) {
        return usuarioRepositorio.buscarPorId(id);
    }

    public Usuario obtenerPorEmail(String email) {
        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : null;
        return usuarioRepositorio.buscarPorEmail(normalizedEmail);
    }

    public Usuario obtenerPorRuc(String ruc) {
        return usuarioRepositorio.buscarPorRuc(ruc);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepositorio.listarUsuarios();
    }

    public boolean actualizarExperienciaYPreferencias(int id, String nivel, String preferencias) {
        return usuarioRepositorio.actualizarExperienciaYPreferencias(id, nivel, preferencias);
    }
}
