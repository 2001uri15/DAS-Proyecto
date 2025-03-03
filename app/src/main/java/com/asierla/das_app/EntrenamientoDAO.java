package com.asierla.das_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class EntrenamientoDAO {
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public EntrenamientoDAO(Context context) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // 📌 Insertar un nuevo entrenamiento
    public long insertarEntrenamiento(String actividad, String fechaHora, double distancia, int tiempo, int valoracion, String comentarios) {
        ContentValues values = new ContentValues();
        values.put("actividad", actividad);
        values.put("fechaHora", fechaHora);
        values.put("distancia", distancia);
        values.put("tiempo", tiempo);
        values.put("valoracion", valoracion);
        values.put("comentarios", comentarios);

        return db.insert("entrenamientos", null, values);
    }

    // 📌 Insertar kilómetros asociados a un entrenamiento
    public void insertarKilometro(long idEntrenamiento, int kilometro, int tiempoKm, double velocidad) {
        ContentValues values = new ContentValues();
        values.put("idEntrenamiento", idEntrenamiento);
        values.put("kilometro", kilometro);
        values.put("tiempoKm", tiempoKm);
        values.put("velocidad", velocidad);

        db.insert("kilometros", null, values);
    }

    // 📌 Obtener todos los entrenamientos
    public List<String> obtenerEntrenamientos() {
        List<String> entrenamientos = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM entrenamientos", null);

        if (cursor.moveToFirst()) {
            do {
                String entrenamiento = cursor.getString(1) + " - " + cursor.getString(2); // actividad y fecha
                entrenamientos.add(entrenamiento);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return entrenamientos;
    }

    // 📌 Cerrar la base de datos
    public void cerrar() {
        dbHelper.close();
    }
}
