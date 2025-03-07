package com.asierla.das_app;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class VerEntrenamiento extends AppCompatActivity {
    private TextView ipVelociad, ipEntrena, ipFecha, ipDuracion, ipDistancia, ipComentarios;
    private ImageView ipImagenEntre, btnBorrar;
    private RatingBar ipValoracion;


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

        int idEntreno = getIntent().getIntExtra("idEntrena", 0);
        DBHelper db = new DBHelper(this);
        Entrenamiento entrena = db.obtenerEntrenaById(idEntreno);

        // Poner los datos en los labels
        ipEntrena.setText(entrena.getNombreActividadId());
        ipFecha.setText(entrena.getFecha());
        ipDuracion.setText(entrena.getTiempo());
        ipImagenEntre.setImageResource(entrena.getIcono());
        ipComentarios.setText(entrena.getComentarios());
        ipValoracion.setRating((float) entrena.getValoracion());

        if(entrena.getIdEntrenamiento()>=0 && entrena.getIdEntrenamiento()<=2){
            // Carrera, Bici, Andar
            ipVelociad.setText(String.valueOf(entrena.getVelocidad()) + " km/h");
            ipDistancia.setText(String.valueOf((entrena.getDistancia()/1000)) + " km");
        }else if(entrena.getIdEntrenamiento()>2 && entrena.getIdEntrenamiento()<5){
            // Remo y Ergometro
            ipVelociad.setText(String.valueOf((int)entrena.getVelocidad()) + " ppm");
            ipDistancia.setText(String.valueOf((int)entrena.getDistancia()) + " m");
        }else{
            // Cualquier otro caso
            ipVelociad.setText(String.valueOf(entrena.getVelocidad()));
            ipDistancia.setText(String.valueOf(entrena.getDistancia()));
        }


        btnBorrar.setOnClickListener(v -> borrarEntrena(entrena.getId()));
    }

    public void borrarEntrena(int id){
        DBHelper db = new DBHelper(this);
        db.eliminarEntrenamiento(id);
        finish();
    }
}