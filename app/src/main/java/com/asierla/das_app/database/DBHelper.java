package com.asierla.das_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.asierla.das_app.model.PesasDia;
import com.asierla.das_app.model.Entrenamiento;
import com.asierla.das_app.model.EntrenamientoInterval;
import com.asierla.das_app.R;
import com.asierla.das_app.model.PesasEjercicio;
import com.asierla.das_app.model.MejorPesas;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

        String CREATE_RUTA_TABLE = "CREATE TABLE IF NOT EXISTS Ruta (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "entrenamiento_id INTEGER, " +
                "latitud REAL, " +
                "longitud REAL, " +
                "FOREIGN KEY(entrenamiento_id) REFERENCES entrenamientos(id));";

        String CREATE_TIPO_PESAS = "CREATE TABLE IF NOT EXISTS TipoPesas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre Text, " +
                "tipo int);";

        String CREATE_EJERCICIO = "CREATE TABLE IF NOT EXISTS Ejercicio (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fecha DATE, " +
                "idEjercicio int, " +
                "comentario TEXT, " +
                "FOREIGN KEY(idEjercicio) REFERENCES TipoPesas(id));";

        String CREATE_REPETICIONES = "CREATE TABLE IF NOT EXISTS Repeticiones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "orden int, " +
                "idEjercicio int, " +
                "peso double, " +
                "repeticion int, " +
                "FOREIGN KEY(idEjercicio) REFERENCES Ejercicio(id));";

        db.execSQL(CREATE_TRAININGS_TABLE);
        db.execSQL(CREATE_INTERVAL_TABLE);
        db.execSQL(CREATE_RUTA_TABLE);
        db.execSQL(CREATE_EJERCICIO);
        db.execSQL(CREATE_TIPO_PESAS);
        db.execSQL(CREATE_REPETICIONES);

        // Insertar datos iniciales en la tabla TipoPesas
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Remo con barra', 1);"); // Espalda
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Dominadas', 1);"); // Espalda
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Jalón al pecho', 1);"); // Espalda

        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Press de banca', 2);"); // Pecho
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Press inclinado con mancuernas', 2);"); // Pecho
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Fondos en paralelas', 2);"); // Pecho

        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Press militar', 3);"); // Hombro
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Elevaciones laterales', 3);"); // Hombro
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Face pull', 3);"); // Hombro

        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Sentadillas', 4);"); // Pierna
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Peso muerto', 4);"); // Pierna
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Zancadas', 4);"); // Pierna

        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Curl de bíceps con barra', 5);"); // Bíceps
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Curl de bíceps con mancuernas', 5);"); // Bíceps
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Fondos en banco', 6);"); // Tríceps
        db.execSQL("INSERT INTO TipoPesas (nombre, tipo) VALUES ('Press francés', 6);"); // Tríceps

        insertarDatosSinteticos(db);

        Log.d("DB_CREATION", "Tablas creadas correctamente");
    }

    private void insertarDatosSinteticos(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar la tabla si ya existe para actualizar la estructura
        db.execSQL("DROP TABLE IF EXISTS entrenamientos");
        db.execSQL("DROP TABLE IF EXISTS inter_entrena");
        db.execSQL("DROP TABLE IF EXISTS Ruta");
        db.execSQL("DROP TABLE IF EXISTS Ejercicio");
        db.execSQL("DROP TABLE IF EXISTS Repeticiones");
        onCreate(db);
    }

    // Función para guardar un entrenamiento con parámetros simples
    public long guardarEntrenamientosSimplesErgo(int idActividad, String fechaHora, double distancia,
                                                 long tiempo, double velocidad, int valoracion,
                                                 String comentarios) {
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

    public long guardarIntervalo(long idEntrena, int orden, long tiempo, double distancia,
                                 double velocidad) {
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

    public long guardarPuntoRuta(long entrenamientoId, double latitud, double longitud) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("entrenamiento_id", entrenamientoId);
        values.put("latitud", latitud);
        values.put("longitud", longitud);
        return db.insert("Ruta", null, values);
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

    public long guardarEntrenamientoAuto(int tipoEntrenamiento,String fecha, double distancia,
                                         long tiempoSegundos, float velocidad){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idActividad", tipoEntrenamiento);  // id de la actividad
        values.put("fechaHora", fecha);  // fecha y hora del entrenamiento
        values.put("distancia", distancia);  // distancia recorrida
        values.put("tiempo", tiempoSegundos);  // tiempo en milisegundos
        values.put("velocidad", velocidad);  // velocidad o paladas
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
        return result;
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
        db.delete("Ruta", null, null);
        db.delete("TipoPesas", null, null);
        db.delete("Ejercicio", null, null);
        db.delete("Repeticiones", null, null);
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


    public ArrayList<LatLng> obtenerRutaPorEntrenamientoId(int entrenamientoId) {
        ArrayList<LatLng> puntos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT latitud, longitud FROM Ruta WHERE entrenamiento_id = ?", new String[]{String.valueOf(entrenamientoId)});

        if (cursor.moveToFirst()) {
            do {
                double latitud = cursor.getDouble(0);
                double longitud = cursor.getDouble(1);
                puntos.add(new LatLng(latitud, longitud));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return puntos;
    }

    public void actualizarEntrena(int id, int valoracion, String string) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Crear un objeto ContentValues para almacenar los nuevos valores
        ContentValues values = new ContentValues();
        values.put("valoracion", valoracion); // Actualizar la valoración
        values.put("comentarios", string); // Actualizar los comentarios

        // Ejecutar la actualización en la base de datos
        db.update(
                "entrenamientos", // Nombre de la tabla
                values, // Valores a actualizar
                "id = ?", // Condición WHERE
                new String[]{String.valueOf(id)} // Argumentos para la condición WHERE
        );

        db.close(); // Cerrar la conexión a la base de datos
    }

    public ArrayList<MejorPesas> obtenerMejoresResulatdos() {
        ArrayList<MejorPesas> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta actualizada sin EjercicioGym
        String query = "SELECT " +
                "TP.id AS idTipoPesa, " +
                "TP.nombre, " +
                "MAX(R.peso) AS max_peso " +
                "FROM " +
                "TipoPesas TP " +
                "JOIN " +
                "Ejercicio E ON TP.id = E.idEjercicio " +
                "JOIN " +
                "Repeticiones R ON E.id = R.idEjercicio " +
                "GROUP BY " +
                "TP.id, TP.nombre;";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int idTipoPesa = cursor.getInt(0);
                String nombre = cursor.getString(1);
                double maxPeso = cursor.getDouble(2);
                list.add(new MejorPesas(idTipoPesa, idTipoPesa, nombre, maxPeso));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return list;
    }

    public String[] obtTiposEntrena() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta para obtener los tipos de entrenamiento
        String query = "SELECT id, nombre FROM TipoPesas;";

        Cursor cursor = db.rawQuery(query, null);

        // Crear una lista dinámica para almacenar los resultados
        ArrayList<String> list = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String itm = String.valueOf(id) + " - " + nombre;
                list.add(itm);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Convertir la lista a un array de Strings
        String[] result = new String[list.size()];
        result = list.toArray(result);

        return result; // Devolver el array de Strings
    }

    public long guardarEntrenoPesas(String fecha, int idTipoPesa, String comentarios) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Crear un objeto ContentValues para almacenar los valores
        ContentValues values = new ContentValues();
        values.put("fecha", fecha); // Columna "fecha"
        values.put("idEjercicio", idTipoPesa); // Columna "id_tipo_pesa"
        values.put("comentario", comentarios); // Columna "comentarios"

        // Insertar en la tabla "Entrenamientos"
        long result = db.insert("Ejercicio", null, values);

        // Verificar si la inserción fue exitosa
        if (result == -1) {
            // Error al insertar
            Log.e("DBHelper", "Error al guardar el entrenamiento");
        } else {
            // Inserción exitosa
            Log.d("DBHelper", "Entrenamiento guardado correctamente");
        }

        db.close();
        return result;
    }

    public void guardarRepeticionPesas(int idPesa, int orden, int rep, double peso){
        SQLiteDatabase db = this.getWritableDatabase();

        // Crear un objeto ContentValues para almacenar los valores
        ContentValues values = new ContentValues();
        values.put("idEjercicio", idPesa); // Columna "fecha"
        values.put("orden", orden); // Columna "id_tipo_pesa"
        values.put("repeticion", rep); // Columna "comentarios"
        values.put("peso", peso); // Columna "comentarios"

        // Insertar en la tabla "Entrenamientos"
        long result = db.insert("Repeticiones", null, values);

        // Verificar si la inserción fue exitosa
        if (result == -1) {
            // Error al insertar
            Log.e("DBHelper", "Error al guardar el entrenamiento");
        } else {
            // Inserción exitosa
            Log.d("DBHelper", "Entrenamiento guardado correctamente");
        }

        db.close();
    }

    public ArrayList<PesasDia> obtenerTodosLosEntrenamientosDePesas() {
        ArrayList<PesasDia> entrenamientos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta para obtener todos los entrenamientos de pesas
        String query = "SELECT DISTINCT fecha " +
                "FROM Ejercicio " +
                "ORDER BY fecha DESC;";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String fecha = cursor.getString(0);
                ArrayList<PesasEjercicio> ejercicio = obtenerRepeticionesPorEjercicio(fecha);

                // Crear un objeto Day con los datos obtenidos
                PesasDia pesasDia = new PesasDia(fecha, ejercicio);
                entrenamientos.add(pesasDia);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return entrenamientos;
    }

    public ArrayList<PesasEjercicio> obtenerRepeticionesPorEjercicio(String fecha) {
        ArrayList<PesasEjercicio> repeticiones = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Consulta para obtener la información requerida
        String query = "SELECT TP.nombre, E.id, COUNT(R.repeticion) AS totalRepeticiones, MAX(R.peso) AS pesoMaximo " +
                "FROM Repeticiones R " +
                "JOIN Ejercicio E ON R.idEjercicio = E.id " +
                "JOIN TipoPesas TP ON E.idEjercicio = TP.id " +
                "WHERE E.fecha = ? " +
                "GROUP BY TP.nombre, E.id " +
                "ORDER BY TP.nombre ASC;";

        Cursor cursor = db.rawQuery(query, new String[]{fecha});

        if (cursor.moveToFirst()) {
            do {
                String nombreActividad = cursor.getString(0);
                int idEjercicio = cursor.getInt(1);
                int totalRepeticiones = cursor.getInt(2);
                double pesoMaximo = cursor.getDouble(3);

                // Crear un objeto Exercise con los datos obtenidos
                PesasEjercicio pesasEjercicio = new PesasEjercicio(nombreActividad, idEjercicio, totalRepeticiones, pesoMaximo);
                repeticiones.add(pesasEjercicio);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return repeticiones;
    }

    public long obtenerTiempoTotalSemanaActual() {
        SQLiteDatabase db = this.getReadableDatabase();
        long tiempoTotal = 0;

        // Obtener el primer y último día de la semana actual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        String startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        String endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        // Consulta para sumar el tiempo de todos los entrenamientos de la semana actual
        String query = "SELECT SUM(tiempo) FROM entrenamientos " +
                "WHERE date(fechaHora) BETWEEN date(?) AND date(?)";

        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            tiempoTotal = cursor.getLong(0);
        }

        cursor.close();
        db.close();

        return tiempoTotal+1823412;
    }
}