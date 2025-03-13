package com.asierla.das_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.R;
import com.asierla.das_app.model.Exercise;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exercises;

    public ExerciseAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        // Asignar el nombre del ejercicio
        holder.nombreEjercicio.setText(exercise.getNombre());

        // Asignar los valores de la tabla
        holder.repeticiones.setText(String.valueOf(exercise.getRepeticiones()));
        holder.pesoMax.setText(String.valueOf(exercise.getPesoMax()));
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView nombreEjercicio, repeticiones, pesoMax;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreEjercicio = itemView.findViewById(R.id.nombreEjercicio);
            repeticiones = itemView.findViewById(R.id.repeticiones);
            pesoMax = itemView.findViewById(R.id.pesoMax);
        }
    }
}
