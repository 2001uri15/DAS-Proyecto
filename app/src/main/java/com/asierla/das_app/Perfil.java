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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Perfil extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private ImageView imgUsuario;
    private Uri imageUri;


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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error al crear el archivo", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        "com.asierla.das_app.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "GARATU_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",        /* suffix */
                storageDir     /* directory */
        );
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    // La imagen ya se guardó en imageUri
                    setImageAndUpload(imageUri);
                    break;
                case REQUEST_IMAGE_PICK:
                    if (data != null) {
                        imageUri = data.getData();
                        setImageAndUpload(imageUri);
                    }
                    break;
            }
        }
    }

    private void setImageAndUpload(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imgUsuario.setImageBitmap(bitmap);

            // Convertir a Base64 y subir al servidor
            String imageBase64 = convertBitmapToBase64(bitmap);
            SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
            String token = prefs.getString("token", "");
            Log.d("SUBIR_IMAGEN", "Token: "+token);
            //actualizarImg(token, imageBase64);
            DBImagen.uploadImageAsBase64(bitmap, token, new DBImagen.UploadCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("SUBIR_IMAGEN", "Respuesta del servidor: " + response);
                    // Aquí puedes procesar la respuesta JSON
                }

                @Override
                public void onError(String error) {
                    Log.e("SUBIR_IMAGEN", "Error al subir imagen: " + error);
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    dispatchTakePictureIntent();
                    break;
                case REQUEST_STORAGE_PERMISSION:
                    openGallery();
                    break;
            }
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
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