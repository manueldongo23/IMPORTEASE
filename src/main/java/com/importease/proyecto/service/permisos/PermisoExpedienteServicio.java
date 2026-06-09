package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.model.SolicitudPermisoDato;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.OperacionRepositorio;
import com.importease.proyecto.repository.PermisoRepositorio;
import com.importease.proyecto.repository.UsuarioRepositorio;
import com.importease.proyecto.service.LoggerUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermisoExpedienteServicio {
    private final PermisoRepositorio permisoRepositorio;
    private final OperacionRepositorio operacionRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public PermisoExpedienteServicio() {
        this(new PermisoRepositorio(), new OperacionRepositorio(), new UsuarioRepositorio());
    }

    public PermisoExpedienteServicio(PermisoRepositorio permisoRepositorio, OperacionRepositorio operacionRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.permisoRepositorio = permisoRepositorio;
        this.operacionRepositorio = operacionRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Map<String, Object> autorrellenarExpediente(int solicitudId) {
        Map<String, Object> resultado = new HashMap<>();
        SolicitudPermiso sol = permisoRepositorio.obtenerSolicitud(solicitudId);
        if (sol == null) {
            LoggerUtil.warn("Solicitud no encontrada para autorrellenar: " + solicitudId);
            return resultado;
        }

        Operacion op = operacionRepositorio.obtenerPorId(sol.getOperacionId());
        Usuario usuario = usuarioRepositorio.buscarPorId(sol.getUsuarioId());
        if (op == null || usuario == null) {
            LoggerUtil.warn("Operacion o usuario no encontrado para solicitud: " + solicitudId);
            return resultado;
        }

        permisoRepositorio.eliminarDatosSolicitud(solicitudId);
        List<SolicitudPermisoDato> datos = construirDatos(solicitudId, op, usuario);
        permisoRepositorio.guardarDatosSolicitud(solicitudId, datos);
        permisoRepositorio.actualizarEstado(solicitudId, "EXPEDIENTE_GENERADO");

        resultado.put("datos", datos);
        resultado.put("estado", "EXPEDIENTE_GENERADO");
        return resultado;
    }

    private List<SolicitudPermisoDato> construirDatos(int solicitudId, Operacion op, Usuario usuario) {
        List<SolicitudPermisoDato> datos = new ArrayList<>();
        datos.add(crearDato(solicitudId, "ruc_importador", usuario.getRuc(), "usuarios.ruc"));
        datos.add(crearDato(solicitudId, "razon_social", usuario.getRazonSocial(), "usuarios.razon_social"));
        datos.add(crearDato(solicitudId, "email_contacto", usuario.getEmail(), "usuarios.email"));
        datos.add(crearDato(solicitudId, "producto_descripcion", op.getProductoDesc(), "operaciones.producto_desc"));
        datos.add(crearDato(solicitudId, "codigo_hs", op.getHsCode(), "operaciones.hs_code"));
        datos.add(crearDato(solicitudId, "pais_origen", op.getPaisOrigen(), "operaciones.pais_origen"));
        datos.add(crearDato(solicitudId, "incoterm", op.getIncoterm(), "operaciones.incoterm"));
        datos.add(crearDato(solicitudId, "valor_fob_usd", op.getFob() != null ? op.getFob().toString() : "0", "operaciones.fob"));
        datos.add(crearDato(solicitudId, "valor_flete_usd", op.getFlete() != null ? op.getFlete().toString() : "0", "operaciones.flete"));
        datos.add(crearDato(solicitudId, "valor_seguro_usd", op.getSeguro() != null ? op.getSeguro().toString() : "0", "operaciones.seguro"));
        datos.add(crearDato(solicitudId, "valor_cif_usd", op.getCif() != null ? op.getCif().toString() : "0", "operaciones.cif"));
        datos.add(crearDato(solicitudId, "fecha_solicitud", LocalDate.now().toString(), "system"));
        return datos;
    }

    private SolicitudPermisoDato crearDato(int solicitudId, String campo, String valor, String origen) {
        SolicitudPermisoDato dato = new SolicitudPermisoDato();
        dato.setSolicitudPermisoId(solicitudId);
        dato.setCampo(campo);
        dato.setValor(valor != null ? valor : "");
        dato.setOrigenDato(origen);
        return dato;
    }
}
