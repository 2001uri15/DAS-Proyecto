package com.asierla.das_app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import com.asierla.das_app.database.DBHelper;

/**
 * Implementation of App Widget functionality.
 */
public class HorasEntrenaSemana extends AppWidgetProvider {

    private static final String ACTION_CORRER = "ACTION_CORRER";
    private static final String ACTION_BICI = "ACTION_BICI";
    private static final String ACTION_ANDAR = "ACTION_ANDAR";
    private static final int META_MINUTOS = 150;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, DBHelper dbHelper) {

        SharedPreferences prefs2 = context.getSharedPreferences("Usuario", Context.MODE_PRIVATE);
        String token = prefs2.getString("token", null);
        String usuario = prefs2.getString("nombre", "");
        String apellido = prefs2.getString("apellido", "");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.horas_entrena_semana);
        if (token != null){
            views.setTextViewText(R.id.ipnombre, usuario + " " + apellido);
        }else {
            views.setTextViewText(R.id.ipnombre, "Usuario anonimo");
        }

        // Obtener tiempo total de entrenamiento
        long tiempoTotal = dbHelper.obtenerTiempoTotalSemanaActual();
        int minutos = (int) (tiempoTotal / (60 * 1000)); // Convertir milisegundos a minutos

        // Calcular porcentaje (no más del 100%)
        int porcentaje = Math.min(100, (minutos * 100) / META_MINUTOS);

        // Actualizar la barra de progreso
        views.setProgressBar(R.id.progressBarEntrena, META_MINUTOS, minutos, false);

        // Actualizar los textos de tiempo
        views.setTextViewText(R.id.tvTiempoTranscurrido, minutos + " min");
        views.setTextViewText(R.id.tvTiempoTotal, META_MINUTOS + " min");

        // Configurar las imágenes
        views.setImageViewResource(R.id.btnCorrer, R.drawable.icon_correr);
        views.setImageViewResource(R.id.btnBici, R.drawable.icon_bicicleta);
        views.setImageViewResource(R.id.btnAndar, R.drawable.icon_andar);

        // Configurar los PendingIntents
        setPendingIntent(context, views, R.id.btnCorrer, ACTION_CORRER, 0);
        setPendingIntent(context, views, R.id.btnBici, ACTION_BICI, 1);
        setPendingIntent(context, views, R.id.btnAndar, ACTION_ANDAR, 2);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setPendingIntent(Context context, RemoteViews views, int buttonId,
                                         String action, int tipoEntrenamiento) {
        Intent intent = new Intent(context, HorasEntrenaSemana.class);
        intent.setAction(action);
        intent.putExtra("tipo_entrenamiento", tipoEntrenamiento);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                buttonId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        views.setOnClickPendingIntent(buttonId, pendingIntent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs2 = context.getSharedPreferences("Usuario", Context.MODE_PRIVATE);
        String token = prefs2.getString("token", null);
        String usuario = prefs2.getString("nombre", "");
        String apellido = prefs2.getString("apellido", "");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.horas_entrena_semana);
        if (token != null){
            views.setTextViewText(R.id.ipnombre, usuario + " " + apellido);
        }else {
            views.setTextViewText(R.id.ipnombre, "Usuario anonimo");
        }

        DBHelper dbHelper = new DBHelper(context);
        try {
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, dbHelper);
            }
        } finally {
            dbHelper.close();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null) {
            Intent launchIntent = new Intent(context, Entrena_Correr_Bici_Andar.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            switch (intent.getAction()) {
                case ACTION_CORRER:
                    launchIntent.putExtra("tipo_entrenamiento", 0);
                    break;
                case ACTION_BICI:
                    launchIntent.putExtra("tipo_entrenamiento", 1);
                    break;
                case ACTION_ANDAR:
                    launchIntent.putExtra("tipo_entrenamiento", 2);
                    break;
                default:
                    return;
            }

            context.startActivity(launchIntent);

            // Actualizar el widget después de iniciar la actividad
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, HorasEntrenaSemana.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Inicializar la base de datos si es necesario
    }

    @Override
    public void onDisabled(Context context) {
        // Limpiar recursos si es necesario
    }
}