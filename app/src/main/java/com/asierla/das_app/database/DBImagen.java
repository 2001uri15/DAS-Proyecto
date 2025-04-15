package com.asierla.das_app.database;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBImagen {
    private static final String TAG = "SUBIR_IMAGEN";
    private static final String URL_API_UPDATE = "https://das.egunero.eus/actFoto.php";
    private static final String URL_API_POST = "https://das.egunero.eus/obtFoto.php";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int MAX_IMAGE_WIDTH = 1024;
    private static final int MAX_IMAGE_HEIGHT = 1024;
    private static final int QUALITY = 70;

    public interface UploadCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void uploadImageAsBase64(Bitmap image, String token, UploadCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            OutputStream outputStream = null;

            try {
                // 1. Redimensionar y comprimir la imagen
                Bitmap resizedBitmap = resizeBitmap(image);
                String imageBase64 = bitmapToBase64(resizedBitmap);


                // 2. Preparar los parámetros en formato x-www-form-urlencoded
                String postData = "token=" + URLEncoder.encode(token, "UTF-8") +
                        "&img=" + URLEncoder.encode(imageBase64, "UTF-8");

                Log.d("SUBIR_IMAGEN", "datos : " + postData);

                // 3. Configurar la conexión HTTP
                URL url = new URL(URL_API_UPDATE);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                // 4. Escribir los datos
                outputStream = connection.getOutputStream();
                byte[] input = postData.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                outputStream.flush();

                // 5. Procesar la respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    callback.onSuccess(response);
                } else {
                    callback.onError("HTTP error code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al subir la imagen", e);
                callback.onError(e.getMessage());
            } finally {
                if (outputStream != null) {
                    try { outputStream.close(); } catch (Exception e) { Log.e(TAG, "Error al cerrar stream", e); }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public static void obtenerImagen(String token, UploadCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            OutputStream outputStream = null;

            try {
                // 2. Preparar los parámetros en formato x-www-form-urlencoded
                String postData = "token=" + URLEncoder.encode(token, "UTF-8");

                // 3. Configurar la conexión HTTP
                URL url = new URL(URL_API_POST);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                // 4. Escribir los datos
                outputStream = connection.getOutputStream();
                byte[] input = postData.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
                outputStream.flush();

                // 5. Procesar la respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    java.util.Scanner scanner = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A");
                    String response = scanner.hasNext() ? scanner.next() : "";
                    callback.onSuccess(response);
                } else {
                    callback.onError("HTTP error code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al subir la imagen", e);
                callback.onError(e.getMessage());
            } finally {
                if (outputStream != null) {
                    try { outputStream.close(); } catch (Exception e) { Log.e(TAG, "Error al cerrar stream", e); }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static Bitmap resizeBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width > MAX_IMAGE_WIDTH || height > MAX_IMAGE_HEIGHT) {
            float ratio = Math.min(
                    (float) MAX_IMAGE_WIDTH / width,
                    (float) MAX_IMAGE_HEIGHT / height
            );
            width = Math.round(width * ratio);
            height = Math.round(height * ratio);

            Log.d(TAG, "Redimensionando imagen a: " + width + "x" + height);
            return Bitmap.createScaledBitmap(image, width, height, true);
        }
        return image;
    }

    private static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, QUALITY, byteArrayOutputStream);
        // Eliminar saltos de línea que podrían causar problemas
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP);
    }
}