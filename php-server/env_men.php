<?php
require 'fcm_config.php';
require 'fcm_sender.php';

header('Content-Type: text/plain; charset=utf-8');

try {
    // Verificar existencia del archivo de credenciales
    if (!file_exists(FCM_SERVICE_ACCOUNT_PATH)) {
        throw new Exception("ERROR: Archivo de credenciales no encontrado en: ".FCM_SERVICE_ACCOUNT_PATH);
    }
    
    // Inicializar el sender
    $fcm = new FCMSender(FCM_SERVICE_ACCOUNT_PATH);
    
    // Token de dispositivo de prueba (reemplázalo por uno real)
    $deviceToken = 'fE0Xi9EURhCFnVfLE7NLNE:APA91bE41vQf9nyVhbPxCdUK5eTTu0ZtCpZqJ9_G5JK-8JiNdy39_atyryO96woEcNoCQ9BEdQyqgkVzJDxbLcOZMhIHf5kEIkHh0zYUd6HZsFHvbMdOL1s';
    
    // Enviar notificación
    $response = $fcm->sendToDevice(
        $deviceToken,
        'Prueba de autenticación',
        'Esta notificación prueba que la autenticación funciona',
        ['fecha' => date('Y-m-d H:i:s')]
    );
    
    echo "=== RESPUESTA DE FCM ===\n";
    print_r($response);
    
    if (isset($response['error'])) {
        throw new Exception("Error en FCM: ".$response['error']['message']);
    }
    
    echo "\n¡Notificación enviada con éxito!";
    
} catch (Exception $e) {
    echo "ERROR: ".$e->getMessage()."\n";
    
    // Mostrar información de depuración si está disponible
    if (isset($fcm) && method_exists($fcm, 'getLastRequestInfo')) {
        echo "\n=== INFORMACIÓN DE DEPURACIÓN ===\n";
        print_r($fcm->getLastRequestInfo());
    }
}
