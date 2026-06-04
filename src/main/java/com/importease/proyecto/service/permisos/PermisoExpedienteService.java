package com.importease.proyecto.service.permisos;

import com.importease.proyecto.model.Operacion;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.model.SolicitudPermisoDato;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.OperacionDAO;
import com.importease.proyecto.repository.PermisoDAO;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.LoggerUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermisoExpedienteService {
    private final PermisoDAO permisoDAO;
    private final OperacionDAO operacionDAO;
    private final UsuarioDAO usuarioDAO;

    public PermisoExpedienteService() {
        this(new PermisoDAO(), new OperacionDAO(), new UsuarioDAO());
    }

    public PermisoExpedienteService(PermisoDAO permisoDAO, OperacionDAO operacionDAO, UsuarioDAO usuarioDAO) {
        this.permisoDAO = permisoDAO;
        this.operacionDAO = operacionDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public Map<String, Object> autorrellenarExpediente(int solicitudId) {
        Map<String, Object> resultado = new HashMap<>();
        SolicitudPermiso sol = permisoDAO.obtenerSolicitud(solicitudId);
        if (sol == null) {
            LoggerUtil.warn("Solicitud no encontrada para autorrellenar: " + solicitudId);
            return resultado;
        }

        Operacion op = operacionDAO.obtenerPorId(sol.getOperacionId());
        Usuario usuario = usuarioDAO.buscarPorId(sol.getUsuarioId());
        if (op == null || usuario == null) {
            LoggerUtil.warn("Operacion o usuario no encontrado para solicitud: " + solicitudId);
            return resultado;
        }

        permisoDAO.eliminarDatosSolicitud(solicitudId);
        List<SolicitudPermisoDato> datos = construirDatos(solicitudId, op, usuario);
        permisoDAO.guardarDatosSolicitud(solicitudId, datos);
        permisoDAO.actualizarEstado(solicitudId, "EXPEDIENTE_GENERADO");

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
