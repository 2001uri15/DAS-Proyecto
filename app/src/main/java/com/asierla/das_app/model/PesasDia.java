package com.asierla.das_app.model;

import java.util.List;

public class PesasDia {
    private String fecha;
    private List<PesasEjercicio> ejercicios;

    public PesasDia(String fecha, List<PesasEjercicio> ejercicios) {
        this.fecha = fecha;
        this.ejercicios = ejercicios;
    }

    public String getFecha() {
        return fecha;
    }

    public List<PesasEjercicio> getEjercicios() {
        return ejercicios;
    }
}