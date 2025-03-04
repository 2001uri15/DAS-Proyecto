package com.asierla.das_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class Entrena_Ergo extends AppCompatActivity {

    private Spinner spinnerTipoEntrenamiento;
    private LinearLayout layoutTabla;
    private TableLayout tableIntervalos;
    private Button btnAddRow;
    private EditText comentarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrena_ergo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        spinnerTipoEntrenamiento = findViewById(R.id.spinnerTipoEntrenamiento);
        layoutTabla = findViewById(R.id.layoutTabla);
        tableIntervalos = findViewById(R.id.tableIntervalos);
        btnAddRow = findViewById(R.id.btnAddRow);
        comentarios = findViewById(R.id.inputComentarios);

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

        // Manejar la adición de nuevas filas
        btnAddRow.setOnClickListener(v -> {
            TableRow newRow = new TableRow(this);
            newRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            ));

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
            editTextTiempo.setHint("Tiempo");
            editTextTiempo.setInputType(android.text.InputType.TYPE_CLASS_DATETIME);

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
            newRow.addView(editTextDistancia);
            newRow.addView(editTextTiempo);
            newRow.addView(editTextPaladas);

            // Añadir la fila a la tabla
            tableIntervalos.addView(newRow);
        });

        // Para que se muestre el calendario al hacer clic en el EditText de fecha
        EditText inputDate = findViewById(R.id.inputDate);
        inputDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Formatear la fecha seleccionada
                        String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        inputDate.setText(formattedDate); // Mostrar la fecha en el EditText
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });


        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> {

            String selectedItem = spinnerTipoEntrenamiento.getSelectedItem().toString();
            if(selectedItem=="Solo Remar"){
                Toast.makeText(this, "Se quiere guardar: Solo Remar", Toast.LENGTH_SHORT).show();
            } else if (selectedItem=="Distancia Simple") {
                Toast.makeText(this, "Se quiere guardar: Distancia Simple", Toast.LENGTH_SHORT).show();
            }else if (selectedItem=="Tiempo Simple"){
                Toast.makeText(this, "Se quiere guardar: Tiempo Simple", Toast.LENGTH_SHORT).show();
            }
        });
    }
}