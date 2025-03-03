package com.asierla.das_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistorialEntrenamiento extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntrenamientoAdapter adapter;
    private List<Entrenamiento> entrenamientos;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_entrenamiento);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        entrenamientos = cargarEntrenamientosDesdeDB();

        adapter = new EntrenamientoAdapter(entrenamientos);
        recyclerView.setAdapter(adapter);
    }

    private List<Entrenamiento> cargarEntrenamientosDesdeDB() {
        List<Entrenamiento> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, actividad, tiempo, distancia, fechaHora FROM entrenamientos ORDER BY fechaHora DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String actividad = cursor.getString(1);
                long tiempo = cursor.getLong(2); // Tiempo en segundos
                double distancia = cursor.getDouble(3);
                String fecha = cursor.getString(4);

                int icono = obtenerIconoActividad(actividad);
                int nombreActividadId = obtenerNombreActividad(actividad);
                String tiempoFormateado = formatearTiempo(tiempo);
                String distanciaFormateada = formatearDistancia(distancia);

                lista.add(new Entrenamiento(id, icono, nombreActividadId, tiempoFormateado, distanciaFormateada, fecha));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    private int obtenerIconoActividad(String actividad) {
        switch (actividad.toLowerCase()) {
            case "correr": return R.drawable.icon_correr;
            case "korrika": return R.drawable.icon_correr;
            case "run": return R.drawable.icon_correr;
            case "bici": return R.drawable.icon_bicicleta;
            case "bizikleta": return R.drawable.icon_bicicleta;
            case "bicycle": return R.drawable.icon_bicicleta;
            case "andar": return R.drawable.icon_andar;
            case "ibili": return R.drawable.icon_andar;
            case "walk": return R.drawable.icon_andar;
            case "remo": return R.drawable.icon_remo;
            case "arrauna": return R.drawable.icon_remo;
            case "row": return R.drawable.icon_remo;
            default: return R.drawable.circle_outline;
        }
    }

    private int obtenerNombreActividad(String actividad) {
        switch (actividad.toLowerCase()) {
            case "correr": return R.string.correr;
            case "korrika": return R.string.correr;
            case "run": return R.string.correr;
            case "bici": return R.string.bici;
            case "bizikleta": return R.string.bici;
            case "bicycle": return R.string.bici;
            case "andar": return R.string.andar;
            case "ibili": return R.string.andar;
            case "walk": return R.string.andar;
            case "remo": return R.string.remo;
            case "arrauna": return R.string.remo;
            default: return R.string.actividad_desconocida;
        }
    }

    private String formatearTiempo(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long seg = segundos % 60;

        if (horas > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, seg);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutos, seg);
        }
    }

    private String formatearDistancia(double distancia) {
        return String.format(Locale.getDefault(), "%.2f km", distancia);
    }

    public void eliminarEntrenamiento(int id, int position) {
        // Eliminar del DB
        dbHelper.eliminarEntrenamiento(id);

        // Eliminar de la lista y notificar al adaptador
        entrenamientos.remove(position);
        adapter.notifyItemRemoved(position);
    }

}
