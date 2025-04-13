package com.asierla.das_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            reprogramarAlarmaDespuesReinicio(context);
        }
    }

    private void reprogramarAlarmaDespuesReinicio(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Ajustes", Context.MODE_PRIVATE);

        if (prefs.contains("alarma_hora")) {
            int hora = prefs.getInt("alarma_hora", 8);
            int minuto = prefs.getInt("alarma_minuto", 0);

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(context, AlarmaReceiver.class);
            intent.putExtra("mensaje_notificacion",
                    "Recordatorio reprogramado después del reinicio");

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

            if (calendario.getTimeInMillis() <= System.currentTimeMillis()) {
                calendario.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Programar alarma exacta que funciona en modo Doze
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
}