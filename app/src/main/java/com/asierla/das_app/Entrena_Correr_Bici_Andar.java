package com.asierla.das_app;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.database.DBServer;
import com.asierla.das_app.service.EntrenamientoService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Entrena_Correr_Bici_Andar extends AppCompatActivity implements OnMapReadyCallback {

    // Views
    private TextView tvCuentaAtras, tvTiempo, tvDistancia, tvVelocidad, tvRitmo, tvEntrenamiento;
    private Button btnParar, btnReanudar, btnFinalizar;
    private LinearLayout layoutBotones;
    private ImageView btnMusica;
    private MapView mapView;
    private GoogleMap googleMap;
    private Polyline routePolyline;

    // Estado del entrenamiento
    private boolean isRunning = false;
    private boolean isActivityVisible = false;
    private final Handler handler = new Handler();
    private CountDownTimer countDownTimer;

    // Servicio
    private EntrenamientoService entrenamientoService;
    private boolean isBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EntrenamientoService.LocalBinder binder = (EntrenamientoService.LocalBinder) service;
            entrenamientoService = binder.getService();
            isBound = true;
            if (isRunning && isActivityVisible) {
                startUIUpdates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            // Intentar reconectar después de 1 segundo
            handler.postDelayed(() -> bindToService(), 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de idioma
        setupLanguage();

        setContentView(R.layout.activity_entrena_correr_bici_andar);

        // Inicializar vistas
        initViews();

        // Configurar mapa
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Verificar permisos y optimización de batería
        checkPermissions();
        checkBatteryOptimization();

        // Configurar entrenamiento
        setupTraining(savedInstanceState);

        // Configurar botones y manejo de retroceso
        setupButtons();
        handleBackButton();

        // Restaurar estado si existe
        if (savedInstanceState != null) {
            isRunning = savedInstanceState.getBoolean("isRunning", false);
        }
    }

    private void setupLanguage() {
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es");
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
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
            boolean countdownFinished = savedInstanceState.getBoolean("countdownFinished", false);
            if (countdownFinished) {
                tvCuentaAtras.setVisibility(View.GONE);
            } else {
                startCountdown();
            }

            btnParar.setVisibility(savedInstanceState.getInt("buttonVisibility", View.VISIBLE));
            layoutBotones.setVisibility(savedInstanceState.getInt("layoutVisibility", View.GONE));
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

        // Iniciar servicio si no está en ejecución
        if (!isServiceRunning(EntrenamientoService.class)) {
            Intent serviceIntent = new Intent(this, EntrenamientoService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        // Conectar al servicio
        bindToService();

        // Configurar UI
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);

        // Iniciar actualizaciones
        if (isActivityVisible) {
            startUIUpdates();
        }
    }

    private void bindToService() {
        Intent serviceIntent = new Intent(this, EntrenamientoService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startUIUpdates() {
        handler.removeCallbacks(updateUIRunnable);
        handler.post(updateUIRunnable);
    }

    private final Runnable updateUIRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBound && entrenamientoService != null && isActivityVisible) {
                updateUI();
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void updateUI() {
        long elapsedTime = entrenamientoService.getElapsedTime();
        float totalDistance = entrenamientoService.getTotalDistance();
        List<LatLng> routePoints = entrenamientoService.getRoutePoints();

        runOnUiThread(() -> {
            // Actualizar textos
            tvTiempo.setText(formatTime(elapsedTime));
            tvDistancia.setText(String.format("%.2f km", totalDistance / 1000));

            // Calcular velocidad y ritmo
            if (elapsedTime > 0 && totalDistance > 0) {
                float speed = (totalDistance / elapsedTime) * 3.6f; // m/s a km/h
                tvVelocidad.setText(String.format("%.1f km/h", speed));

                if (speed > 0) {
                    float pace = 16.6667f / speed; // min/km
                    tvRitmo.setText(String.format("%.1f min/km", pace));
                }
            }

            // Actualizar mapa
            if (googleMap != null && routePoints != null && !routePoints.isEmpty()) {
                updateMapRoute(routePoints);
            }
        });
    }

    private void updateMapRoute(List<LatLng> route) {
        runOnUiThread(() -> {
            if (routePolyline != null) {
                routePolyline.remove();
            }

            routePolyline = googleMap.addPolyline(new PolylineOptions()
                    .addAll(route)
                    .width(5)
                    .color(Color.RED));

            // Mover cámara al último punto
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.get(route.size() - 1), 15));
        });
    }

    private void pauseTraining() {
        isRunning = false;
        handler.removeCallbacks(updateUIRunnable);
        btnParar.setVisibility(View.GONE);
        layoutBotones.setVisibility(View.VISIBLE);
    }

    private void resumeTraining() {
        isRunning = true;
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);
        if (isActivityVisible) {
            startUIUpdates();
        }
    }

    private void stopTraining() {
        isRunning = false;
        handler.removeCallbacks(updateUIRunnable);

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

    private void finishNotSave() {
        isRunning = false;
        handler.removeCallbacks(updateUIRunnable);

        // Detener servicio
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        Intent serviceIntent = new Intent(this, EntrenamientoService.class);
        stopService(serviceIntent);

        finish();
    }

    private void saveTrainingToDB() {
        DBHelper dbHelper = new DBHelper(this);

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        float distancia = entrenamientoService.getTotalDistance();
        int tiempoSegundos = (int) entrenamientoService.getElapsedTime() * 1000;
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);

        // Calcular velocidad promedio
        float averageSpeed = 0;
        if (tiempoSegundos > 0) {
            averageSpeed = (distancia / tiempoSegundos) * 3.6f; // m/s a km/h
        }

        long idEntrena = dbHelper.guardarEntrenamientoAuto(tipoEntrenamiento, fecha,
                distancia, tiempoSegundos, averageSpeed);

        guardarEnServer(idEntrena, fecha, distancia, tiempoSegundos, tipoEntrenamiento, averageSpeed);


        // Guardar puntos de ruta
        if (entrenamientoService != null) {
            for (LatLng punto : entrenamientoService.getRoutePoints()) {
                dbHelper.guardarPuntoRuta(idEntrena, punto.latitude, punto.longitude);
                guardarRutaServer(idEntrena, punto.latitude, punto.longitude);
            }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        if (isBound && entrenamientoService != null) {
            updateMapRoute(entrenamientoService.getRoutePoints());
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.que_quieres_hacer)
                .setPositiveButton(R.string.guardar_salir, (dialog, which) -> stopTraining())
                .setNeutralButton(R.string.salir, (dialog, which) -> finishNotSave())
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isRunning", isRunning);
        outState.putBoolean("countdownFinished", tvCuentaAtras.getVisibility() == View.GONE);
        outState.putInt("buttonVisibility", btnParar.getVisibility());
        outState.putInt("layoutVisibility", layoutBotones.getVisibility());
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityVisible = true;
        mapView.onStart();
        if (isServiceRunning(EntrenamientoService.class)) {
            bindToService();
        }
        if (isRunning) {
            startUIUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        isActivityVisible = true;
        if (isRunning) {
            startUIUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
        handler.removeCallbacks(updateUIRunnable);
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (isBound && !isChangingConfigurations()) {
            unbindService(serviceConnection);
        }
    }

    public void guardarEnServer(long idEntrena, String fecha, float distancia,
                                int tiempo, int tipoEntrenamiento, float averageSpeed) {
        int[] list = new DBHelper(this).obtTodosLosId();

        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        Data inputData = new Data.Builder()
                .putString("action", "postDatos")
                .putString("token", token)
                .putInt("idEntrena", (int) idEntrena)
                .putString("fecha", fecha)
                .putDouble("distancia", distancia)
                .putInt("tiempo", tiempo)
                .putDouble("velocidad", averageSpeed)
                .putInt("tipoEntrena", tipoEntrenamiento)
                .build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(DBServer.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Toast.makeText(entrenamientoService, "Se ha guardado correctamente.", Toast.LENGTH_SHORT).show();
                        } else {
                            showError(workInfo.getOutputData().getString("result"));
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(loginRequest);
    }

    public void guardarRutaServer(long idEntrena, double latitud, double longitud) {

        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        Data inputData = new Data.Builder()
                .putString("action", "postRuta")
                .putString("token", token)
                .putInt("idEntrena", (int) idEntrena)
                .putDouble("latitud", latitud)
                .putDouble("longitud", longitud)
                .build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(DBServer.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Toast.makeText(entrenamientoService, "Se ha guardado correctamente.", Toast.LENGTH_SHORT).show();
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