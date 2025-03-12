package com.asierla.das_app.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asierla.das_app.R;
import com.asierla.das_app.model.Dia;
import com.asierla.das_app.adapter.DiasAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class DiasFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiasAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dias, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_dias);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Obtener la lista de días desde la base de datos
        List<Dia> dias = obtenerDiasDesdeBD();


        adapter = new DiasAdapter(dias);

        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Dia> obtenerDiasDesdeBD() {
        // Crear una lista para almacenar los días
        List<Dia> dias = new ArrayList<>();

        // Obtener la fecha de hoy usando LocalDate
        LocalDate fechaHoy = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            fechaHoy = LocalDate.now();
        }

        // Convertir LocalDate a Date (si es necesario)
        Date fechaHoyDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            fechaHoyDate = java.sql.Date.valueOf(String.valueOf(fechaHoy));
        }

        // Crear un objeto Dia con la fecha de hoy y un número de ejercicios (por ejemplo, 5)
        Dia diaHoy = new Dia(1, fechaHoyDate, 5);

        // Añadir el día a la lista
        dias.add(diaHoy);

        // Devolver la lista de días
        return dias;
    }
}