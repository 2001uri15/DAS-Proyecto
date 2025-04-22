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

// Recibir datos POST
$token = $_POST['token'] ?? '';
$nombre = $_POST['nombre'] ?? null;
$apellido = $_POST['apellido'] ?? null;
$mail = $_POST['mail'] ?? null;
$contrasena = $_POST['password'] ?? null;

// Validar token
if (empty($token)) {
    echo json_encode(["status" => "error", "message" => "Token no proporcionado"]);
    exit();
}

// Verificar token y obtener ID de usuario
$stmt_token = $conn->prepare("SELECT idUser FROM Xalarrazabal025_user_app WHERE token = ?");
$stmt_token->bind_param("s", $token);
$stmt_token->execute();
$result_token = $stmt_token->get_result();

if ($result_token->num_rows === 0) {
    echo json_encode(["status" => "error", "message" => "Token inválido o sesión expirada"]);
    exit();
}

$user_data = $result_token->fetch_assoc();
$idUsuario = $user_data['idUser'];
$stmt_token->close();

// Verificar que al menos un campo para actualizar fue enviado
if (is_null($nombre) && is_null($apellido) && is_null($mail) && is_null($contrasena)) {
    echo json_encode(["status" => "error", "message" => "Debe proporcionar al menos un campo para actualizar"]);
    exit();
}

// Verificar si el nuevo email ya existe (si se está actualizando)
if (!is_null($mail)) {
    $stmt_check_mail = $conn->prepare("SELECT id FROM Xalarrazabal025_usuarios WHERE mail = ? AND id != ?");
    $stmt_check_mail->bind_param("si", $mail, $idUsuario);
    $stmt_check_mail->execute();
    $stmt_check_mail->store_result();
    
    if ($stmt_check_mail->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "El correo electrónico ya está en uso por otro usuario"]);
        exit();
    }
    $stmt_check_mail->close();
}

// Construir la consulta SQL dinámicamente
$updates = [];
$params = [];
$types = "";

if (!is_null($nombre)) {
    $updates[] = "nombre = ?";
    $params[] = $nombre;
    $types .= "s";
}

if (!is_null($apellido)) {
    $updates[] = "apellido = ?";
    $params[] = $apellido;
    $types .= "s";
}

if (!is_null($mail)) {
    $updates[] = "mail = ?";
    $params[] = $mail;
    $types .= "s";
}

if (!is_null($contrasena)) {
    $hashedPassword = md5($contrasena); // O usar password_hash()
    $updates[] = "contrasenia = ?";
    $params[] = $hashedPassword;
    $types .= "s";
}

if (count($updates) === 0) {
    echo json_encode(["status" => "error", "message" => "No hay campos válidos para actualizar"]);
    exit();
}

$params[] = $idUsuario;
$types .= "i";

$sql = "UPDATE Xalarrazabal025_usuarios SET " . implode(", ", $updates) . " WHERE id = ?";
$stmt_update = $conn->prepare($sql);

// Enlazar parámetros dinámicamente
$stmt_update->bind_param($types, ...$params);

if ($stmt_update->execute()) {
    // Obtener los datos actualizados del usuario
    $stmt_user = $conn->prepare("SELECT nombre, apellido, username, mail FROM Xalarrazabal025_usuarios WHERE id = ?");
    $stmt_user->bind_param("i", $idUsuario);
    $stmt_user->execute();
    $result_user = $stmt_user->get_result();
    $user = $result_user->fetch_assoc();
    $stmt_user->close();
    
    echo json_encode([
        "status" => "success",
        "message" => "Datos actualizados correctamente",
        "user" => [
            "nombre" => $user['nombre'],
            "apellido" => $user['apellido'],
            "username" => $user['username'],
            "mail" => $user['mail']
        ]
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Error al actualizar los datos"]);
}

$stmt_update->close();
$conn->close();
?>