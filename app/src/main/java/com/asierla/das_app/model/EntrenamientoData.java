package com.asierla.das_app.model;

public class EntrenamientoData {
    private double distancia; // en kilómetros
    private long tiempo; // en segundos
    private double velocidad; // en km/h

    public EntrenamientoData(float distancia, long tiempo, float velocidad) {
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.velocidad = velocidad;
    }

    public double getDistancia() {
        return distancia;
    }

    public long getTiempo() {
        return tiempo;
    }

    public double getVelocidad() {
        return velocidad;
    }

    @Override
    public String toString() {
        return "Distancia: " + distancia + " km, Tiempo: " + tiempo + " s, Velocidad: " + velocidad + " km/h";
    }

}
