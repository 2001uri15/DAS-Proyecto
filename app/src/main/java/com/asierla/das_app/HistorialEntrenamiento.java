package com.asierla.das_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asierla.das_app.adapter.EntrenamientoAdapter;
import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.model.Entrenamiento;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistorialEntrenamiento extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EntrenamientoAdapter adapter;
    private List<Entrenamiento> entrenamientos;
    private DBHelper dbHelper;
    private TextView tvNoEntrenamientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_entrenamiento);

        tvNoEntrenamientos = findViewById(R.id.tvNoEntrenamientos);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        entrenamientos = cargarEntrenamientosDesdeDB();

        // Verificar si la lista está vacía
        if (entrenamientos.isEmpty()) {
            // Mostrar el TextView y ocultar el RecyclerView
            tvNoEntrenamientos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // Mostrar el RecyclerView y ocultar el TextView
            tvNoEntrenamientos.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new EntrenamientoAdapter(entrenamientos);
            recyclerView.setAdapter(adapter);
        }


    }

    /*
     * Para que cuando se vuelva se recarge
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Recargar los entrenamientos desde la base de datos
        entrenamientos.clear();
        entrenamientos.addAll(cargarEntrenamientosDesdeDB());

        // Verificar si la lista está vacía
        TextView tvNoEntrenamientos = findViewById(R.id.tvNoEntrenamientos);
        if (entrenamientos.isEmpty()) {
            // Mostrar el TextView y ocultar el RecyclerView
            tvNoEntrenamientos.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // Mostrar el RecyclerView y ocultar el TextView
            tvNoEntrenamientos.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // Notificar al adaptador que los datos han cambiado
            adapter.notifyDataSetChanged();
        }
    }




    private List<Entrenamiento> cargarEntrenamientosDesdeDB() {
        List<Entrenamiento> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Ajusté la consulta para obtener los campos correctos
        Cursor cursor = db.rawQuery("SELECT id, idActividad, tiempo, distancia, fechaHora, valoracion FROM entrenamientos ORDER BY fechaHora DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int actividad = cursor.getInt(1);
                long tiempo = cursor.getLong(2); // Tiempo en milisegundos
                double distancia = cursor.getDouble(3);
                String fecha = cursor.getString(4);
                int valoracion = cursor.getInt(5);

                int icono = obtenerIconoActividad(actividad);
                int nombreActividadId = obtenerNombreActividad(actividad);
                String tiempoFormateado = formatearTiempo(tiempo);

                lista.add(new Entrenamiento(id, actividad, nombreActividadId, icono, tiempoFormateado, distancia, fecha, 0, valoracion, null));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return lista;
    }

    private int obtenerIconoActividad(int actividad) {
        switch (actividad) {
            case 0:
                return R.drawable.icon_correr;
            case 1:
                return R.drawable.icon_bicicleta;
            case 2:
                return R.drawable.icon_andar;
            case 3:
                return R.drawable.icon_remo;
            case 4:
                return R.drawable.icon_ergo;
            default: return R.drawable.circle_outline;
        }
    }

    private int obtenerNombreActividad(int actividad) {
        switch (actividad) {
            case 0:
                return R.string.correr;
            case 1:
                return R.string.bici;
            case 2:
                return R.string.andar;
            case 3:
                return R.string.remo;
            case 4:
                return R.string.ergo;
            default: return R.drawable.circle_outline;
        }
    }

    // Ajusté la función para formatear el tiempo en milisegundos (long)
    private String formatearTiempo(long tiempo) {
        tiempo = tiempo / 1000;
        long horas = tiempo / 3600;
        long minutos = (tiempo % 3600) / 60;
        long seg = tiempo % 60;

        if (horas > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, seg);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutos, seg);
        }
    }

}
