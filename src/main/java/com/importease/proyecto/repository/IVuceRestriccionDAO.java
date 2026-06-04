/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.importease.proyecto.repository;

import com.importease.proyecto.model.VuceRestriccion;
import java.util.List;

public interface IVuceRestriccionDAO {
    VuceRestriccion obtenerPorEntidad(String entidad);
    List<VuceRestriccion> listarTodas();
}

