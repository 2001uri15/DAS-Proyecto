package com.asierla.das_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TRAININGS_TABLE = "CREATE TABLE entrenamientos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "actividad INTEGER, " +
                "nombre TEXT, " +
                "fechaHora DATETIME, " +
                "distancia REAL, " +
                "tiempo INTEGER, " +
                "valoracion INTEGER, " +
                "comentarios TEXT)";

        String CREATE_KILOMETERS_TABLE = "CREATE TABLE kilometros (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idEntrenamiento INTEGER, " +
                "kilometro INTEGER, " +
                "tiempoKm INTEGER, " +
                "velocidad REAL, " +
                "FOREIGN KEY(idEntrenamiento) REFERENCES entrenamientos(id))";

        db.execSQL(CREATE_TRAININGS_TABLE);
        db.execSQL(CREATE_KILOMETERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS kilometros");
        db.execSQL("DROP TABLE IF EXISTS entrenamientos");
        onCreate(db);
    }

    public void guardarEntrenamiento(String actividad, String fechaHora, float distancia, long tiempo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("actividad", actividad);
        values.put("fechaHora", fechaHora);
        values.put("distancia", distancia);
        values.put("tiempo", tiempo);

        db.insert("entrenamientos", null, values);
        db.close();
    }

    public List<Entrenamiento> obtenerTodosLosEntrenamientos() {
        List<Entrenamiento> entrenamientos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM entrenamientos ORDER BY fechaHora DESC", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String actividad = cursor.getString(1);
                String fecha = cursor.getString(2);
                String distancia = cursor.getString(3);
                String tiempo = cursor.getString(4);

                int icono = obtenerIconoActividad(actividad); // Método para obtener icono
                int nombreActividadId = obtenerNombreActividadId(actividad); // Método para obtener ID de string

                entrenamientos.add(new Entrenamiento(id, icono, nombreActividadId, tiempo, distancia, fecha));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entrenamientos;
    }

    private int obtenerIconoActividad(String actividad) {
        switch (actividad.toLowerCase()) {
            case "correr": return R.drawable.icon_correr;
            case "bici": return R.drawable.icon_bicicleta;
            case "andar": return R.drawable.icon_andar;
            case "remo": return R.drawable.icon_remo;
            default: return R.drawable.circle_outline; // Icono por defecto si no coincide
        }
    }

    private int obtenerNombreActividadId(String actividad) {
        switch (actividad.toLowerCase()) {
            case "correr": return R.string.correr;
            case "bici": return R.string.bici;
            case "andar": return R.string.andar;
            case "remo": return R.string.remo;
            default: return R.string.actividad_desconocida;
        }
    }

    public void eliminarEntrenamiento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("entrenamientos", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void borrarTodosLosDatosDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("entrenamientos", null, null);
        db.delete("kilometros", null, null);
        db.close();
    }
}
