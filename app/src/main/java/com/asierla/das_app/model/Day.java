package com.asierla.das_app.model;

import java.util.List;

public class Day {
    private String fecha;
    private List<Exercise> ejercicios;

    public Day(String fecha, List<Exercise> ejercicios) {
        this.fecha = fecha;
        this.ejercicios = ejercicios;
    }

    public String getFecha() {
        return fecha;
    }

    public List<Exercise> getEjercicios() {
        return ejercicios;
    }
}