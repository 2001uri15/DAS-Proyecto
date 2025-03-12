package com.asierla.das_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.R;
import com.asierla.das_app.model.Dia;

import java.util.List;

public class DiasAdapter extends RecyclerView.Adapter<DiasAdapter.DiaViewHolder> {

    private List<Dia> dias;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Dia dia);
    }

    public DiasAdapter(List<Dia> dias) {
        this.dias = dias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dia, parent, false);
        return new DiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaViewHolder holder, int position) {
        Dia dia = dias.get(position);
        holder.bind(dia, listener);
    }

    @Override
    public int getItemCount() {
        return dias.size();
    }

    static class DiaViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewIcon;
        private TextView textViewFecha;
        private TextView textViewNumEjercicios;

        public DiaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.image_view_icon);
            textViewFecha = itemView.findViewById(R.id.text_view_fecha);
            textViewNumEjercicios = itemView.findViewById(R.id.text_view_num_ejercicios);
        }

        public void bind(final Dia dia, final OnItemClickListener listener) {
            imageViewIcon.setImageResource(dia.getIcon());
            textViewFecha.setText(dia.getFecha());
            textViewNumEjercicios.setText(String.valueOf(dia.getNumEjercicios()));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(dia);
                }
            });
        }
    }
}
