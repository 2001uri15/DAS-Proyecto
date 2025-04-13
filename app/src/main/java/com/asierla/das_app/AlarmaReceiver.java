package com.asierla.das_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARMA", "Alarma recibida");

        String mensaje = intent.getStringExtra("mensaje_notificacion");
        if (mensaje == null) {
            mensaje = "¡Hora de entrenar!";
        }

        mostrarNotificacion(context, mensaje);
    }

    private void mostrarNotificacion(Context context, String mensaje) {
        String canalId = "Alarma_Entrenar";
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear canal de notificación (necesario para Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    canalId,
                    "Alarma_Entrenamiento",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(canal);
        }

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, canalId)
                .setSmallIcon(R.drawable.icon_app)
                .setContentTitle("Recordatorio de entrenamiento")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Mostrar la notificación
        manager.notify(3, builder.build());
    }
}