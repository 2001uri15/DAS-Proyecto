package com.asierla.das_app.model;

public class MejorPesas {
    private int idEjercicio;
    private int icon;
    private String nombre;
    private double maxPeso;

    public MejorPesas(int idEjercicio, int icon, String nombre, double maxPeso) {
        this.idEjercicio = idEjercicio;
        this.icon = icon;
        this.nombre = nombre;
        this.maxPeso = maxPeso;
    }

    public String getNombre() {
        return nombre;
    }

    public int getIdEjercicio() {
        return idEjercicio;
    }

    public double getMaxPeso() {
        return maxPeso;
    }
}
