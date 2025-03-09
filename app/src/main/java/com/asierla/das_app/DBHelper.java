package com.asierla.das_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de entrenamientos con la nueva estructura
        String CREATE_TRAININGS_TABLE = "CREATE TABLE entrenamientos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idActividad INTEGER, " +
                "fechaHora DATETIME, " +
                "tiempo INTEGER, " +  // tiempo como INTEGER para representar la duración en milisegundos
                "distancia DOUBLE, " +
                "velocidad DOUBLE, " +  // Usado para la velocidad/paladas
                "valoracion INTEGER, " +
                "comentarios TEXT)";

        String CREATE_INTERVAL_TABLE = "CREATE TABLE inter_entrena (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "idEntrena INTEGER, " +
                "orden INTEGER, " +
                "tiempo INTEGER, " +  // tiempo como INTEGER para representar la duración en milisegundos
                "distancia DOUBLE, " +
                "velocidad DOUBLE," +
                "FOREIGN KEY(idEntrena) REFERENCES entrenamientos(id))";

        db.execSQL(CREATE_TRAININGS_TABLE);
        db.execSQL(CREATE_INTERVAL_TABLE);

        Log.d("DB_CREATION", "Tablas creadas correctamente");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar la tabla si ya existe para actualizar la estructura
        db.execSQL("DROP TABLE IF EXISTS entrenamientos");
        db.execSQL("DROP TABLE IF EXISTS inter_entrena");
        onCreate(db);
    }

    // Función para guardar un entrenamiento con parámetros simples
    public long guardarEntrenamientosSimplesErgo(int idActividad, String fechaHora, double distancia, long tiempo, double velocidad, int valoracion, String comentarios) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idActividad", idActividad);  // id de la actividad
        values.put("fechaHora", fechaHora);  // fecha y hora del entrenamiento
        values.put("distancia", distancia);  // distancia recorrida
        values.put("tiempo", tiempo);  // tiempo en milisegundos
        values.put("velocidad", velocidad);  // velocidad o paladas
        values.put("valoracion", valoracion);  // valoración del entrenamiento
        values.put("comentarios", comentarios);  // comentarios opcionales

        // Insertar los datos en la base de datos
        long result = db.insert("entrenamientos", null, values);

        if (result == -1) {
            Log.e("DB_ERROR", "Error al insertar el entrenamiento");
        } else {
            Log.d("DB_SUCCESS", "Entrenamiento insertado con ID: " + result);
        }

        db.close();

        return result;
    }

    public long guardarIntervalo(long idEntrena, int orden, long tiempo, double distancia, double velocidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idEntrena", idEntrena);
        values.put("orden", orden);
        values.put("tiempo", (int)tiempo);
        values.put("distancia", distancia);
        values.put("velocidad", velocidad);

        long result = db.insert("inter_entrena", null, values);
        db.close();
        return result;
    }

    public ArrayList<EntrenamientoInterval> obtenerIntervalos(int idEntrena) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        ArrayList<EntrenamientoInterval> entrenamientos = new ArrayList<>();

        try {
            // Corrected query to filter by idEntrena
            cursor = db.rawQuery(
                    "SELECT id, idEntrena, orden, tiempo, distancia, velocidad FROM inter_entrena WHERE idEntrena = ? ORDER BY orden ASC",
                    new String[]{String.valueOf(idEntrena)}
            );

            // Loop through all rows in the cursor
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                int idEntrena2 = cursor.getInt(1);
                int orden = cursor.getInt(2);
                long tiempo = cursor.getLong(3);
                double distancia = cursor.getDouble(4);
                double velocidad = cursor.getDouble(5);

                // Add each interval to the list
                entrenamientos.add(new EntrenamientoInterval(id, idEntrena2, orden, tiempo, distancia, velocidad));
            }
        } finally {
            cursor.close();
            db.close();
        }
        return entrenamientos;
    }

    public void guardarEntrenamientoAuto(int tipoEntrenamiento,String fecha, double distancia, long tiempoSegundos){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idActividad", tipoEntrenamiento);  // id de la actividad
        values.put("fechaHora", fecha);  // fecha y hora del entrenamiento
        values.put("distancia", distancia);  // distancia recorrida
        values.put("tiempo", tiempoSegundos);  // tiempo en milisegundos
        values.put("velocidad", 0);  // velocidad o paladas
        values.put("valoracion", 0);  // valoración del entrenamiento
        values.put("comentarios", "");  // comentarios opcionales

        // Insertar los datos en la base de datos
        long result = db.insert("entrenamientos", null, values);

        if (result == -1) {
            Log.e("DB_ERROR", "Error al insertar el entrenamiento");
        } else {
            Log.d("DB_SUCCESS", "Entrenamiento insertado con ID: " + result);
        }

        db.close();
    }

    public Entrenamiento obtenerEntrenaById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, idActividad, tiempo, distancia, fechaHora, valoracion, velocidad FROM entrenamientos WHERE id = ?", new String[]{String.valueOf(id)});

        Entrenamiento entrenamiento = null;

        if (cursor.moveToFirst()) {
            int id2 = cursor.getInt(0);
            int idActividad = cursor.getInt(1);
            long tiempo = cursor.getLong(2);
            double distancia = cursor.getDouble(3);
            String fecha = cursor.getString(4);
            int valoracion = cursor.getInt(5);
            double velocidad = cursor.getDouble(6);

            int icono = obtenerIconoActividad(idActividad);
            int nombreActividadId = obtenerNombreActividad(idActividad);
            String tiempoFormateado = formatearTiempo(tiempo);

            entrenamiento = new Entrenamiento(id2, idActividad, nombreActividadId, icono, tiempoFormateado, distancia, fecha, velocidad, valoracion, "");
        }

        cursor.close();
        db.close();
        return entrenamiento;
    }

    // Función para eliminar un entrenamiento por su ID
    public void eliminarEntrenamiento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("entrenamientos", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Función para borrar todos los datos de la base de datos (puedes omitir "kilometros" si no la necesitas)
    public void borrarTodosLosDatosDB() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("entrenamientos", null, null);
        db.delete("inter_entrena", null, null);
        db.close();
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
        tiempo = tiempo/1000;
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