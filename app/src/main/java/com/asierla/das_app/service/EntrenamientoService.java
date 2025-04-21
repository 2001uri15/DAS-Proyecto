package com.asierla.das_app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyCallback;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import com.asierla.das_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EntrenamientoService extends Service {
    private static final String CHANNEL_ID = "EntrenamientoChannel";
    private static final int NOTIFICATION_ID = 1;

    // Datos del entrenamiento
    private long startTime;
    private float totalDistance = 0;
    private Location lastLocation;
    private float currentSpeed = 0;
    private float currentPace = 0;
    private float velocidadActual = 0;
    private float ritmoActual = 0;
    private float distanciaTotal = 0; // Cambiamos totalDistance por distanciaTotal para mantener consistencia
    private List<LatLng> routePoints = new ArrayList<>();
    private PowerManager.WakeLock wakeLock;
    private long lastNotificationUpdate = 0;

    // Estado de llamada
    private boolean isCallActive = false;
    private long callStartTime = 0;
    private long pausedTime = 0;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // Llamadas telefónicas
    private TelephonyManager telephonyManager;
    private MyTelephonyCallback telephonyCallback;
    private BroadcastReceiver callStateReceiver;

    // Notificación
    private NotificationManager notificationManager;
    private ScheduledExecutorService notificationUpdater;

    // Binder
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public EntrenamientoService getService() {
            return EntrenamientoService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private class MyTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int state) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("EntrenamientoService", "Llamada entrante detectada");
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (!isCallActive) {
                        isCallActive = true;
                        callStartTime = System.currentTimeMillis();
                        stopLocationUpdates();
                        Log.d("EntrenamientoService", "Llamada iniciada - Pausando entrenamiento");
                    }
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    if (isCallActive) {
                        isCallActive = false;
                        pausedTime += System.currentTimeMillis() - callStartTime;
                        startLocationUpdates();
                        Log.d("EntrenamientoService", "Llamada finalizada - Reanudando entrenamiento");
                    }
                    break;
            }
        }
    }

    private class CallStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                if (!isCallActive) {
                    isCallActive = true;
                    callStartTime = System.currentTimeMillis();
                    stopLocationUpdates();
                    Log.d("EntrenamientoService", "Llamada iniciada - Pausando entrenamiento");
                }
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                if (isCallActive) {
                    isCallActive = false;
                    pausedTime += System.currentTimeMillis() - callStartTime;
                    startLocationUpdates();
                    Log.d("EntrenamientoService", "Llamada finalizada - Reanudando entrenamiento");
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Mantener el dispositivo despierto
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::EntrenamientoService");
        wakeLock.acquire();

        // Configurar detección de llamadas
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback = new MyTelephonyCallback();
            telephonyManager.registerTelephonyCallback(getMainExecutor(), telephonyCallback);
        } else {
            callStateReceiver = new CallStateReceiver();
            IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            registerReceiver(callStateReceiver, filter);
        }

        // Configurar ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        startLocationUpdates();

        startTime = System.currentTimeMillis();

        // Configurar notificación
        notificationManager = getSystemService(NotificationManager.class);
        createNotificationChannel();
        startNotificationUpdates();
    }

    private void startNotificationUpdates() {
        notificationUpdater = Executors.newSingleThreadScheduledExecutor();
        notificationUpdater.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - lastNotificationUpdate >= 1000) {
                updateNotification();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateNotification() {
        long elapsedSeconds = getElapsedTime();
        String notificationText = String.format("Distancia: %.2f km - Tiempo: %s",
                totalDistance / 1000, formatTime(elapsedSeconds));

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Entrenamiento en curso")
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.icon_app)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
        lastNotificationUpdate = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundService();
        return START_STICKY;
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Entrenamiento en curso")
                .setContentText("Iniciando...")
                .setSmallIcon(R.drawable.notificaciones)
                .build();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startForeground(NOTIFICATION_ID, notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                } else {
                    startForeground(NOTIFICATION_ID, notification);
                }
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            Log.e("EntrenamientoService", "Error en startForeground", e);
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            locationRequest.setWaitForAccurateLocation(true);
        }
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || isCallActive) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateTrainingData(location);
                    }
                }
            }
        };
    }

    private synchronized void updateTrainingData(Location location) {
        if (lastLocation != null && location.distanceTo(lastLocation) > 0) {
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance;
            // Calcular diferencia de tiempo entre ubicaciones (en segundos)
            long diferenciaTiempoMillis = location.getTime() - lastLocation.getTime();
            float diferenciaTiempoSegundos = diferenciaTiempoMillis / 1000f;
            float diferenciaTiempoHoras = diferenciaTiempoSegundos / 3600f;

            // Convertir distancias a kilómetros
            float distanciaKm = distance / 1000f;
            float distanciaTotalKm = distanciaTotal / 1000f;

            // Calcular velocidad en km/h (si hay diferencia de tiempo)
            velocidadActual = (diferenciaTiempoSegundos > 0) ? (distanciaKm / diferenciaTiempoHoras) : 0;

            // Calcular ritmo (min/km)
            if (distanciaKm > 0 && diferenciaTiempoSegundos > 0) {
                ritmoActual = (diferenciaTiempoSegundos / 60f) / distanciaKm;
            } else {
                ritmoActual = 0;
            }
            routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        lastLocation = location;
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

    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Log.e("EntrenamientoService", "Error en permisos de ubicación", e);
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Desregistrar callbacks de llamadas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (telephonyManager != null && telephonyCallback != null) {
                telephonyManager.unregisterTelephonyCallback(telephonyCallback);
            }
        } else {
            if (callStateReceiver != null) {
                unregisterReceiver(callStateReceiver);
            }
        }

        stopLocationUpdates();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (notificationUpdater != null) {
            notificationUpdater.shutdown();
        }
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Entrenamiento Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal para notificaciones de entrenamiento en curso");
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Métodos públicos para la actividad
    public synchronized long getElapsedTime() {
        long currentTime = System.currentTimeMillis();
        if (isCallActive) {
            return ((currentTime - startTime - pausedTime - (currentTime - callStartTime)) / 1000);
        }
        return ((currentTime - startTime - pausedTime) / 1000);
    }

    public synchronized float getTotalDistance() {
        return totalDistance;
    }

    public synchronized List<LatLng> getRoutePoints() {
        return new ArrayList<>(routePoints);
    }

    public synchronized Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public synchronized float getVelocidadActual() {
        return velocidadActual;
    }

    public synchronized float getRitmoActual() {
        return ritmoActual;
    }

    public synchronized String getRitmoFormateado() {
        if (ritmoActual > 0) {
            int minutos = (int) ritmoActual;
            int segundos = (int) ((ritmoActual - minutos) * 60);
            return String.format("%02d:%02d /km", minutos, segundos);
        }
        return "--:-- /km";
    }
}