package com.asierla.das_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.R;
import com.asierla.das_app.model.Day;

import java.util.List;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {

    private List<Day> days;

    public DayAdapter(List<Day> days) {
        this.days = days;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        Day day = days.get(position);
        holder.fecha.setText(day.getFecha());
        holder.numEjercicios.setText(holder.itemView.getContext().getString(R.string.ejercicios, day.getEjercicios().size()));


        // Configurar el RecyclerView de ejercicios
        ExerciseAdapter exerciseAdapter = new ExerciseAdapter(day.getEjercicios());
        holder.recyclerViewEjercicios.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewEjercicios.setAdapter(exerciseAdapter);

        // Expandir/colapsar al hacer clic
        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded = holder.recyclerViewEjercicios.getVisibility() == View.VISIBLE;
            holder.recyclerViewEjercicios.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView fecha, numEjercicios;
        RecyclerView recyclerViewEjercicios;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            fecha = itemView.findViewById(R.id.fecha);
            numEjercicios = itemView.findViewById(R.id.numEjercicios);
            recyclerViewEjercicios = itemView.findViewById(R.id.recyclerViewEjercicios);
        }
    }
}