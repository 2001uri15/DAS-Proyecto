package com.asierla.das_app;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.asierla.das_app.database.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AnadirEntrena extends AppCompatActivity {
    private EditText ipFecha;
    private TableLayout tableLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtener idioma guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anadir_entrena);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Coger los items de la vista
        ipFecha = findViewById(R.id.ipFecha);
        Spinner spinner = findViewById(R.id.spinner);
        Button btnGuardar = findViewById(R.id.btnGuardar);
        Button btnAddRow = findViewById(R.id.btnAddRow);
        EditText ipComentarios = findViewById(R.id.ipComentarios);
        tableLayout = findViewById(R.id.tableLayout);

        // Poner la fecha por defecto la de hoy
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        ipFecha.setText(dateFormat.format(calendar.getTime()));



        // Crear un array de strings con los datos
        DBHelper db = new DBHelper(this);
        String[] datos = db.obtTiposEntrena();
        // Usar un ArrayAdapter con el layout predeterminado
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnGuardar.setOnClickListener(v -> {
            // Obtener el valor seleccionado del Spinner
            String valor = spinner.getSelectedItem().toString();

            // Dividir la cadena en partes usando " - " como delimitador
            String[] parts = valor.split(" - "); // Divide la cadena en ["1", "Texto"]

            // Verificar que el formato sea correcto
            if (parts.length > 0) {
                try {
                    // Obtener el número de la primera parte
                    String numberStr = parts[0]; // "1"
                    int idTipoPesa = Integer.parseInt(numberStr); // Convertir a entero

                    // Obtener los demás valores
                    String fecha = ipFecha.getText().toString();
                    String comentarios = ipComentarios.getText().toString();

                    // Guardar el entrenamiento principal en la base de datos
                    long idPesas = db.guardarEntrenoPesas(fecha, idTipoPesa, comentarios);

                    // Recorrer las filas de la tabla y guardar cada repetición
                    for (int i = 1; i < tableLayout.getChildCount(); i++) { // Empezar desde 1 para saltar la fila de encabezados
                        View view = tableLayout.getChildAt(i);
                        if (view instanceof TableRow) {
                            TableRow row = (TableRow) view;

                            // Obtener los valores de cada EditText en la fila
                            EditText editOrden = (EditText) row.getChildAt(0);
                            EditText editRepeticion = (EditText) row.getChildAt(1);
                            EditText editPeso = (EditText) row.getChildAt(2);

                            // Convertir los valores a los tipos correctos
                            int orden = Integer.parseInt(editOrden.getText().toString());
                            int repeticion = Integer.parseInt(editRepeticion.getText().toString());
                            double peso = Double.parseDouble(editPeso.getText().toString());

                            // Guardar la repetición en la base de datos
                            db.guardarRepeticionPesas((int)idPesas, orden, repeticion, peso);
                        }
                    }

                    // Mostrar un mensaje de éxito
                    Toast.makeText(this, "Entrenamiento y repeticiones guardados correctamente", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    // Manejar error si el número no es válido
                    Toast.makeText(this, "Error: Formato de número no válido", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Manejar otros errores
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                // Manejar error si el formato no es correcto
                Toast.makeText(this, "Error: Formato de Spinner no válido", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para el botón "Añadir fila"
        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBlankRow(); // Añadir una fila en blanco
            }
        });



    }

    public void showDatePickerDialog(View v) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    ipFecha.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    // Método para añadir una fila en blanco
    private void addBlankRow() {
        TableRow tableRow = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tableRow.setLayoutParams(layoutParams);

        // Añadir EditText para Orden
        EditText editOrden = new EditText(this);
        editOrden.setHint(R.string.orden);
        editOrden.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        TableRow.LayoutParams paramsOrden = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        );
        editOrden.setLayoutParams(paramsOrden);
        tableRow.addView(editOrden);

        // Añadir EditText para Repetición
        EditText editRepeticion = new EditText(this);
        editRepeticion.setHint(R.string.repeticion);
        editRepeticion.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        TableRow.LayoutParams paramsRepeticion = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        );
        editRepeticion.setLayoutParams(paramsRepeticion);
        tableRow.addView(editRepeticion);

        // Añadir EditText para Peso
        EditText editPeso = new EditText(this);
        editPeso.setHint(R.string.peso);
        editPeso.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        TableRow.LayoutParams paramsPeso = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        );
        editPeso.setLayoutParams(paramsPeso);
        tableRow.addView(editPeso);

        // Añadir la fila a la tabla
        tableLayout.addView(tableRow);
    }

}