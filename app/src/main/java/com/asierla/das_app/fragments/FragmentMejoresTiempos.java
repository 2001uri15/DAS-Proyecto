package com.asierla.das_app.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.asierla.das_app.R;
import com.asierla.das_app.adapter.MejorPesasAdapter;
import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.model.MejorPesas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class FragmentMejoresTiempos extends Fragment {

    private RecyclerView recyclerView;
    private MejorPesasAdapter adapter;

    public FragmentMejoresTiempos() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mejores_tiempos, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Use requireContext() to ensure the context is not null
        DBHelper db = new DBHelper(requireContext());

        // Configurar el adaptador
        adapter = new MejorPesasAdapter(db.obtenerMejoresResulatdos());
        recyclerView.setAdapter(adapter);

        return view;
    }
}