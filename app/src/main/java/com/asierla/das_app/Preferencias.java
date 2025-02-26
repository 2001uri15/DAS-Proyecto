package com.asierla.das_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

public class Preferencias extends AppCompatActivity {


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
        setContentView(R.layout.activity_preferencias);

        // Obtener botón de guardar
        Button btnGuardar = findViewById(R.id.btnGuardar);
        // Idiomas disponibles
        String[] idiomas = {"Euskara", "English", "Castellano"};

        // Crear el Adapter usando el layout simple_list_item_single_choice
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_single_choice,
                idiomas
        );

        // Obtener el ListView del layout y asignar el Adapter
        ListView listViewIdiomas = findViewById(R.id.listIdioma);
        listViewIdiomas.setAdapter(adapter);


        // Acción al presionar Guardar
        btnGuardar.setOnClickListener(v -> {
            int posicionSeleccionada = listViewIdiomas.getCheckedItemPosition();

            if (posicionSeleccionada == ListView.INVALID_POSITION) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No hay ningún idioma seleccionado", Snackbar.LENGTH_SHORT);
                snackbar.show();
            } else {
                String idiomaSeleccionado = idiomas[posicionSeleccionada];
                String valor2 = "";

                switch (idiomaSeleccionado) {
                    case "Castellano":
                        valor2 = "es";
                        break;
                    case "Euskara":
                        valor2 = "eu";
                        break;
                    case "English":
                        valor2 = "en";
                        break;
                }

                // Guardar en SharedPreferences
                SharedPreferences prefs2 = getSharedPreferences("Ajustes", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs2.edit();
                editor.putString("idioma", valor2);
                editor.apply();

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "El idioma se ha actualizado", Snackbar.LENGTH_SHORT);
                snackbar.show();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        ImageButton btnBack = findViewById(R.id.btnBackHome);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Preferencias.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}