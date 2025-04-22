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

// Validar datos obligatorios
if (empty($token) || empty($idEntrena)) {
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
    
    // Iniciar transacción para asegurar consistencia
    $conn->begin_transaction();
    
    try {
        // 1. Borrar la ruta asociada al entrenamiento
        $stmt = $conn->prepare("DELETE FROM Xalarrazabal025_Ruta WHERE id_local = ?");
        $stmt->bind_param("i", $idEntrena);
        $stmt->execute();
        
        // 2. Borrar el entrenamiento
        $stmt = $conn->prepare("DELETE FROM Xalarrazabal025_entrenamientos WHERE usuario_id = ? AND id_local = ?");
        $stmt->bind_param("ii", $usuario_id, $idEntrena);
        $stmt->execute();
        
        if ($stmt->affected_rows === 0) {
            throw new Exception("No se encontró el entrenamiento para este usuario");
        }
        
        // Confirmar transacción
        $conn->commit();
        
        echo json_encode(["status" => "success", "message" => "Entrenamiento eliminado correctamente"]);
        
    } catch (Exception $e) {
        // Revertir transacción en caso de error
        $conn->rollback();
        echo json_encode(["status" => "error", "message" => "Error al eliminar: " . $e->getMessage()]);
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