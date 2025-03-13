package com.asierla.das_app.model;

public class PesasEjercicio {
    private String nombre;
    private int id;
    private int repeticiones;
    private double pesoMax;

    public PesasEjercicio(String nombre, int id, int repeticiones, double pesoMax) {
        this.nombre = nombre;
        this.id = id;
        this.repeticiones = repeticiones;
        this.pesoMax = pesoMax;
    }

    public String getNombre() {
        return nombre;
    }

    public int getRepeticiones() {
        return repeticiones;
    }

    public double getPesoMax() {
        return pesoMax;
    }

    public int getId() {
        return id;
    }
}