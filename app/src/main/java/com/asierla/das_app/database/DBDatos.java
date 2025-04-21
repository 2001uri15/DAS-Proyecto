package com.asierla.das_app.database;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

public class DBDatos {
    private static final String BASE_URL = "https://das.egunero.eus/";
    private static final String TAG = "DBDatos";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    private static final int MAX_MEMORY_RESPONSE_SIZE = 1048576; // 1MB (respuestas más grandes se guardarán en archivo)
    private final Context context;

    public DBDatos(Context context) {
        this.context = context.getApplicationContext();
    }

    public interface ApiCallback {
        void onSuccess(JSONObject responseJson);
        void onSuccess(File responseFile);
        void onError(String errorMessage);
    }

    public void actualizarDatos(String token, String idLocales, ApiCallback callback) {
        String recurso = "obtEntrNoLocal.php";

        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("ids_locales", idLocales);

        hacerPeticionAsync(recurso, params, callback);
    }

    private void hacerPeticionAsync(String endpoint, Map<String, String> params, ApiCallback callback) {
        new Thread(() -> {
            try {
                // Validación de parámetros de entrada
                if (endpoint == null || endpoint.trim().isEmpty()) {
                    throw new IllegalArgumentException("El endpoint no puede ser nulo o vacío");
                }

                if (params == null) {
                    throw new IllegalArgumentException("Los parámetros no pueden ser nulos");
                }

                HttpURLConnection urlConnection = null;
                InputStream inputStream = null;

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
                    urlConnection.setReadTimeout(30000); // Aumentado para respuestas grandes

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

                    Log.d(TAG, "Enviando parámetros: " + postData);

                    // Enviar datos
                    try (OutputStream os = urlConnection.getOutputStream()) {
                        byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                        os.flush();
                    }

                    // Procesar respuesta
                    int responseCode = urlConnection.getResponseCode();
                    Log.d(TAG, "Código de respuesta: " + responseCode);

                    if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());

                        // Verificar el tamaño del contenido si está disponible
                        int contentLength = urlConnection.getContentLength();
                        Log.d(TAG, "Tamaño del contenido: " + contentLength + " bytes");

                        // Determinar si guardar en archivo o procesar en memoria
                        if (contentLength > MAX_MEMORY_RESPONSE_SIZE) {
                            File responseFile = guardarRespuestaEnArchivo(inputStream);
                            callback.onSuccess(responseFile);
                        } else {
                            // Procesar en memoria
                            JSONObject jsonResponse = procesarRespuestaEnMemoria(inputStream);
                            callback.onSuccess(jsonResponse);
                        }
                    } else {
                        // Manejar errores HTTP
                        inputStream = urlConnection.getErrorStream();
                        String errorMessage = leerStream(inputStream);
                        throw new IOException("Error en el servidor: " + responseCode + " - " + errorMessage);
                    }
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Error al cerrar inputStream", e);
                        }
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            } catch (MalformedURLException e) {
                callback.onError("URL mal formada: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                callback.onError("Tiempo de espera agotado al conectar con el servidor");
            } catch (SSLHandshakeException e) {
                callback.onError("Error de seguridad SSL: " + e.getMessage());
            } catch (IOException e) {
                callback.onError("Error de comunicación: " + e.getMessage());
            } catch (JSONException e) {
                callback.onError("Error al procesar la respuesta JSON: " + e.getMessage());
            } catch (Exception e) {
                callback.onError("Error inesperado: " + e.getMessage());
            }
        }).start();
    }

    private File guardarRespuestaEnArchivo(InputStream inputStream) throws IOException {
        File outputFile = File.createTempFile("api_response_", ".json", context.getCacheDir());
        Log.d(TAG, "Guardando respuesta grande en archivo: " + outputFile.getAbsolutePath());

        try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                // Opcional: Log cada 5MB procesados
                if (totalBytes % (5 * 1024 * 1024) == 0) {
                    Log.d(TAG, "Procesados " + (totalBytes / (1024 * 1024)) + "MB");
                }
            }

            Log.d(TAG, "Total de bytes guardados: " + totalBytes);
        }

        return outputFile;
    }

    private JSONObject procesarRespuestaEnMemoria(InputStream inputStream) throws IOException, JSONException {
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                responseBuilder.append(buffer, 0, charsRead);
            }
        }

        String responseString = responseBuilder.toString();
        return new JSONObject(responseString);
    }

    private String leerStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        return responseBuilder.toString();
    }
}