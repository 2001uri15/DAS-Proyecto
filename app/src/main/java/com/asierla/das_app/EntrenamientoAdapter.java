package com.asierla.das_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EntrenamientoAdapter extends RecyclerView.Adapter<EntrenamientoAdapter.EntrenamientoViewHolder> {

    private List<Entrenamiento> entrenamientos;

    public EntrenamientoAdapter(List<Entrenamiento> entrenamientos) {
        this.entrenamientos = entrenamientos;
    }

    @NonNull
    @Override
    public EntrenamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_histo_entrenamiento, parent, false);
        return new EntrenamientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrenamientoViewHolder holder, int position) {
        Entrenamiento entrenamiento = entrenamientos.get(position);
        holder.iconoActividad.setImageResource(entrenamiento.getIcono());
        holder.textActividad.setText(holder.itemView.getContext().getString(entrenamiento.getNombreActividadId()));
        holder.textTiempo.setText(entrenamiento.getTiempo());
        holder.textDistancia.setText(entrenamiento.getDistancia());
        holder.textFecha.setText(entrenamiento.getFecha());

        // Configurar el botón de borrar
        holder.btnBorrar.setOnClickListener(v -> {
            // Llamar a un método en la actividad para borrar el entrenamiento
            ((HistorialEntrenamiento) holder.itemView.getContext()).eliminarEntrenamiento(entrenamiento.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return entrenamientos.size();
    }

    public static class EntrenamientoViewHolder extends RecyclerView.ViewHolder {
        ImageView iconoActividad, btnBorrar; // Declarar btnBorrar aquí
        TextView textActividad, textTiempo, textDistancia, textFecha;

        public EntrenamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            iconoActividad = itemView.findViewById(R.id.iconoActividad);
            textActividad = itemView.findViewById(R.id.textActividad);
            textTiempo = itemView.findViewById(R.id.textTiempo);
            textDistancia = itemView.findViewById(R.id.textDistancia);
            textFecha = itemView.findViewById(R.id.textFecha);
            btnBorrar = itemView.findViewById(R.id.btnBorrar); // Inicializar btnBorrar aquí
        }
    }
}
