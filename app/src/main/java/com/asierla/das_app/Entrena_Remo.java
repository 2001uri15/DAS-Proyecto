package com.asierla.das_app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;

public class Entrena_Remo extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    private NotificationManager elManager;
    private NotificationCompat.Builder elBuilder;

    private TextView tvCuentaAtras, tvTiempo, tvDistancia, tvVelocidad, tvRitmo, tvEntrenamiento;
    private Button btnParar, btnReanudar, btnFinalizar;
    private LinearLayout layoutBotones;
    private ImageView btnMusica;
    private MapView mapView;
    private GoogleMap googleMap;
    private boolean isRunning = false;
    private long startTime, elapsedTime;
    private float totalDistance = 0;
    private Location lastLocation;
    private Polyline routePolyline;
    private PolylineOptions polyline;
    private final Handler handler = new Handler();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private ArrayList<LatLng> routePoints = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastAccelerationTime = 0; // Tiempo de la última aceleración detectada
    private float accelerationThreshold = 1.5f; // Umbral de detección de aceleración (ajusta según sea necesario)
    private int accelerationsPerMinute = 0; // Número de aceleraciones por minuto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrena_remo);

        // Inicializar vistas
        tvTiempo = findViewById(R.id.tvTiempo);
        tvDistancia = findViewById(R.id.tvDistancia);
        btnParar = findViewById(R.id.btnParar);
        btnReanudar = findViewById(R.id.btnReanudar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnMusica = findViewById(R.id.btnMusica);
        mapView = findViewById(R.id.mapView);
        layoutBotones = findViewById(R.id.layoutBotones);
        tvEntrenamiento = findViewById(R.id.tvEntrenamiento);

        elManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear el canal de notificación (solo para Android O y superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal", NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(elCanal);
        }

        // Inicializar el NotificationCompat.Builder
        elBuilder = new NotificationCompat.Builder(this, "IdCanal")
                .setSmallIcon(R.drawable.icon_remo) // Icono de la notificación
                .setContentTitle("Entrenamiento") // Título de la notificación
                .setContentText("El entrenamiento va a comenzar") // Texto de la notificación
                .setAutoCancel(true); // La notificación se cierra al hacer clic en ella

        // Mostrar la notificación inicial
        elManager.notify(1, elBuilder.build());

        // Inicializar el MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Inicializar el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar la LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // Intervalo de actualización en milisegundos
        locationRequest.setFastestInterval(500); // Intervalo más rápido
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Inicializar el LocationCallback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Actualizar la ubicación y la distancia
                    updateLocation(location);
                }
            }
        };

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Inicializar el SensorManager y el sensor de acelerómetro
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                Toast.makeText(this, "El dispositivo no tiene un sensor de acelerómetro", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sensor de acelerómetro disponible", Toast.LENGTH_SHORT).show();
            }
        }

        // Configurar listeners de botones
        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());
        btnMusica.setOnClickListener(v -> openMusica());

        // Iniciar el entrenamiento automáticamente
        startTraining();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // Registrar el listener del sensor
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Desregistrar el listener del sensor para ahorrar batería
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy(); // Método necesario para el MapView
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory(); // Método necesario para el MapView
    }


    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                tvTiempo.setText(formatTime(elapsedTime)); // Actualiza el TextView con el tiempo formateado

                // Actualizar la notificación con el tiempo y la distancia
                if (elBuilder != null) { // Verificar que el Builder no sea null
                    elBuilder.setContentTitle("Entrenamiento")
                            .setContentText("Distancia: " + String.valueOf(totalDistance) + "m   Tiempo: " + formatTime(elapsedTime))
                            .setSmallIcon(R.drawable.icon_remo) // Icono de la notificación
                            .setAutoCancel(true);

                    elManager.notify(1, elBuilder.build()); // Mostrar la notificación actualizada
                }

                handler.postDelayed(this, 1000); // Repite cada segundo
            }
        }
    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    private void startTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis() - (elapsedTime * 1000); // Restaurar el tiempo acumulado
        handler.post(timerRunnable); // Inicia el temporizador
        startLocationUpdates();
    }

    private void pauseTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable); // Detener actualización
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Guardar el tiempo acumulado
        btnParar.setVisibility(View.GONE);
        layoutBotones.setVisibility(View.VISIBLE);
        stopLocationUpdates();
    }

    private void resumeTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis() - (elapsedTime * 1000); // Restauramos el tiempo desde el punto de pausa
        handler.post(timerRunnable);
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);
        startLocationUpdates();
    }

    private void stopTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        stopLocationUpdates();
        Toast.makeText(this, R.string.entrena_guardado_finalizado, Toast.LENGTH_SHORT).show();

        // En la notificación ponga total km y el tiempo
        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Entrenamiento Finalizado")
                .setContentText("Distancia: " + String.format("%.2f km", totalDistance / 1000) + " Tiempo: " + formatTime(elapsedTime))
                .setSmallIcon(R.drawable.icon_remo)
                .setAutoCancel(true);
        elManager.notify(1, elBuilder.build());

        Intent intent = new Intent(this, HistorialEntrenamiento.class);
        startActivity(intent);
        finish();
    }


    private int obtenerNombreActividad(int actividad) {
        switch (actividad) {
            case 0:
                return R.string.correr;
            case 1:
                return R.string.bici;
            case 2:
                return R.string.andar;
            case 3:
                return R.string.remo;
            case 4:
                return R.string.ergo;
            default: return R.drawable.circle_outline;
        }
    }


    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            // Formato HH:MM:SS si hay horas
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            // Formato MM:SS si no hay horas
            return String.format("%02d:%02d", minutes, secs);
        }
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
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://open.spotify.com"));
                startActivity(intent);
            } catch (Exception e) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"));
                startActivity(webIntent);
            }
        }
    }

    private void updateLocation(Location location) {
        if (lastLocation != null) {
            // Calcular la distancia entre la ubicación actual y la anterior
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance; // Acumular la distancia total

            // Actualizar la vista de distancia
            if (tvDistancia != null) { // Verificar que tvDistancia no sea null
                tvDistancia.setText(String.format("%.2f m", totalDistance));
            }
        }
        lastLocation = location; // Guardar la ubicación actual como la última

        // Actualizar el mapa con la nueva ubicación
        if (googleMap != null) {
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            routePoints.add(newLatLng); // Agregar el punto a la ruta

            // Dibujar la ruta en el mapa
            if (routePolyline != null) {
                routePolyline.remove(); // Eliminar la línea anterior
            }
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .color(Color.RED)
                    .width(10);
            routePolyline = googleMap.addPolyline(polylineOptions);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Aceleración en el eje X
            float y = event.values[1]; // Aceleración en el eje Y
            float z = event.values[2]; // Aceleración en el eje Z

            // Log para depuración
            Log.d("Acelerómetro", "Valores: X=" + x + ", Y=" + y + ", Z=" + z);

            detectAcceleration(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No es necesario implementar esto para este caso
    }

    private void detectAcceleration(SensorEvent event) {
        float x = event.values[0]; // Aceleración en el eje X
        float y = event.values[1]; // Aceleración en el eje Y
        float z = event.values[2]; // Aceleración en el eje Z

        // Calcular la magnitud de la aceleración
        float accelerationMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

        // Log para depuración
        Log.d("Acelerómetro", "Magnitud: " + accelerationMagnitude);

        // Detectar si hay una aceleración significativa
        if (accelerationMagnitude > accelerationThreshold) {
            long currentTime = System.currentTimeMillis();

            // Calcular el tiempo desde la última aceleración detectada
            if (lastAccelerationTime != 0) {
                long timeDifference = currentTime - lastAccelerationTime;

                // Calcular las aceleraciones por minuto
                if (timeDifference > 0) {
                    accelerationsPerMinute = (int) (60000 / timeDifference); // 60000 ms = 1 minuto
                    updateVelocityUI(); // Actualizar la interfaz de usuario
                }
            }

            // Guardar el tiempo de la última aceleración detectada
            lastAccelerationTime = currentTime;
        }
    }

    private void updateVelocityUI() {
        runOnUiThread(() -> {
            if (tvVelocidad != null) {
                tvVelocidad.setText(String.format("%d ppm", accelerationsPerMinute)); // ppm = pulsaciones por minuto
            }
        });
    }

    private void resetAccelerationCounter() {
        lastAccelerationTime = 0;
        accelerationsPerMinute = 0;
        updateVelocityUI();
    }
}