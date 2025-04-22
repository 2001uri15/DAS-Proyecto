<?php
header('Content-Type: application/json; charset=utf-8');

// Conexión a la base de datos
$servername = "bbdd.egunero.eus";
$username = "ddb251190"; 
$password = "}K2@f1$;Eps@{m"; 
$dbname = "ddb251190"; 

$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar la conexión
if ($conn->connect_error) {
    die(json_encode(["status" => "error", "message" => "Error de conexión"]));
}

$conn->set_charset("utf8mb4");

// Verificar si se recibieron datos por POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = $_POST['username'] ?? '';
    $password = $_POST['password'] ?? '';
    $tokenFCM = $_POST['tokenFCM'] ?? '';

    if (empty($username) || empty($password)) {
        echo json_encode(["status" => "error", "message" => "Campos vacíos"]);
        exit();
    }

    // Consultar usuario
    $stmt = $conn->prepare("SELECT id, nombre, apellido, username, mail, contrasenia, foto FROM Xalarrazabal025_usuarios WHERE username = ?");
    $stmt->bind_param("s", $username);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $row = $result->fetch_assoc();
        $userId = $row['id'];
        $nombre = $row['nombre'];
        $apellido = $row['apellido'];
        $user = $row['username'];
        $mail = $row['mail'];
        $hashedPassword = $row['contrasenia'];
        $foto = $row['foto'] !== null ? base64_encode($row['foto']) : null;

        // Verificar contraseña (asumiendo MD5)
        if (md5($password) === $hashedPassword) {
            // Generar token
            $token = bin2hex(random_bytes(32));

            // Insertar token (fecha se autogenera)
            $stmt_token = $conn->prepare("INSERT INTO Xalarrazabal025_user_app (idUser, token, tokenFCM) VALUES (?, ?, ?)");
            $stmt_token->bind_param("iss", $userId, $token, $tokenFCM);

            if ($stmt_token->execute()) {
                echo json_encode([
                    "status" => "success",
                    "token" => $token,
                    "mensaje" => "Inicio de sesión exitoso",
                    "nombre" => $nombre,
                    "apellido" => $apellido,
                    "username" => $user,
                    "mail" => $mail
                ]);
            } else {
                echo json_encode([
                    "status" => "error",
                    "message" => "Error al guardar el token: " . $conn->error
                ]);
            }

            $stmt_token->close();
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "Contraseña incorrecta"
            ]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "Usuario no encontrado"]);
    }

    $stmt->close();
} else {
    echo json_encode(["status" => "error", "message" => "Método no permitido"]);
}

$conn->close();
?>

