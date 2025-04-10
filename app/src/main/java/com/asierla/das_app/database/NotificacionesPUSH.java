package com.asierla.das_app.database;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.asierla.das_app.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificacionesPUSH extends FirebaseMessagingService{
    private static final String TAG = "ServicioFirebase";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        if (message.getData().size() > 0) {
            Log.d("FIREBASE_SERVICIO", "hay datos");
        }
        if (message.getNotification() != null) {
            Log.d("FIREBASE_SERVICIO", "hay noti");


            NotificationManager elManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(getApplicationContext(), "IdCanal");

            //Crear canal de notificaciones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel elCanal = new NotificationChannel("IdCanal", "channelName", NotificationManager.IMPORTANCE_DEFAULT);
                //Configuración del canal
                elCanal.setDescription("Descripción del canal");
                elCanal.enableLights(true);
                elCanal.setLightColor(Color.RED);
                elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                elCanal.enableVibration(true);
                elManager.createNotificationChannel(elCanal);
            }


            // Crear y personalizar la notificacion
            elBuilder.setSmallIcon(R.drawable.icon_app)
                    .setContentTitle(message.getNotification().getTitle())
                    .setContentText(message.getNotification().getBody())
                    .setAutoCancel(true);

            elManager.notify(2, elBuilder.build());

        }

    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        super.onNewToken(token);
    }


}
