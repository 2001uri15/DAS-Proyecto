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

// Validar datos de entrada
if (empty($token)) {
    echo json_encode(["status" => "error", "message" => "Token no proporcionado"]);
    exit();
}

// Preparar la consulta
$sql = "SELECT `foto` FROM `Xalarrazabal025_usuarios` WHERE id = (SELECT idUser FROM Xalarrazabal025_user_app WHERE token = ?)";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(["status" => "error", "message" => "Error al preparar la consulta"]);
    exit();
}

// Bind de parámetros y ejecución
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "No se encontró usuario con ese token", "foto" => null]);
} else {
    $row = $result->fetch_assoc();
    $foto = $row['foto'];
    
    /* Convertir el BLOB a base64 si no es null
    $fotoData = null;
    if ($foto !== null) {
        $fotoData = base64_encode($foto);
    }*/
    
    echo json_encode([
        "status" => "success", 
        "message" => "Foto obtenida correctamente",
        "foto" => $foto
    ]);
}

$stmt->close();
$conn->close();
?>
