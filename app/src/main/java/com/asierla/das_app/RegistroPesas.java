package com.asierla.das_app;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.asierla.das_app.fragments.FragmentMejoresTiempos;
import com.asierla.das_app.fragments.FragmentTodasPesas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RegistroPesas extends AppCompatActivity {

    private Button btnTodasPesas;
    private Button btnMejoresTiempos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_pesas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Que solo pueda estar en forma vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        btnTodasPesas = findViewById(R.id.btnTodasPesas);
        btnMejoresTiempos = findViewById(R.id.btnMejorTiempo);

        btnTodasPesas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new FragmentTodasPesas());
                btnTodasPesas.setBackgroundResource(R.drawable.boton_seleccionado);
                btnMejoresTiempos.setBackgroundResource(android.R.color.transparent);
            }
        });

        btnMejoresTiempos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new FragmentMejoresTiempos());
                btnMejoresTiempos.setBackgroundResource(R.drawable.boton_seleccionado);
                btnTodasPesas.setBackgroundResource(android.R.color.transparent);
            }
        });

        // Cargar el fragment por defecto
        loadFragment(new FragmentTodasPesas());
        btnTodasPesas.setBackgroundResource(R.drawable.boton_seleccionado);


    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}