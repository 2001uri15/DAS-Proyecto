package com.asierla.das_app;

public class EntrenamientoInterval {
    private int id;
    private int idEntrena;
    private int orden;
    private long tiempo;
    private double distancia;
    private double paladas;

    public EntrenamientoInterval(int id, int idEntrena, int orden, long tiempo, double distancia, double paladas) {
        this.id = id;
        this.idEntrena = idEntrena;
        this.orden = orden;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.paladas = paladas;
    }

    public int getId() {
        return id;
    }

    public int getIdEntrena() {
        return idEntrena;
    }

    public int getOrden() {
        return orden;
    }

    public long getTiempo() {
        return tiempo;
    }

    public double getPaladas() {
        return paladas;
    }

    public double getDistancia() {
        return distancia;
    }
}
