package com.asierla.das_app.database;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBDatos {
    private static final String BASE_URL = "https://das.egunero.eus/";
    private static final String TAG = "DBServer";
    private final Context context;
    private final ExecutorService executorService;
    private String authToken;

    public DBDatos(Context context) {
        this.context = context.getApplicationContext();
        this.authToken = authToken;
        this.executorService = Executors.newFixedThreadPool(3); // Pool de 3 hilos
    }

    public interface ApiCallback {
        void onSuccess(File responseFile);
        void onSuccess(String responseString);
        void onFailure(String error);
        void onProgress(int percent, long bytesReceived, long totalBytes);
    }

    public void obtenerEntradasNoLocales(List<String> ids, boolean forceToFile, ApiCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;

            try {
                // Construir URL con parámetros
                String query = buildQueryString(ids);
                URL url = new URL(BASE_URL + "obtEntrNoLocal.php?" + query);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000); // 30 segundos
                connection.setReadTimeout(300000); // 5 minutos para respuestas grandes

                // Configurar conexión
                connection.setDoInput(true);
                connection.setUseCaches(false);

                // Conectar
                connection.connect();

                // Verificar respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    callback.onFailure("HTTP error code: " + responseCode);
                    return;
                }

                // Obtener información de la respuesta
                long contentLength = connection.getContentLength();
                String contentType = connection.getContentType();

                // Decidir si guardar en archivo o memoria
                boolean useFile = forceToFile || contentLength > 10 * 1024 * 1024; // >10MB -> archivo

                if (useFile) {
                    processLargeResponse(connection, contentLength, callback);
                } else {
                    processSmallResponse(connection, contentLength, callback);
                }

            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
                if (connection != null) connection.disconnect();
            }
        });
    }

    private String buildQueryString(List<String> ids) throws UnsupportedEncodingException {
        StringBuilder query = new StringBuilder();
        query.append("token=").append(URLEncoder.encode(authToken, "UTF-8"));

        for (String id : ids) {
            query.append("&ids[]=").append(URLEncoder.encode(id, "UTF-8"));
        }

        return query.toString();
    }

    private void processSmallResponse(HttpURLConnection connection, long contentLength,
                                      ApiCallback callback) throws IOException {

        StringBuilder response = new StringBuilder();
        InputStream inputStream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        char[] buffer = new char[8192]; // 8KB buffer
        int read;
        long totalRead = 0;

        while ((read = reader.read(buffer)) != -1) {
            response.append(buffer, 0, read);
            totalRead += read;

            if (contentLength > 0) {
                int progress = (int) ((totalRead * 100) / contentLength);
                callback.onProgress(progress, totalRead, contentLength);
            }
        }

        callback.onSuccess(response.toString());
    }

    private void processLargeResponse(HttpURLConnection connection, long contentLength,
                                      ApiCallback callback) throws IOException {

        File outputFile = createTempFile();
        InputStream inputStream = connection.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[8192]; // 8KB buffer
        int read;
        long totalRead = 0;

        try {
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                totalRead += read;

                if (contentLength > 0) {
                    int progress = (int) ((totalRead * 100) / contentLength);
                    callback.onProgress(progress, totalRead, contentLength);
                }
            }

            outputStream.flush();
            callback.onSuccess(outputFile);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing input stream", e);
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing output stream", e);
            }
        }
    }

    private File createTempFile() throws IOException {
        File cacheDir = context.getCacheDir();
        return File.createTempFile("api_response_", ".tmp", cacheDir);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void cleanup() {
        executorService.shutdown();
        cleanTempFiles();
    }

    private void cleanTempFiles() {
        File cacheDir = context.getCacheDir();
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith("api_response_"));
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < System.currentTimeMillis() - 24 * 60 * 60 * 1000) {
                    file.delete();
                }
            }
        }
    }
}