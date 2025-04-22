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

// Recibir datos POST con nombres alternativos
$nombre     = $_POST['nombre']     ?? '';
$apellido   = $_POST['apellido']   ?? '';
$username   = $_POST['username']   ?? '';
$mail       = $_POST['mail']       ?? '';
$contrasena = $_POST['password']   ?? ''; // Acepta ambos nombres
$privacidad = $_POST['privacidad'] ?? '0'; // Corregido el nombre del campo
$tokenFCM = $_POST['tokenFCM'] ?? '';

// Validaciones básicas
if (empty($nombre) || empty($apellido) || empty($username) || empty($mail) || empty($contrasena)) {
    echo json_encode([
        "status" => "error", 
        "message" => "Todos los campos obligatorios deben ser enviados",
        "received_data" => $_POST // Para depuración
    ]);
    exit();
}

// Comprobar si ya existe el usuario
$stmt_check = $conn->prepare("SELECT id FROM Xalarrazabal025_usuarios WHERE username = ? OR mail = ?");
$stmt_check->bind_param("ss", $username, $mail);
$stmt_check->execute();
$stmt_check->store_result();

if ($stmt_check->num_rows > 0) {
    echo json_encode(["status" => "error", "message" => "El usuario o correo ya existe"]);
    exit();
}
$stmt_check->close();

// Insertar nuevo usuario (sin foto)
$stmt_insert = $conn->prepare("INSERT INTO Xalarrazabal025_usuarios (nombre, apellido, username, mail, contrasenia, privacidad) VALUES (?, ?, ?, ?, ?, ?)");
$hashedPassword = md5($contrasena);  // Para más seguridad: usar password_hash()
$stmt_insert->bind_param("sssssi", $nombre, $apellido, $username, $mail, $hashedPassword, $privacidad);

if ($stmt_insert->execute()) {
    $idUsuario = $stmt_insert->insert_id;

    // Crear token y guardar sesión
    $token = bin2hex(random_bytes(32));
    $fecha = date('Y-m-d H:i:s');

    $stmt_token = $conn->prepare("INSERT INTO Xalarrazabal025_user_app (idUser, fecha, token, tokenFCM) VALUES (?, ?, ?, ?)");
    $stmt_token->bind_param("isss", $idUsuario, $fecha, $token, $tokenFCM);
    $stmt_token->execute();
    $stmt_token->close();

    echo json_encode([
        "status"    => "success",
        "message"   => "Registro exitoso",
        "token"     => $token,
        "nombre"    => $nombre,
        "apellido"  => $apellido,
        "username"  => $username,
        "mail"      => $mail
    ]);
} else {
    echo json_encode(["status" => "error", "message" => "Error al registrar usuario"]);
}

$stmt_insert->close();
$conn->close();
?>
