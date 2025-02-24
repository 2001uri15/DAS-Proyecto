package com.asierla.das_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class Entrenamiento extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private TextView tvCuentaAtras, tvTiempo, tvDistancia;
    private Button btnParar, btnReanudar, btnFinalizar;
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isRunning = false;
    private long startTime, elapsedTime;
    private float totalDistance = 0;
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrenamiento);

        tvCuentaAtras = findViewById(R.id.tvCuentaAtras);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvDistancia = findViewById(R.id.tvDistancia);
        btnParar = findViewById(R.id.btnParar);
        btnReanudar = findViewById(R.id.btnReanudar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        mapView = findViewById(R.id.mapView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        checkLocationPermission();

        new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCuentaAtras.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                tvCuentaAtras.setVisibility(View.GONE);
                startTraining();
            }
        }.start();

        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permisos concedidos, habilitar la ubicación en el mapa y solicitar actualizaciones de ubicación
            setupLocationUpdates();
        } else {
            // Solicitar permisos si no están concedidos
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (isRunning && lastLocation != null) {
                        totalDistance += lastLocation.distanceTo(location);
                        tvDistancia.setText(String.format("Distancia: %.2f km", totalDistance / 1000)); // Distancia en kilómetros
                    }
                    lastLocation = location;
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
        btnParar.setVisibility(View.GONE);
        btnReanudar.setVisibility(View.VISIBLE);
        btnFinalizar.setVisibility(View.VISIBLE);
    }

    private void resumeTraining() {
        isRunning = true;
        btnParar.setVisibility(View.VISIBLE);
        btnReanudar.setVisibility(View.GONE);
        btnFinalizar.setVisibility(View.GONE);
    }

    private void stopTraining() {
        isRunning = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Toast.makeText(this, "Entrenamiento finalizado", Toast.LENGTH_SHORT).show();
    }

    private void updateTimer() {
        new Thread(() -> {
            while (isRunning) {
                runOnUiThread(() -> {
                    elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                    tvTiempo.setText(formatTime(elapsedTime)); // Mostrar tiempo en formato HH:MM:SS
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
            return String.format("%02d:%02d:%02d", hours, minutes, secs); // Formato HH:MM:SS
        } else {
            return String.format("%02d:%02d", minutes, secs); // Formato MM:SS
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, habilitar ubicación en el mapa y actualizar ubicación
                setupLocationUpdates();
            } else {
                // Permiso denegado, mostrar un mensaje o manejar la falta de permisos
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        // Asegurarse de que la ubicación se habilite en el mapa si el permiso está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true); // Habilitar la ubicación del usuario
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
}
