package com.asierla.das_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private NotificationManager elManager;
    private NotificationCompat.Builder elBuilder;

    public void crearNotificacion(String idCanal, String nombreCanal, int idNotificacion){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel(idCanal, nombreCanal,
                    NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(elCanal);
        }

        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Mensaje de Alerta")
                .setContentText("Ejemplo de notificación en DAS.")
                .setSubText("Información extra")
                .setAutoCancel(true);
    }

    public void editarNotificacion(String text){

    }


}
