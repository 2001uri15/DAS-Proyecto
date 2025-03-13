package com.asierla.das_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.model.MejorPesas;
import com.asierla.das_app.R;

import java.util.List;

public class MejorPesasAdapter extends RecyclerView.Adapter<MejorPesasAdapter.MejorPesasViewHolder> {

    private List<MejorPesas> mejorPesasList;

    public MejorPesasAdapter(List<MejorPesas> mejorPesasList) {
        this.mejorPesasList = mejorPesasList;
    }

    @NonNull
    @Override
    public MejorPesasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mejor_pesas, parent, false);
        return new MejorPesasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MejorPesasViewHolder holder, int position) {
        MejorPesas mejorPesas = mejorPesasList.get(position);
        holder.iconImageView.setImageResource(R.drawable.icon_pesas);
        holder.nombreTextView.setText(mejorPesas.getNombre());
        holder.maxPesoTextView.setText(String.valueOf(mejorPesas.getMaxPeso()));
    }

    @Override
    public int getItemCount() {
        return mejorPesasList.size();
    }

    public static class MejorPesasViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nombreTextView;
        TextView maxPesoTextView;

        public MejorPesasViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconoActividad);
            nombreTextView = itemView.findViewById(R.id.textActivida);
            maxPesoTextView = itemView.findViewById(R.id.textPeso);
        }
    }
}