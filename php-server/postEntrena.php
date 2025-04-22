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
$fecha = $_POST['fecha'] ?? '';
$distancia = $_POST['distancia'] ?? '';
$tiempo = $_POST['tiempo'] ?? '';
$velo = $_POST['velocidad'] ?? '';
$valoracion = $_POST['valoracion'] ?? null;
$comentarios = $_POST['comentarios'] ?? null;
$tipoEntrena = $_POST['tipoEntrena'] ?? null;

// Validar datos obligatorios
if (empty($token) || empty($idEntrena) || empty($fecha) || empty($distancia) || empty($tiempo) || empty($velo)) {
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
    
    // Insertar el entrenamiento
    $stmt = $conn->prepare("INSERT INTO Xalarrazabal025_entrenamientos 
                           (usuario_id, id_local, idActividad, fechaHora, tiempo, distancia, velocidad, valoracion, comentarios) 
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
    
    // Asignar valores por defecto para campos opcionales
    $idActividad = $tipoEntrena; // Puedes cambiarlo o recibirlo por POST si es necesario
    $valoracion = !empty($valoracion) ? $valoracion : null;
    $comentarios = !empty($comentarios) ? $comentarios : null;
    
    $stmt->bind_param("iisssssss", 
        $usuario_id,
        $idEntrena,
        $idActividad,
        $fecha,
        $tiempo,
        $distancia,
        $velo,
        $valoracion,
        $comentarios
    );
    
    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Entrenamiento guardado correctamente"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error al guardar el entrenamiento: " . $stmt->error]);
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