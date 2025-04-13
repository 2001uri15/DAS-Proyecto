package com.asierla.das_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.asierla.das_app.database.DBServer;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if user is already logged in
        SharedPreferences user = getSharedPreferences("Usuario", MODE_PRIVATE);
        Boolean iniciado = user.getBoolean("iniciado", false);
        String token = user.getString("token", "");

        if(iniciado && !token.isEmpty()) {
            showProgress(true);
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(tokenFCM -> verificarToken(token, tokenFCM))
                    .addOnFailureListener(e -> {
                        Log.e("FCM", "Error obteniendo token", e);
                        showProgress(false);
                        goToLogin();
                    });
        } else {
            goToLogin();
        }
    }

    private void verificarToken(String token, String tokenFCM) {
        Data inputData = new Data.Builder()
                .putString("action", "validate_token")
                .putString("token", token)
                .putString("tokenFCM", tokenFCM)
                .build();

        OneTimeWorkRequest loginRequest = new OneTimeWorkRequest.Builder(DBServer.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        showProgress(false); // Ocultar ProgressBar cuando termine

                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            try {
                                JSONObject response = new JSONObject(workInfo.getOutputData().getString("result"));
                                if (response.getString("status").equals("success")) {
                                    Intent intent = new Intent(MainActivity.this, Home.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    clearUserData();
                                    goToLogin();
                                }
                            } catch (JSONException e) {
                                clearUserData();
                                goToLogin();
                            }
                        } else {
                            clearUserData();
                            goToLogin();
                        }
                    }
                });

        WorkManager.getInstance(this).enqueue(loginRequest);
    }

    private void showProgress(boolean show) {
        runOnUiThread(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                progressBar.setIndeterminate(true); // Asegura que gire continuamente
            }
        });
    }

    private void clearUserData() {
        SharedPreferences prefs = getSharedPreferences("Usuario", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("iniciado", false);
        editor.putString("token", null);
        editor.putString("nombre", null);
        editor.putString("apellido", null);
        editor.putString("username", null);
        editor.putString("mail", null);
        editor.apply();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, IogIn.class);
        startActivity(intent);
        finish();
    }
}