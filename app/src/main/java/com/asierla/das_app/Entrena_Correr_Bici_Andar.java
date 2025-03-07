package com.asierla.das_app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Entrena_Correr_Bici_Andar extends AppCompatActivity implements OnMapReadyCallback{
    private TextView tvCuentaAtras, tvTiempo, tvDistancia, tvVelocidad, tvRitmo, tvEntrenamiento;
    private Button btnParar, btnReanudar, btnFinalizar;
    private LinearLayout layoutBotones;
    private ImageView btnMusica;
    private MapView mapView;
    private GoogleMap googleMap;
    private boolean isRunning = false;
    private long startTime, elapsedTime;
    private float totalDistance = 0;
    private Location lastLocation;
    private Polyline routePolyline;
    private PolylineOptions polyline;
    private final Handler handler = new Handler();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener idioma guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "es"); // Por defecto español

        // Aplicar idioma antes de cargar el contenido
        Locale nuevaloc = new Locale(idioma);
        Locale.setDefault(nuevaloc);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(nuevaloc);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());


        // Coger todos los items con los que interactuo
        setContentView(R.layout.activity_entrena_correr_bici_andar);
        tvCuentaAtras = findViewById(R.id.tvCuentaAtras);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvDistancia = findViewById(R.id.tvDistancia);
        tvVelocidad = findViewById(R.id.tvVelocidad);
        tvRitmo = findViewById(R.id.tvRitmo);
        btnParar = findViewById(R.id.btnParar);
        btnReanudar = findViewById(R.id.btnReanudar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnMusica = findViewById(R.id.btnMusica);
        mapView = findViewById(R.id.mapView);
        layoutBotones = findViewById(R.id.layoutBotones);
        tvEntrenamiento = findViewById(R.id.tvEntrenamiento);

        // Coger el tipo de entrenamiento
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);
        switch (tipoEntrenamiento){
            case 0:
                // Correr
                tvEntrenamiento.setText(R.string.correr);
                break;
            case 1:
                // Bicicleta
                tvEntrenamiento.setText(R.string.bici);
                break;
            case 2:
                // Andar
                tvEntrenamiento.setText(R.string.andar);
                break;
        }



        // Inicializar el MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync((OnMapReadyCallback) this);

        // Permisos de la Ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mapView.getMapAsync(this);
        }


        // Restaurar el estado guardado
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime");
            elapsedTime = savedInstanceState.getLong("elapsedTime");
            totalDistance = savedInstanceState.getFloat("totalDistance");
            isRunning = savedInstanceState.getBoolean("isRunning");

            if (elapsedTime > 0) {
                tvCuentaAtras.setVisibility(View.GONE);
            }

            /* Restaurar ubicación anterior
            if (savedInstanceState.containsKey("lastLatitude") && savedInstanceState.containsKey("lastLongitude")) {
                double latitude = savedInstanceState.getDouble("lastLatitude");
                double longitude = savedInstanceState.getDouble("lastLongitude");
                lastLocation = new Location("");
                lastLocation.setLatitude(latitude);
                lastLocation.setLongitude(longitude);
            }*/

            /* Restaurar los puntos de la Polyline
            if (savedInstanceState.containsKey("polylinePoints")) {
                ArrayList<LatLng> points = savedInstanceState.getParcelableArrayList("polylinePoints");
                if (points != null && !points.isEmpty()) {
                    polyline = new PolylineOptions()
                            .addAll(points)
                            .color(Color.RED)
                            .width(10);
                    if (googleMap != null) {
                        routePolyline = googleMap.addPolyline(polyline);
                    }
                }
            }*/
        }

        // Iniciar la cuenta atrás solo si no estamos restaurando el estado
        if (!isRunning) {
            new CountDownTimer(4000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tvCuentaAtras.setText(String.valueOf(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    tvCuentaAtras.setVisibility(View.GONE);
                    startTraining();
                }
            }.start();
        } else {
            // Si la actividad ya estaba corriendo, reiniciar el cronómetro y actualizar la distancia
            tvDistancia.setText(String.format("%.2f km", totalDistance / 1000));
        }


        // Configurar listeners de botones
        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());
        btnMusica.setOnClickListener(v -> openMusica());

    }


    /*
     * Esta función se inicia cuando la cuenta atras de 4 segundos se inica
     * Esta funión se encarga de iniciar el entrenamiento
     */
    private void startTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis();
        handler.post(timerRunnable); // Inicia el temporizador
    }


    /*
     * Esta función se encarga de parar el entrenamiento
     * Hace que aparezcan los botones de reanudar y finalizar en entrena
     * Guarda en tiempo de entrenamiento que llevamos
     */
    private void pauseTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable); // Detener actualización
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        btnParar.setVisibility(View.GONE);
        layoutBotones.setVisibility(View.VISIBLE);
    }

    /*
     * Esta función se encarga de reanudar el entrenamiento despues de la pausa
     * Vuelve a poner visible el boton de parar
     * Recupera el tiempo de entrenamiento que llevabamos
     */
    private void resumeTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis() - (elapsedTime * 1000); // Restauramos el tiempo desde el punto de pausa
        handler.post(timerRunnable);
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);
    }


    /*
     * Esta función se encarga de parar y guardar el entrenamiento en memoria.
     * Despues de guardar se visualiza el entrenamiento en la lista de entrenas.
     */
    private void stopTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        guardarEntrenamientoEnBD();
        Toast.makeText(this, "Entrenamiento finalizado y guardado", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Entrena_Correr_Bici_Andar.this, HistorialEntrenamiento.class);
        startActivity(intent);
        finish();
    }



    /*
     * Esta funión pone los segundos en formato HH:MM:SS o MM:SS
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }



    /*
     * Esta función sirve para guardar el estado actual de una vista.
     * La utilizo para guardar los datos de Vertical a Horizontal y vic.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Guardar el estado actual
        outState.putLong("startTime", startTime);
        outState.putLong("elapsedTime", elapsedTime);
        outState.putFloat("totalDistance", totalDistance);
        outState.putBoolean("isRunning", isRunning);

        // Guardar si la cuenta atrás está en curso
        outState.putBoolean("isCountingDown", tvCuentaAtras.getVisibility() == View.VISIBLE);

        // Guardar ubicación si es necesario
        if (lastLocation != null) {
            outState.putDouble("lastLatitude", lastLocation.getLatitude());
            outState.putDouble("lastLongitude", lastLocation.getLongitude());
        }

        // Guardar los puntos de la Polyline
        if (polyline != null) {
            outState.putParcelableArrayList("polylinePoints", new ArrayList<>(polyline.getPoints()));
        }
    }

    /*
     * Para abrir una aplicación de reproducción de musica
     */
    private void openMusica() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Abriendo música...", Snackbar.LENGTH_SHORT);
        snackbar.show();

        String spotifyPackage = "com.spotify.music";
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(spotifyPackage);

        if (launchIntent != null) {
            startActivity(launchIntent);
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://open.spotify.com"));
                startActivity(intent);
            } catch (Exception e) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com"));
                startActivity(webIntent);
            }
        }
    }

    /*
     * Guarda los datos en la bd.
     */
    private void guardarEntrenamientoEnBD() {
        DBHelper dbHelper = new DBHelper(this);

        // Obtener el tipo de actividad desde el TextView o desde la Intent (como ya lo haces)
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        float distancia = totalDistance; // Convertir a metros si es necesario
        long tiempoSegundos = elapsedTime;

        // Obtener el tipo de entrenamiento (de la Intent)
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);

        // Guardar los datos en la base de datos
        dbHelper.guardarEntrenamientoAuto(tipoEntrenamiento, fecha, distancia, tiempoSegundos);
        Toast.makeText(this, "Entrenamiento guardado", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    /*
     * Para que se actualice cada minuto
     */
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                tvTiempo.setText(formatTime(elapsedTime));
                handler.postDelayed(this, 1000); // Repite cada segundo
            }
        }
    };
}
