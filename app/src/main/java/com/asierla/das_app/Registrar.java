package com.asierla.das_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.asierla.das_app.database.DBServer;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Registrar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView iniciarSesion = findViewById(R.id.iniciarSesion);
        iniciarSesion.setOnClickListener(v->{
            Intent intent = new Intent(this, IogIn.class);
            startActivity(intent);
            finish();
        });

        TextView btnRegistro = findViewById(R.id.btnRegistro);
        btnRegistro.setOnClickListener(v->{
            CheckBox privacidad = findViewById(R.id.checkPrivacidad);
            EditText usuario = findViewById(R.id.inputUsuario);
            EditText nombre = findViewById(R.id.inputNombre);
            EditText apellido = findViewById(R.id.inputApellido);
            EditText contra1 = findViewById(R.id.inputContrasena);
            EditText contra2 = findViewById(R.id.inputConfirmarContrasena);
            EditText mail = findViewById(R.id.inputmail);

            if(!privacidad.isChecked()){
                Toast.makeText(this, "Tienes que aceptar la privacidad", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!contra1.getText().toString().equals(contra2.getText().toString())) {
                Toast.makeText(this, "Las contraseñas tienen que ser iguales", Toast.LENGTH_SHORT).show();
                return;
            }

            int priva;
            if (privacidad.isChecked()){
                priva=1;
            } else {
                priva = 0;
            }

            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(tokenFCM -> loginUser(usuario.getText().toString(), contra1.getText().toString(),
                            nombre.getText().toString(), apellido.getText().toString(), priva,
                            mail.getText().toString(), tokenFCM))
                    .addOnFailureListener(e -> Log.e("FCM", "Error obteniendo token", e));

        });
    }

    private void loginUser(String username, String password, String nombre, String apellido,
                           int privacidad, String mail, String tokenFCM) {
        Data inputData = new Data.Builder()
                .putString("action", "registrar")
                .putString("username", username)
                .putString("password", password)
                .putString("nombre", nombre)
                .putString("apellido", apellido)
                .putInt("privacidad", privacidad)
                .putString("mail", mail)
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
                                    editor.putBoolean("iniciado", true);
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