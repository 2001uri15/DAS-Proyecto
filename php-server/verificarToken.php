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
    die(json_encode(["status" => "error", "message" => "Error de conexión"]));
}

$conn->set_charset("utf8mb4");

// Verificar si se recibió el token y tokenFCM por GET o POST
$token = $_GET['token'] ?? $_POST['token'] ?? '';
$tokenFCM = $_GET['tokenFCM'] ?? $_POST['tokenFCM'] ?? '';

if (empty($token)) {
    echo json_encode(["status" => "error", "message" => "Token de usuario no proporcionado"]);
    exit();
}

if (empty($tokenFCM)) {
    echo json_encode(["status" => "error", "message" => "Token FCM no proporcionado"]);
    exit();
}

// Consultar si el token existe
$stmt = $conn->prepare("SELECT idUser, fecha FROM Xalarrazabal025_user_app WHERE token = ?");
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    
    // Actualizar el token FCM
    $updateStmt = $conn->prepare("UPDATE Xalarrazabal025_user_app SET tokenFCM = ? WHERE token = ?");
    $updateStmt->bind_param("ss", $tokenFCM, $token);
    
    if ($updateStmt->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "Token válido y FCM actualizado",
            "idUser" => $row['idUser'],
            "fecha" => $row['fecha']
        ]);
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Token válido pero error al actualizar FCM"
        ]);
    }
    
    $updateStmt->close();
} else {
    echo json_encode([
        "status" => "invalid",
        "message" => "Token no válido o expirado"
    ]);
}

$stmt->close();
$conn->close();
?>
