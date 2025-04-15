package com.asierla.das_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.asierla.das_app.database.DBImagen;
import com.asierla.das_app.database.DBServer;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Perfil extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ImageView imgUsuario;
    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap laminiatura = (Bitmap) bundle.get("data");
                    //GUARDAR COMO FICHERO
                    // Memoria externa
                    File eldirectorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String nombrefichero = "IMG_" + timeStamp + "_";
                    File imagenFich = new File(eldirectorio, nombrefichero + ".jpg");
                    OutputStream os;
                    try {
                        os = new FileOutputStream(imagenFich);
                        laminiatura.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.flush();
                        os.close();
                        MediaScannerConnection.scanFile(
                                this,
                                new String[]{imagenFich.getAbsolutePath()},
                                new String[]{"image/jpeg"},
                                null
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
                    String token = prefs.getString("token", "");

                    DBImagen.uploadImageAsBase64(laminiatura, token, new DBImagen.UploadCallback() {
                        @Override
                        public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    String status = jsonResponse.getString("status");
                                    String message = jsonResponse.getString("message");

                                    if (status.equals("success")) {
                                        imgUsuario.setImageBitmap(laminiatura);
                                        Toast.makeText(Perfil.this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Perfil.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    onError("Error al procesar respuesta: " + e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                Toast.makeText(Perfil.this, "Error subiendo imagen: " + error, Toast.LENGTH_SHORT).show();
                                Log.e("ImageUpload", "Error: " + error);
                            });
                        }
                    });
                }
            });
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Log.d("IMG", "URI seleccionada: " + uri);

                    // Mostrar carga mientras se procesa
                    runOnUiThread(() ->
                            Toast.makeText(this, "Procesando imagen...", Toast.LENGTH_SHORT).show());

                    new Thread(() -> {
                        try {
                            // 1. Convertir imagen a Base64
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
                            String token = prefs.getString("token", "");

                            // 3. Subir al servidor
                            DBImagen.uploadImageAsBase64(bitmap, token, new DBImagen.UploadCallback() {
                                @Override
                                public void onSuccess(String response) {
                                    runOnUiThread(() -> {
                                        try {
                                            // 4. Mostrar imagen solo si se subió correctamente
                                            ImageView imageView = findViewById(R.id.profileImage);
                                            imageView.setImageURI(uri);
                                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            Toast.makeText(Perfil.this, "Imagen actualizada!", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.e("IMG_VIEW", "Error al mostrar imagen", e);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(Perfil.this, "Error al subir: " + error, Toast.LENGTH_SHORT).show();
                                        Log.e("UPLOAD_ERR", error);
                                    });
                                }
                            });

                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(Perfil.this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
                                Log.e("IMG_PROCESS", e.getMessage());
                            });
                        }
                    }).start();

                } else {
                    Log.d("IMG", "No se seleccionó imagen");
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se seleccionó imagen", Toast.LENGTH_SHORT).show());
                }
            });


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

        getFotoPerfilDBImagen();


        imgUsuario = findViewById(R.id.profileImage);
        imgUsuario.setOnClickListener(v -> showImagePickerDialog());

    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        builder.setItems(new CharSequence[]{"Tomar foto", "Elegir de galería", "Cancelar"},
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermission();
                            break;
                        case 1:
                            Log.d("IMG", "Dialogo");
                            checkStoragePermission();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void checkStoragePermission() {
        Log.d("IMG", "Permisos");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("IMG", "Permisos: if");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            Log.d("IMG", "Permisos: else");
            openGallery();  // Si ya tiene permiso, abre la galería
        }
    }

    private void openGallery() {
        Log.d("IMG", "Galeria");
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    private void dispatchTakePictureIntent() {
        Intent elIntentFoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(elIntentFoto);
    }

    private void getFotoPerfilDBImagen() {
        Log.d("IMAGEN_PERFIL", "Fun obtener Imagen");
        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs.getString("token", "");
        DBImagen.obtenerImagen(token, new DBImagen.UploadCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Parsear la respuesta JSON
                    JSONObject jsonResponse = new JSONObject(response);
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    if (status.equals("success")) {
                        String fotoBase64 = jsonResponse.getString("foto");
                        Log.d("IMAGEN_PERFIL", "Longitud img : " + fotoBase64.length());

                        // Decodificar la imagen base64
                        byte[] decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                        // Mostrar la imagen en el ImageView
                        if (bitmap != null) {
                            imgUsuario.setImageBitmap(bitmap);
                        } else {
                            Log.e("ImageError", "No se pudo decodificar la imagen");
                        }
                    } else {
                        Log.e("APIError", message);
                    }
                } catch (JSONException e) {
                    Log.e("JSONError", "Error al parsear la respuesta", e);
                } catch (IllegalArgumentException e) {
                    Log.e("Base64Error", "Cadena base64 inválida", e);
                }
            }

            @Override
            public void onError(String error) {
                Log.e("NetworkError", error);
            }
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

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}