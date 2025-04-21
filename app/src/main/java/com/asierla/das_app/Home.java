package com.asierla.das_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.asierla.das_app.database.DBDatos;
import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.database.DBImagen;
import com.asierla.das_app.database.DBServer;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ImageView imgUsuario;

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

        // Para poner el nombre del usuario y si esta iniciado el perfil
        setupNavHeader(navigationView);

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
        btnPesas.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, RegistroPesas.class);
            startActivity(intent);
        });

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

            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_background);
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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

        SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs2.getString("token", null);
        if (token!=null){
            actualizarDatos();
        }
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
        } else if (id == R.id.nav_perfil) {
            // Acción para "Perfil"
            Intent intent = new Intent(Home.this, Perfil.class); // Asume que tienes una actividad PerfilActivity
            startActivity(intent);
        } else if (id == R.id.nav_salir) {
            // Acción para cerrar sesión e ir a la página de Inicio de sesión
            // Decimos que no ha iniciado.
            SharedPreferences prefs2 = getSharedPreferences("Usuario", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs2.edit();
            String token = prefs2.getString("token", "");
            editor.putBoolean("iniciado", false);
            editor.putString("token", null);
            editor.putString("nombre", null);
            editor.putString("apellido", null);
            editor.putString("username", null);
            editor.putString("mail", null);
            editor.apply();

            borrarSesion(token);

            // Ir a la página de Inicio de sesión
            Intent intent = new Intent(Home.this, IogIn.class);
            startActivity(intent);
            finish();
        }

        // Cerrar el Navigation Drawer después de la selección
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                        runOnUiThread(() -> {
                            if (bitmap != null) {
                                imgUsuario.setImageBitmap(bitmap);
                            } else {
                                Log.e("ImageError", "No se pudo decodificar la imagen");
                            }
                        });
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

    @Override
    public void onBackPressed() {
        // Cerrar el Navigation Drawer si está abierto
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupNavHeader(NavigationView navigationView) {
        // Obtener la vista del header (puede ser null si no hay header)
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;



        // Obtener referencias a los views
        TextView tvUsername = headerView.findViewById(R.id.username);
        TextView tvMail = headerView.findViewById(R.id.mail);

        // Obtener datos del usuario (desde SharedPreferences, base de datos, etc.)
        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        String nombre = prefs.getString("nombre", getString(R.string.app_name));
        String apellido = prefs.getString("apellido", " ");
        String email = prefs.getString("mail", " ");
        String token = prefs.getString("token", null);

        // Actualizar views
        tvUsername.setText(nombre + " " + apellido);
        tvMail.setText(email);


        // Mostrar u ocultar ítems del menú según si está logueado
        Menu menu = navigationView.getMenu();
        MenuItem profileItem = menu.findItem(R.id.nav_perfil);

        if (token!=null) {
            profileItem.setVisible(true);
            imgUsuario = headerView.findViewById(R.id.userFoto);
            getFotoPerfilDBImagen();
        }else{
            profileItem.setVisible(false);
        }

    }


    private void borrarSesion(String token) {
        Data inputData = new Data.Builder()
                .putString("action", "borrarSesion")
                .putString("token", token)
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
                                    String status = response.getString("status");
                                    new DBHelper(Home.this).borrarTodosLosDatosDB();
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
    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar la imagen del perfil y los datos del header
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupNavHeader(navigationView);
    }

    public void actualizarDatos() {
        int[] list = new DBHelper(this).obtTodosLosId();
        String idsStr = Arrays.stream(list)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        Log.d("ENTRE", Arrays.toString(list));
        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        DBDatos dbDatos = new DBDatos(this);
        dbDatos.actualizarDatos(token, idsStr, new DBDatos.ApiCallback() {
            @Override
            public void onSuccess(JSONObject responseJson) {
                // Usamos runOnUiThread para asegurarnos que el código se ejecute en el hilo principal
                runOnUiThread(() -> {
                    try {
                        String status = responseJson.getString("status");
                        Log.e("DBDatos", "JSON " + status);
                        if (responseJson.getString("status").equals("success")) {
                            JSONArray entrenamientos = responseJson.getJSONArray("data");
                            DBHelper dbHelper = new DBHelper(Home.this);

                            for (int i = 0; i < entrenamientos.length(); i++) {
                                JSONObject entrenamiento = entrenamientos.getJSONObject(i);

                                // Guardar el entrenamiento
                                long entrenamientoId = dbHelper.guardarEntrenamientoConID(
                                        entrenamiento.getInt("id_local"),
                                        entrenamiento.getInt("idActividad"),
                                        entrenamiento.getString("fechaHora"),
                                        entrenamiento.getDouble("distancia"),
                                        entrenamiento.getLong("tiempo"),
                                        entrenamiento.isNull("velocidad") ? 0 : entrenamiento.getDouble("velocidad"),
                                        entrenamiento.isNull("valoracion") ? 0 : entrenamiento.getInt("valoracion"),
                                        entrenamiento.isNull("comentarios") ? null : entrenamiento.getString("comentarios")
                                );

                                // Guardar las rutas si existen
                                if (entrenamiento.has("rutas")) {
                                    JSONArray rutas = entrenamiento.getJSONArray("rutas");
                                    for (int j = 0; j < rutas.length(); j++) {
                                        JSONObject ruta = rutas.getJSONObject(j);
                                        dbHelper.guardarPuntoRuta(
                                                entrenamientoId,
                                                ruta.getDouble("latitud"),
                                                ruta.getDouble("longitud")
                                        );
                                    }
                                }
                            }
                            Toast.makeText(Home.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Home.this, "ERROR: " + responseJson.getString("status"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("DBDatos", "Error al parsear JSON", e);
                        Toast.makeText(Home.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(File responseFile) {
                runOnUiThread(() -> {
                    try {
                        Log.e("DBDatos", "FILE");
                        // Aquí puedes procesar el archivo si es necesario
                    } finally {
                        boolean rel = responseFile.delete();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Log.e("DBDatos", "Error en la petición: " + errorMessage);
                    Toast.makeText(Home.this, "Error al cargar los datos: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}