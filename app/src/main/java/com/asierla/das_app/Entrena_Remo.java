package com.asierla.das_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Entrena_Remo extends AppCompatActivity {

    private TextView tvPaladas, tvTiempo500m, tvDistancia, tvTiempo;
    private LinearLayout containerModoEspecifico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrena_remo);

        // Configura el padding para los system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener referencias a las vistas
        tvPaladas = findViewById(R.id.tvPaladas);
        tvTiempo500m = findViewById(R.id.tvTiempo500m);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvTiempo = findViewById(R.id.tvTiempo);
        containerModoEspecifico = findViewById(R.id.containerModoEspecifico);

        // Obtener el tipo de entrenamiento desde el Intent
        Intent intent = getIntent();
        String tipoEntrenamiento = intent.getStringExtra("tipoEntrenamiento");
        String distancia = intent.getStringExtra("distancia");
        String tiempo = intent.getStringExtra("tiempo");
        String descanso = intent.getStringExtra("descanso");

        // Configurar la interfaz según el tipo de entrenamiento
        switch (tipoEntrenamiento) {
            case "Solo Remar":
                configurarSoloRemar();
                break;
            case "Distancia Simple":
                configurarDistanciaSimple(distancia);
                break;
            case "Tiempo Simple":
                configurarTiempoSimple(tiempo);
                break;
            case "Intervalos de Distancia":
                configurarIntervalosDistancia(distancia, descanso);
                break;
            case "Intervalos de Tiempo":
                configurarIntervalosTiempo(tiempo, descanso);
                break;
        }
    }



    private void configurarSoloRemar() {
        // Configurar la interfaz para "Solo Remar"
        tvDistancia.setText("Distancia: 0m");
        tvTiempo.setText("Tiempo: 00:00:00");
    }

    private void configurarDistanciaSimple(String distancia) {
        // Configurar la interfaz para "Distancia Simple"
        tvDistancia.setText("Distancia: " + distancia + "m");
        tvTiempo.setText("Tiempo: 00:00:00");
    }

    private void configurarTiempoSimple(String tiempo) {
        // Configurar la interfaz para "Tiempo Simple"
        tvTiempo.setText("Tiempo: " + tiempo);
    }

    private void configurarIntervalosDistancia(String distancia, String descanso) {
        // Configurar la interfaz para "Intervalos de Distancia"
        View intervaloView = LayoutInflater.from(this).inflate(R.layout.layout_intervalo_distancia, containerModoEspecifico, false);
        /*TextView tvDistanciaIntervalo = intervaloView.findViewById(R.id.tvDistanciaIntervalo);
        TextView tvRepeticion = intervaloView.findViewById(R.id.tvRepeticion);
        TextView tvTiempoDescanso = intervaloView.findViewById(R.id.tvTiempoDescanso);

        tvDistanciaIntervalo.setText("Distancia: " + distancia + "m");
        tvRepeticion.setText("Repetición: 1");
        tvTiempoDescanso.setText("Descanso: " + descanso);*/

        containerModoEspecifico.addView(intervaloView);
    }

    private void configurarIntervalosTiempo(String tiempo, String descanso) {
        /* Configurar la interfaz para "Intervalos de Tiempo"
        View intervaloView = LayoutInflater.from(this).inflate(R.layout.layout_intervalo_tiempo, containerModoEspecifico, false);
        //TextView tvTiempoIntervalo = intervaloView.findViewById(R.id.tvTiempoIntervalo);
        TextView tvRepeticion = intervaloView.findViewById(R.id.tvRepeticion);
        TextView tvTiempoDescanso = intervaloView.findViewById(R.id.tvTiempoDescanso);

        //tvTiempoIntervalo.setText("Tiempo: " + tiempo);
        tvRepeticion.setText("Repetición: 1");
        tvTiempoDescanso.setText("Descanso: " + descanso);

        containerModoEspecifico.addView(intervaloView);*/
    }
}