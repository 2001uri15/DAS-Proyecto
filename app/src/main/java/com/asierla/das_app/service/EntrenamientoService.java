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

public class EntrenamientoService extends Service {
    private static final String CHANNEL_ID = "EntrenamientoChannel";
    private static final int NOTIFICATION_ID = 1;

    // Datos del entrenamiento
    private long startTime;
    private float totalDistance = 0;
    private Location lastLocation;
    private List<LatLng> routePoints = new ArrayList<>();
    private PowerManager.WakeLock wakeLock;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    // Broadcast
    private BroadcastReceiver airplaneModeReceiver;

    // Binder
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public EntrenamientoService getService() {
            return EntrenamientoService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Mantener el dispositivo despierto
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::TrainingWakeLock");
        wakeLock.acquire();

        // Configurar ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();

        // Registrar BroadcastReceiver para modo avión
        registerAirplaneModeReceiver();

        // Canal de notificación
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification("Entrenamiento en curso");
        startForegroundService(notification);

        startLocationUpdates();
        startTime = System.currentTimeMillis();

        return START_STICKY;
    }

    private void startForegroundService(Notification notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Verificamos permisos de ubicación
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    startForeground(NOTIFICATION_ID, notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                } else {
                    // Fallback sin tipo específico
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Entrenamiento Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Canal para notificaciones de entrenamiento en curso");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Entrenamiento en curso")
                .setContentText(text)
                .setSmallIcon(R.drawable.icon_app)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    private void updateNotification(String text) {
        Notification notification = createNotification(text);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
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
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    updateTrainingData(location);
                }
            }
        };
    }

    private void updateTrainingData(Location location) {
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance;

            LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
            routePoints.add(newPoint);
        }
        lastLocation = location;

        // Actualizar notificación
        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        String notificationText = String.format("Distancia: %.2f km - Tiempo: %s",
                totalDistance / 1000, formatTime(elapsedTime));
        updateNotification(notificationText);
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
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void registerAirplaneModeReceiver() {
        airplaneModeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                    boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
                    String message = isAirplaneModeOn ?
                            "Modo avión activado - Entrenamiento continúa" :
                            "Modo avión desactivado";
                    updateNotification(message);
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(airplaneModeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (airplaneModeReceiver != null) {
            unregisterReceiver(airplaneModeReceiver);
        }
    }

    // Métodos públicos para la actividad
    public long getElapsedTime() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public List<LatLng> getRoutePoints() {
        return routePoints;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}