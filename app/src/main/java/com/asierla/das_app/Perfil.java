package com.asierla.das_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

public class Perfil extends AppCompatActivity {
    private static final int REQUEST_READ_STORAGE = 101;
    private static final int PICK_IMAGE_REQUEST = 102;
    private ImageView imgUsuario;


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



        imgUsuario = findViewById(R.id.profileImage);
        imgUsuario.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Seleciona");
            builder.setItems(new CharSequence[]{
                    getString(R.string.take_photo),
                    getString(R.string.choose_from_gallery),
                    getString(R.string.cancelar)
            }, (dialog, which) -> {
                switch (which) {
                    case 0:
                        //checkCameraPermissionAndTakePhoto();
                        break;
                    case 1:
                        checkStoragePermissionAndPickImage();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                }
            });
            builder.show();
        });
        
    }

    private void checkStoragePermissionAndPickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        } else {
            pickImageFromGallery();
        }
    }


    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    // Manejar el resultado de la selección
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri selectedImageUri = data.getData();
                Log.d("Foto", "d");
            }
        }
    }

    // Manejar la respuesta de los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_STORAGE) {
            if (true) { //grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permiso denegado para acceder al almacenamiento", Toast.LENGTH_SHORT).show();
            }
        }
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

        if (!Objects.requireNonNull(contra1.getText()).toString().equals(Objects.requireNonNull(contra2.getText()).toString())) {
            Toast.makeText(this, "Las contraseñas tienen que ser iguales", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs2.getString("token", "");

        actualizarDatosBD(token, contra1.getText().toString(), Objects.requireNonNull(nombre.getText()).toString(),
                Objects.requireNonNull(apellido.getText()).toString(),
                Objects.requireNonNull(mail.getText()).toString());
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
                                JSONObject response = new JSONObject(Objects.requireNonNull(workInfo.getOutputData().getString("result")));

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

    private void actualizarImg(String token, String img) {

        Data.Builder dataBuilder = new Data.Builder()
                .putString("action", "actualizarImg")
                .putString("token", token)
                .putString("foto", img);
        Data inputData = dataBuilder.build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(DBServer.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            try {
                                JSONObject response = new JSONObject(Objects.requireNonNull(workInfo.getOutputData().getString("result")));

                                if (response.getString("status").equals("success")) {
                                    String fotoBase64 = response.getString("img");

                                    // Decodificar Base64 a Bitmap
                                    if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                                        byte[] decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                                        // Asignar el Bitmap al ImageView
                                        imgUsuario.setImageBitmap(bitmap);
                                    }

                                    // Redirigir al MainActivity (o Perfil)
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