package com.importease.proyecto.repository;

import com.importease.proyecto.model.Operacion;
import java.util.List;
import java.util.Map;

public interface IOperacionRepositorio {
    boolean guardar(Operacion op);
    Operacion obtenerPorId(int id);
    List<Operacion> listarPorUsuario(int usuarioId);
    boolean actualizarDam(int id, String numeroDam, String estado, String canal);
    Map<String, Object> obtenerEstadisticas(int usuarioId);
}
