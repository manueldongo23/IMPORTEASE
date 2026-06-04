package com.importease.proyecto.model;

import java.sql.Timestamp;

public class Proveedor {
    private int id;
    private String nombre;
    private String paisOrigen;
    private int aniosAntiguedad;
    private boolean tieneTradeAssurance;
    private Timestamp fechaRegistro;

    public Proveedor() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }

    public int getAniosAntiguedad() { return aniosAntiguedad; }
    public void setAniosAntiguedad(int aniosAntiguedad) { this.aniosAntiguedad = aniosAntiguedad; }

    public boolean isTieneTradeAssurance() { return tieneTradeAssurance; }
    public void setTieneTradeAssurance(boolean tieneTradeAssurance) { this.tieneTradeAssurance = tieneTradeAssurance; }

    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

