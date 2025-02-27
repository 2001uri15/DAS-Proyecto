package com.asierla.das_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtener idioma guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnCorrer = findViewById(R.id.btnCorrer);
        btnCorrer.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, v_Entrenamiento.class);
            intent.putExtra("tipo_entrenamiento", "correr");
            startActivity(intent);
        });

        Button btnBici = findViewById(R.id.btnBici);
        btnBici.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, v_Entrenamiento.class);
            intent.putExtra("tipo_entrenamiento", "bici");
            startActivity(intent);
        });

        Button btnAndar = findViewById(R.id.btnAndar); // Si es para andar, cambia el id si es necesario
        btnAndar.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, v_Entrenamiento.class);
            intent.putExtra("tipo_entrenamiento", "andar");
            startActivity(intent);
        });

    }
}