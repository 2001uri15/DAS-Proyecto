package com.asierla.das_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.R;
import com.asierla.das_app.model.PesasEjercicio;

import java.util.List;

public class EjercicioAdapter extends RecyclerView.Adapter<EjercicioAdapter.ExerciseViewHolder> {

    private List<PesasEjercicio> pesasEjercicios;

    public EjercicioAdapter(List<PesasEjercicio> pesasEjercicios) {
        this.pesasEjercicios = pesasEjercicios;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        PesasEjercicio pesasEjercicio = pesasEjercicios.get(position);

        // Asignar el nombre del ejercicio
        holder.nombreEjercicio.setText(pesasEjercicio.getNombre());

        // Asignar los valores de la tabla
        holder.repeticiones.setText(String.valueOf(pesasEjercicio.getRepeticiones()));
        holder.pesoMax.setText(String.valueOf(pesasEjercicio.getPesoMax()));
    }

    @Override
    public int getItemCount() {
        return pesasEjercicios.size();
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
