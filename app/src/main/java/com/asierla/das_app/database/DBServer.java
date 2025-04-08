package com.asierla.das_app.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DBServer extends Worker {
    private static final String BASE_URL = "https://das.egunero.eus/";
    private static final String TAG = "DBServer";

    public DBServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String action = getInputData().getString("action");
        String username = getInputData().getString("username");
        String password = getInputData().getString("password");
        String token = getInputData().getString("token");

        try {
            String result;
            switch (action) {
                case "login":
                    result = loginUser(username, password);
                    break;
                case "validate_token":
                    result = validateToken(token);
                    break;
                case "registrar":
                    result = null;//registrar(username, nombre, apellido, mail, password, privacidad);
                    break;
                default:
                    return Result.failure(createOutputData("Error: Acción no válida"));
            }

            JSONObject jsonResponse = new JSONObject(result);
            String status = jsonResponse.getString("status");

            if (status.equals("error")) {
                return Result.failure(createOutputData(jsonResponse.getString("message")));
            }

            return Result.success(createOutputData(result));
        } catch (IOException e) {
            Log.e(TAG, "Error de conexión: " + e.getMessage());
            return Result.failure(createOutputData("Error de conexión"));
        } catch (JSONException e) {
            Log.e(TAG, "Error al parsear JSON: " + e.getMessage());
            return Result.failure(createOutputData("Error al procesar la respuesta"));
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado: " + e.getMessage());
            return Result.failure(createOutputData("Error inesperado"));
        }
    }

    private String hacerPeticion(String endpoint, Map<String, String> params) throws IOException {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(BASE_URL + endpoint);
            urlConnection = (HttpURLConnection) url.openConnection();

            // Configurar conexión
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            // Construir parámetros POST
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }

            // Enviar parámetros
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Verificar respuesta
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // Leer respuesta
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            return response.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error cerrando reader", e);
                }
            }
        }
    }

    private Data createOutputData(String message) {
        return new Data.Builder()
                .putString("result", message)
                .build();
    }

    /*
     * A partir de aqui están todas las funciones para hacer las consultas de las peticiones
     */

    private String loginUser(String username, String password) throws IOException {
        String recurso = "sartu.php";

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        return hacerPeticion(recurso, params);
    }

    private String registrar(String username, String nombre, String apellido, String contrasenia,
                             String mail, String privacidad) throws IOException{
        String recurso = "registrar.php";

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("nombre", nombre);
        params.put("apellidos", apellido);
        params.put("password", contrasenia);
        params.put("mail", mail);
        params.put("privacidad", privacidad);

        return hacerPeticion(recurso, params);
    }

    private String validateToken(String token) throws IOException {
        String recurso = "verificarToken.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        return hacerPeticion(recurso, params);
    }
}