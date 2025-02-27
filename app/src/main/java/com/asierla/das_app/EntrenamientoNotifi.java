package com.asierla.das_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class EntrenamientoNotifi {
    private static final String CHANNEL_ID = "channel_entrenamiento";
    private static final String CHANNEL_NAME = "Entrenamiento Activo";

    private NotificationManager notificationManager;

    public EntrenamientoNotifi(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        crearCanalNotificacion(context);
    }

    public void crearCanalNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public Notification crearNotificacion(Context context, String contenido) {
        Notification noti = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            noti = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Entrenamiento en progreso")
                    .setContentText(contenido)
                    .setSmallIcon(R.drawable.musica) // Asegúrate de que tienes un ícono adecuado
                    .setOngoing(true) // No se puede deslizar para cerrar
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en pantalla de bloqueo
                    .build();
        } else {
            // Para versiones anteriores a Oreo (API < 26)
            noti = new NotificationCompat.Builder(context)
                    .setContentTitle("Entrenamiento en progreso")
                    .setContentText(contenido)
                    .setSmallIcon(R.drawable.musica)
                    .setOngoing(true)
                    .build();
        }
        return noti;
    }

    public void actualizarNotificacion(Context context, String contenido) {
        Notification notification = crearNotificacion(context, contenido);
        notificationManager.notify(1, notification);
    }
}
