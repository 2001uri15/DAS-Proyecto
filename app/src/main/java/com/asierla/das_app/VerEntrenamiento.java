package com.asierla.das_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

import com.asierla.das_app.database.DBHelper;
import com.asierla.das_app.model.Entrenamiento;
import com.asierla.das_app.model.EntrenamientoInterval;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Locale;

public class VerEntrenamiento extends AppCompatActivity {
    private TextView ipVelociad, ipEntrena, ipFecha, ipDuracion, ipDistancia, ipComentarios, ipRitmo;
    private ImageView ipImagenEntre, btnMenu;
    private RatingBar ipValoracion;
    private TableLayout tableIntervalos;
    private RelativeLayout relaVueltas, relaMapa;

    private MapView mapView;
    private GoogleMap googleMap;
    private ArrayList<LatLng> ubi;


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
        btnMenu = findViewById(R.id.btnMenu);
        ipValoracion = findViewById(R.id.textValo);
        ipRitmo = findViewById(R.id.ipRitmo);
        tableIntervalos = findViewById(R.id.tableIntervalos);
        relaVueltas = findViewById(R.id.relaVueltas);
        relaMapa = findViewById(R.id.relaMapa);

        int idEntreno = getIntent().getIntExtra("idEntrena", 0);
        DBHelper db = new DBHelper(this);
        Entrenamiento entrena = db.obtenerEntrenaById(idEntreno);

        // Poner los datos en los labels
        ArrayList<EntrenamientoInterval> intervalos = db.obtenerIntervalos(idEntreno);
        ipEntrena.setText(entrena.getNombreActividadId());
        ipFecha.setText(entrena.getFecha());
        ipDuracion.setText(entrena.getTiempo());
        ipImagenEntre.setImageResource(entrena.getIcono());
        ipValoracion.setRating((float) entrena.getValoracion());
        ipComentarios.setText(entrena.getComentarios());

        if(entrena.getIdEntrenamiento()>=0 && entrena.getIdEntrenamiento()<=2){
            // Carrera, Bici, Andar
            ipVelociad.setText(String.format("%.1f km/h", entrena.getVelocidad()));
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
            int i = 0;
            for (EntrenamientoInterval intervalo : intervalos) {
                TableRow row = new TableRow(this);
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                ));


                if (i % 2 == 0) {
                    row.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                } else {
                    row.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
                }

// TextView para el número de intervalo
                TextView tvOrden = new TextView(this);
                tvOrden.setText(String.valueOf(intervalo.getOrden()));
                tvOrden.setGravity(Gravity.CENTER);
                tvOrden.setTextSize(14);
                tvOrden.setPadding(2, 2, 2, 2); // Ajusta el padding según sea necesario
                row.addView(tvOrden);

// TextView para la distancia
                TextView tvDistancia = new TextView(this);
                if(entrena.getIdEntrenamiento()>=0 && entrena.getIdEntrenamiento()<=2){
                    tvDistancia.setText(String.format(intervalo.getDistancia() + " km"));
                }else if (entrena.getIdEntrenamiento()>2 && entrena.getIdEntrenamiento()<5){
                    tvDistancia.setText(String.format((int)intervalo.getDistancia() + " m"));
                }
                tvDistancia.setGravity(Gravity.CENTER);
                tvDistancia.setTextSize(14);
                tvDistancia.setPadding(2, 2, 2, 2); // Ajusta el padding según sea necesario
                row.addView(tvDistancia);

// TextView para el tiempo
                TextView tvTiempo = new TextView(this);
                tvTiempo.setText(convertirATiempo(intervalo.getTiempo()));
                tvTiempo.setTextSize(14);
                tvTiempo.setGravity(Gravity.CENTER);
                tvTiempo.setPadding(2, 2, 2, 2); // Ajusta el padding según sea necesario
                row.addView(tvTiempo);

// TextView para el ritmo
                TextView tvRitmo = new TextView(this);
                tvRitmo.setText(String.valueOf((int)intervalo.getPaladas()));
                tvRitmo.setTextSize(14);
                tvRitmo.setGravity(Gravity.CENTER);
                tvRitmo.setPadding(2, 2, 2, 2); // Ajusta el padding según sea necesario
                row.addView(tvRitmo);

                tableIntervalos.addView(row);
                i++;
            }
        }else{
            relaVueltas.setVisibility(View.GONE);
        }

        ubi = db.obtenerRutaPorEntrenamientoId(idEntreno);
        if(ubi.size()>0){
            mapView = findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    dibujarRuta();
                }
            });
        }else{
            relaMapa.setVisibility(View.GONE);
        }




        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un PopupMenu
                PopupMenu popupMenu = new PopupMenu(VerEntrenamiento.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_entrenamiento, popupMenu.getMenu());

                // Manejar las acciones de los ítems del menú
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.action_editar) {
                            mostrarDialogoEditar();
                            return true;
                        } else if (id == R.id.action_borrar) {
                            borrarEntrena(entrena.getId());
                            finish();
                        }
                        return false;
                    }
                });

                // Mostrar el menú
                popupMenu.show();
            }
        });
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

    private void mostrarDialogoEditar() {
        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Entrenamiento");

        // Inflar el layout del diálogo
        View view = getLayoutInflater().inflate(R.layout.dialog_editar_entrenamiento, null);
        builder.setView(view);

        // Inicializar las variables
        RatingBar ratingBar = view.findViewById(R.id.ratingBarEditar); // Asegúrate de que el ID coincida con el XML
        EditText editTextComentarios = view.findViewById(R.id.editTextComentarios); // Asegúrate de que el ID coincida con el XML

        // Cargar los valores actuales desde la base de datos
        //cargarValoresActuales();
        DBHelper db = new DBHelper(this);
        Entrenamiento entrena = db.obtenerEntrenaById(getIntent().getIntExtra("idEntrena", 0));
        ratingBar.setRating((float) entrena.getValoracion());
        editTextComentarios.setText(entrena.getComentarios());

        // Configurar botones del diálogo
        builder.setPositiveButton(R.string.guardar, (dialog, which) -> {
            // Guardar los cambios en la base de datos
            //guardarCambios();
            db.actualizarEntrena(getIntent().getIntExtra("idEntrena", 0), (int) ratingBar.getRating(), editTextComentarios.getText().toString());
            Intent intent = new Intent(this, VerEntrenamiento.class);
            intent.putExtra("idEntrena", getIntent().getIntExtra("idEntrena", 0)); // Pasar el ID del entrenamiento
            startActivity(intent);
            finish(); // Cerrar la actividad actual
        });

        builder.setNegativeButton(R.string.cancelar, (dialog, which) -> {
            dialog.dismiss();
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
        dialog.show();
    }

    private void dibujarRuta() {
        if (googleMap == null || ubi == null || ubi.isEmpty()) return;

        // Dibujar la línea de la ruta
        googleMap.addPolyline(new PolylineOptions()
                .addAll(ubi)
                .color(Color.RED)
                .width(8f)
                .geodesic(true));

        // Ajustar la cámara para que toda la ruta sea visible con un zoom adecuado
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng punto : ubi) {
            builder.include(punto);
        }

        // Establecer los límites de la cámara con un padding
        googleMap.setOnMapLoadedCallback(() -> {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
        });
    }
}