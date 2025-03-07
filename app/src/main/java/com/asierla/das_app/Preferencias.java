package com.asierla.das_app;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Locale;

public class Preferencias extends AppCompatActivity {
    private ListView listViewIdiomas;
    private Button btnGuardar, btnNotificaciones, btnPermisos, btnBorrarDatos;

    protected void onCreate(Bundle savedInstanceState) {
        // Cargamos el layout de la vista
        setContentView(R.layout.activity_preferencias);

        // Inicialización de vistas y otros componentes
        listViewIdiomas = findViewById(R.id.listIdioma);
        btnGuardar = findViewById(R.id.btnGuardar);

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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // Idiomas disponibles
        String[] idiomas = {"Euskara", "English", "Castellano"};
        // Crear el Adapter usando el layout simple_list_item_single_choice
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_single_choice,
                idiomas
        );
        // Obtener el ListView del layout y asignar el Adapter
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Preferencias.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        // Para abrir la hoja de ajustes
        btnNotificaciones = findViewById(R.id.btnNotificaciones);
        btnNotificaciones.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        });

        // Permisos que tiene la aplicación
        btnPermisos = findViewById(R.id.btnPermisos);
        btnPermisos.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
        });
        
        btnBorrarDatos = findViewById(R.id.btnBorrarDatos);
        btnBorrarDatos.setOnClickListener(v->{
            DBHelper db = new DBHelper(this);
            db.borrarTodosLosDatosDB();
            Toast.makeText(this, "Se han borrado todos los datos locales.", Toast.LENGTH_SHORT).show();
        });

    }
}