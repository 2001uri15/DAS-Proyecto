package com.asierla.das_app;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class VerEntrenamiento extends AppCompatActivity {
    private TextView ipVelociad, ipEntrena, ipFecha, ipDuracion, ipDistancia, ipComentarios, ipRitmo;
    private ImageView ipImagenEntre, btnBorrar;
    private RatingBar ipValoracion;
    private TableLayout tableIntervalos;
    private RelativeLayout relaVueltas;


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
        setContentView(R.layout.activity_ver_entrenamiento);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Colores de las imagenes
        ImageView imageView5 = findViewById(R.id.imageView5);
        int color = ContextCompat.getColor(this, R.color.duracion); // Obtén el color correctamente
        imageView5.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        ImageView imageView6 = findViewById(R.id.imageView6);
        color = ContextCompat.getColor(this, R.color.distancia); // Obtén el color correctamente
        imageView6.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        ImageView imageView7 = findViewById(R.id.imageView7);
        color = ContextCompat.getColor(this, R.color.velocidad); // Obtén el color correctamente
        imageView7.setColorFilter(color, PorterDuff.Mode.SRC_IN);

        ImageView imageView8 = findViewById(R.id.imageView8);
        color = ContextCompat.getColor(this, R.color.velocidad); // Obtén el color correctamente
        imageView8.setColorFilter(color, PorterDuff.Mode.SRC_IN);


        // Obtener la información del entrenamiento
        ipDistancia = findViewById(R.id.ipDistancia);
        ipEntrena = findViewById(R.id.ipEntrena);
        ipFecha = findViewById(R.id.ipFecha);
        ipDuracion = findViewById(R.id.ipDuracion);
        ipVelociad = findViewById(R.id.ipVelocidad);
        ipImagenEntre = findViewById(R.id.ipImagenEntre);
        ipComentarios = findViewById(R.id.ipComentarios);
        btnBorrar = findViewById(R.id.btnBorrar);
        ipValoracion = findViewById(R.id.textValo);
        ipRitmo = findViewById(R.id.ipRitmo);
        tableIntervalos = findViewById(R.id.tableIntervalos);
        relaVueltas = findViewById(R.id.relaVueltas);

        int idEntreno = getIntent().getIntExtra("idEntrena", 0);
        DBHelper db = new DBHelper(this);
        Entrenamiento entrena = db.obtenerEntrenaById(idEntreno);

        // Poner los datos en los labels
        ArrayList<EntrenamientoInterval> intervalos = db.obtenerIntervalos(idEntreno);
        ipEntrena.setText(entrena.getNombreActividadId());
        ipFecha.setText(entrena.getFecha());
        ipDuracion.setText(entrena.getTiempo());
        ipImagenEntre.setImageResource(entrena.getIcono());
        ipComentarios.setText(entrena.getComentarios());
        ipValoracion.setRating((float) entrena.getValoracion());

        if(entrena.getIdEntrenamiento()>=0 && entrena.getIdEntrenamiento()<=2){
            // Carrera, Bici, Andar
            ipVelociad.setText(String.valueOf(entrena.getVelocidad()) + " km/h");
            ipDistancia.setText(String.format("%.2f km", entrena.getDistancia() / 1000.0));
        }else if(entrena.getIdEntrenamiento()>2 && entrena.getIdEntrenamiento()<5){
            // Remo y Ergometro
            ipVelociad.setText(String.valueOf((int)entrena.getVelocidad()) + " ppm");
            ipDistancia.setText(String.valueOf((int)entrena.getDistancia()) + " m");
            ipRitmo.setText(String.valueOf(convertirATiempo(500*(convertirAMilisegundos(entrena.getTiempo())/entrena.getDistancia()))));
        }else{
            // Cualquier otro caso
            ipVelociad.setText(String.valueOf(entrena.getVelocidad()));
            ipDistancia.setText(String.valueOf(entrena.getDistancia()));
        }

        // Cargar intervalos
        if(intervalos.size()!=0){
            for (EntrenamientoInterval intervalo : intervalos) {
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));

                TextView tvOrden = new TextView(this);
                tvOrden.setText(String.valueOf(intervalo.getOrden()));
                tvOrden.setGravity(Gravity.CENTER);
                row.addView(tvOrden);

                TextView tvDistancia = new TextView(this);
                tvDistancia.setText(String.format((int)intervalo.getDistancia() + " m"));
                tvDistancia.setGravity(Gravity.CENTER);
                row.addView(tvDistancia);

                TextView tvTiempo = new TextView(this);
                tvTiempo.setText(convertirATiempo(intervalo.getTiempo()));
                tvTiempo.setGravity(Gravity.CENTER);
                row.addView(tvTiempo);

                TextView tvRitmo = new TextView(this);
                tvRitmo.setText(String.valueOf((int)intervalo.getPaladas()));
                tvRitmo.setGravity(Gravity.CENTER);
                row.addView(tvRitmo);

                tableIntervalos.addView(row);
            }
        }else{
            relaVueltas.setVisibility(View.GONE);
        }
        



        btnBorrar.setOnClickListener(v -> borrarEntrena(entrena.getId()));
    }

    public void borrarEntrena(int id){
        DBHelper db = new DBHelper(this);
        db.eliminarEntrenamiento(id);
        finish();
    }

    public int convertirAMilisegundos(String tiempo) {
        if (tiempo == null || tiempo.isEmpty()) {
            throw new IllegalArgumentException("La entrada no puede ser nula o vacía.");
        }

        tiempo = tiempo.trim(); // Eliminar espacios al inicio y final

        // Dividir en minutos y segundos/milisegundos
        String[] partes = tiempo.split(":");


        int minutos;
        int segundos;
        int milisegundos = 0; // Valor por defecto para milisegundos

        try {
            minutos = Integer.parseInt(partes[0]);

            // Verificar si hay milisegundos
            if (partes[1].contains(",")) {
                String[] segundosYMilisegundos = partes[1].split(",");
                if (segundosYMilisegundos.length != 2) {
                    throw new IllegalArgumentException("Formato incorrecto. Debe ser MM:SS,X");
                }
                segundos = Integer.parseInt(segundosYMilisegundos[0]);
                milisegundos = Integer.parseInt(segundosYMilisegundos[1]);
            } else {
                // Si no hay milisegundos, solo parsear los segundos
                segundos = Integer.parseInt(partes[1]);
            }

            // Validar rangos
            if (minutos < 0 || segundos < 0 || segundos >= 60 || milisegundos < 0 || milisegundos >= 1000) {
                throw new IllegalArgumentException("Valores fuera de rango. Minutos: 0-59, Segundos: 0-59, Milisegundos: 0-999");
            }

            // Convertir a milisegundos
            return (minutos * 60 + segundos) * 1000 + milisegundos;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato incorrecto. Los valores deben ser números enteros.");
        }
    }

    public String convertirATiempo(double milisegundos) {
        int minutos = (int) (milisegundos / 60000);
        int segundos = (int) ((milisegundos % 60000) / 1000);
        int decimas = (int) ((milisegundos % 1000) / 100); // Primera cifra decimal

        return String.format("%02d:%02d,%d", minutos, segundos, decimas);
    }
}