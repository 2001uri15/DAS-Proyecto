package com.asierla.das_app.model;

public class Entrenamiento {
    private int id;
    private int idEntrenamiento;
    private int nombre;
    private int icono;
    private String tiempo;
    private double distancia;
    private String fecha;
    private double velocidad;
    private int valoracion;
    private String comentarios;

    public Entrenamiento(int id, int idEntrenamiento, int nombre, int icono,
                         String tiempo, double distancia, String fecha,
                         double velocidad, int valoracion,
                         String comentarios) {
        this.id = id;
        this.idEntrenamiento = idEntrenamiento;
        this.icono = icono;
        this.nombre = nombre;
        this.tiempo = tiempo;
        this.distancia = distancia;
        this.fecha = fecha;
        this.velocidad = velocidad;
        this.valoracion = valoracion;
        this.comentarios = comentarios;
    }


    public int getId(){ return id; }
    public int getIdEntrenamiento(){ return idEntrenamiento;}
    public int getIcono() { return icono; }
    public int getNombreActividadId() { return nombre; }
    public String getTiempo() { return tiempo; }
    public double getDistancia() { return distancia; }
    public String getFecha() { return fecha; }
    public double getVelocidad(){return velocidad;}
    public int getValoracion(){return valoracion;}
    public String getComentarios(){return comentarios;}
}
