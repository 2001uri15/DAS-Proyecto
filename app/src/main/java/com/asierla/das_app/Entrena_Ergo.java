package com.asierla.das_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.asierla.das_app.database.DBHelper;

import java.util.Calendar;

public class Entrena_Ergo extends AppCompatActivity {

    private Spinner spinnerTipoEntrenamiento;
    private LinearLayout layoutTabla;
    private TableLayout tableIntervalos;
    private Button btnAddRow;
    private EditText inputComentarios, inputPaladas, inputTiempo, inputDistancia, inputDate;
    private RatingBar ipValoracion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrena_ergo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Que solo pueda estar en forma vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inicializar vistas
        spinnerTipoEntrenamiento = findViewById(R.id.spinnerTipoEntrenamiento);
        layoutTabla = findViewById(R.id.layoutTabla);
        tableIntervalos = findViewById(R.id.tableIntervalos);
        btnAddRow = findViewById(R.id.btnAddRow);
        inputComentarios = findViewById(R.id.inputComentarios);
        inputPaladas = findViewById(R.id.inputPaladas);
        inputTiempo = findViewById(R.id.inputTiempo);
        inputDistancia = findViewById(R.id.inputDistancia);
        inputDate = findViewById(R.id.inputDate);
        ipValoracion = findViewById(R.id.ipValoracion);

        // Escuchar cambios en el Spinner
        spinnerTipoEntrenamiento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Intervalos de Distancia") || selectedItem.equals("Intervalos de Tiempo")) {
                    layoutTabla.setVisibility(View.VISIBLE); // Mostrar la tabla
                } else {
                    layoutTabla.setVisibility(View.GONE); // Ocultar la tabla
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        btnAddRow.setOnClickListener(v -> {
            TableRow newRow = new TableRow(this);
            newRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

            // Campo de orden
            TextView textViewOrden = new TextView(this);
            textViewOrden.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    0.5f
            ));
            textViewOrden.setGravity(Gravity.CENTER);

            // Asignar el número de orden empezando desde 0
            int orden = tableIntervalos.getChildCount(); // Empieza en 0
            textViewOrden.setText(String.valueOf(orden));

            // Campo de distancia
            EditText editTextDistancia = new EditText(this);
            editTextDistancia.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1
            ));
            editTextDistancia.setHint("Distancia");
            editTextDistancia.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

            // Campo de tiempo
            EditText editTextTiempo = new EditText(this);
            editTextTiempo.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1
            ));
            editTextTiempo.setHint("MM:SS,XX");
            editTextTiempo.setInputType(android.text.InputType.TYPE_CLASS_TEXT); // Permitir texto para usar : y ,

            // Campo de paladas
            EditText editTextPaladas = new EditText(this);
            editTextPaladas.setLayoutParams(new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1
            ));
            editTextPaladas.setHint("Paladas");
            editTextPaladas.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

            // Añadir los campos a la fila
            newRow.addView(textViewOrden);
            newRow.addView(editTextDistancia);
            newRow.addView(editTextTiempo);
            newRow.addView(editTextPaladas);

            // Añadir la fila a la tabla
            tableIntervalos.addView(newRow);
        });

        // Para que se muestre el calendario al hacer clic en el EditText de fecha
        inputDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Formatear la fecha seleccionada en formato yyyy-MM-dd
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        inputDate.setText(formattedDate); // Mostrar la fecha en el EditText
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        inputTiempo.setOnClickListener(v -> {
            // Obtener la hora, minuto y segundo actuales
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            // Crear el LinearLayout con orientación horizontal
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(20, 20, 20, 20);

            // Crear el layout para horas
            LinearLayout layoutHoras = new LinearLayout(this);
            layoutHoras.setOrientation(LinearLayout.VERTICAL);
            TextView labelHoras = new TextView(this);
            labelHoras.setText("Horas");
            labelHoras.setGravity(Gravity.CENTER);
            NumberPicker hourPicker = new NumberPicker(this);
            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(23); // 24 horas
            hourPicker.setValue(hour);
            layoutHoras.addView(labelHoras);
            layoutHoras.addView(hourPicker);

            // Crear el layout para minutos
            LinearLayout layoutMinutos = new LinearLayout(this);
            layoutMinutos.setOrientation(LinearLayout.VERTICAL);
            TextView labelMinutos = new TextView(this);
            labelMinutos.setText("Minuto");
            labelMinutos.setGravity(Gravity.CENTER);
            NumberPicker minutePicker = new NumberPicker(this);
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(59); // 60 minutos
            minutePicker.setValue(minute);
            layoutMinutos.addView(labelMinutos);
            layoutMinutos.addView(minutePicker);

            // Crear el layout para segundos
            LinearLayout layoutSegundos = new LinearLayout(this);
            layoutSegundos.setOrientation(LinearLayout.VERTICAL);
            TextView labelSegundos = new TextView(this);
            labelSegundos.setText("Segundo");
            labelSegundos.setGravity(Gravity.CENTER);
            NumberPicker secondPicker = new NumberPicker(this);
            secondPicker.setMinValue(0);
            secondPicker.setMaxValue(59); // 60 segundos
            secondPicker.setValue(second);
            layoutSegundos.addView(labelSegundos);
            layoutSegundos.addView(secondPicker);

            // Añadir los tres layouts al layout principal
            layout.addView(layoutHoras);
            layout.addView(layoutMinutos);
            layout.addView(layoutSegundos);

            // Mostrar el layout en un AlertDialog
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Selecciona Tiempo")
                    .setView(layout)
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        // Formatear el tiempo en hh:mm:ss
                        String formattedTime = String.format("%02d:%02d:%02d", hourPicker.getValue(), minutePicker.getValue(), secondPicker.getValue());
                        inputTiempo.setText(formattedTime); // Mostrar el tiempo en el EditText
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Botón para guardar los entrenamientos
        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> {
            String selectedItem = spinnerTipoEntrenamiento.getSelectedItem().toString();
            if (selectedItem.equals("Solo Remar") || selectedItem.equals("Distancia Simple") || selectedItem.equals("Tiempo Simple")) {
                Toast.makeText(this, "Guardando entrenamiento simple", Toast.LENGTH_SHORT).show();

                String tiempoStr = inputTiempo.getText().toString().trim();

                if (!tiempoStr.matches("\\d{1,2}:\\d{1,2}:\\d{1,2}")) {
                    Toast.makeText(this, R.string.formato_incorrecto_tiempo, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convertir hh:mm:ss a milisegundos
                String[] partes = tiempoStr.split(":");
                int horas = Integer.parseInt(partes[0]);
                int minutos = Integer.parseInt(partes[1]);
                int segundos = Integer.parseInt(partes[2]);
                int valoracion = (int) ipValoracion.getRating();

                long tiempoEnMilisegundos = (horas * 3600 + minutos * 60 + segundos) * 1000L;

                // Insertar en la base de datos
                DBHelper db = new DBHelper(this);
                long id = db.guardarEntrenamientosSimplesErgo(
                        4,  // ID de la actividad (remar)
                        inputDate.getText().toString(),
                        Double.parseDouble(inputDistancia.getText().toString().trim()),
                        tiempoEnMilisegundos,  // tiempo en milisegundos
                        Double.parseDouble(inputPaladas.getText().toString().trim()),
                        valoracion,
                        inputComentarios.getText().toString()
                );

                if (id != -1) {
                    Intent intent = new Intent(Entrena_Ergo.this, VerEntrenamiento.class);
                    intent.putExtra("idEntrena", (int) id);  // Pasar el id del entrenamiento
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show();
                }

            } else if (selectedItem.equals("Intervalos de Distancia") || selectedItem.equals("Intervalos de Tiempo")) {

                // Convertir hh:mm:ss a milisegundos
                String tiempoStr = inputTiempo.getText().toString().trim();
                String[] partes = tiempoStr.split(":");
                int horas = Integer.parseInt(partes[0]);
                int minutos = Integer.parseInt(partes[1]);
                int segundos = Integer.parseInt(partes[2]);
                int valoracion = (int) ipValoracion.getRating();
                long tiempoEnMilisegundos = (horas * 3600 + minutos * 60 + segundos) * 1000L;

                // Insertar en la base de datos
                DBHelper db = new DBHelper(this);
                long idEntrena = db.guardarEntrenamientosSimplesErgo(
                        4,  // ID de la actividad (remar)
                        inputDate.getText().toString(),
                        Double.parseDouble(inputDistancia.getText().toString().trim()),
                        tiempoEnMilisegundos,  // tiempo en milisegundos
                        Double.parseDouble(inputPaladas.getText().toString().trim()),
                        valoracion,
                        inputComentarios.getText().toString()
                );

                for (int i = 1; i < tableIntervalos.getChildCount(); i++) {
                    TableRow row = (TableRow) tableIntervalos.getChildAt(i);
                    TextView textViewOrden = (TextView) row.getChildAt(0);
                    EditText editTextDistancia = (EditText) row.getChildAt(1);
                    EditText editTextTiempo = (EditText) row.getChildAt(2);
                    EditText editTextPaladas = (EditText) row.getChildAt(3);

                    try {
                        int orden = Integer.parseInt(textViewOrden.getText().toString());
                        double distancia = Double.parseDouble(editTextDistancia.getText().toString().trim());

                        String tiempoStr2 = editTextTiempo.getText().toString().trim();
                        if (!tiempoStr2.matches("\\d{1,2}:\\d{1,2},\\d{1,2}")) {
                            Toast.makeText(this, R.string.formato_incorrecto_tiempo, Toast.LENGTH_SHORT).show();
                            return; // Detener el proceso si hay un error
                        }

                        int tiempo = (int) convertirTiempoAMilisegundos(tiempoStr2);  // Usar tiempoStr2 en lugar de tiempoStr
                        double paladas = Double.parseDouble(editTextPaladas.getText().toString().trim());


                        long resil = db.guardarIntervalo(idEntrena, orden, tiempo, distancia, paladas);
                        if (resil == -1) {
                            Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show();
                            return;  // Detener el proceso si hay un error
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                /*
                 * Visualizamos el entrenamiento
                 */
                Intent intent = new Intent(Entrena_Ergo.this, VerEntrenamiento.class);
                intent.putExtra("idEntrena", idEntrena);  // Pasar el id del entrenamiento
                startActivity(intent);
                finish();
            }
        });
    }

    private long convertirTiempoAMilisegundos(String tiempo) {
        String[] partes = tiempo.split(":");
        int minutos = Integer.parseInt(partes[0]);
        String[] segundosMilisegundos = partes[1].split(",");
        int segundos = Integer.parseInt(segundosMilisegundos[0]);
        int milisegundos = Integer.parseInt(segundosMilisegundos[1]) * 10; // Convertir centésimas a milisegundos

        return (minutos * 60 + segundos) * 1000L + milisegundos;
    }
}