<?php
/*
Table: ensembles - (id, ensemblename, ensembledate, ensembleauthor, ensembleuser_id, ensemblejson)
Table: users - (id, email, password, date_created, verified)
Table: log
*/

$host = "localhost";
$usuario = "user";
$pwd = "pwd";
$base_de_datos = "db";

// Create connection using mysqli instead of mysql_connect
$conn = new mysqli($host, $usuario, $pwd, $base_de_datos);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$ensembleName = "-";
$ensembleAuthor = "-";

// Get and check Username (register)
$userName = $_POST["username"];
$result = $conn->query("SELECT id FROM users WHERE email = '$userName'");
if($result->num_rows == 0) { // user not found, create new
    $conn->query("INSERT INTO users (email) VALUES ('$userName')");
    $result = $conn->query("SELECT id FROM users WHERE email = '$userName'");
}
$user = $result->fetch_row(); // user[0]=ensembleuser_id
$ensembleUser = $user[0];

// Get and check Action
$action = $_POST["action"];
switch ($action) {
    case "setEnsemble":
        $ensembleName = $_POST["ensemblename"];
        $ensembleAuthor = $_POST["ensembleauthor"];
        $ensembleJSON = $_POST["ensemblejson"];
        $conn->query("DELETE FROM ensembles WHERE ensemblename = '$ensembleName' AND ensembleauthor = '$ensembleAuthor' AND ensembleuser_id = '$ensembleUser'");
        $conn->query("INSERT INTO ensembles (ensemblename, ensembleauthor, ensembleuser_id, ensemblejson) VALUES ('$ensembleName', '$ensembleAuthor', '$ensembleUser', '$ensembleJSON')");
        echo "setEnsemble_ok";
        break;
    case "deleteEnsemble":
        $ensembleName = $_POST["ensemblename"];
        $ensembleAuthor = $_POST["ensembleauthor"];
        $conn->query("DELETE FROM ensembles WHERE ensemblename = '$ensembleName' AND ensembleauthor = '$ensembleAuthor' AND ensembleuser_id = '$ensembleUser'");

        // Now return the ensemble list again to populate the dialog
        // Local User first
        $result = $conn->query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id = '$ensembleUser'");
        while ($row = $result->fetch_assoc()) {
            $user = $row["ensembleuser_id"];
            $resultUser = $conn->query("SELECT email FROM users WHERE id = '$user'");
            $user = $resultUser->fetch_row();
            $user = explode("@", $user[0], 2);
            $user = $user[0]; // user without the @...
            echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
        }
        // All Users then
        $result = $conn->query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id != '$ensembleUser'");
        while ($row = $result->fetch_assoc()) {
            $user = $row["ensembleuser_id"];
            $resultUser = $conn->query("SELECT email FROM users WHERE id = '$user'");
            $user = $resultUser->fetch_row();
            $user = explode("@", $user[0], 2);
            $user = $user[0]; // user without the @...
            echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
        }
        break;
    case "getEnsemble":
        $ensembleName = $_POST["ensemblename"];
        $ensembleAuthor = $_POST["ensembleauthor"];
        $ensembleUser = $_POST["ensembleuser"];
        $resultUser = $conn->query("SELECT id FROM users WHERE email LIKE '$ensembleUser%'");
        $ensembleUserId = $resultUser->fetch_row();
        $ensembleUserId = $ensembleUserId[0];
        $result = $conn->query("SELECT ensemblejson FROM ensembles WHERE ensemblename = '$ensembleName' AND ensembleauthor = '$ensembleAuthor' AND ensembleuser_id = '$ensembleUserId'");
        $ensembleJSON = $result->fetch_row();
        echo $ensembleJSON[0];
        break;
    case "getEnsembleList":
        // Local User first
        $result = $conn->query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id = '$ensembleUser'");
        while ($row = $result->fetch_assoc()) {
            $user = $row["ensembleuser_id"];
            $resultUser = $conn->query("SELECT email FROM users WHERE id = '$user'");
            $user = $resultUser->fetch_row();
            $user = explode("@", $user[0], 2);
            $user = $user[0]; // user without the @...
            echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
        }
        // All Users then
        $result = $conn->query("SELECT ensemblename, ensembleauthor, ensembleuser_id FROM ensembles WHERE ensembleuser_id != '$ensembleUser'");
        while ($row = $result->fetch_assoc()) {
            $user = $row["ensembleuser_id"];
            $resultUser = $conn->query("SELECT email FROM users WHERE id = '$user'");
            $user = $resultUser->fetch_row();
            $user = explode("@", $user[0], 2);
            $user = $user[0]; // user without the @...
            echo $row["ensemblename"]."_by_".$row["ensembleauthor"]."_u_".$user."+";
        }
        break;
    default:
        break;
}

// Register User Activity
$conn->query("INSERT INTO log (username, ensemblename, ensembleauthor, action) VALUES ('$userName', '$ensembleName', '$ensembleAuthor', '$action')");

// Close connection
$conn->close();
?>
