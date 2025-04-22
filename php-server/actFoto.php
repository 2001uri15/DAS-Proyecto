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
    die(json_encode(["status" => "error", "message" => "Error de conexión a la base de datos"]));
}

$conn->set_charset("utf8mb4");

// Verificar método POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["status" => "error", "message" => "Solo se aceptan peticiones POST"]);
    exit();
}

$token = $_POST['token'] ?? "";
$img = $_POST['img'] ?? null;

// Validar datos de entrada
if (empty($token)) {
    echo json_encode(["status" => "error", "message" => "Token no proporcionado"]);
    exit();
}

if (empty($img)) {
    echo json_encode(["status" => "error", "message" => "Imagen no proporcionada"]);
    exit();
}

// Preparar la consulta
$sql = "UPDATE Xalarrazabal025_usuarios 
        SET foto = ? 
        WHERE id = (SELECT idUser FROM Xalarrazabal025_user_app WHERE token = ?)";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Error al preparar la consulta"]);
    exit();
}

// Bind de parámetros y ejecución
$stmt->bind_param("ss", $img, $token);
$stmt->execute();

if ($stmt->affected_rows > 0) {
    echo json_encode([
        "status" => "success",
        "message" => "Imagen actualizada correctamente",
        "img" => $img
    ]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "No se pudo actualizar la imagen. Verifica el token.",
        "img" => null
    ]);
}

$stmt->close();
$conn->close();
?>
