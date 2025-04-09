package com.asierla.das_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Perfil extends AppCompatActivity {

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
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ponemos los datos del usuario registrado
        asignarDatosUsuario();

        // Si se presiona el boton de guardar
        Button btnGuardar = findViewById(R.id.saveButton);
        btnGuardar.setOnClickListener(v->{
            actualizarUsuario();
        });
    }

    private void asignarDatosUsuario() {
        TextView username = findViewById(R.id.usernameText);
        TextInputEditText nombre = findViewById(R.id.nameEditText);
        TextInputLayout apellidoLayout = findViewById(R.id.lastNameInputLayout); // Cambiado a TextInputLayout
        TextInputEditText mail = findViewById(R.id.emailEditText);

        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        username.setText(prefs.getString("username", ""));
        nombre.setText(prefs.getString("nombre", ""));

        // Obtener el EditText desde el TextInputLayout
        TextInputEditText apellidoEditText = (TextInputEditText) apellidoLayout.getEditText();
        if (apellidoEditText != null) {
            apellidoEditText.setText(prefs.getString("apellido", ""));
        }

        mail.setText(prefs.getString("mail", ""));
    }

    private void actualizarUsuario() {
        // Cogemos la información
        TextInputEditText nombre = findViewById(R.id.nameEditText);
        TextInputEditText apellido = findViewById(R.id.lastNameEditText);
        TextInputEditText mail = findViewById(R.id.emailEditText);
        TextInputEditText contra1 = findViewById(R.id.passwordEditText);
        TextInputEditText contra2 = findViewById(R.id.confirmPasswordEditText);

        if (!contra1.getText().toString().equals(contra2.getText().toString())) {
            Toast.makeText(this, "Las contraseñas tienen que ser iguales", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs2.getString("token", "");

        actualizarDatosBD(token, contra1.getText().toString(), nombre.getText().toString(), apellido.getText().toString(),
                mail.getText().toString());
    }

    private void actualizarDatosBD(String token, String password, String nombre,
                                   String apellido, String mail) {

        Data.Builder dataBuilder = new Data.Builder()
                .putString("action", "actualizarUsar")
                .putString("token", token)
                .putString("nombre", nombre)
                .putString("apellido", apellido)
                .putString("mail", mail);

        // Solo añadir password si no está vacío
        if (password != null && !password.trim().isEmpty()) {
            dataBuilder.putString("password", password);
        }

        Data inputData = dataBuilder.build();

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
                                    JSONObject user = response.getJSONObject("user");
                                    String nombre2 = user.getString("nombre");
                                    String apellido2 = user.getString("apellido");
                                    String username2 = user.getString("username");
                                    String mail2 = user.getString("mail");

                                    // Guardar datos de sesión (usando SharedPreferences, por ejemplo)
                                    SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs2.edit();
                                    editor.putBoolean("iniciado", true);
                                    editor.putString("nombre", nombre2);
                                    editor.putString("apellido", apellido2);
                                    editor.putString("username", username2);
                                    editor.putString("mail", mail2);
                                    editor.apply();

                                    // Redirigir al main activity
                                    startActivity(new Intent(this, Perfil.class));
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