package com.asierla.das_app.model;

import com.asierla.das_app.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Dia {
    private int id;
    private Date fecha;
    private int numEjercicios; // Número de ejercicios realizados ese día

    public Dia(int id, Date fecha, int numEjercicios) {
        this.id = id;
        this.fecha = fecha;
        this.numEjercicios = numEjercicios;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public CharSequence getFecha() {
        // Formatear la fecha como una cadena
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fecha);
    }

    public int getNumEjercicios() {
        return numEjercicios;
    }



    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public void setNumEjercicios(int numEjercicios) {
        this.numEjercicios = numEjercicios;
    }

    public int getIcon() {
        return R.drawable.icon_pesas;
    }
}