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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.SSLHandshakeException;

public class DBServer extends Worker {
    private static final String BASE_URL = "https://das.egunero.eus/";
    private static final String TAG = "DBServer";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final long LARGE_RESPONSE_THRESHOLD = 100000; // 100KB

    public DBServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String action = getInputData().getString("action");
        String username = getInputData().getString("username");
        String password = getInputData().getString("password");
        String nombre = getInputData().getString("nombre");
        String apellido = getInputData().getString("apellido");
        String mail = getInputData().getString("mail");
        String token = getInputData().getString("token");
        String tokenFCM = getInputData().getString("tokenFCM");
        String foto = getInputData().getString("foto");
        int privacidad = getInputData().getInt("privacidad", 1);
        int[] idsLocales = getInputData().getIntArray("ids_locales");
        String idEntrena = String.valueOf(getInputData().getInt("idEntrena", 0));
        String fecha = getInputData().getString("fecha");
        double distancia = getInputData().getDouble("distancia", 0);
        int tiempo = getInputData().getInt("tiempo", 0);
        double velocidad = getInputData().getDouble("velocidad", 0);
        int tipoEntrena = getInputData().getInt("tipoEntrena", 0);
        double latitud = getInputData().getDouble("latitud", 0);
        double longitud = getInputData().getDouble("longitud", 0);

        try {
            Data resultData;
            switch (Objects.requireNonNull(action)) {
                case "login":
                    resultData = handleStandardRequest("login", loginUser(username, password, tokenFCM));
                    break;
                case "validate_token":
                    resultData = handleStandardRequest("validate_token", validateToken(token, tokenFCM));
                    break;
                case "registrar":
                    resultData = handleStandardRequest("registrar", registrar(username, nombre, apellido, password, mail, String.valueOf(privacidad), tokenFCM));
                    break;
                case "borrarSesion":
                    resultData = handleStandardRequest("borrarSesion", borrarSesion(token));
                    break;
                case "actualizarUsar":
                    resultData = handleStandardRequest("actualizarUsar", actualizarUsuario(token, nombre, apellido, mail, password));
                    break;
                case "actualizarImg":
                    resultData = handleStandardRequest("actualizarImg", actualizarFoto(token, foto));
                    break;
                case "actDatos":
                    resultData = handleLargeDataRequest(actDatos(token, idsLocales));
                    break;
                case "postDatos":
                    resultData = handleStandardRequest("postDatos", postEntrena(token, idEntrena, fecha, distancia, tiempo, velocidad, tipoEntrena));
                    break;
                case "postRuta":
                    resultData = handleStandardRequest("postRuta", postRuta(token, idEntrena, latitud, longitud));
                    break;
                case "deleteEntrena":
                    resultData = handleStandardRequest("deleteEntrena", deleteEntrena(token, idEntrena));
                    break;
                default:
                    return Result.failure(createErrorOutput("Acción no válida"));
            }

            return Result.success(resultData);
        } catch (IOException e) {
            Log.e(TAG, "Error de conexión: " + e.getMessage());
            return Result.failure(createErrorOutput("Error de conexión: " + e.getMessage()));
        } catch (JSONException e) {
            Log.e(TAG, "Error al parsear JSON: " + e.getMessage());
            return Result.failure(createErrorOutput("Error al procesar la respuesta"));
        } catch (Exception e) {
            Log.e(TAG, "Error inesperado: " + e.getMessage());
            return Result.failure(createErrorOutput("Error inesperado: " + e.getMessage()));
        }
    }

    private Data handleStandardRequest(String action, String response) throws JSONException {
        JSONObject jsonResponse = new JSONObject(response);
        String status = jsonResponse.getString("status");

        if (status.equals("error")) {
            return createErrorOutput(jsonResponse.getString("message"));
        }

        return new Data.Builder()
                .putString("action", action)
                .putString("result", response)
                .putBoolean("is_file", false)
                .build();
    }

    private Data handleLargeDataRequest(File dataFile) throws IOException {
        // Para respuestas muy grandes, devolvemos la ruta del archivo
        return new Data.Builder()
                .putString("action", "actDatos")
                .putString("file_path", dataFile.getAbsolutePath())
                .putBoolean("is_file", true)
                .build();
    }

    private Data createErrorOutput(String message) {
        return new Data.Builder()
                .putString("error", message)
                .build();
    }

    private File hacerPeticionLargeData(String endpoint, Map<String, String> params) throws IOException {
        HttpURLConnection urlConnection = null;
        File tempFile = null;

        try {
            String fullUrl = BASE_URL + (endpoint.startsWith("/") ? endpoint.substring(1) : endpoint);
            URL url = new URL(fullUrl);
            Log.d(TAG, "Conectando a: " + fullUrl);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(30000); // Mayor timeout para datos grandes

            // Construir parámetros POST
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (param.getKey() == null || param.getValue() == null) {
                    continue;
                }
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }

            // Enviar datos
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Crear archivo temporal
            tempFile = File.createTempFile("large_resp_", ".json", getApplicationContext().getCacheDir());

            // Stream directo de red a archivo
            try (InputStream input = urlConnection.getInputStream();
                 OutputStream output = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;

        } catch (Exception e) {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private String hacerPeticion(String endpoint, Map<String, String> params) throws IOException {
        // Validación de parámetros de entrada
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("El endpoint no puede ser nulo o vacío");
        }

        if (params == null) {
            throw new IllegalArgumentException("Los parámetros no pueden ser nulos");
        }

        HttpURLConnection urlConnection = null;
        String response = null;

        try {
            // Construir URL con validación
            String fullUrl = BASE_URL + (endpoint.startsWith("/") ? endpoint.substring(1) : endpoint);
            URL url = new URL(fullUrl);
            Log.d(TAG, "Conectando a: " + fullUrl);

            // Configurar conexión
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);

            // Construir y validar parámetros POST
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (param.getKey() == null || param.getValue() == null) {
                    Log.w(TAG, "Parámetro nulo detectado - Key: " + param.getKey() + ", Value: " + param.getValue());
                    continue;
                }

                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"))
                        .append('=')
                        .append(URLEncoder.encode(param.getValue(), "UTF-8"));
            }

            // Verificar si hay parámetros válidos
            if (postData.length() == 0) {
                throw new IOException("No se proporcionaron parámetros válidos para la solicitud");
            }

            Log.d(TAG, "Enviando parámetros: " + postData.toString());

            // Enviar datos
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            // Procesar respuesta
            int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "Código de respuesta: " + responseCode);

            InputStream inputStream;
            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }

            // Leer respuesta
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine);
                }
                response = responseBuilder.toString();
            }

            if (response.trim().isEmpty()) {
                throw new IOException("Respuesta vacía del servidor");
            }

            Log.d(TAG, "Respuesta recibida: " + response);

        } catch (MalformedURLException e) {
            throw new IOException("URL mal formada: " + e.getMessage(), e);
        } catch (SocketTimeoutException e) {
            throw new IOException("Tiempo de espera agotado al conectar con el servidor", e);
        } catch (SSLHandshakeException e) {
            throw new IOException("Error de seguridad SSL: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("Error de comunicación: " + e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response;
    }

    private Data createOutputData(String message) {
        return new Data.Builder()
                .putString("result", message)
                .build();
    }

    /*
     * A partir de aqui están todas las funciones para hacer las consultas de las peticiones
     */

    private String loginUser(String username, String password, String tokenFCM) throws IOException {
        String recurso = "sartu.php";

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("tokenFCM", tokenFCM);

        return hacerPeticion(recurso, params);
    }

    private String registrar(String username, String nombre, String apellido, String password,
                             String mail, String privacidad, String tokenFCM) throws IOException {
        String recurso = "registrar.php";

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("nombre", nombre);
        params.put("apellido", apellido);
        params.put("password", password);
        params.put("mail", mail);
        params.put("privacidad", privacidad);
        params.put("tokenFCM", tokenFCM);

        return hacerPeticion(recurso, params);
    }

    private String borrarSesion(String token) throws IOException {
        String recurso = "cerrarSesion.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);

        return hacerPeticion(recurso, params);
    }

    private String validateToken(String token, String tokenFCM) throws IOException {
        String recurso = "verificarToken.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("tokenFCM", tokenFCM);

        return hacerPeticion(recurso, params);
    }

    private String actualizarUsuario(String token, String nombre, String apellido, String mail, String contra) throws IOException {
        String recurso = "actualizarUser.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("nombre", nombre);
        params.put("apellido", apellido);
        params.put("mail", mail);
        params.put("password", contra);

        return hacerPeticion(recurso, params);
    }

    private String actualizarFoto(String token, String foto) throws IOException {
        String recurso = "actualizarFoto.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("foto", foto);

        return hacerPeticion(recurso, params);
    }

    private File actDatos(String token, int[] idsLocales) throws IOException {
        String recurso = "obtEntrNoLocal.php";
        String idsStr = Arrays.stream(idsLocales)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("ids_locales", idsStr);

        return hacerPeticionLargeData(recurso, params);
    }

    private String postEntrena(String token, String idEntrena, String fecha, double distancia,
                               int tiempo, double velocidad, int tipoEntrena) throws IOException {
        String recurso = "postEntrena.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("idEntrena", idEntrena);
        params.put("fecha", fecha);
        params.put("distancia", String.valueOf(distancia));
        params.put("tiempo", String.valueOf(tiempo));
        params.put("velocidad", String.valueOf(velocidad));
        params.put("tipoEntrena", String.valueOf(tipoEntrena));

        return hacerPeticion(recurso, params);
    }

    private String postRuta(String token, String idEntrena, double latitud, double longitud) throws IOException {
        String recurso = "postRuta.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("idEntrena", idEntrena);
        params.put("latitud", String.valueOf(latitud));
        params.put("longitud", String.valueOf(longitud));

        return hacerPeticion(recurso, params);
    }

    private String deleteEntrena(String token, String idEntrena) throws IOException {
        String recurso = "deleteEntrena.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("idEntrena", idEntrena);

        return hacerPeticion(recurso, params);
    }



}