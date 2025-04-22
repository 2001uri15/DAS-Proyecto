<?php
header('Content-Type: application/json; charset=utf-8');

// Conexión a la base de datos
$servername = "bbdd.egunero.eus";
$username = "ddb251190"; 
$password = "}K2@f1$;Eps@{m"; 
$dbname = "ddb251190"; 

$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Error de conexión: " . $conn->connect_error]));
}

$conn->set_charset("utf8mb4");

// Recibir datos POST
$token = $_POST['token'] ?? '';
$idEntrena = $_POST['idEntrena'] ?? '';
$latitud = $_POST['latitud'] ?? '';  // Fixed typo (was 'latutud')
$longitud = $_POST['longitud'] ?? '';

// Validar datos obligatorios
if (empty($token) || empty($idEntrena) || empty($latitud) || empty($longitud)) {
    echo json_encode(["status" => "error", "message" => "Faltan campos obligatorios"]);
    exit;
}

try {
    // Obtener el ID del usuario a partir del token
    $stmt = $conn->prepare("SELECT idUser FROM Xalarrazabal025_user_app WHERE token = ?");
    $stmt->bind_param("s", $token);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        echo json_encode(["status" => "error", "message" => "Token no válido"]);
        exit;
    }
    
    $user = $result->fetch_assoc();
    $usuario_id = $user['idUser'];
    
    // Insertar la ubicación en la ruta
    $stmt = $conn->prepare("INSERT INTO Xalarrazabal025_Ruta 
                           (id_local, latitud, longitud) 
                           VALUES (?, ?, ?)");
    
    // Convertir valores a los tipos correctos
    $idActividad = (int)$idEntrena;  // Convertir a int
    $latitud = (double)$latitud;    // Convertir a double
    $longitud = (double)$longitud;  // Convertir a double
    
    $stmt->bind_param("idd", 
        $idActividad,
        $latitud,
        $longitud
    );
    
    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Ubicación guardada correctamente"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error al guardar la ubicación: " . $stmt->error]);
    }
    
} catch (Exception $e) {
    echo json_encode(["status" => "error", "message" => "Error: " . $e->getMessage()]);
} finally {
    if (isset($stmt)) {
        $stmt->close();
    }
    $conn->close();
}
?>