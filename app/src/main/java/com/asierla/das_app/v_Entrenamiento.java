package com.asierla.das_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class v_Entrenamiento extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView tvCuentaAtras, tvTiempo, tvDistancia, tvVelocidad, tvRitmo, tvEntrenamiento;
    private Button btnParar, btnReanudar, btnFinalizar;
    private LinearLayout layoutBotones;
    private ImageView btnMusica;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isRunning = false;
    private long startTime, elapsedTime;
    private float totalDistance = 0;
    private Location lastLocation;
    private Polyline routePolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);

        // Obtener idioma guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());


        tvCuentaAtras = findViewById(R.id.tvCuentaAtras);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvVelocidad = findViewById(R.id.tvVelocidad);
        tvRitmo = findViewById(R.id.tvRitmo);
        btnParar = findViewById(R.id.btnParar);
        btnReanudar = findViewById(R.id.btnReanudar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnMusica = findViewById(R.id.btnMusica);
        mapView = findViewById(R.id.mapView);
        layoutBotones = findViewById(R.id.layoutBotones);
        tvEntrenamiento = findViewById(R.id.tvEntrenamiento);

        // Obtener el tipo de entrenamiento pasado desde Home
        String tipoEntrenamiento = getIntent().getStringExtra("tipo_entrenamiento");
        // Seleccionar el recurso de string según el tipo de entrenamiento
        int stringResId;
        switch (tipoEntrenamiento) {
            case "correr":
                stringResId = R.string.correr;
                break;
            case "bici":
                stringResId = R.string.bici;
                break;
            case "andar":
                stringResId = R.string.andar;
                break;
            default:
                stringResId = R.string.app_name; // En caso de error, muestra el nombre de la app
                break;
        }

        // Establecer el nombre en el TextView
        tvEntrenamiento.setText(getString(stringResId));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        checkLocationPermission();

        // Restaurar el estado guardado
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime");
            elapsedTime = savedInstanceState.getLong("elapsedTime");
            totalDistance = savedInstanceState.getFloat("totalDistance");
            isRunning = savedInstanceState.getBoolean("isRunning");

            if(elapsedTime>0){
                tvCuentaAtras.setVisibility(View.GONE);
            }

            // Restaurar ubicación anterior
            if (savedInstanceState.containsKey("lastLatitude") && savedInstanceState.containsKey("lastLongitude")) {
                double latitude = savedInstanceState.getDouble("lastLatitude");
                double longitude = savedInstanceState.getDouble("lastLongitude");
                lastLocation = new Location("");
                lastLocation.setLatitude(latitude);
                lastLocation.setLongitude(longitude);
            }
        }

        // Iniciar la cuenta atrás solo si no estamos restaurando el estado
        if (!isRunning) {
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tvCuentaAtras.setText(String.valueOf(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    tvCuentaAtras.setVisibility(View.GONE);
                    startTraining();
                }
            }.start();
        } else {
            // Si la actividad ya estaba corriendo, reiniciar el cronómetro y actualizar la distancia
            updateTimer();
            tvDistancia.setText(String.format("%.2f km", totalDistance / 1000));
        }

        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());

        // Abrir el reproductor de música al pulsar btnMusica
        btnMusica.setOnClickListener(v -> openMusica());

        // Si presionas el botón de atrás del mobil
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(v_Entrenamiento.this)
                        .setMessage("¿Estás seguro de que quieres salir?")
                        .setCancelable(false)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Cancelar notificación activa antes de salir
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (isRunning && lastLocation != null) {
                        float distance = lastLocation.distanceTo(location);
                        totalDistance += distance;
                        tvDistancia.setText(String.format("%.2f km", totalDistance / 1000));

                        // Calcular velocidad (km/h)
                        elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                        float speedKmh = (totalDistance / elapsedTime) * 3.6f; // Convertir m/s a km/h
                        tvVelocidad.setText(String.format("%.2f km/h", speedKmh));

                        // Calcular ritmo (min/km)
                        if (totalDistance > 0) {
                            float pace = (elapsedTime / 60f) / (totalDistance / 1000);
                            int min = (int) pace;
                            int sec = (int) ((pace - min) * 60);
                            tvRitmo.setText(String.format("%02d:%02d /km", min, sec));
                        }


                        if (googleMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            if (routePolyline == null) {
                                routePolyline = googleMap.addPolyline(new PolylineOptions().add(currentLocation).color(0xFF00FF00));
                            } else {
                                routePolyline.getPoints().add(currentLocation);
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                    }
                    lastLocation = location;
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void startTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis();
        updateTimer();
    }

    private void pauseTraining() {
        isRunning = false;
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Guardamos el tiempo cuando se pausa
        lastLocation = null; // Opcional: puedes guardar la última localización si lo necesitas
        btnParar.setVisibility(View.GONE);
        layoutBotones.setVisibility(View.VISIBLE);
    }

    private void resumeTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis() - (elapsedTime * 1000); // Restauramos el tiempo desde el punto de pausa
        updateTimer(); // Reanudar cronómetro
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);
    }

    private void stopTraining() {
        isRunning = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        guardarEntrenamientoEnBD();
        Toast.makeText(this, "Entrenamiento finalizado y guardado", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(v_Entrenamiento.this, HistorialEntrenamiento.class);
        startActivity(intent);
        finish();
    }


    private void updateTimer() {
        new Thread(() -> {
            while (isRunning) {
                runOnUiThread(() -> {
                    elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    tvTiempo.setText(formatTime(elapsedTime));
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void openMusica() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Abriendo música...", Snackbar.LENGTH_SHORT);
        snackbar.show();

        String spotifyPackage = "com.spotify.music";
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(spotifyPackage);

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            try {
                Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                startActivity(intent);
            } catch (Exception e) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"));
                startActivity(webIntent);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Guardar el estado actual
        outState.putLong("startTime", startTime);
        outState.putLong("elapsedTime", elapsedTime);
        outState.putFloat("totalDistance", totalDistance);
        outState.putBoolean("isRunning", isRunning);

        // Guardar si la cuenta atrás está en curso
        outState.putBoolean("isCountingDown", tvCuentaAtras.getVisibility() == View.VISIBLE);

        // Guardar ubicación si es necesario
        if (lastLocation != null) {
            outState.putDouble("lastLatitude", lastLocation.getLatitude());
            outState.putDouble("lastLongitude", lastLocation.getLongitude());
        }
    }

    private void guardarEntrenamientoEnBD() {
        DBHelper dbHelper = new DBHelper(this);

        String actividad = tvEntrenamiento.getText().toString();
        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        float distanciaKm = totalDistance / 1000; // Convertir a km
        long tiempoSegundos = elapsedTime;

        dbHelper.guardarEntrenamiento(actividad, fechaHora, distanciaKm, tiempoSegundos);
        Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
    }

}
