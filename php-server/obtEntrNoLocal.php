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

// Obtener parámetros
$token = $_POST['token'] ?? "";
$idsLocales = isset($_POST['ids_locales']) ? explode(',', $_POST['ids_locales']) : [];

if (empty($token)) {
    die(json_encode([
        'status' => 'error',
        'message' => 'Token no proporcionado'
    ]));
}

// Validar el token y obtener el usuario_id
$sqlUser = "SELECT idUser FROM Xalarrazabal025_user_app WHERE token = ?";
$stmtUser = $conn->prepare($sqlUser);
$stmtUser->bind_param('s', $token);
$stmtUser->execute();
$resultUser = $stmtUser->get_result();

if ($resultUser->num_rows === 0) {
    die(json_encode([
        'status' => 'error',
        'message' => 'Token inválido'
    ]));
}

$userData = $resultUser->fetch_assoc();
$usuario_id = $userData['idUser'];

// Obtener todos los entrenamientos que no tenga
if (!empty($idsLocales)) {
    $placeholders = implode(',', array_fill(0, count($idsLocales), '?'));
    $sqlEntrenamientos = "SELECT * FROM Xalarrazabal025_entrenamientos WHERE usuario_id = ? AND id_local NOT IN ($placeholders)";
    $stmtEntrenamientos = $conn->prepare($sqlEntrenamientos);
    
    $types = 'i' . str_repeat('i', count($idsLocales));
    $params = array_merge([$usuario_id], $idsLocales);
    $stmtEntrenamientos->bind_param($types, ...$params);
} else {
    $sqlEntrenamientos = "SELECT * FROM Xalarrazabal025_entrenamientos WHERE usuario_id = ?";
    $stmtEntrenamientos = $conn->prepare($sqlEntrenamientos);
    $stmtEntrenamientos->bind_param('i', $usuario_id);
}

$stmtEntrenamientos->execute();
$resultEntrenamientos = $stmtEntrenamientos->get_result();
$entrenamientos = [];

// Preparar la consulta para obtener rutas
$sqlRutas = "SELECT `id`, `id_local`, `latitud`, `longitud` FROM `Xalarrazabal025_Ruta` WHERE `id_local`=?";
$stmtRutas = $conn->prepare($sqlRutas);

while ($row = $resultEntrenamientos->fetch_assoc()) {
    // Obtener rutas para este entrenamiento
    $stmtRutas->bind_param('i', $row['id_local']);
    $stmtRutas->execute();
    $resultRutas = $stmtRutas->get_result();
    
    $rutas = [];
    while ($ruta = $resultRutas->fetch_assoc()) {
        $rutas[] = $ruta;
    }
    
    // Añadir las rutas al array del entrenamiento
    $row['rutas'] = $rutas;
    $entrenamientos[] = $row;
}

// Devolver respuesta JSON
echo json_encode([
    'status' => 'success',
    'data' => $entrenamientos
]);

// Cerrar conexiones
$stmtUser->close();
$stmtEntrenamientos->close();
$stmtRutas->close();
$conn->close();
?>