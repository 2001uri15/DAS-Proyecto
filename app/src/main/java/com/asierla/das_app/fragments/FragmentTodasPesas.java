package com.asierla.das_app.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.asierla.das_app.AnadirEntrena;
import com.asierla.das_app.R;
import com.asierla.das_app.adapter.DayAdapter;
import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.model.Day;
import com.asierla.das_app.model.Exercise;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class FragmentTodasPesas extends Fragment {

    private RecyclerView recyclerView;
    private DayAdapter dayAdapter;
    private List<Day> days; // Lista de días
    private DBHelper dbHelper;

    public FragmentTodasPesas() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todas_pesas, container, false);

        // Inicializar la base de datos
        dbHelper = new DBHelper(getContext());

        // Configurar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        days = new ArrayList<>();
        dayAdapter = new DayAdapter(days);
        recyclerView.setAdapter(dayAdapter);

        // Cargar datos desde la base de datos
        new LoadWorkoutsTask().execute();

        // Configurar FloatingActionButton
        FloatingActionButton fabAddWorkout = view.findViewById(R.id.fabAddWorkout);
        fabAddWorkout.setOnClickListener(v -> {
            // Lógica para añadir un nuevo entrenamiento
            addNewWorkout();
        });

        return view;
    }

    // Método para añadir un nuevo entrenamiento
    private void addNewWorkout() {
        Intent intent = new Intent(getContext(), AnadirEntrena.class);
        startActivity(intent);
    }

    // AsyncTask para cargar los entrenamientos desde la base de datos
    private class LoadWorkoutsTask extends AsyncTask<Void, Void, List<Day>> {
        @Override
        protected List<Day> doInBackground(Void... voids) {
            // Obtener todos los entrenamientos de pesas desde la base de datos
            return dbHelper.obtenerTodosLosEntrenamientosDePesas();
        }

        @Override
        protected void onPostExecute(List<Day> loadedDays) {
            // Actualizar la lista de días y notificar al adaptador
            days.clear();
            days.addAll(loadedDays);
            dayAdapter.notifyDataSetChanged();
        }
    }
}