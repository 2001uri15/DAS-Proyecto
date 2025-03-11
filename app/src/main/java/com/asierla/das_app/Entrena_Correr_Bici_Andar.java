package com.asierla.das_app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.model.EntrenamientoData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Entrena_Correr_Bici_Andar extends AppCompatActivity implements OnMapReadyCallback {
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
    private List<Float> speedList = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private ArrayList<LatLng> routePoints = new ArrayList<>();
    private ArrayList<EntrenamientoData> entrenamientoDataList = new ArrayList<EntrenamientoData>();

    private NotificationManager elManager;
    private NotificationCompat.Builder elBuilder;





    @Override
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


        // Pedir permiso para las notificaciones
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)!=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        }


        elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        elBuilder = new NotificationCompat.Builder(this, "IdCanal");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel("IdCanal", "NombreCanal",
                    NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(elCanal);
        }

        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Entrenamiento")
                .setContentText("En entrenamiento va a comenzar")
                .setSmallIcon(obtenerIconoActividad(getIntent().getIntExtra("tipo_entrenamiento", 0)))
                .setAutoCancel(true);

        elManager.notify(1, elBuilder.build());

        // Cargar la vista
        setContentView(R.layout.activity_entrena_correr_bici_andar);

        // Inicializar vistas
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

        // Poner el nombre del entrena
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);
        tvEntrenamiento.setText(obtenerNombreActividad(tipoEntrenamiento));

        // Inicializar el MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); // Configurar el callback para cuando el mapa esté listo

        // Configurar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Inicializar FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();

        // Restaurar el estado guardado
        if (savedInstanceState != null) {
            startTime = savedInstanceState.getLong("startTime");
            elapsedTime = savedInstanceState.getLong("elapsedTime");
            totalDistance = savedInstanceState.getFloat("totalDistance");
            isRunning = savedInstanceState.getBoolean("isRunning");
            lastLocation = savedInstanceState.getParcelable("lastLocation");
            routePoints = savedInstanceState.getParcelableArrayList("routePoints");
            entrenamientoDataList = (ArrayList<EntrenamientoData>) savedInstanceState.getSerializable("entrenamientoDataList");

            if (elapsedTime > 0) {
                tvCuentaAtras.setVisibility(View.GONE);
            }

            // Si hay puntos guardados, dibujar la ruta en el mapa
            if (routePoints != null && googleMap != null) {
                routePolyline = googleMap.addPolyline(new PolylineOptions().addAll(routePoints).width(5).color(Color.RED));
            }

            // Si la actividad estaba en ejecución, reiniciar el Handler
            if (isRunning) {
                startTraining();
            }
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
        }

        // Configurar listeners de botones
        btnParar.setOnClickListener(v -> pauseTraining());
        btnReanudar.setOnClickListener(v -> resumeTraining());
        btnFinalizar.setOnClickListener(v -> stopTraining());
        btnMusica.setOnClickListener(v -> openMusica());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mostrarDialogoRetroceso();
            }
        });
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };
    }

    private void updateLocation(Location location) {
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            totalDistance += distance;

            // Obtener el tiempo transcurrido entre las ubicaciones
            long timeDeltaMillis = location.getTime() - lastLocation.getTime();
            float timeDeltaSeconds = timeDeltaMillis / 1000f; // Convertir a segundos
            float timeDeltaHours = timeDeltaSeconds / 3600f; // Convertir a horas

            float distanceKm = distance / 1000f; // Convertir a kilómetros
            float totalDistanceKm = totalDistance / 1000f; // Convertir a kilómetros

            // Calcular velocidad en km/h usando el tiempo exacto de la ubicación
            float speed = (timeDeltaSeconds > 0) ? (distanceKm / timeDeltaHours) : 0;
            speedList.add(speed);

            // Actualizar la UI con la distancia y velocidad
            tvDistancia.setText(String.format("%.2f km", totalDistanceKm));
            tvVelocidad.setText(String.format("%.2f km/h", speed));

            // Calcular el ritmo (min/km) en formato mm:ss /km
            if (distance > 0 && timeDeltaSeconds > 0) {
                float paceMinutes = (timeDeltaSeconds / 60f) / distanceKm; // min/km
                int minutes = (int) paceMinutes;
                int seconds = (int) ((paceMinutes - minutes) * 60);

                tvRitmo.setText(String.format("%02d:%02d /km", minutes, seconds));
            } else {
                tvRitmo.setText("--:-- /km"); // Mostrar un valor neutro si no hay distancia
            }

            entrenamientoDataList.add(new EntrenamientoData(totalDistanceKm, (long) timeDeltaSeconds, speed));

            // Dibujar la ruta en el mapa
            LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
            routePoints.add(newPoint);

            if (googleMap != null) {
                if (routePolyline != null) {
                    routePolyline.remove(); // Eliminar la Polyline anterior
                }
                routePolyline = googleMap.addPolyline(new PolylineOptions().addAll(routePoints).width(5).color(Color.RED));
            }
        }
        lastLocation = location;
    }

    private void startTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis() - (elapsedTime * 1000); // Restaurar el tiempo acumulado
        handler.post(timerRunnable); // Inicia el temporizador
        startLocationUpdates();
    }

    private void pauseTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable); // Detener actualización
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000; // Guardar el tiempo acumulado
        btnParar.setVisibility(View.GONE);
        layoutBotones.setVisibility(View.VISIBLE);
        stopLocationUpdates();
    }

    private void resumeTraining() {
        isRunning = true;
        startTime = System.currentTimeMillis() - (elapsedTime * 1000); // Restauramos el tiempo desde el punto de pausa
        handler.post(timerRunnable);
        btnParar.setVisibility(View.VISIBLE);
        layoutBotones.setVisibility(View.GONE);
        startLocationUpdates();
    }

    private void stopTraining() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        stopLocationUpdates();
        guardarEntrenamientoEnBD();
        Toast.makeText(this, R.string.entrena_guardado_finalizado, Toast.LENGTH_SHORT).show();

        // En la notificación ponga total km y el tiempo
        elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("Entrenamiento Finalizado")
                .setContentText("Distancia: "+ String.format("%.2f km", totalDistance / 1000) + "Tiempo: " + formatTime(elapsedTime))
                .setSmallIcon(obtenerIconoActividad(getIntent().getIntExtra("tipo_entrenamiento", 0)))
                .setAutoCancel(true);
        elManager.notify(1, elBuilder.build());

        Intent intent = new Intent(Entrena_Correr_Bici_Andar.this, HistorialEntrenamiento.class);
        startActivity(intent);
        finish();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

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

    private void guardarEntrenamientoEnBD() {
        DBHelper dbHelper = new DBHelper(this);

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        float distancia = totalDistance; // Convertir a metros si es necesario
        long tiempoSegundos = elapsedTime*1000;
        float averageSpeed = calculateAverageSpeed(speedList);
        int tipoEntrenamiento = getIntent().getIntExtra("tipo_entrenamiento", 0);

        long idEntrena = dbHelper.guardarEntrenamientoAuto(tipoEntrenamiento, fecha, distancia, tiempoSegundos, averageSpeed);

        // Guardar los puntos de la ruta
        for (LatLng punto : routePoints) {
            dbHelper.guardarPuntoRuta(idEntrena, punto.latitude, punto.longitude);
        }

        // Obtener los datos agrupados por intervalos de 1 km
        List<EntrenamientoData> kilometroDataList = agruparPorIntervalosDe1Km(entrenamientoDataList);
        int i = 1;
        for (EntrenamientoData data : kilometroDataList) {
            dbHelper.guardarIntervalo(idEntrena, i, data.getTiempo(),data.getDistancia(),data.getVelocidad());
            Log.d("KilometroData", data.toString());
            i++;
        }
    }

    private float calculateAverageSpeed(List<Float> speedList) {
        if (speedList.isEmpty()) {
            return 0; // Evitar división por cero si la lista está vacía
        }
        float sum = 0;
        for (float speed : speedList) {
            sum += speed;
        }
        return sum / speedList.size();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Habilitar la capa de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            // Obtener la última ubicación conocida
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Mover la cámara a la ubicación actual
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15)); // 17 es el nivel de zoom
                            }
                        }
                    });
        }

        // Configurar el tipo de mapa como satélite
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // Si hay puntos guardados, dibujar la ruta en el mapa
        if (routePoints != null && !routePoints.isEmpty()) {
            routePolyline = googleMap.addPolyline(new PolylineOptions().addAll(routePoints).width(5).color(Color.RED));
        }
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
                tvTiempo.setText(formatTime(elapsedTime));
                elBuilder.setSmallIcon(android.R.drawable.stat_sys_warning)
                        .setContentTitle("Entrenamiento")
                        .setContentText("Distancia: "+ String.format("%.2f km", totalDistance / 1000) + "Tiempo: " + formatTime(elapsedTime))
                        .setSmallIcon(obtenerIconoActividad(getIntent().getIntExtra("tipo_entrenamiento", 0)))
                        .setAutoCancel(true);

                elManager.notify(1, elBuilder.build());

                handler.postDelayed(this, 1000); // Repite cada segundo
            }
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar el estado actual
        outState.putLong("startTime", startTime);
        outState.putLong("elapsedTime", elapsedTime);
        outState.putFloat("totalDistance", totalDistance);
        outState.putBoolean("isRunning", isRunning);
        outState.putParcelable("lastLocation", lastLocation);
        outState.putParcelableArrayList("routePoints", routePoints);
        outState.putSerializable("entrenamientoDataList", entrenamientoDataList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (isRunning) {
            startLocationUpdates();
            handler.post(timerRunnable); // Reiniciar el Handler si el entrenamiento estaba en ejecución
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (isRunning) {
            stopLocationUpdates();
            handler.removeCallbacks(timerRunnable); // Detener el Handler si el entrenamiento estaba en ejecución
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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

    private void mostrarDialogoRetroceso() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.que_quieres_hacer);

        // Opción 1: Guardar y Salir
        builder.setPositiveButton(R.string.guardar_salir, (dialog, which) -> {
            startTraining();
        });

        // Opción 2: Salir
        builder.setNeutralButton(R.string.salir, (dialog, which) -> {
            elManager.cancel(1);
            finish();
        });

        // Opción 3: Cancelar
        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            // No hacer nada, simplemente cerrar el diálogo
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
        dialog.show();
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


    private List<EntrenamientoData> agruparPorIntervalosDe1Km(List<EntrenamientoData> entrenamientoDataList) {
        // Crear una lista para almacenar los resultados
        List<EntrenamientoData> kilometroDataList = new ArrayList<>();

        // Ordenar la lista por distancia
        entrenamientoDataList.sort(Comparator.comparing(EntrenamientoData::getDistancia));

        // Variables para acumular datos del intervalo actual
        double intervaloActual = 0.0; // Inicia en 0 km
        long tiempoAcumulado = 0;
        double velocidadAcumulada = 0.0;
        int contador = 0;

        // Recorrer la lista de datos
        for (EntrenamientoData data : entrenamientoDataList) {
            double distancia = data.getDistancia();

            // Si la distancia supera el intervalo actual + 1 km, guardar el intervalo
            if (distancia > intervaloActual + 1.0) {
                if (contador > 0) { // Solo agregar si hay datos en el intervalo
                    double velocidadMedia = velocidadAcumulada / contador;
                    kilometroDataList.add(new EntrenamientoData((float) (intervaloActual + 1.0), tiempoAcumulado, (float) velocidadMedia));
                }

                // Reiniciar las variables para el nuevo intervalo
                intervaloActual = Math.floor(distancia); // Redondear hacia abajo al kilómetro más cercano
                tiempoAcumulado = data.getTiempo();
                velocidadAcumulada = data.getVelocidad();
                contador = 1;
            } else {
                // Acumular datos en el intervalo actual
                tiempoAcumulado += data.getTiempo();
                velocidadAcumulada += data.getVelocidad();
                contador++;
            }
        }

        // Agregar el último intervalo si hay datos
        if (contador > 0) {
            double velocidadMedia = velocidadAcumulada / contador;
            kilometroDataList.add(new EntrenamientoData((float) (intervaloActual + 1.0), tiempoAcumulado,(float) velocidadMedia));
        }

        return kilometroDataList;
    }
}