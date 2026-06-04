/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.importease.proyecto.service;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Usuario;
import java.math.BigDecimal;

public class RiskScoringService {

    public static ResultadoRiesgo evaluarRiesgo(BigDecimal cif, HsCode hs, Usuario usuario) {
        ResultadoRiesgo res = new ResultadoRiesgo();
        int score = 0;
        if (cif.compareTo(new BigDecimal("20000")) > 0) score += 40;
        else if (cif.compareTo(new BigDecimal("5000")) > 0) score += 20;
        else score += 5;
        if (hs != null && hs.getAdValorem().compareTo(new BigDecimal("10")) > 0) score += 25;
        else if (hs != null && hs.getAdValorem().compareTo(new BigDecimal("5")) > 0) score += 15;
        if (hs != null && hs.isRequiereVuce()) score += 20;
        if (usuario != null && "PRIMERA_IMPORTACION".equals(usuario.getPerfil())) score += 15;
        else if (usuario != null && usuario.isBuenContribuyente()) score -= 10;
        else score += 5;
        score = Math.max(0, Math.min(100, score));
        res.setScore(score);
        String canal;
        if (score <= 25) {
            canal = "VERDE";
            res.setMensajeCanal("Riesgo estimado bajo. Canal probable Verde; no representa canal oficial SUNAT.");
        } else if (score <= 60) {
            canal = "NARANJA";
            res.setMensajeCanal("Riesgo estimado medio. Canal probable Naranja; no representa canal oficial SUNAT.");
        } else {
            canal = "ROJO";
            res.setMensajeCanal("Riesgo estimado alto. Canal probable Rojo; no representa canal oficial SUNAT.");
        }
        res.setCanal(canal);
        return res;
    }

    public static class ResultadoRiesgo {
        private int score;
        private String canal;
        private String mensajeCanal;
        // getters y setters
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getCanal() { return canal; }
        public void setCanal(String canal) { this.canal = canal; }
        public String getMensajeCanal() { return mensajeCanal; }
        public void setMensajeCanal(String mensajeCanal) { this.mensajeCanal = mensajeCanal; }
    }
}

