package com.asierla.das_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class v_Entrenamiento extends AppCompatActivity implements OnMapReadyCallback {

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

    private BroadcastReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

        // Inicializar el MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Permiso concedido, iniciar el servicio
            /*Intent serviceIntent = new Intent(this, LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }*/
        }

        // Restaurar el estado guardado
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime");
            elapsedTime = savedInstanceState.getLong("elapsedTime");
            totalDistance = savedInstanceState.getFloat("totalDistance");
            isRunning = savedInstanceState.getBoolean("isRunning");

            if (elapsedTime > 0) {
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

            // Restaurar los puntos de la Polyline
            if (savedInstanceState.containsKey("polylinePoints")) {
                ArrayList<LatLng> points = savedInstanceState.getParcelableArrayList("polylinePoints");
                if (points != null && !points.isEmpty()) {
                    polyline = new PolylineOptions()
                            .addAll(points)
                            .color(Color.RED)
                            .width(10);
                    if (googleMap != null) {
                        routePolyline = googleMap.addPolyline(polyline);
                    }
                }
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

        // Configurar listeners de botones
        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());

        // Abrir el reproductor de música al pulsar btnMusica
        btnMusica.setOnClickListener(v -> openMusica());

        // Registrar el BroadcastReceiver para recibir actualizaciones de ubicación
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longitude = intent.getDoubleExtra("longitude", 0);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"));
        }
    }

    private void updateLocation(Location location) {
        if (isRunning) {
            if (lastLocation != null) {
                // Filtro para ignorar ubicaciones con baja precisión o velocidad muy baja
                if (location.getAccuracy() < 10 && location.getSpeed() > 0.5) {
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

                        // Inicializar PolylineOptions si es la primera vez
                        if (polyline == null) {
                            polyline = new PolylineOptions()
                                    .add(currentLocation)
                                    .color(Color.RED) // Color de la línea
                                    .width(14); // Grosor de la línea
                            routePolyline = googleMap.addPolyline(polyline);
                        } else {
                            // Agregar el nuevo punto a la Polyline existente
                            polyline.add(currentLocation);
                            routePolyline.setPoints(polyline.getPoints());
                        }

                        // Mover la cámara al nuevo punto
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17));
                    }
                }
            }
            lastLocation = location; // Actualizar lastLocation
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
        /*Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);*/
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
        // Habilitar la capa de ubicación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        // Cambiar a vista satelital
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
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
        // Detener el servicio y desregistrar el receptor
        /*Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        unregisterReceiver(locationReceiver);*/
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

        // Guardar los puntos de la Polyline
        if (polyline != null) {
            outState.putParcelableArrayList("polylinePoints", new ArrayList<>(polyline.getPoints()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciar el servicio
                /*Intent serviceIntent = new Intent(this, LocationService.class);
                startService(serviceIntent);*/
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarEntrenamientoEnBD() {
        DBHelper dbHelper = new DBHelper(this);

        String actividad = tvEntrenamiento.getText().toString();
        String fechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        float distanciaKm = totalDistance / 1000; // Convertir a km
        long tiempoSegundos = elapsedTime;

        dbHelper.guardarEntrenamientoAuto(0, fechaHora, distanciaKm, tiempoSegundos);
        Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
    }
}