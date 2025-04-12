package com.asierla.das_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Date;

public class AlarmaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARMA", "Alarma recibida a las " + new Date());
        mostrarNotificacion(context);
    }

    private void mostrarNotificacion(Context context) {
        String canalId = "canal_recordatorio";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear canal de notificación (solo necesario en Android 8.0+)
        NotificationChannel canal = new NotificationChannel(
                canalId,
                "Recordatorios de entrenamiento",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(canal);

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("¡Hora de entrenar!")
                .setContentText("No olvides completar tu rutina diaria")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Mostrar la notificación
        manager.notify(3, builder.build());
    }
}