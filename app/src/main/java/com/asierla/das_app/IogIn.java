package com.asierla.das_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.Manifest;
import android.widget.TextView;
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
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class IogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
        boolean iniciado = prefs2.getBoolean("iniciado", false);
        if (iniciado){
            Intent intent = new Intent(IogIn.this, Home.class);
            startActivity(intent);
            finish();
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        }


        // Obtener idioma guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());


            EdgeToEdge.enable(this);

            setContentView(R.layout.activity_login);
            ImageButton btnBack = findViewById(R.id.btnBack);

            btnBack.setOnClickListener(v -> {
                Intent intent = new Intent(IogIn.this, Preferencias.class);
                startActivity(intent);
                finish();
            });

            Button btnEntrar = findViewById(R.id.btnEntrar);
            btnEntrar.setOnClickListener(v -> {
                TextView usuario = findViewById(R.id.inputUsuario);
                TextView contra = findViewById(R.id.inputContrasena);
                CheckBox mateSesiCheck = findViewById(R.id.matenerSesi);
                boolean mateSesi = mateSesiCheck.isChecked();

                FirebaseMessaging.getInstance().getToken()
                        .addOnSuccessListener(tokenFCM -> loginUser(usuario.getText().toString(), contra.getText().toString(), mateSesi, tokenFCM))
                        .addOnFailureListener(e -> Log.e("FCM", "Error obteniendo token", e));

            });

            Button btnEntrarSinIniciar = findViewById(R.id.btnEntrarSinIniciar);
            btnEntrarSinIniciar.setOnClickListener(v -> {
                SharedPreferences prefs3 = getSharedPreferences("Usuario", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs3.edit();
                editor.putBoolean("iniciado", true);
                editor.apply();
                Intent intent = new Intent(IogIn.this, Home.class);
                startActivity(intent);
                finish();
            });

            Button btnRegistrar = findViewById(R.id.btnRegistrar);
            btnRegistrar.setOnClickListener(v -> {
                Intent intent = new Intent(IogIn.this, Registrar.class);
                startActivity(intent);
                finish();
            });

    }


    private void loginUser(String username, String password, boolean manSesi, String tokenFCM) {
        Data inputData = new Data.Builder()
                .putString("action", "login")
                .putString("username", username)
                .putString("password", password)
                .putString("tokenFCM", tokenFCM)
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
                                    // Cogemos la info de JSON de la respuesta
                                    String token = response.getString("token");
                                    String nombre2 = response.getString("nombre");
                                    String apellido2 = response.getString("apellido");
                                    String username2 = response.getString("username");
                                    String mail2 = response.getString("mail");

                                    // Guardar datos de sesión (usando SharedPreferences, por ejemplo)
                                    SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs2.edit();
                                    if(manSesi){
                                        editor.putBoolean("iniciado", true);
                                    }else {
                                        editor.putBoolean("iniciado", false);
                                    }
                                    editor.putString("token", token);
                                    editor.putString("nombre", nombre2);
                                    editor.putString("apellido", apellido2);
                                    editor.putString("username", username2);
                                    editor.putString("mail", mail2);
                                    editor.apply();

                                    // Redirigir al main activity
                                    startActivity(new Intent(this, Home.class));
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