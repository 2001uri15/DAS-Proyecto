package com.asierla.das_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentView(R.layout.activity_home);
        Button btnCorrer = findViewById(R.id.btnCorrer);
        btnCorrer.setOnClickListener(v -> {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Entrenamiento de Correr", Snackbar.LENGTH_SHORT);
            snackbar.show();
            Intent intent = new Intent(Home.this, Entrenamiento.class);
            startActivity(intent);
        });
    }
}