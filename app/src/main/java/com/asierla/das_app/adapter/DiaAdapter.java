package com.asierla.das_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.R;
import com.asierla.das_app.model.PesasDia;

import java.util.List;

public class DiaAdapter extends RecyclerView.Adapter<DiaAdapter.DayViewHolder> {

    private List<PesasDia> pesasDias;

    public DiaAdapter(List<PesasDia> pesasDias) {
        this.pesasDias = pesasDias;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        PesasDia pesasDia = pesasDias.get(position);
        holder.fecha.setText(pesasDia.getFecha());
        holder.numEjercicios.setText(holder.itemView.getContext().getString(R.string.ejercicios, pesasDia.getEjercicios().size()));


        // Configurar el RecyclerView de ejercicios
        EjercicioAdapter ejercicioAdapter = new EjercicioAdapter(pesasDia.getEjercicios());
        holder.recyclerViewEjercicios.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewEjercicios.setAdapter(ejercicioAdapter);

        // Expandir/colapsar al hacer clic
        holder.itemView.setOnClickListener(v -> {
            boolean isExpanded = holder.recyclerViewEjercicios.getVisibility() == View.VISIBLE;
            holder.recyclerViewEjercicios.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public int getItemCount() {
        return pesasDias.size();
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