package com.asierla.das_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.asierla.das_app.database.DBServer;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        // Si ya ha iniciado que valla a home
        Boolean iniciado = prefs.getBoolean("iniciado", false);
        if(iniciado){
            Intent intent = new Intent(MainActivity.this, Home.class);c
            startActivity(intent);
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        }


        // Obtener idioma guardado en SharedPreferences
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);


        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Preferencias.class);
            startActivity(intent);
            finish();
        });

        Button btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(v -> {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No se puede iniciar sesión, acción no disponible", Snackbar.LENGTH_SHORT);
            snackbar.show();
        });

        Button btnEntrarSinIniciar = findViewById(R.id.btnEntrarSinIniciar);
        btnEntrarSinIniciar.setOnClickListener(v -> {
            SharedPreferences prefs2 = getSharedPreferences("Ajustes", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs2.edit();
            editor.putBoolean("iniciado", true);
            editor.apply();
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish();
        });

        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(v -> {
            SharedPreferences prefs2 = getSharedPreferences("Ajustes", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs2.edit();
            editor.putBoolean("iniciado", true);
            editor.apply();
            Intent intent = new Intent(MainActivity.this, Registrar.class);
            startActivity(intent);
            finish();
        });
    }


    private void loginUser(String username, String password) {
        Data inputData = new Data.Builder()
                .putString("action", "login")
                .putString("username", username)
                .putString("password", password)
                .build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(DBServer.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            try {
                                JSONObject response = new JSONObject(workInfo.getOutputData().getString("result"));

                                if (response.getString("status").equals("success")) {
                                    /*
                                     * Hay que guardar la informacón del usuario en preferencias
                                     * - token
                                     * - nombre, apellido, username, mail, foto.
                                     */
                                    // Login exitoso
                                    String token = response.getString("token");
                                    String nombre = response.getString("nombre");
                                    String apellido = response.getString("apellido");
                                    String username2 = response.getString("username");
                                    String mail = response.getString("mail");
                                    String foto = response.optString("foto", null);

                                    // Guardar datos de sesión (usando SharedPreferences, por ejemplo)
                                    //saveUserData(token, nombre, apellido, username2, mail, foto);

                                    // Redirigir al main activity
                                    startActivity(new Intent(this, MainActivity.class));
                                    finish();
                                } else {
                                    showError(response.getString("message"));
                                }
                            } catch (JSONException e) {
                                showError("Error al procesar la respuesta");
                            }
                        } else {
                            showError(workInfo.getOutputData().getString("result"));
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(loginRequest);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}