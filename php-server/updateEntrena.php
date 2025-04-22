<?php
header('Content-Type: application/json; charset=utf-8');

// Conexión a la base de datos
$servername = "bbdd.egunero.eus";
$username = "ddb251190"; 
$password = "}K2@f1$;Eps@{m"; 
$dbname = "ddb251190"; 

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Error de conexión: " . $conn->connect_error]));
}

$conn->set_charset("utf8mb4");

// Recibir datos POST (sanitizados)
$token = $_POST['token'] ?? '';
$idEntrena = intval($_POST['idEntrena'] ?? 0); // Convertir a entero
$valoracion = $_POST['valoracion'] ?? null;    // Opcional
$comen = $_POST['comentario'] ?? null;         // Opcional

// Validar datos obligatorios
if (empty($token) || $idEntrena <= 0) {
    echo json_encode(["status" => "error", "message" => "Token o ID de entrenamiento inválido"]);
    exit;
}

// Verificar primero si el usuario existe
$stmtCheck = $conn->prepare("SELECT `idUser` FROM `Xalarrazabal025_user_app` WHERE `token` = ?");
$stmtCheck->bind_param("s", $token);
$stmtCheck->execute();
$stmtCheck->store_result();

if ($stmtCheck->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Token no válido o usuario no encontrado"]);
    exit;
}
$stmtCheck->close();

// Actualizar entrenamiento
$stmt = $conn->prepare("
    UPDATE `Xalarrazabal025_entrenamientos` 
    SET `valoracion` = ?, `comentarios` = ? 
    WHERE `id_local` = ? 
    AND `usuario_id` = (SELECT `idUser` FROM `Xalarrazabal025_user_app` WHERE `token` = ?)
");
$stmt->bind_param("isis", $valoracion, $comen, $idEntrena, $token);
$stmt->execute();

if ($stmt->affected_rows > 0) {
    echo json_encode(["status" => "success", "message" => "Datos actualizados correctamente"]);
} else {
    // No se actualizó ningún registro (posiblemente id_local no existe o no pertenece al usuario)
    echo json_encode(["status" => "error", "message" => "No se encontró el entrenamiento o no tienes permisos"]);
}

$stmt->close();
$conn->close();
?>