package com.asierla.das_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Configurar el Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Configurar el botón de hamburguesa (toggle)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Mostrar el botón de hamburguesa en la barra de acción
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        ImageView btnNav = findViewById(R.id.btnNav);

        // Configurar el clic del botón para abrir el Navigation Drawer
        btnNav.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START); // Cerrar el Drawer si está abierto
            } else {
                drawerLayout.openDrawer(GravityCompat.START); // Abrir el Drawer si está cerrado
            }
        });


        // Configurar los botones de la actividad
        Button btnCorrer = findViewById(R.id.btnCorrer);
        btnCorrer.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Entrena_Correr_Bici_Andar.class);
            intent.putExtra("tipo_entrenamiento", 0);
            startActivity(intent);
        });

        Button btnBici = findViewById(R.id.btnBici);
        btnBici.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Entrena_Correr_Bici_Andar.class);
            intent.putExtra("tipo_entrenamiento", 1);
            startActivity(intent);
        });

        Button btnAndar = findViewById(R.id.btnAndar);
        btnAndar.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Entrena_Correr_Bici_Andar.class);
            intent.putExtra("tipo_entrenamiento", 2);
            startActivity(intent);
        });

        LinearLayout btnHistorial = findViewById(R.id.btnHistorial);
        btnHistorial.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, HistorialEntrenamiento.class);
            startActivity(intent);
        });

        LinearLayout btnPesas = findViewById(R.id.btnPesas);
        btnPesas.setOnClickListener(view -> Toast.makeText(this, R.string.no_disponible, Toast.LENGTH_SHORT).show());

        LinearLayout btnErgo = findViewById(R.id.btnErgo);
        btnErgo.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, Entrena_Ergo.class);
            startActivity(intent);
        });

        Button btnRemo = findViewById(R.id.btnRemo);
        btnRemo.setOnClickListener(v -> {
            // Inflar el diseño del diálogo
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_entrenamiento, null);

            // Crear el diálogo
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setTitle(R.string.tipo_entrena)
                    .create();

            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
            dialog.show();

            // Obtener referencias a las vistas
            Spinner spinnerTipoEntrenamiento = dialogView.findViewById(R.id.spinnerTipoEntrenamiento);
            LinearLayout containerDistanciaSimple = dialogView.findViewById(R.id.containerDistanciaSimple);
            EditText etTiempoSimple = dialogView.findViewById(R.id.etTiempoSimple);
            LinearLayout containerIntervalosDistancia = dialogView.findViewById(R.id.containerIntervalosDistancia);
            LinearLayout containerIntervalosTiempo = dialogView.findViewById(R.id.containerIntervalosTiempo);
            Button btnComenzar = dialogView.findViewById(R.id.btnComenzar);
            Button btnCancelar = dialogView.findViewById(R.id.btnCancelar);

            // Configurar el Spinner
            spinnerTipoEntrenamiento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Ocultar todos los contenedores
                    containerDistanciaSimple.setVisibility(View.GONE);
                    etTiempoSimple.setVisibility(View.GONE);
                    containerIntervalosDistancia.setVisibility(View.GONE);
                    containerIntervalosTiempo.setVisibility(View.GONE);

                    // Mostrar el contenedor correspondiente según la opción seleccionada
                    switch (position) {
                        case 1: // Distancia Simple
                            containerDistanciaSimple.setVisibility(View.VISIBLE);
                            break;
                        case 2: // Tiempo Simple
                            etTiempoSimple.setVisibility(View.VISIBLE);
                            break;
                        case 3: // Intervalos de Distancia
                            containerIntervalosDistancia.setVisibility(View.VISIBLE);
                            break;
                        case 4: // Intervalos de Tiempo
                            containerIntervalosTiempo.setVisibility(View.VISIBLE);
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // No hacer nada
                }
            });

            // Configurar el botón Comenzar
            btnComenzar.setOnClickListener(v1 -> {
                // Obtener los datos ingresados por el usuario
                int selectedPosition = spinnerTipoEntrenamiento.getSelectedItemPosition();
                String tipoEntrenamiento = spinnerTipoEntrenamiento.getSelectedItem().toString(); // Nombre del tipo de entrenamiento
                String distancia = "";
                String tiempo = "";
                String descanso = "";

                switch (selectedPosition) {
                    case 1: // Distancia Simple
                        distancia = ((EditText) dialogView.findViewById(R.id.etDistancia)).getText().toString();
                        break;
                    case 2: // Tiempo Simple
                        tiempo = etTiempoSimple.getText().toString();
                        break;
                    case 3: // Intervalos de Distancia
                        distancia = ((EditText) dialogView.findViewById(R.id.etDistanciaIntervalos)).getText().toString();
                        descanso = ((EditText) dialogView.findViewById(R.id.etDescansoDistancia)).getText().toString();
                        break;
                    case 4: // Intervalos de Tiempo
                        tiempo = ((EditText) dialogView.findViewById(R.id.etTiempoIntervalos)).getText().toString();
                        descanso = ((EditText) dialogView.findViewById(R.id.etDescansoTiempo)).getText().toString();
                        break;
                }

                // Crear un Intent para iniciar la actividad Entrena_Remo
                Intent intent = new Intent(Home.this, Entrena_Remo.class);
                intent.putExtra("tipoEntrenamiento", tipoEntrenamiento); // Pasar el tipo de entrenamiento
                intent.putExtra("distancia", distancia); // Pasar la distancia (si aplica)
                intent.putExtra("tiempo", tiempo); // Pasar el tiempo (si aplica)
                intent.putExtra("descanso", descanso); // Pasar el descanso (si aplica)

                // Iniciar la actividad
                startActivity(intent);

                // Cerrar el diálogo
                dialog.dismiss();
            });

            // Configurar el botón Cancelar
            btnCancelar.setOnClickListener(v1 -> dialog.dismiss());

            // Mostrar el diálogo
            dialog.show();
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Manejar las selecciones del menú
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Acción para "Inicio"
        } else if (id == R.id.nav_ajustes) {
            // Acción para "Ajustes"
            Intent intent = new Intent(Home.this, Preferencias.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_salir) {
            // Acción para cerrar sesión e ir a la página de Inicio de sesión
            // Decimos que no ha iniciado.
            SharedPreferences prefs2 = getSharedPreferences("Ajustes", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs2.edit();
            editor.putBoolean("iniciado", false);
            editor.apply();

            // Ir a la página de Inicio de sesión
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Cerrar el Navigation Drawer después de la selección
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Cerrar el Navigation Drawer si está abierto
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}