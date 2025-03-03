package com.asierla.das_app;

public class Entrenamiento {
    private int id;
    private int icono;
    private int nombreActividadId;
    private String tiempo;
    private String distancia;
    private String fecha;

    public Entrenamiento(int id, int icono, int nombreActividadId, String tiempo, String distancia, String fecha) {
        this.id = id;
        this.icono = icono;
        this.nombreActividadId = nombreActividadId;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.fecha = fecha;
    }

    public int getId(){ return id; }
    public int getIcono() { return icono; }
    public int getNombreActividadId() { return nombreActividadId; }
    public String getTiempo() { return tiempo; }
    public String getDistancia() { return distancia; }
    public String getFecha() { return fecha; }
}
