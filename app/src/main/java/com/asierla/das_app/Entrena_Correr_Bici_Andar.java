package com.asierla.das_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.model.EntrenamientoData;
import com.asierla.das_app.service.EntrenamientoService;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Entrena_Correr_Bici_Andar extends AppCompatActivity implements OnMapReadyCallback {
    private TextView tvCuentaAtras, tvTiempo, tvDistancia, tvVelocidad, tvRitmo, tvEntrenamiento;
    private Button btnParar, btnReanudar, btnFinalizar;
    private LinearLayout layoutBotones;
    private ImageView btnMusica;
    private MapView mapView;
    private GoogleMap googleMap;
    private boolean isRunning = false;
    private long elapsedTime = 0;
    private float totalDistance = 0;
    private Location lastLocation;
    private Polyline routePolyline;
    private final Handler handler = new Handler();
    private List<Float> speedList = new ArrayList<>();
    private CountDownTimer countDownTimer;

    // Servicio
    private EntrenamientoService entrenamientoService;
    private boolean isBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EntrenamientoService.LocalBinder binder = (EntrenamientoService.LocalBinder) service;
            entrenamientoService = binder.getService();
            isBound = true;

            // Restaurar datos del servicio
            if (entrenamientoService != null) {
                elapsedTime = entrenamientoService.getElapsedTime();
                totalDistance = entrenamientoService.getTotalDistance();
                lastLocation = entrenamientoService.getLastLocation();

                updateUI();

                if (isRunning) {
                    handler.post(timerRunnable);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de idioma
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_entrena_correr_bici_andar);

        // Inicializar vistas
        initViews();

        // Configurar mapa
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Verificar permisos
        checkPermissions();

        // Verificar optimización de batería
        checkBatteryOptimization();

        // Configurar entrenamiento
        setupTraining(savedInstanceState);

        // Configurar botones
        setupButtons();

        // Manejar botón de retroceso
        handleBackButton();
    }

    private void initViews() {
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
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void setupTraining(Bundle savedInstanceState) {
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);
        tvEntrenamiento.setText(obtenerNombreActividad(tipoEntrenamiento));

        if (savedInstanceState == null) {
            startCountdown();
        } else {
            // Restaurar estado del countdown
            boolean countdownFinished = savedInstanceState.getBoolean("countdownFinished", false);
            if (countdownFinished) {
                tvCuentaAtras.setVisibility(View.GONE);
            } else {
                startCountdown();
            }
        }
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(4000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCuentaAtras.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                tvCuentaAtras.setVisibility(View.GONE);
                startTraining();
            }
        }.start();
    }

    private void setupButtons() {
        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());
        btnMusica.setOnClickListener(v -> openMusicApp());
    }

    private void handleBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    private void startTraining() {
        isRunning = true;

        // Iniciar servicio en primer plano
        Intent serviceIntent = new Intent(this, EntrenamientoService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        handler.post(timerRunnable);
    }

    private void pauseTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        btnParar.setVisibility(View.GONE);
        layoutBotones.setVisibility(View.VISIBLE);
    }

    private void resumeTraining() {
        isRunning = true;
        handler.post(timerRunnable);
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);
    }

    private void stopTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);

        // Detener servicio
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        Intent serviceIntent = new Intent(this, EntrenamientoService.class);
        stopService(serviceIntent);

        saveTrainingToDB();
        finish();
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && isBound && entrenamientoService != null) {
                updateUI();
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void updateUI() {
        elapsedTime = entrenamientoService.getElapsedTime();
        totalDistance = entrenamientoService.getTotalDistance();

        tvTiempo.setText(formatTime(elapsedTime));
        tvDistancia.setText(String.format("%.2f km", totalDistance / 1000));

        // Actualizar mapa si hay nuevos puntos
        if (googleMap != null && entrenamientoService.getRoutePoints() != null) {
            updateMapRoute();
        }
    }

    private void updateMapRoute() {
        List<LatLng> currentRoute = entrenamientoService.getRoutePoints();
        if (currentRoute != null && !currentRoute.isEmpty()) {
            if (routePolyline != null) {
                routePolyline.remove();
            }
            routePolyline = googleMap.addPolyline(new PolylineOptions()
                    .addAll(currentRoute)
                    .width(5)
                    .color(Color.RED));

            // Mover cámara al último punto
            LatLng lastPoint = currentRoute.get(currentRoute.size() - 1);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 15));
        }
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

    private void openMusicApp() {
        Snackbar.make(findViewById(android.R.id.content),
                "Abriendo música...", Snackbar.LENGTH_SHORT).show();

        String spotifyPackage = "com.spotify.music";
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(spotifyPackage);

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://open.spotify.com")));
            } catch (Exception e) {
                Toast.makeText(this, "No se pudo abrir Spotify", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTrainingToDB() {
        DBHelper dbHelper = new DBHelper(this);

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        float distancia = totalDistance;
        long tiempoSegundos = elapsedTime;
        float averageSpeed = calculateAverageSpeed();
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);

        long idEntrena = dbHelper.guardarEntrenamientoAuto(tipoEntrenamiento, fecha,
                distancia, tiempoSegundos, averageSpeed);

        // Guardar puntos de ruta
        if (entrenamientoService != null) {
            for (LatLng punto : entrenamientoService.getRoutePoints()) {
                dbHelper.guardarPuntoRuta(idEntrena, punto.latitude, punto.longitude);
            }
        }
    }

    private float calculateAverageSpeed() {
        if (speedList.isEmpty()) return 0;
        float sum = 0;
        for (float speed : speedList) {
            sum += speed;
        }
        return sum / speedList.size();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if (entrenamientoService != null && entrenamientoService.getRoutePoints() != null) {
            updateMapRoute();
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.que_quieres_hacer)
                .setPositiveButton(R.string.guardar_salir, (dialog, which) -> stopTraining())
                .setNeutralButton(R.string.salir, (dialog, which) -> finish())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private int obtenerNombreActividad(int actividad) {
        switch (actividad) {
            case 0: return R.string.correr;
            case 1: return R.string.bici;
            case 2: return R.string.andar;
            case 3: return R.string.remo;
            case 4: return R.string.ergo;
            default: return R.string.correr;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Guardar estado del countdown
        outState.putBoolean("countdownFinished", tvCuentaAtras.getVisibility() == View.GONE);

        // Guardar estado de la actividad
        outState.putBoolean("isRunning", isRunning);
        outState.putLong("elapsedTime", elapsedTime);
        outState.putFloat("totalDistance", totalDistance);
        outState.putBoolean("isBound", isBound);

        // Guardar estado de los botones
        outState.putInt("buttonVisibility", btnParar.getVisibility());
        outState.putInt("layoutVisibility", layoutBotones.getVisibility());

        // Guardar última ubicación conocida
        if (lastLocation != null) {
            outState.putParcelable("lastLocation", lastLocation);
        }

        // Guardar lista de velocidades
        outState.putSerializable("speedList", new ArrayList<>(speedList));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (isRunning) {
            handler.post(timerRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        handler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (isBound) {
            unbindService(serviceConnection);
        }
    }
}