package com.asierla.das_app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class AlarmaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARMA", "Alarma recibida");

        String mensaje = intent.getStringExtra("mensaje_notificacion");
        if (mensaje == null) {
            mensaje = "No olvides completar tu rutina diaria.";
        }

        mostrarNotificacion(context, mensaje);

        // Reprogramar la alarma para el próximo día
        reprogramarAlarma(context);
    }

    private void reprogramarAlarma(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Ajustes", Context.MODE_PRIVATE);
        if (prefs.contains("alarma_hora")) {
            int hora = prefs.getInt("alarma_hora", 8);
            int minuto = prefs.getInt("alarma_minuto", 0);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, AlarmaReceiver.class);
            intent.putExtra("mensaje_notificacion", "¡Hora de entrenar!");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendario = Calendar.getInstance();
            calendario.set(Calendar.HOUR_OF_DAY, hora);
            calendario.set(Calendar.MINUTE, minuto);
            calendario.set(Calendar.SECOND, 0);
            calendario.add(Calendar.DAY_OF_YEAR, 1); // Siempre programar para mañana

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendario.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendario.getTimeInMillis(),
                        pendingIntent
                );
            }
        }
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
                .setSmallIcon(R.drawable.notificaciones)
                .setContentTitle("¡Hora de entrenar!")
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Mostrar la notificación
        manager.notify(3, builder.build());
    }
}