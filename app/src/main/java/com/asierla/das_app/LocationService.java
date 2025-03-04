package com.asierla.das_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Crear un canal de notificación para Android 8.0 (API nivel 26) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "location_service_channel",
                    "Servicio de Ubicación",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Crear una notificación para el servicio en primer plano
        Notification notification = new NotificationCompat.Builder(this, "location_service_channel")
                .setContentTitle("App en ejecución")
                .setContentText("Recopilando datos de ubicación")
                .setSmallIcon(R.drawable.icon_correr) // Cambia por tu ícono
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // Iniciar el servicio en primer plano
        startForeground(1, notification);

        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(1000)
                .setFastestInterval(500)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        // Aquí puedes procesar las ubicaciones recibidas
                        // Por ejemplo, actualizar la distancia, velocidad, etc.
                        Log.d("LocationUpdate", "Latitud: " + location.getLatitude() + ", Longitud: " + location.getLongitude());
                    }
                }
            }
        };

        // Solicitar actualizaciones de ubicación
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detener las actualizaciones de ubicación cuando el servicio se detiene
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}