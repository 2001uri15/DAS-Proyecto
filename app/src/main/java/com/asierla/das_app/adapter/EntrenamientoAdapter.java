package com.asierla.das_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.model.Entrenamiento;
import com.asierla.das_app.R;
import com.asierla.das_app.VerEntrenamiento;
import com.asierla.das_app.database.DBHelper;

import java.util.List;

public class EntrenamientoAdapter extends RecyclerView.Adapter<EntrenamientoAdapter.EntrenamientoViewHolder> {

    private List<Entrenamiento> entrenamientos;
    private DBHelper dbHelper;

    public EntrenamientoAdapter(List<Entrenamiento> entrenamientos) {
        this.entrenamientos = entrenamientos;
        Context context = null;
        this.dbHelper = new DBHelper(context);  // Inicializar correctamente DBHelper con el contexto
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
        holder.textFecha.setText(entrenamiento.getFecha());

        if(entrenamiento.getIdEntrenamiento()>=0 && entrenamiento.getIdEntrenamiento()<=2){
            // Carrera, Bici, Andar
            double distanciaEnKm = entrenamiento.getDistancia() / 1000.0;
            String distanciaFormateada = String.format("%.2f", distanciaEnKm);
            holder.textDistancia.setText(distanciaFormateada + " km");
        }else if(entrenamiento.getIdEntrenamiento()>2 && entrenamiento.getIdEntrenamiento()<5){
            // Remo y Ergometro
            holder.textDistancia.setText(String.valueOf((int)entrenamiento.getDistancia()) + " m");
        }else{
            // Cualquier otro caso
            holder.textDistancia.setText(String.valueOf(entrenamiento.getDistancia()));
        }


        // Configurar el clic en el item
        holder.itemHis.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, VerEntrenamiento.class);  // Reemplaza con la actividad que abrirás
            intent.putExtra("idEntrena", entrenamiento.getId());  // Pasar el id del entrenamiento
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return entrenamientos.size();
    }

    public static class EntrenamientoViewHolder extends RecyclerView.ViewHolder {
        ImageView iconoActividad, btnBorrar; // Declarar btnBorrar aquí
        TextView textActividad, textTiempo, textDistancia, textFecha;
        RelativeLayout itemHis;  // Declarar itemHis aquí

        public EntrenamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            iconoActividad = itemView.findViewById(R.id.iconoActividad);
            textActividad = itemView.findViewById(R.id.textActivida);
            textTiempo = itemView.findViewById(R.id.textTiempo);
            textDistancia = itemView.findViewById(R.id.textPeso);
            textFecha = itemView.findViewById(R.id.textFecha);
            itemHis = itemView.findViewById(R.id.itemHis);  // Inicializar itemHis aquí
        }
    }
}
